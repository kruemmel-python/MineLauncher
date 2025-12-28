# MineLauncherRPG – Handbuch (In‑Game)

Dieses Handbuch beschreibt **alle Befehle** und **In‑Game‑Möglichkeiten** für Spieler und Admins.

---

## 0) Launcher‑CLI (außerhalb des Spiels)

- **Client starten:** `dotnet run --project C_launcher -- play [Name]`
  - Beispiel: `dotnet run --project C_launcher -- play Alex`

---

## 1) Admin erstellen (Berechtigungen)

### Variante A: OP (schnell)
1. Konsole öffnen.
2. `op <Spielername>` ausführen.
3. Der Spieler hat Zugriff auf alle `rpg.*` Rechte.

### Variante B: Permissions (empfohlen)
Wenn du ein Permissions‑Plugin nutzt (z. B. LuckPerms):
1. Gruppe `rpg-admin` anlegen.
2. Rechte vergeben:
   - `rpg.admin.*` (volle Adminrechte)
   - oder gezielt:
     - `rpg.admin` (Admin‑Menü)
     - `rpg.editor` (Editor‑Werkzeuge)
     - `rpg.debug` (Debug‑Overlay)
     - `rpg.mod` (Moderationstools)
3. Spieler der Gruppe hinzufügen.

> **Hinweis:** Ohne Permissions‑Plugin kannst du einzelne Spieler nur über `op` voll berechtigen.

---

## 2) Spieler‑Handbuch

### 2.1 Hauptmenü & Skillbaum
- **Hauptmenü:** `/rpg`
- **Skillbaum:** `/rpg skilltree`

### 2.2 Skills
- **Skill lernen:** `/rpg` → **Skills** → Skill anklicken
- **Skill nutzen:** `/rpg skill <id>`
- **Skill binden:** `/rpg bind <slot 1-9> <skillId>`
  - Beispiel: `/rpg bind 2 dash`

### 2.3 Quests
- **Questliste:** `/rpg quest list`
- **Quest abbrechen:** `/rpg quest abandon <id>`
- **Quest prüfen/abschließen:** `/rpg quest complete <id>`

### 2.4 Klassen
- **Liste:** `/rpg class list`
- **Wählen:** `/rpg class choose <id>`

### 2.5 Respec & Berufe
- **Respec:** `/rpg respec`
- **Berufe:**
  - Liste: `/rpg profession list`
  - Setzen: `/rpg profession set <name> <level>`

### 2.6 Geld & Transfer
- **Gold anzeigen:** `/rpg money`
- **Gold senden:** `/rpg pay <player> <amount>`

### 2.7 Welt‑Events
- **Aktive Events:** `/rpg event list`
- **Event‑Status:** `/rpg event status <id>`

### 2.8 Crafting‑Aufträge
- **Aufträge anzeigen:** `/rpg order list`
- **Auftrag erstellen:** `/rpg order create <material> <amount> <reward>`
- **Auftrag erfüllen:** `/rpg order fulfill <id>`

### 2.9 Housing / Home
- **Home setzen:** `/rpg home set`
- **Hinweis:** Setzt auch die Respawn‑Position des Spielers.
- **Home teleportieren:** `/rpg home go`
- **Upgrade:** `/rpg home upgrade <craft|teleport|buff>`

### 2.10 Fraktionen
- **Ruf anzeigen:** `/rpg faction`

---

## 3) Party, Gilden & Kommunikation

### 3.1 Party
- `/party create`
- `/party invite <player>`
- `/party join <leader>`
- `/party leave`
- `/party chat <message>`
- **Shortcut:** `/p <...>` (alias für `/party`)

### 3.2 Gilden
- `/guild create <id> <name>`
- `/guild invite <player>`
- `/guild accept`
- `/guild leave`
- `/guild disband`
- `/guild info`
- `/guild chat <message>`
- **Shortcut:** `/g <message>` (Gilden‑Chat)

**Gildenbank:**
- `/guild bank balance`
- `/guild bank deposit <amount>`
- `/guild bank withdraw <amount>`

**Gildenquests:**
- `/guild quest list`
- `/guild quest create <id> <goal> <name>`
- `/guild quest progress <id> <amount>`
- `/guild quest complete <id>`

**Gildenhalle:**
- `/guild hall set` (Leader)
- `/guild hall go`
- `/guild hall upgrade <craft|teleport|buff>`

### 3.3 Voice‑Chat
- `/voicechat party` (Party‑Channel)
- `/voicechat guild` (Gilden‑Channel)
- `/voicechat leave` (verlassen)

---

## 4) Handel, Auktionshaus, PvP, Dungeons

### 4.1 Auktionshaus
- `/auction list`
- `/auction sell <price>` (Item in der Hand)
- `/auction buy <id>`

### 4.2 Handel (Gold‑Trade)
- `/trade request <player>`
- `/trade accept`
- `/trade offer <gold>`
- `/trade requestgold <gold>`
- `/trade ready`
- `/trade cancel`

### 4.3 PvP
- `/pvp join` (Queue)
- `/pvp top` (Rangliste)
- `/pvp season` (aktive Saison)

### 4.4 Dungeons
- `/dungeon enter`
- `/dungeon leave`
- `/dungeon generate <theme>` (z. B. `wfc`, `gruft`)
- `/dungeon queue <theme>` (Matchmaking)
- `/dungeon leavequeue`
- `/dungeon role <tank|heal|dps>`

---

## 5) Admin‑Handbuch

### 5.1 Admin‑Menü
- **Öffnen:** `/rpgadmin`
- Inhalte: Zonen, NPCs, Quests, Loot, Skills, Verzauberungen, Debug, Bau‑Manager, Permissions.

