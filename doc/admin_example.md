# ⚔️ MineLauncher RPG - Admin Handbook

Welcome to the **MineLauncher RPG System**. This plugin transforms a PaperMC Server into a fully functional MMORPG with classes, skills, dungeons, guilds, and a persistent SQL database.

## 📋 Prerequisites
*   **Server:** PaperMC 1.20.4 (or newer)
*   **Java:** JDK 17+
*   **Database:** PostgreSQL (mandatory for player data)

## 🧰 Launcher CLI (Outside the Game)

*   **Start Client:** `dotnet run --project C_launcher -- play [Name]`
    Example: `dotnet run --project C_launcher -- play Alex`

---

## 🚀 Step 0: Installation & Database (IMPORTANT)

Before starting the gameplay setup, the technical foundation must be established.

1.  **Install Plugin:**
    *   Place the `rpg-plugin-1.0.0.jar` into the `plugins/` folder.
    *   Start the server once to generate the configurations.
    *   Stop the server.

2.  **Connect Database (`config.yml`):**
    Open `plugins/RPGPlugin/config.yml` and enter your PostgreSQL details:
    ```yaml
    database:
      host: localhost
      port: 5432
      name: rpg_db
      user: your_user
      password: your_password
      poolSize: 10
    ```

3.  **Start Server:**
    Upon startup, the plugin will automatically create the tables (`rpg_players`, `rpg_skills`, etc.).

---

# 🛠️ Master Guide: From Zero to Playable

This guide walks you through creating the content.
**Goal:** At the end, two players can log in, choose classes, level up in a party, trade, establish guilds, and conquer dungeons.

## 🏗️ Part 1: The Foundation (Classes & Skills)

### 1. Define Skills (`skills.yml`)
We will create a **"Fireball"** for Mages and a **"Smash"** for Warriors.

```yaml
fireball:
  name: "Fireball"
  type: ACTIVE
  category: MAGIC
  cooldown: 5
  manaCost: 20
  effects:
  - type: PROJECTILE
    params: { type: SMALL_FIREBALL }
  - type: SOUND
    params: { sound: ENTITY_BLAZE_SHOOT }
  - type: DAMAGE
    params: { amount: 8 }

smash:
  name: "Smash"
  type: ACTIVE
  category: ATTACK
  cooldown: 8
  manaCost: 15
  effects:
  - type: DAMAGE
    params: { amount: 12, radius: 3 } # Area Damage
  - type: PARTICLE
    params: { type: EXPLOSION_LARGE, count: 3 }
  - type: SOUND
    params: { sound: ENTITY_GENERIC_EXPLODE, volume: 0.5 }
```

### 2. Define Classes (`classes.yml`)
Here we assign the skills to the classes.

```yaml
mage:
  name: "&9Mage"
  startSkills:
  - fireball

warrior:
  name: "&cWarrior"
  startSkills:
  - smash
```

> 🔄 **Tip:** Use `/reload` (or server restart) after configuration changes.

---

## 💰 Part 2: Economy & Items

### 1. Create Loot Table (`loot.yml`)
Monsters should drop Gold and rare weapons.

```yaml
starter_loot:
  appliesTo: ZOMBIE
  entries:
  - material: GOLD_NUGGET
    chance: 0.8
    minAmount: 1
    maxAmount: 3
    rarity: COMMON
  - material: IRON_SWORD
    chance: 0.1
    minAmount: 1
    maxAmount: 1
    rarity: RARE # Stats are generated automatically!
```

### 2. Define Shop (`shops.yml`)
A merchant for buying and selling.

```yaml
village_merchant:
  title: "Village Merchant"
  items:
    - slot: 0
      material: GOLD_NUGGET
      name: "&eGold Nugget"
      buyPrice: 0      # Not buyable
      sellPrice: 10    # Sell price
    - slot: 1
      material: POTION
      name: "&cHealing Potion"
      buyPrice: 50
      sellPrice: 0
```

### 3. Set up Merchant NPC (In-Game)
1.  Go to the village square.
2.  `/rpgadmin npc create merchant VENDOR`
3.  `/rpgadmin npc linkshop merchant village_merchant`
4.  *(Optional)* Dialogue: `/rpgadmin npc dialog merchant` -> Chat: "I buy your gold!"

