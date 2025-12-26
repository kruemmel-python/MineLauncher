# C_launcher

A simple C# launcher with a GUI for Minecraft servers (Vanilla, Paper, Bukkit, CraftBukkit) supporting Online and Offline mode. Additionally, a custom client launcher is integrated, which loads the Minecraft files and starts the game in Offline mode.

## Prerequisites

- Windows
- .NET 8 SDK
- Java (set in the `PATH` or in the config path)

## Usage

```bash
# in the repo root:
dotnet run --project C_launcher
# or in the C_launcher folder:
dotnet run --project C_launcher.csproj
```

The GUI creates/opens the configuration and offers server download + start, as well as client loading + start.

Optionally, the client can also be started via CLI:

```bash
dotnet run --project C_launcher -- play [Name]
```

## Configuration

The file `launcher-config.json` contains all settings:

- `server.type`: `vanilla`, `paper`, `bukkit`, or `craftbukkit`
- `server.version`: Minecraft version
- `server.paperBuild`: Optional, Paper build number (empty = latest)
- `server.onlineMode`: `true` for Online, `false` for Offline
- `server.acceptEula`: `true` if the EULA is accepted
- `server.port`: Server port
- `java.path`: e.g., `java` or a full path
- `java.minMemory` / `java.maxMemory`: RAM settings
- `game.clientVersion`: Minecraft version for the client (must match the server)
- `game.gameDirectory`: Location to store client files (relative or absolute)
- `game.maxMemoryMb`: RAM for the client
- `game.offlineMode`: `true` starts the client in Offline mode
- `game.offlineUsername`: Name for Offline login (Default: `Player`)

All files are stored in the subdirectory `servers/<server.name>`.

---

# MineLauncherRPG – Commands & In-Game Features

This section summarizes **all commands and in-game features** of the project (Launcher + RPG Plugin).

## Launcher CLI

| Command | Description | Example |
| :--- | :--- | :--- |
| `play [Name]` | Starts the client in Offline mode (Name optional) | `dotnet run --project C_launcher -- play Alex` |

## RPG – Player Commands

| Command | Description | Example |
| :--- | :--- | :--- |
| `/rpg` | Opens the RPG Menu (Character, Skills, Quests, Factions) | `/rpg` |
| `/rpg skill <id>` | Activates a learned skill | `/rpg skill dash` |
| `/rpg quest list` | Opens the quest list | `/rpg quest list` |
| `/rpg quest abandon <id>` | Abandons an active quest | `/rpg quest abandon starter` |
| `/rpg quest complete <id>` | Checks/forces completion if ready | `/rpg quest complete starter` |
| `/rpg respec` | Reset of skills/attributes | `/rpg respec` |
| `/rpg class list` | Lists classes | `/rpg class list` |
| `/rpg class choose <id>` | Chooses a class | `/rpg class choose mage` |
| `/rpg bind <slot 1-9> <skillId>` | Binds skill to slot | `/rpg bind 2 heal` |
| `/rpg money` | Shows gold | `/rpg money` |
| `/rpg pay <player> <amount>` | Transfers gold | `/rpg pay Steve 50` |
| `/rpg profession list` | Lists professions | `/rpg profession list` |
| `/rpg profession set <name> <level>` | Sets profession level | `/rpg profession set mining 5` |
| `/rpg skilltree` | Opens the skill tree | `/rpg skilltree` |

### Party & Chat

| Command | Description | Example |
| :--- | :--- | :--- |
| `/party create` | Create party | `/party create` |
| `/party invite <player>` | Invite player | `/party invite Alex` |
| `/party join <leader>` | Join party | `/party join Alex` |
| `/party leave` | Leave party | `/party leave` |
| `/party chat <message>` | Party chat | `/party chat Hello` |
| `/p <...>` | Shortcut for `/party ...` | `/p chat Hi` |

### Guilds & Chat