### 5.2 Editor‑Wand (Zonen)
- **Befehl:** `/rpgadmin wand`
- **Links‑Klick:** Pos1 setzen
- **Rechts‑Klick:** Pos2 setzen

### 5.3 Zonen
- `/rpgadmin zone create <id>`
- `/rpgadmin zone setlevel <id> <min> <max>`
- `/rpgadmin zone setmod <id> <slow> <damage>`

### 5.4 NPCs
- `/rpgadmin npc create <id> <role>`
- `/rpgadmin npc dialog <id>`
- `/rpgadmin npc linkquest <npcId> <questId>`
- `/rpgadmin npc linkshop <npcId> <shopId>`
- `/rpgadmin npc faction <npcId> <factionId>`
- `/rpgadmin npc rank <npcId> <rankId>`

### 5.5 Quests
- `/rpgadmin quest create <id> <name>`
- `/rpgadmin quest addstep <id> <type> <target> <amount>`
  - **Typen:** `KILL`, `COLLECT`, `TALK`, `EXPLORE`, `CRAFT`, `USE_ITEM`, `DEFEND`, `ESCORT`

### 5.6 Loot‑Tabellen
- `/rpgadmin loot create <id> <appliesTo>`
- `/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>`
  - **Rarity:** `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`

### 5.7 Skills
- `/rpgadmin skill create <id>`
- `/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>`
- `/rpgadmin skill addeffect <id> <effectType> <param:value>...`

### 5.8 Custom Mobs & Spawner
- `/rpgadmin mob spawn <mobId>`
- `/rpgadmin spawner create <id> <zoneId>`
- `/rpgadmin spawner addmob <id> <mobId> <chance>`
- `/rpgadmin spawner setlimit <id> <amount>`

### 5.8.1 Welt‑Events
- `/rpgadmin event create <id> <zoneId> <name>`
- `/rpgadmin event addstep <id> <type> <target> <amount>`
- `/rpgadmin event reward <id> <xp> <gold>`
- `/rpgadmin event unlock <id> <questId>`
- `/rpgadmin event start <id>`
- `/rpgadmin event stop <id>`

### 5.8.2 Ressourcen‑Nodes
- `/rpgadmin node create <profession> <material> <respawnSeconds> <xp>`

### 5.8.3 PvP‑Saison
- `/rpgadmin pvp seasonstart <id> <name> <days>`
- `/rpgadmin pvp seasonend`

### 5.9 Gebäude & Schemata
- `/rpgadmin build` oder `/rpgadmin build gui` (Bau‑Manager)
- `/rpgadmin build <id>` (Gebäude platzieren)
- `/rpgadmin build undo` (Undo)
- `/rpgadmin build move` (Move‑GUI)

### 5.9.1 Worldbuilding‑Tools (GUI)
- **Worldbuilding** → **Bereich füllen** (Blockauswahl)
- **Worldbuilding** → **Bereich löschen** (setzt markierte Area auf Luft)

### 5.10 Permissions‑System
- `/rpgadmin perms` (GUI)
- `/rpgadmin perms role create <key> <displayName>`
- `/rpgadmin perms role delete <key>`
- `/rpgadmin perms role rename <key> <displayName>`
- `/rpgadmin perms role parent add <role> <parent>`
- `/rpgadmin perms role parent remove <role> <parent>`
- `/rpgadmin perms role node <role> <node> <allow|deny|inherit>`
- `/rpgadmin perms user setprimary <player> <role>`
- `/rpgadmin perms user add <player> <role>`
- `/rpgadmin perms user remove <player> <role>`
- `/rpgadmin perms user info <player> <node>`

### 5.11 Behavior‑Editor
- `/behavior edit <tree>` (öffnet den GUI‑Editor)

### 5.12 Lootchat
- `/lootchat [true|false]` (Broadcasts an/aus)

---

## 6) In‑Game‑Möglichkeiten (GUI & Systeme)

- **RPG Menü** (`/rpg`) mit Charakter, Skills, Quests, Fraktionen.
- **Skill‑GUI** zum Lernen von Skills.
- **Skillbaum** (`/rpg skilltree`).
- **Admin‑Menü** (`/rpgadmin`) mit Debug‑Toggle, Bau‑Manager, Permissions.
- **Verzauberungen‑Admin**: Rezepte erstellen/bearbeiten/löschen im Admin‑Menü.
- **Bau‑Manager**: Kategorien, Gebäude, Einzel‑Schemata, Undo, Move‑GUI.
- **Permissions‑GUI**: Rollen, Vererbung, Nodes, Spielerrollen, Audit‑Log.
- **Behavior‑Editor GUI** für KI‑Bäume.
- **Auktionshaus**: Listings per `/auction`.
- **Gildenbank & Gildenquests**.
- **Dungeon‑Instanzen** per `/dungeon generate`.
- **PvP‑Matchmaking** per `/pvp join`.

---

## 7) WorldCreatorPlugin – Weltverwaltung

- **Menü öffnen:** `/worlds`
- **Welten auflisten:** `/worlds list`
- **Teleport:** `/worlds tp <welt>`

---

## 7) Quick‑Start (Minimal)
1. Admin erstellen (`op` oder Permissions).
2. `/rpgadmin wand` → Pos1/Pos2 setzen.
3. `/rpgadmin zone create startzone`.
4. `/rpgadmin npc create guide QUESTGIVER`.
5. `/rpgadmin quest create starter "Erste Schritte"`.
6. `/rpgadmin quest addstep starter KILL ZOMBIE 3`.
7. Spieler nutzt `/rpg` → Quests → annehmen.