---

## 🌍 Part 3: The World (Mobs & Zones)

### 1. Create Custom Mob (`mobs.yml`)

```yaml
forest_zombie:
  name: "&2Forest Lurker"
  type: ZOMBIE
  health: 30
  damage: 5
  xp: 15
  lootTable: starter_loot
  skillIntervalSeconds: 10
  skills: []
```

### 2. Set up Zone and Spawner (In-Game)
1.  **Define Zone:**
    *   Get the wand: `/rpgadmin wand`
    *   Mark the area (Left/Right-click).
    *   Create Zone: `/rpgadmin zone create forest_zone`
    *   Set Level: `/rpgadmin zone setlevel forest_zone 1 10`
2.  **Activate Spawner:**
    *   `/rpgadmin spawner create forest_spawner forest_zone`
    *   `/rpgadmin spawner addmob forest_spawner forest_zombie 1.0`
    *   `/rpgadmin spawner setlimit forest_spawner 8`

---

## 📜 Part 4: Quests & Story

1.  **Create Quest:**
    *   `/rpgadmin quest create forest_cleansing "Cleansing the Forest"`
    *   `/rpgadmin quest addstep forest_cleansing KILL ZOMBIE 5`
    *   `/rpgadmin quest addstep forest_cleansing COLLECT GOLD_NUGGET 3`
2.  **Spawn Quest NPC:**
    *   `/rpgadmin npc create guard QUESTGIVER`
    *   `/rpgadmin npc dialog guard` -> Chat: "The forest is unsafe. Help us!"
    *   `/rpgadmin npc linkquest guard forest_cleansing`

---

## 🏘️ Part 5: Buildings & Schematics (New)

### 1. Schematic Folder
*   Place your `.schem` files in `plugins/RPGPlugin/schematics/` (or adjust `building.schematicsFolder` in `config.yml`).

### 2. Define Building (`buildings.yml`)
Example for multi-story buildings, basements, and furnishing:

```yaml
buildings:
  cottage:
    name: "Small House"
    category: RESIDENTIAL
    schematic: "cottage_ground.schem"
    floorSchematic: "cottage_floor.schem"
    minFloors: 1
    maxFloors: 2
    floorHeight: 5
    basement:
      schematic: "cottage_basement.schem"
      depth: 4
    offset:
      x: 0
      y: 0
      z: 0
    furniture:
      - schematic: "cottage_furniture.schem"
        x: 0
        y: 0
        z: 0
        rotation: 0
```

### 3. Place Building (GUI)
1.  Open the Admin Menu: `/rpgadmin`
2.  Click **Build Manager** → Category → Building.
3.  Right-click a block to place the building.

### 4. Place Single Schematic (GUI)
1.  Open the Admin Menu: `/rpgadmin`
2.  Click **Build Manager** → **Single Schematic**.
3.  Enter the filename (e.g., `house.schem`).
4.  Right-click a block to place only that schematic.

### 5. Undo Placement
*   `/rpgadmin build undo` reverts the last placement for the admin.

---

## 🤝 Part 6: Social & End-Game

### 1. Dungeons (Instanced)
Creates a temporary world for a group.
*   **Command:** `/dungeon generate wfc` (Wave Function Collapse) or `/dungeon generate crypt` (Default).
*   **Logic:** Generates world → Teleports party → Spawns boss → Deletes world after completion/timeout.

### 2. Guilds & Party
*   **Party:** `/party invite <Name>` (Shares XP in proximity).
*   **Guild:** `/guild create <ID> <Name>` (Guilds with bank and quests).

### 3. Auction House
*   Hold an item in hand → `/auction sell <Price>`.

### 4. PvP & Elo
*   Arenas must be configured in `arenas.yml`.
*   Players use `/pvp join` to enter the queue for Elo matches.

---

## 🧠 Part 7: Behavior Editor (Mob AI)

With the Behavior Tree editor, you can create and test AI trees for mobs.

### 1. Open Behavior Editor
*   `/behavior edit <tree>` opens the editor.
*   Example: `/behavior edit skeleton_king`

