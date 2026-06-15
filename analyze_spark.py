#!/usr/bin/env python3
"""Quick-and-dirty spark .sparkprofile analyzer.

Walks the binary protobuf, rebuilds each thread's stack-trace tree, and prints
the top self-time methods per thread plus a roll-up by mod (inferred from the
top-level package).
"""

from __future__ import annotations

import struct
import sys
from collections import defaultdict
from pathlib import Path


def varint(buf: bytes, pos: int) -> tuple[int, int]:
    r, s = 0, 0
    while True:
        b = buf[pos]
        pos += 1
        r |= (b & 0x7F) << s
        if not (b & 0x80):
            return r, pos
        s += 7


def fields(buf: bytes, start: int, end: int):
    pos = start
    while pos < end:
        tag, pos = varint(buf, pos)
        fn = tag >> 3
        wt = tag & 7
        if wt == 0:
            v, pos = varint(buf, pos)
            yield fn, wt, v
        elif wt == 1:
            v = struct.unpack_from("<Q", buf, pos)[0]
            pos += 8
            yield fn, wt, v
        elif wt == 2:
            ln, pos = varint(buf, pos)
            yield fn, wt, (pos, pos + ln)
            pos += ln
        elif wt == 5:
            v = struct.unpack_from("<I", buf, pos)[0]
            pos += 4
            yield fn, wt, v
        else:
            raise ValueError(f"unsupported wire {wt}")


def packed_doubles(buf, start, end):
    n = (end - start) // 8
    return list(struct.unpack_from(f"<{n}d", buf, start))


def packed_int32(buf, start, end):
    out, pos = [], start
    while pos < end:
        v, pos = varint(buf, pos)
        out.append(v)
    return out


def parse_stn(buf, start, end):
    node = {"class": "", "method": "", "times": [], "refs": []}
    for fn, wt, val in fields(buf, start, end):
        if fn == 3 and wt == 2:
            s, e = val
            node["class"] = buf[s:e].decode("utf-8", "replace")
        elif fn == 4 and wt == 2:
            s, e = val
            node["method"] = buf[s:e].decode("utf-8", "replace")
        elif fn == 8 and wt == 2:
            s, e = val
            node["times"] = packed_doubles(buf, s, e)
        elif fn == 8 and wt == 1:
            node["times"].append(struct.unpack("<d", struct.pack("<Q", val))[0])
        elif fn == 9 and wt == 2:
            s, e = val
            node["refs"] = packed_int32(buf, s, e)
        elif fn == 9 and wt == 0:
            node["refs"].append(val)
    return node


def parse_thread(buf, start, end):
    t = {"name": "", "nodes": [], "times": [], "roots": []}
    for fn, wt, val in fields(buf, start, end):
        if fn == 1 and wt == 2:
            s, e = val
            t["name"] = buf[s:e].decode("utf-8", "replace")
        elif fn == 3 and wt == 2:
            s, e = val
            t["nodes"].append(parse_stn(buf, s, e))
        elif fn == 4 and wt == 2:
            s, e = val
            t["times"] = packed_doubles(buf, s, e)
        elif fn == 4 and wt == 1:
            t["times"].append(struct.unpack("<d", struct.pack("<Q", val))[0])
        elif fn == 5 and wt == 2:
            s, e = val
            t["roots"] = packed_int32(buf, s, e)
        elif fn == 5 and wt == 0:
            t["roots"].append(val)
    return t


