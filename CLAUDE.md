# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Bank is a BentoBox addon for Minecraft (Spigot) that provides an island-based banking/economy system. It integrates with Vault for economy operations and supports multiple BentoBox game modes (BSkyBlock, AOneBlock, AcidIsland, SkyGrid, CaveBlock).

## Build Commands

```bash
mvn clean package          # Build the plugin JAR
mvn test                   # Run tests only
mvn verify                 # Build + tests + coverage
```

Output JAR goes to `target/`. The build requires Java 21+.

## Testing

- JUnit 5 (Jupiter) with Mockito (incl. static mocking) and MockBukkit
- Run a single test: `mvn test -Dtest=BankManagerTest`
- Tests are in `src/test/java/world/bentobox/bank/` mirroring main source structure
- JaCoCo coverage reports: `target/site/jacoco/`

## Architecture

**Entry point:** `BankPladdon` → `Bank` (addon lifecycle: onEnable loads Vault, config, BankManager, registers commands with each game mode)

**Core layers:**
- **BankManager** — Central business logic: deposit/withdraw/balance operations, compound interest calculation, LRU cache (max 20 accounts), async database persistence. Listens for island deletion events to clean up accounts.
- **Money** — Value type wrapping `BigDecimal` with 2 decimal places. All currency arithmetic goes through this class.
- **BankAccounts** — Persisted data object per island (balance, transaction history, interest timestamp). Uses BentoBox's database layer.
- **Settings** — Maps to `config.yml`. Interest rate, compound period, cooldown, game modes, command names.

**Command hierarchy:**
- `commands/user/` — Player commands (balance, deposit, withdraw, statement, baltop) registered under each game mode's island command
- `commands/admin/` — Admin commands (balance, give, set, take, statement) with AbstractAdminBankCommand handling target player resolution
- Both extend `AbstractBankCommand` which provides shared argument parsing

**PhManager** — Registers BentoBox placeholders for each game mode (balance, top-N rankings). Caches top-10 for 10 seconds.

## Key Dependencies (provided at runtime, not bundled)

- Paper API 1.21.x
- BentoBox 3.14.0-SNAPSHOT
- Vault API 1.7

## Localization

23 language files in `src/main/resources/locales/`, matching BentoBox's full locale set (cs, de, en-US, es, fr, hr, hu, id, it, ja, ko, lv, nl, pl, pt, pt-BR, ro, ru, tr, uk, vi, zh-CN, zh-HK). `en-US.yml` is the reference; all other files must carry the same keys. Message keys are referenced in commands via BentoBox's `user.sendMessage()` system. Use the `/sync-locales` skill to find and fill missing keys.

## CI

GitHub Actions (`.github/workflows/build.yml`): triggers on push to `develop` and PRs. Runs Maven verify with SonarCloud analysis using Java 21.

## Dependency Source Lookup

When you need to inspect source code for a dependency (e.g., BentoBox, addons):

1. **Check local Maven repo first**: `~/.m2/repository/` — sources jars are named `*-sources.jar`
2. **Check the workspace**: Look for sibling directories or Git submodules that may contain the dependency as a local project (e.g., `../bentoBox`, `../addon-*`)
3. **Check Maven local cache for already-extracted sources** before downloading anything
4. Only download a jar or fetch from the internet if the above steps yield nothing useful

Prefer reading `.java` source files directly from a local Git clone over decompiling or extracting a jar.

In general, the latest version of BentoBox should be targeted.

## Project Layout

Related projects are checked out as siblings under `~/git/`:

**Core:**
- `bentobox/` — core BentoBox framework

**Game modes:**
- `addon-acidisland/` — AcidIsland game mode
- `addon-bskyblock/` — BSkyBlock game mode
- `Boxed/` — Boxed game mode (expandable box area)
- `CaveBlock/` — CaveBlock game mode
- `OneBlock/` — AOneBlock game mode
- `SkyGrid/` — SkyGrid game mode
- `RaftMode/` — Raft survival game mode
- `StrangerRealms/` — StrangerRealms game mode
- `Brix/` — plot game mode
- `parkour/` — Parkour game mode
- `poseidon/` — Poseidon game mode
- `gg/` — gg game mode

**Addons:**
- `addon-level/` — island level calculation
- `addon-challenges/` — challenges system
- `addon-welcomewarpsigns/` — warp signs
- `addon-limits/` — block/entity limits
- `addon-invSwitcher/` / `invSwitcher/` — inventory switcher
- `addon-biomes/` / `Biomes/` — biomes management
- `Bank/` — island bank
- `Border/` — world border for islands
- `Chat/` — island chat
- `CheckMeOut/` — island submission/voting
- `ControlPanel/` — game mode control panel
- `Converter/` — ASkyBlock to BSkyBlock converter
- `DimensionalTrees/` — dimension-specific trees
- `discordwebhook/` — Discord integration
- `Downloads/` — BentoBox downloads site
- `DragonFights/` — per-island ender dragon fights
- `ExtraMobs/` — additional mob spawning rules
- `FarmersDance/` — twerking crop growth
- `GravityFlux/` — gravity addon
- `Greenhouses-addon/` — greenhouse biomes
- `IslandFly/` — island flight permission
- `IslandRankup/` — island rankup system
- `Likes/` — island likes/dislikes
- `Limits/` — block/entity limits
- `lost-sheep/` — lost sheep adventure
- `MagicCobblestoneGenerator/` — custom cobblestone generator
- `PortalStart/` — portal-based island start
- `pp/` — pp addon
- `Regionerator/` — region management
- `Residence/` — residence addon
- `TopBlock/` — top ten for OneBlock
- `TwerkingForTrees/` — twerking tree growth
- `Upgrades/` — island upgrades (Vault)
- `Visit/` — island visiting
- `weblink/` — web link addon
- `CrowdBound/` — CrowdBound addon

**Data packs:**
- `BoxedDataPack/` — advancement datapack for Boxed

**Documentation & tools:**
- `docs/` — main documentation site
- `docs-chinese/` — Chinese documentation
- `docs-french/` — French documentation
- `BentoBoxWorld.github.io/` — GitHub Pages site
- `website/` — website
- `translation-tool/` — translation tool

Check these for source before any network fetch.

## Key Dependencies (source locations)

- `world.bentobox:bentobox` → `~/git/bentobox/src/`
