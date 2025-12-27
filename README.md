# C_launcher

Ein einfacher C#-Launcher mit GUI für Minecraft-Server (Vanilla, Paper, Bukkit, CraftBukkit) mit Online- und Offline-Modus. Zusätzlich ist ein eigener Client-Launcher integriert, der die Minecraft-Dateien lädt und das Spiel im Offline-Modus startet.

## Voraussetzungen

- Windows
- .NET 8 SDK
- Java (im `PATH` oder im Config-Pfad gesetzt)

## Nutzung

```bash
# im Repo-Root:
dotnet run --project C_launcher
# oder im Ordner C_launcher:
dotnet run --project C_launcher.csproj
```

Die GUI erstellt/öffnet die Konfiguration und bietet Download + Start des Servers sowie Laden/Start des Clients.

Optional kann der Client auch per CLI gestartet werden:

```bash
dotnet run --project C_launcher -- play [Name]
```

## Konfiguration

Die Datei `launcher-config.json` enthält alle Einstellungen:

- `server.type`: `vanilla`, `paper`, `bukkit` oder `craftbukkit`
- `server.version`: Minecraft-Version
- `server.paperBuild`: Optional, Paper-Buildnummer (leer = latest)
- `server.onlineMode`: `true` für Online, `false` für Offline
- `server.acceptEula`: `true` wenn die EULA akzeptiert ist
- `server.port`: Server-Port
- `java.path`: z. B. `java` oder ein voller Pfad
- `java.minMemory` / `java.maxMemory`: RAM-Settings
- `game.clientVersion`: Minecraft-Version für den Client (muss zum Server passen)
- `game.gameDirectory`: Speicherort der Client-Dateien (relativ oder absolut)
- `game.maxMemoryMb`: RAM für den Client
- `game.offlineMode`: `true` startet den Client im Offline-Modus
- `game.offlineUsername`: Name für Offline-Login (Default: `Player`)

Alle Dateien werden im Unterordner `servers/<server.name>` abgelegt.

---

# MineLauncherRPG – Commands & In-Game Features

Dieser Abschnitt fasst **alle Commands und In‑Game‑Möglichkeiten** des Projekts zusammen (Launcher + RPG‑Plugin).

## Launcher-CLI

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `play [Name]` | Startet den Client im Offline-Modus (Name optional) | `dotnet run --project C_launcher -- play Alex` |

## RPG – Spielerbefehle

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `/rpg` | Öffnet das RPG‑Menü (Charakter, Skills, Quests, Fraktionen) | `/rpg` |
| `/rpg skill <id>` | Aktiviert einen gelernten Skill | `/rpg skill dash` |
| `/rpg quest list` | Öffnet die Questliste | `/rpg quest list` |
| `/rpg quest abandon <id>` | Bricht eine aktive Quest ab | `/rpg quest abandon starter` |
| `/rpg quest complete <id>` | Prüft/erzwingt Abschluss, falls fertig | `/rpg quest complete starter` |
| `/rpg respec` | Reset von Skills/Attributen | `/rpg respec` |
| `/rpg class list` | Listet Klassen | `/rpg class list` |
| `/rpg class choose <id>` | Wählt eine Klasse | `/rpg class choose mage` |
| `/rpg bind <slot 1-9> <skillId>` | Bindet Skill auf Slot | `/rpg bind 2 heal` |
| `/rpg money` | Zeigt Gold | `/rpg money` |
| `/rpg pay <player> <amount>` | Überweist Gold | `/rpg pay Steve 50` |
| `/rpg profession list` | Listet Berufe | `/rpg profession list` |
| `/rpg profession set <name> <level>` | Setzt Berufslevel | `/rpg profession set mining 5` |
| `/rpg skilltree` | Öffnet den Skillbaum | `/rpg skilltree` |
| `/rpg event list` | Zeigt aktive Welt‑Events | `/rpg event list` |
| `/rpg event status <id>` | Event‑Fortschritt anzeigen | `/rpg event status invasion` |
| `/rpg order list` | Crafting‑Aufträge anzeigen | `/rpg order list` |
| `/rpg order create <material> <amount> <reward>` | Auftrag erstellen | `/rpg order create IRON_SWORD 2 250` |
| `/rpg order fulfill <id>` | Auftrag erfüllen | `/rpg order fulfill order_1` |
| `/rpg home set` | Home setzen | `/rpg home set` |
| `/rpg home go` | Zum Home teleportieren | `/rpg home go` |
| `/rpg home upgrade <craft|teleport|buff>` | Housing‑Upgrade | `/rpg home upgrade craft` |
| `/rpg faction` | Fraktionsruf anzeigen | `/rpg faction` |

