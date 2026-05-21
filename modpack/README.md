# Skygrad Modrinth Modpack

## Layout
- `modrinth.index.json` — pack manifest (Modrinth `.mrpack` format v1)
- `overrides/` — files copied verbatim into the instance root
  - `overrides/mods/skygrad-0.1.1.jar` — the Skygrad mod, bundled directly
  - `overrides/config/ftbquests/` — quest data

## Mods in the manifest
60 mods, downloaded from Modrinth at install time. See `modrinth.index.json`.
Snapshot taken 2026-05-21 against MC 1.21.1 / NeoForge 21.1.228.

Excluded by request: Sinytra Connector, Forgified Fabric API, Voxy.

## Sideloaded mods (in `overrides/mods/`)
Not on Modrinth, so shipped inside the `.mrpack`:
- `skygrad-0.1.1.jar`
- `ftb-library-neoforge-2101.1.31.jar`
- `ftb-quests-neoforge-2101.1.24.jar`
- `ftb-teams-neoforge-2101.1.10.jar`
- `blockui-1.0.211-1.21.1-snapshot.jar`
- `domum-ornamentum-1.0.233-snapshot-main.jar`
- `framework-neoforge-1.21.1-0.13.11.jar`
- `goblintraders-neoforge-1.21.1-1.11.2.jar`
- `minecolonies-1.1.1307-1.21.1-snapshot.jar`
- `multipiston-1.2.58-1.21.1.jar`
- `structurize-1.0.823-1.21.1-snapshot.jar`
- `towntalk-1.2.0.jar`

When updating, replace the jar files in `overrides/mods/` — no manifest change needed.

## Refreshing a manifest entry
Hit `https://api.modrinth.com/v2/project/<slug>/version?loaders=[%22neoforge%22]&game_versions=[%221.21.1%22]`
and copy `files[0]`'s `url`, `size`, `hashes.sha1`, and `hashes.sha512`.

## Building the .mrpack
A `.mrpack` is just a zip with `modrinth.index.json` at the root:

```powershell
Compress-Archive -Path modrinth.index.json,overrides -DestinationPath skygrad-0.1.1.zip -Force
Rename-Item skygrad-0.1.1.zip skygrad-0.1.1.mrpack
```

## Notes
- Skygrad's own jar is shipped via `overrides/mods/` rather than a manifest entry,
  so the pack works before Skygrad is published to Modrinth.
- Once Skygrad is on Modrinth, move it from `overrides/mods/` into `files[]` to get
  per-mod versioning.
