# Bank
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_Bank&metric=bugs)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_Bank)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_Bank&metric=coverage)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_Bank)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_Bank&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_Bank)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_Bank&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_Bank)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=BentoBoxWorld_Bank&metric=security_rating)](https://sonarcloud.io/dashboard?id=BentoBoxWorld_Bank)
[![Build Status](https://ci.codemc.io/job/BentoBoxWorld/job/Bank/badge/icon)](https://ci.codemc.io/job/BentoBoxWorld/job/Bank/)
[![gitlocalized ](https://gitlocalize.com/repo/5451/whole_project/badge.svg)](https://gitlocalize.com/repo/5451/whole_project?utm_source=badge)

**Bank** provides an **island bank** to enable island members to share money.

Created and maintained by [tastybento](https://github.com/tastybento).

## Introduction

Each island has a bank account. Players can deposit or withdraw money from their regular economy accounts into the island account where it is pooled. The island owner can decide which rank of team member can access the account via the settings menu. There is a `baltop` command that players can use to see which island has the most, or least money. 

<img src=https://github.com/BentoBoxWorld/Bank/assets/4407265/b496e8d1-6342-4c70-86a2-49f203a7ce93 width="400">

### Features

* Save or spend money as an island team
* Compete to have the highest balance in the game
* See a full history of transactions on the account

### Requirements
**Bank** requires an economy to be installed on the server that uses Vault. Ideally, the economy should be multi-world aware otherwise money may end up being shared between worlds and game modes.

## Commands
### Player commands

The default player command is `bank` and it can be changed in the config.yml. So you use the bank you do `/island bank` for example.

* `bank deposit <amount>` - deposit money into the island bank
* `bank withdraw <amount>` - withdraw money from the island bank
* `bank balance` - see your island bank balance
* `bank statement` - see a fancy statement of deposits/withdrawals, etc. on your island bank account

### Admin commands

The default admin command is `bank` and it can be changed in the config.yml.

Admin commands make money by magic.
* `bank give <player> <amount>` - deposit money into the player's island bank
* `bank take <player> <amount>` - withdraw money from the player's island bank
* `bank set <player> <amount>` - set the player's island bank balance to an amount
* `bank balance <player>` - see a player's island bank balance
* `bank statement <player>` - see a fancy statement of deposits/withdrawals, etc. on the player's island bank account



## Interest

The Bank addon supports automatic compound interest on island bank balances.

Interest is configured with three settings in `config.yml`:

| Setting | Default | Description |
|---|---|---|
| `interest-rate` | `10` | Annual interest rate as a percentage (e.g. `10` = 10% per year). Set to `0` or less to disable interest. |
| `compound-period` | `1` | How often interest is compounded, in **days**. |
| `cooldown` | `60` | Cooldown in **seconds** between a player's deposit and withdrawal commands. This does **not** affect how often interest is paid. |

### How interest is calculated

Interest is calculated using the standard **compound interest formula**:

```
A = P × (1 + r/n)^(n×t)
```

Where:
- **P** = current island bank balance
- **r** = annual interest rate as a decimal (e.g. `10` → `0.10`)
- **n** = number of compounding periods per year (`365 / compound-period`)
- **t** = elapsed time in years since interest was last paid

The interest paid is `A − P`. It is only applied when at least one full `compound-period` has elapsed since the last payment, and only if the amount is at least `0.01`.

### Example

With the defaults (`interest-rate: 10`, `compound-period: 1`) and a balance of **10,000**:

- After **1 day**: `10,000 × (1 + 0.10/365)^1 ≈ 10,002.74` (interest ≈ **2.74**)
- After **1 year**: `10,000 × (1 + 0.10/365)^365 ≈ 11,051.56` (interest ≈ **1,051.56**)

### When interest is calculated

Interest is recalculated (lazily) whenever:
- The server starts
- A player logs in
- A player deposits money
- A player withdraws money

> **Tip:** Do not set `compound-period` longer than your server's typical uptime between reboots, otherwise interest may not be paid regularly.



```
bank:
  # BentoBox GameModes that can use Bank
  game-modes:
  - BSkyBlock
  - AOneBlock
  - AcidIsland
  - SkyGrid
  - CaveBlock
  commands:
    # User command
    user: bank
    # Admin command
    admin: bank
```

## Permissions

```
permissions:
  '[gamemode].bank.user':
    description: Player can use bank command
    default: true
  '[gamemode].bank.user.balance':
    description: Player can use bank balance command
    default: true
  '[gamemode].bank.user.deposit':
    description: Player can use the bank deposit command
    default: true
  '[gamemode].bank.user.withdraw':
    description: Player can use bank withdraw command
    default: true
  '[gamemode].bank.user.statement':
    description: Player can use the bank statement command
    default: true
  '[gamemode].bank.user.baltop':
    description: Player can use bank baltop command
    default: true
  '[gamemode].bank.admin':
    description: Player can use admin command
    default: op
  '[gamemode].bank.admin.balance':
    description: Player can use admin balance command
    default: op
  '[gamemode].bank.admin.give':
    description: Player can use the admin give command
    default: op
  '[gamemode].bank.admin.take':
    description: Player can use admin take command
    default: op
  '[gamemode].bank.admin.statement':
    description: Player can use the admin statement command
    default: op
  '[gamemode].bank.admin.set':
    description: Player can use admin set command
    default: op

```

## Like this addon?
You can [sponsor](https://github.com/sponsors/tastybento) to get more addons like this and make this one better!

