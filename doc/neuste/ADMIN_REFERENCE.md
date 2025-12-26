# 🛠️ MineLauncherRPG – Admin Referenz

Technische Dokumentation aller Befehle, Permissions, Konfigurationen und Datentypen für Server-Administratoren.

---

## 1. Installation & Datenbank (Wichtig!)
Das Plugin benötigt zwingend eine PostgreSQL-Datenbank.

**`config.yml`**:
```yaml
database:
  host: localhost
  port: 5432
  name: rpg_db      # Datenbank muss existieren (oder User muss Rechte haben, sie zu erstellen)
  user: rpg_user
  password: secure_password
  poolSize: 10      # Verbindungen im Pool (Standard: 10)
```

---

## 2. Berechtigungen (Permissions)
| Node | Beschreibung | Standard |
| :--- | :--- | :--- |
| `rpg.admin.*` | Voller Zugriff auf alles. | OP |
| `rpg.admin` | Zugriff auf das `/rpgadmin` Menü & Befehle. | OP |
| `rpg.editor` | Nutzung der Editor-Wand. | OP |
| `rpg.debug` | Sieht technische Infos (Zone, Quest-Status) in der Actionbar. | OP |

---

## 3. Welt-Management (In-Game Befehle)

### Editor Wand
- Befehl: `/rpgadmin wand`
- **Links-Klick:** Pos1 setzen.
- **Rechts-Klick:** Pos2 setzen.

### Zonen (Regionen)
Zonen definieren Bereiche für Level-Ranges, Monster-Spawns und Umgebungs-Effekte.
- `/rpgadmin zone create <id>` (Erstellt Zone aus Wand-Selektion)
- `/rpgadmin zone setlevel <id> <min> <max>` (Definiert Level-Anforderung)
- `/rpgadmin zone setmod <id> <slow> <damage>` (Multiplikatoren: 1.0 = normal)

### Spawner (Automatisches Füllen)
Füllt Zonen automatisch mit Mobs aus der `mobs.yml`.
- `/rpgadmin spawner create <spawnerId> <zoneId>`
- `/rpgadmin spawner addmob <spawnerId> <mobId> <chance>` (Chance: 0.1 bis 1.0)
- `/rpgadmin spawner setlimit <spawnerId> <anzahl>` (Limit aktiver Mobs in dieser Zone)

### Dungeons (Instanzen)
Erstellt temporäre Welten basierend auf WFC (Wave Function Collapse).
- `/dungeon generate wfc` (Generiert einen zufälligen Dungeon für die Party)
- **Konfiguration:** Start/End-Punkte müssen in der `config.yml` definiert sein (siehe unten).

---

## 4. Content-Erstellung (In-Game Befehle)

### NPCs
- `/rpgadmin npc create <id> <Rolle>`
  - Rollen: `QUESTGIVER`, `VENDOR`, `TRAINER`, `TELEPORTER`, `BANKER`
- `/rpgadmin npc dialog <id>` (Danach Text in den Chat tippen)
- `/rpgadmin npc linkquest <npcId> <questId>` (Verknüpft Queststart)
- `/rpgadmin npc linkshop <npcId> <shopId>` (Verknüpft Händler-GUI)

### Quests
- `/rpgadmin quest create <id> <Anzeigename>`
- `/rpgadmin quest addstep <id> <Typ> <Ziel> <Menge>`
  - Typen: `KILL` (MobID), `COLLECT` (Material), `TALK` (NPC), `EXPLORE` (ZoneID)

### Loot Tables
Steuert Drops von Mobs.
- `/rpgadmin loot create <id> <MobTyp>` (z.B. ZOMBIE)
- `/rpgadmin loot addentry <id> <Material> <Chance> <Min> <Max> <Rarity>`
  - Beispiel: `/rpgadmin loot addentry wald_loot GOLD_NUGGET 0.5 1 3 COMMON`

### Skills (Editor)
- `/rpgadmin skill create <id>`
- `/rpgadmin skill set <id> <mana|cooldown|type|category> <wert>`
- `/rpgadmin skill addeffect <id> <EFFEKT> <param:wert>...`
  - **Effekte:** `DAMAGE`, `HEAL`, `PROJECTILE`, `POTION`, `SOUND`, `XP`, `PARTICLE`, `VELOCITY`, `AGGRO`
  - *Beispiel:* `... addeffect DAMAGE amount:10 radius:3`

---

## 5. Dateistruktur & YAML-Konfigurationen

Einige komplexe Features werden direkt in den Dateien konfiguriert.

### `mobs.yml` (Custom Mobs)
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
  behaviorTree: skeleton_king # Verweist auf behaviors/skeleton_king.yml
```

### `classes.yml` (Klassen)
```yaml
warrior:
  name: "Krieger"
  startSkills:
  - taunt
  - smash
```

### `arenas.yml` (PvP)
Arenen müssen aktuell manuell per Koordinaten konfiguriert werden.
```yaml
arena1:
  world: world
  pos1: {x: -10, y: 60, z: -10}
  pos2: {x: 10, y: 70, z: 10}
  spawn1: {x: -5, y: 65, z: 0}
  spawn2: {x: 5, y: 65, z: 0}
```

### `config.yml` (Dungeon Locations)
Definiert, wo Spieler landen, wenn sie Dungeons betreten oder verlassen (falls der Dungeon gelöscht wird).
```yaml
dungeon:
  entrance:
    world: world
    x: 100.5
    y: 64
    z: 100.5
  exit:
    world: world
    x: 100.5
    y: 64
    z: 100.5
```