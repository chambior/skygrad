#!/usr/bin/env python3
"""Add a Modrinth mod to modrinth.index.json.

Usage:
    python add_mod.py <modrinth-url> [--version VERSION] [--dry-run]

The URL can be any Modrinth project page, e.g.
    https://modrinth.com/mod/jei
    https://modrinth.com/mod/jei/version/19.27.0.340

The script reads the modpack's Minecraft + loader from the existing
modrinth.index.json "dependencies" block, queries the Modrinth API for a
matching version, and appends a new entry to files[] while preserving the
file's existing compact-inline formatting.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

INDEX_PATH = Path(__file__).resolve().parent / "modrinth.index.json"
API = "https://api.modrinth.com/v2"
USER_AGENT = "skygrad-modpack/add_mod.py (github.com/chambior/skygrad)"


def http_get_json(url: str) -> object:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    try:
        with urllib.request.urlopen(req) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        sys.exit(f"HTTP {e.code} from {url}: {e.read().decode('utf-8', 'replace')}")
    except urllib.error.URLError as e:
        sys.exit(f"Network error fetching {url}: {e.reason}")


def parse_url(url: str) -> tuple[str, str | None]:
    m = re.search(r"modrinth\.com/[^/]+/([^/?#]+)", url)
    if not m:
        sys.exit(f"Could not extract project slug from URL: {url}")
    slug = m.group(1)
    vm = re.search(r"/version/([^/?#]+)", url)
    return slug, (vm.group(1) if vm else None)


def pick_version(versions: list[dict], mc: str, loader: str, hint: str | None) -> dict:
    if hint:
        for v in versions:
            if v["id"] == hint or v["version_number"] == hint:
                return v
        sys.exit(f"Version {hint!r} not found in project's published versions.")
    matching = [
        v for v in versions
        if mc in v.get("game_versions", []) and loader in v.get("loaders", [])
    ]
    if not matching:
        sys.exit(f"No version matches Minecraft {mc} + loader {loader}.")
    # /version returns newest first.
    return matching[0]


def primary_file(version: dict) -> dict:
    for f in version["files"]:
        if f.get("primary"):
            return f
    return version["files"][0]


def env_from_project(project: dict) -> dict:
    def m(side: str) -> str:
        return "required" if side in ("required", "optional") else "unsupported"
    return {
        "client": m(project.get("client_side", "required")),
        "server": m(project.get("server_side", "required")),
    }


def format_entry(entry: dict) -> str:
    """Reproduce the compact inline style used by the existing entries."""
    hashes = json.dumps(entry["hashes"], separators=(", ", ": "))
    env = json.dumps(entry["env"], separators=(", ", ": "))
    downloads = json.dumps(entry["downloads"], separators=(", ", ": "))
    return (
        "    {\n"
        f'      "path": {json.dumps(entry["path"])},\n'
        f'      "hashes": {hashes},\n'
        f'      "env": {env},\n'
        f'      "downloads": {downloads},\n'
        f'      "fileSize": {entry["fileSize"]}\n'
        "    }"
    )


def main() -> None:
    p = argparse.ArgumentParser(description="Add a Modrinth mod to modrinth.index.json.")
    p.add_argument("url", help="Modrinth project URL")
    p.add_argument("--version", help="Specific version id or version_number to install")
    p.add_argument("--env-client", choices=["required", "optional", "unsupported"],
                   help="Override the client env flag")
    p.add_argument("--env-server", choices=["required", "optional", "unsupported"],
                   help="Override the server env flag")
    p.add_argument("--dry-run", action="store_true", help="Print the entry without writing")
    args = p.parse_args()

    if not INDEX_PATH.exists():
        sys.exit(f"{INDEX_PATH} not found")

    text = INDEX_PATH.read_text(encoding="utf-8")
    index = json.loads(text)

    deps = index.get("dependencies", {})
    mc = deps.get("minecraft")
    loader = next((k for k in deps if k != "minecraft"), None)
    if not mc or not loader:
        sys.exit('dependencies must include "minecraft" and a loader')

    slug, version_hint = parse_url(args.url)
    if args.version:
        version_hint = args.version

    print(f"Resolving '{slug}' against mc={mc} loader={loader}...")
    project = http_get_json(f"{API}/project/{slug}")

    query = urllib.parse.urlencode({
        "game_versions": json.dumps([mc]),
        "loaders": json.dumps([loader]),
    })
    versions = http_get_json(f"{API}/project/{project['slug']}/version?{query}")
    if not versions and version_hint:
        # Falling back to unfiltered listing so an explicit --version still works
        # even when its loader tag doesn't match.
        versions = http_get_json(f"{API}/project/{project['slug']}/version")

    version = pick_version(versions, mc, loader, version_hint)
    f = primary_file(version)

    env = env_from_project(project)
    if args.env_client:
        env["client"] = args.env_client
    if args.env_server:
        env["server"] = args.env_server

    entry = {
        "path": f"mods/{f['filename']}",
        "hashes": {
            "sha1": f["hashes"]["sha1"],
            "sha512": f["hashes"]["sha512"],
        },
        "env": env,
        "downloads": [f["url"]],
        "fileSize": f["size"],
    }

    print(f"  -> {project['title']} v{version['version_number']} ({f['filename']})")
    print(f"     env client={env['client']} server={env['server']} size={f['size']:,}")

    for existing in index["files"]:
        if existing["path"] == entry["path"]:
            sys.exit(f"Already present at path {entry['path']}")
        if existing.get("hashes", {}).get("sha1") == entry["hashes"]["sha1"]:
            sys.exit(f"Already present with same sha1 at {existing['path']}")

    formatted = format_entry(entry)

    if args.dry_run:
        print("\n--- dry-run, would append: ---")
        print(formatted)
        return

    anchor = "\n  ]\n}"
    idx = text.rfind(anchor)
    if idx == -1:
        # Fallback: full rewrite (will reflow the whole file).
        index["files"].append(entry)
        INDEX_PATH.write_text(
            json.dumps(index, indent=2, ensure_ascii=False) + "\n",
            encoding="utf-8",
        )
        print(f"Appended {entry['path']} (file reformatted; layout differs from original).")
        return

    new_text = text[:idx] + ",\n" + formatted + text[idx:]
    INDEX_PATH.write_text(new_text, encoding="utf-8")
    print(f"Appended {entry['path']} to {INDEX_PATH.name}.")


if __name__ == "__main__":
    main()
