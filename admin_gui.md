# Admin GUI Anleitung

Diese Anleitung beschreibt **alle Admin-GUI-Funktionen** im RPG-Plugin und erklärt Schritt für Schritt, was ein Admin anklicken und eingeben muss.

## Voraussetzungen

- Du benötigst die Berechtigung `rpg.editor`.
- Verwende die **Editor-Wand** (Stick), um Zonen-Grenzen zu setzen:
  - Linksklick auf einen Block = **Pos1**
  - Rechtsklick auf einen Block = **Pos2**
- Die Admin-GUI öffnest du im Spiel über **/rpgadmin** (oder den Server-Shortcut, der das Admin-Menü öffnet).

> **Tipp:** In allen Editoren gilt:
> - **Linksklick** auf einen Eintrag = **Bearbeiten**
> - **Rechtsklick** auf einen Eintrag = **Löschen**
> - **Grüner Block (Slot 53)** = **Erstellen**

---

## 1) Admin-Menü öffnen

1. Gib **/rpgadmin** ein.
2. Es öffnet sich das **RPG Admin** Menü mit folgenden Buttons:
   - **Zonen-Editor** (Kompass)
   - **NPC-Editor** (Villager Spawn Egg)
   - **Quest-Editor** (Buch)
   - **Loot-Tabellen** (Truhe)
   - **Skills & Klassen** (Blaze Powder)
   - **Debug Overlay** (Redstone)
   - **Bau-Manager** (Bricks)
   - **Permissions** (Name Tag)

---

## 2) Zonen-Editor

### Öffnen
- Im Admin-Menü auf **Zonen-Editor** klicken.

### Zone erstellen
1. Stelle mit der **Editor-Wand** Pos1 und Pos2 ein.
2. Im Zonen-Editor auf **Grüner Block** klicken.
3. Eingabeformat:
   ```
   <id>
   ```
   Beispiel:
   ```
   forest_zone
   ```

### Zone bearbeiten
1. **Linksklick** auf eine Zone.
2. Eingabeformat:
   ```
   <aktion> <parameter>
   ```
3. Unterstützte Aktionen:
   - **Name ändern**
     ```
     name <neuerName>
     ```
   - **Level-Bereich**
     ```
     level <min> <max>
     ```
   - **Modifier** (Langsamkeit/Damage)
     ```
     mod <slow> <damage>
     ```
     Beispiel:
     ```
     mod 0.8 1.2
     ```
   - **Bounds neu setzen** (mit Wand)
     ```
     bounds
     ```
     -> Pos1/Pos2 muss vorher gesetzt sein.
   - **Welt ändern**
     ```
     world <weltname>
     ```

### Zone löschen
- **Rechtsklick** auf eine Zone.

---

## 3) NPC-Editor

### Öffnen
- Im Admin-Menü auf **NPC-Editor** klicken.

### NPC erstellen
1. Im NPC-Editor auf **Grüner Block** klicken.
2. Eingabeformat:
   ```
   <id> <role> [shopId]
   ```
3. Beispiel:
   ```
   smith VENDOR blacksmith_shop
   ```
4. Der NPC wird **sofort an deiner aktuellen Position gespawnt** (ein Villager erscheint direkt vor Ort).

### NPC-Shop Vorlagen nutzen
Im NPC-Editor findest du fertige **Shop-Vorlagen** in der unteren Reihe. So nutzt du sie:

1. **Klicke** auf eine Vorlage (z. B. **Waffenhändler**, **Rüstungshändler**, **Gegenstandshändler**, **Rohstoffhändler**, **Questgiver**, **Shop (shops.yml)**).
2. Danach wirst du nach einer Eingabe gefragt:
   - **Für Waffen-/Rüstungs-/Gegenstand-/Rohstoffhändler**:
     ```
     <id>
     ```
     Beispiel:
     ```
     waffi
     ```
   - **Für Shop (shops.yml)**:
     ```
     <id> <shopId>
     ```
     Beispiel:
     ```
     vendor1 blacksmith_shop
     ```
3. Der NPC wird **sofort an deiner aktuellen Position gespawnt**.

**Hinweis zu den Vorlagen:**
- **Waffenhändler** verkauft/kauft alle Waffen aus dem Spiel.
- **Rüstungshändler** verkauft/kauft alle Rüstungen aus dem Spiel.
- **Gegenstandshändler** verkauft/kauft diverse Items & Verbrauchsgüter.
- **Rohstoffhändler** verkauft/kauft Ressourcen & Erze.
- Alle Händler können **zusätzlich RPG-Items** (Item-Generator) ankaufen/verkaufen, wenn sie vom Material her passen.

### NPC bearbeiten
1. **Linksklick** auf einen NPC.
2. Eingabeformat:
   ```
   <aktion> <parameter>
   ```
3. Unterstützte Aktionen:
   - **Name ändern**
     ```
     name <neuerName>
     ```
   - **Rolle ändern**
     ```
     role <rolle>
     ```
   - **Dialog setzen**
     ```
     dialog <text>
     ```
   - **Quest verlinken**
     ```
     quest <questId|none>
     ```
   - **Shop verlinken**
     ```
     shop <shopId|none>
     ```
   - **Fraktion setzen**
     ```
     faction <factionId|none>
     ```
   - **Ranganforderung**
     ```
     rank <rankId|none>
     ```
   - **Position ändern (Teleport)**
     ```
     move
     ```

