# Skygrad Modrinth Modpack

## Layout
- `modrinth.index.json` — pack manifest (Modrinth `.mrpack` format v1)
- `overrides/` — files copied verbatim into the instance root
  - `overrides/mods/skygrad-1.1.0.jar` — the Skygrad mod, bundled directly
  - `overrides/config/ftbquests/` — quest data

## Mods in the manifest
73 mods, downloaded from Modrinth at install time. See `modrinth.index.json`.
Refreshed 2026-07-12 against MC 1.21.1 / NeoForge 21.1.228.

Distant Horizons provides LOD / far-render terrain natively on NeoForge.
No Fabric compatibility layer is used: Sinytra Connector, Forgified Fabric API,
and Voxy have been removed.

## Sideloaded mods (in `overrides/mods/`)
Not on Modrinth (CurseForge / build-server only), so shipped inside the `.mrpack`:
- `skygrad-1.1.0.jar` — refreshed from the build by `packageModpack`
- `ftb-library-neoforge-2101.1.32.jar`
- `ftb-quests-neoforge-2101.1.27.jar`
- `ftb-teams-neoforge-2101.1.10.jar`
- `framework-neoforge-1.21.1-0.13.20.jar`
- `goblintraders-neoforge-1.21.1-1.11.2.jar`
- `blockui-1.0.211-1.21.1-snapshot.jar`
- `domum-ornamentum-1.0.233-snapshot-main.jar`
- `minecolonies-1.1.1346-1.21.1-snapshot.jar`
- `structurize-1.0.823-1.21.1-snapshot.jar`
- `multipiston-1.2.58-1.21.1.jar`
- `stylecolonies-1.15.54-1.21.1.jar`
- `jadecolonies-1.21.1-1.6.0.jar`
- `createcolonies-2.0.4.jar`
- `createmoredrillheads-2.1.0-1.21.1.jar`
- `Colony4ComputerCraft-1.21.1-2.8.2.jar`
- `ae2jeiintegration-1.2.1.jar`
- `morered-1.21.1-6.0.0.3.jar`
- `towntalk-1.2.0.jar`

When updating, replace the jar files in `overrides/mods/` — no manifest change needed.
The MineColonies core set (minecolonies / structurize / blockui / domum-ornamentum)
uses tightly-matched snapshot builds; update them together, not individually.

## Refreshing a manifest entry
Hit `https://api.modrinth.com/v2/project/<slug>/version?loaders=[%22neoforge%22]&game_versions=[%221.21.1%22]`
and copy `files[0]`'s `url`, `size`, `hashes.sha1`, and `hashes.sha512`.

## Building the .mrpack
A `.mrpack` is just a zip with `modrinth.index.json` at the root:

```powershell
Compress-Archive -Path modrinth.index.json,overrides -DestinationPath skygrad-1.1.0.zip -Force
Rename-Item skygrad-1.1.0.zip skygrad-1.1.0.mrpack
```

## Notes
- Skygrad's own jar is shipped via `overrides/mods/` rather than a manifest entry,
  so the pack works before Skygrad is published to Modrinth.
- Once Skygrad is on Modrinth, move it from `overrides/mods/` into `files[]` to get
  per-mod versioning.
