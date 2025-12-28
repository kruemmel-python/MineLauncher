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
  class: mage
  minLevel: 3
  cooldown: 5
  manaCost: 20
  tags: [burst, fire]
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
  class: warrior
  minLevel: 3
  cooldown: 8
  manaCost: 15
  tags: [melee]
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
  presets: {}

warrior:
  name: "&cKrieger"
  startSkills:
  - smash
  presets: {}
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

### 2.1 Enchant-Rezepte verwalten (Admin-GUI)
* `/rpgadmin` → **Verzauberungen** öffnen
* Rezepte erstellen/bearbeiten/löschen (wird in `enchantments.yml` gespeichert)

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

## 🌐 Teil 4.5: Welt‑Events & Meta‑Quests

1. **Event erstellen (Zone‑basiert):**
   * `/rpgadmin event create invasion wald_zone "Wald‑Invasion"`
   * `/rpgadmin event addstep invasion KILL ZOMBIE 50`
   * `/rpgadmin event reward invasion 250 200`
2. **Quest erst nach Event freischalten:**
   * `/rpgadmin event unlock invasion heldenpfad`
   * Danach in `quests.yml` wird `requiredEvent: invasion` gesetzt.
3. **Event starten/stoppen:**
   * `/rpgadmin event start invasion`
   * `/rpgadmin event stop invasion`

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

### 3.1 Worldbuilding (Bereich löschen)
1.  Admin-Menü → **Worldbuilding**.
2.  Pos1/Pos2 setzen (Editor‑Wand).
3.  **Bereich löschen** anklicken → markierte Area wird geleert.

### 4. Einzel-Schema platzieren (GUI)
1.  Öffne das Admin-Menü: `/rpgadmin`
2.  Klicke **Bau-Manager** → **Einzel-Schema**.
3.  Gib den Dateinamen ein (z.B. `haus.schem`).
4.  Rechtsklick auf einen Block, um nur dieses Schema zu platzieren.

### 5. Platzierung rückgängig machen
*   `/rpgadmin build undo` macht die letzte Platzierung für den Admin rückgängig.

---

## ⛏️ Teil 5.5: Ressourcen‑Nodes & Berufe

Erstelle respawnende Sammelknoten mit Beruf‑XP:

```
/rpgadmin node create mining IRON_ORE 60 8
```

Spieler können so **Mining‑XP** gezielt farmen (respawnt nach 60s).

---

## 📦 Teil 5.6: Crafting‑Aufträge (Spielerwirtschaft)

Spieler können Aufträge erstellen und andere erfüllen:

```
/rpg order create IRON_SWORD 2 250
/rpg order list
/rpg order fulfill order_1
```

---

## 🤝 Teil 6: Social & End-Game

### 1. Dungeons (Instanziert)
Erstellt eine temporäre Welt für eine Gruppe.
*   **Befehl:** `/dungeon generate wfc` (Wave Function Collapse) oder `/dungeon generate gruft` (Standard).
*   **Logik:** Generiert Welt → Teleportiert Party → Spawnt Boss → Löscht Welt nach Abschluss/Timeout.
*   **Matchmaking:** `/dungeon queue wfc`, Rolle setzen: `/dungeon role <tank|heal|dps>`

### 2. Gilden & Party
*   **Party:** `/party invite <Name>` (Teilt XP im Umkreis).
*   **Gilde:** `/guild create <ID> <Name>` (Gilden mit Bank und Quests).
*   **Gildenhalle:** `/guild hall set` (Leader), `/guild hall upgrade <craft|teleport|buff>`

### 3. Auktionshaus
*   Item in die Hand nehmen → `/auction sell <Preis>`.

### 4. PvP & Elo
*   Arenen müssen in `arenas.yml` konfiguriert werden.
*   Spieler nutzen `/pvp join`, um in die Warteschlange für Elo-Matches zu kommen.
*   **Saison:** `/pvp season` (Anzeige), `/rpgadmin pvp seasonstart <id> <name> <days>`

### 5. Welten verwalten (WorldCreatorPlugin)
* `/worlds` öffnet das World‑Creator‑Menü.
* `/worlds list` listet alle Welten.
* `/worlds tp <welt>` teleportiert zur Welt.

---

## 🏳️ Teil 6.5: Fraktionen & NPC‑Gating

1. **Fraktion definieren:** `factions.yml` mit Rängen (Neutral/Freundlich/Ehrfürchtig).
2. **NPC an Fraktion binden:**
   * `/rpgadmin npc faction wache adventurers`
   * `/rpgadmin npc rank wache revered`
3. **Effekte:**
   * Shop‑Rabatte je Rang.
   * Dungeon‑Zugang über Rang‑Flag.

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