### NPC löschen
- **Rechtsklick** auf einen NPC.

---

## 4) Quest-Editor

### Öffnen
- Im Admin-Menü auf **Quest-Editor** klicken.

### Quest erstellen
1. Im Quest-Editor auf **Grüner Block** klicken.
2. Eingabeformat:
   ```
   <id> <name>
   ```
3. Beispiel:
   ```
   wolf_hunt Wolfsplage
   ```

### Quest bearbeiten
1. **Linksklick** auf eine Quest.
2. Eingabeformat:
   ```
   <aktion> <parameter>
   ```
3. Unterstützte Aktionen:
   - **Name ändern**
     ```
     name <neuerName>
     ```
   - **Beschreibung setzen**
     ```
     desc <text>
     ```
   - **Mindestlevel setzen**
     ```
     minlevel <level>
     ```
   - **Wiederholbar**
     ```
     repeatable <true|false>
     ```
   - **Event-Requirement**
     ```
     event <eventId|none>
     ```
   - **Quest-Step hinzufügen**
     ```
     addstep <type> <target> <amount>
     ```

### Quest löschen
- **Rechtsklick** auf eine Quest.

---

## 5) Loot-Tabellen

### Öffnen
- Im Admin-Menü auf **Loot-Tabellen** klicken.

### Loot-Tabelle erstellen
1. Im Loot-Editor auf **Grüner Block** klicken.
2. Eingabeformat:
   ```
   <id> <appliesTo>
   ```
3. Beispiel:
   ```
   forest_loot ZOMBIE
   ```

### Loot-Tabelle bearbeiten
1. **Linksklick** auf eine Tabelle.
2. Eingabeformat:
   ```
   <aktion> <parameter>
   ```
3. Unterstützte Aktionen:
   - **Ziel ändern**
     ```
     applies <target>
     ```
   - **Eintrag hinzufügen**
     ```
     addentry <material> <chance> <min> <max> <rarity>
     ```
     Beispiel:
     ```
     addentry IRON_NUGGET 0.5 1 3 COMMON
     ```
   - **Alle Einträge löschen**
     ```
     clear
     ```

### Loot-Tabelle löschen
- **Rechtsklick** auf eine Tabelle.

---

## 6) Skills & Klassen

### Öffnen
- Im Admin-Menü auf **Skills & Klassen** klicken.

---

### 6.1 Skill-Admin

#### Skill erstellen
1. Im Skill-Editor auf **Grüner Block** klicken.
2. Eingabeformat:
   ```
   <id>
   ```

#### Skill bearbeiten
1. **Linksklick** auf einen Skill.
2. Eingabeformat:
   ```
   <aktion> <parameter>
   ```
3. Unterstützte Aktionen:
   - **Name ändern**
     ```
     name <neuerName>
     ```
   - **Cooldown setzen**
     ```
     cooldown <sekunden>
     ```
   - **Mana setzen**
     ```
     mana <wert>
     ```
   - **Kategorie setzen**
     ```
     category <kategorie>
     ```
   - **Typ setzen**
     ```
     type <typ>
     ```
   - **Voraussetzung**
     ```
     requires <skillId|none>
     ```
   - **Effekt hinzufügen**
     ```
     addeffect <effectType> <param:value>...
     ```
     Beispiel:
     ```
     addeffect DAMAGE amount:6 radius:4
     ```
   - **Effekte leeren**
     ```
     cleareffects
     ```

#### Skill löschen
- **Rechtsklick** auf einen Skill.

---

### 6.2 Klassen-Admin

#### Klassen-Admin öffnen
- Im Skill-Editor auf **"Klassen verwalten"** klicken.

#### Klasse erstellen
1. Im Klassen-Editor auf **Grüner Block** klicken.
2. Eingabeformat:
   ```
   <id> <name>
   ```

#### Klasse bearbeiten
1. **Linksklick** auf eine Klasse.
2. Eingabeformat:
   ```
   <aktion> <parameter>
   ```
3. Unterstützte Aktionen:
   - **Name ändern**
     ```
     name <neuerName>
     ```
   - **Start-Skill hinzufügen**
     ```
     addskill <skillId>
     ```
   - **Start-Skill entfernen**
     ```
     removeskill <skillId>
     ```

#### Klasse löschen
- **Rechtsklick** auf eine Klasse.

---

## 7) Debug Overlay

- Im Admin-Menü auf **Debug Overlay** klicken.
- Es erscheint eine Nachricht:
  - **Debug aktiviert** oder **Debug deaktiviert**.

---

## 8) Bau-Manager

- Im Admin-Menü auf **Bau-Manager** klicken.
- Öffnet die **Gebäude-Kategorien**.
- Von dort kannst du:
  - eine Kategorie wählen
  - oder **Einzel-Schema** nutzen

---

## 9) Permissions

- Im Admin-Menü auf **Permissions** klicken.
- Öffnet das Berechtigungs-Menü:
  - **Rollen verwalten**
  - **Spieler-Rollen**
  - **Audit Log**