| Command | Description | Example |
| :--- | :--- | :--- |
| `/guild create <id> <name>` | Create guild | `/guild create knights "The Knights"` |
| `/guild invite <player>` | Invite player | `/guild invite Alex` |
| `/guild accept` | Accept invitation | `/guild accept` |
| `/guild leave` | Leave guild | `/guild leave` |
| `/guild disband` | Disband guild (Leader) | `/guild disband` |
| `/guild info` | Guild info | `/guild info` |
| `/guild chat <message>` | Guild chat | `/guild chat Hello` |
| `/guild bank balance` | Account balance | `/guild bank balance` |
| `/guild bank deposit <amount>` | Deposit gold | `/guild bank deposit 100` |
| `/guild bank withdraw <amount>` | Withdraw gold (Officer/Leader) | `/guild bank withdraw 50` |
| `/guild quest list` | Show guild quests | `/guild quest list` |
| `/guild quest create <id> <goal> <name>` | Create quest | `/guild quest create wolfhunt 25 "Wolf Hunt"` |
| `/guild quest progress <id> <amount>` | Set progress | `/guild quest progress wolfhunt 5` |
| `/guild quest complete <id>` | Complete quest | `/guild quest complete wolfhunt` |
| `/g <message>` | Shortcut for Guild Chat | `/g Hello` |

### Trading & Economy

| Command | Description | Example |
| :--- | :--- | :--- |
| `/auction list` | Show auctions | `/auction list` |
| `/auction sell <price>` | Sell item in hand | `/auction sell 250` |
| `/auction buy <id>` | Buy auction | `/auction buy 1a2b3c4d` |
| `/trade request <player>` | Request trade | `/trade request Alex` |
| `/trade accept` | Accept trade | `/trade accept` |
| `/trade offer <gold>` | Offer own gold | `/trade offer 100` |
| `/trade requestgold <gold>` | Request gold | `/trade requestgold 50` |
| `/trade ready` | Confirm trade | `/trade ready` |
| `/trade cancel` | Cancel trade | `/trade cancel` |

### Dungeons, PvP & Voice

| Command | Description | Example |
| :--- | :--- | :--- |
| `/dungeon enter` | Enter dungeon | `/dungeon enter` |
| `/dungeon leave` | Leave dungeon | `/dungeon leave` |
| `/dungeon generate <theme>` | Generate instance | `/dungeon generate wfc` |
| `/pvp join` | PvP Queue | `/pvp join` |
| `/pvp top` | Top list | `/pvp top` |
| `/voicechat party` | Party voice channel | `/voicechat party` |
| `/voicechat guild` | Guild voice channel | `/voicechat guild` |
| `/voicechat leave` | Leave voice channel | `/voicechat leave` |
| `/lootchat [true|false]` | Toggle loot chat | `/lootchat false` |

## RPG – Admin Commands

