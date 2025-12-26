# ⚔️ MineLauncher RPG - Admin Handbuch

Willkommen beim **MineLauncher RPG System**. Dieses Plugin verwandelt einen PaperMC Server in ein voll funktionsfähiges MMORPG mit Klassen, Skills, Dungeons, Gilden und einer persistenten SQL-Datenbank.

## 📋 Voraussetzungen
*   **Server:** PaperMC 1.20.4 (oder neuer)
*   **Java:** JDK 17+
*   **Datenbank:** PostgreSQL (zwingend erforderlich für Spielerdaten)

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