> Tip: The Tree name must match `behaviorTree` in `mobs.yml` (e.g., `skeleton_king`).

### 2. Basic Workflow
1.  Open Editor: `/behavior edit skeleton_king`
2.  Add nodes (e.g., `Selector`, `Sequence`, `Cooldown`, `MeleeAttack`, `CastSkill`)
3.  Save/Apply in the GUI (depending on the buttons in the editor)
4.  Spawn the mob to test the behavior.

### 3. Example Tree (Concept)
Goal: The boss attacks in melee and uses a skill every 8 seconds.

*   **Selector**
    *   **Sequence**
        *   `Cooldown(8s)`
        *   `CastSkill(ember_shot)`
    *   `MeleeAttack`

### 4. Example in `mobs.yml`
```yaml
skeleton_king:
  name: "&cSkeleton King"
  type: SKELETON
  health: 80
  damage: 10
  mainHand: DIAMOND_SWORD
  helmet: GOLDEN_HELMET
  skills:
    - shield_wall
    - ember_shot
  skillIntervalSeconds: 8
  xp: 180
  lootTable: forest_mobs
  behaviorTree: skeleton_king
```

> Then open `/behavior edit skeleton_king` and build the tree accordingly.

---

## ✅ Walkthrough: The Player Experience

This is what the loop looks like for your players:

1.  **Start:** Login → `/rpg class choose warrior`.
2.  **Setup:** Bind skill → `/rpg bind 2 smash`.
3.  **Quest:** Go to the **Guard**, accept the quest.
4.  **Combat:** Run into the **Forest Zone**. Mobs spawn automatically.
5.  **Action:** Press key 2 (Right-click) → Skill triggers.
6.  **Loot:** Mobs drop gold and rare items with stats.
7.  **Trade:** Return to the **Merchant** → Sell gold.
8.  **Completion:** Turn in the quest to the Guard → XP & Level Up.
9.  **Endgame:** Invite friends, form a guild, start a dungeon!

---

## 🔧 Important Admin Commands

| Command | Description |
| :--- | :--- |
| `/rpgadmin wand` | Gives the tool for marking zones. |
| `/rpgadmin zone` | Manage regions and level ranges. |
| `/rpgadmin npc` | Create and configure NPCs. |
| `/rpgadmin quest` | Create quests in-game. |
| `/rpgadmin skill` | Edit/create skills. |
| `/rpgadmin mob` | Manually spawn Custom Mobs. |
| `/rpgadmin spawner` | Configure automatic spawners. |
| `/rpgadmin build gui` | Opens the Build Manager. |
| `/rpgadmin build <id>` | Starts placement of a building. |
| `/rpgadmin build undo` | Reverts the last placement. |

---

# 📌 Complete Command Overview (with Examples)

## Player Commands

| Command | Example | Purpose |
| :--- | :--- | :--- |
| `/rpg` | `/rpg` | Opens the RPG Menu (Character, Skills, Quests, Factions) |
| `/rpg skill <id>` | `/rpg skill dash` | Activate skill |
| `/rpg quest list` | `/rpg quest list` | Open quest list |
| `/rpg quest abandon <id>` | `/rpg quest abandon starter` | Abandon quest |
| `/rpg quest complete <id>` | `/rpg quest complete starter` | Check quest completion |
| `/rpg respec` | `/rpg respec` | Reset skills/attributes |
| `/rpg class list` | `/rpg class list` | List classes |
| `/rpg class choose <id>` | `/rpg class choose mage` | Choose class |
| `/rpg bind <slot 1-9> <skillId>` | `/rpg bind 2 heal` | Bind skill to slot |
| `/rpg money` | `/rpg money` | View gold |
| `/rpg pay <player> <amount>` | `/rpg pay Alex 50` | Send gold |
| `/rpg profession list` | `/rpg profession list` | View professions |
| `/rpg profession set <name> <level>` | `/rpg profession set mining 5` | Set profession |
| `/rpg skilltree` | `/rpg skilltree` | Open skill tree |

## Party

