# RPG YAML Dashboard (Streamlit) – Dokumentation

Diese Datei beschreibt **genau**, was `rpg_yaml_dashboard.py` macht, **welche Dateien/Ordner** es erwartet und **wo** du das Script ablegen musst, damit Autodetektion + Icons + Edit/Save zuverlässig funktionieren.

---

## 1. Zweck: Was macht das Programm?

`rpg_yaml_dashboard.py` ist ein **lokales Streamlit-Dashboard** für dein MineLauncherRPG-Projekt. Es dient als **Admin-/Dev-Tool**, um die RPG-YAMLs komfortabel zu:

- **anzeigen** (Mobs, Skills, Quests, NPCs, LootTables, Enchantments, Spawners)
- **filtern** (z. B. nach Mob-Type, Boss, LootTable, Suche, Min-XP, etc.)
- **querreferenzieren** (z. B. „Welche Spawner nutzen diesen Mob?“)
- **mit Icons visualisieren** (Item-Icons aus dem Minecraft Client-JAR)
- **Datensätze editieren** (YAML pro Datensatz im Editor)
- **speichern mit Backup + Atomic Write**
- **live neu laden** (Cache clear / rerun)

Zusätzlich zeigt es im Mob-Detailbereich ein **Mob-Portrait**:
- bevorzugt ein **spezifisches Spawn-Egg** (wenn im Client-JAR vorhanden)
- ansonsten eine **Entity-Textur** (Face-Crop oben links 16×16 → 64×64)
- ansonsten ein **generisches Spawn-Egg**

---

## 2. Welche Dateien liest das Dashboard?

Das Dashboard erwartet im Plugin-Ordner diese YAML-Dateien:

- `mobs.yml`
- `skills.yml`
- `quests.yml`
- `npcs.yml`
- `loot.yml`
- `enchantments.yml`
- `spawners.yml`

**Wichtig:** Jede dieser Dateien muss im Root ein YAML-Mapping sein:

```yaml
some_id:
  key: value
another_id:
  ...
````

Also: **id → object**. Keine Listen als Root.

---

## 3. Woher kommen die Icons?

Icons werden direkt aus dem **Minecraft-Client-JAR** gelesen (keine API, kein Internet).

Erwartetes Layout (CmlLib / dein Launcher-Setup):

```
<project_root>/
  client_files/
    versions/
      1.20.4/
        1.20.4.jar
```

Das Script sucht standardmäßig hier:

* `client_root = <project_root>/client_files`
* `client_version = <launcher-config.json> (game.clientVersion)` oder fallback `1.20.4`
* JAR: `client_files/versions/<version>/<version>.jar`

Wenn das bei dir anders ist, kannst du im Sidebar-Feld **Client-Ordner** und **Client-Version** anpassen.

---

## 4. Woher kommen die Plugin-YAMLs?

Das Script versucht den Plugin-Ordner zu finden über:

* `project_root` (Ordner, der **launcher-config.json** enthält)
* daraus wird automatisch abgeleitet:

  * `installRoot` (Default `servers`)
  * `server.name` (Default `paper-server`)

Dann ist der Standard-Serverpfad:

```
<project_root>/<installRoot>/<server.name>/
```

und der Standard-Plugin-Pfad:

```
<server_root>/plugins/MineLauncherRPG/
```

Im Sidebar kannst du das jederzeit überschreiben:

* `Server-Root`
* `Plugin-Root`

---

## 5. Wo muss das Python-Script liegen?

### Variante A (empfohlen): Script ins Projekt-Root

Lege `rpg_yaml_dashboard.py` **direkt** in den Ordner, in dem auch deine `launcher-config.json` liegt.

Beispiel:

```
MineLauncher/
  launcher-config.json
  rpg_yaml_dashboard.py
  client_files/
  servers/
    paper-server/
      plugins/
        MineLauncherRPG/
          mobs.yml
          skills.yml
          ...
```

**Warum ist das die beste Variante?**

* `project_root` ist beim Start automatisch korrekt (Current Working Directory).
* Client-JAR und Serverpfade können automatisch gefunden werden.
* Du musst im Sidebar selten manuell Pfade anpassen.

---

### Variante B: Script irgendwo anders (geht auch)

Du kannst `rpg_yaml_dashboard.py` auch z. B. in einen Tools-Ordner legen, **aber dann** musst du im Dashboard im Sidebar **Projekt-Root** korrekt setzen (Pfad zu `launcher-config.json`).

Beispiel:

```
Tools/
  rpg_yaml_dashboard.py

