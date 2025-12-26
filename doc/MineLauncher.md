# CodeDump for Project: `MineLauncher.zip`

_Generated on 2025-12-26T12:47:48.073Z_

## File: `MineLauncher/admin_example.md`  
- Path: `MineLauncher/admin_example.md`  
- Size: 17861 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```markdown
# ⚔️ MineLauncher RPG - Admin Handbuch

Willkommen beim **MineLauncher RPG System**. Dieses Plugin verwandelt einen PaperMC Server in ein voll funktionsfähiges MMORPG mit Klassen, Skills, Dungeons, Gilden und einer persistenten SQL-Datenbank.

## 📋 Voraussetzungen
*   **Server:** PaperMC 1.20.4 (oder neuer)
*   **Java:** JDK 17+
*   **Datenbank:** PostgreSQL (zwingend erforderlich für Spielerdaten)

## 🧰 Launcher-CLI (außerhalb des Spiels)

*   **Client starten:** `dotnet run --project C_launcher -- play [Name]`  
    Beispiel: `dotnet run --project C_launcher -- play Alex`

---

## 🚀 Schritt 0: Installation & Datenbank (WICHTIG)

Bevor du mit dem Gameplay-Setup beginnst, muss die Technik stehen.

1.  **Plugin installieren:**
    *   Platziere die `rpg-plugin-1.0.0.jar` im `plugins/` Ordner.
    *   Starte den Server einmal, um die Configs zu generieren.
    *   Stoppe den Server.

2.  **Datenbank verbinden (`config.yml`):**
    Öffne `plugins/RPGPlugin/config.yml` und trage deine PostgreSQL-Daten ein:
    ```yaml
    database:
      host: localhost
      port: 5432
      name: rpg_db
      user: dein_user
      password: dein_passwort
      poolSize: 10
    ```

3.  **Server starten:**
    Beim Start erstellt das Plugin automatisch die Tabellen (`rpg_players`, `rpg_skills`, etc.).

---

# 🛠️ Master-Guide: Von Null auf Spielbar

Dieser Guide führt dich durch die Erstellung des Contents.
**Ziel:** Am Ende können sich zwei Spieler einloggen, Klassen wählen, in einer Party leveln, handeln, Gilden gründen und Dungeons bezwingen.

## 🏗️ Teil 1: Das Fundament (Klassen & Skills)

### 1. Skills definieren (`skills.yml`)
Wir erstellen einen **"Feuerball"** für Magier und einen **"Wuchtschlag"** für Krieger.

```yaml
fireball:
  name: "Feuerball"
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
  name: "Wuchtschlag"
  type: ACTIVE
  category: ATTACK
  cooldown: 8
  manaCost: 15
  effects:
  - type: DAMAGE
    params: { amount: 12, radius: 3 } # Flächenschaden
  - type: PARTICLE
    params: { type: EXPLOSION_LARGE, count: 3 }
  - type: SOUND
    params: { sound: ENTITY_GENERIC_EXPLODE, volume: 0.5 }
```

### 2. Klassen definieren (`classes.yml`)
Hier weisen wir die Skills den Klassen zu.

```yaml
mage:
  name: "&9Magier"
  startSkills:
  - fireball

warrior:
  name: "&cKrieger"
  startSkills:
  - smash
```

> 🔄 **Tipp:** Nutze `/reload` (oder Server-Neustart) nach Config-Änderungen.

---

## 💰 Teil 2: Wirtschaft & Items

### 1. Loot-Table erstellen (`loot.yml`)
Monster sollen Gold und seltene Waffen droppen.

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
    rarity: RARE # Stats werden automatisch generiert!
```

### 2. Shop definieren (`shops.yml`)
Ein Händler für An- und Verkauf.

```yaml
village_merchant:
  title: "Dorfhändler"
  items:
    - slot: 0
      material: GOLD_NUGGET
      name: "&eGoldklumpen"
      buyPrice: 0      # Nicht kaufbar
      sellPrice: 10    # Verkaufspreis
    - slot: 1
      material: POTION
      name: "&cHeiltrank"
      buyPrice: 50
      sellPrice: 0
```

### 3. Händler-NPC aufstellen (In-Game)
1.  Gehe zum Dorfplatz.
2.  `/rpgadmin npc create kaufmann VENDOR`
3.  `/rpgadmin npc linkshop kaufmann village_merchant`
4.  *(Optional)* Dialog: `/rpgadmin npc dialog kaufmann` -> Chatte: "Ich kaufe dein Gold!"

---

## 🌍 Teil 3: Die Welt (Mobs & Zonen)

### 1. Custom Mob erstellen (`mobs.yml`)

```yaml
forest_zombie:
  name: "&2Waldschlurfer"
  type: ZOMBIE
  health: 30
  damage: 5
  xp: 15
  lootTable: starter_loot
  skillIntervalSeconds: 10
  skills: []
```

### 2. Zone und Spawner einrichten (In-Game)
1.  **Zone definieren:**
    *   Hole den Zauberstab: `/rpgadmin wand`
    *   Markiere den Bereich (Links-/Rechtsklick).
    *   Erstelle Zone: `/rpgadmin zone create wald_zone`
    *   Setze Level: `/rpgadmin zone setlevel wald_zone 1 10`
2.  **Spawner aktivieren:**
    *   `/rpgadmin spawner create wald_spawner wald_zone`
    *   `/rpgadmin spawner addmob wald_spawner forest_zombie 1.0`
    *   `/rpgadmin spawner setlimit wald_spawner 8`

---

## 📜 Teil 4: Quests & Story

1.  **Quest anlegen:**
    *   `/rpgadmin quest create wald_reinigung "Reinigung des Waldes"`
    *   `/rpgadmin quest addstep wald_reinigung KILL ZOMBIE 5`
    *   `/rpgadmin quest addstep wald_reinigung COLLECT GOLD_NUGGET 3`
2.  **Quest-NPC spawnen:**
    *   `/rpgadmin npc create wache QUESTGIVER`
    *   `/rpgadmin npc dialog wache` -> Chatte: "Der Wald ist unsicher. Hilf uns!"
    *   `/rpgadmin npc linkquest wache wald_reinigung`

---

## 🏘️ Teil 5: Gebäude & Schemata (Neu)

### 1. Schematic-Ordner
*   Lege deine `.schem` Dateien in `plugins/RPGPlugin/schematics/` ab (oder passe `building.schematicsFolder` in `config.yml` an).

### 2. Gebäude definieren (`buildings.yml`)
Beispiel für mehrstöckige Gebäude, Keller und Einrichtung:

```yaml
buildings:
  cottage:
    name: "Kleines Wohnhaus"
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

### 3. Gebäude platzieren (GUI)
1.  Öffne das Admin-Menü: `/rpgadmin`
2.  Klicke **Bau-Manager** → Kategorie → Gebäude.
3.  Rechtsklick auf einen Block, um das Gebäude zu platzieren.

### 4. Einzel-Schema platzieren (GUI)
1.  Öffne das Admin-Menü: `/rpgadmin`
2.  Klicke **Bau-Manager** → **Einzel-Schema**.
3.  Gib den Dateinamen ein (z.B. `haus.schem`).
4.  Rechtsklick auf einen Block, um nur dieses Schema zu platzieren.

### 5. Platzierung rückgängig machen
*   `/rpgadmin build undo` macht die letzte Platzierung für den Admin rückgängig.

---

## 🤝 Teil 6: Social & End-Game

### 1. Dungeons (Instanziert)
Erstellt eine temporäre Welt für eine Gruppe.
*   **Befehl:** `/dungeon generate wfc` (Wave Function Collapse) oder `/dungeon generate gruft` (Standard).
*   **Logik:** Generiert Welt → Teleportiert Party → Spawnt Boss → Löscht Welt nach Abschluss/Timeout.

### 2. Gilden & Party
*   **Party:** `/party invite <Name>` (Teilt XP im Umkreis).
*   **Gilde:** `/guild create <ID> <Name>` (Gilden mit Bank und Quests).

### 3. Auktionshaus
*   Item in die Hand nehmen → `/auction sell <Preis>`.

### 4. PvP & Elo
*   Arenen müssen in `arenas.yml` konfiguriert werden.
*   Spieler nutzen `/pvp join`, um in die Warteschlange für Elo-Matches zu kommen.

---

## 🧠 Teil 7: Behavior-Editor (Mob KI)

Mit dem Behavior-Tree-Editor kannst du KI-Bäume für Mobs erstellen und testen.

### 1. Behavior-Editor öffnen
*   `/behavior edit <tree>` öffnet den Editor.
*   Beispiel: `/behavior edit skeleton_king`

> Tipp: Der Tree-Name muss mit `behaviorTree` in `mobs.yml` übereinstimmen (z. B. `skeleton_king`).

### 2. Basis-Workflow
1.  Editor öffnen: `/behavior edit skeleton_king`
2.  Knoten hinzufügen (z. B. `Selector`, `Sequence`, `Cooldown`, `MeleeAttack`, `CastSkill`)
3.  Speichern/Anwenden im GUI (je nach Buttons im Editor)
4.  Mob spawnen, um das Verhalten zu testen.

### 3. Beispiel-Baum (Konzept)
Ziel: Der Boss greift im Nahkampf an und nutzt alle 8 Sekunden einen Skill.

*   **Selector**
    *   **Sequence**
        *   `Cooldown(8s)`
        *   `CastSkill(ember_shot)`
    *   `MeleeAttack`

### 4. Beispiel in `mobs.yml`
```yaml
skeleton_king:
  name: "&cSkelettkönig"
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

> Danach `/behavior edit skeleton_king` öffnen und den Baum passend aufbauen.

---

## ✅ Walkthrough: Das Spieler-Erlebnis

So sieht der Loop für deine Spieler aus:

1.  **Start:** Login → `/rpg class choose warrior`.
2.  **Setup:** Skill binden → `/rpg bind 2 smash`.
3.  **Quest:** Zur **Wache** gehen, Quest annehmen.
4.  **Kampf:** In die **Wald-Zone** laufen. Mobs spawnen automatisch.
5.  **Action:** Taste 2 (Rechtsklick) → Skill löst aus.
6.  **Loot:** Mobs droppen Gold und seltene Items mit Stats.
7.  **Handel:** Zurück zum **Kaufmann** → Gold verkaufen.
8.  **Abschluss:** Quest bei der Wache abgeben → XP & Level Up.
9.  **Endgame:** Freunde einladen, Gilde gründen, Dungeon starten!

---

## 🔧 Wichtige Admin-Befehle

| Befehl | Beschreibung |
| :--- | :--- |
| `/rpgadmin wand` | Gibt das Tool zum Markieren von Zonen. |
| `/rpgadmin zone` | Verwalten von Regionen und Level-Ranges. |
| `/rpgadmin npc` | Erstellen und Konfigurieren von NPCs. |
| `/rpgadmin quest` | Quests ingame erstellen. |
| `/rpgadmin skill` | Skills bearbeiten/erstellen. |
| `/rpgadmin mob` | Manuelles Spawnen von Custom Mobs. |
| `/rpgadmin spawner` | Konfiguration der automatischen Spawner. |
| `/rpgadmin build gui` | Öffnet den Bau-Manager. |
| `/rpgadmin build <id>` | Startet Platzierung eines Gebäudes. |
| `/rpgadmin build undo` | Macht die letzte Platzierung rückgängig. |

---

# 📌 Vollständige Befehlsübersicht (mit Beispielen)

## Spieler-Befehle

| Befehl | Beispiel | Zweck |
| --- | --- | --- |
| `/rpg` | `/rpg` | Öffnet das RPG-Menü (Charakter, Skills, Quests, Fraktionen) |
| `/rpg skill <id>` | `/rpg skill dash` | Skill aktivieren |
| `/rpg quest list` | `/rpg quest list` | Questliste öffnen |
| `/rpg quest abandon <id>` | `/rpg quest abandon starter` | Quest abbrechen |
| `/rpg quest complete <id>` | `/rpg quest complete starter` | Quest-Abschluss prüfen |
| `/rpg respec` | `/rpg respec` | Skills/Attribute reset |
| `/rpg class list` | `/rpg class list` | Klassen auflisten |
| `/rpg class choose <id>` | `/rpg class choose mage` | Klasse wählen |
| `/rpg bind <slot 1-9> <skillId>` | `/rpg bind 2 heal` | Skill auf Slot binden |
| `/rpg money` | `/rpg money` | Gold anzeigen |
| `/rpg pay <player> <amount>` | `/rpg pay Alex 50` | Gold senden |
| `/rpg profession list` | `/rpg profession list` | Berufe anzeigen |
| `/rpg profession set <name> <level>` | `/rpg profession set mining 5` | Beruf setzen |
| `/rpg skilltree` | `/rpg skilltree` | Skillbaum öffnen |

## Party

| Befehl | Beispiel | Zweck |
| --- | --- | --- |
| `/party create` | `/party create` | Party erstellen |
| `/party invite <player>` | `/party invite Alex` | Spieler einladen |
| `/party join <leader>` | `/party join Alex` | Party beitreten |
| `/party leave` | `/party leave` | Party verlassen |
| `/party chat <message>` | `/party chat Hallo` | Party-Chat |
| `/p <...>` | `/p chat Hi` | Shortcut für `/party` |

## Gilden

| Befehl | Beispiel | Zweck |
| --- | --- | --- |
| `/guild create <id> <name>` | `/guild create knights "Die Ritter"` | Gilde erstellen |
| `/guild invite <player>` | `/guild invite Alex` | Einladen |
| `/guild accept` | `/guild accept` | Einladung annehmen |
| `/guild leave` | `/guild leave` | Gilde verlassen |
| `/guild disband` | `/guild disband` | Gilde auflösen |
| `/guild info` | `/guild info` | Info anzeigen |
| `/guild chat <message>` | `/guild chat Hallo` | Gilden-Chat |
| `/guild bank balance` | `/guild bank balance` | Bankstand |
| `/guild bank deposit <amount>` | `/guild bank deposit 100` | Einzahlen |
| `/guild bank withdraw <amount>` | `/guild bank withdraw 50` | Abheben |
| `/guild quest list` | `/guild quest list` | Gildenquests |
| `/guild quest create <id> <goal> <name>` | `/guild quest create wolfhunt 25 "Wolfjagd"` | Quest erstellen |
| `/guild quest progress <id> <amount>` | `/guild quest progress wolfhunt 5` | Fortschritt |
| `/guild quest complete <id>` | `/guild quest complete wolfhunt` | Quest abschließen |
| `/g <message>` | `/g Hallo` | Shortcut Gilden-Chat |

## Handel, Auktion, PvP, Dungeons

| Befehl | Beispiel | Zweck |
| --- | --- | --- |
| `/auction list` | `/auction list` | Auktionen anzeigen |
| `/auction sell <price>` | `/auction sell 250` | Item verkaufen |
| `/auction buy <id>` | `/auction buy 1a2b3c4d` | Auktion kaufen |
| `/trade request <player>` | `/trade request Alex` | Handel anfragen |
| `/trade accept` | `/trade accept` | Handel annehmen |
| `/trade offer <gold>` | `/trade offer 100` | Gold anbieten |
| `/trade requestgold <gold>` | `/trade requestgold 50` | Gold verlangen |
| `/trade ready` | `/trade ready` | Handel bestätigen |
| `/trade cancel` | `/trade cancel` | Handel abbrechen |
| `/dungeon enter` | `/dungeon enter` | Dungeon betreten |
| `/dungeon leave` | `/dungeon leave` | Dungeon verlassen |
| `/dungeon generate <theme>` | `/dungeon generate wfc` | Instanz generieren |
| `/pvp join` | `/pvp join` | PvP-Queue |
| `/pvp top` | `/pvp top` | Rangliste |
| `/voicechat party` | `/voicechat party` | Party-Voice |
| `/voicechat guild` | `/voicechat guild` | Gilden-Voice |
| `/voicechat leave` | `/voicechat leave` | Voice verlassen |
| `/lootchat [true|false]` | `/lootchat false` | Lootchat an/aus |

## Admin-Befehle

| Befehl | Beispiel | Zweck |
| --- | --- | --- |
| `/rpgadmin` | `/rpgadmin` | Admin-Menü |
| `/rpgadmin wand` | `/rpgadmin wand` | Editor-Wand |
| `/rpgadmin zone create <id>` | `/rpgadmin zone create startzone` | Zone erstellen |
| `/rpgadmin zone setlevel <id> <min> <max>` | `/rpgadmin zone setlevel startzone 1 10` | Level-Range |
| `/rpgadmin zone setmod <id> <slow> <damage>` | `/rpgadmin zone setmod startzone 0.9 1.1` | Zone-Modifier |
| `/rpgadmin npc create <id> <role>` | `/rpgadmin npc create guide QUESTGIVER` | NPC erstellen |
| `/rpgadmin npc dialog <id>` | `/rpgadmin npc dialog guide` | Dialog setzen |
| `/rpgadmin npc linkquest <npcId> <questId>` | `/rpgadmin npc linkquest guide starter` | Quest verlinken |
| `/rpgadmin npc linkshop <npcId> <shopId>` | `/rpgadmin npc linkshop guide village_merchant` | Shop verlinken |
| `/rpgadmin quest create <id> <name>` | `/rpgadmin quest create starter "Start"` | Quest erstellen |
| `/rpgadmin quest addstep <id> <type> <target> <amount>` | `/rpgadmin quest addstep starter KILL ZOMBIE 3` | Quest-Step |
| `/rpgadmin loot create <id> <appliesTo>` | `/rpgadmin loot create forest ZOMBIE` | Loot-Tabelle |
| `/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>` | `/rpgadmin loot addentry forest GOLD_NUGGET 0.5 1 3 COMMON` | Loot-Entry |
| `/rpgadmin skill create <id>` | `/rpgadmin skill create dash` | Skill erstellen |
| `/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>` | `/rpgadmin skill set dash cooldown 3` | Skill setzen |
| `/rpgadmin skill addeffect <id> <effectType> <param:value>...` | `/rpgadmin skill addeffect dash VELOCITY x:0.8 y:0.3` | Effekt hinzufügen |
| `/rpgadmin mob spawn <mobId>` | `/rpgadmin mob spawn forest_zombie` | Custom-Mob spawnen |
| `/rpgadmin spawner create <id> <zoneId>` | `/rpgadmin spawner create forest_spawn startzone` | Spawner erstellen |
| `/rpgadmin spawner addmob <id> <mobId> <chance>` | `/rpgadmin spawner addmob forest_spawn forest_zombie 1.0` | Mob hinzufügen |
| `/rpgadmin spawner setlimit <id> <amount>` | `/rpgadmin spawner setlimit forest_spawn 8` | Spawn-Limit |
| `/rpgadmin build` / `/rpgadmin build gui` | `/rpgadmin build gui` | Bau-Manager |
| `/rpgadmin build <id>` | `/rpgadmin build cottage` | Gebäude platzieren |
| `/rpgadmin build undo` | `/rpgadmin build undo` | Undo |
| `/rpgadmin build move` | `/rpgadmin build move` | Move-GUI |
| `/rpgadmin perms` | `/rpgadmin perms` | Permissions-GUI |
| `/rpgadmin perms role create <key> <displayName>` | `/rpgadmin perms role create mod "Moderator"` | Rolle erstellen |
| `/rpgadmin perms role delete <key>` | `/rpgadmin perms role delete mod` | Rolle löschen |
| `/rpgadmin perms role rename <key> <displayName>` | `/rpgadmin perms role rename mod "Mod"` | Rolle umbenennen |
| `/rpgadmin perms role parent add <role> <parent>` | `/rpgadmin perms role parent add mod admin` | Parent hinzufügen |
| `/rpgadmin perms role parent remove <role> <parent>` | `/rpgadmin perms role parent remove mod admin` | Parent entfernen |
| `/rpgadmin perms role node <role> <node> <allow|deny|inherit>` | `/rpgadmin perms role node mod rpg.admin allow` | Node setzen |
| `/rpgadmin perms user setprimary <player> <role>` | `/rpgadmin perms user setprimary Alex mod` | Primary Rolle |
| `/rpgadmin perms user add <player> <role>` | `/rpgadmin perms user add Alex mod` | Rolle hinzufügen |
| `/rpgadmin perms user remove <player> <role>` | `/rpgadmin perms user remove Alex mod` | Rolle entfernen |
| `/rpgadmin perms user info <player> <node>` | `/rpgadmin perms user info Alex rpg.admin` | Node prüfen |
| `/behavior edit <tree>` | `/behavior edit skeleton_king` | Behavior-Editor |

---

# 🧭 In-Game Möglichkeiten (GUI & Systeme)

- **RPG-Menü**: `/rpg` öffnet Charakter, Skills, Quests, Fraktionen.
- **Skill-GUI**: Skills lernen und verwalten.
- **Skillbaum**: `/rpg skilltree` öffnet den visuellen Baum.
- **Admin-Menü**: `/rpgadmin` mit Debug, Bau-Manager, Permissions.
- **Bau-Manager**: Kategorien, Gebäude, Einzel-Schemata, Undo, Move-GUI.
- **Permissions-GUI**: Rollen, Vererbung, Nodes, Spielerrollen, Audit-Log.
- **Behavior-Editor**: KI-Bäume per GUI.
- **Voice-Chat**: Party/Gilden-Channels via `/voicechat`.
- **Auktionshaus**: Listings via `/auction`.
- **Gildenbank & Gildenquests** per `/guild`.
- **Dungeon-Instanzen** via `/dungeon generate`.
- **PvP-Matchmaking** via `/pvp join`.

```

## File: `MineLauncher/C_launcher.csproj`  
- Path: `MineLauncher/C_launcher.csproj`  
- Size: 431 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <OutputType>WinExe</OutputType>
    <TargetFramework>net8.0-windows</TargetFramework>
    <UseWindowsForms>true</UseWindowsForms>
    <ImplicitUsings>enable</ImplicitUsings>
    <Nullable>enable</Nullable>
    <RootNamespace>CLauncher</RootNamespace>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="CmlLib.Core" Version="4.0.6" />
  </ItemGroup>

</Project>

```

## File: `MineLauncher/MainForm.cs`  
- Path: `MineLauncher/MainForm.cs`  
- Size: 9958 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using System.Diagnostics;
using CLauncher.Models;
using CLauncher.Services;

namespace CLauncher;

public sealed class MainForm : Form
{
    private readonly string _basePath;
    private readonly ConfigService _configService;
    private readonly DownloadService _downloadService;
    private readonly ServerService _serverService;
    private readonly LauncherService _launcherService;
    private readonly ClientLauncherService _clientLauncherService;

    private LauncherConfig _config;

    private readonly Button _initButton;
    private readonly Button _openConfigButton;
    private readonly Button _downloadButton;
    private readonly Button _launchButton;
    private readonly Button _launchClientButton;
    private readonly Button _showConfigButton;
    private readonly Button _saveConfigButton;
    private readonly TextBox _statusBox;
    private readonly TextBox _configEditor;

    public MainForm(
        string basePath,
        ConfigService configService,
        DownloadService downloadService,
        ServerService serverService,
        LauncherService launcherService,
        ClientLauncherService clientLauncherService)
    {
        _basePath = basePath;
        _configService = configService;
        _downloadService = downloadService;
        _serverService = serverService;
        _launcherService = launcherService;
        _clientLauncherService = clientLauncherService;

        Text = "C_launcher";
        Width = 900;
        Height = 600;
        StartPosition = FormStartPosition.CenterScreen;
        DoubleBuffered = true;
        BackColor = Color.FromArgb(15, 17, 26);
        ForeColor = Color.WhiteSmoke;

        _initButton = new Button { Text = "Config erstellen", Width = 150 };
        _openConfigButton = new Button { Text = "Config öffnen", Width = 150 };
        _downloadButton = new Button { Text = "Server downloaden", Width = 150 };
        _launchButton = new Button { Text = "Server starten", Width = 150 };
        _launchClientButton = new Button { Text = "Client starten", Width = 150 };
        _showConfigButton = new Button { Text = "Config laden", Width = 150 };
        _saveConfigButton = new Button { Text = "Config speichern", Width = 150 };

        StyleButton(_initButton);
        StyleButton(_openConfigButton);
        StyleButton(_downloadButton);
        StyleButton(_launchButton);
        StyleButton(_launchClientButton);
        StyleButton(_showConfigButton);
        StyleButton(_saveConfigButton);

        _statusBox = new TextBox
        {
            Multiline = true,
            ReadOnly = true,
            ScrollBars = ScrollBars.Vertical,
            Dock = DockStyle.Fill,
            BackColor = Color.FromArgb(12, 14, 22),
            ForeColor = Color.Gainsboro,
            BorderStyle = BorderStyle.FixedSingle,
            Font = new Font("Consolas", 10, FontStyle.Regular)
        };

        _configEditor = new TextBox
        {
            Multiline = true,
            ScrollBars = ScrollBars.Vertical,
            Dock = DockStyle.Fill,
            BackColor = Color.FromArgb(12, 14, 22),
            ForeColor = Color.Gainsboro,
            BorderStyle = BorderStyle.FixedSingle,
            Font = new Font("Consolas", 10, FontStyle.Regular)
        };

        var buttonPanel = new FlowLayoutPanel
        {
            Dock = DockStyle.Top,
            Height = 120,
            Padding = new Padding(10),
            AutoSize = false,
            BackColor = Color.FromArgb(30, 34, 48)
        };

        buttonPanel.Controls.AddRange(new Control[]
        {
            _initButton,
            _openConfigButton,
            _showConfigButton,
            _saveConfigButton,
            _downloadButton,
            _launchButton,
            _launchClientButton
        });

        var contentPanel = new SplitContainer
        {
            Dock = DockStyle.Fill,
            Orientation = Orientation.Vertical,
            SplitterDistance = 420,
            BackColor = Color.FromArgb(15, 17, 26)
        };

        contentPanel.Panel1.Controls.Add(_statusBox);
        contentPanel.Panel2.Controls.Add(_configEditor);

        Controls.Add(contentPanel);
        Controls.Add(buttonPanel);

        _initButton.Click += async (_, _) => await RunActionAsync(InitConfigAsync);
        _openConfigButton.Click += (_, _) => OpenConfig();
        _showConfigButton.Click += (_, _) => ShowConfig();
        _saveConfigButton.Click += (_, _) => SaveConfigFromEditor();
        _downloadButton.Click += async (_, _) => await RunActionAsync(DownloadServerAsync);
        _launchButton.Click += async (_, _) => await RunActionAsync(LaunchServerAsync);
        _launchClientButton.Click += async (_, _) => await RunActionAsync(LaunchClientAsync);

        _config = _configService.LoadOrCreate();
        AppendStatus("Launcher bereit. Config geladen.");
        AppendStatus($"Config: {_configService.ConfigPath}");
        LoadConfigToEditor();
    }

    private static void StyleButton(Button button)
    {
        button.Height = 40;
        button.Margin = new Padding(6);
        button.FlatStyle = FlatStyle.Flat;
        button.BackColor = Color.FromArgb(45, 110, 210);
        button.ForeColor = Color.WhiteSmoke;
        button.Font = new Font("Segoe UI", 10, FontStyle.Bold);
        button.FlatAppearance.BorderSize = 1;
        button.FlatAppearance.BorderColor = Color.FromArgb(90, 140, 230);
        button.FlatAppearance.MouseDownBackColor = Color.FromArgb(35, 85, 170);
        button.FlatAppearance.MouseOverBackColor = Color.FromArgb(60, 130, 230);
    }

    private Task InitConfigAsync()
    {
        _configService.Save(_config);
        AppendStatus("Config gespeichert.");
        LoadConfigToEditor();
        return Task.CompletedTask;
    }

    private void OpenConfig()
    {
        if (!File.Exists(_configService.ConfigPath))
        {
            _configService.Save(_config);
        }

        Process.Start(new ProcessStartInfo
        {
            FileName = _configService.ConfigPath,
            UseShellExecute = true
        });
    }

    private void ShowConfig()
    {
        if (!File.Exists(_configService.ConfigPath))
        {
            _configService.Save(_config);
        }

        LoadConfigToEditor();
    }

    private void LoadConfigToEditor()
    {
        _config = _configService.LoadOrCreate();
        _configEditor.Text = _configService.Serialize(_config);
    }

    private void SaveConfigFromEditor()
    {
        try
        {
            var parsedConfig = _configService.Deserialize(_configEditor.Text);
            _configService.Save(parsedConfig);
            _config = parsedConfig;
            AppendStatus("Config gespeichert.");
        }
        catch (Exception ex)
        {
            AppendStatus($"Fehler beim Speichern: {ex.Message}");
        }
    }

    private async Task DownloadServerAsync()
    {
        _config = _configService.LoadOrCreate();

        var serverDir = _serverService.ResolveServerDirectory(_config, _basePath);
        var jarPath = _serverService.GetServerJarPath(_config, serverDir);

        PaperBuildInfo? buildInfo = null;
        string? vanillaUrl = null;
        if (_config.Server.Type.Equals("paper", StringComparison.OrdinalIgnoreCase))
        {
            var build = _config.Server.PaperBuild
                ?? (await _downloadService.GetLatestPaperBuildAsync(_config.Server.Version, CancellationToken.None)).Build;
            buildInfo = new PaperBuildInfo(_config.Server.Version, build);
        }
        else if (_config.Server.Type.Equals("vanilla", StringComparison.OrdinalIgnoreCase))
        {
            vanillaUrl = await _downloadService.GetVanillaServerUrlAsync(_config.Server.Version, CancellationToken.None);
        }

        var url = _serverService.BuildDownloadUrl(_config.Server, buildInfo, vanillaUrl);
        AppendStatus($"Download: {url}");

        await _downloadService.DownloadFileAsync(url, jarPath, CancellationToken.None);
        _serverService.EnsureEula(_config.Server, serverDir);
        _serverService.EnsureServerProperties(_config, serverDir);
        AppendStatus("Download abgeschlossen.");
    }

    private async Task LaunchServerAsync()
    {
        _config = _configService.LoadOrCreate();

        var serverDir = _serverService.ResolveServerDirectory(_config, _basePath);
        var jarPath = _serverService.GetServerJarPath(_config, serverDir);

        if (!File.Exists(jarPath))
        {
            await DownloadServerAsync();
        }

        _serverService.EnsureEula(_config.Server, serverDir);
        _serverService.EnsureServerProperties(_config, serverDir);

        AppendStatus("Server wird gestartet...");
        _launcherService.LaunchServer(_config, serverDir, jarPath);
    }

    private async Task LaunchClientAsync()
    {
        _config = _configService.LoadOrCreate();
        AppendStatus("Client wird gestartet...");
        await _clientLauncherService.LaunchClientAsync(
            _config.Game.OfflineUsername,
            _config.Game.ClientVersion);
    }

    private async Task RunActionAsync(Func<Task> action)
    {
        SetButtonsEnabled(false);
        try
        {
            await action();
        }
        catch (Exception ex)
        {
            AppendStatus($"Fehler: {ex.Message}");
        }
        finally
        {
            SetButtonsEnabled(true);
        }
    }

    private void SetButtonsEnabled(bool enabled)
    {
        _initButton.Enabled = enabled;
        _openConfigButton.Enabled = enabled;
        _showConfigButton.Enabled = enabled;
        _saveConfigButton.Enabled = enabled;
        _downloadButton.Enabled = enabled;
        _launchButton.Enabled = enabled;
        _launchClientButton.Enabled = enabled;
    }

    private void AppendStatus(string message)
    {
        _statusBox.AppendText($"[{DateTime.Now:HH:mm:ss}] {message}{Environment.NewLine}");
    }
}

```

## File: `MineLauncher/Models/LauncherConfig.cs`  
- Path: `MineLauncher/Models/LauncherConfig.cs`  
- Size: 1251 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
namespace CLauncher.Models;

public sealed class LauncherConfig
{
    public string InstallRoot { get; set; } = "servers";
    public ServerConfig Server { get; set; } = new();
    public JavaConfig Java { get; set; } = new();
    public GameConfig Game { get; set; } = new();
}

public sealed class ServerConfig
{
    public string Name { get; set; } = "paper-server";
    public string Type { get; set; } = "paper"; // vanilla | paper | bukkit | craftbukkit
    public string Version { get; set; } = "1.20.4";
    public int? PaperBuild { get; set; }
    public bool OnlineMode { get; set; } = true;
    public int Port { get; set; } = 25565;
    public bool AcceptEula { get; set; } = false;
    public string JarFileName { get; set; } = "server.jar";
}

public sealed class JavaConfig
{
    public string Path { get; set; } = "java";
    public string MinMemory { get; set; } = "1G";
    public string MaxMemory { get; set; } = "2G";
}

public sealed class GameConfig
{
    public string ClientVersion { get; set; } = "1.20.4";
    public string GameDirectory { get; set; } = "client";
    public int MaxMemoryMb { get; set; } = 2048;
    public bool OfflineMode { get; set; } = true;
    public string OfflineUsername { get; set; } = "Player";
}

```

## File: `MineLauncher/plugins/RPGPlugin/pom.xml`  
- Path: `MineLauncher/plugins/RPGPlugin/pom.xml`  
- Size: 3608 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>rpg-plugin</artifactId>
    <version>1.0.0</version>
    <name>MineLauncherRPG</name>

    <properties>
        <java.version>17</java.version>
        <paper.version>1.20.4-R0.1-SNAPSHOT</paper.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>${paper.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.2</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.11.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <release>${java.version}</release>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <createDependencyReducedPom>false</createDependencyReducedPom>
                            <shadedArtifactAttached>false</shadedArtifactAttached>
                            <outputFile>${project.build.directory}/${project.artifactId}-${project.version}.jar</outputFile>
                            <filters>
                                <filter>
                                    <artifact>*:*</artifact>
                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                        <exclude>module-info.class</exclude>
                                        <exclude>META-INF/versions/**</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorContext.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorContext.java`  
- Size: 1165 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class BehaviorContext {
    private final RPGPlugin plugin;
    private final LivingEntity mob;
    private final MobDefinition definition;
    private Player target;
    private final Map<String, Long> cooldowns = new HashMap<>();

    public BehaviorContext(RPGPlugin plugin, LivingEntity mob, MobDefinition definition) {
        this.plugin = plugin;
        this.mob = mob;
        this.definition = definition;
    }

    public RPGPlugin plugin() {
        return plugin;
    }

    public LivingEntity mob() {
        return mob;
    }

    public MobDefinition definition() {
        return definition;
    }

    public Player target() {
        return target;
    }

    public void setTarget(Player target) {
        this.target = target;
    }

    public Map<String, Long> cooldowns() {
        return cooldowns;
    }

    public UUID mobId() {
        return mob.getUniqueId();
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorNode.java`  
- Size: 399 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import java.util.UUID;

public abstract class BehaviorNode {
    private final String id;

    protected BehaviorNode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public abstract BehaviorStatus tick(BehaviorContext context);

    protected String key(UUID entityId) {
        return id + ":" + entityId;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorStatus.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/BehaviorStatus.java`  
- Size: 104 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public enum BehaviorStatus {
    SUCCESS,
    FAILURE,
    RUNNING
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CastSkillNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CastSkillNode.java`  
- Size: 633 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class CastSkillNode extends BehaviorNode {
    private final String skillId;

    public CastSkillNode(String id, String skillId) {
        super(id);
        this.skillId = skillId;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        boolean success = context.plugin().useMobSkill(context.mob(), target, skillId);
        return success ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CompositeNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CompositeNode.java`  
- Size: 357 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeNode extends BehaviorNode {
    private final List<BehaviorNode> children = new ArrayList<>();

    protected CompositeNode(String id) {
        super(id);
    }

    public List<BehaviorNode> children() {
        return children;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CooldownNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/CooldownNode.java`  
- Size: 853 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public class CooldownNode extends BehaviorNode {
    private final BehaviorNode child;
    private final long cooldownMillis;

    public CooldownNode(String id, BehaviorNode child, long cooldownMillis) {
        super(id);
        this.child = child;
        this.cooldownMillis = cooldownMillis;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        long now = System.currentTimeMillis();
        String key = key(context.mobId());
        Long last = context.cooldowns().get(key);
        if (last != null && now - last < cooldownMillis) {
            return BehaviorStatus.FAILURE;
        }
        BehaviorStatus status = child.tick(context);
        if (status == BehaviorStatus.SUCCESS) {
            context.cooldowns().put(key, now);
        }
        return status;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/FleeNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/FleeNode.java`  
- Size: 850 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FleeNode extends BehaviorNode {
    public FleeNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        Location mobLoc = context.mob().getLocation();
        Location targetLoc = target.getLocation();
        Vector direction = mobLoc.toVector().subtract(targetLoc.toVector());
        if (direction.lengthSquared() == 0) {
            return BehaviorStatus.FAILURE;
        }
        direction.normalize().multiply(0.35);
        context.mob().setVelocity(direction);
        return BehaviorStatus.RUNNING;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/HealSelfNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/HealSelfNode.java`  
- Size: 534 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public class HealSelfNode extends BehaviorNode {
    private final double amount;

    public HealSelfNode(String id, double amount) {
        super(id);
        this.amount = amount;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        double maxHealth = Math.max(1, context.definition().health());
        double next = Math.min(maxHealth, context.mob().getHealth() + amount);
        context.mob().setHealth(next);
        return BehaviorStatus.SUCCESS;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/HealthBelowNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/HealthBelowNode.java`  
- Size: 547 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public class HealthBelowNode extends BehaviorNode {
    private final double threshold;

    public HealthBelowNode(String id, double threshold) {
        super(id);
        this.threshold = threshold;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        double maxHealth = Math.max(1, context.definition().health());
        double current = context.mob().getHealth();
        return (current / maxHealth) < threshold ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/InverterNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/InverterNode.java`  
- Size: 567 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public class InverterNode extends BehaviorNode {
    private final BehaviorNode child;

    public InverterNode(String id, BehaviorNode child) {
        super(id);
        this.child = child;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        BehaviorStatus status = child.tick(context);
        return switch (status) {
            case SUCCESS -> BehaviorStatus.FAILURE;
            case FAILURE -> BehaviorStatus.SUCCESS;
            case RUNNING -> BehaviorStatus.RUNNING;
        };
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/MeleeAttackNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/MeleeAttackNode.java`  
- Size: 638 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class MeleeAttackNode extends BehaviorNode {
    public MeleeAttackNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        if (target.getLocation().distanceSquared(context.mob().getLocation()) > 9) {
            return BehaviorStatus.FAILURE;
        }
        target.damage(context.definition().damage(), context.mob());
        return BehaviorStatus.SUCCESS;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/SelectorNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/SelectorNode.java`  
- Size: 515 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public class SelectorNode extends CompositeNode {
    public SelectorNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        for (BehaviorNode child : children()) {
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.SUCCESS || status == BehaviorStatus.RUNNING) {
                return status;
            }
        }
        return BehaviorStatus.FAILURE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/SequenceNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/SequenceNode.java`  
- Size: 608 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

public class SequenceNode extends CompositeNode {
    public SequenceNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        for (BehaviorNode child : children()) {
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.FAILURE) {
                return BehaviorStatus.FAILURE;
            }
            if (status == BehaviorStatus.RUNNING) {
                return BehaviorStatus.RUNNING;
            }
        }
        return BehaviorStatus.SUCCESS;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/TargetDistanceAboveNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/TargetDistanceAboveNode.java`  
- Size: 659 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class TargetDistanceAboveNode extends BehaviorNode {
    private final double distance;

    public TargetDistanceAboveNode(String id, double distance) {
        super(id);
        this.distance = distance;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        double dist = target.getLocation().distance(context.mob().getLocation());
        return dist > distance ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/WalkToTargetNode.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/behavior/WalkToTargetNode.java`  
- Size: 647 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.behavior;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class WalkToTargetNode extends BehaviorNode {
    public WalkToTargetNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        if (context.mob() instanceof Mob mob) {
            mob.setTarget(target);
            mob.getPathfinder().moveTo(target);
            return BehaviorStatus.RUNNING;
        }
        return BehaviorStatus.FAILURE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/AuctionCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/AuctionCommand.java`  
- Size: 4726 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.AuctionListing;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AuctionCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public AuctionCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/auction <list|sell|buy>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> listAuctions(player);
            case "sell" -> sellAuction(player, args);
            case "buy" -> buyAuction(player, args);
            default -> player.sendMessage(Text.mm("<gray>/auction <list|sell|buy>"));
        }
        return true;
    }

    private void listAuctions(Player player) {
        if (plugin.auctionHouseManager().listings().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Auktionen verfügbar."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Auktionen:"));
        for (AuctionListing listing : plugin.auctionHouseManager().listings().values()) {
            player.sendMessage(Text.mm("<gray>" + listing.id() + " - <gold>" + listing.price() + "</gold> Gold"));
        }
    }

    private void sellAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/auction sell <price>"));
            return;
        }
        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Preis ungültig."));
            return;
        }
        if (price <= 0) {
            player.sendMessage(Text.mm("<red>Preis muss > 0 sein."));
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(Text.mm("<red>Halte ein Item in der Hand."));
            return;
        }
        String data = plugin.auctionHouseManager().serializeItem(item);
        if (data == null) {
            player.sendMessage(Text.mm("<red>Item konnte nicht gespeichert werden."));
            return;
        }
        String id = UUID.randomUUID().toString().substring(0, 8);
        AuctionListing listing = new AuctionListing(id);
        listing.setSeller(player.getUniqueId());
        listing.setPrice(price);
        listing.setItemData(data);
        plugin.auctionHouseManager().addListing(listing);
        player.getInventory().setItemInMainHand(null);
        player.sendMessage(Text.mm("<green>Auktion erstellt: " + id));
    }

    private void buyAuction(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/auction buy <id>"));
            return;
        }
        String id = args[1];
        AuctionListing listing = plugin.auctionHouseManager().getListing(id);
        if (listing == null) {
            player.sendMessage(Text.mm("<red>Auktion nicht gefunden."));
            return;
        }
        var buyerProfile = plugin.playerDataManager().getProfile(player);
        if (buyerProfile.gold() < listing.price()) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        ItemStack item = plugin.auctionHouseManager().deserializeItem(listing.itemData());
        if (item == null) {
            player.sendMessage(Text.mm("<red>Item nicht verfügbar."));
            return;
        }
        buyerProfile.setGold(buyerProfile.gold() - listing.price());
        player.getInventory().addItem(item);
        if (listing.seller() != null) {
            var seller = plugin.getServer().getPlayer(listing.seller());
            if (seller != null) {
                var sellerProfile = plugin.playerDataManager().getProfile(seller);
                sellerProfile.setGold(sellerProfile.gold() + listing.price());
                seller.sendMessage(Text.mm("<green>Dein Item wurde verkauft für " + listing.price() + " Gold."));
            }
        }
        plugin.auctionHouseManager().removeListing(id);
        player.sendMessage(Text.mm("<green>Item gekauft."));
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/BehaviorCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/BehaviorCommand.java`  
- Size: 982 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BehaviorCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public BehaviorCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 2 || !"edit".equalsIgnoreCase(args[0])) {
            player.sendMessage(Text.mm("<gray>/behavior edit <tree>"));
            return true;
        }
        String treeName = args[1];
        plugin.behaviorTreeEditorGui().open(player, treeName);
        return true;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/DungeonCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/DungeonCommand.java`  
- Size: 2733 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DungeonCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public DungeonCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/dungeon <enter|leave|generate>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "enter" -> enterDungeon(player);
            case "leave" -> leaveDungeon(player);
            case "generate" -> generateDungeon(player, args);
            default -> player.sendMessage(Text.mm("<gray>/dungeon <enter|leave|generate>"));
        }
        return true;
    }

    private void enterDungeon(Player player) {
        Location spawn = plugin.dungeonManager().getEntrance();
        if (spawn == null) {
            player.sendMessage(Text.mm("<red>Dungeon nicht konfiguriert."));
            return;
        }
        plugin.dungeonManager().enterDungeon(player);
        player.sendMessage(Text.mm("<green>Dungeon betreten."));
    }

    private void leaveDungeon(Player player) {
        plugin.dungeonManager().leaveDungeon(player);
        player.sendMessage(Text.mm("<yellow>Dungeon verlassen."));
    }

    private void generateDungeon(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/dungeon generate <theme>"));
            return;
        }
        String theme = args[1];
        var party = plugin.partyManager().getParty(player.getUniqueId());
        java.util.List<Player> members = new java.util.ArrayList<>();
        if (party.isPresent()) {
            for (java.util.UUID memberId : party.get().members()) {
                Player member = player.getServer().getPlayer(memberId);
                if (member != null) {
                    members.add(member);
                }
            }
            Player leader = player.getServer().getPlayer(party.get().leader());
            if (leader != null && !members.contains(leader)) {
                members.add(leader);
            }
        } else {
            members.add(player);
        }
        plugin.dungeonManager().generateDungeon(player, theme, members);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/GuildCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/GuildCommand.java`  
- Size: 15171 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Guild;
import com.example.rpg.model.GuildMemberRole;
import com.example.rpg.model.GuildQuest;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public GuildCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (label.equalsIgnoreCase("g")) {
            if (args.length == 0) {
                player.sendMessage(Text.mm("<gray>/g <message>"));
                return true;
            }
            String message = join(args, 0);
            guildChat(player, new String[] {"chat", message});
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.mm("<gray>/guild <create|invite|accept|leave|disband|info|chat|bank|quest>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> createGuild(player, args);
            case "invite" -> invitePlayer(player, args);
            case "accept" -> acceptInvite(player);
            case "leave" -> leaveGuild(player);
            case "disband" -> disbandGuild(player);
            case "info" -> guildInfo(player);
            case "chat" -> guildChat(player, args);
            case "bank" -> bankCommand(player, args);
            case "quest" -> questCommand(player, args);
            default -> player.sendMessage(Text.mm("<gray>/guild <create|invite|accept|leave|disband|info|chat|bank|quest>"));
        }
        return true;
    }

    private void createGuild(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild create <id> <name>"));
            return;
        }
        if (plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Gilde."));
            return;
        }
        String id = args[1].toLowerCase();
        String name = join(args, 2);
        if (plugin.guildManager().guildById(id).isPresent()) {
            player.sendMessage(Text.mm("<red>Gilden-ID existiert bereits."));
            return;
        }
        plugin.guildManager().createGuild(id, name, player);
        player.sendMessage(Text.mm("<green>Gilde erstellt: " + name));
    }

    private void invitePlayer(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um einzuladen."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild invite <player>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        if (plugin.guildManager().isMember(target.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Spieler ist bereits in einer Gilde."));
            return;
        }
        plugin.guildManager().invite(target.getUniqueId(), guild.id());
        player.sendMessage(Text.mm("<green>Einladung gesendet."));
        target.sendMessage(Text.mm("<yellow>Gildeneinladung von " + guild.name() + ". /guild accept"));
    }

    private void acceptInvite(Player player) {
        if (plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Gilde."));
            return;
        }
        Optional<Guild> guild = plugin.guildManager().acceptInvite(player.getUniqueId());
        if (guild.isEmpty()) {
            player.sendMessage(Text.mm("<red>Keine Einladung gefunden."));
            return;
        }
        player.sendMessage(Text.mm("<green>Du bist der Gilde beigetreten."));
    }

    private void leaveGuild(Player player) {
        if (!plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        plugin.guildManager().leaveGuild(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Gilde verlassen."));
    }

    private void disbandGuild(Player player) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (!guild.leader().equals(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Nur der Gildenleiter kann auflösen."));
            return;
        }
        plugin.guildManager().disbandGuild(guild);
        player.sendMessage(Text.mm("<yellow>Gilde aufgelöst."));
    }

    private void guildInfo(Player player) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        String members = guild.members().keySet().stream()
            .map(uuid -> {
                Player online = player.getServer().getPlayer(uuid);
                return online != null ? online.getName() : uuid.toString().substring(0, 8);
            })
            .collect(Collectors.joining(", "));
        player.sendMessage(Text.mm("<gold>Gilde: <white>" + guild.name()));
        player.sendMessage(Text.mm("<gray>Mitglieder: <white>" + members));
        player.sendMessage(Text.mm("<gray>Gildenbank: <gold>" + guild.bankGold() + "</gold> Gold"));
    }

    private void guildChat(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild chat <message>"));
            return;
        }
        Guild guild = guildOpt.get();
        String message = join(args, 1);
        for (UUID member : guild.members().keySet()) {
            Player target = player.getServer().getPlayer(member);
            if (target != null) {
                target.sendMessage(Text.mm("<aqua>[Gilde] <white>" + player.getName() + ": " + message));
            }
        }
    }

    private void bankCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild bank <balance|deposit|withdraw>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "balance" -> player.sendMessage(Text.mm("<gold>Gildenbank: " + guild.bankGold() + " Gold"));
            case "deposit" -> depositGuild(player, guild, args);
            case "withdraw" -> withdrawGuild(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild bank <balance|deposit|withdraw>"));
        }
    }

    private void depositGuild(Player player, Guild guild, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild bank deposit <amount>"));
            return;
        }
        Integer amount = parseAmount(player, args[2]);
        if (amount == null) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (profile.gold() < amount) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        profile.setGold(profile.gold() - amount);
        plugin.guildManager().deposit(guild, amount);
        player.sendMessage(Text.mm("<green>" + amount + " Gold eingezahlt."));
    }

    private void withdrawGuild(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte zum Abheben."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild bank withdraw <amount>"));
            return;
        }
        Integer amount = parseAmount(player, args[2]);
        if (amount == null) {
            return;
        }
        if (!plugin.guildManager().withdraw(guild, amount)) {
            player.sendMessage(Text.mm("<red>Gildenbank hat nicht genug Gold."));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.setGold(profile.gold() + amount);
        player.sendMessage(Text.mm("<green>" + amount + " Gold abgehoben."));
    }

    private void questCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild quest <list|create|progress|complete>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list" -> listQuests(player, guild);
            case "create" -> createQuest(player, guild, args);
            case "progress" -> progressQuest(player, guild, args);
            case "complete" -> completeQuest(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild quest <list|create|progress|complete>"));
        }
    }

    private void listQuests(Player player, Guild guild) {
        if (guild.quests().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Gilden-Quests verfügbar."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Gilden-Quests:</gold>"));
        for (GuildQuest quest : guild.quests().values()) {
            String status = quest.completed() ? "<green>abgeschlossen" : "<yellow>" + quest.progress() + "/" + quest.goal();
            player.sendMessage(Text.mm("<gray>" + quest.id() + " - <white>" + quest.name() + " <gray>(" + status + "<gray>)"));
        }
    }

    private void createQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Quests zu erstellen."));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/guild quest create <id> <goal> <name>"));
            return;
        }
        String id = args[2].toLowerCase();
        Integer goal = parseAmount(player, args[3]);
        if (goal == null) {
            return;
        }
        if (guild.quests().containsKey(id)) {
            player.sendMessage(Text.mm("<red>Quest-ID existiert bereits."));
            return;
        }
        String name = args.length > 4 ? join(args, 4) : id;
        GuildQuest quest = new GuildQuest(id);
        quest.setName(name);
        quest.setDescription("Gildenquest");
        quest.setGoal(goal);
        quest.setProgress(0);
        quest.setCompleted(false);
        guild.quests().put(id, quest);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Gilden-Quest erstellt."));
    }

    private void progressQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Fortschritt zu setzen."));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/guild quest progress <id> <amount>"));
            return;
        }
        GuildQuest quest = guild.quests().get(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Integer amount = parseAmount(player, args[3]);
        if (amount == null) {
            return;
        }
        quest.setProgress(quest.progress() + amount);
        if (quest.progress() >= quest.goal()) {
            quest.setCompleted(true);
        }
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Quest-Fortschritt aktualisiert."));
    }

    private void completeQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Quests abzuschließen."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild quest complete <id>"));
            return;
        }
        GuildQuest quest = guild.quests().get(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        quest.setCompleted(true);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Quest abgeschlossen."));
    }

    private boolean isOfficerOrLeader(Guild guild, UUID member) {
        GuildMemberRole role = guild.members().get(member);
        return role == GuildMemberRole.LEADER || role == GuildMemberRole.OFFICER;
    }

    private Integer parseAmount(Player player, String input) {
        int amount;
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return null;
        }
        if (amount <= 0) {
            player.sendMessage(Text.mm("<red>Betrag muss > 0 sein."));
            return null;
        }
        return amount;
    }

    private String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) {
                builder.append(" ");
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/LootChatCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/LootChatCommand.java`  
- Size: 902 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LootChatCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public LootChatCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean current = plugin.getConfig().getBoolean("lootchat.enabled", true);
        boolean next = args.length == 1 ? Boolean.parseBoolean(args[0]) : !current;
        plugin.getConfig().set("lootchat.enabled", next);
        plugin.saveConfig();
        sender.sendMessage(Text.mm(next ? "<green>Lootchat aktiviert." : "<red>Lootchat deaktiviert."));
        return true;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/PartyCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/PartyCommand.java`  
- Size: 4553 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Party;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PartyCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public PartyCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.mm("<gray>/party <create|invite|join|leave|chat>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> createParty(player);
            case "invite" -> invitePlayer(player, args);
            case "join" -> joinParty(player, args);
            case "leave" -> leaveParty(player);
            case "chat" -> partyChat(player, args);
            default -> player.sendMessage(Text.mm("<gray>/party <create|invite|join|leave|chat>"));
        }
        return true;
    }

    private void createParty(Player player) {
        if (plugin.partyManager().getParty(player.getUniqueId()).isPresent()) {
            player.sendMessage(Text.mm("<yellow>Du bist bereits in einer Party."));
            return;
        }
        plugin.partyManager().createParty(player.getUniqueId());
        player.sendMessage(Text.mm("<green>Party erstellt."));
    }

    private void invitePlayer(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party invite <player>"));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        plugin.partyManager().getParty(player.getUniqueId()).ifPresentOrElse(party -> {
            target.sendMessage(Text.mm("<yellow>Party Einladung von " + player.getName() + ". Benutze /party join " + player.getName()));
        }, () -> player.sendMessage(Text.mm("<red>Du hast keine Party.")));
    }

    private void joinParty(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party join <leader>"));
            return;
        }
        Player leader = Bukkit.getPlayer(args[1]);
        if (leader == null) {
            player.sendMessage(Text.mm("<red>Leader nicht online."));
            return;
        }
        plugin.partyManager().getParty(leader.getUniqueId()).ifPresentOrElse(party -> {
            if (plugin.partyManager().getParty(player.getUniqueId()).isPresent()) {
                player.sendMessage(Text.mm("<yellow>Du bist bereits in einer Party."));
                return;
            }
            plugin.partyManager().addMember(party, player.getUniqueId());
            leader.sendMessage(Text.mm("<green>" + player.getName() + " ist beigetreten."));
            player.sendMessage(Text.mm("<green>Du bist der Party beigetreten."));
        }, () -> player.sendMessage(Text.mm("<red>Party nicht gefunden.")));
    }

    private void leaveParty(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.partyManager().getParty(uuid).isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Du bist in keiner Party."));
            return;
        }
        plugin.partyManager().removeMember(uuid);
        player.sendMessage(Text.mm("<green>Party verlassen."));
    }

    private void partyChat(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/party chat <message>"));
            return;
        }
        plugin.partyManager().getParty(player.getUniqueId()).ifPresentOrElse(party -> {
            String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            for (UUID member : party.members()) {
                Player target = Bukkit.getPlayer(member);
                if (target != null) {
                    target.sendMessage(Text.mm("<aqua>[Party] " + player.getName() + ": <white>" + message));
                }
            }
        }, () -> player.sendMessage(Text.mm("<yellow>Du bist in keiner Party.")));
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/PvpCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/PvpCommand.java`  
- Size: 1740 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvpCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public PvpCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/pvp <join|top>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "join" -> plugin.arenaManager().joinQueue(player);
            case "top" -> showTop(player);
            default -> player.sendMessage(Text.mm("<gray>/pvp <join|top>"));
        }
        return true;
    }

    private void showTop(Player player) {
        List<PlayerProfile> profiles = plugin.arenaManager().topPlayers(10);
        player.sendMessage(Text.mm("<gold>PvP Rangliste:"));
        int index = 1;
        for (PlayerProfile profile : profiles) {
            String name = plugin.getServer().getOfflinePlayer(profile.uuid()).getName();
            if (name == null) {
                name = profile.uuid().toString().substring(0, 8);
            }
            player.sendMessage(Text.mm("<gray>" + index++ + ". <white>" + name
                + " <gold>(" + profile.elo() + ")"));
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/RPGAdminCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/RPGAdminCommand.java`  
- Size: 35254 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
import com.example.rpg.model.SkillType;
import com.example.rpg.model.Zone;
import com.example.rpg.skill.SkillEffectConfig;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class RPGAdminCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGAdminCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (!plugin.permissionService().has(player, "rpg.admin")) {
            player.sendMessage(Text.mm("<red>Keine Rechte."));
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openAdminMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "wand" -> giveWand(player);
            case "zone" -> handleZone(player, args);
            case "npc" -> handleNpc(player, args);
            case "quest" -> handleQuest(player, args);
            case "loot" -> handleLoot(player, args);
            case "skill" -> handleSkill(player, args);
            case "mob" -> handleMob(player, args);
            case "spawner" -> handleSpawner(player, args);
            case "build" -> handleBuild(player, args);
            case "perms" -> handlePerms(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin <wand|zone|npc|quest|loot|skill|mob|spawner|build|perms>"));
        }
        return true;
    }

    private void handlePerms(Player player, String[] args) {
        if (!plugin.permissionService().has(player, "rpg.admin.perms")) {
            player.sendMessage(Text.mm("<red>Keine Rechte."));
            return;
        }
        if (args.length < 2 || "gui".equalsIgnoreCase(args[1])) {
            plugin.guiManager().openPermissionsMain(player);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "role" -> handlePermRole(player, args);
            case "user" -> handlePermUser(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin perms <gui|role|user>"));
        }
    }

    private void handlePermRole(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin perms role <create|delete|parent|node|rename>"));
            return;
        }
        switch (args[2].toLowerCase()) {
            case "create" -> {
                if (args.length < 5) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms role create <key> <displayName>"));
                    return;
                }
                String key = args[3];
                String displayName = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
                plugin.permissionService().createRole(player, key, displayName);
                player.sendMessage(Text.mm("<green>Rolle erstellt."));
            }
            case "delete" -> {
                if (args.length < 4) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms role delete <key>"));
                    return;
                }
                plugin.permissionService().deleteRole(player, args[3]);
                player.sendMessage(Text.mm("<green>Rolle gelöscht."));
            }
            case "rename" -> {
                if (args.length < 5) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms role rename <key> <displayName>"));
                    return;
                }
                String key = args[3];
                String displayName = String.join(" ", java.util.Arrays.copyOfRange(args, 4, args.length));
                plugin.permissionService().renameRole(player, key, displayName);
                player.sendMessage(Text.mm("<green>Rolle umbenannt."));
            }
            case "parent" -> handlePermParents(player, args);
            case "node" -> handlePermNodes(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin perms role <create|delete|parent|node|rename>"));
        }
    }

    private void handlePermParents(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(Text.mm("<gray>/rpgadmin perms role parent <add|remove> <role> <parent>"));
            return;
        }
        String action = args[3].toLowerCase();
        String role = args[4];
        String parent = args[5];
        if ("add".equals(action)) {
            boolean added = plugin.permissionService().addParent(player, role, parent);
            player.sendMessage(Text.mm(added ? "<green>Parent hinzugefügt." : "<red>Parent nicht hinzugefügt."));
            return;
        }
        if ("remove".equals(action)) {
            plugin.permissionService().removeParent(player, role, parent);
            player.sendMessage(Text.mm("<green>Parent entfernt."));
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpgadmin perms role parent <add|remove> <role> <parent>"));
    }

    private void handlePermNodes(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(Text.mm("<gray>/rpgadmin perms role node <role> <node> <allow|deny|inherit>"));
            return;
        }
        String role = args[3];
        String node = args[4];
        String decisionRaw = args.length > 5 ? args[5] : "inherit";
        com.example.rpg.permissions.PermissionDecision decision = switch (decisionRaw.toLowerCase()) {
            case "allow" -> com.example.rpg.permissions.PermissionDecision.ALLOW;
            case "deny" -> com.example.rpg.permissions.PermissionDecision.DENY;
            default -> com.example.rpg.permissions.PermissionDecision.INHERIT;
        };
        plugin.permissionService().setRoleNode(player, role, node, decision);
        player.sendMessage(Text.mm("<green>Node gesetzt."));
    }

    private void handlePermUser(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin perms user <setprimary|add|remove|info> ..."));
            return;
        }
        String action = args[2].toLowerCase();
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin perms user <setprimary|add|remove|info> <player> [role/node]"));
            return;
        }
        org.bukkit.OfflinePlayer target = plugin.getServer().getOfflinePlayer(args[3]);
        UUID targetId = target.getUniqueId();
        switch (action) {
            case "setprimary" -> {
                if (args.length < 5) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms user setprimary <player> <role>"));
                    return;
                }
                plugin.permissionService().assignPrimary(player, targetId, args[4]);
                player.sendMessage(Text.mm("<green>Primary Rolle gesetzt."));
            }
            case "add" -> {
                if (args.length < 5) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms user add <player> <role>"));
                    return;
                }
                plugin.permissionService().addRole(player, targetId, args[4]);
                player.sendMessage(Text.mm("<green>Rolle hinzugefügt."));
            }
            case "remove" -> {
                if (args.length < 5) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms user remove <player> <role>"));
                    return;
                }
                plugin.permissionService().removeRole(player, targetId, args[4]);
                player.sendMessage(Text.mm("<green>Rolle entfernt."));
            }
            case "info" -> {
                if (args.length < 5) {
                    player.sendMessage(Text.mm("<gray>/rpgadmin perms user info <player> <node>"));
                    return;
                }
                var explain = plugin.permissionService().explain(targetId, args[4]);
                player.sendMessage(Text.mm("<yellow>Ergebnis: " + (explain.allowed() ? "ALLOW" : "DENY")));
                if (explain.winningRole() != null) {
                    player.sendMessage(Text.mm("<gray>Role: " + explain.winningRole() + " Node: " + explain.winningNode()));
                }
            }
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin perms user <setprimary|add|remove|info> ..."));
        }
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemBuilder(Material.STICK)
            .name(Text.mm("<yellow>Editor Wand"))
            .loreLine(Text.mm("<gray>Links: Pos1, Rechts: Pos2"))
            .build();
        var meta = wand.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.wandKey(), PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(Text.mm("<green>Editor Wand erhalten."));
    }

    private void handleZone(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone <create|setlevel|setmod>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createZone(player, args);
            case "setlevel" -> setZoneLevel(player, args);
            case "setmod" -> setZoneModifiers(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin zone <create|setlevel|setmod>"));
        }
    }

    private void createZone(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone create <id>"));
            return;
        }
        Location pos1 = readPosition(player, "pos1");
        Location pos2 = readPosition(player, "pos2");
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
            return;
        }
        String id = args[2];
        Zone zone = new Zone(id);
        zone.setName(id);
        zone.setWorld(pos1.getWorld().getName());
        zone.setBounds(pos1, pos2);
        plugin.zoneManager().zones().put(id, zone);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone erstellt: " + id);
        player.sendMessage(Text.mm("<green>Zone erstellt: " + id));
    }

    private void setZoneLevel(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone setlevel <id> <min> <max>"));
            return;
        }
        Zone zone = plugin.zoneManager().getZone(args[2]);
        if (zone == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        Integer min = parseInt(args[3]);
        Integer max = parseInt(args[4]);
        if (min == null || max == null || min < 1 || max < min) {
            player.sendMessage(Text.mm("<red>Ungültiger Levelbereich. Beispiel: <white>/rpgadmin zone setlevel <id> 1 30</white>"));
            return;
        }
        zone.setMinLevel(min);
        zone.setMaxLevel(max);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone Level gesetzt: " + zone.id());
        player.sendMessage(Text.mm("<green>Zone Level aktualisiert."));
    }

    private void setZoneModifiers(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone setmod <id> <slow> <damage>"));
            return;
        }
        Zone zone = plugin.zoneManager().getZone(args[2]);
        if (zone == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        Double slow = parseDouble(args[3]);
        Double dmg = parseDouble(args[4]);
        if (slow == null || dmg == null || slow <= 0.0 || dmg <= 0.0) {
            player.sendMessage(Text.mm("<red>Ungültige Werte. Beispiel: <white>/rpgadmin zone setmod <id> 0.8 1.2</white>"));
            return;
        }
        zone.setSlowMultiplier(slow);
        zone.setDamageMultiplier(dmg);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone Modifikatoren gesetzt: " + zone.id());
        player.sendMessage(Text.mm("<green>Zone Modifikatoren aktualisiert."));
    }

    private void handleNpc(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc <create|dialog|linkquest|linkshop>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createNpc(player, args);
            case "dialog" -> setNpcDialog(player, args);
            case "linkquest" -> linkNpcQuest(player, args);
            case "linkshop" -> linkNpcShop(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin npc <create|dialog|linkquest|linkshop>"));
        }
    }

    private void handleBuild(Player player, String[] args) {
        if (args.length < 2) {
            plugin.guiManager().openBuildingCategories(player);
            return;
        }
        if ("gui".equalsIgnoreCase(args[1])) {
            plugin.guiManager().openBuildingCategories(player);
            return;
        }
        if ("undo".equalsIgnoreCase(args[1])) {
            plugin.buildingManager().undoLast(player);
            return;
        }
        if ("move".equalsIgnoreCase(args[1])) {
            plugin.guiManager().openSchematicMoveGui(player);
            return;
        }
        plugin.buildingManager().beginPlacement(player, args[1], com.example.rpg.schematic.Transform.Rotation.NONE);
    }

    private void createNpc(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc create <id> <role>"));
            return;
        }
        String id = args[2];
        Optional<NpcRole> roleOpt = parseEnum(NpcRole.class, args[3]);
        if (roleOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannte Rolle. Erlaubt: <white>" + java.util.Arrays.toString(NpcRole.values())));
            return;
        }
        NpcRole role = roleOpt.get();
        Npc npc = new Npc(id);
        npc.setName(id);
        npc.setRole(role);
        npc.setLocation(player.getLocation());
        npc.setDialog(List.of("Hallo!", "Ich habe eine Aufgabe für dich."));
        plugin.npcManager().npcs().put(id, npc);
        plugin.npcManager().spawnNpc(npc);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC erstellt: " + id);
        player.sendMessage(Text.mm("<green>NPC erstellt: " + id));
    }

    private void setNpcDialog(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc dialog <id>"));
            return;
        }
        String id = args[2];
        Npc npc = plugin.npcManager().getNpc(id);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        plugin.promptManager().prompt(player, Text.mm("<yellow>Dialogzeile eingeben:"), input -> {
            npc.setDialog(List.of(input));
            plugin.npcManager().saveNpc(npc);
            plugin.auditLog().log(player, "NPC Dialog gesetzt: " + id);
            player.sendMessage(Text.mm("<green>Dialog gespeichert."));
        });
    }

    private void linkNpcQuest(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc linkquest <npcId> <questId>"));
            return;
        }
        String npcId = args[2];
        String questId = args[3];
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        Quest quest = plugin.questManager().getQuest(questId);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        npc.setQuestLink(questId);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC Quest verlinkt: " + npcId + " -> " + questId);
        player.sendMessage(Text.mm("<green>NPC verlinkt mit Quest: " + quest.name()));
    }

    private void linkNpcShop(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc linkshop <npcId> <shopId>"));
            return;
        }
        String npcId = args[2];
        String shopId = args[3];
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        if (plugin.shopManager().getShop(shopId) == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        npc.setShopId(shopId);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC Shop verlinkt: " + npcId + " -> " + shopId);
        player.sendMessage(Text.mm("<green>NPC verlinkt mit Shop: " + shopId));
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest <create|addstep>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createQuest(player, args);
            case "addstep" -> addQuestStep(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin quest <create|addstep>"));
        }
    }

    private void createQuest(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest create <id> <name>"));
            return;
        }
        String id = args[2];
        String name = args[3];
        Quest quest = new Quest(id);
        quest.setName(name);
        quest.setDescription("Neue Quest");
        quest.setRepeatable(false);
        quest.setMinLevel(1);
        quest.setSteps(new java.util.ArrayList<>());
        plugin.questManager().quests().put(id, quest);
        plugin.questManager().saveQuest(quest);
        plugin.auditLog().log(player, "Quest erstellt: " + id);
        player.sendMessage(Text.mm("<green>Quest erstellt: " + id));
    }

    private void addQuestStep(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest addstep <id> <type> <target> <amount>"));
            return;
        }
        Quest quest = plugin.questManager().getQuest(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Optional<QuestStepType> typeOpt = parseEnum(QuestStepType.class, args[3]);
        if (typeOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannter Step-Typ. Erlaubt: <white>" + java.util.Arrays.toString(QuestStepType.values())));
            return;
        }
        QuestStepType type = typeOpt.get();
        String target = args[4];
        Integer amount = parseInt(args[5]);
        if (amount == null || amount < 1) {
            player.sendMessage(Text.mm("<red>Amount muss >= 1 sein.</red>"));
            return;
        }
        quest.steps().add(new QuestStep(type, target, amount));
        plugin.questManager().saveQuest(quest);
        plugin.auditLog().log(player, "Quest Step hinzugefügt: " + quest.id());
        player.sendMessage(Text.mm("<green>Quest Step hinzugefügt."));
    }

    private void handleLoot(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot <create|addentry>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createLoot(player, args);
            case "addentry" -> addLootEntry(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin loot <create|addentry>"));
        }
    }

    private void createLoot(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot create <id> <appliesTo>"));
            return;
        }
        String id = args[2];
        String appliesTo = args[3];
        LootTable table = new LootTable(id);
        table.setAppliesTo(appliesTo);
        plugin.lootManager().tables().put(id, table);
        plugin.lootManager().saveTable(table);
        plugin.auditLog().log(player, "Loot Table erstellt: " + id);
        player.sendMessage(Text.mm("<green>Loot Table erstellt."));
    }

    private void addLootEntry(Player player, String[] args) {
        if (args.length < 8) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>"));
            return;
        }
        LootTable table = plugin.lootManager().getTable(args[2]);
        if (table == null) {
            player.sendMessage(Text.mm("<red>Loot Table nicht gefunden."));
            return;
        }
        String material = args[3];
        Material mat = Material.matchMaterial(material.toUpperCase(Locale.ROOT));
        if (mat == null) {
            player.sendMessage(Text.mm("<red>Unbekanntes Material: <white>" + material + "</white>"));
            return;
        }
        Double chance = parseDouble(args[4]);
        Integer min = parseInt(args[5]);
        Integer max = parseInt(args[6]);
        Optional<Rarity> rarityOpt = parseEnum(Rarity.class, args[7]);
        if (chance == null || min == null || max == null || rarityOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Ungültige Parameter. Beispiel: <white>/rpgadmin loot addentry <id> IRON_NUGGET 0.5 1 3 COMMON</white>"));
            return;
        }
        if (chance < 0.0 || chance > 1.0 || min < 1 || max < min) {
            player.sendMessage(Text.mm("<red>Chance 0..1 und min/max gültig setzen.</red>"));
            return;
        }
        Rarity rarity = rarityOpt.get();
        table.entries().add(new LootEntry(mat.name(), chance, min, max, rarity));
        plugin.lootManager().saveTable(table);
        plugin.auditLog().log(player, "Loot Entry hinzugefügt: " + table.id());
        player.sendMessage(Text.mm("<green>Loot Entry hinzugefügt."));
    }

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill <create|set|addeffect>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createSkill(player, args);
            case "set" -> setSkillValue(player, args);
            case "addeffect" -> addSkillEffect(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin skill <create|set|addeffect>"));
        }
    }

    private void handleMob(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin mob spawn <mobId>"));
            return;
        }
        if (!args[1].equalsIgnoreCase("spawn")) {
            player.sendMessage(Text.mm("<gray>/rpgadmin mob spawn <mobId>"));
            return;
        }
        String mobId = args[2];
        var mobDef = plugin.mobManager().getMob(mobId);
        if (mobDef == null) {
            player.sendMessage(Text.mm("<red>Mob nicht gefunden."));
            return;
        }
        var world = player.getWorld();
        org.bukkit.entity.EntityType type;
        try {
            type = org.bukkit.entity.EntityType.valueOf(mobDef.type().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            player.sendMessage(Text.mm("<red>Ungültiger Mob-Typ.</red>"));
            return;
        }
        var entity = world.spawnEntity(player.getLocation(), type);
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, mobDef);
            plugin.mobManager().saveMob(mobDef);
            plugin.auditLog().log(player, "Mob gespawnt: " + mobId);
            player.sendMessage(Text.mm("<green>Mob gespawnt: " + mobId));
        } else {
            entity.remove();
            player.sendMessage(Text.mm("<red>Mob-Typ ist kein LivingEntity."));
        }
    }

    private void handleSpawner(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner <create|addmob|setlimit>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createSpawner(player, args);
            case "addmob" -> addSpawnerMob(player, args);
            case "setlimit" -> setSpawnerLimit(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin spawner <create|addmob|setlimit>"));
        }
    }

    private void createSpawner(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner create <id> <zoneId>"));
            return;
        }
        String id = args[2];
        String zoneId = args[3];
        if (plugin.zoneManager().getZone(zoneId) == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        var spawner = new com.example.rpg.model.Spawner(id);
        spawner.setZoneId(zoneId);
        spawner.setMaxMobs(6);
        spawner.setSpawnInterval(200);
        plugin.spawnerManager().spawners().put(id, spawner);
        plugin.spawnerManager().saveSpawner(spawner);
        plugin.auditLog().log(player, "Spawner erstellt: " + id);
        player.sendMessage(Text.mm("<green>Spawner erstellt: " + id));
    }

    private void addSpawnerMob(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner addmob <id> <mobId> <chance>"));
            return;
        }
        var spawner = plugin.spawnerManager().getSpawner(args[2]);
        if (spawner == null) {
            player.sendMessage(Text.mm("<red>Spawner nicht gefunden."));
            return;
        }
        String mobId = args[3];
        if (plugin.mobManager().getMob(mobId) == null) {
            player.sendMessage(Text.mm("<red>Mob nicht gefunden."));
            return;
        }
        Double chance = parseDouble(args[4]);
        if (chance == null || chance <= 0) {
            player.sendMessage(Text.mm("<red>Chance ungültig.</red>"));
            return;
        }
        spawner.mobs().put(mobId, chance);
        plugin.spawnerManager().saveSpawner(spawner);
        plugin.auditLog().log(player, "Spawner Mob hinzugefügt: " + spawner.id());
        player.sendMessage(Text.mm("<green>Spawner Mob hinzugefügt."));
    }

    private void setSpawnerLimit(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin spawner setlimit <id> <amount>"));
            return;
        }
        var spawner = plugin.spawnerManager().getSpawner(args[2]);
        if (spawner == null) {
            player.sendMessage(Text.mm("<red>Spawner nicht gefunden."));
            return;
        }
        Integer limit = parseInt(args[3]);
        if (limit == null || limit < 0) {
            player.sendMessage(Text.mm("<red>Limit ungültig.</red>"));
            return;
        }
        spawner.setMaxMobs(limit);
        plugin.spawnerManager().saveSpawner(spawner);
        plugin.auditLog().log(player, "Spawner Limit gesetzt: " + spawner.id());
        player.sendMessage(Text.mm("<green>Spawner Limit aktualisiert."));
    }

    private void createSkill(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill create <id>"));
            return;
        }
        String id = args[2].toLowerCase(Locale.ROOT);
        Skill skill = new Skill(id);
        skill.setName(id);
        skill.setType(SkillType.ACTIVE);
        skill.setCategory(SkillCategory.ATTACK);
        skill.setCooldown(10);
        skill.setManaCost(10);
        skill.setEffects(new java.util.ArrayList<>());
        plugin.skillManager().skills().put(id, skill);
        plugin.skillManager().saveSkill(skill);
        plugin.auditLog().log(player, "Skill erstellt: " + id);
        player.sendMessage(Text.mm("<green>Skill erstellt: " + id));
    }

    private void setSkillValue(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill set <id> <cooldown|mana|category|type|name|requires> <value>"));
            return;
        }
        Skill skill = plugin.skillManager().getSkill(args[2]);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Skill nicht gefunden."));
            return;
        }
        String field = args[3].toLowerCase(Locale.ROOT);
        String value = args[4];
        switch (field) {
            case "cooldown" -> {
                Integer cd = parseInt(value);
                if (cd == null || cd < 0) {
                    player.sendMessage(Text.mm("<red>Cooldown ungültig.</red>"));
                    return;
                }
                skill.setCooldown(cd);
            }
            case "mana" -> {
                Integer mana = parseInt(value);
                if (mana == null || mana < 0) {
                    player.sendMessage(Text.mm("<red>Mana ungültig.</red>"));
                    return;
                }
                skill.setManaCost(mana);
            }
            case "category" -> {
                Optional<SkillCategory> category = parseEnum(SkillCategory.class, value);
                if (category.isEmpty()) {
                    player.sendMessage(Text.mm("<red>Unbekannte Kategorie.</red>"));
                    return;
                }
                skill.setCategory(category.get());
            }
            case "type" -> {
                Optional<SkillType> type = parseEnum(SkillType.class, value);
                if (type.isEmpty()) {
                    player.sendMessage(Text.mm("<red>Unbekannter Typ.</red>"));
                    return;
                }
                skill.setType(type.get());
            }
            case "name" -> skill.setName(value);
            case "requires" -> skill.setRequiredSkill(value.equalsIgnoreCase("none") ? null : value);
            default -> {
                player.sendMessage(Text.mm("<red>Unbekanntes Feld.</red>"));
                return;
            }
        }
        plugin.skillManager().saveSkill(skill);
        plugin.auditLog().log(player, "Skill gesetzt: " + skill.id() + " " + field + "=" + value);
        player.sendMessage(Text.mm("<green>Skill aktualisiert."));
    }

    private void addSkillEffect(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin skill addeffect <id> <effectType> <param:value>..."));
            return;
        }
        Skill skill = plugin.skillManager().getSkill(args[2]);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Skill nicht gefunden."));
            return;
        }
        Optional<SkillEffectType> typeOpt = parseEnum(SkillEffectType.class, args[3]);
        if (typeOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannter Effekt-Typ. Erlaubt: <white>"
                + java.util.Arrays.toString(SkillEffectType.values())));
            return;
        }
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        for (int i = 4; i < args.length; i++) {
            String token = args[i];
            if (!token.contains(":")) {
                continue;
            }
            String[] parts = token.split(":", 2);
            params.put(parts[0], parseParamValue(parts[1]));
        }
        skill.effects().add(new SkillEffectConfig(typeOpt.get(), params));
        plugin.skillManager().saveSkill(skill);
        plugin.auditLog().log(player, "Skill Effekt hinzugefügt: " + skill.id() + " " + typeOpt.get());
        player.sendMessage(Text.mm("<green>Effekt hinzugefügt."));
    }

    private Location readPosition(Player player, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        String value = player.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(",");
        if (parts.length < 4) {
            return null;
        }
        return new Location(player.getServer().getWorld(parts[0]),
            Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    // -----------------------
    // Parsing-Helper (crash-sicher)
    // -----------------------
    private static Integer parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> type, String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Optional.of(Enum.valueOf(type, key));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Object parseParamValue(String raw) {
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return raw;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/RPGCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/RPGCommand.java`  
- Size: 9875 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.ClassDefinition;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RPGCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openPlayerMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "skill" -> handleSkill(player, args);
            case "quest" -> handleQuest(player, args);
            case "respec" -> handleRespec(player);
            case "class" -> handleClass(player, args);
            case "bind" -> handleBind(player, args);
            case "money" -> handleMoney(player);
            case "pay" -> handlePay(player, args);
            case "profession" -> handleProfession(player, args);
            case "skilltree" -> plugin.skillTreeGui().open(player);
            default -> player.sendMessage(Text.mm("<gray>/rpg <skill|quest|respec|class|bind|money|pay|profession|skilltree>"));
        }
        return true;
    }

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg skill <id>"));
            return;
        }
        String skillId = args[1].toLowerCase();
        plugin.useSkill(player, skillId);
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg quest <accept|abandon|list>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> plugin.guiManager().openQuestList(player);
            case "abandon" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest abandon <id>"));
                    return;
                }
                String questId = args[2];
                profile.activeQuests().remove(questId);
                player.sendMessage(Text.mm("<yellow>Quest abgebrochen: " + questId));
            }
            case "complete" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest complete <id>"));
                    return;
                }
                String questId = args[2];
                var quest = plugin.questManager().getQuest(questId);
                var progress = profile.activeQuests().get(questId);
                if (quest == null || progress == null) {
                    player.sendMessage(Text.mm("<red>Quest nicht aktiv."));
                    return;
                }
                if (!plugin.completeQuestIfReady(player, quest, progress)) {
                    player.sendMessage(Text.mm("<yellow>Quest noch nicht abgeschlossen."));
                }
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg quest <list|abandon|complete>"));
        }
    }

    private void handleRespec(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.learnedSkills().clear();
        profile.setSkillPoints(profile.level() * 2);
        profile.stats().replaceAll((stat, value) -> 5);
        profile.applyAttributes(player);
        player.sendMessage(Text.mm("<green>Respec durchgeführt. Skillpunkte zurückgesetzt."));
    }

    private void handleClass(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            player.sendMessage(Text.mm("<yellow>Klassen: " + plugin.classManager().classes().keySet()));
            return;
        }
        if (args[1].equalsIgnoreCase("choose")) {
            if (args.length < 3) {
                player.sendMessage(Text.mm("<gray>/rpg class choose <id>"));
                return;
            }
            String id = args[2];
            ClassDefinition definition = plugin.classManager().getClass(id);
            if (definition == null) {
                player.sendMessage(Text.mm("<red>Unbekannte Klasse."));
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            profile.setClassId(id);
            for (String skill : definition.startSkills()) {
                profile.learnedSkills().put(skill, 1);
            }
            player.sendMessage(Text.mm("<green>Klasse gewählt: " + definition.name()));
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
    }

    private void handleBind(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg bind <slot 1-9> <skillId>"));
            return;
        }
        Integer slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Slot muss 1-9 sein."));
            return;
        }
        if (slot < 1 || slot > 9) {
            player.sendMessage(Text.mm("<red>Slot muss 1-9 sein."));
            return;
        }
        String skillId = args[2].toLowerCase();
        Skill skill = plugin.skillManager().getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Unbekannter Skill."));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            player.sendMessage(Text.mm("<red>Skill nicht gelernt."));
            return;
        }
        plugin.skillHotbarManager().bindSkill(profile, slot, skillId);
        player.sendMessage(Text.mm("<green>Skill gebunden: Slot " + slot + " -> " + skill.name()));
    }

    private void handleMoney(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        player.sendMessage(Text.mm("<gold>Gold: <white>" + profile.gold()));
    }

    private void handlePay(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg pay <player> <amount>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        Integer amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        if (amount <= 0) {
            player.sendMessage(Text.mm("<red>Betrag muss > 0 sein."));
            return;
        }
        PlayerProfile senderProfile = plugin.playerDataManager().getProfile(player);
        PlayerProfile targetProfile = plugin.playerDataManager().getProfile(target);
        if (senderProfile.gold() < amount) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        senderProfile.setGold(senderProfile.gold() - amount);
        targetProfile.setGold(targetProfile.gold() + amount);
        player.sendMessage(Text.mm("<green>Du hast <gold>" + amount + "</gold> Gold an " + target.getName() + " gesendet."));
        target.sendMessage(Text.mm("<green>Du hast <gold>" + amount + "</gold> Gold von " + player.getName() + " erhalten."));
    }

    private void handleProfession(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg profession <list|set>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> {
                if (profile.professions().isEmpty()) {
                    player.sendMessage(Text.mm("<yellow>Keine Berufe freigeschaltet."));
                    return;
                }
                String summary = profile.professions().entrySet().stream()
                    .filter(entry -> entry.getKey().endsWith("_level"))
                    .map(entry -> entry.getKey().replace("_level", "") + ": " + entry.getValue())
                    .collect(java.util.stream.Collectors.joining(", "));
                player.sendMessage(Text.mm("<gold>Berufe: <white>" + summary));
            }
            case "set" -> {
                if (args.length < 4) {
                    player.sendMessage(Text.mm("<gray>/rpg profession set <name> <level>"));
                    return;
                }
                Integer level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.mm("<red>Level ungültig."));
                    return;
                }
                plugin.professionManager().setLevel(profile, args[2].toLowerCase(), level);
                player.sendMessage(Text.mm("<green>Beruf gesetzt."));
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg profession <list|set>"));
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/TradeCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/TradeCommand.java`  
- Size: 6032 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.TradeRequest;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TradeCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public TradeCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/trade <request|accept|offer|requestgold|ready|cancel>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "request" -> requestTrade(player, args);
            case "accept" -> acceptTrade(player);
            case "offer" -> offerGold(player, args);
            case "requestgold" -> requestGold(player, args);
            case "ready" -> readyTrade(player);
            case "cancel" -> cancelTrade(player);
            default -> player.sendMessage(Text.mm("<gray>/trade <request|accept|offer|requestgold|ready|cancel>"));
        }
        return true;
    }

    private void requestTrade(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade request <player>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        plugin.tradeManager().requestTrade(player.getUniqueId(), target.getUniqueId());
        player.sendMessage(Text.mm("<green>Handel angefragt."));
        target.sendMessage(Text.mm("<yellow>Handelsanfrage von " + player.getName() + ". /trade accept"));
    }

    private void acceptTrade(Player player) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Keine Anfrage."));
            return;
        }
        player.sendMessage(Text.mm("<green>Handel akzeptiert. Beide Seiten können Gold setzen."));
    }

    private void offerGold(Player player, String[] args) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade offer <gold>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        request.setGoldOffer(amount);
        player.sendMessage(Text.mm("<green>Du bietest " + amount + " Gold."));
    }

    private void requestGold(Player player, String[] args) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/trade requestgold <gold>"));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        request.setGoldRequest(amount);
        player.sendMessage(Text.mm("<green>Du verlangst " + amount + " Gold."));
    }

    private void readyTrade(Player player) {
        TradeRequest request = plugin.tradeManager().getRequest(player.getUniqueId());
        if (request == null) {
            player.sendMessage(Text.mm("<red>Kein Handel aktiv."));
            return;
        }
        if (request.requester().equals(player.getUniqueId())) {
            request.setRequesterReady(true);
        } else {
            request.setTargetReady(true);
        }
        if (request.requesterReady() && request.targetReady()) {
            completeTrade(request);
        } else {
            player.sendMessage(Text.mm("<green>Bereit gesetzt. Warte auf den Handelspartner."));
        }
    }

    private void completeTrade(TradeRequest request) {
        Player requester = plugin.getServer().getPlayer(request.requester());
        Player target = plugin.getServer().getPlayer(request.target());
        if (requester == null || target == null) {
            return;
        }
        var requesterProfile = plugin.playerDataManager().getProfile(requester);
        var targetProfile = plugin.playerDataManager().getProfile(target);
        if (requesterProfile.gold() < request.goldOffer() || targetProfile.gold() < request.goldRequest()) {
            requester.sendMessage(Text.mm("<red>Handel fehlgeschlagen: nicht genug Gold."));
            target.sendMessage(Text.mm("<red>Handel fehlgeschlagen: nicht genug Gold."));
            plugin.tradeManager().clear(request.requester());
            return;
        }
        requesterProfile.setGold(requesterProfile.gold() - request.goldOffer() + request.goldRequest());
        targetProfile.setGold(targetProfile.gold() - request.goldRequest() + request.goldOffer());
        requester.sendMessage(Text.mm("<green>Handel abgeschlossen."));
        target.sendMessage(Text.mm("<green>Handel abgeschlossen."));
        plugin.tradeManager().clear(request.requester());
    }

    private void cancelTrade(Player player) {
        plugin.tradeManager().clear(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Handel abgebrochen."));
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/VoiceChatCommand.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/command/VoiceChatCommand.java`  
- Size: 1216 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoiceChatCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public VoiceChatCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/voicechat <party|guild|leave>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "party" -> plugin.voiceChatManager().joinParty(player);
            case "guild" -> plugin.voiceChatManager().joinGuild(player);
            case "leave" -> plugin.voiceChatManager().leave(player);
            default -> player.sendMessage(Text.mm("<gray>/voicechat <party|guild|leave>"));
        }
        return true;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/DatabaseService.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/DatabaseService.java`  
- Size: 6789 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class DatabaseService {
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;
    private final ExecutorService executor;
    private final String jdbcUrl;
    private final String databaseName;
    private final String username;
    private final String password;

    public DatabaseService(JavaPlugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        String host = config.getString("database.host", "localhost");
        int port = config.getInt("database.port", 5432);
        String database = config.getString("database.name", "rpg");
        this.databaseName = database;
        this.username = config.getString("database.user", "rpg");
        this.password = config.getString("database.password", "minecraft");
        int poolSize = config.getInt("database.poolSize", 10);

        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("PostgreSQL JDBC driver not found.");
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setDriverClassName("org.postgresql.Driver");
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setPoolName("MineLauncherRPG");
        hikariConfig.setAutoCommit(true);
        this.dataSource = createDataSource(hikariConfig);
        this.executor = Executors.newFixedThreadPool(Math.max(2, poolSize));
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ExecutorService executor() {
        return executor;
    }

    public void initTables() {
        if (dataSource == null) {
            plugin.getLogger().severe("Database not available. Skipping table initialization.");
            return;
        }
        String playersTable = """
            CREATE TABLE IF NOT EXISTS rpg_players (
                uuid UUID PRIMARY KEY,
                level INT,
                xp INT,
                skill_points INT,
                mana INT,
                max_mana INT,
                class_id TEXT,
                gold INT,
                guild_id TEXT,
                elo INT,
                professions JSONB,
                stats JSONB,
                learned_skills JSONB,
                active_quests JSONB,
                completed_quests JSONB,
                faction_rep JSONB,
                skill_cooldowns JSONB,
                skill_bindings JSONB
            )
            """;
        String skillsTable = """
            CREATE TABLE IF NOT EXISTS rpg_skills (
                player_uuid UUID PRIMARY KEY,
                data JSONB
            )
            """;
        String questsTable = """
            CREATE TABLE IF NOT EXISTS rpg_quests (
                player_uuid UUID PRIMARY KEY,
                data JSONB
            )
            """;
        String rolesTable = """
            CREATE TABLE IF NOT EXISTS rpg_roles (
                role_key TEXT PRIMARY KEY,
                display_name TEXT NOT NULL,
                parents JSONB NOT NULL DEFAULT '[]',
                nodes JSONB NOT NULL DEFAULT '{}'
            )
            """;
        String playerRolesTable = """
            CREATE TABLE IF NOT EXISTS rpg_player_roles (
                player_uuid UUID PRIMARY KEY,
                primary_role TEXT,
                extra_roles JSONB NOT NULL DEFAULT '[]'
            )
            """;
        String auditTable = """
            CREATE TABLE IF NOT EXISTS rpg_audit_log (
                id BIGSERIAL PRIMARY KEY,
                ts TIMESTAMPTZ NOT NULL DEFAULT now(),
                actor_uuid UUID,
                actor_name TEXT,
                action TEXT NOT NULL,
                target TEXT NOT NULL,
                before JSONB,
                after JSONB
            )
            """;
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(skillsTable);
            statement.execute(questsTable);
            statement.execute(rolesTable);
            statement.execute(playerRolesTable);
            statement.execute(auditTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to init database tables: " + e.getMessage());
        }
    }

    public void shutdown() {
        executor.shutdown();
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private HikariDataSource createDataSource(HikariConfig hikariConfig) {
        try {
            return new HikariDataSource(hikariConfig);
        } catch (RuntimeException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("existiert nicht")) {
                plugin.getLogger().warning("Database not found. Attempting to create '" + databaseName + "'.");
                if (createDatabase()) {
                    return new HikariDataSource(hikariConfig);
                }
            }
            plugin.getLogger().severe("Failed to initialize database: " + ex.getMessage());
            return null;
        }
    }

    private boolean createDatabase() {
        String adminUrl = jdbcUrl.replace("/" + databaseName, "/postgres");
        try (Connection connection = java.sql.DriverManager.getConnection(adminUrl, username, password);
             java.sql.PreparedStatement exists = connection.prepareStatement(
                 "SELECT 1 FROM pg_database WHERE datname = ?")) {
            exists.setString(1, databaseName);
            try (java.sql.ResultSet resultSet = exists.executeQuery()) {
                if (resultSet.next()) {
                    return true;
                }
            }
            try (java.sql.Statement statement = connection.createStatement()) {
                statement.execute("CREATE DATABASE \"" + databaseName + "\"");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create database: " + e.getMessage());
            return false;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/PlayerDao.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/PlayerDao.java`  
- Size: 298 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.db;

import com.example.rpg.model.PlayerProfile;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDao {
    CompletableFuture<Void> savePlayer(PlayerProfile profile);
    CompletableFuture<PlayerProfile> loadPlayer(UUID uuid);
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/SqlPlayerDao.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/db/SqlPlayerDao.java`  
- Size: 9995 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.db;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.RPGStat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqlPlayerDao implements PlayerDao {
    private final DatabaseService databaseService;
    private final Gson gson = new Gson();
    private final Type mapStringInt = new TypeToken<Map<String, Integer>>() {}.getType();
    private final Type mapStringLong = new TypeToken<Map<String, Long>>() {}.getType();
    private final Type mapIntString = new TypeToken<Map<Integer, String>>() {}.getType();
    private final Type listString = new TypeToken<List<String>>() {}.getType();
    private final Type mapStringObject = new TypeToken<Map<String, Object>>() {}.getType();

    public SqlPlayerDao(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @Override
    public CompletableFuture<Void> savePlayer(PlayerProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO rpg_players (uuid, level, xp, skill_points, mana, max_mana, class_id, gold, guild_id, elo,
                    professions, stats, learned_skills, active_quests, completed_quests, faction_rep, skill_cooldowns, skill_bindings)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb, ?::jsonb)
                ON CONFLICT (uuid) DO UPDATE SET
                    level = EXCLUDED.level,
                    xp = EXCLUDED.xp,
                    skill_points = EXCLUDED.skill_points,
                    mana = EXCLUDED.mana,
                    max_mana = EXCLUDED.max_mana,
                    class_id = EXCLUDED.class_id,
                    gold = EXCLUDED.gold,
                    guild_id = EXCLUDED.guild_id,
                    elo = EXCLUDED.elo,
                    professions = EXCLUDED.professions,
                    stats = EXCLUDED.stats,
                    learned_skills = EXCLUDED.learned_skills,
                    active_quests = EXCLUDED.active_quests,
                    completed_quests = EXCLUDED.completed_quests,
                    faction_rep = EXCLUDED.faction_rep,
                    skill_cooldowns = EXCLUDED.skill_cooldowns,
                    skill_bindings = EXCLUDED.skill_bindings
                """;
            try (Connection connection = databaseService.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, profile.uuid());
                statement.setInt(2, profile.level());
                statement.setInt(3, profile.xp());
                statement.setInt(4, profile.skillPoints());
                statement.setInt(5, profile.mana());
                statement.setInt(6, profile.maxMana());
                statement.setString(7, profile.classId());
                statement.setInt(8, profile.gold());
                statement.setString(9, profile.guildId());
                statement.setInt(10, profile.elo());
                statement.setString(11, gson.toJson(profile.professions()));
                statement.setString(12, gson.toJson(statsToMap(profile.stats())));
                statement.setString(13, gson.toJson(profile.learnedSkills()));
                statement.setString(14, gson.toJson(questsToMap(profile.activeQuests())));
                statement.setString(15, gson.toJson(profile.completedQuests().stream().toList()));
                statement.setString(16, gson.toJson(profile.factionRep()));
                statement.setString(17, gson.toJson(profile.skillCooldowns()));
                statement.setString(18, gson.toJson(profile.skillBindings()));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, databaseService.executor());
    }

    @Override
    public CompletableFuture<PlayerProfile> loadPlayer(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM rpg_players WHERE uuid = ?";
            try (Connection connection = databaseService.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setObject(1, uuid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    PlayerProfile profile = new PlayerProfile(uuid);
                    profile.setLevel(resultSet.getInt("level"));
                    profile.setXp(resultSet.getInt("xp"));
                    profile.setSkillPoints(resultSet.getInt("skill_points"));
                    profile.setMana(resultSet.getInt("mana"));
                    profile.setMaxMana(resultSet.getInt("max_mana"));
                    profile.setClassId(resultSet.getString("class_id"));
                    profile.setGold(resultSet.getInt("gold"));
                    profile.setGuildId(resultSet.getString("guild_id"));
                    profile.setElo(resultSet.getInt("elo"));
                    applyMap(resultSet.getString("professions"), profile.professions(), mapStringInt);
                    Map<String, Integer> stats = fromJson(resultSet.getString("stats"), mapStringInt);
                    if (stats != null) {
                        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                            try {
                                profile.stats().put(RPGStat.valueOf(entry.getKey()), entry.getValue());
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                    applyMap(resultSet.getString("learned_skills"), profile.learnedSkills(), mapStringInt);
                    loadQuests(resultSet.getString("active_quests"), profile);
                    Set<String> completed = fromJson(resultSet.getString("completed_quests"),
                        new TypeToken<Set<String>>() {}.getType());
                    if (completed != null) {
                        profile.completedQuests().addAll(completed);
                    }
                    applyMap(resultSet.getString("faction_rep"), profile.factionRep(), mapStringInt);
                    applyMap(resultSet.getString("skill_cooldowns"), profile.skillCooldowns(), mapStringLong);
                    Map<Integer, String> bindings = fromJson(resultSet.getString("skill_bindings"), mapIntString);
                    if (bindings != null) {
                        profile.skillBindings().putAll(bindings);
                    }
                    return profile;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, databaseService.executor());
    }

    private Map<String, Integer> statsToMap(Map<RPGStat, Integer> stats) {
        Map<String, Integer> mapped = new HashMap<>();
        for (Map.Entry<RPGStat, Integer> entry : stats.entrySet()) {
            mapped.put(entry.getKey().name(), entry.getValue());
        }
        return mapped;
    }

    private Map<String, Object> questsToMap(Map<String, QuestProgress> quests) {
        Map<String, Object> data = new HashMap<>();
        for (QuestProgress progress : quests.values()) {
            Map<String, Object> quest = new HashMap<>();
            Map<String, Integer> steps = new HashMap<>();
            for (Map.Entry<Integer, Integer> entry : progress.stepProgress().entrySet()) {
                steps.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            quest.put("steps", steps);
            quest.put("completed", progress.completed());
            data.put(progress.questId(), quest);
        }
        return data;
    }

    private void loadQuests(String json, PlayerProfile profile) {
        Map<String, Object> data = fromJson(json, mapStringObject);
        if (data == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            QuestProgress progress = new QuestProgress(entry.getKey());
            if (entry.getValue() instanceof Map<?, ?> map) {
                Object stepsObj = map.get("steps");
                if (stepsObj instanceof Map<?, ?> steps) {
                    for (Map.Entry<?, ?> stepEntry : steps.entrySet()) {
                        try {
                            int step = Integer.parseInt(String.valueOf(stepEntry.getKey()));
                            int value = Integer.parseInt(String.valueOf(stepEntry.getValue()));
                            progress.incrementStep(step, value);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                Object completed = map.get("completed");
                if (completed instanceof Boolean done) {
                    progress.setCompleted(done);
                }
            }
            profile.activeQuests().put(entry.getKey(), progress);
        }
    }

    private <T> void applyMap(String json, Map<String, T> target, Type type) {
        Map<String, T> data = fromJson(json, type);
        if (data != null) {
            target.putAll(data);
        }
    }

    private <T> T fromJson(String json, Type type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return gson.fromJson(json, type);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/DungeonGenerator.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/DungeonGenerator.java`  
- Size: 9229 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.dungeon.wfc.Pattern;
import com.example.rpg.dungeon.wfc.WfcGenerator;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

public class DungeonGenerator {
    private final RPGPlugin plugin;
    private final Random random = new Random();
    private final WfcGenerator wfcGenerator;

    public DungeonGenerator(RPGPlugin plugin) {
        this.plugin = plugin;
        this.wfcGenerator = new WfcGenerator(plugin);
    }

    public DungeonInstance generate(String theme, List<Player> party) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int roomSize = 9;
        int grid = 4;
        int baseY = 60;
        List<Location> roomCenters = new ArrayList<>();
        for (int x = 0; x < grid; x++) {
            for (int z = 0; z < grid; z++) {
                int startX = x * (roomSize + 2);
                int startZ = z * (roomSize + 2);
                carveRoom(world, startX, baseY, startZ, roomSize, Material.STONE_BRICKS);
                roomCenters.add(new Location(world, startX + roomSize / 2.0, baseY + 1, startZ + roomSize / 2.0));
                if (x > 0) {
                    carveCorridor(world, startX - 1, baseY, startZ + roomSize / 2, Material.COBBLESTONE);
                }
                if (z > 0) {
                    carveCorridor(world, startX + roomSize / 2, baseY, startZ - 1, Material.COBBLESTONE);
                }
            }
        }
        Location start = roomCenters.get(0).clone();
        Location bossRoom = roomCenters.get(roomCenters.size() - 1).clone();
        spawnSpawners(roomCenters, theme);
        spawnBoss(bossRoom);
        spawnSigns(start, bossRoom, theme);
        return new DungeonInstance(world, start, bossRoom);
    }

    public void generateWfc(String theme, List<Player> party, Consumer<DungeonInstance> callback) {
        String worldName = "dungeon_" + System.currentTimeMillis();
        World world = plugin.getServer().createWorld(new WorldCreator(worldName));
        int width = plugin.getConfig().getInt("dungeon.wfc.width", 10);
        int height = plugin.getConfig().getInt("dungeon.wfc.height", 3);
        int depth = plugin.getConfig().getInt("dungeon.wfc.depth", 10);
        int originY = plugin.getConfig().getInt("dungeon.wfc.originY", 60);
        wfcGenerator.generate(theme, width, height, depth).thenAccept(patterns -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (patterns == null) {
                        return;
                    }
                    Location start = new Location(world, 1, originY + 2, 1);
                    Location bossRoom = buildFromPatterns(world, patterns, originY);
                    DungeonInstance instance = new DungeonInstance(world, start, bossRoom);
                    callback.accept(instance);
                }
            }.runTask(plugin);
        });
    }

    private Location buildFromPatterns(World world, Pattern[][][] patterns, int originY) {
        Location start = null;
        Location farthest = null;
        double bestDistance = 0;
        int cellSize = 2;
        for (int x = 0; x < patterns.length; x++) {
            for (int y = 0; y < patterns[x].length; y++) {
                for (int z = 0; z < patterns[x][y].length; z++) {
                    Pattern pattern = patterns[x][y][z];
                    if (pattern == null) {
                        continue;
                    }
                    int baseX = x * cellSize;
                    int baseY = originY + y * cellSize;
                    int baseZ = z * cellSize;
                    placePattern(world, pattern, baseX, baseY, baseZ);
                    if ("FLOOR".equals(pattern.socketDown())) {
                        Location center = new Location(world, baseX + 0.5, baseY + 1, baseZ + 0.5);
                        if (start == null) {
                            start = center;
                        }
                        double distance = start != null ? start.distanceSquared(center) : 0;
                        if (distance > bestDistance) {
                            bestDistance = distance;
                            farthest = center;
                        }
                    }
                }
            }
        }
        if (farthest == null) {
            farthest = new Location(world, 1, originY + 2, 1);
        }
        spawnBoss(farthest);
        return farthest;
    }

    private void placePattern(World world, Pattern pattern, int baseX, int baseY, int baseZ) {
        Material[] blocks = pattern.blocks();
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    Material material = blocks[index++];
                    world.getBlockAt(baseX + x, baseY + y, baseZ + z).setType(material, false);
                }
            }
        }
    }

    private void carveRoom(World world, int startX, int startY, int startZ, int size, Material material) {
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                world.getBlockAt(x, startY, z).setType(material);
                for (int y = 1; y <= 4; y++) {
                    world.getBlockAt(x, startY + y, z).setType(Material.AIR);
                }
            }
        }
        for (int x = startX; x < startX + size; x++) {
            for (int z = startZ; z < startZ + size; z++) {
                if (x == startX || x == startX + size - 1 || z == startZ || z == startZ + size - 1) {
                    world.getBlockAt(x, startY + 5, z).setType(material);
                }
            }
        }
    }

    private void carveCorridor(World world, int startX, int startY, int startZ, Material material) {
        for (int i = 0; i < 3; i++) {
            world.getBlockAt(startX + i, startY, startZ).setType(material);
            for (int y = 1; y <= 3; y++) {
                world.getBlockAt(startX + i, startY + y, startZ).setType(Material.AIR);
            }
        }
    }

    private void spawnSpawners(List<Location> roomCenters, String theme) {
        String spawnerId = theme + "_spawner";
        Spawner spawner = plugin.spawnerManager().getSpawner(spawnerId);
        for (int i = 1; i < roomCenters.size() - 1; i++) {
            Location location = roomCenters.get(i);
            if (spawner == null) {
                spawnFallbackMob(location);
                continue;
            }
            if (spawner.mobs().isEmpty()) {
                spawnFallbackMob(location);
                continue;
            }
            String mobId = spawner.mobs().keySet().iterator().next();
            MobDefinition mob = plugin.mobManager().getMob(mobId);
            if (mob == null) {
                spawnFallbackMob(location);
                continue;
            }
            var entity = location.getWorld().spawnEntity(location, EntityType.valueOf(mob.type().toUpperCase()));
            if (entity instanceof org.bukkit.entity.LivingEntity living) {
                plugin.customMobListener().applyDefinition(living, mob);
            }
        }
    }

    private void spawnFallbackMob(Location location) {
        location.getWorld().spawnEntity(location, EntityType.ZOMBIE);
    }

    private void spawnBoss(Location bossRoom) {
        MobDefinition boss = plugin.mobManager().getMob("boss_zombie");
        if (boss == null) {
            return;
        }
        var entity = bossRoom.getWorld().spawnEntity(bossRoom, EntityType.valueOf(boss.type().toUpperCase()));
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, boss);
            TextDisplay display = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
            Component bossName = LegacyComponentSerializer.legacySection().deserialize(boss.name());
            display.text(Component.text("Boss: ").append(bossName));
        }
    }

    private void spawnSigns(Location start, Location bossRoom, String theme) {
        TextDisplay startSign = start.getWorld().spawn(start.clone().add(0, 2, 0), TextDisplay.class);
        startSign.text(Text.mm("<gold>Dungeon: " + theme));
        TextDisplay bossSign = bossRoom.getWorld().spawn(bossRoom.clone().add(0, 2, 0), TextDisplay.class);
        bossSign.text(Text.mm("<red>Boss-Raum"));
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/DungeonInstance.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/DungeonInstance.java`  
- Size: 577 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon;

import org.bukkit.Location;
import org.bukkit.World;

public class DungeonInstance {
    private final World world;
    private final Location start;
    private final Location bossRoom;

    public DungeonInstance(World world, Location start, Location bossRoom) {
        this.world = world;
        this.start = start;
        this.bossRoom = bossRoom;
    }

    public World world() {
        return world;
    }

    public Location start() {
        return start;
    }

    public Location bossRoom() {
        return bossRoom;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/Direction.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/Direction.java`  
- Size: 393 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon.wfc;

public enum Direction {
    UP,
    DOWN,
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/Pattern.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/Pattern.java`  
- Size: 950 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon.wfc;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

public class Pattern {
    private final String id;
    private final Material[] blocks;
    private final Map<Direction, String> sockets = new EnumMap<>(Direction.class);
    private final double weight;

    public Pattern(String id, Material[] blocks, double weight) {
        this.id = id;
        this.blocks = blocks;
        this.weight = weight;
    }

    public String id() {
        return id;
    }

    public Material[] blocks() {
        return blocks;
    }

    public double weight() {
        return weight;
    }

    public void setSocket(Direction direction, String socket) {
        sockets.put(direction, socket);
    }

    public String socket(Direction direction) {
        return sockets.getOrDefault(direction, "AIR");
    }

    public String socketDown() {
        return socket(Direction.DOWN);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/PatternLoader.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/PatternLoader.java`  
- Size: 6059 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class PatternLoader {
    private final JavaPlugin plugin;

    public PatternLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public List<Pattern> loadPatterns(String themeName) {
        List<Pattern> patterns = new ArrayList<>();
        ThemeMaterials materials = resolveTheme(themeName);

        Pattern air = new Pattern("air", fill(Material.AIR), 1.0);
        for (Direction direction : Direction.values()) {
            air.setSocket(direction, "AIR");
        }
        patterns.add(air);

        Pattern floor = new Pattern("floor", floorBlocks(materials.floor()), 1.5);
        floor.setSocket(Direction.DOWN, "FLOOR");
        floor.setSocket(Direction.UP, "AIR");
        floor.setSocket(Direction.NORTH, "AIR");
        floor.setSocket(Direction.SOUTH, "AIR");
        floor.setSocket(Direction.EAST, "AIR");
        floor.setSocket(Direction.WEST, "AIR");
        patterns.add(floor);

        Pattern wallNorth = new Pattern("wall_north", wallBlocks(Direction.NORTH, materials.wall()), 1.0);
        wallNorth.setSocket(Direction.DOWN, "FLOOR");
        wallNorth.setSocket(Direction.UP, "AIR");
        wallNorth.setSocket(Direction.NORTH, "WALL");
        wallNorth.setSocket(Direction.SOUTH, "AIR");
        wallNorth.setSocket(Direction.EAST, "AIR");
        wallNorth.setSocket(Direction.WEST, "AIR");
        patterns.add(wallNorth);
        Pattern wallEast = rotateY(wallNorth, "wall_east");
        Pattern wallSouth = rotateY(wallEast, "wall_south");
        Pattern wallWest = rotateY(wallSouth, "wall_west");
        patterns.add(wallEast);
        patterns.add(wallSouth);
        patterns.add(wallWest);

        Pattern corridor = new Pattern("corridor_ns", corridorBlocks(materials.corridor()), 1.2);
        corridor.setSocket(Direction.DOWN, "FLOOR");
        corridor.setSocket(Direction.UP, "AIR");
        corridor.setSocket(Direction.NORTH, "OPEN");
        corridor.setSocket(Direction.SOUTH, "OPEN");
        corridor.setSocket(Direction.EAST, "WALL");
        corridor.setSocket(Direction.WEST, "WALL");
        patterns.add(corridor);
        patterns.add(rotateY(corridor, "corridor_ew"));

        return patterns;
    }

    private Material[] fill(Material material) {
        Material[] blocks = new Material[8];
        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = material;
        }
        return blocks;
    }

    private Material[] floorBlocks(Material floorMaterial) {
        Material[] blocks = new Material[8];
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    blocks[index++] = y == 0 ? floorMaterial : Material.AIR;
                }
            }
        }
        return blocks;
    }

    private Material[] wallBlocks(Direction direction, Material wallMaterial) {
        Material[] blocks = floorBlocks(wallMaterial);
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    if (y == 1 && isWallCell(direction, x, z)) {
                        blocks[index] = wallMaterial;
                    }
                    index++;
                }
            }
        }
        return blocks;
    }

    private boolean isWallCell(Direction direction, int x, int z) {
        return switch (direction) {
            case NORTH -> z == 0;
            case SOUTH -> z == 1;
            case EAST -> x == 1;
            case WEST -> x == 0;
            default -> false;
        };
    }

    private Material[] corridorBlocks(Material corridorMaterial) {
        return floorBlocks(corridorMaterial);
    }

    private Pattern rotateY(Pattern base, String newId) {
        Material[] rotated = new Material[8];
        int index = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    int rx = z;
                    int rz = 1 - x;
                    int targetIndex = rx * 4 + y * 2 + rz;
                    rotated[targetIndex] = base.blocks()[index++];
                }
            }
        }
        Pattern rotatedPattern = new Pattern(newId, rotated, base.weight());
        rotatedPattern.setSocket(Direction.UP, base.socket(Direction.UP));
        rotatedPattern.setSocket(Direction.DOWN, base.socket(Direction.DOWN));
        rotatedPattern.setSocket(Direction.NORTH, base.socket(Direction.WEST));
        rotatedPattern.setSocket(Direction.EAST, base.socket(Direction.NORTH));
        rotatedPattern.setSocket(Direction.SOUTH, base.socket(Direction.EAST));
        rotatedPattern.setSocket(Direction.WEST, base.socket(Direction.SOUTH));
        return rotatedPattern;
    }

    private ThemeMaterials resolveTheme(String themeName) {
        String key = themeName != null ? themeName.toLowerCase() : "crypt";
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("dungeon.themes." + key);
        Material floor = readMaterial(section, "floor_material", Material.STONE_BRICKS);
        Material wall = readMaterial(section, "wall_material", Material.COBBLESTONE);
        Material corridor = readMaterial(section, "corridor_material", Material.COBBLESTONE);
        return new ThemeMaterials(floor, wall, corridor);
    }

    private Material readMaterial(ConfigurationSection section, String path, Material fallback) {
        if (section == null) {
            return fallback;
        }
        String raw = section.getString(path, fallback.name());
        Material material = Material.matchMaterial(raw);
        return material != null ? material : fallback;
    }

    private record ThemeMaterials(Material floor, Material wall, Material corridor) {}
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/WaveGrid.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/WaveGrid.java`  
- Size: 1347 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon.wfc;

import java.util.ArrayList;
import java.util.List;

public class WaveGrid {
    private final List<Pattern>[][][] possibilities;
    private final boolean[][][] collapsed;

    @SuppressWarnings("unchecked")
    public WaveGrid(int width, int height, int depth, List<Pattern> initial) {
        possibilities = new List[width][height][depth];
        collapsed = new boolean[width][height][depth];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    possibilities[x][y][z] = new ArrayList<>(initial);
                }
            }
        }
    }

    public List<Pattern> possibilities(int x, int y, int z) {
        return possibilities[x][y][z];
    }

    public void setPossibilities(int x, int y, int z, List<Pattern> list) {
        possibilities[x][y][z] = list;
    }

    public boolean collapsed(int x, int y, int z) {
        return collapsed[x][y][z];
    }

    public void setCollapsed(int x, int y, int z, boolean value) {
        collapsed[x][y][z] = value;
    }

    public int width() {
        return possibilities.length;
    }

    public int height() {
        return possibilities[0].length;
    }

    public int depth() {
        return possibilities[0][0].length;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/WfcGenerator.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/dungeon/wfc/WfcGenerator.java`  
- Size: 6089 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.dungeon.wfc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;

public class WfcGenerator {
    private final PatternLoader patternLoader;
    private final Random random = new Random();

    public WfcGenerator(JavaPlugin plugin) {
        this.patternLoader = new PatternLoader(plugin);
    }

    public CompletableFuture<Pattern[][][]> generate(String themeName, int width, int height, int depth) {
        return CompletableFuture.supplyAsync(() -> {
            List<Pattern> patterns = patternLoader.loadPatterns(themeName);
            for (int attempt = 0; attempt < 5; attempt++) {
                Pattern[][][] result = runAttempt(width, height, depth, patterns);
                if (result != null) {
                    return result;
                }
            }
            return null;
        });
    }

    private Pattern[][][] runAttempt(int width, int height, int depth, List<Pattern> patterns) {
        WaveGrid grid = new WaveGrid(width, height, depth, patterns);
        while (true) {
            int[] cell = findLowestEntropyCell(grid);
            if (cell == null) {
                break;
            }
            int x = cell[0];
            int y = cell[1];
            int z = cell[2];
            Pattern chosen = pickWeighted(grid.possibilities(x, y, z));
            grid.setPossibilities(x, y, z, List.of(chosen));
            grid.setCollapsed(x, y, z, true);
            if (!propagate(grid, x, y, z)) {
                return null;
            }
        }
        Pattern[][][] patternsResult = new Pattern[width][height][depth];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    List<Pattern> options = grid.possibilities(x, y, z);
                    patternsResult[x][y][z] = options.isEmpty() ? null : options.get(0);
                }
            }
        }
        return patternsResult;
    }

    private int[] findLowestEntropyCell(WaveGrid grid) {
        int bestX = -1;
        int bestY = -1;
        int bestZ = -1;
        int bestEntropy = Integer.MAX_VALUE;
        for (int x = 0; x < grid.width(); x++) {
            for (int y = 0; y < grid.height(); y++) {
                for (int z = 0; z < grid.depth(); z++) {
                    if (grid.collapsed(x, y, z)) {
                        continue;
                    }
                    int size = grid.possibilities(x, y, z).size();
                    if (size == 0) {
                        return new int[] {x, y, z};
                    }
                    if (size < bestEntropy) {
                        bestEntropy = size;
                        bestX = x;
                        bestY = y;
                        bestZ = z;
                    }
                }
            }
        }
        if (bestX == -1) {
            return null;
        }
        return new int[] {bestX, bestY, bestZ};
    }

    private Pattern pickWeighted(List<Pattern> options) {
        double total = options.stream().mapToDouble(Pattern::weight).sum();
        double roll = random.nextDouble() * total;
        double current = 0;
        for (Pattern pattern : options) {
            current += pattern.weight();
            if (roll <= current) {
                return pattern;
            }
        }
        return options.get(0);
    }

    private boolean propagate(WaveGrid grid, int startX, int startY, int startZ) {
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {startX, startY, startZ});
        while (!queue.isEmpty()) {
            int[] cell = queue.poll();
            int x = cell[0];
            int y = cell[1];
            int z = cell[2];
            for (Direction direction : Direction.values()) {
                int nx = x + offsetX(direction);
                int ny = y + offsetY(direction);
                int nz = z + offsetZ(direction);
                if (!inside(grid, nx, ny, nz)) {
                    continue;
                }
                List<Pattern> neighborOptions = grid.possibilities(nx, ny, nz);
                List<Pattern> filtered = new ArrayList<>();
                for (Pattern option : neighborOptions) {
                    if (compatible(grid.possibilities(x, y, z), option, direction)) {
                        filtered.add(option);
                    }
                }
                if (filtered.isEmpty()) {
                    return false;
                }
                if (filtered.size() != neighborOptions.size()) {
                    grid.setPossibilities(nx, ny, nz, filtered);
                    queue.add(new int[] {nx, ny, nz});
                }
            }
        }
        return true;
    }

    private boolean compatible(List<Pattern> sourceOptions, Pattern neighbor, Direction direction) {
        for (Pattern source : sourceOptions) {
            String socketA = source.socket(direction);
            String socketB = neighbor.socket(direction.opposite());
            if (socketA.equals(socketB)) {
                return true;
            }
        }
        return false;
    }

    private boolean inside(WaveGrid grid, int x, int y, int z) {
        return x >= 0 && x < grid.width()
            && y >= 0 && y < grid.height()
            && z >= 0 && z < grid.depth();
    }

    private int offsetX(Direction direction) {
        return switch (direction) {
            case EAST -> 1;
            case WEST -> -1;
            default -> 0;
        };
    }

    private int offsetY(Direction direction) {
        return switch (direction) {
            case UP -> 1;
            case DOWN -> -1;
            default -> 0;
        };
    }

    private int offsetZ(Direction direction) {
        return switch (direction) {
            case SOUTH -> 1;
            case NORTH -> -1;
            default -> 0;
        };
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/BehaviorTreeEditorGui.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/BehaviorTreeEditorGui.java`  
- Size: 1785 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.gui;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class BehaviorTreeEditorGui {
    private final RPGPlugin plugin;

    public BehaviorTreeEditorGui(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, String treeName) {
        Inventory inventory = Bukkit.createInventory(new GuiHolders.BehaviorTreeEditorHolder(treeName), 27,
            Component.text("Behavior Editor: " + treeName));

        inventory.setItem(10, new ItemBuilder(Material.TOTEM_OF_UNDYING)
            .name(Text.mm("<gold>Notfall-Heilung"))
            .loreLine(Text.mm("<gray>health_below + shield_wall + heal_self"))
            .loreLine(Text.mm("<yellow>Klick, um Parameter einzugeben"))
            .build());

        inventory.setItem(12, new ItemBuilder(Material.BLAZE_ROD)
            .name(Text.mm("<red>Fernkampf-Phase"))
            .loreLine(Text.mm("<gray>target_distance_above + cast_skill"))
            .loreLine(Text.mm("<yellow>Klick, um Parameter einzugeben"))
            .build());

        inventory.setItem(14, new ItemBuilder(Material.IRON_SWORD)
            .name(Text.mm("<green>Nahkampf"))
            .loreLine(Text.mm("<gray>melee_attack"))
            .loreLine(Text.mm("<yellow>Klick, um hinzuzufügen"))
            .build());

        inventory.setItem(16, new ItemBuilder(Material.BARRIER)
            .name(Text.mm("<red>Zurücksetzen"))
            .loreLine(Text.mm("<gray>Leert den Baum"))
            .build());

        player.openInventory(inventory);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/GuiHolders.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/GuiHolders.java`  
- Size: 6107 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Robuste Identifikation von GUIs:
 * Nicht über Inventory-Titel (anfällig für Farbe/Locale),
 * sondern über InventoryHolder-Typen.
 */
public final class GuiHolders {
    private GuiHolders() {}

    public static final class PlayerMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class AdminMenuHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class QuestListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SkillTreeHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class BuildingCategoryHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class BuildingListHolder implements InventoryHolder {
        private final String category;

        public BuildingListHolder(String category) {
            this.category = category;
        }

        public String category() {
            return category;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class BehaviorTreeEditorHolder implements InventoryHolder {
        private final String treeName;

        public BehaviorTreeEditorHolder(String treeName) {
            this.treeName = treeName;
        }

        public String treeName() {
            return treeName;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class ShopHolder implements InventoryHolder {
        private final String shopId;

        public ShopHolder(String shopId) {
            this.shopId = shopId;
        }

        public String shopId() {
            return shopId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class SchematicMoveHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PermissionsMainHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleListHolder implements InventoryHolder {
        private final int page;

        public RoleListHolder(int page) {
            this.page = page;
        }

        public int page() {
            return page;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleDetailHolder implements InventoryHolder {
        private final String roleKey;

        public RoleDetailHolder(String roleKey) {
            this.roleKey = roleKey;
        }

        public String roleKey() {
            return roleKey;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleNodesHolder implements InventoryHolder {
        private final String roleKey;
        private final int page;

        public RoleNodesHolder(String roleKey, int page) {
            this.roleKey = roleKey;
            this.page = page;
        }

        public String roleKey() {
            return roleKey;
        }

        public int page() {
            return page;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class RoleParentsHolder implements InventoryHolder {
        private final String roleKey;

        public RoleParentsHolder(String roleKey) {
            this.roleKey = roleKey;
        }

        public String roleKey() {
            return roleKey;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PlayerListHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PlayerRoleHolder implements InventoryHolder {
        private final java.util.UUID targetId;

        public PlayerRoleHolder(java.util.UUID targetId) {
            this.targetId = targetId;
        }

        public java.util.UUID targetId() {
            return targetId;
        }

        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }

    public static final class PermissionAuditHolder implements InventoryHolder {
        @Override
        public @NotNull Inventory getInventory() {
            throw new UnsupportedOperationException();
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/GuiManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/GuiManager.java`  
- Size: 22020 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.gui;

import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.BuildingManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import com.example.rpg.model.BuildingCategory;
import com.example.rpg.model.BuildingDefinition;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class GuiManager {
    private final PlayerDataManager playerDataManager;
    private final QuestManager questManager;
    private final SkillManager skillManager;
    private final ClassManager classManager;
    private final FactionManager factionManager;
    private final BuildingManager buildingManager;
    private final com.example.rpg.permissions.PermissionService permissionService;
    private final NamespacedKey questKey;
    private final NamespacedKey skillKey;
    private final NamespacedKey buildingKey;
    private final NamespacedKey buildingCategoryKey;
    private final NamespacedKey permRoleKey;
    private final NamespacedKey permPlayerKey;
    private final NamespacedKey permNodeKey;
    private final NamespacedKey permActionKey;

    public GuiManager(PlayerDataManager playerDataManager, QuestManager questManager, SkillManager skillManager,
                      ClassManager classManager, FactionManager factionManager, BuildingManager buildingManager,
                      com.example.rpg.permissions.PermissionService permissionService,
                      NamespacedKey questKey, NamespacedKey skillKey, NamespacedKey buildingKey, NamespacedKey buildingCategoryKey,
                      NamespacedKey permRoleKey, NamespacedKey permPlayerKey, NamespacedKey permNodeKey, NamespacedKey permActionKey) {
        this.playerDataManager = playerDataManager;
        this.questManager = questManager;
        this.skillManager = skillManager;
        this.classManager = classManager;
        this.factionManager = factionManager;
        this.buildingManager = buildingManager;
        this.permissionService = permissionService;
        this.questKey = questKey;
        this.skillKey = skillKey;
        this.buildingKey = buildingKey;
        this.buildingCategoryKey = buildingCategoryKey;
        this.permRoleKey = permRoleKey;
        this.permPlayerKey = permPlayerKey;
        this.permNodeKey = permNodeKey;
        this.permActionKey = permActionKey;
    }

    public void openPlayerMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerMenuHolder(), 27, Component.text("RPG Menü"));
        PlayerProfile profile = playerDataManager.getProfile(player);

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<gold>Charakter"))
            .loreLine(Text.mm("<gray>Level: <white>" + profile.level()))
            .loreLine(Text.mm("<gray>XP: <white>" + profile.xp() + "/" + profile.xpNeeded()))
            .loreLine(Text.mm("<gray>Klasse: <white>" + resolveClassName(profile.classId())))
            .build());

        inv.setItem(12, new ItemBuilder(Material.NETHER_STAR)
            .name(Text.mm("<aqua>Skills"))
            .loreLine(Text.mm("<gray>Skillpunkte: <white>" + profile.skillPoints()))
            .build());

        inv.setItem(14, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<green>Quests"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.activeQuests().size()))
            .build());

        inv.setItem(16, new ItemBuilder(Material.EMERALD)
            .name(Text.mm("<yellow>Fraktionen"))
            .loreLine(Text.mm("<gray>Ruf verwalten"))
            .loreLine(Text.mm("<gray>Aktiv: <white>" + profile.factionRep().size() + "/" + factionManager.factions().size()))
            .build());

        player.openInventory(inv);
    }

    public void openAdminMenu(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.AdminMenuHolder(), 27, Component.text("RPG Admin"));
        inv.setItem(10, new ItemBuilder(Material.COMPASS)
            .name(Text.mm("<gold>Zonen-Editor"))
            .loreLine(Text.mm("<gray>Regionen verwalten"))
            .build());
        inv.setItem(11, new ItemBuilder(Material.VILLAGER_SPAWN_EGG)
            .name(Text.mm("<green>NPC-Editor"))
            .loreLine(Text.mm("<gray>NPCs platzieren"))
            .build());
        inv.setItem(12, new ItemBuilder(Material.WRITABLE_BOOK)
            .name(Text.mm("<aqua>Quest-Editor"))
            .loreLine(Text.mm("<gray>Quests erstellen"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.CHEST)
            .name(Text.mm("<yellow>Loot-Tabellen"))
            .loreLine(Text.mm("<gray>Loot konfigurieren"))
            .build());
        inv.setItem(14, new ItemBuilder(Material.BLAZE_POWDER)
            .name(Text.mm("<light_purple>Skills & Klassen"))
            .loreLine(Text.mm("<gray>Skills verwalten"))
            .build());
        inv.setItem(15, new ItemBuilder(Material.REDSTONE)
            .name(Text.mm("<red>Debug Overlay"))
            .loreLine(Text.mm("<gray>Region/Quest Debug"))
            .build());
        inv.setItem(16, new ItemBuilder(Material.BRICKS)
            .name(Text.mm("<gold>Bau-Manager"))
            .loreLine(Text.mm("<gray>Gebäude platzieren"))
            .build());
        inv.setItem(17, new ItemBuilder(Material.NAME_TAG)
            .name(Text.mm("<aqua>Permissions"))
            .loreLine(Text.mm("<gray>Rollen & Rechte verwalten"))
            .build());
        player.openInventory(inv);
    }

    public void openBuildingCategories(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.BuildingCategoryHolder(), 27, Component.text("Gebäude Kategorien"));
        int slot = 10;
        for (BuildingCategory category : BuildingCategory.values()) {
            ItemStack item = new ItemBuilder(Material.BOOKSHELF)
                .name(Text.mm("<yellow>" + category.displayName()))
                .loreLine(Text.mm("<gray>Kategorie öffnen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(buildingCategoryKey, PersistentDataType.STRING, category.name());
            item.setItemMeta(meta);
            inv.setItem(slot, item);
            slot += 2;
        }
        ItemStack single = new ItemBuilder(Material.PAPER)
            .name(Text.mm("<gold>Einzel-Schema"))
            .loreLine(Text.mm("<gray>Nur ein Schema platzieren"))
            .build();
        ItemMeta singleMeta = single.getItemMeta();
        singleMeta.getPersistentDataContainer().set(buildingCategoryKey, PersistentDataType.STRING, "SINGLE");
        single.setItemMeta(singleMeta);
        inv.setItem(22, single);
        player.openInventory(inv);
    }

    public void openBuildingList(Player player, BuildingCategory category) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.BuildingListHolder(category.name()), 54, Component.text(category.displayName()));
        int slot = 0;
        for (BuildingDefinition building : buildingManager.byCategory().getOrDefault(category, List.of())) {
            if (slot >= inv.getSize()) {
                break;
            }
            ItemStack item = new ItemBuilder(Material.OAK_DOOR)
                .name(Text.mm("<green>" + building.name()))
                .loreLine(Text.mm("<gray>ID: <white>" + building.id()))
                .loreLine(Text.mm("<yellow>Klick: platzieren"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(buildingKey, PersistentDataType.STRING, building.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openQuestList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.QuestListHolder(), 27, Component.text("Quests"));
        int slot = 0;
        for (Quest quest : questManager.quests().values()) {
            if (slot >= inv.getSize()) {
                break;
            }
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<green>" + quest.name()))
                .loreLine(Text.mm("<gray>" + quest.description()))
                .loreLine(Text.mm("<gray>Min Level: <white>" + quest.minLevel()))
                .loreLine(Text.mm("<yellow>Klicke zum Annehmen"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(questKey, PersistentDataType.STRING, quest.id());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openSkillList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillListHolder(), 27, Component.text("Skills"));
        PlayerProfile profile = playerDataManager.getProfile(player);
        int slot = 0;
        for (var entry : skillManager.skills().entrySet()) {
            if (slot >= inv.getSize()) {
                break;
            }
            String id = entry.getKey();
            var skill = entry.getValue();
            List<Component> lore = new ArrayList<>();
            lore.add(Text.mm("<gray>Kategorie: <white>" + skill.category()));
            lore.add(Text.mm("<gray>Typ: <white>" + skill.type()));
            lore.add(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"));
            lore.add(Text.mm("<gray>Mana: <white>" + skill.manaCost()));
            lore.add(Text.mm("<gray>Rang: <white>" + profile.learnedSkills().getOrDefault(id, 0)));
            if (skill.requiredSkill() != null) {
                lore.add(Text.mm("<gray>Voraussetzung: <white>" + skill.requiredSkill()));
            }
            if (!skill.effects().isEmpty()) {
                for (var effect : skill.effects()) {
                    lore.add(Text.mm("<gray>Effekt: <white>" + effect.describe()));
                }
            }
            lore.add(Text.mm("<yellow>Klick: Skill lernen"));
            ItemStack item = new ItemBuilder(Material.ENCHANTED_BOOK)
                .name(Text.mm("<aqua>" + skill.name()))
                .loreLines(lore)
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(skillKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        player.openInventory(inv);
    }

    public void openSchematicMoveGui(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SchematicMoveHolder(), 27, Component.text("Schematic verschieben"));
        inv.setItem(11, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Links")).build());
        inv.setItem(15, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Rechts")).build());
        inv.setItem(13, new ItemBuilder(Material.FEATHER).name(Text.mm("<yellow>Hoch")).build());
        inv.setItem(22, new ItemBuilder(Material.ANVIL).name(Text.mm("<yellow>Runter")).build());
        inv.setItem(26, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Fertig")).build());
        player.openInventory(inv);
    }

    public void openPermissionsMain(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PermissionsMainHolder(), 27, Component.text("Permissions"));
        inv.setItem(11, new ItemBuilder(Material.BOOK)
            .name(Text.mm("<yellow>Rollen"))
            .loreLine(Text.mm("<gray>Rollen verwalten"))
            .build());
        inv.setItem(13, new ItemBuilder(Material.PLAYER_HEAD)
            .name(Text.mm("<green>Spieler"))
            .loreLine(Text.mm("<gray>Rollen zuweisen"))
            .build());
        inv.setItem(15, new ItemBuilder(Material.PAPER)
            .name(Text.mm("<light_purple>Audit Log"))
            .loreLine(Text.mm("<gray>Letzte Änderungen"))
            .build());
        player.openInventory(inv);
    }

    public void openRoleList(Player player, int page) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleListHolder(page), 54, Component.text("Rollen"));
        int start = page * 45;
        int slot = 0;
        List<com.example.rpg.permissions.Role> roles = new ArrayList<>(permissionService.roles().values());
        roles.sort(java.util.Comparator.comparing(com.example.rpg.permissions.Role::key));
        for (int i = start; i < roles.size() && slot < 45; i++) {
            var role = roles.get(i);
            ItemStack item = new ItemBuilder(Material.BOOK)
                .name(Text.mm("<yellow>" + role.displayName()))
                .loreLine(Text.mm("<gray>Key: <white>" + role.key()))
                .loreLine(Text.mm("<gray>Nodes: <white>" + role.nodes().size()))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permRoleKey, PersistentDataType.STRING, role.key());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(53, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Neue Rolle")).build());
        player.openInventory(inv);
    }

    public void openRoleDetails(Player player, String roleKey) {
        var role = permissionService.roles().get(roleKey);
        if (role == null) {
            player.sendMessage(Text.mm("<red>Rolle nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleDetailHolder(roleKey), 27, Component.text("Rolle: " + role.displayName()));
        inv.setItem(11, new ItemBuilder(Material.NAME_TAG).name(Text.mm("<yellow>Eltern"))
            .loreLine(Text.mm("<gray>Vererbung verwalten")).build());
        inv.setItem(13, new ItemBuilder(Material.WRITABLE_BOOK).name(Text.mm("<yellow>Nodes"))
            .loreLine(Text.mm("<gray>Rechte verwalten")).build());
        inv.setItem(15, new ItemBuilder(Material.ANVIL).name(Text.mm("<yellow>Umbenennen"))
            .loreLine(Text.mm("<gray>Display-Name ändern")).build());
        inv.setItem(26, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Löschen")).build());
        player.openInventory(inv);
    }

    public void openRoleNodes(Player player, String roleKey, int page) {
        var role = permissionService.roles().get(roleKey);
        if (role == null) {
            player.sendMessage(Text.mm("<red>Rolle nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleNodesHolder(roleKey, page), 54, Component.text("Nodes: " + role.displayName()));
        List<String> nodes = new ArrayList<>(role.nodes().keySet());
        nodes.sort(String::compareToIgnoreCase);
        int start = page * 45;
        int slot = 0;
        for (int i = start; i < nodes.size() && slot < 45; i++) {
            String node = nodes.get(i);
            var decision = role.nodes().get(node);
            ItemStack item = new ItemBuilder(Material.PAPER)
                .name(Text.mm("<yellow>" + node))
                .loreLine(Text.mm("<gray>Status: <white>" + decision))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permRoleKey, PersistentDataType.STRING, roleKey);
            meta.getPersistentDataContainer().set(permNodeKey, PersistentDataType.STRING, node);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(53, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Node hinzufügen")).build());
        player.openInventory(inv);
    }

    public void openRoleParents(Player player, String roleKey) {
        var role = permissionService.roles().get(roleKey);
        if (role == null) {
            player.sendMessage(Text.mm("<red>Rolle nicht gefunden."));
            return;
        }
        Inventory inv = Bukkit.createInventory(new GuiHolders.RoleParentsHolder(roleKey), 54, Component.text("Eltern: " + role.displayName()));
        int slot = 0;
        for (var entry : permissionService.roles().values()) {
            if (slot >= 45) {
                break;
            }
            boolean active = role.parents().contains(entry.key());
            ItemStack item = new ItemBuilder(active ? Material.EMERALD_BLOCK : Material.GRAY_STAINED_GLASS_PANE)
                .name(Text.mm("<yellow>" + entry.displayName()))
                .loreLine(Text.mm(active ? "<green>Aktiv" : "<gray>Inaktiv"))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permRoleKey, PersistentDataType.STRING, roleKey);
            meta.getPersistentDataContainer().set(permNodeKey, PersistentDataType.STRING, entry.key());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        player.openInventory(inv);
    }

    public void openPlayerList(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerListHolder(), 54, Component.text("Spieler Rollen"));
        int slot = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) {
                break;
            }
            ItemStack item = new ItemBuilder(Material.PLAYER_HEAD)
                .name(Text.mm("<yellow>" + online.getName()))
                .loreLine(Text.mm("<gray>UUID: <white>" + online.getUniqueId()))
                .build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(permPlayerKey, PersistentDataType.STRING, online.getUniqueId().toString());
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        inv.setItem(53, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Spieler suchen")).build());
        player.openInventory(inv);
    }

    public void openPlayerRoles(Player player, java.util.UUID targetId) {
        var roles = permissionService.getPlayerRoles(targetId);
        String name = Bukkit.getOfflinePlayer(targetId).getName();
        Inventory inv = Bukkit.createInventory(new GuiHolders.PlayerRoleHolder(targetId), 27,
            Component.text("Rollen: " + (name != null ? name : targetId.toString())));
        String primaryRole = roles.primaryRole() != null ? roles.primaryRole() : "Keine";
        inv.setItem(10, new ItemBuilder(Material.BOOK).name(Text.mm("<yellow>Primary: <white>" + primaryRole)).build());
        inv.setItem(12, new ItemBuilder(Material.ANVIL).name(Text.mm("<green>Primary setzen")).build());
        inv.setItem(14, new ItemBuilder(Material.EMERALD).name(Text.mm("<green>Rolle hinzufügen")).build());
        inv.setItem(16, new ItemBuilder(Material.BARRIER).name(Text.mm("<red>Rolle entfernen")).build());
        inv.setItem(22, new ItemBuilder(Material.PAPER).name(Text.mm("<yellow>Node prüfen")).build());
        player.openInventory(inv);
    }

    public void openAuditLog(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.PermissionAuditHolder(), 54, Component.text("Audit Log"));
        List<String> entries = permissionService.auditLog().recent(50);
        int slot = 0;
        for (String line : entries) {
            if (slot >= 45) {
                break;
            }
            inv.setItem(slot++, new ItemBuilder(Material.PAPER).name(Text.mm("<gray>" + line)).build());
        }
        inv.setItem(45, new ItemBuilder(Material.ARROW).name(Text.mm("<yellow>Zurück")).build());
        player.openInventory(inv);
    }

    public void openShop(Player player, ShopDefinition shop) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.ShopHolder(shop.id()), 27, Component.text(shop.title()));
        for (ShopItem item : shop.items().values()) {
            Material material = Material.matchMaterial(item.material());
            if (material == null) {
                continue;
            }
            ItemBuilder builder = new ItemBuilder(material);
            if (item.name() != null && !item.name().isBlank()) {
                builder.name(net.kyori.adventure.text.Component.text(
                    org.bukkit.ChatColor.translateAlternateColorCodes('&', item.name())));
            }
            if (item.buyPrice() > 0) {
                builder.loreLine(Text.mm("<gray>Kaufen: <gold>" + item.buyPrice() + " Gold"));
            }
            if (item.sellPrice() > 0) {
                builder.loreLine(Text.mm("<gray>Verkaufen: <gold>" + item.sellPrice() + " Gold"));
            }
            inv.setItem(item.slot(), builder.build());
        }
        player.openInventory(inv);
    }

    private String resolveClassName(String classId) {
        if (classId == null) {
            return "Keine";
        }
        var definition = classManager.getClass(classId);
        return definition != null ? definition.name() : classId;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/SkillTreeGui.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/gui/SkillTreeGui.java`  
- Size: 4169 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.gui;

import com.example.rpg.RPGPlugin;
import com.example.rpg.manager.SkillTreeManager;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SkillTreeGui {
    private final RPGPlugin plugin;

    public SkillTreeGui(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(new GuiHolders.SkillTreeHolder(), 54, Component.text("Skillbaum"));
        SkillTreeManager treeManager = plugin.skillTreeManager();
        treeManager.rebuild();
        Map<String, Integer> slots = layout(treeManager);
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (Map.Entry<String, Integer> entry : slots.entrySet()) {
            Skill skill = plugin.skillManager().getSkill(entry.getKey());
            if (skill == null) {
                continue;
            }
            boolean learned = profile.learnedSkills().containsKey(skill.id());
            boolean unlocked = skill.requiredSkill() == null
                || profile.learnedSkills().containsKey(skill.requiredSkill());
            Material material = learned ? Material.ENCHANTED_BOOK : unlocked ? Material.BOOK : Material.BARRIER;
            ItemBuilder builder = new ItemBuilder(material)
                .name(Text.mm(learned ? "<green>" + skill.name() : unlocked ? "<yellow>" + skill.name() : "<red>" + skill.name()))
                .loreLine(Text.mm("<gray>Mana: <white>" + skill.manaCost()))
                .loreLine(Text.mm("<gray>Cooldown: <white>" + skill.cooldown() + "s"))
                .loreLine(Text.mm("<gray>Voraussetzung: <white>" + (skill.requiredSkill() == null ? "Keine" : skill.requiredSkill())));
            if (learned) {
                builder.loreLine(Text.mm("<green>Bereits gelernt"));
            } else if (unlocked) {
                builder.loreLine(Text.mm("<yellow>Klick zum Lernen"));
            } else {
                builder.loreLine(Text.mm("<red>Gesperrt"));
            }
            ItemStack item = builder.build();
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(plugin.skillKey(), PersistentDataType.STRING, skill.id());
            item.setItemMeta(meta);
            inv.setItem(entry.getValue(), item);
        }
        for (int slot : slots.values()) {
            int linkSlot = slot + 1;
            if (linkSlot < inv.getSize() && inv.getItem(linkSlot) == null) {
                inv.setItem(linkSlot, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(Component.text(" ")).build());
            }
        }
        player.openInventory(inv);
    }

    private Map<String, Integer> layout(SkillTreeManager treeManager) {
        Map<String, Integer> slots = new HashMap<>();
        int[] depthIndex = new int[6];
        ArrayDeque<SkillTreeManager.SkillNode> queue = new ArrayDeque<>(treeManager.roots());
        while (!queue.isEmpty()) {
            SkillTreeManager.SkillNode node = queue.poll();
            int depth = depth(node);
            int row = Math.min(depth, 5);
            int col = depthIndex[row]++;
            int slot = row * 9 + Math.min(col * 2, 8);
            slots.put(node.skill().id(), slot);
            for (SkillTreeManager.SkillNode child : node.children()) {
                queue.add(child);
            }
        }
        return slots;
    }

    private int depth(SkillTreeManager.SkillNode node) {
        int depth = 0;
        SkillTreeManager.SkillNode current = node;
        while (current.parent() != null) {
            depth++;
            current = current.parent();
        }
        return depth;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ArenaListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ArenaListener.java`  
- Size: 700 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ArenaListener implements Listener {
    private final RPGPlugin plugin;

    public ArenaListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);
        plugin.arenaManager().handleDeath(player);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/BehaviorEditorListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/BehaviorEditorListener.java`  
- Size: 4152 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.BehaviorTreeEditorGui;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class BehaviorEditorListener implements Listener {
    private final RPGPlugin plugin;
    private final BehaviorTreeEditorGui gui;

    public BehaviorEditorListener(RPGPlugin plugin, BehaviorTreeEditorGui gui) {
        this.plugin = plugin;
        this.gui = gui;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!(event.getInventory().getHolder() instanceof GuiHolders.BehaviorTreeEditorHolder holder)) {
            return;
        }
        event.setCancelled(true);
        String treeName = holder.treeName();
        switch (event.getSlot()) {
            case 10 -> promptEmergency(player, treeName);
            case 12 -> promptRanged(player, treeName);
            case 14 -> {
                plugin.behaviorTreeManager().addTemplate(treeName, Map.of("type", "melee_attack"));
                player.sendMessage(Text.mm("<green>Nahkampf hinzugefügt."));
                gui.open(player, treeName);
            }
            case 16 -> {
                plugin.behaviorTreeManager().resetTree(treeName);
                player.sendMessage(Text.mm("<yellow>Behavior Tree zurückgesetzt."));
                gui.open(player, treeName);
            }
            default -> {
            }
        }
    }

    private void promptEmergency(Player player, String treeName) {
        plugin.promptManager().prompt(player, Text.mm("<gray>Notfall-Heilung: <threshold> <skillId> <healAmount>"),
            input -> {
                String[] parts = input.split("\\s+");
                if (parts.length < 3) {
                    player.sendMessage(Text.mm("<red>Format: <threshold> <skillId> <healAmount>"));
                    return;
                }
                double threshold = parseDouble(parts[0], 0.2);
                String skillId = parts[1];
                double heal = parseDouble(parts[2], 6);
                List<Map<String, Object>> children = new ArrayList<>();
                children.add(Map.of("type", "health_below", "threshold", threshold));
                children.add(Map.of("type", "cast_skill", "skill", skillId));
                children.add(Map.of("type", "heal_self", "amount", heal));
                plugin.behaviorTreeManager().addTemplate(treeName, Map.of("type", "sequence", "children", children));
                player.sendMessage(Text.mm("<green>Notfall-Sequenz hinzugefügt."));
                gui.open(player, treeName);
            });
    }

    private void promptRanged(Player player, String treeName) {
        plugin.promptManager().prompt(player, Text.mm("<gray>Fernkampf: <distance> <skillId>"), input -> {
            String[] parts = input.split("\\s+");
            if (parts.length < 2) {
                player.sendMessage(Text.mm("<red>Format: <distance> <skillId>"));
                return;
            }
            double distance = parseDouble(parts[0], 10);
            String skillId = parts[1];
            List<Map<String, Object>> children = new ArrayList<>();
            children.add(Map.of("type", "target_distance_above", "distance", distance));
            children.add(Map.of("type", "cast_skill", "skill", skillId));
            plugin.behaviorTreeManager().addTemplate(treeName, Map.of("type", "sequence", "children", children));
            player.sendMessage(Text.mm("<green>Fernkampf-Sequenz hinzugefügt."));
            gui.open(player, treeName);
        });
    }

    private double parseDouble(String input, double fallback) {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/BuildingPlacementListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/BuildingPlacementListener.java`  
- Size: 1134 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class BuildingPlacementListener implements Listener {
    private final RPGPlugin plugin;

    public BuildingPlacementListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!plugin.permissionService().has(event.getPlayer(), "rpg.admin")) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        Location target = event.getClickedBlock().getLocation().add(0, 1, 0);
        if (plugin.buildingManager().handlePlacement(event.getPlayer(), target)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Text.mm("<gray>Platziere Gebäude..."));
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/CombatListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/CombatListener.java`  
- Size: 5054 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public CombatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        plugin.partyManager().getParty(damager.getUniqueId()).ifPresent(party -> {
            if (party.members().contains(target.getUniqueId())) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        int xp = 10 + event.getEntity().getType().ordinal() % 10;
        var partyOpt = plugin.partyManager().getParty(killer.getUniqueId());
        java.util.List<Player> recipients = new java.util.ArrayList<>();
        if (partyOpt.isPresent()) {
            for (java.util.UUID memberId : partyOpt.get().members()) {
                Player member = plugin.getServer().getPlayer(memberId);
                if (member != null && member.getWorld().equals(killer.getWorld())
                    && member.getLocation().distanceSquared(killer.getLocation()) <= 30 * 30) {
                    recipients.add(member);
                }
            }
        } else {
            recipients.add(killer);
        }
        boolean split = plugin.getConfig().getBoolean("rpg.party.xpSplit", true);
        int share = split ? Math.max(1, xp / Math.max(1, recipients.size())) : xp;
        for (Player member : recipients) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            profile.addXp(share);
            profile.applyAttributes(member);
        }

        LootTable table = plugin.lootManager().getTableFor(event.getEntity().getType().name());
        if (table != null) {
            for (LootEntry entry : table.entries()) {
                if (random.nextDouble() <= entry.chance()) {
                    Material material = Material.matchMaterial(entry.material());
                    if (material != null) {
                        int level = plugin.playerDataManager().getProfile(killer).level();
                        ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), level);
                        item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                        event.getDrops().add(item);
                        plugin.broadcastLoot(killer, item);
                    }
                }
            }
        }

        for (Player member : recipients) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            for (QuestProgress progress : profile.activeQuests().values()) {
                Quest quest = plugin.questManager().getQuest(progress.questId());
                if (quest == null) {
                    continue;
                }
                for (int i = 0; i < quest.steps().size(); i++) {
                    QuestStep step = quest.steps().get(i);
                    if (step.type() == QuestStepType.KILL && step.target().equalsIgnoreCase(event.getEntity().getType().name())) {
                        progress.incrementStepClamped(i, 1, step.amount());
                    }
                }
                plugin.completeQuestIfReady(member, quest, progress);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.addXp(1);
        profile.applyAttributes(player);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.addXp(2);
        profile.applyAttributes(player);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/CustomMobListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/CustomMobListener.java`  
- Size: 9651 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.behavior.BehaviorContext;
import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.PlayerProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class CustomMobListener implements Listener {
    private final RPGPlugin plugin;
    private final NamespacedKey mobKey;
    private final Random random = new Random();
    private final Map<UUID, BukkitTask> behaviorTasks = new HashMap<>();
    private final Map<UUID, BehaviorContext> behaviorContexts = new HashMap<>();
    private final Map<UUID, TextDisplay> healthBars = new HashMap<>();

    public CustomMobListener(RPGPlugin plugin) {
        this.plugin = plugin;
        this.mobKey = new NamespacedKey(plugin, "custom_mob_id");
    }

    public NamespacedKey mobKey() {
        return mobKey;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob != null) {
            event.setDamage(mob.damage());
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        String mobId = getMobId(entity);
        if (mobId == null) {
            return;
        }
        removeHealthBar(entity);
        BukkitTask task = behaviorTasks.remove(entity.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        behaviorContexts.remove(entity.getUniqueId());
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer != null) {
            PlayerProfile profile = plugin.playerDataManager().getProfile(killer);
            profile.addXp(mob.xp());
            profile.applyAttributes(killer);
        }
        if (mob.lootTable() != null) {
            LootTable table = plugin.lootManager().getTable(mob.lootTable());
            if (table != null) {
                for (LootEntry entry : table.entries()) {
                    if (random.nextDouble() <= entry.chance()) {
                        Material material = Material.matchMaterial(entry.material());
                        if (material != null) {
                            ItemStack item = plugin.itemGenerator().createRpgItem(material, entry.rarity(), 1);
                            item.setAmount(entry.minAmount() + random.nextInt(Math.max(1, entry.maxAmount() - entry.minAmount() + 1)));
                            event.getDrops().add(item);
                            if (killer != null) {
                                plugin.broadcastLoot(killer, item);
                            }
                        }
                    }
                }
            }
        }
    }

    public void applyDefinition(LivingEntity entity, MobDefinition mob) {
        String name = mob.name();
        entity.customName(null);
        entity.setCustomNameVisible(false);
        if (entity.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(mob.health());
        }
        entity.setHealth(mob.health());
        entity.getPersistentDataContainer().set(mobKey, PersistentDataType.STRING, mob.id());
        if (mob.mainHand() != null) {
            Material material = Material.matchMaterial(mob.mainHand());
            if (material != null) {
                entity.getEquipment().setItemInMainHand(new ItemStack(material));
            }
        }
        if (mob.helmet() != null) {
            Material material = Material.matchMaterial(mob.helmet());
            if (material != null) {
                entity.getEquipment().setHelmet(new ItemStack(material));
            }
        }
        attachHealthBar(entity, mob, entity.getHealth());
        startBehaviorLoop(entity, mob);
    }

    private String getMobId(LivingEntity entity) {
        if (!entity.getPersistentDataContainer().has(mobKey, PersistentDataType.STRING)) {
            return null;
        }
        return entity.getPersistentDataContainer().get(mobKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onMobDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        double nextHealth = Math.max(0, living.getHealth() - event.getFinalDamage());
        updateHealthBar(living, mob, nextHealth);
    }

    @EventHandler
    public void onMobHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        String mobId = getMobId(living);
        if (mobId == null) {
            return;
        }
        MobDefinition mob = plugin.mobManager().getMob(mobId);
        if (mob == null) {
            return;
        }
        double maxHealth = mob.health();
        double nextHealth = Math.min(maxHealth, living.getHealth() + event.getAmount());
        updateHealthBar(living, mob, nextHealth);
    }

    private void startBehaviorLoop(LivingEntity entity, MobDefinition mob) {
        BehaviorNode root = plugin.behaviorTreeManager().getTree(mob.behaviorTree());
        BehaviorContext context = new BehaviorContext(plugin, entity, mob);
        behaviorContexts.put(entity.getUniqueId(), context);
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (entity.isDead() || !entity.isValid()) {
                BukkitTask running = behaviorTasks.remove(entity.getUniqueId());
                if (running != null) {
                    running.cancel();
                }
                behaviorContexts.remove(entity.getUniqueId());
                return;
            }
            Player target = findTarget(entity);
            context.setTarget(target);
            if (target == null) {
                return;
            }
            root.tick(context);
        }, 1L, 1L);
        behaviorTasks.put(entity.getUniqueId(), task);
    }

    private Player findTarget(LivingEntity entity) {
        return entity.getWorld().getPlayers().stream()
            .filter(player -> player.getLocation().distanceSquared(entity.getLocation()) <= 400)
            .min((a, b) -> Double.compare(a.getLocation().distanceSquared(entity.getLocation()),
                b.getLocation().distanceSquared(entity.getLocation())))
            .orElse(null);
    }

    private void attachHealthBar(LivingEntity entity, MobDefinition mob, double health) {
        removeHealthBar(entity);
        TextDisplay display = entity.getWorld().spawn(entity.getLocation().add(0, 1.6, 0), TextDisplay.class);
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);
        display.text(net.kyori.adventure.text.Component.text(buildHealthText(mob, health)));
        entity.addPassenger(display);
        healthBars.put(entity.getUniqueId(), display);
    }

    private void updateHealthBar(LivingEntity entity, MobDefinition mob, double health) {
        TextDisplay display = healthBars.get(entity.getUniqueId());
        if (display == null || display.isDead()) {
            attachHealthBar(entity, mob, health);
            return;
        }
        display.text(net.kyori.adventure.text.Component.text(buildHealthText(mob, health)));
    }

    private void removeHealthBar(LivingEntity entity) {
        TextDisplay display = healthBars.remove(entity.getUniqueId());
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    private String buildHealthText(MobDefinition mob, double health) {
        double maxHealth = Math.max(1, mob.health());
        int bars = 10;
        int filled = (int) Math.round((health / maxHealth) * bars);
        filled = Math.min(bars, Math.max(0, filled));
        int empty = bars - filled;
        StringBuilder bar = new StringBuilder();
        bar.append("§7[§a");
        bar.append("|".repeat(filled));
        bar.append("§c");
        bar.append("|".repeat(empty));
        bar.append("§7]");
        return mob.name() + " " + bar + " §f" + Math.round(health) + "/" + Math.round(maxHealth) + " HP";
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/DamageIndicatorListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/DamageIndicatorListener.java`  
- Size: 2751 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class DamageIndicatorListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public DamageIndicatorListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getFinalDamage() <= 0) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof TextDisplay) {
            return;
        }
        NamedTextColor color = isMagicDamage(event.getCause()) ? NamedTextColor.AQUA : NamedTextColor.RED;
        String text = "-" + Math.round(event.getFinalDamage()) + " ❤";
        spawnIndicator(entity.getLocation(), text, color);
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (event.getAmount() <= 0) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof TextDisplay) {
            return;
        }
        String text = "+" + Math.round(event.getAmount()) + " ❤";
        spawnIndicator(entity.getLocation(), text, NamedTextColor.GREEN);
    }

    private boolean isMagicDamage(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case MAGIC, DRAGON_BREATH, WITHER, POISON -> true;
            default -> false;
        };
    }

    private void spawnIndicator(Location base, String text, NamedTextColor color) {
        Location location = base.clone().add(offset(), 1.2 + offset(), offset());
        TextDisplay display = base.getWorld().spawn(location, TextDisplay.class);
        display.text(Component.text(text, color));
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (display.isDead()) {
                task.cancel();
                return;
            }
            display.teleport(display.getLocation().add(0, 0.04, 0));
        }, 0L, 1L);

        plugin.getServer().getScheduler().runTaskLater(plugin, display::remove, 20L);
    }

    private double offset() {
        return (random.nextDouble() - 0.5) * 0.6;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/GuiListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/GuiListener.java`  
- Size: 21750 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.gui.GuiHolders;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.inventory.Inventory;
import org.bukkit.Sound;
import org.bukkit.Material;

public class GuiListener implements Listener {
    private final RPGPlugin plugin;

    public GuiListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null) {
            return;
        }
        var holder = event.getInventory().getHolder();
        if (holder instanceof GuiHolders.PlayerMenuHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 12 -> plugin.guiManager().openSkillList(player);
                case 14 -> plugin.guiManager().openQuestList(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.AdminMenuHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 15) {
                boolean enabled = plugin.toggleDebug(player.getUniqueId());
                player.sendMessage(Text.mm(enabled ? "<green>Debug aktiviert." : "<red>Debug deaktiviert."));
            } else if (event.getSlot() == 16) {
                plugin.guiManager().openBuildingCategories(player);
            } else if (event.getSlot() == 17) {
                plugin.guiManager().openPermissionsMain(player);
            }
            return;
        }
        if (holder instanceof GuiHolders.BuildingCategoryHolder) {
            event.setCancelled(true);
            String category = resolveBuildingCategory(current);
            if (category == null) {
                return;
            }
            if ("SINGLE".equalsIgnoreCase(category)) {
                player.closeInventory();
                plugin.promptManager().prompt(player, Text.mm("<yellow>Schematic-Dateiname eingeben (z.B. haus.schem):"), input -> {
                    plugin.buildingManager().beginSingleSchematicPlacement(player, input, com.example.rpg.schematic.Transform.Rotation.NONE);
                });
                return;
            }
            plugin.guiManager().openBuildingList(player, com.example.rpg.model.BuildingCategory.fromString(category));
            return;
        }
        if (holder instanceof GuiHolders.BuildingListHolder) {
            event.setCancelled(true);
            String buildingId = resolveBuilding(current);
            if (buildingId == null) {
                return;
            }
            plugin.buildingManager().beginPlacement(player, buildingId, com.example.rpg.schematic.Transform.Rotation.NONE);
            player.closeInventory();
            return;
        }
        if (holder instanceof GuiHolders.QuestListHolder) {
            event.setCancelled(true);
            Quest quest = resolveQuest(current);
            if (quest == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.level() < quest.minLevel()) {
                player.sendMessage(Text.mm("<red>Du brauchst Level " + quest.minLevel() + "."));
                return;
            }
            if (profile.activeQuests().containsKey(quest.id())) {
                player.sendMessage(Text.mm("<yellow>Quest bereits aktiv."));
                return;
            }
            if (profile.completedQuests().contains(quest.id()) && !quest.repeatable()) {
                player.sendMessage(Text.mm("<red>Quest bereits abgeschlossen."));
                return;
            }
            profile.activeQuests().put(quest.id(), new QuestProgress(quest.id()));
            player.sendMessage(Text.mm("<green>Quest angenommen: " + quest.name()));
            return;
        }
        if (holder instanceof GuiHolders.SkillListHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            profile.learnedSkills().put(skill.id(), profile.learnedSkills().getOrDefault(skill.id(), 0) + 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.guiManager().openSkillList(player);
            return;
        }
        if (holder instanceof GuiHolders.SkillTreeHolder) {
            event.setCancelled(true);
            Skill skill = resolveSkill(current);
            if (skill == null) {
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            if (profile.skillPoints() <= 0) {
                player.sendMessage(Text.mm("<red>Keine Skillpunkte."));
                return;
            }
            if (skill.requiredSkill() != null && !profile.learnedSkills().containsKey(skill.requiredSkill())) {
                player.sendMessage(Text.mm("<red>Du musst zuerst " + skill.requiredSkill() + " lernen."));
                return;
            }
            if (profile.learnedSkills().containsKey(skill.id())) {
                player.sendMessage(Text.mm("<yellow>Bereits gelernt."));
                return;
            }
            profile.learnedSkills().put(skill.id(), 1);
            profile.setSkillPoints(profile.skillPoints() - 1);
            player.sendMessage(Text.mm("<green>Skill gelernt: " + skill.name()));
            plugin.skillTreeGui().open(player);
            return;
        }
        if (holder instanceof GuiHolders.ShopHolder shopHolder) {
            event.setCancelled(true);
            handleShopClick(player, event.getInventory(), event.getSlot(), current, shopHolder, event.isRightClick());
        }
        if (holder instanceof GuiHolders.SchematicMoveHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> plugin.buildingManager().moveLastPlacement(player, -1, 0, 0);
                case 15 -> plugin.buildingManager().moveLastPlacement(player, 1, 0, 0);
                case 13 -> plugin.buildingManager().moveLastPlacement(player, 0, 1, 0);
                case 22 -> plugin.buildingManager().moveLastPlacement(player, 0, -1, 0);
                case 26 -> player.closeInventory();
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.PermissionsMainHolder) {
            event.setCancelled(true);
            switch (event.getSlot()) {
                case 11 -> plugin.guiManager().openRoleList(player, 0);
                case 13 -> plugin.guiManager().openPlayerList(player);
                case 15 -> plugin.guiManager().openAuditLog(player);
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleListHolder roleListHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle erstellen: <key> <displayName>"), input -> {
                    String[] parts = input.split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <key> <displayName>"));
                        return;
                    }
                    plugin.permissionService().createRole(player, parts[0], parts[1]);
                    plugin.guiManager().openRoleList(player, roleListHolder.page());
                });
                return;
            }
            String roleKey = resolveRoleKey(current);
            if (roleKey != null) {
                plugin.guiManager().openRoleDetails(player, roleKey);
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleDetailHolder roleDetailHolder) {
            event.setCancelled(true);
            String roleKey = roleDetailHolder.roleKey();
            switch (event.getSlot()) {
                case 11 -> plugin.guiManager().openRoleParents(player, roleKey);
                case 13 -> plugin.guiManager().openRoleNodes(player, roleKey, 0);
                case 15 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Neuer Display-Name für " + roleKey + ":"), input -> {
                    if (input.isBlank()) {
                        player.sendMessage(Text.mm("<red>Display-Name darf nicht leer sein."));
                        return;
                    }
                    plugin.permissionService().renameRole(player, roleKey, input.trim());
                    plugin.guiManager().openRoleDetails(player, roleKey);
                });
                case 26 -> {
                    plugin.permissionService().deleteRole(player, roleKey);
                    plugin.guiManager().openRoleList(player, 0);
                }
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.RoleParentsHolder roleParentsHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openRoleDetails(player, roleParentsHolder.roleKey());
                return;
            }
            String parentKey = resolveNodeKey(current);
            if (parentKey == null) {
                return;
            }
            String roleKey = roleParentsHolder.roleKey();
            var role = plugin.permissionService().roles().get(roleKey);
            if (role == null) {
                return;
            }
            if (role.parents().contains(parentKey)) {
                plugin.permissionService().removeParent(player, roleKey, parentKey);
            } else {
                plugin.permissionService().addParent(player, roleKey, parentKey);
            }
            plugin.guiManager().openRoleParents(player, roleKey);
            return;
        }
        if (holder instanceof GuiHolders.RoleNodesHolder roleNodesHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openRoleDetails(player, roleNodesHolder.roleKey());
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Node setzen: <node> <allow|deny|inherit>"), input -> {
                    String[] parts = input.split("\\s+", 2);
                    if (parts.length < 2) {
                        player.sendMessage(Text.mm("<red>Format: <node> <allow|deny|inherit>"));
                        return;
                    }
                    var decision = parseDecision(parts[1]);
                    plugin.permissionService().setRoleNode(player, roleNodesHolder.roleKey(), parts[0], decision);
                    plugin.guiManager().openRoleNodes(player, roleNodesHolder.roleKey(), roleNodesHolder.page());
                });
                return;
            }
            String node = resolveNodeKey(current);
            if (node == null) {
                return;
            }
            var role = plugin.permissionService().roles().get(roleNodesHolder.roleKey());
            if (role == null) {
                return;
            }
            var currentDecision = role.nodes().getOrDefault(node, com.example.rpg.permissions.PermissionDecision.INHERIT);
            var nextDecision = switch (currentDecision) {
                case INHERIT -> com.example.rpg.permissions.PermissionDecision.ALLOW;
                case ALLOW -> com.example.rpg.permissions.PermissionDecision.DENY;
                case DENY -> com.example.rpg.permissions.PermissionDecision.INHERIT;
            };
            plugin.permissionService().setRoleNode(player, roleNodesHolder.roleKey(), node, nextDecision);
            plugin.guiManager().openRoleNodes(player, roleNodesHolder.roleKey(), roleNodesHolder.page());
            return;
        }
        if (holder instanceof GuiHolders.PlayerListHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
                return;
            }
            if (event.getSlot() == 53) {
                plugin.promptManager().prompt(player, Text.mm("<yellow>Spielername eingeben:"), input -> {
                    var target = plugin.getServer().getOfflinePlayer(input);
                    if (target == null) {
                        player.sendMessage(Text.mm("<red>Spieler nicht gefunden."));
                        return;
                    }
                    plugin.guiManager().openPlayerRoles(player, target.getUniqueId());
                });
                return;
            }
            UUID targetId = resolvePlayerId(current);
            if (targetId != null) {
                plugin.guiManager().openPlayerRoles(player, targetId);
            }
            return;
        }
        if (holder instanceof GuiHolders.PlayerRoleHolder playerRoleHolder) {
            event.setCancelled(true);
            UUID targetId = playerRoleHolder.targetId();
            switch (event.getSlot()) {
                case 12 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Primary Rolle setzen:"), input -> {
                    plugin.permissionService().assignPrimary(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 14 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle hinzufügen:"), input -> {
                    plugin.permissionService().addRole(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 16 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Rolle entfernen:"), input -> {
                    plugin.permissionService().removeRole(player, targetId, input.trim());
                    plugin.guiManager().openPlayerRoles(player, targetId);
                });
                case 22 -> plugin.promptManager().prompt(player, Text.mm("<yellow>Node prüfen:"), input -> {
                    var explain = plugin.permissionService().explain(targetId, input.trim());
                    player.sendMessage(Text.mm("<yellow>Ergebnis: " + (explain.allowed() ? "ALLOW" : "DENY")));
                    if (explain.winningRole() != null) {
                        player.sendMessage(Text.mm("<gray>Role: " + explain.winningRole() + " Node: " + explain.winningNode()));
                    }
                });
                default -> {
                }
            }
            return;
        }
        if (holder instanceof GuiHolders.PermissionAuditHolder) {
            event.setCancelled(true);
            if (event.getSlot() == 45) {
                plugin.guiManager().openPermissionsMain(player);
            }
        }
    }

    private Quest resolveQuest(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String questId = meta.getPersistentDataContainer().get(plugin.questKey(), PersistentDataType.STRING);
        if (questId == null) {
            return null;
        }
        return plugin.questManager().getQuest(questId);
    }

    private Skill resolveSkill(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String skillId = meta.getPersistentDataContainer().get(plugin.skillKey(), PersistentDataType.STRING);
        if (skillId == null) {
            return null;
        }
        return plugin.skillManager().getSkill(skillId);
    }

    private String resolveBuilding(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.buildingKey(), PersistentDataType.STRING);
    }

    private String resolveBuildingCategory(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.buildingCategoryKey(), PersistentDataType.STRING);
    }

    private String resolveRoleKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.permRoleKey(), PersistentDataType.STRING);
    }

    private String resolveNodeKey(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(plugin.permNodeKey(), PersistentDataType.STRING);
    }

    private UUID resolvePlayerId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String value = meta.getPersistentDataContainer().get(plugin.permPlayerKey(), PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    private com.example.rpg.permissions.PermissionDecision parseDecision(String value) {
        return switch (value.toLowerCase()) {
            case "allow" -> com.example.rpg.permissions.PermissionDecision.ALLOW;
            case "deny" -> com.example.rpg.permissions.PermissionDecision.DENY;
            default -> com.example.rpg.permissions.PermissionDecision.INHERIT;
        };
    }

    private void handleShopClick(Player player, Inventory inventory, int slot, ItemStack clicked,
                                 GuiHolders.ShopHolder holder, boolean rightClick) {
        var shop = plugin.shopManager().getShop(holder.shopId());
        if (shop == null) {
            player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
            return;
        }
        var shopItem = shop.items().get(slot);
        if (shopItem == null) {
            return;
        }
        var profile = plugin.playerDataManager().getProfile(player);
        Material material = Material.matchMaterial(shopItem.material());
        if (material == null) {
            player.sendMessage(Text.mm("<red>Item ungültig."));
            return;
        }
        if (rightClick) {
            int sellPrice = shopItem.sellPrice();
            if (sellPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht verkauft werden."));
                return;
            }
            if (!player.getInventory().contains(material)) {
                player.sendMessage(Text.mm("<red>Du hast dieses Item nicht."));
                return;
            }
            removeOne(player.getInventory(), material);
            profile.setGold(profile.gold() + sellPrice);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Verkauft für <gold>" + sellPrice + "</gold> Gold."));
        } else {
            int buyPrice = shopItem.buyPrice();
            if (buyPrice <= 0) {
                player.sendMessage(Text.mm("<red>Dieses Item kann nicht gekauft werden."));
                return;
            }
            if (profile.gold() < buyPrice) {
                player.sendMessage(Text.mm("<red>Nicht genug Gold."));
                return;
            }
            profile.setGold(profile.gold() - buyPrice);
            player.getInventory().addItem(new ItemStack(material));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
            player.sendMessage(Text.mm("<green>Gekauft für <gold>" + buyPrice + "</gold> Gold."));
        }
        player.updateInventory();
        plugin.playerDataManager().saveProfile(profile);
    }

    private void removeOne(Inventory inventory, Material material) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack == null || stack.getType() != material) {
                continue;
            }
            if (stack.getAmount() > 1) {
                stack.setAmount(stack.getAmount() - 1);
            } else {
                inventory.setItem(i, null);
            }
            return;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ItemStatListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ItemStatListener.java`  
- Size: 890 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ItemStatListener implements Listener {
    private final RPGPlugin plugin;

    public ItemStatListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.itemStatManager().updateSetBonus(player));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.itemStatManager().updateSetBonus(event.getPlayer());
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/NpcListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/NpcListener.java`  
- Size: 2022 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.util.Text;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class NpcListener implements Listener {
    private final RPGPlugin plugin;

    public NpcListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING)) {
            return;
        }
        String npcId = entity.getPersistentDataContainer().get(plugin.npcManager().npcKey(), PersistentDataType.STRING);
        if (npcId == null) {
            return;
        }
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!npc.dialog().isEmpty()) {
            player.sendMessage(Text.mm("<gold>" + npc.name() + ":"));
            for (String line : npc.dialog()) {
                player.sendMessage(Text.mm("<gray>" + line));
            }
        }
        if (npc.role() == NpcRole.QUESTGIVER && npc.questLink() != null) {
            player.sendMessage(Text.mm("<yellow>Quest verfügbar: <white>" + npc.questLink()));
            plugin.guiManager().openQuestList(player);
        }
        if (npc.role() == NpcRole.VENDOR && npc.shopId() != null) {
            var shop = plugin.shopManager().getShop(npc.shopId());
            if (shop == null) {
                player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
                return;
            }
            plugin.guiManager().openShop(player, shop);
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/NpcProtectionListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/NpcProtectionListener.java`  
- Size: 1270 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Schutz: NPCs sollen nicht beschädigt, nicht getargetet und nicht "interaktiv kaputt" gemacht werden.
 * (Ohne externe Plugins.)
 */
public class NpcProtectionListener implements Listener {
    private final RPGPlugin plugin;

    public NpcProtectionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isNpc(Entity entity) {
        return entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (isNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && isNpc(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    // Intentionally no PlayerInteractAtEntityEvent cancel to avoid breaking normal right-click.
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/PlayerListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/PlayerListener.java`  
- Size: 4573 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, Float> walkSpeed = new HashMap<>();
    private final Map<UUID, Float> flySpeed = new HashMap<>();

    public PlayerListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        freeze(player);
        plugin.playerDataManager().loadProfileAsync(player.getUniqueId()).whenComplete((profile, error) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                PlayerProfile resolved = profile != null ? profile : plugin.playerDataManager().getProfile(player);
                resolved.applyAttributes(player);
                unfreeze(player);
            });
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.promptManager().handle(player, event.getMessage())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onWandUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!plugin.permissionService().has(player, "rpg.editor")) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.STICK) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(plugin.wandKey(), PersistentDataType.BYTE)) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos1 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos1"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            player.sendMessage(Text.mm("<green>Pos2 gesetzt: " + event.getClickedBlock().getLocation().toVector()));
            player.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey(plugin, "pos2"),
                org.bukkit.persistence.PersistentDataType.STRING,
                serializeLocation(event.getClickedBlock().getLocation())
            );
        }
    }

    private String serializeLocation(org.bukkit.Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private void freeze(Player player) {
        walkSpeed.put(player.getUniqueId(), player.getWalkSpeed());
        flySpeed.put(player.getUniqueId(), player.getFlySpeed());
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
        player.setInvulnerable(true);
        player.setCollidable(false);
    }

    private void unfreeze(Player player) {
        Float walk = walkSpeed.remove(player.getUniqueId());
        Float fly = flySpeed.remove(player.getUniqueId());
        player.setWalkSpeed(walk != null ? walk : 0.2f);
        player.setFlySpeed(fly != null ? fly : 0.1f);
        player.setInvulnerable(false);
        player.setCollidable(true);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ProfessionListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ProfessionListener.java`  
- Size: 2388 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

public class ProfessionListener implements Listener {
    private final RPGPlugin plugin;

    public ProfessionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Material material = event.getBlock().getType();
        String materialKey = material.name();
        PlayerProfile profile = plugin.playerDataManager().getProfile(event.getPlayer());
        int miningXp = plugin.professionManager().xpForMaterial("mining", materialKey);
        int herbalismXp = plugin.professionManager().xpForMaterial("herbalism", materialKey);
        if (miningXp > 0) {
            plugin.professionManager().addXp(profile, "mining", miningXp, event.getPlayer());
        }
        if (herbalismXp > 0) {
            plugin.professionManager().addXp(profile, "herbalism", herbalismXp, event.getPlayer());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType().isAir()) {
            return;
        }
        String materialKey = result.getType().name();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        int required = plugin.professionManager().requiredLevelForCraft("blacksmithing", materialKey);
        if (required > 0 && plugin.professionManager().getLevel(profile, "blacksmithing") < required) {
            event.setCancelled(true);
            player.sendMessage(Text.mm("<red>Benötigtes Schmiede-Level: " + required));
            return;
        }
        int xp = plugin.professionManager().xpForMaterial("blacksmithing", materialKey);
        if (xp > 0) {
            plugin.professionManager().addXp(profile, "blacksmithing", xp, player);
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/SkillHotbarListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/SkillHotbarListener.java`  
- Size: 1156 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class SkillHotbarListener implements Listener {
    private final RPGPlugin plugin;

    public SkillHotbarListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        var player = event.getPlayer();
        int slot = player.getInventory().getHeldItemSlot() + 1;
        var profile = plugin.playerDataManager().getProfile(player);
        String skillId = plugin.skillHotbarManager().getBinding(profile, slot);
        if (skillId == null || skillId.isBlank()) {
            return;
        }
        plugin.useSkill(player, skillId);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ZoneListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/listener/ZoneListener.java`  
- Size: 2561 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestProgress;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Zone;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ZoneListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, String> lastZone = new HashMap<>();

    public ZoneListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Zone zone = plugin.zoneManager().getZoneAt(event.getTo());
        String zoneId = zone != null ? zone.id() : null;
        String previous = lastZone.get(player.getUniqueId());
        if ((zoneId == null && previous != null) || (zoneId != null && !zoneId.equals(previous))) {
            lastZone.put(player.getUniqueId(), zoneId);
            if (zone != null) {
                player.sendMessage(ChatColor.AQUA + "Zone betreten: " + zone.name());
                if (zone.slowMultiplier() < 1.0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 0));
                }
                handleExploreQuests(player, zone);
            } else {
                player.sendMessage(ChatColor.GRAY + "Zone verlassen.");
            }
        }
    }

    private void handleExploreQuests(Player player, Zone zone) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        for (QuestProgress progress : profile.activeQuests().values()) {
            Quest quest = plugin.questManager().getQuest(progress.questId());
            if (quest == null) {
                continue;
            }
            for (int i = 0; i < quest.steps().size(); i++) {
                QuestStep step = quest.steps().get(i);
                if (step.type() == QuestStepType.EXPLORE && step.target().equalsIgnoreCase(zone.id())) {
                    progress.incrementStepClamped(i, 1, step.amount());
                }
            }
            plugin.completeQuestIfReady(player, quest, progress);
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ArenaManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ArenaManager.java`  
- Size: 7629 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Arena;
import com.example.rpg.model.ArenaStatus;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.EloCalculator;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ArenaManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<UUID, Arena> arenaByPlayer = new HashMap<>();
    private final Queue<UUID> queue = new ArrayDeque<>();

    public ArenaManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "arenas.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public void joinQueue(Player player) {
        if (arenaByPlayer.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Arena."));
            return;
        }
        if (queue.contains(player.getUniqueId())) {
            player.sendMessage(Text.mm("<yellow>Du bist bereits in der Warteschlange."));
            return;
        }
        queue.add(player.getUniqueId());
        player.sendMessage(Text.mm("<green>Du bist der PvP-Warteschlange beigetreten."));
        tryStartMatch();
    }

    public void removeFromQueue(Player player) {
        queue.remove(player.getUniqueId());
    }

    public Optional<Arena> arenaFor(Player player) {
        return Optional.ofNullable(arenaByPlayer.get(player.getUniqueId()));
    }

    public void handleDeath(Player loser) {
        Arena arena = arenaByPlayer.get(loser.getUniqueId());
        if (arena == null) {
            return;
        }
        Player winner = plugin.getServer().getPlayer(other(arena, loser.getUniqueId()));
        endMatch(arena, winner, loser);
    }

    public List<PlayerProfile> topPlayers(int limit) {
        List<PlayerProfile> profiles = new ArrayList<>(plugin.playerDataManager().profiles().values());
        profiles.sort(Comparator.comparingInt(PlayerProfile::elo).reversed());
        return profiles.subList(0, Math.min(limit, profiles.size()));
    }

    private void tryStartMatch() {
        if (queue.size() < 2) {
            return;
        }
        Arena arena = arenas.values().stream()
            .filter(a -> a.status() == ArenaStatus.WAITING)
            .findFirst()
            .orElse(null);
        if (arena == null) {
            return;
        }
        UUID playerOne = queue.poll();
        UUID playerTwo = queue.poll();
        Player p1 = plugin.getServer().getPlayer(playerOne);
        Player p2 = plugin.getServer().getPlayer(playerTwo);
        if (p1 == null || p2 == null) {
            if (p1 != null) {
                queue.add(p1.getUniqueId());
            }
            if (p2 != null) {
                queue.add(p2.getUniqueId());
            }
            return;
        }
        arena.setPlayerOne(playerOne);
        arena.setPlayerTwo(playerTwo);
        arena.setStatus(ArenaStatus.FIGHTING);
        arenaByPlayer.put(playerOne, arena);
        arenaByPlayer.put(playerTwo, arena);
        teleportPlayers(arena, p1, p2);
        p1.sendMessage(Text.mm("<gold>PvP-Kampf gestartet!"));
        p2.sendMessage(Text.mm("<gold>PvP-Kampf gestartet!"));
    }

    private void teleportPlayers(Arena arena, Player p1, Player p2) {
        World world = plugin.getServer().getWorld(arena.world());
        if (world == null) {
            return;
        }
        p1.teleport(new Location(world, arena.spawn1x() + 0.5, arena.spawn1y(), arena.spawn1z() + 0.5));
        p2.teleport(new Location(world, arena.spawn2x() + 0.5, arena.spawn2y(), arena.spawn2z() + 0.5));
    }

    private void endMatch(Arena arena, Player winner, Player loser) {
        arena.setStatus(ArenaStatus.ENDING);
        if (winner != null && loser != null) {
            PlayerProfile winnerProfile = plugin.playerDataManager().getProfile(winner);
            PlayerProfile loserProfile = plugin.playerDataManager().getProfile(loser);
            int winnerNew = EloCalculator.calculateNewRating(winnerProfile.elo(), loserProfile.elo(), 1.0, 32);
            int loserNew = EloCalculator.calculateNewRating(loserProfile.elo(), winnerProfile.elo(), 0.0, 32);
            winnerProfile.setElo(winnerNew);
            loserProfile.setElo(loserNew);
            winner.sendMessage(Text.mm("<green>Du hast gewonnen! Neuer ELO: " + winnerNew));
            loser.sendMessage(Text.mm("<red>Du hast verloren! Neuer ELO: " + loserNew));
            plugin.playerDataManager().saveProfile(winnerProfile);
            plugin.playerDataManager().saveProfile(loserProfile);
        }
        arenaByPlayer.remove(arena.playerOne());
        arenaByPlayer.remove(arena.playerTwo());
        arena.setPlayerOne(null);
        arena.setPlayerTwo(null);
        arena.setStatus(ArenaStatus.WAITING);
    }

    private UUID other(Arena arena, UUID player) {
        if (arena.playerOne() != null && arena.playerOne().equals(player)) {
            return arena.playerTwo();
        }
        return arena.playerOne();
    }

    private void load() {
        arenas.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Arena arena = new Arena(id);
            arena.setWorld(section.getString("world", "world"));
            arena.setX1(section.getInt("pos1.x"));
            arena.setY1(section.getInt("pos1.y"));
            arena.setZ1(section.getInt("pos1.z"));
            arena.setX2(section.getInt("pos2.x"));
            arena.setY2(section.getInt("pos2.y"));
            arena.setZ2(section.getInt("pos2.z"));
            arena.setSpawn1x(section.getInt("spawn1.x"));
            arena.setSpawn1y(section.getInt("spawn1.y"));
            arena.setSpawn1z(section.getInt("spawn1.z"));
            arena.setSpawn2x(section.getInt("spawn2.x"));
            arena.setSpawn2y(section.getInt("spawn2.y"));
            arena.setSpawn2z(section.getInt("spawn2.z"));
            arenas.put(id, arena);
        }
    }

    private void seedDefaults() {
        config.set("arena1.world", "world");
        config.set("arena1.pos1.x", -10);
        config.set("arena1.pos1.y", 60);
        config.set("arena1.pos1.z", -10);
        config.set("arena1.pos2.x", 10);
        config.set("arena1.pos2.y", 70);
        config.set("arena1.pos2.z", 10);
        config.set("arena1.spawn1.x", -5);
        config.set("arena1.spawn1.y", 65);
        config.set("arena1.spawn1.z", 0);
        config.set("arena1.spawn2.x", 5);
        config.set("arena1.spawn2.y", 65);
        config.set("arena1.spawn2.z", 0);
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save arenas.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/AuctionHouseManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/AuctionHouseManager.java`  
- Size: 3726 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.AuctionListing;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class AuctionHouseManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, AuctionListing> listings = new HashMap<>();

    public AuctionHouseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "auctions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, AuctionListing> listings() {
        return listings;
    }

    public AuctionListing getListing(String id) {
        return listings.get(id);
    }

    public void addListing(AuctionListing listing) {
        listings.put(listing.id(), listing);
        saveListing(listing);
    }

    public void removeListing(String id) {
        listings.remove(id);
        config.set(id, null);
        save();
    }

    public void saveListing(AuctionListing listing) {
        ConfigurationSection section = config.createSection(listing.id());
        section.set("seller", listing.seller() != null ? listing.seller().toString() : null);
        section.set("price", listing.price());
        section.set("item", listing.itemData());
        save();
    }

    public String serializeItem(ItemStack item) {
        try (java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
             BukkitObjectOutputStream dataOut = new BukkitObjectOutputStream(output)) {
            dataOut.writeObject(item);
            return java.util.Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize item: " + e.getMessage());
            return null;
        }
    }

    public ItemStack deserializeItem(String data) {
        if (data == null) {
            return null;
        }
        try (java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(java.util.Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataIn = new BukkitObjectInputStream(input)) {
            Object obj = dataIn.readObject();
            return obj instanceof ItemStack item ? item : null;
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().warning("Failed to deserialize item: " + e.getMessage());
            return null;
        }
    }

    private void load() {
        listings.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            AuctionListing listing = new AuctionListing(id);
            String seller = section.getString("seller", null);
            listing.setSeller(seller != null ? UUID.fromString(seller) : null);
            listing.setPrice(section.getInt("price", 0));
            listing.setItemData(section.getString("item", null));
            listings.put(id, listing);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save auctions.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/BehaviorTreeManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/BehaviorTreeManager.java`  
- Size: 8095 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.behavior.BehaviorNode;
import com.example.rpg.behavior.CastSkillNode;
import com.example.rpg.behavior.CooldownNode;
import com.example.rpg.behavior.FleeNode;
import com.example.rpg.behavior.HealthBelowNode;
import com.example.rpg.behavior.HealSelfNode;
import com.example.rpg.behavior.InverterNode;
import com.example.rpg.behavior.MeleeAttackNode;
import com.example.rpg.behavior.SelectorNode;
import com.example.rpg.behavior.SequenceNode;
import com.example.rpg.behavior.TargetDistanceAboveNode;
import com.example.rpg.behavior.WalkToTargetNode;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BehaviorTreeManager {
    private final JavaPlugin plugin;
    private final File folder;
    private final Map<String, BehaviorNode> trees = new HashMap<>();

    public BehaviorTreeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "behaviors");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        seedSkeletonKing();
        loadAll();
    }

    public BehaviorNode getTree(String name) {
        if (name == null) {
            return defaultTree();
        }
        return trees.getOrDefault(name, defaultTree());
    }

    public void addTemplate(String treeName, Map<String, Object> template) {
        YamlConfiguration config = loadConfig(treeName);
        List<Map<?, ?>> children = new ArrayList<>(config.getMapList("children"));
        children.add(template);
        config.set("type", "selector");
        config.set("children", children);
        saveConfig(treeName, config);
        loadAll();
    }

    public void resetTree(String treeName) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", "selector");
        config.set("children", new ArrayList<>());
        saveConfig(treeName, config);
        loadAll();
    }

    private void loadAll() {
        trees.clear();
        File[] files = folder.listFiles((dir, file) -> file.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            BehaviorNode root = parseNode(config, "root");
            String key = file.getName().replace(".yml", "");
            if (root != null) {
                trees.put(key, root);
            }
        }
    }

    private BehaviorNode parseNode(ConfigurationSection section, String fallbackId) {
        if (section == null) {
            return null;
        }
        String type = section.getString("type", "selector");
        String id = section.getString("id", fallbackId + "-" + UUID.randomUUID());
        return buildNode(type, id, section);
    }

    private BehaviorNode buildNode(String type, String id, ConfigurationSection section) {
        return switch (type.toLowerCase()) {
            case "selector" -> buildComposite(new SelectorNode(id), section);
            case "sequence" -> buildComposite(new SequenceNode(id), section);
            case "inverter" -> {
                BehaviorNode child = parseChild(section, "child", id);
                yield child != null ? new InverterNode(id, child) : null;
            }
            case "cooldown" -> {
                BehaviorNode child = parseChild(section, "child", id);
                long cooldown = (long) (section.getDouble("cooldownSeconds", 5) * 1000);
                yield child != null ? new CooldownNode(id, child, cooldown) : null;
            }
            case "melee_attack" -> new MeleeAttackNode(id);
            case "cast_skill" -> new CastSkillNode(id, section.getString("skill", "ember_shot"));
            case "flee" -> new FleeNode(id);
            case "heal_self" -> new HealSelfNode(id, section.getDouble("amount", 6));
            case "walk_to_target" -> new WalkToTargetNode(id);
            case "health_below" -> new HealthBelowNode(id, section.getDouble("threshold", 0.2));
            case "target_distance_above" -> new TargetDistanceAboveNode(id, section.getDouble("distance", 10));
            default -> null;
        };
    }

    private BehaviorNode buildComposite(com.example.rpg.behavior.CompositeNode node, ConfigurationSection section) {
        List<Map<?, ?>> children = section.getMapList("children");
        for (int i = 0; i < children.size(); i++) {
            Map<?, ?> data = children.get(i);
            if (!(data.get("type") instanceof String childType)) {
                continue;
            }
            YamlConfiguration childConfig = new YamlConfiguration();
            for (Map.Entry<?, ?> entry : data.entrySet()) {
                childConfig.set(String.valueOf(entry.getKey()), entry.getValue());
            }
            BehaviorNode child = buildNode(childType, node.id() + "-child-" + i, childConfig);
            if (child != null) {
                node.children().add(child);
            }
        }
        return node;
    }

    private BehaviorNode parseChild(ConfigurationSection section, String key, String id) {
        ConfigurationSection childSection = section.getConfigurationSection(key);
        if (childSection != null) {
            return parseNode(childSection, id + "-child");
        }
        return null;
    }

    private BehaviorNode defaultTree() {
        SelectorNode root = new SelectorNode("default-root");
        SequenceNode chase = new SequenceNode("default-chase");
        chase.children().add(new TargetDistanceAboveNode("default-dist", 2));
        chase.children().add(new WalkToTargetNode("default-walk"));
        root.children().add(chase);
        root.children().add(new MeleeAttackNode("default-melee"));
        return root;
    }

    private void seedSkeletonKing() {
        File file = new File(folder, "skeleton_king.yml");
        if (file.exists()) {
            return;
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set("type", "selector");
        List<Map<String, Object>> children = new ArrayList<>();

        Map<String, Object> emergency = new HashMap<>();
        emergency.put("type", "sequence");
        List<Map<String, Object>> emergencyChildren = new ArrayList<>();
        emergencyChildren.add(Map.of("type", "health_below", "threshold", 0.2));
        emergencyChildren.add(Map.of("type", "cast_skill", "skill", "shield_wall"));
        emergencyChildren.add(Map.of("type", "heal_self", "amount", 8));
        emergency.put("children", emergencyChildren);
        children.add(emergency);

        Map<String, Object> ranged = new HashMap<>();
        ranged.put("type", "sequence");
        List<Map<String, Object>> rangedChildren = new ArrayList<>();
        rangedChildren.add(Map.of("type", "target_distance_above", "distance", 10));
        rangedChildren.add(Map.of("type", "cast_skill", "skill", "ember_shot"));
        ranged.put("children", rangedChildren);
        children.add(ranged);

        children.add(Map.of("type", "melee_attack"));
        config.set("children", children);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to seed skeleton_king.yml: " + e.getMessage());
        }
    }

    private YamlConfiguration loadConfig(String treeName) {
        File file = new File(folder, treeName + ".yml");
        return YamlConfiguration.loadConfiguration(file);
    }

    private void saveConfig(String treeName, YamlConfiguration config) {
        File file = new File(folder, treeName + ".yml");
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save behavior tree " + treeName + ": " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/BuildingManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/BuildingManager.java`  
- Size: 25005 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.BuildingCategory;
import com.example.rpg.model.BuildingDefinition;
import com.example.rpg.model.FurnitureDefinition;
import com.example.rpg.schematic.Schematic;
import com.example.rpg.schematic.SchematicPaster;
import com.example.rpg.schematic.SpongeSchemLoader;
import com.example.rpg.schematic.Transform;
import com.example.rpg.util.Text;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class BuildingManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, BuildingDefinition> buildings = new HashMap<>();
    private final Map<UUID, PlacementSession> placementSessions = new HashMap<>();
    private final SpongeSchemLoader loader = new SpongeSchemLoader();
    private final Map<String, CompletableFuture<Schematic>> schematicCache = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<com.example.rpg.schematic.UndoBuffer>> undoHistory = new HashMap<>();
    private final Map<UUID, PlacementRecord> lastPlacement = new HashMap<>();
    private final Random random = new Random();
    private final Executor asyncExecutor = CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS);

    public BuildingManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "buildings.yml");
        if (!file.exists()) {
            plugin.saveResource("buildings.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        ensureSchematicsFolder();
        load();
    }

    public Map<String, BuildingDefinition> buildings() {
        return buildings;
    }

    public Map<BuildingCategory, List<BuildingDefinition>> byCategory() {
        Map<BuildingCategory, List<BuildingDefinition>> categorized = new EnumMap<>(BuildingCategory.class);
        for (BuildingCategory category : BuildingCategory.values()) {
            categorized.put(category, new ArrayList<>());
        }
        for (BuildingDefinition definition : buildings.values()) {
            categorized.get(definition.category()).add(definition);
        }
        return categorized;
    }

    public BuildingDefinition getBuilding(String id) {
        return buildings.get(id);
    }

    public void beginPlacement(Player player, String buildingId, Transform.Rotation rotation) {
        BuildingDefinition definition = buildings.get(buildingId);
        if (definition == null) {
            player.sendMessage(Text.mm("<red>Gebäude nicht gefunden."));
            return;
        }
        placementSessions.put(player.getUniqueId(), new PlacementSession(buildingId, null, rotation));
        player.sendMessage(Text.mm("<green>Platzierungsmodus aktiv. Rechtsklick auf einen Block zum Platzieren."));
    }

    public void beginSingleSchematicPlacement(Player player, String schematicName, Transform.Rotation rotation) {
        if (schematicName == null || schematicName.isBlank()) {
            player.sendMessage(Text.mm("<red>Kein Schematic angegeben."));
            return;
        }
        placementSessions.put(player.getUniqueId(), new PlacementSession(null, schematicName.trim(), rotation));
        player.sendMessage(Text.mm("<green>Platzierungsmodus aktiv. Rechtsklick auf einen Block zum Platzieren."));
    }

    public boolean handlePlacement(Player player, Location target) {
        PlacementSession session = placementSessions.remove(player.getUniqueId());
        if (session == null) {
            return false;
        }
        if (session.schematicName() != null) {
            placeSingleSchematic(player, target, session.schematicName(), session.rotation());
            return true;
        }
        BuildingDefinition definition = buildings.get(session.buildingId());
        if (definition == null) {
            player.sendMessage(Text.mm("<red>Gebäude nicht gefunden."));
            return true;
        }
        placeBuilding(player, target, definition, session.rotation());
        return true;
    }

    private void placeBuilding(Player player, Location origin, BuildingDefinition definition, Transform.Rotation rotation) {
        String schematicName = definition.schematic();
        if (schematicName == null || schematicName.isBlank()) {
            player.sendMessage(Text.mm("<red>Kein Haupt-Schematic gesetzt."));
            return;
        }
        List<CompletableFuture<Schematic>> futures = new ArrayList<>();
        CompletableFuture<Schematic> baseFuture = loadSchematicAsync(schematicName);
        futures.add(baseFuture);
        CompletableFuture<Schematic> floorFuture = null;
        if (definition.floorSchematic() != null) {
            floorFuture = loadSchematicAsync(definition.floorSchematic());
            futures.add(floorFuture);
        }
        CompletableFuture<Schematic> basementFuture = null;
        if (definition.basementSchematic() != null) {
            basementFuture = loadSchematicAsync(definition.basementSchematic());
            futures.add(basementFuture);
        }
        Map<FurnitureDefinition, CompletableFuture<Schematic>> furnitureFutures = new HashMap<>();
        for (FurnitureDefinition furniture : definition.furniture()) {
            CompletableFuture<Schematic> furnitureFuture = loadSchematicAsync(furniture.schematic());
            furnitureFutures.put(furniture, furnitureFuture);
            futures.add(furnitureFuture);
        }
        final CompletableFuture<Schematic> finalBaseFuture = baseFuture;
        final CompletableFuture<Schematic> finalFloorFuture = floorFuture;
        final CompletableFuture<Schematic> finalBasementFuture = basementFuture;
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .whenCompleteAsync((ignored, throwable) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (throwable != null) {
                    plugin.getLogger().warning("Failed to load schematic: " + throwable.getMessage());
                    player.sendMessage(Text.mm("<red>Gebäude konnte nicht geladen werden."));
                    return;
                }
                try {
                    com.example.rpg.schematic.UndoBuffer undoBuffer = new com.example.rpg.schematic.UndoBuffer();
                    int floors = resolveFloors(definition);
                    List<PlacementPart> parts = pasteBuilding(origin, definition, rotation, floors, finalBaseFuture.join(),
                        finalFloorFuture != null ? finalFloorFuture.join() : null,
                        finalBasementFuture != null ? finalBasementFuture.join() : null,
                        furnitureFutures,
                        undoBuffer);
                    pushUndo(player.getUniqueId(), undoBuffer);
                    recordPlacement(player.getUniqueId(), origin, parts);
                    player.sendMessage(Text.mm("<green>Gebäude platziert: " + definition.name()));
                    plugin.guiManager().openSchematicMoveGui(player);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Failed to paste building: " + ex.getMessage());
                    player.sendMessage(Text.mm("<red>Gebäude konnte nicht platziert werden."));
                }
            }), asyncExecutor);
    }

    private void placeSingleSchematic(Player player, Location origin, String schematicName, Transform.Rotation rotation) {
        CompletableFuture<Schematic> future = loadSchematicAsync(schematicName);
        future.whenCompleteAsync((schematic, throwable) -> plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (throwable != null) {
                plugin.getLogger().warning("Failed to load schematic: " + throwable.getMessage());
                player.sendMessage(Text.mm("<red>Schematic konnte nicht geladen werden."));
                return;
            }
            try {
                SchematicPaster paster = new SchematicPaster(plugin);
                Transform transform = new Transform(rotation, 0, 0, 0);
                com.example.rpg.schematic.UndoBuffer undoBuffer = new com.example.rpg.schematic.UndoBuffer();
                prepareArea(origin, schematic, transform, 3, undoBuffer);
                paster.pasteInBatches(origin.getWorld(), origin, schematic,
                    new SchematicPaster.PasteOptions(false, transform, undoBuffer), 5000);
                pushUndo(player.getUniqueId(), undoBuffer);
                recordPlacement(player.getUniqueId(), origin, List.of(new PlacementPart(schematic, transform, false)));
                player.sendMessage(Text.mm("<green>Schematic platziert: " + schematicName));
                plugin.guiManager().openSchematicMoveGui(player);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to paste schematic: " + ex.getMessage());
                player.sendMessage(Text.mm("<red>Bereinigung oder Platzierung fehlgeschlagen."));
            }
        }), asyncExecutor);
    }

    public void undoLast(Player player) {
        if (!undoInternal(player)) {
            player.sendMessage(Text.mm("<yellow>Kein Gebäude zum Rückgängig machen."));
            return;
        }
        player.sendMessage(Text.mm("<green>Letztes Gebäude rückgängig gemacht."));
    }

    public void moveLastPlacement(Player player, int dx, int dy, int dz) {
        PlacementRecord record = lastPlacement.get(player.getUniqueId());
        if (record == null) {
            player.sendMessage(Text.mm("<yellow>Kein Schematic zum Verschieben gefunden."));
            return;
        }
        if (!undoInternal(player)) {
            player.sendMessage(Text.mm("<yellow>Kein Schematic zum Verschieben gefunden."));
            return;
        }
        Location newOrigin = record.origin().clone().add(dx, dy, dz);
        com.example.rpg.schematic.UndoBuffer undoBuffer = new com.example.rpg.schematic.UndoBuffer();
        for (PlacementPart part : record.parts()) {
            prepareArea(newOrigin, part.schematic(), part.transform(), 3, undoBuffer);
            new SchematicPaster(plugin).pasteInBatches(newOrigin.getWorld(), newOrigin, part.schematic(),
                new SchematicPaster.PasteOptions(part.includeAir(), part.transform(), undoBuffer), 5000);
        }
        pushUndo(player.getUniqueId(), undoBuffer);
        recordPlacement(player.getUniqueId(), newOrigin, record.parts());
        player.sendMessage(Text.mm("<green>Schematic verschoben."));
    }

    private void pushUndo(UUID playerId, com.example.rpg.schematic.UndoBuffer buffer) {
        undoHistory.computeIfAbsent(playerId, key -> new ArrayDeque<>()).push(buffer);
    }

    private List<PlacementPart> pasteBuilding(Location origin, BuildingDefinition definition, Transform.Rotation rotation, int floors, Schematic base,
                               Schematic floor, Schematic basement, Map<FurnitureDefinition, CompletableFuture<Schematic>> furnitureFutures,
                               com.example.rpg.schematic.UndoBuffer undoBuffer) {
        SchematicPaster paster = new SchematicPaster(plugin);
        List<PlacementPart> parts = new ArrayList<>();
        Transform baseTransform = new Transform(rotation, definition.offsetX(), definition.offsetY(), definition.offsetZ());
        prepareArea(origin, base, baseTransform, 3, undoBuffer);
        paster.pasteInBatches(origin.getWorld(), origin, base,
            new SchematicPaster.PasteOptions(definition.includeAir(), baseTransform, undoBuffer), 5000);
        parts.add(new PlacementPart(base, baseTransform, definition.includeAir()));
        if (basement != null && definition.basementDepth() > 0) {
            Transform basementTransform = new Transform(rotation, definition.offsetX(), definition.offsetY() - definition.basementDepth(), definition.offsetZ());
            prepareArea(origin, basement, basementTransform, 3, undoBuffer);
            paster.pasteInBatches(origin.getWorld(), origin, basement,
                new SchematicPaster.PasteOptions(definition.includeAir(), basementTransform, undoBuffer), 5000);
            parts.add(new PlacementPart(basement, basementTransform, definition.includeAir()));
        }
        for (int i = 1; i < floors; i++) {
            Schematic floorSchematic = floor != null ? floor : base;
            Transform floorTransform = new Transform(rotation, definition.offsetX(), definition.offsetY() + definition.floorHeight() * i, definition.offsetZ());
            prepareArea(origin, floorSchematic, floorTransform, 3, undoBuffer);
            paster.pasteInBatches(origin.getWorld(), origin, floorSchematic,
                new SchematicPaster.PasteOptions(definition.includeAir(), floorTransform, undoBuffer), 5000);
            parts.add(new PlacementPart(floorSchematic, floorTransform, definition.includeAir()));
        }
        for (var entry : furnitureFutures.entrySet()) {
            FurnitureDefinition furniture = entry.getKey();
            Schematic furnitureSchematic = entry.getValue().join();
            Transform.Rotation combinedRotation = rotationForDegrees((rotationToDegrees(rotation) + furniture.rotation()) % 360);
            Transform transform = new Transform(combinedRotation,
                definition.offsetX() + furniture.offsetX(),
                definition.offsetY() + furniture.offsetY(),
                definition.offsetZ() + furniture.offsetZ());
            prepareArea(origin, furnitureSchematic, transform, 3, undoBuffer);
            paster.pasteInBatches(origin.getWorld(), origin, furnitureSchematic,
                new SchematicPaster.PasteOptions(definition.includeAir(), transform, undoBuffer), 2000);
            parts.add(new PlacementPart(furnitureSchematic, transform, definition.includeAir()));
        }
        return parts;
    }

    private int resolveFloors(BuildingDefinition definition) {
        int minFloors = Math.max(1, definition.minFloors());
        int maxFloors = Math.max(minFloors, definition.maxFloors());
        return Math.max(1, random.nextInt(maxFloors - minFloors + 1) + minFloors);
    }

    private void prepareArea(Location origin, Schematic schematic, Transform transform, int buffer,
                             com.example.rpg.schematic.UndoBuffer undoBuffer) {
        if (origin.getWorld() == null) {
            throw new IllegalStateException("World not available for placement.");
        }
        Bounds bounds = calculateBounds(schematic, transform, buffer);
        loadChunks(origin, bounds);
        clearEntities(origin, bounds);
        clearBlocks(origin, bounds, undoBuffer);
    }

    private void clearBlocks(Location origin, Bounds bounds, com.example.rpg.schematic.UndoBuffer undoBuffer) {
        for (int x = bounds.minX; x <= bounds.maxX; x++) {
            for (int y = bounds.minY; y <= bounds.maxY; y++) {
                for (int z = bounds.minZ; z <= bounds.maxZ; z++) {
                    var block = origin.getWorld().getBlockAt(origin.getBlockX() + x, origin.getBlockY() + y, origin.getBlockZ() + z);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    if (isSupportedGround(block.getType()) && y == bounds.minY) {
                        continue;
                    }
                    if (undoBuffer != null) {
                        undoBuffer.add(block.getLocation(), block.getBlockData());
                    }
                    block.setType(Material.AIR, false);
                }
            }
        }
    }

    private void clearEntities(Location origin, Bounds bounds) {
        BoundingBox box = BoundingBox.of(
            new org.bukkit.util.Vector(
                origin.getBlockX() + bounds.minX,
                origin.getBlockY() + bounds.minY,
                origin.getBlockZ() + bounds.minZ
            ),
            new org.bukkit.util.Vector(
                origin.getBlockX() + bounds.maxX + 1,
                origin.getBlockY() + bounds.maxY + 1,
                origin.getBlockZ() + bounds.maxZ + 1
            )
        );
        for (var entity : origin.getWorld().getNearbyEntities(box)) {
            if (entity instanceof Player) {
                continue;
            }
            entity.remove();
        }
    }

    private boolean isSupportedGround(Material material) {
        return material == Material.DIRT
            || material == Material.GRASS_BLOCK
            || material == Material.STONE
            || material == Material.COBBLESTONE;
    }

    private void loadChunks(Location origin, Bounds bounds) {
        if (origin.getWorld() == null) {
            return;
        }
        int minX = origin.getBlockX() + bounds.minX;
        int maxX = origin.getBlockX() + bounds.maxX;
        int minZ = origin.getBlockZ() + bounds.minZ;
        int maxZ = origin.getBlockZ() + bounds.maxZ;
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                if (!origin.getWorld().isChunkLoaded(x, z)) {
                    origin.getWorld().getChunkAt(x, z);
                }
            }
        }
    }

    private Bounds calculateBounds(Schematic schematic, Transform transform, int buffer) {
        int width = schematic.width();
        int height = schematic.height();
        int length = schematic.length();
        int[][] corners = new int[][]{
            transform.apply(0, 0, 0, width, length),
            transform.apply(width - 1, 0, 0, width, length),
            transform.apply(0, 0, length - 1, width, length),
            transform.apply(width - 1, 0, length - 1, width, length),
            transform.apply(0, height - 1, 0, width, length),
            transform.apply(width - 1, height - 1, 0, width, length),
            transform.apply(0, height - 1, length - 1, width, length),
            transform.apply(width - 1, height - 1, length - 1, width, length)
        };
        int minX = corners[0][0];
        int maxX = corners[0][0];
        int minY = corners[0][1];
        int maxY = corners[0][1];
        int minZ = corners[0][2];
        int maxZ = corners[0][2];
        for (int[] corner : corners) {
            minX = Math.min(minX, corner[0]);
            maxX = Math.max(maxX, corner[0]);
            minY = Math.min(minY, corner[1]);
            maxY = Math.max(maxY, corner[1]);
            minZ = Math.min(minZ, corner[2]);
            maxZ = Math.max(maxZ, corner[2]);
        }
        return new Bounds(minX - buffer, maxX + buffer, minY, maxY, minZ - buffer, maxZ + buffer);
    }

    private CompletableFuture<Schematic> loadSchematicAsync(String name) {
        return schematicCache.computeIfAbsent(name, key -> CompletableFuture.supplyAsync(() -> {
            File file = new File(schematicsFolder(), key);
            if (!file.exists()) {
                throw new IllegalStateException("Schematic not found: " + key);
            }
            try {
                return loader.load(file);
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                schematicCache.remove(key);
            }
        }));
    }

    private File schematicsFolder() {
        String folderName = plugin.getConfig().getString("building.schematicsFolder", "schematics");
        return new File(plugin.getDataFolder(), folderName);
    }

    private void ensureSchematicsFolder() {
        File folder = schematicsFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Could not create schematics folder: " + folder.getAbsolutePath());
        }
    }

    private void load() {
        buildings.clear();
        ConfigurationSection root = config.getConfigurationSection("buildings");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            BuildingDefinition definition = new BuildingDefinition(id);
            definition.setName(section.getString("name", id));
            definition.setCategory(BuildingCategory.fromString(section.getString("category")));
            definition.setSchematic(section.getString("schematic", null));
            definition.setFloorSchematic(section.getString("floorSchematic", null));
            definition.setMinFloors(section.getInt("minFloors", 1));
            definition.setMaxFloors(section.getInt("maxFloors", definition.minFloors()));
            if (definition.maxFloors() < definition.minFloors()) {
                definition.setMaxFloors(definition.minFloors());
            }
            definition.setFloorHeight(section.getInt("floorHeight", 5));
            definition.setIncludeAir(section.getBoolean("includeAir", false));
            ConfigurationSection offset = section.getConfigurationSection("offset");
            if (offset != null) {
                definition.setOffset(offset.getInt("x", 0), offset.getInt("y", 0), offset.getInt("z", 0));
            }
            ConfigurationSection basement = section.getConfigurationSection("basement");
            if (basement != null) {
                definition.setBasementSchematic(basement.getString("schematic", null));
                definition.setBasementDepth(basement.getInt("depth", 0));
            }
            List<Map<?, ?>> furnitureList = section.getMapList("furniture");
            for (Map<?, ?> entry : furnitureList) {
                Object schematicValue = entry.get("schematic");
                String furnitureSchematic = schematicValue != null ? String.valueOf(schematicValue) : "";
                int offsetX = parseInt(entry.get("x"), 0);
                int offsetY = parseInt(entry.get("y"), 0);
                int offsetZ = parseInt(entry.get("z"), 0);
                int rotation = parseInt(entry.get("rotation"), 0);
                if (!furnitureSchematic.isBlank()) {
                    definition.addFurniture(new FurnitureDefinition(furnitureSchematic, offsetX, offsetY, offsetZ, rotation));
                }
            }
            buildings.put(id, definition);
        }
    }

    private int parseInt(Object value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Transform.Rotation rotationForDegrees(int degrees) {
        return switch (degrees) {
            case 90 -> Transform.Rotation.CLOCKWISE_90;
            case 180 -> Transform.Rotation.CLOCKWISE_180;
            case 270 -> Transform.Rotation.CLOCKWISE_270;
            default -> Transform.Rotation.NONE;
        };
    }

    private int rotationToDegrees(Transform.Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> 90;
            case CLOCKWISE_180 -> 180;
            case CLOCKWISE_270 -> 270;
            default -> 0;
        };
    }

    private boolean undoInternal(Player player) {
        Deque<com.example.rpg.schematic.UndoBuffer> history = undoHistory.get(player.getUniqueId());
        if (history == null || history.isEmpty()) {
            return false;
        }
        com.example.rpg.schematic.UndoBuffer buffer = history.pop();
        for (var snapshot : buffer.snapshots()) {
            snapshot.location().getBlock().setBlockData(snapshot.data(), false);
        }
        return true;
    }

    private void recordPlacement(UUID playerId, Location origin, List<PlacementPart> parts) {
        lastPlacement.put(playerId, new PlacementRecord(origin, parts));
    }

    private record PlacementSession(String buildingId, String schematicName, Transform.Rotation rotation) {
    }

    private record PlacementRecord(Location origin, List<PlacementPart> parts) {
    }

    private record PlacementPart(Schematic schematic, Transform transform, boolean includeAir) {
    }

    private record Bounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ClassManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ClassManager.java`  
- Size: 2885 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.ClassDefinition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ClassManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ClassDefinition> classes = new HashMap<>();

    public ClassManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "classes.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public ClassDefinition getClass(String id) {
        return classes.get(id);
    }

    public Map<String, ClassDefinition> classes() {
        return classes;
    }

    public void saveClass(ClassDefinition definition) {
        ConfigurationSection section = config.createSection(definition.id());
        section.set("name", definition.name());
        section.set("startSkills", definition.startSkills());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ClassDefinition definition : classes.values()) {
            saveClass(definition);
        }
        save();
    }

    private void load() {
        classes.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ClassDefinition definition = new ClassDefinition(id);
            definition.setName(section.getString("name", id));
            definition.setStartSkills(section.getStringList("startSkills"));
            classes.put(id, definition);
        }
    }

    private void seedDefaults() {
        ClassDefinition warrior = new ClassDefinition("warrior");
        warrior.setName("Krieger");
        warrior.setStartSkills(List.of("taunt"));

        ClassDefinition ranger = new ClassDefinition("ranger");
        ranger.setName("Ranger");
        ranger.setStartSkills(List.of("dash"));

        ClassDefinition mage = new ClassDefinition("mage");
        mage.setName("Magier");
        mage.setStartSkills(List.of("heal"));

        classes.put(warrior.id(), warrior);
        classes.put(ranger.id(), ranger);
        classes.put(mage.id(), mage);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save classes.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/DungeonManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/DungeonManager.java`  
- Size: 4450 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.dungeon.DungeonGenerator;
import com.example.rpg.dungeon.DungeonInstance;
import com.example.rpg.util.Text;
import com.example.rpg.util.WorldUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DungeonManager {
    private final RPGPlugin plugin;
    private final FileConfiguration config;
    private Location entrance;
    private Location exit;
    private final Map<UUID, Location> returnLocations = new HashMap<>();
    private final DungeonGenerator generator;
    private final Map<UUID, DungeonInstance> activeInstances = new HashMap<>();
    private final List<DungeonInstance> allInstances = new ArrayList<>();

    public DungeonManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.generator = new DungeonGenerator(plugin);
        load();
    }

    public Location getEntrance() {
        return entrance;
    }

    public void enterDungeon(org.bukkit.entity.Player player) {
        returnLocations.put(player.getUniqueId(), player.getLocation());
        if (entrance != null) {
            player.teleport(entrance);
        }
    }

    public void leaveDungeon(org.bukkit.entity.Player player) {
        Location back = returnLocations.remove(player.getUniqueId());
        activeInstances.remove(player.getUniqueId());
        if (back != null) {
            player.teleport(back);
            return;
        }
        if (exit != null) {
            player.teleport(exit);
            return;
        }
        if (!plugin.getServer().getWorlds().isEmpty()) {
            player.teleport(plugin.getServer().getWorlds().get(0).getSpawnLocation());
        }
    }

    public void generateDungeon(Player player, String theme, List<Player> party) {
        if (!party.contains(player)) {
            party.add(player);
        }
        java.util.function.Consumer<DungeonInstance> onGenerated = instance -> {
            allInstances.add(instance);
            for (Player member : party) {
                returnLocations.put(member.getUniqueId(), member.getLocation());
                activeInstances.put(member.getUniqueId(), instance);
                if (instance.start() != null) {
                    member.teleport(instance.start());
                    member.sendMessage(Text.mm("<green>Dungeon generiert: " + theme));
                }
            }
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> closeDungeon(instance), 20L * 60L * 15L);
        };

        if ("wfc".equalsIgnoreCase(theme)) {
            generator.generateWfc(theme, party, onGenerated);
            return;
        }
        DungeonInstance instance = generator.generate(theme, party);
        onGenerated.accept(instance);
    }

    public void closeDungeon(DungeonInstance instance) {
        if (!allInstances.contains(instance)) {
            return;
        }
        WorldUtils.unloadAndDeleteWorld(instance.world(), exit != null ? exit : entrance);
        allInstances.remove(instance);
        activeInstances.values().removeIf(active -> active.equals(instance));
    }

    public void shutdown() {
        for (DungeonInstance instance : new ArrayList<>(allInstances)) {
            WorldUtils.unloadAndDeleteWorld(instance.world(), exit);
        }
        allInstances.clear();
        activeInstances.clear();
    }

    private void load() {
        String world = config.getString("dungeon.entrance.world", null);
        if (world != null && plugin.getServer().getWorld(world) != null) {
            entrance = new Location(plugin.getServer().getWorld(world),
                config.getDouble("dungeon.entrance.x"),
                config.getDouble("dungeon.entrance.y"),
                config.getDouble("dungeon.entrance.z"));
        }
        String exitWorld = config.getString("dungeon.exit.world", null);
        if (exitWorld != null && plugin.getServer().getWorld(exitWorld) != null) {
            exit = new Location(plugin.getServer().getWorld(exitWorld),
                config.getDouble("dungeon.exit.x"),
                config.getDouble("dungeon.exit.y"),
                config.getDouble("dungeon.exit.z"));
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/FactionManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/FactionManager.java`  
- Size: 2234 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Faction;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class FactionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Faction> factions = new HashMap<>();

    public FactionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "factions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Faction getFaction(String id) {
        return factions.get(id);
    }

    public Map<String, Faction> factions() {
        return factions;
    }

    public void saveFaction(Faction faction) {
        ConfigurationSection section = config.createSection(faction.id());
        section.set("name", faction.name());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Faction faction : factions.values()) {
            saveFaction(faction);
        }
        save();
    }

    private void load() {
        factions.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Faction faction = new Faction(id);
            faction.setName(section.getString("name", id));
            factions.put(id, faction);
        }
    }

    private void seedDefaults() {
        Faction faction = new Faction("adventurers");
        faction.setName("Abenteurergilde");
        factions.put(faction.id(), faction);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save factions.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/GuildManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/GuildManager.java`  
- Size: 7780 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Guild;
import com.example.rpg.model.GuildMemberRole;
import com.example.rpg.model.GuildQuest;
import com.example.rpg.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class GuildManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Guild> guilds = new HashMap<>();
    private final Map<UUID, String> guildByMember = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    public GuildManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "guilds.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Optional<Guild> guildById(String id) {
        return Optional.ofNullable(guilds.get(id));
    }

    public Optional<Guild> guildFor(UUID member) {
        String id = guildByMember.get(member);
        return id == null ? Optional.empty() : guildById(id);
    }

    public boolean isMember(UUID member) {
        return guildByMember.containsKey(member);
    }

    public void createGuild(String id, String name, Player leader) {
        Guild guild = new Guild(id);
        guild.setName(name);
        guild.setLeader(leader.getUniqueId());
        guild.members().put(leader.getUniqueId(), GuildMemberRole.LEADER);
        guilds.put(id, guild);
        guildByMember.put(leader.getUniqueId(), id);
        PlayerProfile profile = plugin.playerDataManager().getProfile(leader);
        profile.setGuildId(id);
        saveGuild(guild);
    }

    public void disbandGuild(Guild guild) {
        for (UUID member : guild.members().keySet()) {
            guildByMember.remove(member);
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            profile.setGuildId(null);
        }
        guilds.remove(guild.id());
        config.set(guild.id(), null);
        save();
    }

    public void invite(UUID target, String guildId) {
        pendingInvites.put(target, guildId);
    }

    public Optional<Guild> acceptInvite(UUID playerId) {
        String guildId = pendingInvites.remove(playerId);
        if (guildId == null) {
            return Optional.empty();
        }
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return Optional.empty();
        }
        guild.members().put(playerId, GuildMemberRole.MEMBER);
        guildByMember.put(playerId, guildId);
        PlayerProfile profile = plugin.playerDataManager().getProfile(playerId);
        profile.setGuildId(guildId);
        saveGuild(guild);
        return Optional.of(guild);
    }

    public void leaveGuild(UUID member) {
        String guildId = guildByMember.remove(member);
        if (guildId == null) {
            return;
        }
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return;
        }
        if (guild.leader() != null && guild.leader().equals(member)) {
            disbandGuild(guild);
            return;
        }
        guild.members().remove(member);
        PlayerProfile profile = plugin.playerDataManager().getProfile(member);
        profile.setGuildId(null);
        saveGuild(guild);
    }

    public void setRole(Guild guild, UUID member, GuildMemberRole role) {
        guild.members().put(member, role);
        saveGuild(guild);
    }

    public void deposit(Guild guild, int amount) {
        guild.setBankGold(guild.bankGold() + amount);
        saveGuild(guild);
    }

    public boolean withdraw(Guild guild, int amount) {
        if (guild.bankGold() < amount) {
            return false;
        }
        guild.setBankGold(guild.bankGold() - amount);
        saveGuild(guild);
        return true;
    }

    public void saveAll() {
        for (Guild guild : guilds.values()) {
            saveGuild(guild);
        }
        save();
    }

    private void load() {
        guilds.clear();
        guildByMember.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Guild guild = new Guild(id);
            guild.setName(section.getString("name", id));
            String leader = section.getString("leader", null);
            if (leader != null) {
                guild.setLeader(UUID.fromString(leader));
            }
            guild.setBankGold(section.getInt("bankGold", 0));
            ConfigurationSection members = section.getConfigurationSection("members");
            if (members != null) {
                for (String uuid : members.getKeys(false)) {
                    try {
                        GuildMemberRole role = GuildMemberRole.valueOf(members.getString(uuid, "MEMBER"));
                        UUID memberId = UUID.fromString(uuid);
                        guild.members().put(memberId, role);
                        guildByMember.put(memberId, id);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            ConfigurationSection quests = section.getConfigurationSection("quests");
            if (quests != null) {
                for (String questId : quests.getKeys(false)) {
                    ConfigurationSection questSection = quests.getConfigurationSection(questId);
                    if (questSection == null) {
                        continue;
                    }
                    GuildQuest quest = new GuildQuest(questId);
                    quest.setName(questSection.getString("name", questId));
                    quest.setDescription(questSection.getString("description", ""));
                    quest.setGoal(questSection.getInt("goal", 1));
                    quest.setProgress(questSection.getInt("progress", 0));
                    quest.setCompleted(questSection.getBoolean("completed", false));
                    guild.quests().put(questId, quest);
                }
            }
            guilds.put(id, guild);
        }
    }

    private void saveGuild(Guild guild) {
        ConfigurationSection section = config.createSection(guild.id());
        section.set("name", guild.name());
        section.set("leader", guild.leader() != null ? guild.leader().toString() : null);
        section.set("bankGold", guild.bankGold());
        ConfigurationSection members = section.createSection("members");
        for (Map.Entry<UUID, GuildMemberRole> entry : guild.members().entrySet()) {
            members.set(entry.getKey().toString(), entry.getValue().name());
        }
        ConfigurationSection quests = section.createSection("quests");
        for (GuildQuest quest : guild.quests().values()) {
            ConfigurationSection questSection = quests.createSection(quest.id());
            questSection.set("name", quest.name());
            questSection.set("description", quest.description());
            questSection.set("goal", quest.goal());
            questSection.set("progress", quest.progress());
            questSection.set("completed", quest.completed());
        }
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save guilds.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ItemStatManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ItemStatManager.java`  
- Size: 4615 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.util.Text;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemStatManager {
    private final Random random = new Random();
    private final NamespacedKey strengthKey;
    private final NamespacedKey critKey;
    private final NamespacedKey healthKey;
    private final NamespacedKey setIdKey;
    private final Map<String, PotionEffectType> setBonuses = Map.of(
        "ember", PotionEffectType.FIRE_RESISTANCE,
        "guardian", PotionEffectType.DAMAGE_RESISTANCE,
        "swift", PotionEffectType.SPEED
    );

    public ItemStatManager(JavaPlugin plugin) {
        this.strengthKey = new NamespacedKey(plugin, "stat_strength");
        this.critKey = new NamespacedKey(plugin, "stat_crit");
        this.healthKey = new NamespacedKey(plugin, "stat_health");
        this.setIdKey = new NamespacedKey(plugin, "set_id");
    }

    public void applyAffixes(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        String prefix = randomFrom(List.of("Brennendes", "Gefrorenes", "Stählernen", "Mystisches"));
        String suffix = randomFrom(List.of("der Stärke", "der Präzision", "des Lebens"));
        meta.displayName(Component.text(prefix + " " + prettyName(item.getType().name()) + " " + suffix));

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(strengthKey, PersistentDataType.INTEGER, 1 + random.nextInt(4));
        data.set(critKey, PersistentDataType.DOUBLE, 0.02 + random.nextDouble() * 0.08);
        data.set(healthKey, PersistentDataType.INTEGER, 2 + random.nextInt(6));
        data.set(setIdKey, PersistentDataType.STRING, randomFrom(setBonuses.keySet().stream().toList()));

        updateLore(meta);
        item.setItemMeta(meta);
    }

    public void updateLore(ItemMeta meta) {
        PersistentDataContainer data = meta.getPersistentDataContainer();
        int strength = data.getOrDefault(strengthKey, PersistentDataType.INTEGER, 0);
        double crit = data.getOrDefault(critKey, PersistentDataType.DOUBLE, 0.0);
        int health = data.getOrDefault(healthKey, PersistentDataType.INTEGER, 0);
        String setId = data.get(setIdKey, PersistentDataType.STRING);
        meta.lore(List.of(
            Text.mm("<gray>Stärke: <white>" + strength),
            Text.mm("<gray>Krit-Chance: <white>" + String.format("%.1f%%", crit * 100)),
            Text.mm("<gray>Leben: <white>" + health),
            setId != null ? Text.mm("<gold>Set: " + setId + " (4 Teile)") : Text.mm("<gray>Kein Set")
        ));
    }

    public void updateSetBonus(Player player) {
        Map<String, Integer> counts = new java.util.HashMap<>();
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getItemMeta() == null) {
                continue;
            }
            String setId = item.getItemMeta().getPersistentDataContainer().get(setIdKey, PersistentDataType.STRING);
            if (setId == null) {
                continue;
            }
            counts.put(setId, counts.getOrDefault(setId, 0) + 1);
        }
        for (Map.Entry<String, PotionEffectType> entry : setBonuses.entrySet()) {
            PotionEffectType type = entry.getValue();
            if (type == null) {
                continue;
            }
            if (counts.getOrDefault(entry.getKey(), 0) >= 4) {
                player.addPotionEffect(new PotionEffect(type, 220, 0, true, false));
            } else {
                player.removePotionEffect(type);
            }
        }
    }

    public NamespacedKey strengthKey() {
        return strengthKey;
    }

    public NamespacedKey critKey() {
        return critKey;
    }

    public NamespacedKey healthKey() {
        return healthKey;
    }

    public NamespacedKey setIdKey() {
        return setIdKey;
    }

    private String randomFrom(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }

    private String prettyName(String material) {
        return material.toLowerCase().replace("_", " ");
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/LootManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/LootManager.java`  
- Size: 4381 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Rarity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LootManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, LootTable> tables = new HashMap<>();

    public LootManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "loot.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Map<String, LootTable> tables() {
        return tables;
    }

    public LootTable getTable(String id) {
        return tables.get(id);
    }

    public LootTable getTableFor(String key) {
        for (LootTable table : tables.values()) {
            if (table.appliesTo().equalsIgnoreCase(key)) {
                return table;
            }
        }
        return null;
    }

    public void saveTable(LootTable table) {
        ConfigurationSection section = config.createSection(table.id());
        section.set("appliesTo", table.appliesTo());
        List<Map<String, Object>> entries = new ArrayList<>();
        for (LootEntry entry : table.entries()) {
            Map<String, Object> map = new HashMap<>();
            map.put("material", entry.material());
            map.put("chance", entry.chance());
            map.put("minAmount", entry.minAmount());
            map.put("maxAmount", entry.maxAmount());
            map.put("rarity", entry.rarity().name());
            entries.add(map);
        }
        section.set("entries", entries);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (LootTable table : tables.values()) {
            saveTable(table);
        }
        save();
    }

    private void load() {
        tables.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            LootTable table = new LootTable(id);
            table.setAppliesTo(section.getString("appliesTo", "ZOMBIE"));
            List<LootEntry> entries = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList("entries")) {
                Object materialValue = raw.containsKey("material") ? raw.get("material") : "IRON_NUGGET";
                Object chanceValue = raw.containsKey("chance") ? raw.get("chance") : 0.3;
                Object minValue = raw.containsKey("minAmount") ? raw.get("minAmount") : 1;
                Object maxValue = raw.containsKey("maxAmount") ? raw.get("maxAmount") : 1;
                Object rarityValue = raw.containsKey("rarity") ? raw.get("rarity") : "COMMON";
                String material = String.valueOf(materialValue);
                double chance = Double.parseDouble(String.valueOf(chanceValue));
                int minAmount = Integer.parseInt(String.valueOf(minValue));
                int maxAmount = Integer.parseInt(String.valueOf(maxValue));
                Rarity rarity = Rarity.valueOf(String.valueOf(rarityValue));
                entries.add(new LootEntry(material, chance, minAmount, maxAmount, rarity));
            }
            table.setEntries(entries);
            tables.put(id, table);
        }
    }

    private void seedDefaults() {
        LootTable table = new LootTable("forest_mobs");
        table.setAppliesTo("ZOMBIE");
        table.setEntries(List.of(
            new LootEntry("IRON_NUGGET", 0.5, 1, 3, Rarity.COMMON),
            new LootEntry("EMERALD", 0.15, 1, 1, Rarity.RARE)
        ));
        tables.put(table.id(), table);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save loot.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/MobManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/MobManager.java`  
- Size: 4354 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.MobDefinition;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MobManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, MobDefinition> mobs = new HashMap<>();

    public MobManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "mobs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public MobDefinition getMob(String id) {
        return mobs.get(id);
    }

    public Map<String, MobDefinition> mobs() {
        return mobs;
    }

    public void saveMob(MobDefinition mob) {
        ConfigurationSection section = config.createSection(mob.id());
        section.set("name", mob.name());
        section.set("type", mob.type());
        section.set("health", mob.health());
        section.set("damage", mob.damage());
        section.set("mainHand", mob.mainHand());
        section.set("helmet", mob.helmet());
        section.set("skills", mob.skills());
        section.set("skillIntervalSeconds", mob.skillIntervalSeconds());
        section.set("xp", mob.xp());
        section.set("lootTable", mob.lootTable());
        section.set("behaviorTree", mob.behaviorTree());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (MobDefinition mob : mobs.values()) {
            saveMob(mob);
        }
        save();
    }

    private void load() {
        mobs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            MobDefinition mob = new MobDefinition(id);
            mob.setName(section.getString("name", id));
            mob.setType(section.getString("type", "ZOMBIE"));
            mob.setHealth(section.getDouble("health", 40));
            mob.setDamage(section.getDouble("damage", 6));
            mob.setMainHand(section.getString("mainHand", null));
            mob.setHelmet(section.getString("helmet", null));
            mob.setSkills(section.getStringList("skills"));
            mob.setSkillIntervalSeconds(section.getInt("skillIntervalSeconds", 8));
            mob.setXp(section.getInt("xp", 50));
            mob.setLootTable(section.getString("lootTable", null));
            mob.setBehaviorTree(section.getString("behaviorTree", null));
            mobs.put(id, mob);
        }
    }

    private void seedDefaults() {
        MobDefinition zombie = new MobDefinition("boss_zombie");
        zombie.setName("§cSeuchenbringer");
        zombie.setType("ZOMBIE");
        zombie.setHealth(60);
        zombie.setDamage(8);
        zombie.setMainHand("IRON_SWORD");
        zombie.setHelmet("IRON_HELMET");
        zombie.setSkills(List.of("ember_shot", "whirlwind"));
        zombie.setSkillIntervalSeconds(10);
        zombie.setXp(120);
        zombie.setLootTable("forest_mobs");
        mobs.put(zombie.id(), zombie);

        MobDefinition skeletonKing = new MobDefinition("skeleton_king");
        skeletonKing.setName("§cSkelettkönig");
        skeletonKing.setType("SKELETON");
        skeletonKing.setHealth(80);
        skeletonKing.setDamage(10);
        skeletonKing.setMainHand("DIAMOND_SWORD");
        skeletonKing.setHelmet("GOLDEN_HELMET");
        skeletonKing.setSkills(List.of("shield_wall", "ember_shot"));
        skeletonKing.setSkillIntervalSeconds(8);
        skeletonKing.setXp(180);
        skeletonKing.setLootTable("forest_mobs");
        skeletonKing.setBehaviorTree("skeleton_king");
        mobs.put(skeletonKing.id(), skeletonKing);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save mobs.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/NpcManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/NpcManager.java`  
- Size: 4569 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class NpcManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Npc> npcs = new HashMap<>();
    private final NamespacedKey npcKey;

    public NpcManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "npcs.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        this.npcKey = new NamespacedKey(plugin, "npc_id");
        load();
    }

    public NamespacedKey npcKey() {
        return npcKey;
    }

    public Map<String, Npc> npcs() {
        return npcs;
    }

    public Npc getNpc(String id) {
        return npcs.get(id);
    }

    public void spawnAll() {
        for (Npc npc : npcs.values()) {
            spawnNpc(npc);
        }
    }

    public void spawnNpc(Npc npc) {
        World world = Bukkit.getWorld(npc.world());
        if (world == null) {
            return;
        }
        Location location = npc.toLocation(world);
        Villager villager = (Villager) world.spawnEntity(location, EntityType.VILLAGER);
        villager.customName(Component.text(npc.name()));
        villager.setCustomNameVisible(true);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.getPersistentDataContainer().set(npcKey, PersistentDataType.STRING, npc.id());
        npc.setUuid(villager.getUniqueId());
    }

    public void saveNpc(Npc npc) {
        ConfigurationSection section = config.createSection(npc.id());
        section.set("name", npc.name());
        section.set("role", npc.role().name());
        section.set("world", npc.world());
        section.set("x", npc.x());
        section.set("y", npc.y());
        section.set("z", npc.z());
        section.set("yaw", npc.yaw());
        section.set("pitch", npc.pitch());
        section.set("dialog", npc.dialog());
        section.set("questLink", npc.questLink());
        section.set("shopId", npc.shopId());
        section.set("uuid", npc.uuid() != null ? npc.uuid().toString() : null);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Npc npc : npcs.values()) {
            saveNpc(npc);
        }
        save();
    }

    private void load() {
        npcs.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Npc npc = new Npc(id);
            npc.setName(section.getString("name", id));
            npc.setRole(NpcRole.valueOf(section.getString("role", "QUESTGIVER")));
            npc.setWorld(section.getString("world", "world"));
            npc.setDialog(section.getStringList("dialog"));
            npc.setQuestLink(section.getString("questLink", null));
            npc.setShopId(section.getString("shopId", null));
            npc.setUuid(section.contains("uuid") ? UUID.fromString(section.getString("uuid")) : null);
            World world = Bukkit.getWorld(npc.world());
            double x = section.getDouble("x");
            double y = section.getDouble("y");
            double z = section.getDouble("z");
            float yaw = (float) section.getDouble("yaw");
            float pitch = (float) section.getDouble("pitch");
            if (world != null) {
                npc.setLocation(new Location(world, x, y, z, yaw, pitch));
            } else {
                npc.setRawLocation(npc.world(), x, y, z, yaw, pitch);
            }
            npcs.put(id, npc);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save npcs.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/PartyManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/PartyManager.java`  
- Size: 1251 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Party;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PartyManager {
    private final Map<UUID, Party> partiesByMember = new HashMap<>();

    public Party createParty(UUID leader) {
        Party party = new Party(leader);
        partiesByMember.put(leader, party);
        return party;
    }

    public Optional<Party> getParty(UUID member) {
        return Optional.ofNullable(partiesByMember.get(member));
    }

    public void addMember(Party party, UUID member) {
        party.addMember(member);
        partiesByMember.put(member, party);
    }

    public void removeMember(UUID member) {
        Party party = partiesByMember.get(member);
        if (party == null) {
            return;
        }

        party.removeMember(member);
        partiesByMember.remove(member);

        if (party.leader().equals(member)) {
            for (UUID uuid : party.members()) {
                partiesByMember.remove(uuid);
            }
            party.members().clear();
            return;
        }

        if (party.members().isEmpty()) {
            partiesByMember.remove(party.leader());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/PlayerDataManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/PlayerDataManager.java`  
- Size: 1837 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.db.PlayerDao;
import com.example.rpg.model.PlayerProfile;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerDataManager {
    private final JavaPlugin plugin;
    private final PlayerDao playerDao;
    private final Map<UUID, PlayerProfile> profiles = new HashMap<>();

    public PlayerDataManager(JavaPlugin plugin, PlayerDao playerDao) {
        this.plugin = plugin;
        this.playerDao = playerDao;
    }

    public PlayerProfile getProfile(UUID uuid) {
        return profiles.computeIfAbsent(uuid, PlayerProfile::new);
    }

    public PlayerProfile getProfile(Player player) {
        return getProfile(player.getUniqueId());
    }

    public Map<UUID, PlayerProfile> profiles() {
        return profiles;
    }

    public CompletableFuture<PlayerProfile> loadProfileAsync(UUID uuid) {
        return playerDao.loadPlayer(uuid).exceptionally(error -> {
            plugin.getLogger().warning("Failed to load player " + uuid + ": " + error.getMessage());
            return null;
        }).thenApply(profile -> {
            PlayerProfile resolved = profile != null ? profile : new PlayerProfile(uuid);
            profiles.put(uuid, resolved);
            return resolved;
        });
    }

    public void saveProfile(PlayerProfile profile) {
        playerDao.savePlayer(profile).exceptionally(error -> {
            plugin.getLogger().warning("Failed to save player " + profile.uuid() + ": " + error.getMessage());
            return null;
        });
    }

    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            saveProfile(profile);
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ProfessionManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ProfessionManager.java`  
- Size: 7825 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ProfessionManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ProfessionDefinition> professions = new HashMap<>();

    public ProfessionManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "professions.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public int getLevel(PlayerProfile profile, String profession) {
        return profile.professions().getOrDefault(profession + "_level", 1);
    }

    public void setLevel(PlayerProfile profile, String profession, int level) {
        profile.professions().put(profession + "_level", Math.max(1, level));
    }

    public Map<String, Integer> professions(PlayerProfile profile) {
        return profile.professions();
    }

    public int addXp(PlayerProfile profile, String profession, int xp, Player player) {
        int currentXp = profile.professions().getOrDefault(profession + "_xp", 0);
        int newXp = currentXp + Math.max(0, xp);
        profile.professions().put(profession + "_xp", newXp);
        int level = profile.professions().getOrDefault(profession + "_level", 1);
        int oldLevel = level;
        int threshold = level * 100;
        while (newXp >= threshold) {
            newXp -= threshold;
            level++;
            threshold = level * 100;
        }
        profile.professions().put(profession + "_level", level);
        profile.professions().put(profession + "_xp", newXp);
        if (player != null && level > oldLevel) {
            fireLevelRewards(profession, level, player);
        }
        return level;
    }

    public int xpForMaterial(String profession, String material) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return 0;
        }
        return definition.xpSources().getOrDefault(material, 0);
    }

    public int requiredLevelForCraft(String profession, String resultMaterial) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return 0;
        }
        return definition.craftRequirements().getOrDefault(resultMaterial, 0);
    }

    public Map<String, ProfessionDefinition> definitions() {
        return professions;
    }

    private void fireLevelRewards(String profession, int level, Player player) {
        ProfessionDefinition definition = professions.get(profession);
        if (definition == null) {
            return;
        }
        List<String> commands = definition.levelRewards().get(level);
        if (commands == null) {
            return;
        }
        for (String command : commands) {
            String resolved = command.replace("{player}", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), resolved);
        }
        player.sendMessage(com.example.rpg.util.Text.mm("<gold>Beruf " + definition.displayName()
            + " Level " + level + " erreicht!"));
    }

    private void load() {
        professions.clear();
        ConfigurationSection root = config.getConfigurationSection("professions");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ProfessionDefinition definition = new ProfessionDefinition(id);
            definition.setDisplayName(section.getString("display", id));
            ConfigurationSection xpSources = section.getConfigurationSection("xpSources");
            if (xpSources != null) {
                for (String material : xpSources.getKeys(false)) {
                    definition.xpSources().put(material, xpSources.getInt(material, 0));
                }
            }
            ConfigurationSection craftReq = section.getConfigurationSection("craftRequirements");
            if (craftReq != null) {
                for (String material : craftReq.getKeys(false)) {
                    definition.craftRequirements().put(material, craftReq.getInt(material, 0));
                }
            }
            ConfigurationSection rewards = section.getConfigurationSection("levelRewards");
            if (rewards != null) {
                for (String levelKey : rewards.getKeys(false)) {
                    try {
                        int lvl = Integer.parseInt(levelKey);
                        definition.levelRewards().put(lvl, rewards.getStringList(levelKey));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            professions.put(id, definition);
        }
    }

    private void seedDefaults() {
        config.set("professions.mining.display", "Bergbau");
        config.set("professions.mining.xpSources.COAL_ORE", 5);
        config.set("professions.mining.xpSources.IRON_ORE", 8);
        config.set("professions.mining.xpSources.DIAMOND_ORE", 15);
        config.set("professions.mining.levelRewards.5", List.of("give {player} iron_pickaxe 1"));

        config.set("professions.herbalism.display", "Kräuterkunde");
        config.set("professions.herbalism.xpSources.WHEAT", 4);
        config.set("professions.herbalism.xpSources.CARROTS", 4);
        config.set("professions.herbalism.xpSources.NETHER_WART", 8);
        config.set("professions.herbalism.levelRewards.5", List.of("give {player} golden_apple 1"));

        config.set("professions.blacksmithing.display", "Schmiedekunst");
        config.set("professions.blacksmithing.xpSources.IRON_SWORD", 10);
        config.set("professions.blacksmithing.xpSources.DIAMOND_SWORD", 20);
        config.set("professions.blacksmithing.craftRequirements.IRON_SWORD", 3);
        config.set("professions.blacksmithing.craftRequirements.DIAMOND_SWORD", 6);
        config.set("professions.blacksmithing.levelRewards.5", List.of("give {player} anvil 1"));
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save professions.yml: " + e.getMessage());
        }
    }

    public static class ProfessionDefinition {
        private final String id;
        private String displayName;
        private final Map<String, Integer> xpSources = new HashMap<>();
        private final Map<String, Integer> craftRequirements = new HashMap<>();
        private final Map<Integer, List<String>> levelRewards = new HashMap<>();

        public ProfessionDefinition(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String displayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Map<String, Integer> xpSources() {
            return xpSources;
        }

        public Map<String, Integer> craftRequirements() {
            return craftRequirements;
        }

        public Map<Integer, List<String>> levelRewards() {
            return levelRewards;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/QuestManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/QuestManager.java`  
- Size: 5056 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestReward;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Quest> quests = new HashMap<>();

    public QuestManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "quests.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Quest getQuest(String id) {
        return quests.get(id);
    }

    public Map<String, Quest> quests() {
        return quests;
    }

    public void saveQuest(Quest quest) {
        ConfigurationSection section = config.createSection(quest.id());
        section.set("name", quest.name());
        section.set("description", quest.description());
        section.set("repeatable", quest.repeatable());
        section.set("minLevel", quest.minLevel());
        List<Map<String, Object>> steps = new ArrayList<>();
        for (QuestStep step : quest.steps()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", step.type().name());
            map.put("target", step.target());
            map.put("amount", step.amount());
            steps.add(map);
        }
        section.set("steps", steps);
        QuestReward reward = quest.reward();
        section.set("reward.xp", reward.xp());
        section.set("reward.skillPoints", reward.skillPoints());
        section.set("reward.factionRep", reward.factionRep());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Quest quest : quests.values()) {
            saveQuest(quest);
        }
        save();
    }

    private void load() {
        quests.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Quest quest = new Quest(id);
            quest.setName(section.getString("name", id));
            quest.setDescription(section.getString("description", ""));
            quest.setRepeatable(section.getBoolean("repeatable", false));
            quest.setMinLevel(section.getInt("minLevel", 1));
            List<QuestStep> steps = new ArrayList<>();
            for (Map<?, ?> raw : section.getMapList("steps")) {
                Object typeValue = raw.containsKey("type") ? raw.get("type") : "KILL";
                Object targetValue = raw.containsKey("target") ? raw.get("target") : "ZOMBIE";
                Object amountValue = raw.containsKey("amount") ? raw.get("amount") : 1;
                String typeName = String.valueOf(typeValue);
                String target = String.valueOf(targetValue);
                int amount = Integer.parseInt(String.valueOf(amountValue));
                QuestStepType type = QuestStepType.valueOf(typeName);
                steps.add(new QuestStep(type, target, amount));
            }
            quest.setSteps(steps);
            QuestReward reward = new QuestReward();
            reward.setXp(section.getInt("reward.xp", 50));
            reward.setSkillPoints(section.getInt("reward.skillPoints", 1));
            ConfigurationSection factionRep = section.getConfigurationSection("reward.factionRep");
            if (factionRep != null) {
                Map<String, Integer> rep = new HashMap<>();
                for (String faction : factionRep.getKeys(false)) {
                    rep.put(faction, factionRep.getInt(faction));
                }
                reward.setFactionRep(rep);
            }
            quest.setReward(reward);
            quests.put(id, quest);
        }
    }

    private void seedDefaults() {
        Quest quest = new Quest("starter_hunt");
        quest.setName("Wolfsplage");
        quest.setDescription("Jage 3 Wölfe und kehre zurück.");
        quest.setRepeatable(false);
        quest.setMinLevel(1);
        quest.setSteps(List.of(new QuestStep(QuestStepType.KILL, "WOLF", 3)));
        QuestReward reward = new QuestReward();
        reward.setXp(120);
        reward.setSkillPoints(1);
        quest.setReward(reward);
        quests.put(quest.id(), quest);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save quests.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ShopManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ShopManager.java`  
- Size: 4435 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.ShopDefinition;
import com.example.rpg.model.ShopItem;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, ShopDefinition> shops = new HashMap<>();

    public ShopManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "shops.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public ShopDefinition getShop(String id) {
        return shops.get(id);
    }

    public Map<String, ShopDefinition> shops() {
        return shops;
    }

    public void saveShop(ShopDefinition shop) {
        ConfigurationSection section = config.createSection(shop.id());
        section.set("title", shop.title());
        List<Map<String, Object>> items = new java.util.ArrayList<>();
        for (ShopItem item : shop.items().values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("slot", item.slot());
            map.put("material", item.material());
            map.put("name", item.name());
            map.put("buyPrice", item.buyPrice());
            map.put("sellPrice", item.sellPrice());
            items.add(map);
        }
        section.set("items", items);
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (ShopDefinition shop : shops.values()) {
            saveShop(shop);
        }
        save();
    }

    private void load() {
        shops.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            ShopDefinition shop = new ShopDefinition(id);
            shop.setTitle(section.getString("title", id));
            Map<Integer, ShopItem> items = new HashMap<>();
            for (Map<?, ?> raw : section.getMapList("items")) {
                ShopItem item = new ShopItem();
                Object slotValue = raw.containsKey("slot") ? raw.get("slot") : 0;
                Object materialValue = raw.containsKey("material") ? raw.get("material") : "STONE";
                Object nameValue = raw.containsKey("name") ? raw.get("name") : "";
                Object buyValue = raw.containsKey("buyPrice") ? raw.get("buyPrice") : 0;
                Object sellValue = raw.containsKey("sellPrice") ? raw.get("sellPrice") : 0;
                item.setSlot(Integer.parseInt(String.valueOf(slotValue)));
                item.setMaterial(String.valueOf(materialValue));
                item.setName(String.valueOf(nameValue));
                item.setBuyPrice(Integer.parseInt(String.valueOf(buyValue)));
                item.setSellPrice(Integer.parseInt(String.valueOf(sellValue)));
                items.put(item.slot(), item);
            }
            shop.setItems(items);
            shops.put(id, shop);
        }
    }

    private void seedDefaults() {
        ShopDefinition shop = new ShopDefinition("blacksmith");
        shop.setTitle("Dorfschmied");
        ShopItem sword = new ShopItem();
        sword.setSlot(0);
        sword.setMaterial("IRON_SWORD");
        sword.setName("&7Eisenschwert");
        sword.setBuyPrice(100);
        sword.setSellPrice(20);
        ShopItem potion = new ShopItem();
        potion.setSlot(1);
        potion.setMaterial("POTION");
        potion.setName("&aHeiltrank");
        potion.setBuyPrice(50);
        potion.setSellPrice(10);
        Map<Integer, ShopItem> items = new HashMap<>();
        items.put(sword.slot(), sword);
        items.put(potion.slot(), potion);
        shop.setItems(items);
        shops.put(shop.id(), shop);
        saveAll();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save shops.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillHotbarManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillHotbarManager.java`  
- Size: 603 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.PlayerProfile;

public class SkillHotbarManager {
    private final PlayerDataManager playerDataManager;

    public SkillHotbarManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void bindSkill(PlayerProfile profile, int slot, String skillId) {
        profile.skillBindings().put(slot, skillId);
        playerDataManager.saveProfile(profile);
    }

    public String getBinding(PlayerProfile profile, int slot) {
        return profile.skillBindings().get(slot);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillManager.java`  
- Size: 15212 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillCategory;
import com.example.rpg.model.SkillType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Skill> skills = new HashMap<>();

    public SkillManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "skills.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public Skill getSkill(String id) {
        return skills.get(id);
    }

    public Map<String, Skill> skills() {
        return skills;
    }

    public void saveSkill(Skill skill) {
        ConfigurationSection section = config.createSection(skill.id());
        section.set("name", skill.name());
        section.set("type", skill.type().name());
        section.set("category", skill.category().name());
        section.set("cooldown", skill.cooldown());
        section.set("manaCost", skill.manaCost());
        section.set("effects", serializeEffects(skill.effects()));
        section.set("parent", skill.requiredSkill());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Skill skill : skills.values()) {
            saveSkill(skill);
        }
        save();
    }

    private void load() {
        skills.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Skill skill = new Skill(id);
            skill.setName(section.getString("name", id));
            skill.setType(SkillType.valueOf(section.getString("type", "ACTIVE")));
            skill.setCategory(SkillCategory.valueOf(section.getString("category", "ATTACK")));
            skill.setCooldown(section.getInt("cooldown", 10));
            skill.setManaCost(section.getInt("manaCost", 20));
            String parent = section.getString("parent", null);
            if (parent == null) {
                parent = section.getString("requiredSkill", null);
            }
            skill.setRequiredSkill(parent);
            skill.setEffects(loadEffects(section));
            skills.put(id, skill);
        }
    }

    private void seedDefaults() {
        Skill healPulse = new Skill("heal_pulse");
        healPulse.setName("Heilpuls");
        healPulse.setType(SkillType.ACTIVE);
        healPulse.setCategory(SkillCategory.HEALING);
        healPulse.setCooldown(20);
        healPulse.setManaCost(20);
        healPulse.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 4))));

        Skill greaterHeal = new Skill("greater_heal");
        greaterHeal.setName("Große Heilung");
        greaterHeal.setType(SkillType.ACTIVE);
        greaterHeal.setCategory(SkillCategory.HEALING);
        greaterHeal.setCooldown(30);
        greaterHeal.setManaCost(35);
        greaterHeal.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 8))));
        greaterHeal.setRequiredSkill("heal_pulse");

        Skill divineBlessing = new Skill("divine_blessing");
        divineBlessing.setName("Segen");
        divineBlessing.setType(SkillType.ACTIVE);
        divineBlessing.setCategory(SkillCategory.HEALING);
        divineBlessing.setCooldown(45);
        divineBlessing.setManaCost(45);
        divineBlessing.setEffects(List.of(effectConfig("HEAL", Map.of("amount", 12)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_BEACON_POWER_SELECT", "volume", 1.0, "pitch", 1.0))));
        divineBlessing.setRequiredSkill("greater_heal");

        Skill emberShot = new Skill("ember_shot");
        emberShot.setName("Flammenstoß");
        emberShot.setType(SkillType.ACTIVE);
        emberShot.setCategory(SkillCategory.MAGIC);
        emberShot.setCooldown(12);
        emberShot.setManaCost(18);
        emberShot.setEffects(List.of(effectConfig("PROJECTILE", Map.of("type", "SMALL_FIREBALL")),
            effectConfig("SOUND", Map.of("sound", "ENTITY_BLAZE_SHOOT", "volume", 1.0, "pitch", 1.2))));

        Skill frostBolt = new Skill("frost_bolt");
        frostBolt.setName("Frostbolzen");
        frostBolt.setType(SkillType.ACTIVE);
        frostBolt.setCategory(SkillCategory.MAGIC);
        frostBolt.setCooldown(18);
        frostBolt.setManaCost(25);
        frostBolt.setEffects(List.of(effectConfig("PROJECTILE", Map.of("type", "SNOWBALL")),
            effectConfig("POTION", Map.of("type", "SLOW", "duration", 60, "amplifier", 1, "radius", 6)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_GLASS_BREAK", "volume", 0.8, "pitch", 1.4))));
        frostBolt.setRequiredSkill("ember_shot");

        Skill arcaneBurst = new Skill("arcane_burst");
        arcaneBurst.setName("Arkane Explosion");
        arcaneBurst.setType(SkillType.ACTIVE);
        arcaneBurst.setCategory(SkillCategory.MAGIC);
        arcaneBurst.setCooldown(30);
        arcaneBurst.setManaCost(35);
        arcaneBurst.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 6, "radius", 5, "maxTargets", 10)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_ILLUSIONER_CAST_SPELL", "volume", 1.0, "pitch", 1.2))));
        arcaneBurst.setRequiredSkill("frost_bolt");

        Skill powerStrike = new Skill("power_strike");
        powerStrike.setName("Machtstoß");
        powerStrike.setType(SkillType.ACTIVE);
        powerStrike.setCategory(SkillCategory.ATTACK);
        powerStrike.setCooldown(8);
        powerStrike.setManaCost(10);
        powerStrike.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 8, "radius", 3, "maxTargets", 1)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_PLAYER_ATTACK_STRONG", "volume", 1.0, "pitch", 1.0))));

        Skill whirlwind = new Skill("whirlwind");
        whirlwind.setName("Wirbelwind");
        whirlwind.setType(SkillType.ACTIVE);
        whirlwind.setCategory(SkillCategory.ATTACK);
        whirlwind.setCooldown(20);
        whirlwind.setManaCost(20);
        whirlwind.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 5, "radius", 4, "maxTargets", 10)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_PLAYER_ATTACK_SWEEP", "volume", 1.0, "pitch", 0.8))));
        whirlwind.setRequiredSkill("power_strike");

        Skill execute = new Skill("execute");
        execute.setName("Hinrichtung");
        execute.setType(SkillType.ACTIVE);
        execute.setCategory(SkillCategory.ATTACK);
        execute.setCooldown(35);
        execute.setManaCost(30);
        execute.setEffects(List.of(effectConfig("DAMAGE", Map.of("amount", 12, "radius", 3, "maxTargets", 1)),
            effectConfig("SOUND", Map.of("sound", "ENTITY_WITHER_SKELETON_HURT", "volume", 1.0, "pitch", 0.9))));
        execute.setRequiredSkill("whirlwind");

        Skill shieldWall = new Skill("shield_wall");
        shieldWall.setName("Schildwall");
        shieldWall.setType(SkillType.ACTIVE);
        shieldWall.setCategory(SkillCategory.DEFENSE);
        shieldWall.setCooldown(25);
        shieldWall.setManaCost(15);
        shieldWall.setEffects(List.of(effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 120, "amplifier", 0)),
            effectConfig("SOUND", Map.of("sound", "ITEM_SHIELD_BLOCK", "volume", 1.0, "pitch", 1.0))));

        Skill fortify = new Skill("fortify");
        fortify.setName("Bollwerk");
        fortify.setType(SkillType.ACTIVE);
        fortify.setCategory(SkillCategory.DEFENSE);
        fortify.setCooldown(35);
        fortify.setManaCost(25);
        fortify.setEffects(List.of(effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 200, "amplifier", 1)),
            effectConfig("POTION", Map.of("type", "ABSORPTION", "duration", 200, "amplifier", 1)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_ANVIL_USE", "volume", 0.7, "pitch", 1.0))));
        fortify.setRequiredSkill("shield_wall");

        Skill deflect = new Skill("deflect");
        deflect.setName("Abwehrhaltung");
        deflect.setType(SkillType.ACTIVE);
        deflect.setCategory(SkillCategory.DEFENSE);
        deflect.setCooldown(45);
        deflect.setManaCost(30);
        deflect.setEffects(List.of(effectConfig("POTION", Map.of("type", "FIRE_RESISTANCE", "duration", 200, "amplifier", 0)),
            effectConfig("POTION", Map.of("type", "DAMAGE_RESISTANCE", "duration", 200, "amplifier", 0)),
            effectConfig("SOUND", Map.of("sound", "ITEM_SHIELD_BLOCK", "volume", 1.0, "pitch", 1.2))));
        deflect.setRequiredSkill("fortify");

        Skill miningFocus = new Skill("mining_focus");
        miningFocus.setName("Bergbau-Fokus");
        miningFocus.setType(SkillType.ACTIVE);
        miningFocus.setCategory(SkillCategory.PROFESSION);
        miningFocus.setCooldown(60);
        miningFocus.setManaCost(15);
        miningFocus.setEffects(List.of(effectConfig("POTION", Map.of("type", "FAST_DIGGING", "duration", 300, "amplifier", 1)),
            effectConfig("XP", Map.of("amount", 10)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_STONE_HIT", "volume", 0.8, "pitch", 1.0))));

        Skill craftingInsight = new Skill("crafting_insight");
        craftingInsight.setName("Handwerkskunst");
        craftingInsight.setType(SkillType.ACTIVE);
        craftingInsight.setCategory(SkillCategory.PROFESSION);
        craftingInsight.setCooldown(60);
        craftingInsight.setManaCost(20);
        craftingInsight.setEffects(List.of(effectConfig("POTION", Map.of("type", "LUCK", "duration", 300, "amplifier", 1)),
            effectConfig("XP", Map.of("amount", 10)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_ANVIL_PLACE", "volume", 0.8, "pitch", 1.1))));
        craftingInsight.setRequiredSkill("mining_focus");

        Skill alchemyMastery = new Skill("alchemy_mastery");
        alchemyMastery.setName("Alchemie-Meister");
        alchemyMastery.setType(SkillType.ACTIVE);
        alchemyMastery.setCategory(SkillCategory.PROFESSION);
        alchemyMastery.setCooldown(90);
        alchemyMastery.setManaCost(30);
        alchemyMastery.setEffects(List.of(effectConfig("POTION", Map.of("type", "REGENERATION", "duration", 120, "amplifier", 0)),
            effectConfig("XP", Map.of("amount", 15)),
            effectConfig("SOUND", Map.of("sound", "BLOCK_BREWING_STAND_BREW", "volume", 0.8, "pitch", 1.0))));
        alchemyMastery.setRequiredSkill("crafting_insight");

        skills.put(healPulse.id(), healPulse);
        skills.put(greaterHeal.id(), greaterHeal);
        skills.put(divineBlessing.id(), divineBlessing);
        skills.put(emberShot.id(), emberShot);
        skills.put(frostBolt.id(), frostBolt);
        skills.put(arcaneBurst.id(), arcaneBurst);
        skills.put(powerStrike.id(), powerStrike);
        skills.put(whirlwind.id(), whirlwind);
        skills.put(execute.id(), execute);
        skills.put(shieldWall.id(), shieldWall);
        skills.put(fortify.id(), fortify);
        skills.put(deflect.id(), deflect);
        skills.put(miningFocus.id(), miningFocus);
        skills.put(craftingInsight.id(), craftingInsight);
        skills.put(alchemyMastery.id(), alchemyMastery);
        saveAll();
    }

    private List<Map<String, Object>> serializeEffects(List<com.example.rpg.skill.SkillEffectConfig> effects) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (com.example.rpg.skill.SkillEffectConfig config : effects) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", config.type().name());
            map.put("params", config.params());
            list.add(map);
        }
        return list;
    }

    private List<com.example.rpg.skill.SkillEffectConfig> loadEffects(ConfigurationSection section) {
        List<com.example.rpg.skill.SkillEffectConfig> effects = new ArrayList<>();
        for (Map<?, ?> raw : section.getMapList("effects")) {
            Object typeValue = raw.containsKey("type") ? raw.get("type") : "HEAL";
            com.example.rpg.skill.SkillEffectType type = com.example.rpg.skill.SkillEffectType.valueOf(String.valueOf(typeValue));
            Map<String, Object> params = new HashMap<>();
            Object paramsValue = raw.get("params");
            if (paramsValue instanceof Map<?, ?> paramMap) {
                for (Map.Entry<?, ?> entry : paramMap.entrySet()) {
                    params.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            effects.add(new com.example.rpg.skill.SkillEffectConfig(type, params));
        }
        if (effects.isEmpty() && section.contains("effect")) {
            String legacy = section.getString("effect", "");
            effects.add(mapLegacyEffect(legacy));
        }
        return effects;
    }

    private com.example.rpg.skill.SkillEffectConfig mapLegacyEffect(String legacy) {
        if (legacy == null) {
            return new com.example.rpg.skill.SkillEffectConfig(com.example.rpg.skill.SkillEffectType.HEAL, Map.of("amount", 4));
        }
        return switch (legacy) {
            case "heal_small" -> effectConfig("HEAL", Map.of("amount", 4));
            case "heal_medium" -> effectConfig("HEAL", Map.of("amount", 8));
            case "heal_large" -> effectConfig("HEAL", Map.of("amount", 12));
            case "fireball" -> effectConfig("PROJECTILE", Map.of("type", "SMALL_FIREBALL"));
            case "frostbolt" -> effectConfig("PROJECTILE", Map.of("type", "SNOWBALL"));
            case "arcane_blast" -> effectConfig("DAMAGE", Map.of("amount", 6, "radius", 5, "maxTargets", 10));
            case "power_strike" -> effectConfig("DAMAGE", Map.of("amount", 8, "radius", 3, "maxTargets", 1));
            case "whirlwind" -> effectConfig("DAMAGE", Map.of("amount", 5, "radius", 4, "maxTargets", 10));
            case "execute" -> effectConfig("DAMAGE", Map.of("amount", 12, "radius", 3, "maxTargets", 1));
            case "dash" -> effectConfig("VELOCITY", Map.of("forward", 1.2, "up", 0.3, "add", false));
            case "taunt" -> effectConfig("AGGRO", Map.of("radius", 8));
            default -> effectConfig("HEAL", Map.of("amount", 4));
        };
    }

    private com.example.rpg.skill.SkillEffectConfig effectConfig(String type, Map<String, Object> params) {
        return new com.example.rpg.skill.SkillEffectConfig(
            com.example.rpg.skill.SkillEffectType.valueOf(type),
            params
        );
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save skills.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillTreeManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SkillTreeManager.java`  
- Size: 2011 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeManager {
    private final SkillManager skillManager;
    private final Map<String, SkillNode> nodes = new HashMap<>();

    public SkillTreeManager(SkillManager skillManager) {
        this.skillManager = skillManager;
        rebuild();
    }

    public void rebuild() {
        nodes.clear();
        for (Skill skill : skillManager.skills().values()) {
            nodes.put(skill.id(), new SkillNode(skill));
        }
        for (SkillNode node : nodes.values()) {
            String parentId = node.skill().requiredSkill();
            if (parentId != null) {
                SkillNode parent = nodes.get(parentId);
                if (parent != null) {
                    parent.children().add(node);
                    node.setParent(parent);
                }
            }
        }
    }

    public List<SkillNode> roots() {
        List<SkillNode> roots = new ArrayList<>();
        for (SkillNode node : nodes.values()) {
            if (node.parent() == null) {
                roots.add(node);
            }
        }
        roots.sort(Comparator.comparing(n -> n.skill().id()));
        return roots;
    }

    public Map<String, SkillNode> nodes() {
        return nodes;
    }

    public static class SkillNode {
        private final Skill skill;
        private SkillNode parent;
        private final List<SkillNode> children = new ArrayList<>();

        public SkillNode(Skill skill) {
            this.skill = skill;
        }

        public Skill skill() {
            return skill;
        }

        public SkillNode parent() {
            return parent;
        }

        public void setParent(SkillNode parent) {
            this.parent = parent;
        }

        public List<SkillNode> children() {
            return children;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SpawnerManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/SpawnerManager.java`  
- Size: 6871 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.MobDefinition;
import com.example.rpg.model.Spawner;
import com.example.rpg.model.Zone;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Spawner> spawners = new HashMap<>();
    private final Random random = new Random();

    public SpawnerManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "spawners.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
        startTask();
    }

    public Spawner getSpawner(String id) {
        return spawners.get(id);
    }

    public Map<String, Spawner> spawners() {
        return spawners;
    }

    public void saveSpawner(Spawner spawner) {
        ConfigurationSection section = config.createSection(spawner.id());
        section.set("zoneId", spawner.zoneId());
        section.set("maxMobs", spawner.maxMobs());
        section.set("spawnInterval", spawner.spawnInterval());
        section.set("mobs", spawner.mobs());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Spawner spawner : spawners.values()) {
            saveSpawner(spawner);
        }
        save();
    }

    private void load() {
        spawners.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Spawner spawner = new Spawner(id);
            spawner.setZoneId(section.getString("zoneId", null));
            spawner.setMaxMobs(section.getInt("maxMobs", 6));
            spawner.setSpawnInterval(section.getInt("spawnInterval", 200));
            ConfigurationSection mobsSection = section.getConfigurationSection("mobs");
            if (mobsSection != null) {
                Map<String, Double> mobs = new HashMap<>();
                for (String mobId : mobsSection.getKeys(false)) {
                    mobs.put(mobId, mobsSection.getDouble(mobId, 1.0));
                }
                spawner.setMobs(mobs);
            }
            spawners.put(id, spawner);
        }
    }

    private void seedDefaults() {
        Spawner spawner = new Spawner("forest_spawner");
        spawner.setZoneId("startzone");
        spawner.setMaxMobs(6);
        spawner.setSpawnInterval(200);
        spawner.setMobs(Map.of("boss_zombie", 1.0));
        spawners.put(spawner.id(), spawner);
        saveAll();
    }

    private void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long now = plugin.getServer().getCurrentTick();
            for (Spawner spawner : spawners.values()) {
                if (spawner.spawnInterval() <= 0) {
                    continue;
                }
                if (now % spawner.spawnInterval() != 0) {
                    continue;
                }
                if (spawner.zoneId() == null) {
                    continue;
                }
                Zone zone = plugin.zoneManager().getZone(spawner.zoneId());
                if (zone == null) {
                    continue;
                }
                if (!hasPlayersInZone(zone)) {
                    continue;
                }
                int current = countMobsInZone(zone);
                if (current >= spawner.maxMobs()) {
                    continue;
                }
                MobDefinition mob = pickMob(spawner);
                if (mob == null) {
                    continue;
                }
                spawnMobInZone(zone, mob);
            }
        }, 40L, 40L);
    }

    private boolean hasPlayersInZone(Zone zone) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return false;
        }
        return world.getPlayers().stream().anyMatch(player -> zone.contains(player.getLocation()));
    }

    private int countMobsInZone(Zone zone) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return 0;
        }
        return (int) world.getLivingEntities().stream()
            .filter(entity -> entity.getPersistentDataContainer()
                .has(plugin.customMobListener().mobKey(), PersistentDataType.STRING))
            .filter(entity -> zone.contains(entity.getLocation()))
            .count();
    }

    private MobDefinition pickMob(Spawner spawner) {
        if (spawner.mobs().isEmpty()) {
            return null;
        }
        double total = spawner.mobs().values().stream().mapToDouble(Double::doubleValue).sum();
        double roll = random.nextDouble() * total;
        double current = 0;
        for (Map.Entry<String, Double> entry : spawner.mobs().entrySet()) {
            current += entry.getValue();
            if (roll <= current) {
                return plugin.mobManager().getMob(entry.getKey());
            }
        }
        String fallback = spawner.mobs().keySet().iterator().next();
        return plugin.mobManager().getMob(fallback);
    }

    private void spawnMobInZone(Zone zone, MobDefinition mob) {
        World world = plugin.getServer().getWorld(zone.world());
        if (world == null) {
            return;
        }
        int x = randomBetween(zone.x1(), zone.x2());
        int z = randomBetween(zone.z1(), zone.z2());
        int y = world.getHighestBlockYAt(x, z);
        Location location = new Location(world, x + 0.5, y + 1, z + 0.5);
        var type = org.bukkit.entity.EntityType.valueOf(mob.type().toUpperCase());
        var entity = world.spawnEntity(location, type);
        if (entity instanceof org.bukkit.entity.LivingEntity living) {
            plugin.customMobListener().applyDefinition(living, mob);
        } else {
            entity.remove();
        }
    }

    private int randomBetween(int min, int max) {
        int low = Math.min(min, max);
        int high = Math.max(min, max);
        return low + random.nextInt(Math.max(1, high - low + 1));
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save spawners.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/TradeManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/TradeManager.java`  
- Size: 797 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.TradeRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TradeManager {
    private final Map<UUID, TradeRequest> requests = new HashMap<>();

    public void requestTrade(UUID requester, UUID target) {
        TradeRequest request = new TradeRequest(requester, target);
        requests.put(requester, request);
        requests.put(target, request);
    }

    public TradeRequest getRequest(UUID player) {
        return requests.get(player);
    }

    public void clear(UUID player) {
        TradeRequest request = requests.remove(player);
        if (request != null) {
            requests.remove(request.requester());
            requests.remove(request.target());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/VoiceChatManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/VoiceChatManager.java`  
- Size: 3358 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

public class VoiceChatManager {
    private final RPGPlugin plugin;
    private final Map<UUID, String> channels = new HashMap<>();
    private Object voiceApi;
    private Method setPlayerGroup;
    private Method createGroup;

    public VoiceChatManager(RPGPlugin plugin) {
        this.plugin = plugin;
        tryInitApi();
    }

    public void joinParty(Player player) {
        Optional<com.example.rpg.model.Party> party = plugin.partyManager().getParty(player.getUniqueId());
        if (party.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Party."));
            return;
        }
        String channel = "party-" + party.get().leader().toString();
        joinChannel(player, channel, "Party");
    }

    public void joinGuild(Player player) {
        String guildId = plugin.playerDataManager().getProfile(player).guildId();
        if (guildId == null) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        String channel = "guild-" + guildId;
        joinChannel(player, channel, "Gilde");
    }

    public void leave(Player player) {
        channels.remove(player.getUniqueId());
        if (!setGroup(player, null, "Lobby")) {
            player.sendMessage(Text.mm("<yellow>Sprachkanal verlassen."));
        }
    }

    private void joinChannel(Player player, String channel, String label) {
        channels.put(player.getUniqueId(), channel);
        if (!setGroup(player, channel, label)) {
            player.sendMessage(Text.mm("<green>Sprachchat (" + label + ") aktiviert."));
            player.sendMessage(Text.mm("<gray>Installiere Simple Voice Chat für Mikrofon-Unterstützung."));
        }
    }

    private boolean setGroup(Player player, String groupId, String label) {
        if (voiceApi == null || setPlayerGroup == null || createGroup == null) {
            return false;
        }
        try {
            UUID uuid = groupId != null ? UUID.nameUUIDFromBytes(groupId.getBytes()) : null;
            Object group = null;
            if (groupId != null) {
                group = createGroup.invoke(voiceApi, uuid, label + " " + groupId);
            }
            setPlayerGroup.invoke(voiceApi, player.getUniqueId(), group);
            player.sendMessage(Text.mm("<green>Sprachchat (" + label + ") aktiviert."));
            return true;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("Voice chat API error: " + e.getMessage());
            return false;
        }
    }

    private void tryInitApi() {
        try {
            Class<?> apiClass = Class.forName("de.maxhenkel.voicechat.api.VoicechatServerApi");
            Method getInstance = apiClass.getMethod("getInstance");
            voiceApi = getInstance.invoke(null);
            createGroup = apiClass.getMethod("createGroup", UUID.class, String.class);
            setPlayerGroup = apiClass.getMethod("setPlayerGroup", UUID.class, Object.class);
        } catch (ReflectiveOperationException e) {
            voiceApi = null;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ZoneManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/manager/ZoneManager.java`  
- Size: 3173 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.manager;

import com.example.rpg.model.Zone;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ZoneManager {
    private final JavaPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Zone> zones = new HashMap<>();

    public ZoneManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "zones.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Map<String, Zone> zones() {
        return zones;
    }

    public Zone getZone(String id) {
        return zones.get(id);
    }

    public Zone getZoneAt(Location location) {
        for (Zone zone : zones.values()) {
            if (zone.contains(location)) {
                return zone;
            }
        }
        return null;
    }

    public void saveZone(Zone zone) {
        ConfigurationSection section = config.createSection(zone.id());
        section.set("name", zone.name());
        section.set("world", zone.world());
        section.set("minLevel", zone.minLevel());
        section.set("maxLevel", zone.maxLevel());
        section.set("slowMultiplier", zone.slowMultiplier());
        section.set("damageMultiplier", zone.damageMultiplier());
        section.set("x1", zone.x1());
        section.set("y1", zone.y1());
        section.set("z1", zone.z1());
        section.set("x2", zone.x2());
        section.set("y2", zone.y2());
        section.set("z2", zone.z2());
        save();
    }

    public void saveAll() {
        config.getKeys(false).forEach(key -> config.set(key, null));
        for (Zone zone : zones.values()) {
            saveZone(zone);
        }
        save();
    }

    private void load() {
        zones.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Zone zone = new Zone(id);
            zone.setName(section.getString("name", id));
            zone.setWorld(section.getString("world", "world"));
            zone.setMinLevel(section.getInt("minLevel", 1));
            zone.setMaxLevel(section.getInt("maxLevel", 60));
            zone.setSlowMultiplier(section.getDouble("slowMultiplier", 1.0));
            zone.setDamageMultiplier(section.getDouble("damageMultiplier", 1.0));
            zone.setCoordinates(
                section.getInt("x1"), section.getInt("y1"), section.getInt("z1"),
                section.getInt("x2"), section.getInt("y2"), section.getInt("z2")
            );
            zones.put(id, zone);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save zones.yml: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Arena.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Arena.java`  
- Size: 2700 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.UUID;

public class Arena {
    private final String id;
    private String world;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    private int spawn1x;
    private int spawn1y;
    private int spawn1z;
    private int spawn2x;
    private int spawn2y;
    private int spawn2z;
    private ArenaStatus status = ArenaStatus.WAITING;
    private UUID playerOne;
    private UUID playerTwo;

    public Arena(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int x1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int y1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int z1() {
        return z1;
    }

    public void setZ1(int z1) {
        this.z1 = z1;
    }

    public int x2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int y2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public int z2() {
        return z2;
    }

    public void setZ2(int z2) {
        this.z2 = z2;
    }

    public int spawn1x() {
        return spawn1x;
    }

    public void setSpawn1x(int spawn1x) {
        this.spawn1x = spawn1x;
    }

    public int spawn1y() {
        return spawn1y;
    }

    public void setSpawn1y(int spawn1y) {
        this.spawn1y = spawn1y;
    }

    public int spawn1z() {
        return spawn1z;
    }

    public void setSpawn1z(int spawn1z) {
        this.spawn1z = spawn1z;
    }

    public int spawn2x() {
        return spawn2x;
    }

    public void setSpawn2x(int spawn2x) {
        this.spawn2x = spawn2x;
    }

    public int spawn2y() {
        return spawn2y;
    }

    public void setSpawn2y(int spawn2y) {
        this.spawn2y = spawn2y;
    }

    public int spawn2z() {
        return spawn2z;
    }

    public void setSpawn2z(int spawn2z) {
        this.spawn2z = spawn2z;
    }

    public ArenaStatus status() {
        return status;
    }

    public void setStatus(ArenaStatus status) {
        this.status = status;
    }

    public UUID playerOne() {
        return playerOne;
    }

    public void setPlayerOne(UUID playerOne) {
        this.playerOne = playerOne;
    }

    public UUID playerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(UUID playerTwo) {
        this.playerTwo = playerTwo;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ArenaStatus.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ArenaStatus.java`  
- Size: 98 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum ArenaStatus {
    WAITING,
    FIGHTING,
    ENDING
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/AuctionListing.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/AuctionListing.java`  
- Size: 726 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.UUID;

public class AuctionListing {
    private final String id;
    private UUID seller;
    private String itemData;
    private int price;

    public AuctionListing(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID seller() {
        return seller;
    }

    public void setSeller(UUID seller) {
        this.seller = seller;
    }

    public String itemData() {
        return itemData;
    }

    public void setItemData(String itemData) {
        this.itemData = itemData;
    }

    public int price() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/BuildingCategory.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/BuildingCategory.java`  
- Size: 809 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.Locale;

public enum BuildingCategory {
    RESIDENTIAL("Wohngebäude"),
    SHOP("Geschäfte"),
    PUBLIC("Öffentliche Einrichtungen"),
    CRAFTING("Hersteller");

    private final String displayName;

    BuildingCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static BuildingCategory fromString(String raw) {
        if (raw == null) {
            return RESIDENTIAL;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        for (BuildingCategory category : values()) {
            if (category.name().equals(normalized)) {
                return category;
            }
        }
        return RESIDENTIAL;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/BuildingDefinition.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/BuildingDefinition.java`  
- Size: 3041 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuildingDefinition {
    private final String id;
    private String name;
    private BuildingCategory category = BuildingCategory.RESIDENTIAL;
    private String schematic;
    private String floorSchematic;
    private int minFloors = 1;
    private int maxFloors = 1;
    private int floorHeight = 5;
    private String basementSchematic;
    private int basementDepth = 0;
    private boolean includeAir = false;
    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private final List<FurnitureDefinition> furniture = new ArrayList<>();

    public BuildingDefinition(String id) {
        this.id = id;
        this.name = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BuildingCategory category() {
        return category;
    }

    public void setCategory(BuildingCategory category) {
        this.category = category;
    }

    public String schematic() {
        return schematic;
    }

    public void setSchematic(String schematic) {
        this.schematic = schematic;
    }

    public String floorSchematic() {
        return floorSchematic;
    }

    public void setFloorSchematic(String floorSchematic) {
        this.floorSchematic = floorSchematic;
    }

    public int minFloors() {
        return minFloors;
    }

    public void setMinFloors(int minFloors) {
        this.minFloors = minFloors;
    }

    public int maxFloors() {
        return maxFloors;
    }

    public void setMaxFloors(int maxFloors) {
        this.maxFloors = maxFloors;
    }

    public int floorHeight() {
        return floorHeight;
    }

    public void setFloorHeight(int floorHeight) {
        this.floorHeight = floorHeight;
    }

    public String basementSchematic() {
        return basementSchematic;
    }

    public void setBasementSchematic(String basementSchematic) {
        this.basementSchematic = basementSchematic;
    }

    public int basementDepth() {
        return basementDepth;
    }

    public void setBasementDepth(int basementDepth) {
        this.basementDepth = basementDepth;
    }

    public boolean includeAir() {
        return includeAir;
    }

    public void setIncludeAir(boolean includeAir) {
        this.includeAir = includeAir;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public void setOffset(int x, int y, int z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
    }

    public List<FurnitureDefinition> furniture() {
        return Collections.unmodifiableList(furniture);
    }

    public void addFurniture(FurnitureDefinition furnitureDefinition) {
        furniture.add(furnitureDefinition);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ClassDefinition.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ClassDefinition.java`  
- Size: 657 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class ClassDefinition {
    private final String id;
    private String name;
    private List<String> startSkills = new ArrayList<>();

    public ClassDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> startSkills() {
        return startSkills;
    }

    public void setStartSkills(List<String> startSkills) {
        this.startSkills = startSkills;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Faction.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Faction.java`  
- Size: 350 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public class Faction {
    private final String id;
    private String name;

    public Faction(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/FurnitureDefinition.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/FurnitureDefinition.java`  
- Size: 141 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public record FurnitureDefinition(String schematic, int offsetX, int offsetY, int offsetZ, int rotation) {
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Guild.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Guild.java`  
- Size: 1059 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Guild {
    private final String id;
    private String name;
    private UUID leader;
    private int bankGold;
    private final Map<UUID, GuildMemberRole> members = new HashMap<>();
    private final Map<String, GuildQuest> quests = new HashMap<>();

    public Guild(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID leader() {
        return leader;
    }

    public void setLeader(UUID leader) {
        this.leader = leader;
    }

    public int bankGold() {
        return bankGold;
    }

    public void setBankGold(int bankGold) {
        this.bankGold = Math.max(0, bankGold);
    }

    public Map<UUID, GuildMemberRole> members() {
        return members;
    }

    public Map<String, GuildQuest> quests() {
        return quests;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/GuildMemberRole.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/GuildMemberRole.java`  
- Size: 100 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum GuildMemberRole {
    LEADER,
    OFFICER,
    MEMBER
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/GuildQuest.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/GuildQuest.java`  
- Size: 1087 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public class GuildQuest {
    private final String id;
    private String name;
    private String description;
    private int goal;
    private int progress;
    private boolean completed;

    public GuildQuest(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int goal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = Math.max(1, goal);
    }

    public int progress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, progress);
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/LootEntry.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/LootEntry.java`  
- Size: 1200 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public class LootEntry {
    private String material;
    private double chance;
    private int minAmount;
    private int maxAmount;
    private Rarity rarity;

    public LootEntry(String material, double chance, int minAmount, int maxAmount, Rarity rarity) {
        this.material = material;
        this.chance = chance;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.rarity = rarity;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public double chance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public int minAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int maxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Rarity rarity() {
        return rarity;
    }

    public void setRarity(Rarity rarity) {
        this.rarity = rarity;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/LootTable.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/LootTable.java`  
- Size: 661 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    private final String id;
    private String appliesTo;
    private List<LootEntry> entries = new ArrayList<>();

    public LootTable(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String appliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public List<LootEntry> entries() {
        return entries;
    }

    public void setEntries(List<LootEntry> entries) {
        this.entries = entries;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/MobDefinition.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/MobDefinition.java`  
- Size: 2227 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class MobDefinition {
    private final String id;
    private String name;
    private String type;
    private double health;
    private double damage;
    private String mainHand;
    private String helmet;
    private List<String> skills = new ArrayList<>();
    private int skillIntervalSeconds;
    private int xp;
    private String lootTable;
    private String behaviorTree;

    public MobDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String type() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double health() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public double damage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public String mainHand() {
        return mainHand;
    }

    public void setMainHand(String mainHand) {
        this.mainHand = mainHand;
    }

    public String helmet() {
        return helmet;
    }

    public void setHelmet(String helmet) {
        this.helmet = helmet;
    }

    public List<String> skills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public int skillIntervalSeconds() {
        return skillIntervalSeconds;
    }

    public void setSkillIntervalSeconds(int skillIntervalSeconds) {
        this.skillIntervalSeconds = skillIntervalSeconds;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public String lootTable() {
        return lootTable;
    }

    public void setLootTable(String lootTable) {
        this.lootTable = lootTable;
    }

    public String behaviorTree() {
        return behaviorTree;
    }

    public void setBehaviorTree(String behaviorTree) {
        this.behaviorTree = behaviorTree;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Npc.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Npc.java`  
- Size: 2490 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Location;

public class Npc {
    private final String id;
    private UUID uuid;
    private String name;
    private NpcRole role;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private List<String> dialog = new ArrayList<>();
    private String questLink;
    private String shopId;

    public Npc(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public UUID uuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NpcRole role() {
        return role;
    }

    public void setRole(NpcRole role) {
        this.role = role;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public void setLocation(Location location) {
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public void setRawLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Location toLocation(org.bukkit.World world) {
        return new Location(world, x, y, z, yaw, pitch);
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public List<String> dialog() {
        return dialog;
    }

    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    public String questLink() {
        return questLink;
    }

    public void setQuestLink(String questLink) {
        this.questLink = questLink;
    }

    public String shopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/NpcRole.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/NpcRole.java`  
- Size: 143 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum NpcRole {
    QUESTGIVER,
    VENDOR,
    TRAINER,
    TELEPORTER,
    BANKER,
    FACTION_AGENT
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Party.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Party.java`  
- Size: 584 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Party {
    private final UUID leader;
    private final Set<UUID> members = new HashSet<>();

    public Party(UUID leader) {
        this.leader = leader;
        members.add(leader);
    }

    public UUID leader() {
        return leader;
    }

    public Set<UUID> members() {
        return members;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/PlayerProfile.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/PlayerProfile.java`  
- Size: 4686 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class PlayerProfile {
    private final UUID uuid;
    private int level = 1;
    private int xp = 0;
    private int skillPoints = 0;
    private int mana = 100;
    private int maxMana = 100;
    private String classId;
    private final Map<RPGStat, Integer> stats = new EnumMap<>(RPGStat.class);
    private final Map<String, Integer> learnedSkills = new HashMap<>();
    private final Map<String, QuestProgress> activeQuests = new HashMap<>();
    private final Set<String> completedQuests = new HashSet<>();
    private final Map<String, Integer> factionRep = new HashMap<>();
    /**
     * Skill-Cooldowns persistent: skillId -> lastUseMillis
     */
    private final Map<String, Long> skillCooldowns = new HashMap<>();
    private final Map<Integer, String> skillBindings = new HashMap<>();
    private int gold = 0;
    private final Map<String, Integer> professions = new HashMap<>();
    private String guildId;
    private int elo = 1000;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;
        for (RPGStat stat : RPGStat.values()) {
            stats.put(stat, 5);
        }
    }

    public UUID uuid() {
        return uuid;
    }

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int skillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public int mana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public int maxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public String classId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Map<RPGStat, Integer> stats() {
        return stats;
    }

    public Map<String, Integer> learnedSkills() {
        return learnedSkills;
    }

    public Map<String, QuestProgress> activeQuests() {
        return activeQuests;
    }

    public Set<String> completedQuests() {
        return completedQuests;
    }

    public Map<String, Integer> factionRep() {
        return factionRep;
    }

    public Map<String, Long> skillCooldowns() {
        return skillCooldowns;
    }

    public Map<Integer, String> skillBindings() {
        return skillBindings;
    }

    public int gold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = Math.max(0, gold);
    }

    public Map<String, Integer> professions() {
        return professions;
    }

    public String guildId() {
        return guildId;
    }

    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }

    public int elo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = Math.max(0, elo);
    }

    public void addXp(int amount) {
        xp += amount;
        while (xp >= xpNeeded()) {
            xp -= xpNeeded();
            level++;
            skillPoints += 2;
        }
    }

    public int xpNeeded() {
        return 100 + (level - 1) * 50;
    }

    public void applyAttributes(Player player) {
        int strength = stats.getOrDefault(RPGStat.STRENGTH, 5);
        int dex = stats.getOrDefault(RPGStat.DEXTERITY, 5);
        int con = stats.getOrDefault(RPGStat.CONSTITUTION, 5);
        int intel = stats.getOrDefault(RPGStat.INTELLIGENCE, 5);

        if (player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(1.0 + strength * 0.2);
        }
        if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0 + con * 0.8);
        }
        if (player.getAttribute(Attribute.GENERIC_ATTACK_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0 + dex * 0.05);
        }
        if (player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1 + dex * 0.002);
        }
        maxMana = 100 + intel * 5;
        mana = Math.min(mana, maxMana);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Profession.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Profession.java`  
- Size: 87 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum Profession {
    GATHERING,
    CRAFTING
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Quest.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Quest.java`  
- Size: 1374 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String id;
    private String name;
    private String description;
    private boolean repeatable;
    private int minLevel;
    private List<QuestStep> steps = new ArrayList<>();
    private QuestReward reward = new QuestReward();

    public Quest(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean repeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public List<QuestStep> steps() {
        return steps;
    }

    public void setSteps(List<QuestStep> steps) {
        this.steps = steps;
    }

    public QuestReward reward() {
        return reward;
    }

    public void setReward(QuestReward reward) {
        this.reward = reward;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestProgress.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestProgress.java`  
- Size: 1355 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class QuestProgress {
    private final String questId;
    private final Map<Integer, Integer> stepProgress = new HashMap<>();
    private boolean completed;

    public QuestProgress(String questId) {
        this.questId = questId;
    }

    public String questId() {
        return questId;
    }

    public Map<Integer, Integer> stepProgress() {
        return stepProgress;
    }

    public void incrementStep(int index, int amount) {
        stepProgress.put(index, stepProgress.getOrDefault(index, 0) + amount);
    }

    /**
     * Erhöht den Fortschritt, aber nie über "required" hinaus.
     * Damit bleibt Progress stabil, und Auswertungen werden deterministisch.
     */
    public void incrementStepClamped(int index, int amount, int required) {
        int current = stepProgress.getOrDefault(index, 0);
        int next = current + Math.max(0, amount);
        if (required > 0) {
            next = Math.min(required, next);
        }
        stepProgress.put(index, next);
    }

    public int getStepProgress(int index) {
        return stepProgress.getOrDefault(index, 0);
    }

    public boolean completed() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestReward.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestReward.java`  
- Size: 685 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class QuestReward {
    private int xp;
    private int skillPoints;
    private Map<String, Integer> factionRep = new HashMap<>();

    public int xp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int skillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public Map<String, Integer> factionRep() {
        return factionRep;
    }

    public void setFactionRep(Map<String, Integer> factionRep) {
        this.factionRep = factionRep;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestStep.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestStep.java`  
- Size: 718 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public class QuestStep {
    private QuestStepType type;
    private String target;
    private int amount;

    public QuestStep(QuestStepType type, String target, int amount) {
        this.type = type;
        this.target = target;
        this.amount = amount;
    }

    public QuestStepType type() {
        return type;
    }

    public void setType(QuestStepType type) {
        this.type = type;
    }

    public String target() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int amount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestStepType.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/QuestStepType.java`  
- Size: 156 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum QuestStepType {
    KILL,
    COLLECT,
    TALK,
    EXPLORE,
    CRAFT,
    USE_ITEM,
    DEFEND,
    ESCORT
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Rarity.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Rarity.java`  
- Size: 627 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import net.kyori.adventure.text.format.NamedTextColor;

public enum Rarity {
    COMMON(NamedTextColor.WHITE, 1.0),
    UNCOMMON(NamedTextColor.GREEN, 0.6),
    RARE(NamedTextColor.BLUE, 0.35),
    EPIC(NamedTextColor.DARK_PURPLE, 0.15),
    LEGENDARY(NamedTextColor.GOLD, 0.05);

    private final NamedTextColor color;
    private final double weight;

    Rarity(NamedTextColor color, double weight) {
        this.color = color;
        this.weight = weight;
    }

    public NamedTextColor color() {
        return color;
    }

    public double weight() {
        return weight;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/RPGStat.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/RPGStat.java`  
- Size: 130 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum RPGStat {
    STRENGTH,
    DEXTERITY,
    CONSTITUTION,
    INTELLIGENCE,
    LUCK
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ShopDefinition.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ShopDefinition.java`  
- Size: 645 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class ShopDefinition {
    private final String id;
    private String title;
    private Map<Integer, ShopItem> items = new HashMap<>();

    public ShopDefinition(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<Integer, ShopItem> items() {
        return items;
    }

    public void setItems(Map<Integer, ShopItem> items) {
        this.items = items;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ShopItem.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/ShopItem.java`  
- Size: 882 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public class ShopItem {
    private int slot;
    private String material;
    private String name;
    private int buyPrice;
    private int sellPrice;

    public int slot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public String material() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int buyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(int buyPrice) {
        this.buyPrice = buyPrice;
    }

    public int sellPrice() {
        return sellPrice;
    }

    public void setSellPrice(int sellPrice) {
        this.sellPrice = sellPrice;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Skill.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Skill.java`  
- Size: 1615 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public class Skill {
    private final String id;
    private String name;
    private SkillType type;
    private SkillCategory category;
    private int cooldown;
    private int manaCost;
    private String requiredSkill;
    private java.util.List<com.example.rpg.skill.SkillEffectConfig> effects = new java.util.ArrayList<>();

    public Skill(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SkillType type() {
        return type;
    }

    public void setType(SkillType type) {
        this.type = type;
    }

    public SkillCategory category() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public int cooldown() {
        return cooldown;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int manaCost() {
        return manaCost;
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public java.util.List<com.example.rpg.skill.SkillEffectConfig> effects() {
        return effects;
    }

    public void setEffects(java.util.List<com.example.rpg.skill.SkillEffectConfig> effects) {
        this.effects = effects;
    }

    public String requiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/SkillCategory.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/SkillCategory.java`  
- Size: 126 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum SkillCategory {
    HEALING,
    MAGIC,
    ATTACK,
    DEFENSE,
    PROFESSION
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/SkillType.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/SkillType.java`  
- Size: 82 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

public enum SkillType {
    ACTIVE,
    PASSIVE
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Spawner.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Spawner.java`  
- Size: 992 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.HashMap;
import java.util.Map;

public class Spawner {
    private final String id;
    private String zoneId;
    private int maxMobs;
    private int spawnInterval;
    private Map<String, Double> mobs = new HashMap<>();

    public Spawner(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String zoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public int maxMobs() {
        return maxMobs;
    }

    public void setMaxMobs(int maxMobs) {
        this.maxMobs = maxMobs;
    }

    public int spawnInterval() {
        return spawnInterval;
    }

    public void setSpawnInterval(int spawnInterval) {
        this.spawnInterval = spawnInterval;
    }

    public Map<String, Double> mobs() {
        return mobs;
    }

    public void setMobs(Map<String, Double> mobs) {
        this.mobs = mobs;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/TradeRequest.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/TradeRequest.java`  
- Size: 1198 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import java.util.UUID;

public class TradeRequest {
    private final UUID requester;
    private final UUID target;
    private int goldOffer;
    private int goldRequest;
    private boolean requesterReady;
    private boolean targetReady;

    public TradeRequest(UUID requester, UUID target) {
        this.requester = requester;
        this.target = target;
    }

    public UUID requester() {
        return requester;
    }

    public UUID target() {
        return target;
    }

    public int goldOffer() {
        return goldOffer;
    }

    public void setGoldOffer(int goldOffer) {
        this.goldOffer = goldOffer;
    }

    public int goldRequest() {
        return goldRequest;
    }

    public void setGoldRequest(int goldRequest) {
        this.goldRequest = goldRequest;
    }

    public boolean requesterReady() {
        return requesterReady;
    }

    public void setRequesterReady(boolean requesterReady) {
        this.requesterReady = requesterReady;
    }

    public boolean targetReady() {
        return targetReady;
    }

    public void setTargetReady(boolean targetReady) {
        this.targetReady = targetReady;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Zone.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/model/Zone.java`  
- Size: 2786 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.model;

import org.bukkit.Location;

public class Zone {
    private final String id;
    private String name;
    private String world;
    private int minLevel;
    private int maxLevel;
    private double slowMultiplier = 1.0;
    private double damageMultiplier = 1.0;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;

    public Zone(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String world() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public double slowMultiplier() {
        return slowMultiplier;
    }

    public void setSlowMultiplier(double slowMultiplier) {
        this.slowMultiplier = slowMultiplier;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public void setDamageMultiplier(double damageMultiplier) {
        this.damageMultiplier = damageMultiplier;
    }

    public void setBounds(Location pos1, Location pos2) {
        this.x1 = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.y1 = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.z1 = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.x2 = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.y2 = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.z2 = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    public boolean contains(Location location) {
        if (location == null || !location.getWorld().getName().equals(world)) {
            return false;
        }
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int z1() {
        return z1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public int z2() {
        return z2;
    }

    public void setCoordinates(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionAuditLog.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionAuditLog.java`  
- Size: 2406 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import com.example.rpg.db.DatabaseService;
import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PermissionAuditLog {
    private final DatabaseService database;
    private final Gson gson = new Gson();

    public PermissionAuditLog(DatabaseService database) {
        this.database = database;
    }

    public void log(UUID actorUuid, String actorName, String action, String target, Object before, Object after) {
        String sql = "INSERT INTO rpg_audit_log (actor_uuid, actor_name, action, target, before, after) VALUES (?, ?, ?, ?, ?::jsonb, ?::jsonb)";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, actorUuid);
            stmt.setString(2, actorName);
            stmt.setString(3, action);
            stmt.setString(4, target);
            stmt.setString(5, before != null ? gson.toJson(before) : null);
            stmt.setString(6, after != null ? gson.toJson(after) : null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to write audit log: " + e.getMessage(), e);
        }
    }

    public List<String> recent(int limit) {
        String sql = "SELECT ts, actor_name, action, target FROM rpg_audit_log ORDER BY ts DESC LIMIT ?";
        List<String> entries = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String ts = rs.getString("ts");
                    String actor = rs.getString("actor_name");
                    String action = rs.getString("action");
                    String target = rs.getString("target");
                    entries.add(ts + " | " + actor + " | " + action + " | " + target);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read audit log: " + e.getMessage(), e);
        }
        return entries;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionDecision.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionDecision.java`  
- Size: 106 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

public enum PermissionDecision {
    ALLOW,
    DENY,
    INHERIT
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionExplanation.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionExplanation.java`  
- Size: 1096 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import java.util.ArrayList;
import java.util.List;

public class PermissionExplanation {
    private final boolean allowed;
    private final String winningRole;
    private final String winningNode;
    private final PermissionDecision winningDecision;
    private final List<String> trace;

    public PermissionExplanation(boolean allowed, String winningRole, String winningNode, PermissionDecision winningDecision,
                                 List<String> trace) {
        this.allowed = allowed;
        this.winningRole = winningRole;
        this.winningNode = winningNode;
        this.winningDecision = winningDecision;
        this.trace = trace != null ? trace : new ArrayList<>();
    }

    public boolean allowed() {
        return allowed;
    }

    public String winningRole() {
        return winningRole;
    }

    public String winningNode() {
        return winningNode;
    }

    public PermissionDecision winningDecision() {
        return winningDecision;
    }

    public List<String> trace() {
        return trace;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionListener.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionListener.java`  
- Size: 724 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import com.example.rpg.RPGPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PermissionListener implements Listener {
    private final RPGPlugin plugin;

    public PermissionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.permissionService().applyAttachments(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.permissionService().removeAttachment(event.getPlayer().getUniqueId());
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionRepository.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionRepository.java`  
- Size: 402 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    List<Role> loadAllRoles();
    void saveRole(Role role);
    void deleteRole(String roleKey);
    Optional<PlayerRoles> loadPlayerRoles(UUID playerId);
    void savePlayerRoles(PlayerRoles playerRoles);
    List<PlayerRoles> listPlayerRoles();
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionResolver.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionResolver.java`  
- Size: 4200 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PermissionResolver {
    private final Map<String, Role> roles;
    private final PermissionDecision defaultDecision;

    public PermissionResolver(Map<String, Role> roles, PermissionDecision defaultDecision) {
        this.roles = roles;
        this.defaultDecision = defaultDecision;
    }

    public PermissionExplanation explain(PlayerRoles playerRoles, String node) {
        List<String> trace = new ArrayList<>();
        if (playerRoles == null) {
            return finalizeDecision(defaultDecision, null, null, trace);
        }
        Set<String> roleKeys = collectRoleKeys(playerRoles);
        DecisionResult result = resolveForRoles(roleKeys, node, trace);
        return finalizeDecision(result.decision(), result.roleKey(), result.node(), trace);
    }

    public boolean resolve(PlayerRoles playerRoles, String node) {
        return explain(playerRoles, node).allowed();
    }

    private PermissionExplanation finalizeDecision(PermissionDecision decision, String roleKey, String node, List<String> trace) {
        boolean allowed = decision == PermissionDecision.ALLOW;
        return new PermissionExplanation(allowed, roleKey, node, decision, trace);
    }

    private Set<String> collectRoleKeys(PlayerRoles playerRoles) {
        Set<String> keys = new HashSet<>();
        if (playerRoles.primaryRole() != null) {
            keys.add(playerRoles.primaryRole());
        }
        keys.addAll(playerRoles.extraRoles());
        Set<String> resolved = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>(keys);
        while (!queue.isEmpty()) {
            String key = queue.poll();
            if (!resolved.add(key)) {
                continue;
            }
            Role role = roles.get(key);
            if (role == null) {
                continue;
            }
            for (String parent : role.parents()) {
                if (!resolved.contains(parent)) {
                    queue.add(parent);
                }
            }
        }
        return resolved;
    }

    private DecisionResult resolveForRoles(Set<String> roleKeys, String node, List<String> trace) {
        PermissionDecision finalDecision = PermissionDecision.INHERIT;
        String winningRole = null;
        String winningNode = null;
        for (String roleKey : roleKeys) {
            Role role = roles.get(roleKey);
            if (role == null) {
                continue;
            }
            for (Map.Entry<String, PermissionDecision> entry : role.nodes().entrySet()) {
                String nodeKey = entry.getKey();
                PermissionDecision decision = entry.getValue();
                if (!matches(nodeKey, node)) {
                    continue;
                }
                trace.add(roleKey + " -> " + nodeKey + " = " + decision);
                if (decision == PermissionDecision.DENY) {
                    return new DecisionResult(PermissionDecision.DENY, roleKey, nodeKey);
                }
                if (decision == PermissionDecision.ALLOW && finalDecision != PermissionDecision.ALLOW) {
                    finalDecision = PermissionDecision.ALLOW;
                    winningRole = roleKey;
                    winningNode = nodeKey;
                }
            }
        }
        if (finalDecision == PermissionDecision.INHERIT) {
            finalDecision = defaultDecision;
        }
        return new DecisionResult(finalDecision, winningRole, winningNode);
    }

    private boolean matches(String rule, String node) {
        if (rule == null) {
            return false;
        }
        if (rule.equalsIgnoreCase(node)) {
            return true;
        }
        if (rule.endsWith(".*")) {
            String prefix = rule.substring(0, rule.length() - 2).toLowerCase();
            return node.toLowerCase().startsWith(prefix);
        }
        return false;
    }

    private record DecisionResult(PermissionDecision decision, String roleKey, String node) {
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionService.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PermissionService.java`  
- Size: 12885 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import com.example.rpg.db.DatabaseService;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionService {
    private final JavaPlugin plugin;
    private final PermissionRepository repository;
    private final PermissionAuditLog auditLog;
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerRoles> playerRoles = new ConcurrentHashMap<>();
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, CacheEntry>> resolvedCache = new ConcurrentHashMap<>();
    private final PermissionDecision defaultDecision;
    private final String defaultRole;
    private final boolean opBypass;
    private final boolean enabled;
    private final boolean auditEnabled;
    private final long cacheTtlMillis;

    public PermissionService(JavaPlugin plugin, DatabaseService database, boolean enabled, String defaultRole,
                             PermissionDecision defaultDecision, boolean opBypass, boolean auditEnabled, long cacheTtlSeconds) {
        this.plugin = plugin;
        this.repository = new PostgresPermissionRepository(database);
        this.auditLog = new PermissionAuditLog(database);
        this.enabled = enabled;
        this.defaultRole = defaultRole;
        this.defaultDecision = defaultDecision;
        this.opBypass = opBypass;
        this.auditEnabled = auditEnabled;
        this.cacheTtlMillis = Math.max(0, cacheTtlSeconds) * 1000L;
        reload();
    }

    public void reload() {
        roles.clear();
        for (Role role : repository.loadAllRoles()) {
            roles.put(role.key(), role);
        }
        if (roles.isEmpty()) {
            bootstrapDefaults();
        }
        resolvedCache.clear();
        playerRoles.clear();
    }

    public boolean has(Player player, String node) {
        if (player == null) {
            return false;
        }
        if (!enabled) {
            return player.hasPermission(node);
        }
        if (opBypass && player.isOp()) {
            return true;
        }
        return resolve(player.getUniqueId(), node);
    }

    public PermissionExplanation explain(Player player, String node) {
        PlayerRoles roles = getPlayerRoles(player.getUniqueId());
        PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
        return resolver.explain(roles, node);
    }

    public void applyAttachments(Player player) {
        removeAttachment(player.getUniqueId());
        if (!enabled) {
            return;
        }
        PermissionAttachment attachment = player.addAttachment(plugin);
        PlayerRoles roles = getPlayerRoles(player.getUniqueId());
        if (roles != null) {
            PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
            Map<String, PermissionDecision> decisions = collectDecisions(roles);
            for (Map.Entry<String, PermissionDecision> entry : decisions.entrySet()) {
                if (entry.getValue() == PermissionDecision.INHERIT) {
                    continue;
                }
                boolean allowed = resolver.resolve(roles, entry.getKey());
                attachment.setPermission(entry.getKey(), allowed);
            }
        }
        attachments.put(player.getUniqueId(), attachment);
    }

    public void removeAttachment(UUID playerId) {
        PermissionAttachment attachment = attachments.remove(playerId);
        if (attachment != null) {
            attachment.remove();
        }
    }

    public void createRole(Player actor, String key, String displayName) {
        Role role = new Role(key, displayName);
        roles.put(key, role);
        repository.saveRole(role);
        audit(actor, "role.create", key, null, role);
        invalidateAll();
    }

    public void renameRole(Player actor, String key, String displayName) {
        Role role = roles.get(key);
        if (role == null) {
            return;
        }
        Role before = cloneRole(role);
        role.setDisplayName(displayName);
        repository.saveRole(role);
        audit(actor, "role.rename", key, before, role);
        invalidateAll();
    }

    public void deleteRole(Player actor, String key) {
        Role before = roles.remove(key);
        repository.deleteRole(key);
        audit(actor, "role.delete", key, before, null);
        invalidateAll();
    }

    public void setRoleNode(Player actor, String roleKey, String node, PermissionDecision decision) {
        Role role = roles.get(roleKey);
        if (role == null) {
            return;
        }
        Role before = cloneRole(role);
        if (decision == PermissionDecision.INHERIT) {
            role.nodes().remove(node);
        } else {
            role.nodes().put(node, decision);
        }
        repository.saveRole(role);
        audit(actor, "role.node", roleKey + ":" + node, before, role);
        invalidateAll();
    }

    public boolean addParent(Player actor, String roleKey, String parentKey) {
        Role role = roles.get(roleKey);
        Role parent = roles.get(parentKey);
        if (role == null || parent == null) {
            return false;
        }
        if (createsCycle(roleKey, parentKey)) {
            return false;
        }
        Role before = cloneRole(role);
        role.parents().add(parentKey);
        repository.saveRole(role);
        audit(actor, "role.parent.add", roleKey + "<-" + parentKey, before, role);
        invalidateAll();
        return true;
    }

    public void removeParent(Player actor, String roleKey, String parentKey) {
        Role role = roles.get(roleKey);
        if (role == null) {
            return;
        }
        Role before = cloneRole(role);
        role.parents().remove(parentKey);
        repository.saveRole(role);
        audit(actor, "role.parent.remove", roleKey + "<-" + parentKey, before, role);
        invalidateAll();
    }

    public void assignPrimary(Player actor, UUID playerId, String roleKey) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PlayerRoles before = clonePlayerRoles(roles);
        roles.setPrimaryRole(roleKey);
        repository.savePlayerRoles(roles);
        audit(actor, "player.primary", playerId.toString(), before, roles);
        invalidatePlayer(playerId);
    }

    public void addRole(Player actor, UUID playerId, String roleKey) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PlayerRoles before = clonePlayerRoles(roles);
        roles.extraRoles().add(roleKey);
        repository.savePlayerRoles(roles);
        audit(actor, "player.role.add", playerId + ":" + roleKey, before, roles);
        invalidatePlayer(playerId);
    }

    public void removeRole(Player actor, UUID playerId, String roleKey) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PlayerRoles before = clonePlayerRoles(roles);
        roles.extraRoles().remove(roleKey);
        if (roleKey.equals(roles.primaryRole())) {
            roles.setPrimaryRole(null);
        }
        repository.savePlayerRoles(roles);
        audit(actor, "player.role.remove", playerId + ":" + roleKey, before, roles);
        invalidatePlayer(playerId);
    }

    public Map<String, Role> roles() {
        return roles;
    }

    public PlayerRoles getPlayerRoles(UUID playerId) {
        return playerRoles.computeIfAbsent(playerId, uuid -> repository.loadPlayerRoles(uuid).orElseGet(() -> {
            PlayerRoles roles = new PlayerRoles(uuid);
            roles.setPrimaryRole(getDefaultRole());
            repository.savePlayerRoles(roles);
            return roles;
        }));
    }

    public List<PlayerRoles> listPlayerRoles() {
        return repository.listPlayerRoles();
    }

    public PermissionExplanation explain(UUID playerId, String node) {
        PlayerRoles roles = getPlayerRoles(playerId);
        PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
        return resolver.explain(roles, node);
    }

    public PermissionAuditLog auditLog() {
        return auditLog;
    }

    private boolean resolve(UUID playerId, String node) {
        if (cacheTtlMillis <= 0) {
            PlayerRoles roles = getPlayerRoles(playerId);
            PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
            return resolver.resolve(roles, node);
        }
        Map<String, CacheEntry> cache = resolvedCache.computeIfAbsent(playerId, key -> new ConcurrentHashMap<>());
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(node);
        if (entry != null && entry.expiresAt() > now) {
            return entry.allowed();
        }
        PlayerRoles roles = getPlayerRoles(playerId);
        PermissionResolver resolver = new PermissionResolver(roles(), defaultDecision);
        boolean allowed = resolver.resolve(roles, node);
        cache.put(node, new CacheEntry(allowed, now + cacheTtlMillis));
        return allowed;
    }

    private void invalidateAll() {
        resolvedCache.clear();
        for (UUID uuid : attachments.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                applyAttachments(player);
            }
        }
    }

    private void invalidatePlayer(UUID playerId) {
        resolvedCache.remove(playerId);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            applyAttachments(player);
        }
    }

    private void audit(Player actor, String action, String target, Object before, Object after) {
        if (actor == null || !auditEnabled) {
            return;
        }
        auditLog.log(actor.getUniqueId(), actor.getName(), action, target, before, after);
    }

    private Map<String, PermissionDecision> collectDecisions(PlayerRoles roles) {
        Map<String, PermissionDecision> decisions = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        if (roles.primaryRole() != null) {
            queue.add(roles.primaryRole());
        }
        queue.addAll(roles.extraRoles());
        while (!queue.isEmpty()) {
            String key = queue.pop();
            if (!visited.add(key)) {
                continue;
            }
            Role role = this.roles.get(key);
            if (role == null) {
                continue;
            }
            decisions.putAll(role.nodes());
            queue.addAll(role.parents());
        }
        return decisions;
    }

    private boolean createsCycle(String roleKey, String parentKey) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>();
        stack.push(parentKey);
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (!visited.add(current)) {
                continue;
            }
            if (current.equals(roleKey)) {
                return true;
            }
            Role role = roles.get(current);
            if (role != null) {
                stack.addAll(role.parents());
            }
        }
        return false;
    }

    private Role cloneRole(Role role) {
        Role clone = new Role(role.key(), role.displayName());
        clone.parents().addAll(role.parents());
        clone.nodes().putAll(role.nodes());
        return clone;
    }

    private PlayerRoles clonePlayerRoles(PlayerRoles roles) {
        PlayerRoles clone = new PlayerRoles(roles.playerId());
        clone.setPrimaryRole(roles.primaryRole());
        clone.extraRoles().addAll(roles.extraRoles());
        return clone;
    }

    private String getDefaultRole() {
        if (defaultRole != null && roles.containsKey(defaultRole)) {
            return defaultRole;
        }
        return roles.containsKey("player") ? "player" : null;
    }

    private void bootstrapDefaults() {
        Role player = new Role("player", "Spieler");
        Role moderator = new Role("moderator", "Moderator");
        Role admin = new Role("admin", "Admin");
        admin.nodes().put("rpg.admin.*", PermissionDecision.ALLOW);
        roles.put(player.key(), player);
        roles.put(moderator.key(), moderator);
        roles.put(admin.key(), admin);
        repository.saveRole(player);
        repository.saveRole(moderator);
        repository.saveRole(admin);
    }

    private record CacheEntry(boolean allowed, long expiresAt) {
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PlayerRoles.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PlayerRoles.java`  
- Size: 647 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerRoles {
    private final UUID playerId;
    private String primaryRole;
    private final Set<String> extraRoles = new HashSet<>();

    public PlayerRoles(UUID playerId) {
        this.playerId = playerId;
    }

    public UUID playerId() {
        return playerId;
    }

    public String primaryRole() {
        return primaryRole;
    }

    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }

    public Set<String> extraRoles() {
        return extraRoles;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PostgresPermissionRepository.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/PostgresPermissionRepository.java`  
- Size: 6942 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import com.example.rpg.db.DatabaseService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class PostgresPermissionRepository implements PermissionRepository {
    private final DatabaseService database;
    private final Gson gson = new Gson();
    private final Type setType = new TypeToken<Set<String>>() {}.getType();
    private final Type mapType = new TypeToken<Map<String, PermissionDecision>>() {}.getType();

    public PostgresPermissionRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public List<Role> loadAllRoles() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT role_key, display_name, parents, nodes FROM rpg_roles";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String key = rs.getString("role_key");
                String displayName = rs.getString("display_name");
                Role role = new Role(key, displayName);
                String parentsJson = rs.getString("parents");
                String nodesJson = rs.getString("nodes");
                if (parentsJson != null && !parentsJson.isBlank()) {
                    Set<String> parents = gson.fromJson(parentsJson, setType);
                    if (parents != null) {
                        role.parents().addAll(parents);
                    }
                }
                if (nodesJson != null && !nodesJson.isBlank()) {
                    Map<String, PermissionDecision> nodes = gson.fromJson(nodesJson, mapType);
                    if (nodes != null) {
                        role.nodes().putAll(nodes);
                    }
                }
                roles.add(role);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load roles: " + e.getMessage(), e);
        }
        return roles;
    }

    @Override
    public void saveRole(Role role) {
        String sql = "INSERT INTO rpg_roles (role_key, display_name, parents, nodes) VALUES (?, ?, ?::jsonb, ?::jsonb) "
            + "ON CONFLICT (role_key) DO UPDATE SET display_name = EXCLUDED.display_name, parents = EXCLUDED.parents, nodes = EXCLUDED.nodes";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, role.key());
            stmt.setString(2, role.displayName());
            stmt.setString(3, gson.toJson(role.parents()));
            stmt.setString(4, gson.toJson(role.nodes()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save role: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRole(String roleKey) {
        String sql = "DELETE FROM rpg_roles WHERE role_key = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, roleKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete role: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<PlayerRoles> loadPlayerRoles(UUID playerId) {
        String sql = "SELECT primary_role, extra_roles FROM rpg_player_roles WHERE player_uuid = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, playerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PlayerRoles roles = new PlayerRoles(playerId);
                    roles.setPrimaryRole(rs.getString("primary_role"));
                    String extraJson = rs.getString("extra_roles");
                    if (extraJson != null && !extraJson.isBlank()) {
                        Set<String> extra = gson.fromJson(extraJson, setType);
                        if (extra != null) {
                            roles.extraRoles().addAll(extra);
                        }
                    }
                    return Optional.of(roles);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load player roles: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void savePlayerRoles(PlayerRoles playerRoles) {
        String sql = "INSERT INTO rpg_player_roles (player_uuid, primary_role, extra_roles) VALUES (?, ?, ?::jsonb) "
            + "ON CONFLICT (player_uuid) DO UPDATE SET primary_role = EXCLUDED.primary_role, extra_roles = EXCLUDED.extra_roles";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, playerRoles.playerId());
            stmt.setString(2, playerRoles.primaryRole());
            stmt.setString(3, gson.toJson(playerRoles.extraRoles()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save player roles: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PlayerRoles> listPlayerRoles() {
        List<PlayerRoles> list = new ArrayList<>();
        String sql = "SELECT player_uuid, primary_role, extra_roles FROM rpg_player_roles";
        try (Connection connection = database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                UUID uuid = rs.getObject("player_uuid", java.util.UUID.class);
                PlayerRoles roles = new PlayerRoles(uuid);
                roles.setPrimaryRole(rs.getString("primary_role"));
                String extraJson = rs.getString("extra_roles");
                if (extraJson != null && !extraJson.isBlank()) {
                    Set<String> extra = gson.fromJson(extraJson, setType);
                    if (extra != null) {
                        roles.extraRoles().addAll(extra);
                    }
                }
                list.add(roles);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list player roles: " + e.getMessage(), e);
        }
        return list;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/Role.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/permissions/Role.java`  
- Size: 842 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.permissions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Role {
    private final String key;
    private String displayName;
    private final Set<String> parents = new HashSet<>();
    private final Map<String, PermissionDecision> nodes = new HashMap<>();

    public Role(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() {
        return key;
    }

    public String displayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<String> parents() {
        return parents;
    }

    public Map<String, PermissionDecision> nodes() {
        return nodes;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/RPGPlugin.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/RPGPlugin.java`  
- Size: 26823 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg;

import com.example.rpg.command.PartyCommand;
import com.example.rpg.command.RPGAdminCommand;
import com.example.rpg.command.RPGCommand;
import com.example.rpg.command.AuctionCommand;
import com.example.rpg.command.DungeonCommand;
import com.example.rpg.command.GuildCommand;
import com.example.rpg.command.PvpCommand;
import com.example.rpg.command.TradeCommand;
import com.example.rpg.command.BehaviorCommand;
import com.example.rpg.command.LootChatCommand;
import com.example.rpg.command.VoiceChatCommand;
import com.example.rpg.db.DatabaseService;
import com.example.rpg.db.PlayerDao;
import com.example.rpg.db.SqlPlayerDao;
import com.example.rpg.gui.BehaviorTreeEditorGui;
import com.example.rpg.gui.GuiManager;
import com.example.rpg.gui.SkillTreeGui;
import com.example.rpg.listener.BehaviorEditorListener;
import com.example.rpg.listener.ArenaListener;
import com.example.rpg.listener.CombatListener;
import com.example.rpg.listener.CustomMobListener;
import com.example.rpg.listener.DamageIndicatorListener;
import com.example.rpg.listener.GuiListener;
import com.example.rpg.listener.ItemStatListener;
import com.example.rpg.listener.BuildingPlacementListener;
import com.example.rpg.listener.NpcListener;
import com.example.rpg.listener.NpcProtectionListener;
import com.example.rpg.listener.PlayerListener;
import com.example.rpg.listener.ProfessionListener;
import com.example.rpg.listener.SkillHotbarListener;
import com.example.rpg.listener.ZoneListener;
import com.example.rpg.manager.ArenaManager;
import com.example.rpg.manager.BehaviorTreeManager;
import com.example.rpg.manager.ClassManager;
import com.example.rpg.manager.DungeonManager;
import com.example.rpg.manager.GuildManager;
import com.example.rpg.manager.FactionManager;
import com.example.rpg.manager.ItemStatManager;
import com.example.rpg.manager.LootManager;
import com.example.rpg.manager.MobManager;
import com.example.rpg.manager.NpcManager;
import com.example.rpg.manager.PartyManager;
import com.example.rpg.manager.PlayerDataManager;
import com.example.rpg.manager.SkillTreeManager;
import com.example.rpg.manager.QuestManager;
import com.example.rpg.manager.AuctionHouseManager;
import com.example.rpg.manager.ShopManager;
import com.example.rpg.manager.SkillHotbarManager;
import com.example.rpg.manager.SkillManager;
import com.example.rpg.manager.SpawnerManager;
import com.example.rpg.manager.ZoneManager;
import com.example.rpg.manager.TradeManager;
import com.example.rpg.manager.ProfessionManager;
import com.example.rpg.manager.BuildingManager;
import com.example.rpg.manager.VoiceChatManager;
import com.example.rpg.util.ItemGenerator;
import com.example.rpg.util.AuditLog;
import com.example.rpg.util.PromptManager;
import com.example.rpg.permissions.PermissionService;
import com.example.rpg.permissions.PermissionDecision;
import com.example.rpg.permissions.PermissionListener;
import com.example.rpg.skill.SkillEffectRegistry;
import com.example.rpg.skill.SkillEffectType;
import com.example.rpg.skill.effects.DamageEffect;
import com.example.rpg.skill.effects.HealEffect;
import com.example.rpg.skill.effects.ParticleEffect;
import com.example.rpg.skill.effects.PotionStatusEffect;
import com.example.rpg.skill.effects.ProjectileEffect;
import com.example.rpg.skill.effects.SoundEffect;
import com.example.rpg.skill.effects.VelocityEffect;
import com.example.rpg.skill.effects.AggroEffect;
import com.example.rpg.skill.effects.XpEffect;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RPGPlugin extends JavaPlugin {
    private DatabaseService databaseService;
    private PlayerDataManager playerDataManager;
    private QuestManager questManager;
    private ZoneManager zoneManager;
    private NpcManager npcManager;
    private LootManager lootManager;
    private MobManager mobManager;
    private SkillManager skillManager;
    private SkillHotbarManager skillHotbarManager;
    private ClassManager classManager;
    private SpawnerManager spawnerManager;
    private ShopManager shopManager;
    private AuctionHouseManager auctionHouseManager;
    private TradeManager tradeManager;
    private ProfessionManager professionManager;
    private DungeonManager dungeonManager;
    private ArenaManager arenaManager;
    private BehaviorTreeManager behaviorTreeManager;
    private GuildManager guildManager;
    private FactionManager factionManager;
    private PartyManager partyManager;
    private GuiManager guiManager;
    private SkillTreeGui skillTreeGui;
    private SkillTreeManager skillTreeManager;
    private ItemStatManager itemStatManager;
    private BehaviorTreeEditorGui behaviorTreeEditorGui;
    private VoiceChatManager voiceChatManager;
    private PermissionService permissionService;
    private BuildingManager buildingManager;
    private PromptManager promptManager;
    private ItemGenerator itemGenerator;
    private SkillEffectRegistry skillEffects;
    private final Set<UUID> debugPlayers = new HashSet<>();
    private CustomMobListener customMobListener;
    private final java.util.Map<UUID, Long> actionBarErrorUntil = new java.util.HashMap<>();
    private final java.util.Map<UUID, String> actionBarErrorMessage = new java.util.HashMap<>();
    private NamespacedKey questKey;
    private NamespacedKey skillKey;
    private NamespacedKey wandKey;
    private NamespacedKey buildingKey;
    private NamespacedKey buildingCategoryKey;
    private NamespacedKey permRoleKey;
    private NamespacedKey permPlayerKey;
    private NamespacedKey permNodeKey;
    private NamespacedKey permActionKey;
    private AuditLog auditLog;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseService = new DatabaseService(this);
        databaseService.initTables();
        PlayerDao playerDao = new SqlPlayerDao(databaseService);
        playerDataManager = new PlayerDataManager(this, playerDao);
        questManager = new QuestManager(this);
        zoneManager = new ZoneManager(this);
        npcManager = new NpcManager(this);
        lootManager = new LootManager(this);
        mobManager = new MobManager(this);
        behaviorTreeManager = new BehaviorTreeManager(this);
        voiceChatManager = new VoiceChatManager(this);
        skillManager = new SkillManager(this);
        skillHotbarManager = new SkillHotbarManager(playerDataManager);
        classManager = new ClassManager(this);
        factionManager = new FactionManager(this);
        spawnerManager = new SpawnerManager(this);
        shopManager = new ShopManager(this);
        auctionHouseManager = new AuctionHouseManager(this);
        tradeManager = new TradeManager();
        professionManager = new ProfessionManager(this);
        dungeonManager = new DungeonManager(this);
        arenaManager = new ArenaManager(this);
        guildManager = new GuildManager(this);
        partyManager = new PartyManager();
        promptManager = new PromptManager();
        itemStatManager = new ItemStatManager(this);
        itemGenerator = new ItemGenerator(this, itemStatManager);
        buildingManager = new BuildingManager(this);
        questKey = new NamespacedKey(this, "quest_id");
        skillKey = new NamespacedKey(this, "skill_id");
        wandKey = new NamespacedKey(this, "editor_wand");
        buildingKey = new NamespacedKey(this, "building_id");
        buildingCategoryKey = new NamespacedKey(this, "building_category");
        permRoleKey = new NamespacedKey(this, "perm_role");
        permPlayerKey = new NamespacedKey(this, "perm_player");
        permNodeKey = new NamespacedKey(this, "perm_node");
        permActionKey = new NamespacedKey(this, "perm_action");
        skillEffects = new SkillEffectRegistry()
            .register(SkillEffectType.HEAL, new HealEffect())
            .register(SkillEffectType.DAMAGE, new DamageEffect())
            .register(SkillEffectType.PROJECTILE, new ProjectileEffect())
            .register(SkillEffectType.POTION, new PotionStatusEffect())
            .register(SkillEffectType.SOUND, new SoundEffect())
            .register(SkillEffectType.XP, new XpEffect())
            .register(SkillEffectType.PARTICLE, new ParticleEffect())
            .register(SkillEffectType.VELOCITY, new VelocityEffect())
            .register(SkillEffectType.AGGRO, new AggroEffect());
        skillTreeManager = new SkillTreeManager(skillManager);
        skillTreeGui = new SkillTreeGui(this);
        behaviorTreeEditorGui = new BehaviorTreeEditorGui(this);
        auditLog = new AuditLog(this);
        permissionService = new PermissionService(this, databaseService,
            getConfig().getBoolean("permissions.enabled", true),
            getConfig().getString("permissions.defaultRole", "player"),
            PermissionDecision.valueOf(getConfig().getString("permissions.defaultDecision", "DENY")),
            getConfig().getBoolean("permissions.opBypass", true),
            getConfig().getBoolean("permissions.auditEnabled", true),
            getConfig().getLong("permissions.cacheTtlSeconds", 30));
        guiManager = new GuiManager(playerDataManager, questManager, skillManager, classManager, factionManager, buildingManager,
            permissionService, questKey, skillKey, buildingKey, buildingCategoryKey, permRoleKey, permPlayerKey, permNodeKey, permActionKey);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ZoneListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DamageIndicatorListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NpcProtectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SkillHotbarListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ProfessionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemStatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArenaListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BehaviorEditorListener(this, behaviorTreeEditorGui), this);
        Bukkit.getPluginManager().registerEvents(new BuildingPlacementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PermissionListener(this), this);
        customMobListener = new CustomMobListener(this);
        Bukkit.getPluginManager().registerEvents(customMobListener, this);

        getCommand("rpg").setExecutor(new RPGCommand(this));
        getCommand("rpgadmin").setExecutor(new RPGAdminCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));
        getCommand("p").setExecutor(new PartyCommand(this));
        getCommand("auction").setExecutor(new AuctionCommand(this));
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("dungeon").setExecutor(new DungeonCommand(this));
        getCommand("guild").setExecutor(new GuildCommand(this));
        getCommand("g").setExecutor(new GuildCommand(this));
        getCommand("pvp").setExecutor(new PvpCommand(this));
        getCommand("behavior").setExecutor(new BehaviorCommand(this));
        getCommand("lootchat").setExecutor(new LootChatCommand(this));
        getCommand("voicechat").setExecutor(new VoiceChatCommand(this));

        npcManager.spawnAll();
        startDebugTask();
        startManaRegenTask();
        startHudTask();
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (questManager != null) {
            questManager.saveAll();
        }
        if (zoneManager != null) {
            zoneManager.saveAll();
        }
        if (npcManager != null) {
            npcManager.saveAll();
        }
        if (lootManager != null) {
            lootManager.saveAll();
        }
        if (mobManager != null) {
            mobManager.saveAll();
        }
        if (skillManager != null) {
            skillManager.saveAll();
        }
        if (classManager != null) {
            classManager.saveAll();
        }
        if (factionManager != null) {
            factionManager.saveAll();
        }
        if (spawnerManager != null) {
            spawnerManager.saveAll();
        }
        if (shopManager != null) {
            shopManager.saveAll();
        }
        if (auctionHouseManager != null) {
            auctionHouseManager.listings().values().forEach(auctionHouseManager::saveListing);
        }
        if (guildManager != null) {
            guildManager.saveAll();
        }
        if (dungeonManager != null) {
            getLogger().info("Cleaning up dungeon worlds...");
            dungeonManager.shutdown();
        }
        if (databaseService != null) {
            databaseService.shutdown();
        }
    }

    public PlayerDataManager playerDataManager() {
        return playerDataManager;
    }

    public QuestManager questManager() {
        return questManager;
    }

    public ZoneManager zoneManager() {
        return zoneManager;
    }

    public NpcManager npcManager() {
        return npcManager;
    }

    public LootManager lootManager() {
        return lootManager;
    }

    public MobManager mobManager() {
        return mobManager;
    }

    public BehaviorTreeManager behaviorTreeManager() {
        return behaviorTreeManager;
    }

    public SkillManager skillManager() {
        return skillManager;
    }

    public SkillHotbarManager skillHotbarManager() {
        return skillHotbarManager;
    }

    public ClassManager classManager() {
        return classManager;
    }

    public FactionManager factionManager() {
        return factionManager;
    }

    public PartyManager partyManager() {
        return partyManager;
    }

    public GuiManager guiManager() {
        return guiManager;
    }

    public PromptManager promptManager() {
        return promptManager;
    }

    public ItemGenerator itemGenerator() {
        return itemGenerator;
    }

    public SkillEffectRegistry skillEffects() {
        return skillEffects;
    }

    public SpawnerManager spawnerManager() {
        return spawnerManager;
    }

    public ShopManager shopManager() {
        return shopManager;
    }

    public AuctionHouseManager auctionHouseManager() {
        return auctionHouseManager;
    }

    public TradeManager tradeManager() {
        return tradeManager;
    }

    public ProfessionManager professionManager() {
        return professionManager;
    }

    public DungeonManager dungeonManager() {
        return dungeonManager;
    }

    public ArenaManager arenaManager() {
        return arenaManager;
    }

    public GuildManager guildManager() {
        return guildManager;
    }

    public SkillTreeManager skillTreeManager() {
        return skillTreeManager;
    }

    public SkillTreeGui skillTreeGui() {
        return skillTreeGui;
    }

    public ItemStatManager itemStatManager() {
        return itemStatManager;
    }

    public BehaviorTreeEditorGui behaviorTreeEditorGui() {
        return behaviorTreeEditorGui;
    }

    public VoiceChatManager voiceChatManager() {
        return voiceChatManager;
    }

    public PermissionService permissionService() {
        return permissionService;
    }

    public BuildingManager buildingManager() {
        return buildingManager;
    }

    public CustomMobListener customMobListener() {
        return customMobListener;
    }

    public AuditLog auditLog() {
        return auditLog;
    }

    public NamespacedKey questKey() {
        return questKey;
    }

    public NamespacedKey skillKey() {
        return skillKey;
    }

    public NamespacedKey wandKey() {
        return wandKey;
    }

    public NamespacedKey buildingKey() {
        return buildingKey;
    }

    public NamespacedKey buildingCategoryKey() {
        return buildingCategoryKey;
    }

    public NamespacedKey permRoleKey() {
        return permRoleKey;
    }

    public NamespacedKey permPlayerKey() {
        return permPlayerKey;
    }

    public NamespacedKey permNodeKey() {
        return permNodeKey;
    }

    public NamespacedKey permActionKey() {
        return permActionKey;
    }

    public void broadcastLoot(Player player, org.bukkit.inventory.ItemStack item) {
        if (!getConfig().getBoolean("lootchat.enabled", true)) {
            return;
        }
        String name = item.getType().name().toLowerCase().replace("_", " ");
        String message = "<gold>" + player.getName() + "</gold> hat <yellow>" + item.getAmount()
            + "x " + name + "</yellow> gelootet.";
        getServer().broadcast(com.example.rpg.util.Text.mm(message));
    }

    public boolean useSkill(Player player, String skillId) {
        var skill = skillManager.getSkill(skillId);
        if (skill == null) {
            notifySkillError(player, "Unbekannter Skill");
            return false;
        }
        var profile = playerDataManager.getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            notifySkillError(player, "Skill nicht gelernt");
            return false;
        }
        if (skill.type() == com.example.rpg.model.SkillType.PASSIVE) {
            notifySkillError(player, "Passiver Skill ist aktiv");
            return false;
        }
        long now = System.currentTimeMillis();
        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
        if (now - last < skill.cooldown() * 1000L) {
            long remaining = (skill.cooldown() * 1000L - (now - last)) / 1000L;
            notifySkillError(player, "Cooldown: " + remaining + "s");
            return false;
        }
        if (profile.mana() < skill.manaCost()) {
            notifySkillError(player, "Nicht genug Mana");
            return false;
        }
        profile.setMana(profile.mana() - skill.manaCost());
        for (var effect : skill.effects()) {
            skillEffects.apply(effect, player, profile);
        }
        profile.skillCooldowns().put(skillId, now);
        player.sendMessage("§aSkill benutzt: " + skill.name());
        return true;
    }

    public boolean useMobSkill(org.bukkit.entity.LivingEntity caster, Player target, String skillId) {
        var skill = skillManager.getSkill(skillId);
        if (skill == null) {
            return false;
        }
        for (var effect : skill.effects()) {
            switch (effect.type()) {
                case DAMAGE -> {
                    double amount = parseDouble(effect.params().getOrDefault("amount", 4));
                    target.damage(amount, caster);
                }
                case PROJECTILE -> {
                    String type = String.valueOf(effect.params().getOrDefault("type", "SNOWBALL")).toUpperCase();
                    if ("SMALL_FIREBALL".equals(type)) {
                        caster.launchProjectile(org.bukkit.entity.SmallFireball.class);
                    } else {
                        caster.launchProjectile(org.bukkit.entity.Snowball.class);
                    }
                }
                case POTION -> {
                    String type = String.valueOf(effect.params().getOrDefault("type", "SLOW")).toUpperCase();
                    int duration = (int) parseDouble(effect.params().getOrDefault("duration", 60));
                    int amplifier = (int) parseDouble(effect.params().getOrDefault("amplifier", 0));
                    var potion = org.bukkit.potion.PotionEffectType.getByName(type);
                    if (potion != null) {
                        target.addPotionEffect(new org.bukkit.potion.PotionEffect(potion, duration, amplifier));
                    }
                }
                case PARTICLE -> {
                    String particleName = String.valueOf(effect.params().getOrDefault("type", "SMOKE")).toUpperCase();
                    int count = (int) parseDouble(effect.params().getOrDefault("count", 10));
                    double speed = parseDouble(effect.params().getOrDefault("speed", 0.01));
                    org.bukkit.Particle particle;
                    try {
                        particle = org.bukkit.Particle.valueOf(particleName);
                    } catch (IllegalArgumentException e) {
                        particle = org.bukkit.Particle.SMOKE_NORMAL;
                    }
                    caster.getWorld().spawnParticle(particle, caster.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
                }
                case SOUND -> {
                    String soundName = String.valueOf(effect.params().getOrDefault("sound", "ENTITY_ZOMBIE_HURT")).toUpperCase();
                    float volume = (float) parseDouble(effect.params().getOrDefault("volume", 1.0));
                    float pitch = (float) parseDouble(effect.params().getOrDefault("pitch", 1.0));
                    org.bukkit.Sound sound;
                    try {
                        sound = org.bukkit.Sound.valueOf(soundName);
                    } catch (IllegalArgumentException e) {
                        sound = org.bukkit.Sound.ENTITY_ZOMBIE_HURT;
                    }
                    caster.getWorld().playSound(caster.getLocation(), sound, volume, pitch);
                }
                default -> {
                }
            }
        }
        return true;
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public void notifySkillError(Player player, String message) {
        actionBarErrorUntil.put(player.getUniqueId(), System.currentTimeMillis() + 2000L);
        actionBarErrorMessage.put(player.getUniqueId(), message);
        player.sendActionBar("§c" + message);
    }

    private void startHudTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = playerDataManager.getProfile(player);
                String health = String.format("❤ Leben: %.0f/%.0f", player.getHealth(),
                    player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null
                        ? player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                        : 20.0);
                String mana = "🔵 Mana: " + profile.mana() + "/" + profile.maxMana();
                String gold = "💰 " + profile.gold();
                Long until = actionBarErrorUntil.get(player.getUniqueId());
                if (until != null && until > System.currentTimeMillis()) {
                    String msg = actionBarErrorMessage.getOrDefault(player.getUniqueId(), "Fehler");
                    player.sendActionBar("§c" + msg);
                } else {
                    player.sendActionBar("§f" + health + " §7| §f" + mana + " §7| §6" + gold);
                }

                int slot = player.getInventory().getHeldItemSlot() + 1;
                String skillId = skillHotbarManager.getBinding(profile, slot);
                if (skillId != null) {
                    var skill = skillManager.getSkill(skillId);
                    if (skill != null && skill.cooldown() > 0) {
                        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
                        long remaining = skill.cooldown() * 1000L - (System.currentTimeMillis() - last);
                        if (remaining > 0) {
                            float progress = Math.max(0f, Math.min(1f, remaining / (skill.cooldown() * 1000f)));
                            player.setExp(progress);
                            player.setLevel((int) Math.ceil(remaining / 1000f));
                        } else {
                            player.setExp(0f);
                            player.setLevel(profile.level());
                        }
                    }
                }
            }
        }, 10L, 10L);
    }

    public boolean toggleDebug(UUID uuid) {
        if (debugPlayers.contains(uuid)) {
            debugPlayers.remove(uuid);
            return false;
        }
        debugPlayers.add(uuid);
        return true;
    }

    private void startDebugTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : debugPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) {
                    continue;
                }
                var zone = zoneManager.getZoneAt(player.getLocation());
                String zoneName = zone != null ? zone.name() : "Keine Zone";
                player.sendActionBar("§7Zone: §f" + zoneName + " §7Quest: §f" + playerDataManager.getProfile(player).activeQuests().size());
            }
        }, 20L, 40L);
    }

    private void startManaRegenTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                var profile = playerDataManager.getProfile(player);
                profile.setMana(Math.min(profile.maxMana(), profile.mana() + 5));
            }
        }, 20L, 40L);
    }

    public boolean completeQuestIfReady(Player player, com.example.rpg.model.Quest quest, com.example.rpg.model.QuestProgress progress) {
        if (progress.completed()) {
            return false;
        }
        boolean done = true;
        for (int i = 0; i < quest.steps().size(); i++) {
            int required = quest.steps().get(i).amount();
            if (progress.getStepProgress(i) < required) {
                done = false;
                break;
            }
        }
        if (!done) {
            return false;
        }
        progress.setCompleted(true);
        var profile = playerDataManager.getProfile(player);
        profile.completedQuests().add(quest.id());
        profile.activeQuests().remove(quest.id());
        profile.addXp(quest.reward().xp());
        profile.setSkillPoints(profile.skillPoints() + quest.reward().skillPoints());
        quest.reward().factionRep().forEach((id, amount) ->
            profile.factionRep().put(id, profile.factionRep().getOrDefault(id, 0) + amount)
        );
        player.sendMessage("§aQuest abgeschlossen: " + quest.name());
        return true;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/BlockEntityApplier.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/BlockEntityApplier.java`  
- Size: 5233 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import net.kyori.adventure.text.Component;
import org.bukkit.Nameable;

public class BlockEntityApplier {
    private final Logger logger;

    public BlockEntityApplier(Logger logger) {
        this.logger = logger;
    }

    public void apply(World world, Location origin, NbtCompound nbt, Transform transform, int width, int length) {
        int[] pos = readPos(nbt);
        if (pos == null || pos.length < 3) {
            return;
        }
        int[] transformed = transform.apply(pos[0], pos[1], pos[2], width, length);
        Location location = origin.clone().add(transformed[0], transformed[1], transformed[2]);
        Block block = world.getBlockAt(location);
        BlockState state = block.getState();
        if (state instanceof Sign sign) {
            applySign(sign, nbt);
        } else if (state instanceof CreatureSpawner spawner) {
            applySpawner(spawner, nbt);
        } else if (state instanceof Chest chest) {
            applyContainerName(chest, nbt);
        } else if (state instanceof Barrel barrel) {
            applyContainerName(barrel, nbt);
        }
    }

    private void applySign(Sign sign, NbtCompound nbt) {
        String[] lines = new String[4];
        boolean hasLine = false;
        for (int i = 0; i < 4; i++) {
            String key = "Text" + (i + 1);
            String value = nbt.getString(key, null);
            if (value != null) {
                lines[i] = parseSignText(value);
                hasLine = true;
            }
        }
        NbtCompound frontText = nbt.getCompound("front_text");
        if (frontText != null) {
            NbtList messages = frontText.getList("messages");
            if (messages != null) {
                List<String> msgList = messages.strings();
                for (int i = 0; i < Math.min(4, msgList.size()); i++) {
                    lines[i] = parseSignText(msgList.get(i));
                    hasLine = true;
                }
            }
        }
        if (!hasLine) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (lines[i] != null) {
                sign.setLine(i, lines[i]);
            }
        }
        sign.update(true, false);
    }

    private void applySpawner(CreatureSpawner spawner, NbtCompound nbt) {
        NbtCompound spawnData = nbt.getCompound("SpawnData");
        String id = null;
        if (spawnData != null) {
            id = spawnData.getString("id", null);
            if (id == null) {
                NbtCompound entity = spawnData.getCompound("entity");
                if (entity != null) {
                    id = entity.getString("id", null);
                }
            }
        }
        if (id == null) {
            id = nbt.getString("EntityId", null);
        }
        if (id == null) {
            return;
        }
        EntityType type = EntityType.fromName(stripNamespace(id));
        if (type != null) {
            spawner.setSpawnedType(type);
            spawner.update(true, false);
        }
    }

    private void applyContainerName(BlockState container, NbtCompound nbt) {
        String name = nbt.getString("CustomName", null);
        if (name == null) {
            return;
        }
        if (container instanceof Nameable nameable) {
            nameable.customName(Component.text(parseSignText(name)));
            container.update(true, false);
        }
    }

    private int[] readPos(NbtCompound nbt) {
        int[] pos = nbt.getIntArray("Pos");
        if (pos != null) {
            return pos;
        }
        NbtList list = nbt.getList("Pos");
        if (list != null) {
            List<Double> values = list.doubles();
            if (values.size() >= 3) {
                return new int[]{values.get(0).intValue(), values.get(1).intValue(), values.get(2).intValue()};
            }
        }
        return null;
    }

    private String parseSignText(String raw) {
        if (raw == null) {
            return "";
        }
        if (raw.startsWith("{") && raw.contains("\"text\"")) {
            int start = raw.indexOf("\"text\"");
            int colon = raw.indexOf(':', start);
            int firstQuote = raw.indexOf('"', colon + 1);
            if (firstQuote >= 0) {
                int secondQuote = raw.indexOf('"', firstQuote + 1);
                if (secondQuote > firstQuote) {
                    return raw.substring(firstQuote + 1, secondQuote);
                }
            }
        }
        return raw.replace('"', ' ').trim();
    }

    private String stripNamespace(String id) {
        if (id == null) {
            return null;
        }
        if (id.contains(":")) {
            return id.substring(id.indexOf(':') + 1);
        }
        return id;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/BlockPalette.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/BlockPalette.java`  
- Size: 394 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import java.util.Map;

public class BlockPalette {
    private final Map<Integer, String> idToState;

    public BlockPalette(Map<Integer, String> idToState) {
        this.idToState = Map.copyOf(idToState);
    }

    public String getState(int id) {
        return idToState.get(id);
    }

    public int size() {
        return idToState.size();
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/EntitySpawner.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/EntitySpawner.java`  
- Size: 3487 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class EntitySpawner {
    private final Logger logger;

    public EntitySpawner(Logger logger) {
        this.logger = logger;
    }

    public void spawn(World world, Location origin, NbtCompound nbt, Transform transform, int width, int length) {
        String id = nbt.getString("Id", null);
        if (id == null) {
            id = nbt.getString("id", null);
        }
        if (id == null) {
            return;
        }
        EntityType type = EntityType.fromName(stripNamespace(id));
        if (type == null) {
            logger.warning("Unsupported entity type: " + id);
            return;
        }
        Location location = resolvePosition(origin, nbt, transform, width, length);
        if (location == null) {
            return;
        }
        Entity entity = world.spawnEntity(location, type);
        if (entity instanceof ArmorStand armorStand) {
            applyArmorStand(armorStand, nbt);
        } else if (entity instanceof ItemFrame itemFrame) {
            applyItemFrame(itemFrame, nbt);
        }
    }

    private Location resolvePosition(Location origin, NbtCompound nbt, Transform transform, int width, int length) {
        NbtList posList = nbt.getList("Pos");
        if (posList == null) {
            return null;
        }
        List<Double> coords = posList.doubles();
        if (coords.size() < 3) {
            return null;
        }
        int x = coords.get(0).intValue();
        int y = coords.get(1).intValue();
        int z = coords.get(2).intValue();
        int[] transformed = transform.apply(x, y, z, width, length);
        return origin.clone().add(transformed[0], transformed[1], transformed[2]);
    }

    private void applyArmorStand(ArmorStand armorStand, NbtCompound nbt) {
        armorStand.setSmall(nbt.getInt("Small", 0) == 1);
        armorStand.setInvisible(nbt.getInt("Invisible", 0) == 1);
        armorStand.setArms(nbt.getInt("ShowArms", 0) == 1);
        String customName = nbt.getString("CustomName", null);
        if (customName != null && !customName.isBlank()) {
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(customName);
        }
    }

    private void applyItemFrame(ItemFrame itemFrame, NbtCompound nbt) {
        NbtCompound item = nbt.getCompound("Item");
        if (item == null) {
            return;
        }
        String id = item.getString("id", null);
        if (id == null) {
            return;
        }
        Material material = Material.matchMaterial(stripNamespace(id).toUpperCase());
        if (material == null) {
            logger.warning("Unknown item frame item: " + id);
            return;
        }
        int count = item.getInt("Count", 1);
        itemFrame.setItem(new ItemStack(material, Math.max(1, count)));
    }

    private String stripNamespace(String id) {
        if (id == null) {
            return null;
        }
        if (id.contains(":")) {
            return id.substring(id.indexOf(':') + 1);
        }
        return id;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtByte.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtByte.java`  
- Size: 172 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtByte(byte value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.BYTE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtByteArray.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtByteArray.java`  
- Size: 185 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtByteArray(byte[] value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.BYTE_ARRAY;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtCompound.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtCompound.java`  
- Size: 2892 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NbtCompound implements NbtTag {
    private final Map<String, NbtTag> values = new HashMap<>();

    @Override
    public byte typeId() {
        return NbtType.COMPOUND;
    }

    public void put(String name, NbtTag tag) {
        values.put(name, tag);
    }

    public Map<String, NbtTag> values() {
        return Collections.unmodifiableMap(values);
    }

    public NbtTag get(String name) {
        return values.get(name);
    }

    public String getString(String name, String fallback) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtString str) {
            return str.value();
        }
        return fallback;
    }

    public int getInt(String name, int fallback) {
        Number number = getNumber(name);
        return number != null ? number.intValue() : fallback;
    }

    public long getLong(String name, long fallback) {
        Number number = getNumber(name);
        return number != null ? number.longValue() : fallback;
    }

    public double getDouble(String name, double fallback) {
        Number number = getNumber(name);
        return number != null ? number.doubleValue() : fallback;
    }

    public Number getNumber(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtByte b) {
            return b.value();
        }
        if (tag instanceof NbtShort s) {
            return s.value();
        }
        if (tag instanceof NbtInt i) {
            return i.value();
        }
        if (tag instanceof NbtLong l) {
            return l.value();
        }
        if (tag instanceof NbtFloat f) {
            return f.value();
        }
        if (tag instanceof NbtDouble d) {
            return d.value();
        }
        return null;
    }

    public NbtCompound getCompound(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtCompound compound) {
            return compound;
        }
        return null;
    }

    public NbtList getList(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtList list) {
            return list;
        }
        return null;
    }

    public byte[] getByteArray(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtByteArray array) {
            return array.value();
        }
        return null;
    }

    public int[] getIntArray(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtIntArray array) {
            return array.value();
        }
        return null;
    }

    public long[] getLongArray(String name) {
        NbtTag tag = values.get(name);
        if (tag instanceof NbtLongArray array) {
            return array.value();
        }
        return null;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtDouble.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtDouble.java`  
- Size: 178 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtDouble(double value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.DOUBLE;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtFloat.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtFloat.java`  
- Size: 175 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtFloat(float value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.FLOAT;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtInt.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtInt.java`  
- Size: 169 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtInt(int value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.INT;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtIntArray.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtIntArray.java`  
- Size: 182 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtIntArray(int[] value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.INT_ARRAY;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtList.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtList.java`  
- Size: 1778 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NbtList implements NbtTag {
    private final byte elementType;
    private final List<NbtTag> values;

    public NbtList(byte elementType, List<NbtTag> values) {
        this.elementType = elementType;
        this.values = new ArrayList<>(values);
    }

    @Override
    public byte typeId() {
        return NbtType.LIST;
    }

    public byte elementType() {
        return elementType;
    }

    public List<NbtTag> values() {
        return Collections.unmodifiableList(values);
    }

    public int size() {
        return values.size();
    }

    public NbtTag get(int index) {
        return values.get(index);
    }

    public List<NbtCompound> compounds() {
        List<NbtCompound> result = new ArrayList<>();
        for (NbtTag tag : values) {
            if (tag instanceof NbtCompound compound) {
                result.add(compound);
            }
        }
        return result;
    }

    public List<String> strings() {
        List<String> result = new ArrayList<>();
        for (NbtTag tag : values) {
            if (tag instanceof NbtString str) {
                result.add(str.value());
            }
        }
        return result;
    }

    public List<Double> doubles() {
        List<Double> result = new ArrayList<>();
        for (NbtTag tag : values) {
            if (tag instanceof NbtDouble dbl) {
                result.add(dbl.value());
            } else if (tag instanceof NbtFloat fl) {
                result.add((double) fl.value());
            } else if (tag instanceof NbtInt i) {
                result.add((double) i.value());
            }
        }
        return result;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtLong.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtLong.java`  
- Size: 172 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtLong(long value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.LONG;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtLongArray.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtLongArray.java`  
- Size: 185 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtLongArray(long[] value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.LONG_ARRAY;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtShort.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtShort.java`  
- Size: 175 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtShort(short value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.SHORT;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtString.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtString.java`  
- Size: 178 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public record NbtString(String value) implements NbtTag {
    @Override
    public byte typeId() {
        return NbtType.STRING;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtTag.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtTag.java`  
- Size: 87 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public interface NbtTag {
    byte typeId();
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtType.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/nbt/NbtType.java`  
- Size: 637 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic.nbt;

public final class NbtType {
    public static final byte END = 0;
    public static final byte BYTE = 1;
    public static final byte SHORT = 2;
    public static final byte INT = 3;
    public static final byte LONG = 4;
    public static final byte FLOAT = 5;
    public static final byte DOUBLE = 6;
    public static final byte BYTE_ARRAY = 7;
    public static final byte STRING = 8;
    public static final byte LIST = 9;
    public static final byte COMPOUND = 10;
    public static final byte INT_ARRAY = 11;
    public static final byte LONG_ARRAY = 12;

    private NbtType() {
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/NbtIO.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/NbtIO.java`  
- Size: 4056 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public final class NbtIO {
    private NbtIO() {
    }

    public static NbtCompound read(File file) throws IOException {
        try (InputStream input = openInputStream(file);
             DataInputStream data = new DataInputStream(input)) {
            byte type = data.readByte();
            if (type != NbtType.COMPOUND) {
                throw new IOException("Root tag is not a compound");
            }
            data.readUTF();
            return readCompound(data);
        }
    }

    private static InputStream openInputStream(File file) throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(file));
        PushbackInputStream pushback = new PushbackInputStream(input, 2);
        byte[] header = new byte[2];
        int read = pushback.read(header);
        if (read > 0) {
            pushback.unread(header, 0, read);
        }
        if (read == 2 && (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b)) {
            return new GZIPInputStream(pushback);
        }
        return pushback;
    }

    private static NbtTag readTagPayload(DataInputStream data, byte type) throws IOException {
        return switch (type) {
            case NbtType.BYTE -> new NbtByte(data.readByte());
            case NbtType.SHORT -> new NbtShort(data.readShort());
            case NbtType.INT -> new NbtInt(data.readInt());
            case NbtType.LONG -> new NbtLong(data.readLong());
            case NbtType.FLOAT -> new NbtFloat(data.readFloat());
            case NbtType.DOUBLE -> new NbtDouble(data.readDouble());
            case NbtType.STRING -> new NbtString(data.readUTF());
            case NbtType.BYTE_ARRAY -> new NbtByteArray(readByteArray(data));
            case NbtType.INT_ARRAY -> new NbtIntArray(readIntArray(data));
            case NbtType.LONG_ARRAY -> new NbtLongArray(readLongArray(data));
            case NbtType.LIST -> readList(data);
            case NbtType.COMPOUND -> readCompound(data);
            default -> throw new IOException("Unsupported NBT tag type: " + type);
        };
    }

    private static byte[] readByteArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        byte[] values = new byte[length];
        data.readFully(values);
        return values;
    }

    private static int[] readIntArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        int[] values = new int[length];
        for (int i = 0; i < length; i++) {
            values[i] = data.readInt();
        }
        return values;
    }

    private static long[] readLongArray(DataInputStream data) throws IOException {
        int length = data.readInt();
        long[] values = new long[length];
        for (int i = 0; i < length; i++) {
            values[i] = data.readLong();
        }
        return values;
    }

    private static NbtList readList(DataInputStream data) throws IOException {
        byte elementType = data.readByte();
        int length = data.readInt();
        List<NbtTag> values = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            values.add(readTagPayload(data, elementType));
        }
        return new NbtList(elementType, values);
    }

    private static NbtCompound readCompound(DataInputStream data) throws IOException {
        NbtCompound compound = new NbtCompound();
        while (true) {
            byte type = data.readByte();
            if (type == NbtType.END) {
                break;
            }
            String name = data.readUTF();
            compound.put(name, readTagPayload(data, type));
        }
        return compound;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/Schematic.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/Schematic.java`  
- Size: 294 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import java.util.List;

public record Schematic(int width, int height, int length, BlockPalette palette, int[] blocks,
                        List<NbtCompound> blockEntities, List<NbtCompound> entities) {
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/SchematicPaster.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/SchematicPaster.java`  
- Size: 8733 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.BlockFace;

public class SchematicPaster {
    public record PasteOptions(boolean includeAir, Transform transform, UndoBuffer undoBuffer) {
    }

    private final JavaPlugin plugin;
    private final Logger logger;

    public SchematicPaster(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public CompletableFuture<Void> pasteInBatches(World world, Location origin, Schematic schematic, PasteOptions options, int batchSize) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        int width = schematic.width();
        int height = schematic.height();
        int length = schematic.length();
        int[] blocks = schematic.blocks();
        BlockPalette palette = schematic.palette();
        ensureChunksLoaded(world, origin, schematic, options.transform());
        Iterator<BlockPlacement> iterator = new BlockIterator(width, height, length, blocks, palette, options.transform());
        new BukkitRunnable() {
            @Override
            public void run() {
                int placed = 0;
                while (iterator.hasNext() && placed < batchSize) {
                    BlockPlacement placement = iterator.next();
                    if (placement.blockData() == null) {
                        logger.warning("Missing palette entry for block index.");
                        continue;
                    }
                    if (!options.includeAir() && placement.isAir()) {
                        continue;
                    }
                    Block block = world.getBlockAt(origin.getBlockX() + placement.x(), origin.getBlockY() + placement.y(), origin.getBlockZ() + placement.z());
                    try {
                        if (options.undoBuffer() != null) {
                            options.undoBuffer().add(block.getLocation(), block.getBlockData());
                        }
                        BlockData data = Bukkit.createBlockData(placement.blockData());
                        data = rotateBlockData(data, options.transform().rotation());
                        block.setBlockData(data, false);
                    } catch (IllegalArgumentException ex) {
                        logger.warning("Invalid block data: " + placement.blockData());
                    }
                    placed++;
                }
                if (!iterator.hasNext()) {
                    applyBlockEntities(world, origin, schematic, options.transform());
                    spawnEntities(world, origin, schematic, options.transform());
                    future.complete(null);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
        return future;
    }

    private void applyBlockEntities(World world, Location origin, Schematic schematic, Transform transform) {
        List<NbtCompound> blockEntities = schematic.blockEntities();
        if (blockEntities == null || blockEntities.isEmpty()) {
            return;
        }
        BlockEntityApplier applier = new BlockEntityApplier(logger);
        for (NbtCompound blockEntity : blockEntities) {
            applier.apply(world, origin, blockEntity, transform, schematic.width(), schematic.length());
        }
    }

    private void spawnEntities(World world, Location origin, Schematic schematic, Transform transform) {
        List<NbtCompound> entities = schematic.entities();
        if (entities == null || entities.isEmpty()) {
            return;
        }
        EntitySpawner spawner = new EntitySpawner(logger);
        for (NbtCompound entity : entities) {
            spawner.spawn(world, origin, entity, transform, schematic.width(), schematic.length());
        }
    }

    private void ensureChunksLoaded(World world, Location origin, Schematic schematic, Transform transform) {
        int width = schematic.width();
        int length = schematic.length();
        int[] min = transform.apply(0, 0, 0, width, length);
        int[] max = transform.apply(width - 1, 0, length - 1, width, length);
        int minX = Math.min(min[0], max[0]) + origin.getBlockX();
        int maxX = Math.max(min[0], max[0]) + origin.getBlockX();
        int minZ = Math.min(min[2], max[2]) + origin.getBlockZ();
        int maxZ = Math.max(min[2], max[2]) + origin.getBlockZ();
        for (int x = minX >> 4; x <= maxX >> 4; x++) {
            for (int z = minZ >> 4; z <= maxZ >> 4; z++) {
                if (!world.isChunkLoaded(x, z)) {
                    world.getChunkAt(x, z);
                }
            }
        }
    }

    private static class BlockPlacement {
        private final int x;
        private final int y;
        private final int z;
        private final String blockData;
        private final boolean air;

        private BlockPlacement(int x, int y, int z, String blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockData = blockData;
            this.air = blockData != null && (blockData.equals("minecraft:air") || blockData.equals("minecraft:cave_air")
                || blockData.equals("minecraft:void_air"));
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        public int z() {
            return z;
        }

        public String blockData() {
            return blockData;
        }

        public boolean isAir() {
            return air;
        }
    }

    private static class BlockIterator implements Iterator<BlockPlacement> {
        private final int width;
        private final int height;
        private final int length;
        private final int[] blocks;
        private final BlockPalette palette;
        private final Transform transform;
        private int index;

        private BlockIterator(int width, int height, int length, int[] blocks, BlockPalette palette, Transform transform) {
            this.width = width;
            this.height = height;
            this.length = length;
            this.blocks = blocks;
            this.palette = palette;
            this.transform = transform;
        }

        @Override
        public boolean hasNext() {
            return index < blocks.length;
        }

        @Override
        public BlockPlacement next() {
            int i = index++;
            int x = i % width;
            int z = (i / width) % length;
            int y = i / (width * length);
            int[] transformed = transform.apply(x, y, z, width, length);
            String blockData = palette.getState(blocks[i]);
            return new BlockPlacement(transformed[0], transformed[1], transformed[2], blockData);
        }
    }

    private BlockData rotateBlockData(BlockData data, Transform.Rotation rotation) {
        if (rotation == Transform.Rotation.NONE) {
            return data;
        }
        if (data instanceof Directional directional) {
            BlockFace face = directional.getFacing();
            BlockFace rotated = rotateFace(face, rotation);
            if (rotated != null) {
                directional.setFacing(rotated);
            }
        } else if (data instanceof Rotatable rotatable) {
            BlockFace face = rotatable.getRotation();
            BlockFace rotated = rotateFace(face, rotation);
            if (rotated != null) {
                rotatable.setRotation(rotated);
            }
        }
        return data;
    }

    private BlockFace rotateFace(BlockFace face, Transform.Rotation rotation) {
        if (face == null) {
            return null;
        }
        return switch (rotation) {
            case CLOCKWISE_90 -> rotateOnce(face);
            case CLOCKWISE_180 -> rotateOnce(rotateOnce(face));
            case CLOCKWISE_270 -> rotateOnce(rotateOnce(rotateOnce(face)));
            default -> face;
        };
    }

    private BlockFace rotateOnce(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.EAST;
            case EAST -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.WEST;
            case WEST -> BlockFace.NORTH;
            default -> face;
        };
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/SpongeSchemLoader.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/SpongeSchemLoader.java`  
- Size: 4345 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpongeSchemLoader {
    public Schematic load(File file) throws IOException {
        NbtCompound root = NbtIO.read(file);
        int width = root.getInt("Width", -1);
        int height = root.getInt("Height", -1);
        int length = root.getInt("Length", -1);
        if (width <= 0 || height <= 0 || length <= 0) {
            throw new IOException("Unsupported schematic format");
        }
        NbtCompound paletteTag = root.getCompound("Palette");
        if (paletteTag == null) {
            throw new IOException("Unsupported schematic format");
        }
        Map<Integer, String> paletteMap = new HashMap<>();
        for (var entry : paletteTag.values().entrySet()) {
            String blockState = entry.getKey();
            var tag = entry.getValue();
            if (tag instanceof com.example.rpg.schematic.nbt.NbtInt nbtInt) {
                paletteMap.put(nbtInt.value(), blockState);
            } else if (tag instanceof com.example.rpg.schematic.nbt.NbtShort nbtShort) {
                paletteMap.put((int) nbtShort.value(), blockState);
            }
        }
        BlockPalette palette = new BlockPalette(paletteMap);
        int total = width * height * length;
        int[] blocks = readBlocks(root, palette.size(), total);
        if (blocks.length != total) {
            throw new IOException("Unsupported schematic format");
        }
        List<NbtCompound> blockEntities = List.of();
        List<NbtCompound> entities = List.of();
        NbtList blockEntityList = root.getList("BlockEntities");
        if (blockEntityList != null) {
            blockEntities = blockEntityList.compounds();
        }
        NbtList entityList = root.getList("Entities");
        if (entityList != null) {
            entities = entityList.compounds();
        }
        return new Schematic(width, height, length, palette, blocks, blockEntities, entities);
    }

    private int[] readBlocks(NbtCompound root, int paletteSize, int totalBlocks) throws IOException {
        byte[] byteData = root.getByteArray("BlockData");
        if (byteData != null) {
            return decodeVarIntArray(byteData, totalBlocks);
        }
        long[] longData = root.getLongArray("BlockData");
        if (longData != null) {
            int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(Math.max(paletteSize - 1, 1)));
            return unpackLongArray(longData, bits, totalBlocks);
        }
        throw new IOException("Unsupported schematic format");
    }

    private int[] decodeVarIntArray(byte[] data, int expected) throws IOException {
        int[] values = new int[expected];
        int index = 0;
        int i = 0;
        while (i < data.length && index < expected) {
            int value = 0;
            int position = 0;
            byte current;
            do {
                if (i >= data.length) {
                    throw new IOException("Unexpected end of block data");
                }
                current = data[i++];
                value |= (current & 0x7F) << position;
                position += 7;
            } while ((current & 0x80) != 0);
            values[index++] = value;
        }
        if (index != expected) {
            throw new IOException("Block data length mismatch");
        }
        return values;
    }

    private int[] unpackLongArray(long[] data, int bits, int expected) {
        int[] values = new int[expected];
        long mask = (1L << bits) - 1L;
        int index = 0;
        int bitIndex = 0;
        while (index < expected) {
            int startLong = bitIndex >> 6;
            int startOffset = bitIndex & 63;
            if (startLong >= data.length) {
                break;
            }
            long value = data[startLong] >>> startOffset;
            int bitsLeft = 64 - startOffset;
            if (bitsLeft < bits && startLong + 1 < data.length) {
                value |= data[startLong + 1] << bitsLeft;
            }
            values[index++] = (int) (value & mask);
            bitIndex += bits;
        }
        return values;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/Transform.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/Transform.java`  
- Size: 1368 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

public class Transform {
    public enum Rotation {
        NONE,
        CLOCKWISE_90,
        CLOCKWISE_180,
        CLOCKWISE_270
    }

    private final Rotation rotation;
    private final int offsetX;
    private final int offsetY;
    private final int offsetZ;

    public Transform(Rotation rotation, int offsetX, int offsetY, int offsetZ) {
        this.rotation = rotation;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    public Rotation rotation() {
        return rotation;
    }

    public int offsetX() {
        return offsetX;
    }

    public int offsetY() {
        return offsetY;
    }

    public int offsetZ() {
        return offsetZ;
    }

    public int[] apply(int x, int y, int z, int width, int length) {
        int rx = x;
        int rz = z;
        switch (rotation) {
            case CLOCKWISE_90 -> {
                rx = length - 1 - z;
                rz = x;
            }
            case CLOCKWISE_180 -> {
                rx = width - 1 - x;
                rz = length - 1 - z;
            }
            case CLOCKWISE_270 -> {
                rx = z;
                rz = width - 1 - x;
            }
            default -> {
            }
        }
        return new int[]{rx + offsetX, y + offsetY, rz + offsetZ};
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/UndoBuffer.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/schematic/UndoBuffer.java`  
- Size: 589 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.schematic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

public class UndoBuffer {
    public record BlockSnapshot(Location location, BlockData data) {
    }

    private final List<BlockSnapshot> snapshots = new ArrayList<>();

    public void add(Location location, BlockData data) {
        snapshots.add(new BlockSnapshot(location, data));
    }

    public List<BlockSnapshot> snapshots() {
        return Collections.unmodifiableList(snapshots);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/AggroEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/AggroEffect.java`  
- Size: 861 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class AggroEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double radius = parseDouble(params.getOrDefault("radius", 8));
        player.getNearbyEntities(radius, radius, radius).stream()
            .filter(entity -> entity instanceof Mob)
            .map(entity -> (Mob) entity)
            .forEach(mob -> mob.setTarget(player));
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 8.0;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/DamageEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/DamageEffect.java`  
- Size: 1982 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Comparator;
import java.util.Map;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DamageEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double amount = parseDouble(params.getOrDefault("amount", 4));
        double radius = parseDouble(params.getOrDefault("radius", 0));
        int maxTargets = parseInt(params.getOrDefault("maxTargets", 1));
        if (radius > 0) {
            player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                .sorted(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
                .limit(Math.max(1, maxTargets))
                .map(entity -> (LivingEntity) entity)
                .forEach(target -> target.damage(amount, player));
            return;
        }

        Entity target = player.getNearbyEntities(3, 2, 3).stream()
            .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
            .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
            .orElse(null);
        if (target instanceof LivingEntity living) {
            living.damage(amount, player);
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/HealEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/HealEffect.java`  
- Size: 951 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

public class HealEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double amount = parseDouble(params.getOrDefault("amount", 4));
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null
            ? player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()
            : 20.0;
        double newHealth = Math.min(maxHealth, player.getHealth() + amount);
        player.setHealth(newHealth);
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/ParticleEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/ParticleEffect.java`  
- Size: 1310 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class ParticleEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String typeName = String.valueOf(params.getOrDefault("type", "SPELL")).toUpperCase();
        int count = parseInt(params.getOrDefault("count", 10));
        double speed = parseDouble(params.getOrDefault("speed", 0.01));
        Particle particle;
        try {
            particle = Particle.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            particle = Particle.SPELL;
        }
        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1.0, 0), count, 0.3, 0.6, 0.3, speed);
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.01;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/PotionStatusEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/PotionStatusEffect.java`  
- Size: 1734 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionStatusEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String typeName = String.valueOf(params.getOrDefault("type", "SPEED")).toUpperCase();
        int duration = parseInt(params.getOrDefault("duration", 100));
        int amplifier = parseInt(params.getOrDefault("amplifier", 0));
        double radius = parseDouble(params.getOrDefault("radius", 0));
        PotionEffectType type = PotionEffectType.getByName(typeName);
        if (type == null) {
            return;
        }
        PotionEffect effect = new PotionEffect(type, duration, amplifier);
        if (radius > 0) {
            player.getNearbyEntities(radius, radius, radius).stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .forEach(target -> target.addPotionEffect(effect));
            return;
        }
        player.addPotionEffect(effect);
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/ProjectileEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/ProjectileEffect.java`  
- Size: 770 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;

public class ProjectileEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String type = String.valueOf(params.getOrDefault("type", "SNOWBALL")).toUpperCase();
        switch (type) {
            case "SMALL_FIREBALL" -> player.launchProjectile(SmallFireball.class);
            case "SNOWBALL" -> player.launchProjectile(Snowball.class);
            default -> player.launchProjectile(Snowball.class);
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/SoundEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/SoundEffect.java`  
- Size: 1090 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        String soundName = String.valueOf(params.getOrDefault("sound", "ENTITY_PLAYER_LEVELUP")).toUpperCase();
        float volume = parseFloat(params.getOrDefault("volume", 1.0));
        float pitch = parseFloat(params.getOrDefault("pitch", 1.0));
        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        }
        player.getWorld().playSound(player.getLocation(), sound, volume, pitch);
    }

    private float parseFloat(Object raw) {
        try {
            return Float.parseFloat(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 1.0f;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/VelocityEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/VelocityEffect.java`  
- Size: 1173 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        double forward = parseDouble(params.getOrDefault("forward", 1.2));
        double up = parseDouble(params.getOrDefault("up", 0.3));
        boolean add = parseBoolean(params.getOrDefault("add", false));
        Vector direction = player.getLocation().getDirection().multiply(forward);
        direction.setY(up);
        if (add) {
            player.setVelocity(player.getVelocity().add(direction));
        } else {
            player.setVelocity(direction);
        }
    }

    private double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(Object raw) {
        return Boolean.parseBoolean(String.valueOf(raw));
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/XpEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/effects/XpEffect.java`  
- Size: 671 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill.effects;

import com.example.rpg.model.PlayerProfile;
import com.example.rpg.skill.SkillEffect;
import java.util.Map;
import org.bukkit.entity.Player;

public class XpEffect implements SkillEffect {
    @Override
    public void apply(Player player, PlayerProfile profile, Map<String, Object> params) {
        int amount = parseInt(params.getOrDefault("amount", 0));
        if (amount > 0) {
            profile.addXp(amount);
        }
    }

    private int parseInt(Object raw) {
        try {
            return Integer.parseInt(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffect.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffect.java`  
- Size: 413 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill;

import com.example.rpg.model.PlayerProfile;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface SkillEffect {
    void apply(Player player, PlayerProfile profile, Map<String, Object> params);

    default List<Component> describe(Map<String, Object> params) {
        return List.of();
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectConfig.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectConfig.java`  
- Size: 1076 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillEffectConfig {
    private SkillEffectType type;
    private Map<String, Object> params = new HashMap<>();

    public SkillEffectConfig(SkillEffectType type, Map<String, Object> params) {
        this.type = type;
        if (params != null) {
            this.params.putAll(params);
        }
    }

    public SkillEffectType type() {
        return type;
    }

    public void setType(SkillEffectType type) {
        this.type = type;
    }

    public Map<String, Object> params() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String describe() {
        if (params.isEmpty()) {
            return type.name();
        }
        String joined = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
        return type.name() + " (" + joined + ")";
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectRegistry.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectRegistry.java`  
- Size: 695 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill;

import java.util.EnumMap;
import java.util.Map;

public class SkillEffectRegistry {
    private final Map<SkillEffectType, SkillEffect> effects = new EnumMap<>(SkillEffectType.class);

    public SkillEffectRegistry register(SkillEffectType type, SkillEffect effect) {
        effects.put(type, effect);
        return this;
    }

    public void apply(SkillEffectConfig config, org.bukkit.entity.Player player,
                      com.example.rpg.model.PlayerProfile profile) {
        SkillEffect effect = effects.get(config.type());
        if (effect == null) {
            return;
        }
        effect.apply(player, profile, config.params());
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectType.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/skill/SkillEffectType.java`  
- Size: 171 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.skill;

public enum SkillEffectType {
    HEAL,
    DAMAGE,
    PROJECTILE,
    POTION,
    SOUND,
    XP,
    PARTICLE,
    VELOCITY,
    AGGRO
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/AuditLog.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/AuditLog.java`  
- Size: 844 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AuditLog {
    private final File file;
    private final JavaPlugin plugin;

    public AuditLog(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "audit.log");
    }

    public void log(CommandSender sender, String action) {
        String line = Instant.now() + " | " + sender.getName() + " | " + action + System.lineSeparator();
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line);
        } catch (IOException e) {
            plugin.getLogger().warning("Audit log failed: " + e.getMessage());
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/EloCalculator.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/EloCalculator.java`  
- Size: 371 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

public final class EloCalculator {
    private EloCalculator() {}

    public static int calculateNewRating(int rating, int opponentRating, double score, int kFactor) {
        double expected = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
        return (int) Math.round(rating + kFactor * (score - expected));
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/ItemBuilder.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/ItemBuilder.java`  
- Size: 1085 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private final List<Component> lore = new ArrayList<>();

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(Component name) {
        meta.displayName(name);
        return this;
    }

    public ItemBuilder loreLine(Component line) {
        lore.add(line);
        return this;
    }

    public ItemBuilder loreLines(List<Component> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemStack build() {
        if (!lore.isEmpty()) {
            meta.lore(lore.stream().collect(Collectors.toList()));
        }
        item.setItemMeta(meta);
        return item;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/ItemGenerator.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/ItemGenerator.java`  
- Size: 1704 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

import com.example.rpg.model.Rarity;
import java.util.List;
import java.util.Random;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemGenerator {
    private final Random random = new Random();
    private final NamespacedKey itemKey;
    private final NamespacedKey rarityKey;
    private final com.example.rpg.manager.ItemStatManager itemStatManager;

    public ItemGenerator(JavaPlugin plugin, com.example.rpg.manager.ItemStatManager itemStatManager) {
        this.itemKey = new NamespacedKey(plugin, "rpg_item");
        this.rarityKey = new NamespacedKey(plugin, "rpg_rarity");
        this.itemStatManager = itemStatManager;
    }

    public ItemStack createRpgItem(Material material, Rarity rarity, int minLevel) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(rarity.name() + " " + material.name()).color(rarity.color()));
        meta.lore(List.of(
            Component.text("Rarity: " + rarity.name()).color(rarity.color()),
            Component.text("Level " + minLevel)
        ));
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.INTEGER, 1);
        meta.getPersistentDataContainer().set(rarityKey, PersistentDataType.STRING, rarity.name());
        item.setItemMeta(meta);
        itemStatManager.applyAffixes(item);
        item.setAmount(1 + random.nextInt(1));
        return item;
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/PromptManager.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/PromptManager.java`  
- Size: 853 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class PromptManager {
    private final Map<UUID, Consumer<String>> prompts = new HashMap<>();

    public void prompt(Player player, Component message, Consumer<String> handler) {
        prompts.put(player.getUniqueId(), handler);
        player.sendMessage(message);
    }

    public boolean handle(Player player, String message) {
        Consumer<String> handler = prompts.remove(player.getUniqueId());
        if (handler == null) {
            return false;
        }
        handler.accept(message);
        return true;
    }

    public void cancel(Player player) {
        prompts.remove(player.getUniqueId());
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/Text.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/Text.java`  
- Size: 353 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private Text() {
    }

    public static Component mm(String input) {
        return MINI.deserialize(input);
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/WorldUtils.java`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/java/com/example/rpg/util/WorldUtils.java`  
- Size: 1405 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.rpg.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldUtils {
    public static void unloadAndDeleteWorld(World world, Location fallback) {
        if (world == null) {
            return;
        }

        for (Player player : world.getPlayers()) {
            if (fallback != null) {
                player.teleport(fallback);
            } else if (!Bukkit.getWorlds().isEmpty()) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
            player.sendMessage(Text.mm("<yellow>Der Dungeon löst sich auf..."));
        }

        Bukkit.unloadWorld(world, false);
        File worldFolder = world.getWorldFolder();
        deleteDirectory(worldFolder);
    }

    private static void deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/resources/buildings.yml`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/resources/buildings.yml`  
- Size: 1055 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```yaml
buildings:
  cottage:
    name: "Kleines Wohnhaus"
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
  market:
    name: "Marktstand"
    category: SHOP
    schematic: "market_stall.schem"
    minFloors: 1
    maxFloors: 1
    floorHeight: 4
  townhall:
    name: "Rathaus"
    category: PUBLIC
    schematic: "townhall_ground.schem"
    floorSchematic: "townhall_floor.schem"
    minFloors: 2
    maxFloors: 3
    floorHeight: 6
  forge:
    name: "Schmiede"
    category: CRAFTING
    schematic: "forge.schem"
    minFloors: 1
    maxFloors: 1
    floorHeight: 5
  testhouse:
    name: "Testhaus"
    category: RESIDENTIAL
    schematic: "testhouse.schem"
    minFloors: 1
    maxFloors: 1
    floorHeight: 5

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/resources/config.yml`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/resources/config.yml`  
- Size: 738 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```yaml
rpg:
  dataVersion: 1
  xp:
    mobBase: 10
    blockBreak: 1
    craft: 2
  manaRegenPerTick: 5
  party:
    xpSplit: true
lootchat:
  enabled: true
database:
  host: localhost
  port: 5432
  name: rpg
  user: rpg
  password: minecraft
  poolSize: 10
permissions:
  enabled: true
  defaultRole: player
  opBypass: true
  defaultDecision: DENY
  auditEnabled: true
  cacheTtlSeconds: 30
dungeon:
  wfc:
    width: 10
    height: 3
    depth: 10
    originY: 60
  themes:
    crypt:
      floor_material: STONE_BRICKS
      wall_material: COBBLESTONE
      corridor_material: COBBLESTONE
  entrance:
    world: world
    x: 0
    y: 64
    z: 0
  exit:
    world: world
    x: 0
    y: 64
    z: 0
building:
  schematicsFolder: schematics

```

## File: `MineLauncher/plugins/RPGPlugin/src/main/resources/plugin.yml`  
- Path: `MineLauncher/plugins/RPGPlugin/src/main/resources/plugin.yml`  
- Size: 1822 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```yaml
name: MineLauncherRPG
main: com.example.rpg.RPGPlugin
version: 1.0.0
author: LauncherTeam
api-version: 1.20
commands:
  rpg:
    description: Open RPG menu and use RPG commands
    usage: /rpg
  rpgadmin:
    description: Open RPG admin tools
    usage: /rpgadmin
  party:
    description: Party management
    usage: /party
  p:
    description: Party chat shortcut
    usage: /p <message>
  auction:
    description: Auction house
    usage: /auction <list|sell|buy>
  trade:
    description: Player trade
    usage: /trade <request|accept|offer|requestgold|ready|cancel>
  dungeon:
    description: Dungeon management
    usage: /dungeon <enter|leave|generate>
  guild:
    description: Guild management
    usage: /guild <create|invite|accept|leave|disband|info|chat|bank|quest>
  g:
    description: Guild chat shortcut
    usage: /g <message>
  pvp:
    description: PvP arenas
    usage: /pvp <join|top>
  behavior:
    description: Behavior tree editor
    usage: /behavior edit <tree>
  lootchat:
    description: Toggle loot chat
    usage: /lootchat [true|false]
  voicechat:
    description: Voice chat channels
    usage: /voicechat <party|guild|leave>
permissions:
  rpg.admin.*:
    description: Full RPG admin permissions
    default: op
    children:
      rpg.admin: true
      rpg.editor: true
      rpg.debug: true
  rpg.editor.*:
    description: Full RPG editor permissions
    default: op
    children:
      rpg.editor: true
  rpg.mod.*:
    description: RPG moderation permissions
    default: op
    children:
      rpg.mod: true
  rpg.admin:
    description: Access admin menu
    default: op
  rpg.editor:
    description: Access editor tools
    default: op
  rpg.mod:
    description: Access moderation tools
    default: op
  rpg.debug:
    description: Access debug overlay
    default: op

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/pom.xml`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/pom.xml`  
- Size: 1648 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>world-creator-plugin</artifactId>
    <version>1.0.0</version>
    <name>WorldCreatorPlugin</name>

    <properties>
        <!-- Paper 1.20.4 benötigt Java 17 -->
        <java.version>17</java.version>
        <paper.version>1.20.4-R0.1-SNAPSHOT</paper.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>papermc</id>
            <url>https://repo.papermc.io/repository/maven-public/</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>io.papermc.paper</groupId>
            <artifactId>paper-api</artifactId>
            <version>${paper.version}</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <!-- verhindert falschen Bytecode -->
                    <release>${java.version}</release>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/README.md`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/README.md`  
- Size: 544 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```markdown
# WorldCreatorPlugin

Ein Bukkit/Spigot-Plugin, das ein Ingame-Menü öffnet, um neue Welten zu erstellen (Leere Welt, Wasserwelt, Sky-Inseln, Dschungel, Wüste) und den Spieler direkt zu teleportieren.

## Build

```bash
mvn -f plugins/WorldCreatorPlugin/pom.xml package
```

Die JAR liegt danach unter `plugins/WorldCreatorPlugin/target/`.

## Nutzung

- Plugin in den Server-Ordner `plugins/` legen.
- Server starten.
- Ingame `/worlds` eingeben, um das Menü zu öffnen.
- Auf ein Symbol klicken, um eine Welt zu erstellen und zu betreten.

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/FixedBiomeProvider.java`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/FixedBiomeProvider.java`  
- Size: 568 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.worldcreator;

import java.util.List;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;

public final class FixedBiomeProvider extends BiomeProvider {
    private final Biome biome;

    public FixedBiomeProvider(Biome biome) {
        this.biome = biome;
    }

    @Override
    public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
        return biome;
    }

    @Override
    public List<Biome> getBiomes(WorldInfo worldInfo) {
        return List.of(biome);
    }
}

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/SkyIslandsChunkGenerator.java`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/SkyIslandsChunkGenerator.java`  
- Size: 1699 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class SkyIslandsChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        var chunkData = createChunkData(world);

        // deterministische Chunk-Zufallsquelle
        var seededRandom = new Random(world.getSeed() ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L));

        if (seededRandom.nextDouble() < 0.35) {
            int centerX = seededRandom.nextInt(16);
            int centerZ = seededRandom.nextInt(16);
            int centerY = 90 + seededRandom.nextInt(30);
            int radius = 4 + seededRandom.nextInt(5);

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = Math.max(1, centerY - radius); y <= centerY + radius; y++) {
                        double dx = x - centerX;
                        double dy = y - centerY;
                        double dz = z - centerZ;

                        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (dist <= radius) {
                            chunkData.setBlock(
                                    x, y, z,
                                    (y == centerY + radius - 1) ? Material.GRASS_BLOCK : Material.STONE
                            );
                        }
                    }
                }
            }
        }

        return chunkData;
    }
}

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/VoidChunkGenerator.java`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/VoidChunkGenerator.java`  
- Size: 431 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.worldcreator;

import java.util.Random;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class VoidChunkGenerator extends ChunkGenerator {

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        return createChunkData(world);
    }
}

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WaterChunkGenerator.java`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WaterChunkGenerator.java`  
- Size: 849 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.worldcreator;

import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public final class WaterChunkGenerator extends ChunkGenerator {
    private static final int SEA_LEVEL = 62;

    @Override
    public ChunkData generateChunkData(
            World world,
            Random random,
            int chunkX,
            int chunkZ,
            BiomeGrid biome
    ) {
        var chunkData = createChunkData(world);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunkData.setBlock(x, 0, z, Material.BEDROCK);
                for (int y = 1; y <= SEA_LEVEL; y++) {
                    chunkData.setBlock(x, y, z, Material.WATER);
                }
            }
        }

        return chunkData;
    }
}

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WorldCreatorPlugin.java`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WorldCreatorPlugin.java`  
- Size: 4535 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.worldcreator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldCreatorPlugin extends JavaPlugin implements Listener {
    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "World Creator";
    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private NamespacedKey worldTypeKey;
    private final Map<WorldTypeOption, Integer> slotMap = new EnumMap<>(WorldTypeOption.class);

    @Override
    public void onEnable() {
        worldTypeKey = new NamespacedKey(this, "world-type");
        Bukkit.getPluginManager().registerEvents(this, this);

        slotMap.put(WorldTypeOption.VOID, 10);
        slotMap.put(WorldTypeOption.WATER, 11);
        slotMap.put(WorldTypeOption.SKY_ISLANDS, 12);
        slotMap.put(WorldTypeOption.JUNGLE, 14);
        slotMap.put(WorldTypeOption.DESERT, 15);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können dieses Kommando nutzen.");
            return true;
        }

        player.openInventory(buildMenu());
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!event.getView().getTitle().equals(MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);
        var clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        var meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        var typeName = meta.getPersistentDataContainer().get(worldTypeKey, PersistentDataType.STRING);
        if (typeName == null) {
            return;
        }

        var option = WorldTypeOption.valueOf(typeName);
        createWorldAndTeleport(player, option);
    }

    private Inventory buildMenu() {
        var inventory = Bukkit.createInventory(null, 27, MENU_TITLE);
        for (var entry : slotMap.entrySet()) {
            inventory.setItem(entry.getValue(), createMenuItem(entry.getKey()));
        }
        return inventory;
    }

    private ItemStack createMenuItem(WorldTypeOption option) {
        var item = new ItemStack(option.getIcon());
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + option.getDisplayName());
            meta.getPersistentDataContainer().set(worldTypeKey, PersistentDataType.STRING, option.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    private void createWorldAndTeleport(Player player, WorldTypeOption option) {
        var worldName = option.name().toLowerCase() + "-" + NAME_FORMAT.format(LocalDateTime.now());
        var creator = new WorldCreator(worldName);

        switch (option) {
            case VOID -> creator.generator(new VoidChunkGenerator());
            case WATER -> creator.generator(new WaterChunkGenerator());
            case SKY_ISLANDS -> creator.generator(new SkyIslandsChunkGenerator());
            case JUNGLE -> creator.biomeProvider(new FixedBiomeProvider(Biome.JUNGLE));
            case DESERT -> creator.biomeProvider(new FixedBiomeProvider(Biome.DESERT));
        }

        World world = creator.createWorld();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Welt konnte nicht erstellt werden.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Welt erstellt: " + world.getName());
        player.teleport(world.getSpawnLocation());
    }
}

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WorldTypeOption.java`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/java/com/example/worldcreator/WorldTypeOption.java`  
- Size: 649 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```java
package com.example.worldcreator;

import org.bukkit.Material;

public enum WorldTypeOption {
    VOID("Leere Welt", Material.GLASS),
    WATER("Wasserwelt", Material.WATER_BUCKET),
    SKY_ISLANDS("Sky Inseln", Material.ELYTRA),
    JUNGLE("Dschungel", Material.JUNGLE_LOG),
    DESERT("Wüste", Material.SAND);

    private final String displayName;
    private final Material icon;

    WorldTypeOption(String displayName, Material icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getIcon() {
        return icon;
    }
}

```

## File: `MineLauncher/plugins/WorldCreatorPlugin/src/main/resources/plugin.yml`  
- Path: `MineLauncher/plugins/WorldCreatorPlugin/src/main/resources/plugin.yml`  
- Size: 209 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```yaml
name: WorldCreatorPlugin
main: com.example.worldcreator.WorldCreatorPlugin
version: 1.0.0
author: LauncherTeam
api-version: 1.20
commands:
  worlds:
    description: Open world creator menu
    usage: /worlds

```

## File: `MineLauncher/Program.cs`  
- Path: `MineLauncher/Program.cs`  
- Size: 1492 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using CLauncher.Services;

namespace CLauncher;

internal static class Program
{
    [STAThread]
    private static void Main()
    {
        ApplicationConfiguration.Initialize();

        var basePath = Directory.GetCurrentDirectory();
        var configPath = Path.Combine(basePath, "launcher-config.json");

        using var httpClient = new HttpClient();
        var configService = new ConfigService(configPath);
        var downloadService = new DownloadService(httpClient);
        var serverService = new ServerService();
        var launcherService = new LauncherService();
        var clientLauncherService = new ClientLauncherService();

        if (Environment.GetCommandLineArgs().Length > 1)
        {
            var args = Environment.GetCommandLineArgs().Skip(1).ToArray();
            var command = args[0].ToLowerInvariant();
            var config = configService.LoadOrCreate();

            if (command == "play")
            {
                var playerName = args.Length > 1 ? args[1] : config.Game.OfflineUsername;
                clientLauncherService
                    .LaunchClientAsync(playerName, config.Game.ClientVersion)
                    .GetAwaiter()
                    .GetResult();
                return;
            }
        }

        Application.Run(new MainForm(
            basePath,
            configService,
            downloadService,
            serverService,
            launcherService,
            clientLauncherService));
    }
}

```

## File: `MineLauncher/README.md`  
- Path: `MineLauncher/README.md`  
- Size: 10695 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```markdown
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
| `/pvp join` | PvP‑Queue | `/pvp join` |
| `/pvp top` | Top‑Liste | `/pvp top` |
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

```

## File: `MineLauncher/RPG_Handbuch.md`  
- Path: `MineLauncher/RPG_Handbuch.md`  
- Size: 6507 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```markdown
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

### 4.4 Dungeons
- `/dungeon enter`
- `/dungeon leave`
- `/dungeon generate <theme>` (z. B. `wfc`, `gruft`)

---

## 5) Admin‑Handbuch

### 5.1 Admin‑Menü
- **Öffnen:** `/rpgadmin`
- Inhalte: Zonen, NPCs, Quests, Loot, Skills, Debug, Bau‑Manager, Permissions.

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

### 5.9 Gebäude & Schemata
- `/rpgadmin build` oder `/rpgadmin build gui` (Bau‑Manager)
- `/rpgadmin build <id>` (Gebäude platzieren)
- `/rpgadmin build undo` (Undo)
- `/rpgadmin build move` (Move‑GUI)

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
- **Bau‑Manager**: Kategorien, Gebäude, Einzel‑Schemata, Undo, Move‑GUI.
- **Permissions‑GUI**: Rollen, Vererbung, Nodes, Spielerrollen, Audit‑Log.
- **Behavior‑Editor GUI** für KI‑Bäume.
- **Auktionshaus**: Listings per `/auction`.
- **Gildenbank & Gildenquests**.
- **Dungeon‑Instanzen** per `/dungeon generate`.
- **PvP‑Matchmaking** per `/pvp join`.

---

## 7) Quick‑Start (Minimal)
1. Admin erstellen (`op` oder Permissions).
2. `/rpgadmin wand` → Pos1/Pos2 setzen.
3. `/rpgadmin zone create startzone`.
4. `/rpgadmin npc create guide QUESTGIVER`.
5. `/rpgadmin quest create starter "Erste Schritte"`.
6. `/rpgadmin quest addstep starter KILL ZOMBIE 3`.
7. Spieler nutzt `/rpg` → Quests → annehmen.

```

## File: `MineLauncher/Services/ClientLauncherService.cs`  
- Path: `MineLauncher/Services/ClientLauncherService.cs`  
- Size: 1468 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using CmlLib.Core;
using CmlLib.Core.Auth;
using CmlLib.Core.ProcessBuilder;

namespace CLauncher.Services;

public sealed class ClientLauncherService
{
    public async Task LaunchClientAsync(string playerName, string versionString)
    {
        var path = new MinecraftPath(Path.Combine(Directory.GetCurrentDirectory(), "client_files"));
        var launcher = new MinecraftLauncher(path);

        Console.WriteLine($"Initialisiere Launcher in: {path.BasePath}");

        var versions = await launcher.GetAllVersionsAsync();
        var selectedVersion = versions.FirstOrDefault(version => version.Name == versionString);
        if (selectedVersion == null)
        {
            Console.WriteLine($"Version {versionString} nicht lokal gefunden. Versuche Download...");
        }

        Console.WriteLine($"Bereite Start von Version {versionString} vor...");

        var session = MSession.CreateOfflineSession(playerName);

        var launchOption = new MLaunchOption
        {
            Session = session,
            MaximumRamMb = 2048,
            ServerIp = "localhost",
            ServerPort = 25565
        };

        var process = await launcher.InstallAndBuildProcessAsync(versionString, launchOption);

        Console.WriteLine("Starte Minecraft Client...");

        process.Start();

        Console.WriteLine("Client gestartet! Dieses Fenster bleibt offen, bis der Client beendet wird.");
        await process.WaitForExitAsync();
    }
}

```

## File: `MineLauncher/Services/ConfigService.cs`  
- Path: `MineLauncher/Services/ConfigService.cs`  
- Size: 1146 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using System.Text.Json;
using CLauncher.Models;

namespace CLauncher.Services;

public sealed class ConfigService
{
    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        WriteIndented = true,
        PropertyNamingPolicy = JsonNamingPolicy.CamelCase
    };

    public string ConfigPath { get; }

    public ConfigService(string configPath)
    {
        ConfigPath = configPath;
    }

    public LauncherConfig LoadOrCreate()
    {
        if (!File.Exists(ConfigPath))
        {
            var config = new LauncherConfig();
            Save(config);
            return config;
        }

        var json = File.ReadAllText(ConfigPath);
        return Deserialize(json);
    }

    public void Save(LauncherConfig config)
    {
        var json = Serialize(config);
        File.WriteAllText(ConfigPath, json);
    }

    public string Serialize(LauncherConfig config)
    {
        return JsonSerializer.Serialize(config, JsonOptions);
    }

    public LauncherConfig Deserialize(string json)
    {
        return JsonSerializer.Deserialize<LauncherConfig>(json, JsonOptions) ?? new LauncherConfig();
    }
}

```

## File: `MineLauncher/Services/DownloadService.cs`  
- Path: `MineLauncher/Services/DownloadService.cs`  
- Size: 3279 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using System.Net.Http.Json;

namespace CLauncher.Services;

public sealed class DownloadService
{
    private readonly HttpClient _httpClient;

    public DownloadService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public async Task DownloadFileAsync(string url, string destinationPath, CancellationToken cancellationToken)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(destinationPath) ?? ".");

        using var response = await _httpClient.GetAsync(url, HttpCompletionOption.ResponseHeadersRead, cancellationToken);
        response.EnsureSuccessStatusCode();

        await using var contentStream = await response.Content.ReadAsStreamAsync(cancellationToken);
        await using var fileStream = new FileStream(destinationPath, FileMode.Create, FileAccess.Write, FileShare.None);
        await contentStream.CopyToAsync(fileStream, cancellationToken);
    }

    public async Task<PaperBuildInfo> GetLatestPaperBuildAsync(string version, CancellationToken cancellationToken)
    {
        var response = await _httpClient.GetFromJsonAsync<PaperVersionResponse>(
            $"https://api.papermc.io/v2/projects/paper/versions/{version}",
            cancellationToken);

        if (response?.Builds is null || response.Builds.Count == 0)
        {
            throw new InvalidOperationException($"No Paper builds found for version {version}.");
        }

        var latestBuild = response.Builds.Max();
        return new PaperBuildInfo(version, latestBuild);
    }

    public async Task<string> GetVanillaServerUrlAsync(string version, CancellationToken cancellationToken)
    {
        var manifest = await _httpClient.GetFromJsonAsync<VersionManifest>(
            "https://piston-meta.mojang.com/mc/game/version_manifest.json",
            cancellationToken);

        var versionEntry = manifest?.Versions.FirstOrDefault(item => item.Id == version);
        if (versionEntry is null)
        {
            throw new InvalidOperationException($"Minecraft version {version} not found in manifest.");
        }

        var versionDetails = await _httpClient.GetFromJsonAsync<VersionDetails>(
            versionEntry.Url,
            cancellationToken);

        var serverUrl = versionDetails?.Downloads?.Server?.Url;
        if (string.IsNullOrWhiteSpace(serverUrl))
        {
            throw new InvalidOperationException($"Server download URL missing for version {version}.");
        }

        return serverUrl;
    }

    private sealed class PaperVersionResponse
    {
        public List<int> Builds { get; set; } = new();
    }

    private sealed class VersionManifest
    {
        public List<VersionEntry> Versions { get; set; } = new();
    }

    private sealed class VersionEntry
    {
        public string Id { get; set; } = string.Empty;
        public string Url { get; set; } = string.Empty;
    }

    private sealed class VersionDetails
    {
        public DownloadSection? Downloads { get; set; }
    }

    private sealed class DownloadSection
    {
        public DownloadItem? Server { get; set; }
    }

    private sealed class DownloadItem
    {
        public string Url { get; set; } = string.Empty;
    }
}

public readonly record struct PaperBuildInfo(string Version, int Build);

```

## File: `MineLauncher/Services/LauncherService.cs`  
- Path: `MineLauncher/Services/LauncherService.cs`  
- Size: 736 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using System.Diagnostics;
using CLauncher.Models;

namespace CLauncher.Services;

public sealed class LauncherService
{
    public void LaunchServer(LauncherConfig config, string serverDirectory, string jarPath)
    {
        var args = $"-Xms{config.Java.MinMemory} -Xmx{config.Java.MaxMemory} -jar \"{jarPath}\" nogui";

        var startInfo = new ProcessStartInfo
        {
            FileName = config.Java.Path,
            Arguments = args,
            WorkingDirectory = serverDirectory,
            UseShellExecute = false
        };

        var process = Process.Start(startInfo);
        if (process is null)
        {
            throw new InvalidOperationException("Failed to start the Java process.");
        }
    }
}

```

## File: `MineLauncher/Services/ServerService.cs`  
- Path: `MineLauncher/Services/ServerService.cs`  
- Size: 3006 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```csharp
using CLauncher.Models;

namespace CLauncher.Services;

public sealed class ServerService
{
    public string ResolveServerDirectory(LauncherConfig config, string basePath)
    {
        return Path.Combine(basePath, config.InstallRoot, config.Server.Name);
    }

    public string GetServerJarPath(LauncherConfig config, string serverDirectory)
    {
        return Path.Combine(serverDirectory, config.Server.JarFileName);
    }

    public string BuildDownloadUrl(ServerConfig serverConfig, PaperBuildInfo? paperBuildInfo, string? vanillaUrl)
    {
        return serverConfig.Type.ToLowerInvariant() switch
        {
            "paper" => BuildPaperUrl(paperBuildInfo ?? throw new InvalidOperationException("Paper build info missing.")),
            "bukkit" => $"https://download.getbukkit.org/bukkit/bukkit-{serverConfig.Version}.jar",
            "craftbukkit" => $"https://download.getbukkit.org/craftbukkit/craftbukkit-{serverConfig.Version}.jar",
            "vanilla" => vanillaUrl ?? throw new InvalidOperationException("Vanilla server URL missing."),
            _ => throw new InvalidOperationException($"Unknown server type: {serverConfig.Type}")
        };
    }

    public void EnsureServerProperties(LauncherConfig config, string serverDirectory)
    {
        Directory.CreateDirectory(serverDirectory);
        var path = Path.Combine(serverDirectory, "server.properties");

        var properties = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase)
        {
            ["online-mode"] = config.Server.OnlineMode ? "true" : "false",
            ["server-port"] = config.Server.Port.ToString()
        };

        var existing = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
        if (File.Exists(path))
        {
            foreach (var line in File.ReadAllLines(path))
            {
                if (string.IsNullOrWhiteSpace(line) || line.TrimStart().StartsWith("#", StringComparison.Ordinal))
                {
                    continue;
                }

                var split = line.Split('=', 2);
                if (split.Length == 2)
                {
                    existing[split[0].Trim()] = split[1].Trim();
                }
            }
        }

        foreach (var pair in properties)
        {
            existing[pair.Key] = pair.Value;
        }

        var lines = existing.Select(pair => $"{pair.Key}={pair.Value}");
        File.WriteAllLines(path, lines);
    }

    public void EnsureEula(ServerConfig config, string serverDirectory)
    {
        var path = Path.Combine(serverDirectory, "eula.txt");
        var value = config.AcceptEula ? "true" : "false";
        File.WriteAllText(path, $"# Generated by C_launcher\neula={value}\n");
    }

    private static string BuildPaperUrl(PaperBuildInfo buildInfo)
    {
        return $"https://api.papermc.io/v2/projects/paper/versions/{buildInfo.Version}/builds/{buildInfo.Build}/downloads/paper-{buildInfo.Version}-{buildInfo.Build}.jar";
    }
}

```

## File: `MineLauncher/Wiki.htlm`  
- Path: `MineLauncher/Wiki.htlm`  
- Size: 27260 Bytes  
- Modified: 2025-12-26 13:29:26 UTC

```
<!DOCTYPE html>
<html lang="de">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>MineLauncher RPG – Admin Wiki</title>
  <style>
    :root {
      color-scheme: light dark;
      --bg: #0f172a;
      --panel: #111827;
      --text: #e5e7eb;
      --muted: #9ca3af;
      --accent: #38bdf8;
      --accent-2: #a78bfa;
      --border: #1f2937;
      --code-bg: #0b1220;
    }
    body {
      margin: 0;
      font-family: "Inter", "Segoe UI", system-ui, -apple-system, sans-serif;
      background: var(--bg);
      color: var(--text);
    }
    header {
      padding: 32px 40px 16px;
      border-bottom: 1px solid var(--border);
      background: linear-gradient(120deg, rgba(56, 189, 248, 0.15), rgba(167, 139, 250, 0.12));
    }
    header h1 {
      margin: 0 0 8px;
      font-size: 30px;
    }
    header p {
      margin: 0;
      color: var(--muted);
    }
    main {
      display: grid;
      grid-template-columns: 280px 1fr;
      gap: 32px;
      padding: 32px 40px;
    }
    nav {
      position: sticky;
      top: 24px;
      align-self: start;
      background: var(--panel);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 16px;
    }
    nav h3 {
      margin: 0 0 12px;
      font-size: 14px;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--muted);
    }
    nav a {
      display: block;
      padding: 8px 10px;
      color: var(--text);
      text-decoration: none;
      border-radius: 8px;
      font-size: 14px;
    }
    nav a:hover {
      background: rgba(56, 189, 248, 0.12);
    }
    section {
      background: var(--panel);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 24px;
      margin-bottom: 24px;
    }
    section h2 {
      margin-top: 0;
      border-bottom: 1px solid var(--border);
      padding-bottom: 8px;
    }
    section h3 {
      margin-bottom: 8px;
    }
    code, pre {
      font-family: "JetBrains Mono", "Fira Code", ui-monospace, SFMono-Regular, Menlo, monospace;
    }
    pre {
      background: var(--code-bg);
      padding: 16px;
      border-radius: 10px;
      overflow-x: auto;
      border: 1px solid var(--border);
    }
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 12px 0 0;
    }
    th, td {
      text-align: left;
      border-bottom: 1px solid var(--border);
      padding: 10px 8px;
      vertical-align: top;
    }
    th {
      color: var(--muted);
      font-size: 13px;
      text-transform: uppercase;
      letter-spacing: 0.06em;
    }
    .badge {
      display: inline-block;
      padding: 3px 10px;
      font-size: 12px;
      border-radius: 999px;
      background: rgba(56, 189, 248, 0.15);
      color: var(--accent);
    }
    .grid-2 {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
    }
    .callout {
      border-left: 3px solid var(--accent);
      background: rgba(56, 189, 248, 0.08);
      padding: 12px 14px;
      border-radius: 8px;
      color: var(--muted);
    }
    footer {
      padding: 24px 40px 40px;
      color: var(--muted);
      font-size: 12px;
    }
    @media (max-width: 980px) {
      main {
        grid-template-columns: 1fr;
      }
      nav {
        position: relative;
      }
    }
  </style>
</head>
<body>
  <header>
    <h1>MineLauncher RPG – Admin Wiki</h1>
    <p>Professionelles Administrationshandbuch für PaperMC 1.20.4+, inklusive Build-System, Dungeons, Gilden, Handel, Behavior-Editor und Permissions.</p>
  </header>
  <main>
    <nav>
      <h3>Inhalt</h3>
      <a href="#overview">Überblick</a>
      <a href="#requirements">Voraussetzungen</a>
      <a href="#installation">Installation & Datenbank</a>
      <a href="#core-config">Kern-Konfiguration</a>
      <a href="#launcher-cli">Launcher-CLI</a>
      <a href="#player-commands">Spielerbefehle</a>
      <a href="#party-guild">Party & Gilden</a>
      <a href="#economy">Handel & Wirtschaft</a>
      <a href="#dungeons-pvp">Dungeons, PvP & Voice</a>
      <a href="#admin-commands">Admin-Befehle</a>
      <a href="#permissions">Permissions-System</a>
      <a href="#skills-classes">Skills & Klassen</a>
      <a href="#loot-shops">Loot & Shops</a>
      <a href="#zones-mobs">Zonen & Mobs</a>
      <a href="#quests">Quests</a>
      <a href="#buildings">Gebäude & Schemata</a>
      <a href="#behavior-editor">Behavior-Editor</a>
      <a href="#pvp">PvP & Elo</a>
      <a href="#troubleshooting">Troubleshooting</a>
      <a href="#faq">FAQ</a>
      <a href="#glossary">Glossar</a>
    </nav>

    <div>
      <section id="overview">
        <h2>Überblick</h2>
        <p>MineLauncher RPG ist ein vollwertiges MMORPG-Framework für PaperMC-Server. Es bietet Klassen, Skills, Quests, Dungeons, Gilden, Handel, PvP, Permissions und ein Build-System mit nativen Sponge-Schematics.</p>
        <div class="grid-2">
          <div class="callout">
            <strong>Highlight:</strong> Native <code>.schem</code>-Engine ohne WorldEdit, inklusive BlockEntities, Entities und Undo.
          </div>
          <div class="callout">
            <strong>Hinweis:</strong> PostgreSQL ist zwingend erforderlich für persistente Spielerdaten.
          </div>
        </div>
      </section>

      <section id="requirements">
        <h2>Voraussetzungen</h2>
        <ul>
          <li><span class="badge">Server</span> PaperMC 1.20.4+</li>
          <li><span class="badge">Java</span> JDK 17+</li>
          <li><span class="badge">Datenbank</span> PostgreSQL (Pflicht)</li>
        </ul>
      </section>

      <section id="installation">
        <h2>Installation & Datenbank</h2>
        <ol>
          <li>Plugin <code>rpg-plugin-1.0.0.jar</code> in den <code>plugins/</code>-Ordner legen.</li>
          <li>Server starten und wieder stoppen, um Configs zu generieren.</li>
          <li><code>plugins/RPGPlugin/config.yml</code> öffnen und PostgreSQL-Zugangsdaten setzen.</li>
        </ol>
        <pre><code>database:
  host: localhost
  port: 5432
  name: rpg_db
  user: dein_user
  password: dein_passwort
  poolSize: 10
</code></pre>
        <p>Beim Start erstellt das Plugin automatisch die Tabellen.</p>
      </section>

      <section id="core-config">
        <h2>Kern-Konfiguration</h2>
        <h3>Wichtige Dateien</h3>
        <ul>
          <li><code>config.yml</code> – globale Settings (Datenbank, Dungeon, Schematics, Permissions)</li>
          <li><code>skills.yml</code> – Skills</li>
          <li><code>classes.yml</code> – Klassen</li>
          <li><code>mobs.yml</code> – Custom Mobs</li>
          <li><code>loot.yml</code> – Loot Tabellen</li>
          <li><code>shops.yml</code> – Shops</li>
          <li><code>buildings.yml</code> – Gebäudedefinitionen</li>
        </ul>
      </section>

      <section id="launcher-cli">
        <h2>Launcher-CLI (außerhalb des Spiels)</h2>
        <p>Der C#-Launcher unterstützt einen CLI-Start für den Client:</p>
        <pre><code>dotnet run --project C_launcher -- play [Name]
# Beispiel
dotnet run --project C_launcher -- play Alex
</code></pre>
      </section>

      <section id="player-commands">
        <h2>Spielerbefehle</h2>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/rpg</code></td><td>RPG-Menü öffnen</td><td><code>/rpg</code></td></tr>
            <tr><td><code>/rpg skill &lt;id&gt;</code></td><td>Skill aktivieren</td><td><code>/rpg skill dash</code></td></tr>
            <tr><td><code>/rpg quest list</code></td><td>Questliste öffnen</td><td><code>/rpg quest list</code></td></tr>
            <tr><td><code>/rpg quest abandon &lt;id&gt;</code></td><td>Quest abbrechen</td><td><code>/rpg quest abandon starter</code></td></tr>
            <tr><td><code>/rpg quest complete &lt;id&gt;</code></td><td>Quest prüfen/abschließen</td><td><code>/rpg quest complete starter</code></td></tr>
            <tr><td><code>/rpg respec</code></td><td>Respec durchführen</td><td><code>/rpg respec</code></td></tr>
            <tr><td><code>/rpg class list</code></td><td>Klassen anzeigen</td><td><code>/rpg class list</code></td></tr>
            <tr><td><code>/rpg class choose &lt;id&gt;</code></td><td>Klasse wählen</td><td><code>/rpg class choose mage</code></td></tr>
            <tr><td><code>/rpg bind &lt;slot 1-9&gt; &lt;skillId&gt;</code></td><td>Skill binden</td><td><code>/rpg bind 2 heal</code></td></tr>
            <tr><td><code>/rpg money</code></td><td>Gold anzeigen</td><td><code>/rpg money</code></td></tr>
            <tr><td><code>/rpg pay &lt;player&gt; &lt;amount&gt;</code></td><td>Gold senden</td><td><code>/rpg pay Alex 50</code></td></tr>
            <tr><td><code>/rpg profession list</code></td><td>Berufe anzeigen</td><td><code>/rpg profession list</code></td></tr>
            <tr><td><code>/rpg profession set &lt;name&gt; &lt;level&gt;</code></td><td>Beruf setzen</td><td><code>/rpg profession set mining 5</code></td></tr>
            <tr><td><code>/rpg skilltree</code></td><td>Skillbaum öffnen</td><td><code>/rpg skilltree</code></td></tr>
          </tbody>
        </table>
      </section>

      <section id="party-guild">
        <h2>Party & Gilden</h2>
        <h3>Party</h3>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/party create</code></td><td>Party erstellen</td><td><code>/party create</code></td></tr>
            <tr><td><code>/party invite &lt;player&gt;</code></td><td>Einladen</td><td><code>/party invite Alex</code></td></tr>
            <tr><td><code>/party join &lt;leader&gt;</code></td><td>Beitreten</td><td><code>/party join Alex</code></td></tr>
            <tr><td><code>/party leave</code></td><td>Verlassen</td><td><code>/party leave</code></td></tr>
            <tr><td><code>/party chat &lt;message&gt;</code></td><td>Party-Chat</td><td><code>/party chat Hallo</code></td></tr>
            <tr><td><code>/p &lt;...&gt;</code></td><td>Shortcut für Party</td><td><code>/p chat Hi</code></td></tr>
          </tbody>
        </table>
        <h3>Gilden</h3>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/guild create &lt;id&gt; &lt;name&gt;</code></td><td>Gilde erstellen</td><td><code>/guild create knights "Die Ritter"</code></td></tr>
            <tr><td><code>/guild invite &lt;player&gt;</code></td><td>Einladen</td><td><code>/guild invite Alex</code></td></tr>
            <tr><td><code>/guild accept</code></td><td>Einladung annehmen</td><td><code>/guild accept</code></td></tr>
            <tr><td><code>/guild leave</code></td><td>Gilde verlassen</td><td><code>/guild leave</code></td></tr>
            <tr><td><code>/guild disband</code></td><td>Gilde auflösen</td><td><code>/guild disband</code></td></tr>
            <tr><td><code>/guild info</code></td><td>Infos anzeigen</td><td><code>/guild info</code></td></tr>
            <tr><td><code>/guild chat &lt;message&gt;</code></td><td>Gilden-Chat</td><td><code>/guild chat Hallo</code></td></tr>
            <tr><td><code>/g &lt;message&gt;</code></td><td>Shortcut Chat</td><td><code>/g Hallo</code></td></tr>
            <tr><td><code>/guild bank balance</code></td><td>Bankstand</td><td><code>/guild bank balance</code></td></tr>
            <tr><td><code>/guild bank deposit &lt;amount&gt;</code></td><td>Einzahlen</td><td><code>/guild bank deposit 100</code></td></tr>
            <tr><td><code>/guild bank withdraw &lt;amount&gt;</code></td><td>Abheben</td><td><code>/guild bank withdraw 50</code></td></tr>
            <tr><td><code>/guild quest list</code></td><td>Quests anzeigen</td><td><code>/guild quest list</code></td></tr>
            <tr><td><code>/guild quest create &lt;id&gt; &lt;goal&gt; &lt;name&gt;</code></td><td>Quest erstellen</td><td><code>/guild quest create wolfhunt 25 "Wolfjagd"</code></td></tr>
            <tr><td><code>/guild quest progress &lt;id&gt; &lt;amount&gt;</code></td><td>Fortschritt</td><td><code>/guild quest progress wolfhunt 5</code></td></tr>
            <tr><td><code>/guild quest complete &lt;id&gt;</code></td><td>Abschließen</td><td><code>/guild quest complete wolfhunt</code></td></tr>
          </tbody>
        </table>
      </section>

      <section id="economy">
        <h2>Handel & Wirtschaft</h2>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/auction list</code></td><td>Auktionen anzeigen</td><td><code>/auction list</code></td></tr>
            <tr><td><code>/auction sell &lt;price&gt;</code></td><td>Item verkaufen</td><td><code>/auction sell 250</code></td></tr>
            <tr><td><code>/auction buy &lt;id&gt;</code></td><td>Auktion kaufen</td><td><code>/auction buy 1a2b3c4d</code></td></tr>
            <tr><td><code>/trade request &lt;player&gt;</code></td><td>Handel anfragen</td><td><code>/trade request Alex</code></td></tr>
            <tr><td><code>/trade accept</code></td><td>Handel annehmen</td><td><code>/trade accept</code></td></tr>
            <tr><td><code>/trade offer &lt;gold&gt;</code></td><td>Gold anbieten</td><td><code>/trade offer 100</code></td></tr>
            <tr><td><code>/trade requestgold &lt;gold&gt;</code></td><td>Gold verlangen</td><td><code>/trade requestgold 50</code></td></tr>
            <tr><td><code>/trade ready</code></td><td>Handel bestätigen</td><td><code>/trade ready</code></td></tr>
            <tr><td><code>/trade cancel</code></td><td>Handel abbrechen</td><td><code>/trade cancel</code></td></tr>
          </tbody>
        </table>
      </section>

      <section id="dungeons-pvp">
        <h2>Dungeons, PvP & Voice</h2>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/dungeon enter</code></td><td>Dungeon betreten</td><td><code>/dungeon enter</code></td></tr>
            <tr><td><code>/dungeon leave</code></td><td>Dungeon verlassen</td><td><code>/dungeon leave</code></td></tr>
            <tr><td><code>/dungeon generate &lt;theme&gt;</code></td><td>Dungeon generieren</td><td><code>/dungeon generate wfc</code></td></tr>
            <tr><td><code>/pvp join</code></td><td>PvP-Queue</td><td><code>/pvp join</code></td></tr>
            <tr><td><code>/pvp top</code></td><td>Rangliste</td><td><code>/pvp top</code></td></tr>
            <tr><td><code>/voicechat party</code></td><td>Party-Voice</td><td><code>/voicechat party</code></td></tr>
            <tr><td><code>/voicechat guild</code></td><td>Gilden-Voice</td><td><code>/voicechat guild</code></td></tr>
            <tr><td><code>/voicechat leave</code></td><td>Voice verlassen</td><td><code>/voicechat leave</code></td></tr>
            <tr><td><code>/lootchat [true|false]</code></td><td>Lootchat umschalten</td><td><code>/lootchat false</code></td></tr>
          </tbody>
        </table>
      </section>

      <section id="admin-commands">
        <h2>Admin-Befehle</h2>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/rpgadmin</code></td><td>Admin-Menü öffnen</td><td><code>/rpgadmin</code></td></tr>
            <tr><td><code>/rpgadmin wand</code></td><td>Editor-Wand (Pos1/Pos2)</td><td><code>/rpgadmin wand</code></td></tr>
            <tr><td><code>/rpgadmin zone create &lt;id&gt;</code></td><td>Zone erstellen</td><td><code>/rpgadmin zone create startzone</code></td></tr>
            <tr><td><code>/rpgadmin zone setlevel &lt;id&gt; &lt;min&gt; &lt;max&gt;</code></td><td>Level-Range</td><td><code>/rpgadmin zone setlevel startzone 1 10</code></td></tr>
            <tr><td><code>/rpgadmin zone setmod &lt;id&gt; &lt;slow&gt; &lt;damage&gt;</code></td><td>Zone-Modifier</td><td><code>/rpgadmin zone setmod startzone 0.9 1.1</code></td></tr>
            <tr><td><code>/rpgadmin npc create &lt;id&gt; &lt;role&gt;</code></td><td>NPC erstellen</td><td><code>/rpgadmin npc create guide QUESTGIVER</code></td></tr>
            <tr><td><code>/rpgadmin npc dialog &lt;id&gt;</code></td><td>Dialog setzen</td><td><code>/rpgadmin npc dialog guide</code></td></tr>
            <tr><td><code>/rpgadmin npc linkquest &lt;npcId&gt; &lt;questId&gt;</code></td><td>Quest verlinken</td><td><code>/rpgadmin npc linkquest guide starter</code></td></tr>
            <tr><td><code>/rpgadmin npc linkshop &lt;npcId&gt; &lt;shopId&gt;</code></td><td>Shop verlinken</td><td><code>/rpgadmin npc linkshop guide village_merchant</code></td></tr>
            <tr><td><code>/rpgadmin quest create &lt;id&gt; &lt;name&gt;</code></td><td>Quest erstellen</td><td><code>/rpgadmin quest create starter "Start"</code></td></tr>
            <tr><td><code>/rpgadmin quest addstep &lt;id&gt; &lt;type&gt; &lt;target&gt; &lt;amount&gt;</code></td><td>Quest-Step</td><td><code>/rpgadmin quest addstep starter KILL ZOMBIE 3</code></td></tr>
            <tr><td><code>/rpgadmin loot create &lt;id&gt; &lt;appliesTo&gt;</code></td><td>Loot-Tabelle</td><td><code>/rpgadmin loot create forest ZOMBIE</code></td></tr>
            <tr><td><code>/rpgadmin loot addentry &lt;id&gt; &lt;material&gt; &lt;chance&gt; &lt;min&gt; &lt;max&gt; &lt;rarity&gt;</code></td><td>Loot-Entry</td><td><code>/rpgadmin loot addentry forest GOLD_NUGGET 0.5 1 3 COMMON</code></td></tr>
            <tr><td><code>/rpgadmin skill create &lt;id&gt;</code></td><td>Skill erstellen</td><td><code>/rpgadmin skill create dash</code></td></tr>
            <tr><td><code>/rpgadmin skill set &lt;id&gt; &lt;cooldown|mana|category|type|name|requires&gt; &lt;value&gt;</code></td><td>Skill-Parameter</td><td><code>/rpgadmin skill set dash cooldown 3</code></td></tr>
            <tr><td><code>/rpgadmin skill addeffect &lt;id&gt; &lt;effectType&gt; &lt;param:value&gt;...</code></td><td>Skill-Effekt</td><td><code>/rpgadmin skill addeffect dash VELOCITY x:0.8 y:0.3</code></td></tr>
            <tr><td><code>/rpgadmin mob spawn &lt;mobId&gt;</code></td><td>Custom-Mob spawnen</td><td><code>/rpgadmin mob spawn forest_zombie</code></td></tr>
            <tr><td><code>/rpgadmin spawner create &lt;id&gt; &lt;zoneId&gt;</code></td><td>Spawner erstellen</td><td><code>/rpgadmin spawner create forest_spawn startzone</code></td></tr>
            <tr><td><code>/rpgadmin spawner addmob &lt;id&gt; &lt;mobId&gt; &lt;chance&gt;</code></td><td>Mob hinzufügen</td><td><code>/rpgadmin spawner addmob forest_spawn forest_zombie 1.0</code></td></tr>
            <tr><td><code>/rpgadmin spawner setlimit &lt;id&gt; &lt;amount&gt;</code></td><td>Spawn-Limit</td><td><code>/rpgadmin spawner setlimit forest_spawn 8</code></td></tr>
            <tr><td><code>/rpgadmin build</code> / <code>build gui</code></td><td>Bau-Manager GUI</td><td><code>/rpgadmin build gui</code></td></tr>
            <tr><td><code>/rpgadmin build &lt;id&gt;</code></td><td>Gebäude platzieren</td><td><code>/rpgadmin build cottage</code></td></tr>
            <tr><td><code>/rpgadmin build undo</code></td><td>Letzte Platzierung rückgängig</td><td><code>/rpgadmin build undo</code></td></tr>
            <tr><td><code>/rpgadmin build move</code></td><td>Move-GUI öffnen</td><td><code>/rpgadmin build move</code></td></tr>
            <tr><td><code>/rpgadmin perms</code></td><td>Permissions-GUI</td><td><code>/rpgadmin perms</code></td></tr>
            <tr><td><code>/behavior edit &lt;tree&gt;</code></td><td>Behavior-Editor</td><td><code>/behavior edit skeleton_king</code></td></tr>
          </tbody>
        </table>
      </section>

      <section id="permissions">
        <h2>Permissions-System</h2>
        <p>Rollen und Nodes können komplett In-Game verwaltet werden.</p>
        <table>
          <thead>
            <tr><th>Befehl</th><th>Beschreibung</th><th>Beispiel</th></tr>
          </thead>
          <tbody>
            <tr><td><code>/rpgadmin perms role create &lt;key&gt; &lt;displayName&gt;</code></td><td>Rolle erstellen</td><td><code>/rpgadmin perms role create mod "Moderator"</code></td></tr>
            <tr><td><code>/rpgadmin perms role delete &lt;key&gt;</code></td><td>Rolle löschen</td><td><code>/rpgadmin perms role delete mod</code></td></tr>
            <tr><td><code>/rpgadmin perms role rename &lt;key&gt; &lt;displayName&gt;</code></td><td>Rolle umbenennen</td><td><code>/rpgadmin perms role rename mod "Mod"</code></td></tr>
            <tr><td><code>/rpgadmin perms role parent add &lt;role&gt; &lt;parent&gt;</code></td><td>Parent hinzufügen</td><td><code>/rpgadmin perms role parent add mod admin</code></td></tr>
            <tr><td><code>/rpgadmin perms role parent remove &lt;role&gt; &lt;parent&gt;</code></td><td>Parent entfernen</td><td><code>/rpgadmin perms role parent remove mod admin</code></td></tr>
            <tr><td><code>/rpgadmin perms role node &lt;role&gt; &lt;node&gt; &lt;allow|deny|inherit&gt;</code></td><td>Node setzen</td><td><code>/rpgadmin perms role node mod rpg.admin allow</code></td></tr>
            <tr><td><code>/rpgadmin perms user setprimary &lt;player&gt; &lt;role&gt;</code></td><td>Primary-Rolle</td><td><code>/rpgadmin perms user setprimary Alex mod</code></td></tr>
            <tr><td><code>/rpgadmin perms user add &lt;player&gt; &lt;role&gt;</code></td><td>Rolle hinzufügen</td><td><code>/rpgadmin perms user add Alex mod</code></td></tr>
            <tr><td><code>/rpgadmin perms user remove &lt;player&gt; &lt;role&gt;</code></td><td>Rolle entfernen</td><td><code>/rpgadmin perms user remove Alex mod</code></td></tr>
            <tr><td><code>/rpgadmin perms user info &lt;player&gt; &lt;node&gt;</code></td><td>Node prüfen</td><td><code>/rpgadmin perms user info Alex rpg.admin</code></td></tr>
          </tbody>
        </table>
        <p>Zusätzlich gibt es eine GUI mit Rollenliste, Role-Details, Parent-Vererbung, Nodes und Audit-Log.</p>
      </section>

      <section id="skills-classes">
        <h2>Skills & Klassen</h2>
        <h3>Skill-Beispiel</h3>
        <pre><code>fireball:
  name: "Feuerball"
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
</code></pre>
        <h3>Klassen-Beispiel</h3>
        <pre><code>mage:
  name: "&9Magier"
  startSkills:
  - fireball
</code></pre>
      </section>

      <section id="loot-shops">
        <h2>Loot & Shops</h2>
        <h3>Loot-Tabelle</h3>
        <pre><code>starter_loot:
  appliesTo: ZOMBIE
  entries:
  - material: GOLD_NUGGET
    chance: 0.8
    minAmount: 1
    maxAmount: 3
    rarity: COMMON
</code></pre>
        <h3>Shop</h3>
        <pre><code>village_merchant:
  title: "Dorfhändler"
  items:
    - slot: 0
      material: GOLD_NUGGET
      name: "&eGoldklumpen"
      buyPrice: 0
      sellPrice: 10
</code></pre>
      </section>

      <section id="zones-mobs">
        <h2>Zonen & Mobs</h2>
        <h3>Zone setzen</h3>
        <ol>
          <li><code>/rpgadmin wand</code> → Bereich markieren</li>
          <li><code>/rpgadmin zone create wald_zone</code></li>
          <li><code>/rpgadmin zone setlevel wald_zone 1 10</code></li>
        </ol>
        <h3>Mob-Definition</h3>
        <pre><code>forest_zombie:
  name: "&2Waldschlurfer"
  type: ZOMBIE
  health: 30
  damage: 5
  xp: 15
  lootTable: starter_loot
</code></pre>
      </section>

      <section id="quests">
        <h2>Quests</h2>
        <ol>
          <li><code>/rpgadmin quest create wald_reinigung "Reinigung des Waldes"</code></li>
          <li><code>/rpgadmin quest addstep wald_reinigung KILL ZOMBIE 5</code></li>
          <li><code>/rpgadmin quest addstep wald_reinigung COLLECT GOLD_NUGGET 3</code></li>
        </ol>
      </section>

      <section id="buildings">
        <h2>Gebäude & Schemata</h2>
        <h3>Schematics bereitstellen</h3>
        <p>Lege <code>.schem</code>-Dateien unter <code>plugins/RPGPlugin/schematics/</code> ab.</p>
        <h3>Gebäude definieren</h3>
        <pre><code>buildings:
  cottage:
    name: "Kleines Wohnhaus"
    category: RESIDENTIAL
    schematic: "cottage_ground.schem"
    floorSchematic: "cottage_floor.schem"
    minFloors: 1
    maxFloors: 2
    floorHeight: 5
    basement:
      schematic: "cottage_basement.schem"
      depth: 4
</code></pre>
        <h3>Einzel-Schema platzieren</h3>
        <ol>
          <li>Admin-Menü öffnen: <code>/rpgadmin</code></li>
          <li>Bau-Manager → Einzel-Schema</li>
          <li>Dateiname eingeben (z. B. <code>haus.schem</code>)</li>
          <li>Rechtsklick zum Platzieren</li>
        </ol>
        <h3>Undo</h3>
        <p><code>/rpgadmin build undo</code> macht die letzte Platzierung rückgängig.</p>
      </section>

      <section id="behavior-editor">
        <h2>Behavior-Editor</h2>
        <p>Der Behavior-Editor verwaltet KI-Bäume für Custom Mobs.</p>
        <pre><code>/behavior edit skeleton_king</code></pre>
        <h3>Beispiel-Flow</h3>
        <ul>
          <li><strong>Selector</strong>
            <ul>
              <li>Sequence → Cooldown(8s) → CastSkill(ember_shot)</li>
              <li>MeleeAttack</li>
            </ul>
          </li>
        </ul>
      </section>

      <section id="pvp">
        <h2>PvP & Elo</h2>
        <p><code>/pvp join</code> startet ein Matchmaking. Arenen werden in <code>arenas.yml</code> definiert.</p>
      </section>

      <section id="troubleshooting">
        <h2>Troubleshooting</h2>
        <ul>
          <li><strong>"Unsupported schematic format"</strong> → Nur Sponge v2 (.schem) verwenden.</li>
          <li><strong>DB-Fehler</strong> → Zugangsdaten prüfen, PostgreSQL erreichbar?</li>
          <li><strong>GUI öffnet nicht</strong> → Permissions prüfen (<code>rpg.admin</code>).</li>
        </ul>
      </section>

      <section id="faq">
        <h2>FAQ</h2>
        <h3>Wie platziere ich nur ein einzelnes Schema?</h3>
        <p>Über das Admin-Menü → Bau-Manager → Einzel-Schema.</p>
        <h3>Wie kann ich eine Platzierung rückgängig machen?</h3>
        <p><code>/rpgadmin build undo</code> macht die letzte Platzierung pro Admin rückgängig.</p>
      </section>

      <section id="glossary">
        <h2>Glossar</h2>
        <ul>
          <li><strong>Behavior Tree</strong> – Graphische KI-Logik für Mobs.</li>
          <li><strong>Schematic</strong> – Vorlage eines Bauwerks als <code>.schem</code>.</li>
          <li><strong>Spawner</strong> – Automatisches Mob-Spawnsystem.</li>
          <li><strong>WFC</strong> – Wave Function Collapse, prozedurale Dungeon-Generierung.</li>
        </ul>
      </section>
    </div>
  </main>
  <footer>
    MineLauncher RPG – Admin Wiki · Stand: v1.0.0
  </footer>
</body>
</html>

```

