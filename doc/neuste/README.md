# ⚔️ MineLauncher & RPG System

Dieses Projekt ist eine Komplettlösung für einen **Custom Minecraft RPG Server**. Es besteht aus drei Hauptkomponenten:

1.  **C_launcher:** Ein C#-basiertes Tool zum Verwalten von Servern und Starten des Minecraft-Clients (inkl. Offline-Support).
2.  **RPGPlugin:** Ein massives PaperMC-Plugin (1.20.4), das MMORPG-Mechaniken (Skills, Dungeons, DB, Gilden) hinzufügt.
3.  **WorldCreatorPlugin:** Ein Hilfs-Plugin zur Generierung spezieller Welten (Void, Skyblock, etc.).

---

## 📋 Systemvoraussetzungen

*   **Betriebssystem:** Windows (für den Launcher), Linux/Windows (für den Server).
*   **Java:** JDK 17 oder höher (für Minecraft 1.20.4).
*   **runtime:** .NET 8 SDK (für den Launcher).
*   **Datenbank:** PostgreSQL (Zwingend für das RPG-Plugin!).

---

## 🚀 Teil 1: Der Launcher (`C_launcher`)

Der Launcher verwaltet die Installation des Servers und ermöglicht das Spielen ohne offiziellen Minecraft-Launcher.

### 1. Installation & Start
Navigiere in den Ordner `MineLauncher/`.

```bash
# Starten im Entwicklungsmodus
dotnet run --project C_launcher.csproj

# Oder nach dem Build die .exe ausführen
```

### 2. Funktionen
*   **Server Download:** Lädt automatisch Paper, Vanilla oder Bukkit herunter.
*   **Client Start:** Lädt Minecraft 1.20.4 (oder andere Versionen) herunter und startet sie.
*   **Offline Mode:** Ermöglicht das Spielen mit beliebigem Spielernamen (`Play`-Button).
*   **Config-Editor:** Integrierter JSON-Editor für schnelle Anpassungen.

### 3. Konfiguration (`launcher-config.json`)
Diese Datei wird beim ersten Start erstellt. Wichtige Einstellungen:

```json
{
  "installRoot": "servers",
  "server": {
    "name": "rpg-server",
    "type": "paper",
    "version": "1.20.4",
    "onlineMode": false,  // Auf false setzen für Offline-Launcher-Support!
    "acceptEula": true
  },
  "game": {
    "clientVersion": "1.20.4",
    "offlineMode": true,
    "offlineUsername": "AdminPlayer"
  }
}
```

---

## 🛠️ Teil 2: Das RPG-Plugin Setup

Das Herzstück des Gameplays. Es verwandelt Minecraft in ein RPG.

### 1. Kompilieren (Build)
Erstelle die `.jar` Dateien aus dem Quellcode.

```bash
# Im Hauptverzeichnis
cd plugins/RPGPlugin
mvn clean package
# Die Datei liegt nun in /target/rpg-plugin-1.0.0.jar
```

Kopiere diese Datei in den `plugins/` Ordner deines Servers (der vom Launcher erstellt wurde, z.B. `servers/rpg-server/plugins/`).

### 2. Datenbank-Verbindung (WICHTIG!)
Das Plugin **startet nicht** ohne PostgreSQL-Datenbank.
1.  Installiere PostgreSQL.
2.  Erstelle eine Datenbank (z.B. `rpg_db`).
3.  Starte den Server einmal, damit der Ordner `plugins/RPGPlugin/` erstellt wird.
4.  Bearbeite `plugins/RPGPlugin/config.yml`:

```yaml
database:
  host: localhost
  port: 5432
  name: rpg_db
  user: dein_db_user
  password: dein_db_passwort
  poolSize: 10
```

### 3. Gameplay-Features
*   **Klassen & Skills:** Definierbare Klassen (Krieger, Magier) mit aktiven Skills und Skilltrees.
*   **Custom Mobs:** Mobs mit Behavior-Trees (KI), Custom Skills und Loot-Tables.
*   **Dungeons:** Instanzierte Dungeons mittels Wave Function Collapse (WFC) Generierung.
*   **Wirtschaft:** Globales Auktionshaus, NPC-Shops, sicherer Spieler-Handel.
*   **Social:** Gilden (mit Bank & Quests), Party-System (XP-Teilen).
*   **Welt:** Regionen (Zonen) mit Level-Anforderungen und automatischen Spawners.