MineLauncher/
  launcher-config.json
  client_files/
  servers/
```

Dann im Dashboard:

* Projekt-Root: `...\MineLauncher`

---

## 6. Installation & Start

### Installation (einmalig)

Einzeiler:

```powershell
py -m pip install streamlit pyyaml pandas pillow
```

### Start

Wenn du im Ordner bist, in dem das Script liegt:

```powershell
py -m streamlit run .\rpg_yaml_dashboard.py
```

---

## 7. Bedienung im Dashboard

### Sidebar: Pfade

1. **Projekt-Root**: Muss auf den Ordner zeigen, der `launcher-config.json` enthält.
2. **Client-Version**: Minecraft-Version der Client-JARs (z. B. `1.20.4`).
3. **Client-Ordner**: Root von `client_files` (CmlLib Layout).
4. **Server-Root**: Root deines Paper-Servers.
5. **Plugin-Root**: `.../plugins/MineLauncherRPG`

### Cache leeren / neu laden

* Button: **Cache leeren / neu laden**
* Vorteil: Wenn du YAMLs extern änderst, bekommst du ohne Restart wieder den aktuellen Stand.

---

## 8. Editieren & Speichern: Wie funktioniert das?

In jedem Tab wird nach dem Detail-View ein Editor angezeigt:

* **„<Typ> bearbeiten (YAML)”**
* du editierst **nur** den Datensatz (ohne Root-Key)

Beim Speichern passiert:

1. YAML wird geparst (`yaml.safe_load`)
2. Validierung: Datensatz muss ein **Mapping/dict** sein
3. Der Datensatz wird im Root-Mapping ersetzt
4. Es wird ein Backup erzeugt:

   * `mobs.yml.bak` (oder jeweilige Datei)
5. Atomic Write:

   * zuerst `<file>.tmp`
   * dann Replace auf die echte YAML

Danach:

* Cache wird gelöscht
* Seite wird per `st.rerun()` neu geladen

### Wichtiger Hinweis zu YAML-Format

PyYAML kann:

* Kommentare entfernen
* Formatierung verändern
* ggf. Reihenfolge einzelner Keys verändern

Wenn du 1:1 Format & Kommentare brauchst, ist `ruamel.yaml` die richtige Erweiterung.

---

## 9. Icon-Check: Trefferquote

Es gibt einen Expander **„Icon-Check (Trefferquote)”**.

Das Script sammelt Materialien aus:

* Mob-Ausrüstung (`mainHand`, `helmet`, etc.)
* Loot-Einträgen (`entries[].material`)

Dann wird gemessen:

* wie viele dieser Materialien im Client-JAR als PNG gefunden werden.

Damit erkennst du sofort:

* ob Materialnamen nicht zur Version passen
* ob du falsche Version/JAR erwischt hast
* ob ein Mapping fehlt

---

## 10. Typische Fehler & Lösungen

### „Kein Client-JAR gefunden“

Ursachen:

* falscher `client_root`
* falsche `client_version`
* JAR liegt nicht im erwarteten CmlLib Layout

Lösung:

* Sidebar `Client-Ordner` und `Client-Version` korrekt setzen.
* Prüfen, ob `<version>.jar` wirklich existiert.

---

### Icons fehlen (— statt Icon)

Ursachen:

* Materialname passt nicht zur Minecraft-Version
* Icon liegt als Block statt Item oder umgekehrt
* Modded/Custom Items haben keine Vanilla-Texture

Lösung:

* Icon-Check ansehen
* Materialnamen in YAML prüfen
* ggf. später `special`-Mapping ergänzen (im Script vorgesehen)

---

### Speichern verändert YAML stark

Ursache:

* PyYAML dump ist nicht formatstabil

Lösung:

* auf `ruamel.yaml` umstellen (optional, später möglich)

---

## 11. Sicherheits- und Praxis-Hinweise (Best Practice)

* Nutze das Tool primär lokal in der Entwicklung.
* Git ist dein Freund: Commit vor großen Änderungen.
* Backups (`.bak`) bleiben erhalten – du kannst jederzeit zurück.

---

## 12. Kurz-Fazit

`rpg_yaml_dashboard.py` ist ein **Admin-UI für deine RPG-Konfiguration**, das:

* alles aus YAMLs lädt,
* Icons aus dem Client-JAR zieht,
* querverlinkt (Spawner ↔ Mobs, NPC ↔ Quests),
* pro Datensatz editieren und sicher speichern kann.

Wenn du es ins **Projekt-Root (neben launcher-config.json)** legst, funktioniert Autodetektion am zuverlässigsten.