| Command | Description | Example |
| :--- | :--- | :--- |
| `/rpgadmin` | Open Admin Menu | `/rpgadmin` |
| `/rpgadmin wand` | Editor Wand (Pos1/Pos2) | `/rpgadmin wand` |
| `/rpgadmin zone create <id>` | Create zone | `/rpgadmin zone create startzone` |
| `/rpgadmin zone setlevel <id> <min> <max>` | Level Range | `/rpgadmin zone setlevel startzone 1 10` |
| `/rpgadmin zone setmod <id> <slow> <damage>` | Zone Modifiers | `/rpgadmin zone setmod startzone 0.9 1.1` |
| `/rpgadmin npc create <id> <role>` | Create NPC | `/rpgadmin npc create guide QUESTGIVER` |
| `/rpgadmin npc dialog <id>` | Set dialogue | `/rpgadmin npc dialog guide` |
| `/rpgadmin npc linkquest <npcId> <questId>` | Link Quest | `/rpgadmin npc linkquest guide starter` |
| `/rpgadmin npc linkshop <npcId> <shopId>` | Link Shop | `/rpgadmin npc linkshop guide village_merchant` |
| `/rpgadmin quest create <id> <name>` | Create quest | `/rpgadmin quest create starter "Start"` |
| `/rpgadmin quest addstep <id> <type> <target> <amount>` | Quest Step | `/rpgadmin quest addstep starter KILL ZOMBIE 3` |
| `/rpgadmin loot create <id> <appliesTo>` | Create Loot Table | `/rpgadmin loot create forest ZOMBIE` |
| `/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>` | Loot Entry | `/rpgadmin loot addentry forest GOLD_NUGGET 0.5 1 3 COMMON` |
| `/rpgadmin skill create <id>` | Create skill | `/rpgadmin skill create dash` |
| `/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>` | Set skill field | `/rpgadmin skill set dash cooldown 3` |
| `/rpgadmin skill addeffect <id> <effectType> <param:value>...` | Add skill effect | `/rpgadmin skill addeffect dash VELOCITY x:0.8 y:0.3` |
| `/rpgadmin mob spawn <mobId>` | Spawn Custom Mob | `/rpgadmin mob spawn forest_zombie` |
| `/rpgadmin spawner create <id> <zoneId>` | Create spawner | `/rpgadmin spawner create forest_spawn startzone` |
| `/rpgadmin spawner addmob <id> <mobId> <chance>` | Add mob | `/rpgadmin spawner addmob forest_spawn forest_zombie 1.0` |
| `/rpgadmin spawner setlimit <id> <amount>` | Set spawn limit | `/rpgadmin spawner setlimit forest_spawn 8` |
| `/rpgadmin build` or `/rpgadmin build gui` | Build Manager GUI | `/rpgadmin build gui` |
| `/rpgadmin build <id>` | Place building | `/rpgadmin build cottage` |
| `/rpgadmin build undo` | Undo last placement | `/rpgadmin build undo` |
| `/rpgadmin build move` | Schematic Move GUI | `/rpgadmin build move` |
| `/rpgadmin perms` | Permissions GUI | `/rpgadmin perms` |
| `/rpgadmin perms role create <key> <displayName>` | Create role | `/rpgadmin perms role create mod "Moderator"` |
| `/rpgadmin perms role delete <key>` | Delete role | `/rpgadmin perms role delete mod` |
| `/rpgadmin perms role rename <key> <displayName>` | Rename role | `/rpgadmin perms role rename mod "Mod"` |
| `/rpgadmin perms role parent add <role> <parent>` | Add parent | `/rpgadmin perms role parent add mod admin` |
| `/rpgadmin perms role parent remove <role> <parent>` | Remove parent | `/rpgadmin perms role parent remove mod admin` |
| `/rpgadmin perms role node <role> <node> <allow|deny|inherit>` | Set node | `/rpgadmin perms role node mod rpg.admin allow` |
| `/rpgadmin perms user setprimary <player> <role>` | Set primary role | `/rpgadmin perms user setprimary Alex mod` |
| `/rpgadmin perms user add <player> <role>` | Add role | `/rpgadmin perms user add Alex mod` |
| `/rpgadmin perms user remove <player> <role>` | Remove role | `/rpgadmin perms user remove Alex mod` |
| `/rpgadmin perms user info <player> <node>` | Check node | `/rpgadmin perms user info Alex rpg.admin` |
| `/behavior edit <tree>` | Behavior Editor | `/behavior edit skeleton_king` |

## In-Game Features (GUI & Systems)

- **RPG Menu** (`/rpg`): Character overview, Skills, Quests, Factions.
- **Skill GUI** (`/rpg` → Skills): Learn, Tooltip details, Skill points.
- **Skill Tree** (`/rpg skilltree`): Visual skill tree.
- **Admin Menu** (`/rpgadmin`): Zones, NPCs, Quests, Loot, Skills, Debug, Build Manager, Permissions.
- **Build Manager**: Categories, Buildings, Single Schematics, Undo, Move GUI.
- **Permissions GUI**: Roles, Inheritance, Nodes, User Roles, Audit Log.
- **Behavior Editor**: AI trees for mobs via GUI.
- **Voice Chat Channels**: Party/Guild channels via `/voicechat`.
- **Auction House**: Item listings via `/auction`.
- **Guild Bank & Quests**: Management via `/guild`.