| Command | Example | Purpose |
| :--- | :--- | :--- |
| `/party create` | `/party create` | Create party |
| `/party invite <player>` | `/party invite Alex` | Invite player |
| `/party join <leader>` | `/party join Alex` | Join party |
| `/party leave` | `/party leave` | Leave party |
| `/party chat <message>` | `/party chat Hello` | Party chat |
| `/p <...>` | `/p chat Hi` | Shortcut for `/party` |

## Guilds

| Command | Example | Purpose |
| :--- | :--- | :--- |
| `/guild create <id> <name>` | `/guild create knights "The Knights"` | Create guild |
| `/guild invite <player>` | `/guild invite Alex` | Invite |
| `/guild accept` | `/guild accept` | Accept invitation |
| `/guild leave` | `/guild leave` | Leave guild |
| `/guild disband` | `/guild disband` | Disband guild |
| `/guild info` | `/guild info` | View info |
| `/guild chat <message>` | `/guild chat Hello` | Guild chat |
| `/guild bank balance` | `/guild bank balance` | Bank balance |
| `/guild bank deposit <amount>` | `/guild bank deposit 100` | Deposit |
| `/guild bank withdraw <amount>` | `/guild bank withdraw 50` | Withdraw |
| `/guild quest list` | `/guild quest list` | Guild quests |
| `/guild quest create <id> <goal> <name>` | `/guild quest create wolfhunt 25 "Wolf Hunt"` | Create quest |
| `/guild quest progress <id> <amount>` | `/guild quest progress wolfhunt 5` | Update progress |
| `/guild quest complete <id>` | `/guild quest complete wolfhunt` | Complete quest |
| `/g <message>` | `/g Hello` | Shortcut Guild Chat |

## Trading, Auction, PvP, Dungeons

| Command | Example | Purpose |
| :--- | :--- | :--- |
| `/auction list` | `/auction list` | View auctions |
| `/auction sell <price>` | `/auction sell 250` | Sell item in hand |
| `/auction buy <id>` | `/auction buy 1a2b3c4d` | Buy auction |
| `/trade request <player>` | `/trade request Alex` | Request trade |
| `/trade accept` | `/trade accept` | Accept trade |
| `/trade offer <gold>` | `/trade offer 100` | Offer gold |
| `/trade requestgold <gold>` | `/trade requestgold 50` | Request gold |
| `/trade ready` | `/trade ready` | Confirm trade |
| `/trade cancel` | `/trade cancel` | Cancel trade |
| `/dungeon enter` | `/dungeon enter` | Enter dungeon |
| `/dungeon leave` | `/dungeon leave` | Leave dungeon |
| `/dungeon generate <theme>` | `/dungeon generate wfc` | Generate instance |
| `/pvp join` | `/pvp join` | Join PvP queue |
| `/pvp top` | `/pvp top` | View leaderboard |
| `/voicechat party` | `/voicechat party` | Party voice |
| `/voicechat guild` | `/voicechat guild` | Guild voice |
| `/voicechat leave` | `/voicechat leave` | Leave voice |
| `/lootchat [true|false]` | `/lootchat false` | Toggle loot chat |

## Admin Commands

