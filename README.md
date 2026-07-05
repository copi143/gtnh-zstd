# gtnh-zstd

[![](https://jitpack.io/v/copi143/gtnh-zstd.svg)](https://jitpack.io/#copi143/gtnh-zstd)
[![](https://github.com/copi143/gtnh-zstd/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/copi143/gtnh-zstd/actions/workflows/build-and-test.yml)

A Minecraft 1.7.10 Forge mod that replaces GZip/Deflate compression with [Zstandard (zstd)](https://facebook.github.io/zstd/) for improved compression ratios and performance.

## Features

- **Zstd region files** — `.mca` anvil files store chunk data with zstd instead of Deflate
- **Zstd NBT** — all `CompressedStreamTools` read/write operations (`.dat` files) use zstd
- **Backward compatible** — detects compression format automatically (zstd/gzip/deflate) via magic bytes
- **Mod compatibility** — patches BetterQuesting and VendingMachine packet assembly to use zstd-aware NBT reads
- **Configurable** — choose compression mode (zstd/gzip/deflate) and compression level (-7 to 22) ingame

## Dependencies

- [UniMixins](https://github.com/LegacyModdingMC/UniMixins) — required
- [BetterQuesting](https://github.com/GTNewHorizons/BetterQuesting) 3.8.71-GTNH+ — optional, for packet assembly compat
- [VendingMachine](https://github.com/GTNewHorizons/VendingMachine) 0.4.95+ — optional, for packet assembly compat
- [zstd-jni](https://github.com/luben/zstd-jni) 1.5.7-7 — bundled (shadowed) into the mod jar

## Build

```bash
./gradlew setupDecompWorkspace
./gradlew build
```

The compiled jar will be at `build/libs/gtnh-zstd-<version>.jar`.

## Installation

Place the jar in the `mods/` folder of a Minecraft 1.7.10 instance with Forge 10.13.4.1614 (or compatible). Ensure [UniMixins](https://github.com/LegacyModdingMC/UniMixins) is also installed.

## Configuration

A config file `config/gtnh-zstd.cfg` is created on first run:

| Option | Default | Description |
|---|---|---|
| `compressionMode` | `zstd` | Output format: `zstd`, `gzip`, `deflate`, or `default` |
| `zstdCompressionLevel` | `3` | Zstd level: -7 (fastest) to 22 (max compression) |

## How it works

The mod uses [Mixin](https://github.com/SpongePowered/Mixin) to overwrite key compression paths at runtime:

- **`RegionFile`** — chunk read/write streams are replaced with zstd wrappers; introduces version byte `3` for zstd-format chunks (vanilla version `2` is Deflate)
- **`CompressedStreamTools`** — all NBT read/write methods are redirected through zstd; `safeWrite` is made atomic (write to `.tmp` then move)
- **BetterQuesting / VendingMachine** — packet deserialization paths use the patched `CompressedStreamTools`

The `Compressed` utility auto-detects the compression format from the first 4 bytes (`28 B5 2F FD` = zstd, `1F 8B 08` = gzip, others fall through to deflate detection).

## License

MIT — see [LICENSE](LICENSE).