### Party & Chat

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `/party create` | Party erstellen | `/party create` |
| `/party invite <player>` | Spieler einladen | `/party invite Alex` |
| `/party join <leader>` | Party beitreten | `/party join Alex` |
| `/party leave` | Party verlassen | `/party leave` |
| `/party chat <message>` | Party‑Chat | `/party chat Hallo` |
| `/p <...>` | Shortcut für `/party ...` | `/p chat Hi` |

### Gilden & Chat

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `/guild create <id> <name>` | Gilde erstellen | `/guild create knights "Die Ritter"` |
| `/guild invite <player>` | Spieler einladen | `/guild invite Alex` |
| `/guild accept` | Einladung annehmen | `/guild accept` |
| `/guild leave` | Gilde verlassen | `/guild leave` |
| `/guild disband` | Gilde auflösen (Leader) | `/guild disband` |
| `/guild info` | Infos zur Gilde | `/guild info` |
| `/guild chat <message>` | Gilden‑Chat | `/guild chat Hallo` |
| `/guild bank balance` | Kontostand | `/guild bank balance` |
| `/guild bank deposit <amount>` | Gold einzahlen | `/guild bank deposit 100` |
| `/guild bank withdraw <amount>` | Gold abheben (Officer/Leader) | `/guild bank withdraw 50` |
| `/guild quest list` | Gildenquests anzeigen | `/guild quest list` |
| `/guild quest create <id> <goal> <name>` | Quest erstellen | `/guild quest create wolfhunt 25 "Wolfjagd"` |
| `/guild quest progress <id> <amount>` | Fortschritt setzen | `/guild quest progress wolfhunt 5` |
| `/guild quest complete <id>` | Quest abschließen | `/guild quest complete wolfhunt` |
| `/guild hall set` | Gildenhalle setzen (Leader) | `/guild hall set` |
| `/guild hall go` | Zur Gildenhalle | `/guild hall go` |
| `/guild hall upgrade <craft|teleport|buff>` | Hallen‑Upgrade | `/guild hall upgrade buff` |
| `/g <message>` | Shortcut für Gilden‑Chat | `/g Hallo` |

### Handel & Wirtschaft

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `/auction list` | Auktionen anzeigen | `/auction list` |
| `/auction sell <price>` | Hand‑Item verkaufen | `/auction sell 250` |
| `/auction buy <id>` | Auktion kaufen | `/auction buy 1a2b3c4d` |
| `/trade request <player>` | Handel anfragen | `/trade request Alex` |
| `/trade accept` | Handel annehmen | `/trade accept` |
| `/trade offer <gold>` | Eigenes Gold bieten | `/trade offer 100` |
| `/trade requestgold <gold>` | Gold verlangen | `/trade requestgold 50` |
| `/trade ready` | Handel bestätigen | `/trade ready` |
| `/trade cancel` | Handel abbrechen | `/trade cancel` |

### Dungeons, PvP & Voice

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `/dungeon enter` | Dungeon betreten | `/dungeon enter` |
| `/dungeon leave` | Dungeon verlassen | `/dungeon leave` |
| `/dungeon generate <theme>` | Instanz erzeugen | `/dungeon generate wfc` |
| `/dungeon queue <theme>` | Matchmaking‑Queue | `/dungeon queue wfc` |
| `/dungeon leavequeue` | Queue verlassen | `/dungeon leavequeue` |
| `/dungeon role <tank|heal|dps>` | Rolle setzen | `/dungeon role tank` |
| `/pvp join` | PvP‑Queue | `/pvp join` |
| `/pvp top` | Top‑Liste | `/pvp top` |
| `/pvp season` | Aktive Saison | `/pvp season` |
| `/voicechat party` | Party‑Sprachkanal | `/voicechat party` |
| `/voicechat guild` | Gilden‑Sprachkanal | `/voicechat guild` |
| `/voicechat leave` | Sprachkanal verlassen | `/voicechat leave` |
| `/lootchat [true|false]` | Loot‑Chat toggle | `/lootchat false` |