| Command | Example | Purpose |
| :--- | :--- | :--- |
| `/rpgadmin` | `/rpgadmin` | Admin Menu |
| `/rpgadmin wand` | `/rpgadmin wand` | Editor Wand |
| `/rpgadmin zone create <id>` | `/rpgadmin zone create startzone` | Create zone |
| `/rpgadmin zone setlevel <id> <min> <max>` | `/rpgadmin zone setlevel startzone 1 10` | Set Level Range |
| `/rpgadmin zone setmod <id> <slow> <damage>` | `/rpgadmin zone setmod startzone 0.9 1.1` | Zone Modifiers |
| `/rpgadmin npc create <id> <role>` | `/rpgadmin npc create guide QUESTGIVER` | Create NPC |
| `/rpgadmin npc dialog <id>` | `/rpgadmin npc dialog guide` | Set dialogue |
| `/rpgadmin npc linkquest <npcId> <questId>` | `/rpgadmin npc linkquest guide starter` | Link Quest |
| `/rpgadmin npc linkshop <npcId> <shopId>` | `/rpgadmin npc linkshop guide village_merchant` | Link Shop |
| `/rpgadmin quest create <id> <name>` | `/rpgadmin quest create starter "Start"` | Create quest |
| `/rpgadmin quest addstep <id> <type> <target> <amount>` | `/rpgadmin quest addstep starter KILL ZOMBIE 3` | Add quest step |
| `/rpgadmin loot create <id> <appliesTo>` | `/rpgadmin loot create forest ZOMBIE` | Create Loot Table |
| `/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>` | `/rpgadmin loot addentry forest GOLD_NUGGET 0.5 1 3 COMMON` | Add Loot Entry |
| `/rpgadmin skill create <id>` | `/rpgadmin skill create dash` | Create skill |
| `/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>` | `/rpgadmin skill set dash cooldown 3` | Set skill property |
| `/rpgadmin skill addeffect <id> <effectType> <param:value>...` | `/rpgadmin skill addeffect dash VELOCITY x:0.8 y:0.3` | Add effect |
| `/rpgadmin mob spawn <mobId>` | `/rpgadmin mob spawn forest_zombie` | Spawn Custom Mob |
| `/rpgadmin spawner create <id> <zoneId>` | `/rpgadmin spawner create forest_spawn startzone` | Create spawner |
| `/rpgadmin spawner addmob <id> <mobId> <chance>` | `/rpgadmin spawner addmob forest_spawn forest_zombie 1.0` | Add mob to spawner |
| `/rpgadmin spawner setlimit <id> <amount>` | `/rpgadmin spawner setlimit forest_spawn 8` | Set spawn limit |
| `/rpgadmin build` / `/rpgadmin build gui` | `/rpgadmin build gui` | Build Manager |
| `/rpgadmin build <id>` | `/rpgadmin build cottage` | Place building |
| `/rpgadmin build undo` | `/rpgadmin build undo` | Undo |
| `/rpgadmin build move` | `/rpgadmin build move` | Move GUI |
| `/rpgadmin perms` | `/rpgadmin perms` | Permissions GUI |
| `/rpgadmin perms role create <key> <displayName>` | `/rpgadmin perms role create mod "Moderator"` | Create role |
| `/rpgadmin perms role delete <key>` | `/rpgadmin perms role delete mod` | Delete role |
| `/rpgadmin perms role rename <key> <displayName>` | `/rpgadmin perms role rename mod "Mod"` | Rename role |
| `/rpgadmin perms role parent add <role> <parent>` | `/rpgadmin perms role parent add mod admin` | Add parent |
| `/rpgadmin perms role parent remove <role> <parent>` | `/rpgadmin perms role parent remove mod admin` | Remove parent |
| `/rpgadmin perms role node <role> <node> <allow|deny|inherit>` | `/rpgadmin perms role node mod rpg.admin allow` | Set node |
| `/rpgadmin perms user setprimary <player> <role>` | `/rpgadmin perms user setprimary Alex mod` | Set primary role |
| `/rpgadmin perms user add <player> <role>` | `/rpgadmin perms user add Alex mod` | Add role |
| `/rpgadmin perms user remove <player> <role>` | `/rpgadmin perms user remove Alex mod` | Remove role |
| `/rpgadmin perms user info <player> <node>` | `/rpgadmin perms user info Alex rpg.admin` | Check node |
| `/behavior edit <tree>` | `/behavior edit skeleton_king` | Behavior Editor |

---

# 🧭 In-Game Possibilities (GUI & Systems)

- **RPG Menu**: `/rpg` opens Character, Skills, Quests, Factions.
- **Skill GUI**: Learn and manage skills.
- **Skill Tree**: `/rpg skilltree` opens the visual tree.
- **Admin Menu**: `/rpgadmin` with Debug, Build Manager, Permissions.
- **Build Manager**: Categories, Buildings, Single Schematics, Undo, Move GUI.
- **Permissions GUI**: Roles, Inheritance, Nodes, User Roles, Audit Log.
- **Behavior Editor**: AI trees via GUI.
- **Voice Chat**: Party/Guild channels via `/voicechat`.
- **Auction House**: Listings via `/auction`.
- **Guild Bank & Guild Quests** via `/guild`.
- **Dungeon Instances** via `/dungeon generate`.
- **PvP Matchmaking** via `/pvp join`.