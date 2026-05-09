#!/usr/bin/env python3

import os
import sys

REPLACEMENTS = {
    '"item": "tfmg:heavy_plate"': '"tag": "c:plates/steel"',
    '"item": "tfmg:steel_ingot"': '"tag": "c:ingots/steel"',
    '"item": "tfmg:steel_nugget"': '"tag": "c:nuggets/steel"',
    '"item": "tfmg:heavy_plate"': '"tag": "c:plates/steel"',
    '"id": "tfmg:steel_ingot"': '"id": "create_ironworks:steel_ingot"',
    '"id": "tfmg:steel_block"': '"id": "create_ironworks:steel_block"',
    '"id": "tfmg:heavy_plate"': '"id": "create_ironworks:steel_sheet"',
    '"id": "tfmg:steel_nugget"': '"id": "create_ironworks:steel_nugget"',
}

def process_file(filepath):
    try:
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()

        modified = False
        total_count = 0

        for search, replace in REPLACEMENTS.items():
            count = content.count(search)

            if count > 0:
                content = content.replace(search, replace)
                modified = True
                total_count += count

        if modified:
            with open(filepath, "w", encoding="utf-8", newline="\n") as f:
                f.write(content)

            print(f"[MODIFIED] {filepath} ({total_count} replacement(s))")
            return False
        else:
            os.remove(filepath)
            print(f"[DELETED FILE] {filepath}")
            return True

    except Exception as e:
        print(f"[ERROR] {filepath}: {e}")
        return False


def remove_empty_directories(root):
    for current_root, dirs, files in os.walk(root, topdown=False):
        if not dirs and not files:
            try:
                os.rmdir(current_root)
                print(f"[DELETED DIR] {current_root}")
            except Exception as e:
                print(f"[ERROR] Could not delete directory {current_root}: {e}")


def process_directory(root):
    for current_root, dirs, files in os.walk(root):
        for filename in files:
            filepath = os.path.join(current_root, filename)
            process_file(filepath)

    remove_empty_directories(root)


def main():
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <folder>")
        sys.exit(1)

    root = sys.argv[1]

    if not os.path.isdir(root):
        print(f"Error: '{root}' is not a directory")
        sys.exit(1)

    process_directory(root)


if __name__ == "__main__":
    main()