## RPG – Adminbefehle

| Befehl | Beschreibung | Beispiel |
| --- | --- | --- |
| `/rpgadmin` | Admin‑Menü öffnen | `/rpgadmin` |
| `/rpgadmin wand` | Editor‑Wand (Pos1/Pos2) | `/rpgadmin wand` |
| `/rpgadmin zone create <id>` | Zone erstellen | `/rpgadmin zone create startzone` |
| `/rpgadmin zone setlevel <id> <min> <max>` | Level‑Range | `/rpgadmin zone setlevel startzone 1 10` |
| `/rpgadmin zone setmod <id> <slow> <damage>` | Zone‑Modifier | `/rpgadmin zone setmod startzone 0.9 1.1` |
| `/rpgadmin npc create <id> <role>` | NPC erstellen | `/rpgadmin npc create guide QUESTGIVER` |
| `/rpgadmin npc dialog <id>` | Dialog setzen | `/rpgadmin npc dialog guide` |
| `/rpgadmin npc linkquest <npcId> <questId>` | Quest verlinken | `/rpgadmin npc linkquest guide starter` |
| `/rpgadmin npc linkshop <npcId> <shopId>` | Shop verlinken | `/rpgadmin npc linkshop guide village_merchant` |
| `/rpgadmin npc faction <npcId> <factionId>` | NPC‑Fraktion setzen | `/rpgadmin npc faction guide adventurers` |
| `/rpgadmin npc rank <npcId> <rankId>` | Ruf‑Rank‑Gate | `/rpgadmin npc rank guide revered` |
| `/rpgadmin quest create <id> <name>` | Quest erstellen | `/rpgadmin quest create starter "Start"` |
| `/rpgadmin quest addstep <id> <type> <target> <amount>` | Quest‑Step | `/rpgadmin quest addstep starter KILL ZOMBIE 3` |
| `/rpgadmin loot create <id> <appliesTo>` | Loot‑Table | `/rpgadmin loot create forest ZOMBIE` |
| `/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>` | Loot‑Entry | `/rpgadmin loot addentry forest GOLD_NUGGET 0.5 1 3 COMMON` |
| `/rpgadmin skill create <id>` | Skill erstellen | `/rpgadmin skill create dash` |
| `/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>` | Skill‑Feld setzen | `/rpgadmin skill set dash cooldown 3` |
| `/rpgadmin skill addeffect <id> <effectType> <param:value>...` | Skill‑Effekt | `/rpgadmin skill addeffect dash VELOCITY x:0.8 y:0.3` |
| `/rpgadmin mob spawn <mobId>` | Custom‑Mob spawnen | `/rpgadmin mob spawn forest_zombie` |
| `/rpgadmin spawner create <id> <zoneId>` | Spawner erstellen | `/rpgadmin spawner create forest_spawn startzone` |
| `/rpgadmin spawner addmob <id> <mobId> <chance>` | Mob hinzufügen | `/rpgadmin spawner addmob forest_spawn forest_zombie 1.0` |
| `/rpgadmin spawner setlimit <id> <amount>` | Spawn‑Limit | `/rpgadmin spawner setlimit forest_spawn 8` |
| `/rpgadmin event create <id> <zoneId> <name>` | Welt‑Event erstellen | `/rpgadmin event create invasion startzone "Invasion"` |
| `/rpgadmin event addstep <id> <type> <target> <amount>` | Event‑Step | `/rpgadmin event addstep invasion KILL ZOMBIE 50` |
| `/rpgadmin event reward <id> <xp> <gold>` | Event‑Belohnung | `/rpgadmin event reward invasion 250 200` |
| `/rpgadmin event unlock <id> <questId>` | Quest freischalten | `/rpgadmin event unlock invasion hero_path` |
| `/rpgadmin event start <id>` | Event starten | `/rpgadmin event start invasion` |
| `/rpgadmin event stop <id>` | Event stoppen | `/rpgadmin event stop invasion` |
| `/rpgadmin pvp seasonstart <id> <name> <days>` | Saison starten | `/rpgadmin pvp seasonstart s1 "Season 1" 30` |
| `/rpgadmin pvp seasonend` | Saison beenden | `/rpgadmin pvp seasonend` |
| `/rpgadmin node create <profession> <material> <respawnSeconds> <xp>` | Ressourcen‑Node | `/rpgadmin node create mining IRON_ORE 60 8` |
| `/rpgadmin build` / `/rpgadmin build gui` | Bau‑Manager GUI | `/rpgadmin build gui` |
| `/rpgadmin build <id>` | Gebäude platzieren | `/rpgadmin build cottage` |
| `/rpgadmin build undo` | Letzte Platzierung rückgängig | `/rpgadmin build undo` |
| `/rpgadmin build move` | Schematic‑Move GUI | `/rpgadmin build move` |
| `/rpgadmin perms` | Permissions‑GUI | `/rpgadmin perms` |
| `/rpgadmin perms role create <key> <displayName>` | Rolle erstellen | `/rpgadmin perms role create mod "Moderator"` |
| `/rpgadmin perms role delete <key>` | Rolle löschen | `/rpgadmin perms role delete mod` |
| `/rpgadmin perms role rename <key> <displayName>` | Rolle umbenennen | `/rpgadmin perms role rename mod "Mod"` |
| `/rpgadmin perms role parent add <role> <parent>` | Parent hinzufügen | `/rpgadmin perms role parent add mod admin` |
| `/rpgadmin perms role parent remove <role> <parent>` | Parent entfernen | `/rpgadmin perms role parent remove mod admin` |
| `/rpgadmin perms role node <role> <node> <allow|deny|inherit>` | Node setzen | `/rpgadmin perms role node mod rpg.admin allow` |
| `/rpgadmin perms user setprimary <player> <role>` | Primary‑Rolle | `/rpgadmin perms user setprimary Alex mod` |
| `/rpgadmin perms user add <player> <role>` | Rolle hinzufügen | `/rpgadmin perms user add Alex mod` |
| `/rpgadmin perms user remove <player> <role>` | Rolle entfernen | `/rpgadmin perms user remove Alex mod` |
| `/rpgadmin perms user info <player> <node>` | Node prüfen | `/rpgadmin perms user info Alex rpg.admin` |
| `/behavior edit <tree>` | Behavior‑Editor | `/behavior edit skeleton_king` |