MOD_PACKAGES = {
    "net.minecraft": "vanilla",
    "net.neoforged": "neoforge",
    "com.mojang": "vanilla",
    "com.simibubi.create": "create",
    "com.jozufozu.flywheel": "flywheel",
    "dev.engine_room.flywheel": "flywheel",
    "com.lcm.lithostitched": "lithostitched",
    "com.github.lithostitched": "lithostitched",
    "lithostitched": "lithostitched",
    "com.starfish_studios": "terralith?",
    "raccoonman.reterraforged": "terralith?",
    "dev.dubhe.curtains": "?",
    "com.terraformersmc": "terralith?",
    "com.kqp.terraforged": "tectonic",
    "com.dfsek.terra": "terra",
    "appeng": "ae2",
    "rearth.oritech": "?",
    "dev.engine_room": "flywheel",
    "dev.ftb": "ftb",
    "com.minecolonies": "minecolonies",
    "com.ldtteam": "minecolonies",
    "rbasamoyai.createbigcannons": "createbigcannons",
    "com.rbasamoyai": "createbigcannons",
    "dan200.computercraft": "cc-tweaked",
    "site.siredvin": "advancedperipherals",
    "de.srendi": "advancedperipherals",
    "fuzs": "puzzleslib?",
    "twilightforest": "twilightforest",
    "biomesoplenty": "biomesoplenty",
    "terraformersmc": "terralith?",
    "potionstudios": "yetanotherconfiglib?",
    "vazkii": "?",
    "dev.shadowsoffire": "?",
    "io.netty": "netty",
    "java.": "jdk",
    "javax.": "jdk",
    "jdk.": "jdk",
    "sun.": "jdk",
    "org.spongepowered.asm": "mixin",
    "skygrad": "skygrad",
    "com.skygrad": "skygrad",
    "redgalaxy": "?",
    "io.papermc": "paper",
    "com.aizistral": "?",
    "ovh.corail": "?",
}


def attribute_mod(cls: str) -> str:
    for prefix, mod in sorted(MOD_PACKAGES.items(), key=lambda kv: -len(kv[0])):
        if cls.startswith(prefix):
            return mod
    parts = cls.split(".")
    return ".".join(parts[:2]) if len(parts) >= 2 else cls


def total_time(node):
    return sum(node["times"])


def compute_self_times(thread):
    nodes = thread["nodes"]
    self_t = [0.0] * len(nodes)
    for i, n in enumerate(nodes):
        tt = total_time(n)
        ct = sum(total_time(nodes[r]) for r in n["refs"] if 0 <= r < len(nodes))
        self_t[i] = max(0.0, tt - ct)
    return self_t


def thread_total_time(thread):
    nodes = thread["nodes"]
    roots = thread["roots"] or list(range(len(nodes)))
    return sum(total_time(nodes[r]) for r in roots if 0 <= r < len(nodes))


def main():
    path = Path(sys.argv[1])
    data = path.read_bytes()
    print(f"file: {path}  size: {len(data):,} bytes")

    threads = []
    for fn, wt, val in fields(data, 0, len(data)):
        if fn == 2 and wt == 2:
            s, e = val
            threads.append(parse_thread(data, s, e))

    print(f"threads: {len(threads)}")
    threads.sort(key=thread_total_time, reverse=True)

    overall_total = sum(thread_total_time(t) for t in threads)
    print(f"\ntotal sampled time across all threads: {overall_total:,.0f} ms")
    print("\n=== Per-thread totals (top 20) ===")
    for t in threads[:20]:
        tt = thread_total_time(t)
        pct = (tt / overall_total * 100) if overall_total else 0
        print(f"  {tt:>14,.0f} ms ({pct:5.1f}%)  {t['name']}  ({len(t['nodes'])} nodes)")

    # Focus on threads of interest
    keywords = ("Server", "Worldgen", "Worker", "Chunk", "Render", "main")
    print("\n=== Detail per relevant thread ===")
    for t in threads:
        name = t["name"]
        if not any(k.lower() in name.lower() for k in keywords):
            continue
        tt = thread_total_time(t)
        if tt < 1000:
            continue
        print(f"\n--- {name} ({tt:,.0f} ms, {len(t['nodes'])} nodes) ---")

        self_t = compute_self_times(t)

        # Top self-time methods
        ranked = sorted(range(len(t["nodes"])), key=lambda i: self_t[i], reverse=True)
        print("  top self-time methods:")
        for i in ranked[:15]:
            n = t["nodes"][i]
            if self_t[i] < tt * 0.005:
                break
            print(f"    {self_t[i]:>10,.0f} ms  {n['class']}.{n['method']}")

        # Mod roll-up by self time
        by_mod = defaultdict(float)
        for i, n in enumerate(t["nodes"]):
            by_mod[attribute_mod(n["class"])] += self_t[i]
        print("  by mod (self time):")
        for mod, tm in sorted(by_mod.items(), key=lambda kv: -kv[1])[:12]:
            pct = (tm / tt * 100) if tt else 0
            print(f"    {tm:>10,.0f} ms ({pct:5.1f}%)  {mod}")


if __name__ == "__main__":
    main()
