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



## Configuration

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