## In‑Game‑Möglichkeiten (GUI & Systeme)

- **RPG Menü** (`/rpg`): Charakterübersicht, Skills, Quests, Fraktionen.
- **Skill‑GUI** (`/rpg` → Skills): Lernen, Tooltip‑Details, Skillpunkte.
- **Skillbaum** (`/rpg skilltree`): Visueller Skill‑Tree.
- **Admin‑Menü** (`/rpgadmin`): Zonen, NPCs, Quests, Loot, Skills, Debug, Bau‑Manager, Permissions.
- **Bau‑Manager**: Kategorien, Gebäude, Einzel‑Schemata, Undo, Move‑GUI.
- **Permissions‑GUI**: Rollen, Vererbung, Nodes, Spielerrollen, Audit‑Log.
- **Behavior‑Editor**: KI‑Bäume für Mobs per GUI.
- **Voice‑Chat‑Kanäle**: Party/Gilden‑Channels per `/voicechat`.
- **Auktionshaus**: Item‑Listings per `/auction`.
- **Gilden‑Bank & Quests**: Verwaltung per `/guild`.
- **Welt‑Events**: Serverweite Fortschritte & Belohnungen.
- **Fraktions‑Ränge**: Shop‑Rabatte und Dungeon‑Zugänge.
- **Dungeon‑Matchmaking**: Rollen‑Queue, Skalierung, No‑Death‑Bonus.
- **Crafting‑Aufträge**: Spieler‑Aufträge mit Goldbelohnung.
- **Ressourcen‑Nodes**: Respawnende Sammelknoten mit Beruf‑XP.
- **Housing & Gildenhallen**: Homes, Teleport‑/Craft‑/Buff‑Upgrades.
- **Skill‑Synergien**: Kombos über Party/Gilde.
- **PvP‑Saisons**: Saison‑Titel und Cosmetics.