---

## 🎮 Admin Guide: Von Null auf Spielbar

Befolge diese Schritte, um die Welt mit Leben zu füllen.

### Schritt 1: Klassen erstellen
Bearbeite `plugins/RPGPlugin/classes.yml` und `skills.yml`.
*   Definiere Skills wie `fireball` oder `smash`.
*   Weise diese Skills Klassen wie `Mage` oder `Warrior` zu.
*   Reload: `/reload`.

### Schritt 2: Die Welt gestalten
Nutze den In-Game Editor.
1.  **Zone erstellen:**
    *   `/rpgadmin wand` (Hole den Zauberstab).
    *   Markiere einen Bereich (Links/Rechtsklick).
    *   `/rpgadmin zone create wald`.
    *   `/rpgadmin zone setlevel wald 1 10`.
2.  **Spawner setzen:**
    *   `/rpgadmin spawner create wald_spawner wald`.
    *   `/rpgadmin spawner addmob wald_spawner forest_zombie 1.0`.

### Schritt 3: NPCs & Quests
1.  **Quest erstellen:**
    *   `/rpgadmin quest create starter "Die erste Jagd"`.
    *   `/rpgadmin quest addstep starter KILL ZOMBIE 5`.
2.  **NPC aufstellen:**
    *   `/rpgadmin npc create wache QUESTGIVER`.
    *   `/rpgadmin npc linkquest wache starter`.
    *   `/rpgadmin npc dialog wache` -> "Hilf uns gegen die Zombies!".

### Schritt 4: Dungeons testen
1.  Erstelle eine Party: `/party create`.
2.  Generiere einen Dungeon: `/dungeon generate wfc`.
3.  Die Party wird in eine generierte Welt teleportiert. Nach 15 Minuten oder Abschluss wird die Welt gelöscht.

---

## ⌨️ Befehlsreferenz

### Spieler
| Befehl | Funktion |
| :--- | :--- |
| `/rpg` | Öffnet das Charakter-Menü. |
| `/rpg skilltree` | Öffnet den Skillbaum zum Lernen. |
| `/rpg bind <1-9> <skill>` | Legt einen Skill auf die Hotbar (Rechtsklick zum Nutzen). |
| `/party ...` | Party-Verwaltung (`invite`, `join`, `leave`). |
| `/guild ...` | Gilden-Verwaltung (`create`, `bank`, `quest`). |
| `/auction ...` | Auktionshaus (`buy`, `sell`). |
| `/trade ...` | Sicherer Handel mit Spielern. |

### Admin (`rpg.admin`)
| Befehl | Funktion |
| :--- | :--- |
| `/rpgadmin wand` | Editor-Tool für Zonen. |
| `/rpgadmin zone` | Zonen erstellen/bearbeiten. |
| `/rpgadmin npc` | NPCs spawnen und verlinken. |
| `/rpgadmin mob` | Mobs manuell spawnen. |
| `/rpgadmin skill` | Skills in-game bearbeiten. |
| `/rpgadmin loot` | Loot-Tables bearbeiten. |
| `/dungeon generate` | Erzwingt Dungeon-Generierung. |

---

## 🌍 World Creator Plugin (Bonus)

Ein kleines Zusatz-Plugin für spezielle Welten.

*   **Befehl:** `/worlds` öffnet ein GUI.
*   **Optionen:** Leere Welt (Void), Wasserwelt, Sky Islands, Dschungel, Wüste.
*   **Funktion:** Erstellt sofort eine neue Bukkit-Welt und teleportiert dich dort hin.

---

## ⚠️ Wichtige Hinweise für den Betrieb

1.  **Dungeon Cleanup:** Das Plugin löscht Dungeon-Welten automatisch. Stelle sicher, dass `dungeon.exit` in der Config auf eine sichere Koordinate (Spawn) gesetzt ist.
2.  **Datenbank:** Mache regelmäßige Backups deiner PostgreSQL-Datenbank. Dort liegen alle Spielerdaten (Inventare, Skills, Gilden).
3.  **Configs:** Änderungen an `.yml` Dateien erfordern meist einen `/reload`. Änderungen an der Datenbank-Struktur erfordern einen Neustart.