# Dungeon-Generierung (Jigsaw + WFC) – Von 0 zum fertigen Dungeon

Diese Anleitung beschreibt die vollständige Erstellung und Generierung eines Dungeons im MineLauncher RPG:
1) **Struktur (Makro) mit Jigsaw‑Räumen**  
2) **Details (Mikro) mit WFC‑Füllung**  
3) **Generierung über das /rpgadmin‑Menü**

> Ziel: Große, vordefinierte Räume verbinden (Jigsaw) und deren Innenraum optional per WFC füllen.

---

## 1) Voraussetzungen

### 1.1 Schematic‑Ordner anlegen
Die Jigsaw‑Räume werden als `.schem` Dateien geladen. Lege den Ordner an:

```
plugins/RPGPlugin/dungeon_rooms/<theme>/
```

Beispiel:
```
plugins/RPGPlugin/dungeon_rooms/crypt/
```

Wenn kein Theme‑Ordner vorhanden ist, wird automatisch `plugins/RPGPlugin/dungeon_rooms/default/` verwendet.

### 1.2 Raum‑Schemata erstellen
Erstelle große Räume (Start, Boss, Schatz, Combat) als `.schem`.  
Empfehlung: In den Dateinamen den Raumtyp markieren:
- `start_room.schem`  
- `boss_room.schem`  
- `loot_room.schem`  
- `combat_room_01.schem`

Diese Namen werden automatisch in RoomTypes übersetzt:
`start`, `boss`, `exit`, `loot`, `elite` → entsprechende Typen.

### 1.3 Jigsaw‑Sockets in die Schematic setzen
Platziere **Jigsaw‑Blöcke** an Ein‑/Ausgängen.  
Der Socket‑Name kommt aus dem NBT‑Feld `name` des Jigsaw‑Blocks (z. B. `corridor_ns`, `room_entry`).
Räume verbinden sich bevorzugt über Sockets mit gleichem Namen.

---

## 2) Konfiguration (config.yml)

Diese Optionen steuern die Hybrid‑Generierung:

```yaml
dungeon:
  jigsaw:
    enabled: false        # Macro: Jigsaw‑Räume
    wfcFill: false        # Micro: WFC‑Füllung im Raum
    wfcTheme: ""          # optional: WFC‑Theme (leer = Dungeon‑Theme)
```

> **Tipp:** Diese Werte können direkt im `/rpgadmin`‑Menü umgeschaltet werden.

---

## 3) Dungeon‑Generierung über /rpgadmin

1. Öffne das Admin‑Menü:
   ```
   /rpgadmin
   ```
2. Klicke **„Dungeons“**.
3. Stelle ein:
   - **Jigsaw Modus** → an/aus  
   - **WFC Raum‑Füllung** → an/aus  
   - **Schematic platzieren** → platziert einzelne `.schem` direkt im Spiel (Rechtsklick zum Setzen)
   - **Schematic speichern** → speichert die Wand‑Auswahl direkt als `.schem`
4. Klicke **„Dungeon generieren“** und gib ein Theme ein (z. B. `crypt`).

Der Dungeon wird sofort generiert und du wirst in die Instanz teleportiert.

---

## 4) Beispiel: Von 0 zum fertigen Dungeon

### Schritt 1: Räume bauen und exportieren
Baue im Spiel folgende Räume, markiere sie mit der Wand und speichere sie als `.schem`:
```
start_room.schem
combat_room_01.schem
combat_room_02.schem
loot_room.schem
boss_room.schem
```
**In‑Game Ablauf:**
1. Wand holen: `/rpgadmin wand`
2. Pos1/Pos2 mit der Wand setzen.
3. Optional: **Worldbuilding → Bereich löschen** zum Leeren der markierten Area.
3. `/rpgadmin` → **Dungeons** → **Schematic speichern**  
   Zielpfad eingeben, z. B. `dungeon_rooms/crypt/start_room.schem`.

Lege sie in:
```
plugins/RPGPlugin/dungeon_rooms/crypt/
```

> **Hinweis:** Zum Platzieren einzelner Schemata im Spiel kannst du
> im `/rpgadmin`‑Menü unter **Dungeons** den Punkt **„Schematic platzieren“** nutzen.

### Schritt 2: Jigsaw‑Sockets setzen
In jedem Raum an Türen Jigsaw‑Blöcke setzen:
```
name = corridor_ns
```
Alle Räume verbinden sich so über denselben Socket.

### Schritt 3: WFC‑Patterns vorbereiten
Falls du WFC‑Füllung nutzen willst, stelle sicher:
```
plugins/RPGPlugin/wfc/crypt/...
```
Pattern müssen zu deinem Theme passen.

### Schritt 4: Im Menü aktivieren
```
/rpgadmin
```
**Dungeons → Jigsaw Modus = ON**  
**WFC Raum‑Füllung = ON**

### Schritt 5: Dungeon generieren
Im Menü **„Dungeon generieren“** klicken und Theme `crypt` eingeben.

Fertig: Der Dungeon wird aus großen Räumen gebaut, Korridore verbinden sie,
und jeder Raum wird mit WFC‑Details gefüllt.

---

## 5) Fehlerbehebung

- **Dungeon bleibt leer:** prüfe ob `.schem` im richtigen Theme‑Ordner liegen.  
- **Keine Verbindung zwischen Räumen:** Jigsaw‑Sockets prüfen (`name` muss identisch sein).  
- **WFC füllt nichts:** stelle sicher, dass es Patterns für das Theme gibt.

---

Viel Erfolg beim Bauen deiner Dungeons!
