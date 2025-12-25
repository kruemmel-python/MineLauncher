# MineLauncherRPG – Handbuch (In‑Game)

Dieses Handbuch beschreibt die Bedienung **in‑game** für Spieler und Admins sowie das Erstellen eines Admins.

---

## 1) Admin erstellen (Berechtigungen)

Das Plugin nutzt Bukkit‑Permissions. Du kannst Adminrechte auf zwei Wegen vergeben:

### Variante A: OP (schnell)
1. **Konsole** öffnen.
2. `op <Spielername>` ausführen.
3. Der Spieler hat jetzt Zugriff auf alle `rpg.*` Rechte.

### Variante B: Permissions (empfohlen)
Wenn du ein Permissions‑Plugin nutzt (z. B. LuckPerms):
1. Erstelle eine Gruppe `rpg-admin`.
2. Vergib folgende Permissions:
   - `rpg.admin.*` (volle Adminrechte)
   - oder gezielt:
     - `rpg.admin` (Admin-Menü)
     - `rpg.editor` (Editor‑Werkzeuge)
     - `rpg.debug` (Debug‑Overlay)
     - `rpg.mod` (Moderationstools)
3. Füge den Spieler der Gruppe hinzu.

> **Hinweis:** Ohne Permissions‑Plugin kannst du einzelne Spieler nur über `op` voll berechtigen.

---

## 2) Spieler‑Handbuch (In‑Game)

### 2.1 Hauptmenü öffnen
- **Befehl:** `/rpg`
- Öffnet das zentrale RPG‑Menü mit Charakter, Skills, Quests, Fraktionen.

### 2.2 Charakter & Progression
- **Level & XP** steigen durch:
  - Kämpfe (Mobs)
  - Crafting
  - Blöcke abbauen
- Attribute wirken automatisch auf:
  - Leben, Angriffsschaden, Angriffsgeschwindigkeit, Bewegung

### 2.3 Skills
- **GUI:** `/rpg` → **Skills**
- **Skill lernen:** Klick auf Skill‑Eintrag (benötigt Skillpunkte)
- **Skill nutzen:** `/rpg skill <id>`
  - Beispiele: `dash`, `heal`, `taunt`

### 2.4 Quests
- **GUI:** `/rpg` → **Quests**
- **Quest annehmen:** Klick im Quest‑Menü
- **Quest abbrechen:** `/rpg quest abandon <id>`
- **Quest abschließen (manuell prüfen):** `/rpg quest complete <id>`

### 2.5 Klasse wählen
- **Liste:** `/rpg class list`
- **Wählen:** `/rpg class choose <id>`

### 2.6 Respec (Skill‑Reset)
- **Befehl:** `/rpg respec`
- Setzt Skills/Attribute zurück und gibt Skillpunkte neu aus.

### 2.7 Party
- **Party erstellen:** `/party create`
- **Einladen:** `/party invite <Spieler>`
- **Beitreten:** `/party join <Leader>`
- **Verlassen:** `/party leave`

---

## 3) Admin‑Handbuch (In‑Game)

### 3.1 Admin‑Menü
- **Befehl:** `/rpgadmin`
- Öffnet das Admin‑Dashboard (Zonen, NPCs, Quests, Loot, Skills, Debug).

### 3.2 Editor‑Wand (Regionen setzen)
- **Befehl:** `/rpgadmin wand`
- **Links‑Klick Block:** Pos1 setzen
- **Rechts‑Klick Block:** Pos2 setzen

### 3.3 Zonen (Regionen)
- **Zone erstellen:**
  ```
  /rpgadmin zone create <id>
  ```
  > Nutzt Pos1/Pos2 der Editor‑Wand.
- **Level‑Range setzen:**
  ```
  /rpgadmin zone setlevel <id> <min> <max>
  ```
- **Modifier setzen (Slow/Damage):**
  ```
  /rpgadmin zone setmod <id> <slow> <damage>
  ```

### 3.4 NPCs
- **NPC erstellen:**
  ```
  /rpgadmin npc create <id> <role>
  ```
  **Rollen:** `QUESTGIVER`, `VENDOR`, `TRAINER`, `TELEPORTER`, `BANKER`, `FACTION_AGENT`
- **Dialog setzen:**
  ```
  /rpgadmin npc dialog <id>
  ```
  Danach die Dialogzeile in den Chat schreiben.

### 3.5 Quests
- **Quest erstellen:**
  ```
  /rpgadmin quest create <id> <name>
  ```
- **Quest‑Step hinzufügen:**
  ```
  /rpgadmin quest addstep <id> <type> <target> <amount>
  ```
  **Typen:** `KILL`, `COLLECT`, `TALK`, `EXPLORE`, `CRAFT`, `USE_ITEM`, `DEFEND`, `ESCORT`

### 3.6 Loot‑Tabellen
- **Loot‑Table erstellen:**
  ```
  /rpgadmin loot create <id> <appliesTo>
  ```
  `appliesTo` = z. B. `ZOMBIE`, `SKELETON`
- **Eintrag hinzufügen:**
  ```
  /rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>
  ```
  **Rarity:** `COMMON`, `UNCOMMON`, `RARE`, `EPIC`, `LEGENDARY`

### 3.7 Debug‑Overlay
- **GUI:** `/rpgadmin` → Debug
- Zeigt Zone & Quest‑Status in der Actionbar.

---

## 4) Tipps für Live‑Betrieb

- **Audit‑Log:** Änderungen werden in `plugins/RPGPlugin/audit.log` protokolliert.
- **Daten‑Files:**
  - `players.yml`, `quests.yml`, `zones.yml`, `npcs.yml`, `loot.yml`, `skills.yml`, `classes.yml`, `factions.yml`
- **Empfehlung:** Änderungen in‑game vornehmen, dann die YAML‑Dateien in Git versionieren.

---

## 5) Quick‑Start (Minimal)
1. Admin erstellen (`op` oder Permissions).
2. `/rpgadmin wand` → Pos1/Pos2 setzen.
3. `/rpgadmin zone create startzone`
4. `/rpgadmin npc create guide QUESTGIVER`
5. `/rpgadmin quest create starter "Erste Schritte"`
6. `/rpgadmin quest addstep starter KILL ZOMBIE 3`
7. Spieler nutzt `/rpg` → Quests → annehmen.

Fertig! 🎉
