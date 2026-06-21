## 🎁 What's new

Bank 1.10.0 is a modernisation release. The addon now targets **Java 21, Paper 1.21.11 and BentoBox 3.14.0**, and its entire locale set has been migrated to BentoBox's modern **MiniMessage** colour format. Alongside the platform work, this release adds a brand-new `latest_transaction` placeholder, ships a full set of language files (including a new Russian translation), and rebuilds the test suite on JUnit 5 + MockBukkit.

Because of the platform and locale-format changes, this is not a drop-in update — please read the **Updating** notes below before installing.

## ✨ Highlights

### 🔺 Platform modernisation — Java 21, Paper 1.21.11, BentoBox 3.14.0 (#65)
- Build upgraded to **Java 21**, **Paper 1.21.11** and **BentoBox 3.14.0**
- `plugin.yml` `api-version` bumped to **1.21**
- Test suite migrated to **JUnit 5 + MockBukkit**
- All Maven plugins updated to the latest stable versions and ~120 SonarCloud issues resolved (complexity, variable shadowing, test smells)

### 🔡 🔺 MiniMessage locale format (#64)
- All locale files converted from legacy `&`/`§` colour codes to BentoBox's **MiniMessage** format
- Aligns Bank with the rest of the 3.14.0 ecosystem and unlocks richer text formatting
- Any custom locale edits you've made will need to be re-expressed in MiniMessage syntax

### 🔡 New transaction placeholder (#61)
- Adds `{gamemode}_latest_transaction`, showing a user's most recent island bank transaction
- Renders as `[Username] [TxType] $[Amount]` (e.g. `tastybento Deposited $500.0`)
- The placeholder text is fully localised

### 🔡 Complete language coverage (#63)
- Adds a new **Russian** locale plus every other language file BentoBox ships, so Bank now matches the full BentoBox locale set (23 languages)

### 🐛 Hardening
- Hardened bank transaction-history parsing against malformed entries (#66)
- Localised the latest-transaction placeholder fallback text (#66)
- Numerous code-quality and safety fixes flagged by static analysis (#66)

## ⚙️ Compatibility

✔️ BentoBox API 3.14.0
✔️ Minecraft 1.21.5 - 26.1.x
✔️ Java 21

## 🔺 Updating — important notes

🔺 **BentoBox 3.14.0 and Java 21 are required.** Update BentoBox first and make sure your server runs Java 21 before installing this version.

🔡 🔺 **Locale files were migrated to MiniMessage.** If you have customised any Bank language files, back them up and re-apply your changes in MiniMessage format. The simplest path is to delete the old locale files and let the addon regenerate them, then redo your edits.

🔡 **New placeholder available.** `{gamemode}_latest_transaction` can be used wherever PlaceholderAPI placeholders are supported (e.g. scoreboards, holograms).

## 📥 How to update
1. Stop the server
2. Back up your BentoBox folder (especially any customised Bank locale files)
3. Update BentoBox to 3.14.0 and confirm the server is running Java 21
4. Drop the new Bank jar into the `addons` folder and remove the old one
5. Start the server, then re-apply any custom locale edits in MiniMessage format
6. You should be good to go!

## Legend

* 🔡 locale files may need to be regenerated or updated
* ⚙️ config options have been removed, renamed, or added
* 🔺 special attention needed

## What's Changed

* 🔡 Add latest transaction placeholder by @tastybento in https://github.com/BentoBoxWorld/Bank/pull/61
* 🔡 Add Russian locale and all missing BentoBox languages by @tastybento in https://github.com/BentoBoxWorld/Bank/pull/63
* 🔡 🔺 Convert locale color codes to MiniMessage format by @tastybento in https://github.com/BentoBoxWorld/Bank/pull/64
* 🔺 Modernise to Java 21 / Paper 1.21.11 / BentoBox 3.14.0 by @tastybento in https://github.com/BentoBoxWorld/Bank/pull/65
* 🔡 Release 1.10.0 — harden history parsing, localise placeholder, code-quality fixes by @tastybento in https://github.com/BentoBoxWorld/Bank/pull/66

**Full Changelog**: https://github.com/BentoBoxWorld/Bank/compare/1.9.1...1.10.0
