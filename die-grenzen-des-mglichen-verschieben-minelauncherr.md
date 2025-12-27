# Die Grenzen des Möglichen verschieben: MineLauncherRPG – Ein umfassendes System für Minecraft-MMORPGs

Von: Ralf Krümmel (Der Entwickler)

Tags: Minecraft, RPG, MMORPG, Plugin, PaperMC, Entwicklung, Spielentwicklung, Servermanagement, Modding, Ralf Krümmel, Fantasy

---

Als Ralf Krümmel, "Der Entwickler", ist es meine Leidenschaft, die Grenzen dessen zu erweitern, was in der Welt von Minecraft möglich ist. Mit Stolz präsentiere ich heute einen tiefgehenden Einblick in unser ambitioniertes Projekt: MineLauncherRPG. Es ist mehr als nur ein Plugin; es ist ein komplettes Framework, das einen Standard-PaperMC-Server in ein voll funktionsfähiges Massively Multiplayer Online Role-Playing Game (MMORPG) verwandelt. Von dynamischen Klassensystemen und mächtigen Skills über prozedural generierte Dungeons und eine florierende Spielerwirtschaft bis hin zu tiefgreifenden Gildenmechaniken und einem kompetitiven PvP-System – MineLauncherRPG ist darauf ausgelegt, ein immersives und persistentes Spielerlebnis zu schaffen. Dieses Reportage beleuchtet die Architektur, die Kernfunktionen und die visionären Werkzeuge, die sowohl Spielern als auch Administratoren zur Verfügung stehen, um unvergessliche Abenteuer zu gestalten.

## Die Vision eines MMORPGs in Minecraft – Eine Einführung
MineLauncherRPG ist eine umfassende Lösung, die das klassische Minecraft-Gameplay mit den komplexen Elementen eines MMORPGs anreichert. Die Spieler können sich auf ein System mit vielfältigen Klassen, einem Arsenal an Skills, herausfordernden Dungeons, einem tiefgehenden Gildensystem und einer robusten, persistenten SQL-Datenbank freuen.

Die technischen Grundlagen sind klar definiert, um eine stabile und leistungsfähige Umgebung zu gewährleisten:
*   Ein Server, der auf PaperMC 1.20.4 oder neuer läuft.
*   Eine Java Development Kit (JDK) Version 17+ für die Ausführung.
*   Zwingend eine PostgreSQL-Datenbank für die Speicherung aller Spieler- und Weltdaten.

Die Reise beginnt bereits vor dem Spielstart mit dem "C_launcher" – einem maßgeschneiderten C#-Launcher, der nicht nur die Server-Administration vereinfacht, sondern auch einen direkten Client-Start im Offline-Modus ermöglicht. Ein einfacher Befehl wie `dotnet run --project C_launcher -- play Alex` genügt, um das Abenteuer zu beginnen. Die neuesten Builds für PaperMC können beispielsweise über die API von PaperMC bezogen werden: [https://api.papermc.io/v2/projects/paper/versions/{version}](https://api.papermc.io/v2/projects/paper/versions/{version}).

## Das Fundament legen: Installation und Infrastruktur
Bevor die ersten Schritte in der neu geschaffenen RPG-Welt unternommen werden können, muss das technische Fundament gelegt werden. Der Prozess ist strukturiert und gewährleistet eine reibungslose Integration:

1.  **Plugin-Installation**: Die `rpg-plugin-1.0.0.jar` wird im `plugins/` Ordner des PaperMC-Servers platziert.
2.  **Erste Initialisierung**: Ein einmaliger Serverstart generiert alle notwendigen Konfigurationsdateien. Anschließend wird der Server gestoppt.
3.  **Datenbank-Verbindung**: Die Datei `plugins/RPGPlugin/config.yml` ist das Herzstück der Datenintegration. Hier werden die Zugangsdaten zur PostgreSQL-Datenbank hinterlegt:
    ```yaml
    database:
      host: localhost
      port: 5432
      name: rpg_db
      user: dein_user
      password: dein_passwort
      poolSize: 10
    ```
    Diese Konfiguration stellt sicher, dass alle Spielerdaten, Fortschritte und Weltinformationen persistent und sicher gespeichert werden. Auch die Mojang-Versionsmanifeste sind für die Client-Downloads essenziell: [https://piston-meta.mojang.com/mc/game/version_manifest.json](https://piston-meta.mojang.com/mc/game/version_manifest.json).
4.  **Abschließender Serverstart**: Nach der Datenbankkonfiguration erstellt das Plugin beim erneuten Start automatisch alle erforderlichen Tabellen wie `rpg_players` und `rpg_skills`.

## Spieler im Zentrum: Charakterentwicklung und Interaktion

### Klassen und Skills: Der Weg des Helden
Die Spieler tauchen in eine Welt ein, in der sie ihren eigenen Weg schmieden können, beginnend mit der Wahl einer Klasse, die in `classes.yml` definiert ist. Ob als tapferer Krieger, geschickter Ranger oder weiser Magier – jede Klasse bietet einzigartige Startfähigkeiten und Boni. Die **Skill-Liste** (`skills.yml`) ist das Herzstück der Charakterentwicklung. Sie umfasst:

*   **Heiler (Healing)**: `Heilpuls` (`heal_pulse`), `Große Heilung` (`greater_heal`), `Segen` (`divine_blessing`).
*   **Magie (Magic)**: `Flammenstoß` (`ember_shot`), `Frostbolzen` (`frost_bolt`), `Arkane Explosion` (`arcane_burst`).
*   **Angriff (Attack)**: `Machtstoß` (`power_strike`), `Wirbelwind` (`whirlwind`), `Hinrichtung` (`execute`).
*   **Verteidigung (Defense)**: `Schildwall` (`shield_wall`), `Bollwerk` (`fortify`), `Abwehrhaltung` (`deflect`).
*   **Berufe (Profession)**: `Bergbau-Fokus` (`mining_focus`), `Handwerkskunst` (`crafting_insight`), `Alchemie-Meister` (`alchemy_mastery`).

Spieler erlernen und binden diese Skills über einfache Befehle wie `/rpg bind <slot 1-9> <skillId>`, die ihre Hotbar in ein mächtiges Arsenal verwandeln.

### Der Skillbaum: Visuelle Progression
Ein intuitiver Skillbaum, zugänglich über `/rpg skilltree`, visualisiert die Lernpfade und Abhängigkeiten der Fähigkeiten. Er ermöglicht eine strategische Planung der Charakterentwicklung und belohnt Spieler für ihre Investition in spezifische Skill-Linien.

### Wirtschaft und Handel: Das Gold der Abenteurer
Die Wirtschaft bildet das Rückgrat jeder lebendigen MMORPG-Welt. MineLauncherRPG bietet hierfür eine robuste Infrastruktur:

*   **Loot-Tabellen (`loot.yml`)**: Monster droppen Gold und seltene Items, deren Stats automatisch generiert werden.
*   **Shops (`shops.yml`)**: Administratoren können individuelle Händler definieren, die An- und Verkauf von Waren ermöglichen. Über das Admin-GUI können auch dynamische RPG-Item-Händler (Waffen, Rüstungen, Items, Rohstoffe) erstellt werden, die ihr Angebot an das Spielerlevel anpassen.
*   **Auktionshaus**: Spieler können Items über `/auction sell <Preis>` anbieten und über `/auction buy <id>` erwerben.
*   **Spielerhandel**: Direkter Handel zwischen Spielern ist über `/trade request <player>` möglich, inklusive Goldangeboten und -anforderungen.
*   **Crafting-Aufträge**: Einzigartig ist das System der Crafting-Aufträge, bei dem Spieler über `/rpg order create <material> <amount> <reward>` Aufträge für andere erstellen und erfüllen können, was eine dynamische Spielerwirtschaft fördert.

### Fraktionen und Ruf: Loyalität zahlt sich aus
Ein tiefgreifendes Fraktionssystem, das in `factions.yml` definiert wird, ermöglicht es, NPCs an bestimmte Fraktionen zu binden und Interaktionen basierend auf dem Spielerruf zu steuern. Ein hoher Ruf bei einer Fraktion kann Shop-Rabatte oder sogar den Zugang zu Dungeons freischalten. NPCs können über `/rpgadmin npc faction <npcId> <factionId>` und `/rpgadmin npc rank <npcId> <rankId>` entsprechend konfiguriert werden.

### Housing & Gildenhallen: Ein Zuhause in der Welt
Spieler können ihre eigenen Heime über `/rpg home set` definieren und zu ihnen zurückkehren. Gilden haben die Möglichkeit, Gildenhallen zu errichten und diese mit Upgrades wie Crafting-Stationen, Teleportpunkten oder Buff-Generatoren zu verbessern, was die Gemeinschaft stärkt und strategische Vorteile bietet.

## Die Welt gestalten: Zonen, Mobs und Quests

### Zonen und Spawner: Die Dynamik der Wildnis
Die Welt von MineLauncherRPG ist in Zonen unterteilt, die über die `Editor-Wand` (`/rpgadmin wand`) definiert werden. Für jede Zone können Levelbereiche (`/rpgadmin zone setlevel <id> <min> <max>`) und Modifikatoren für Langsamkeit oder Schaden festgelegt werden. Spawner (`spawners.yml`) beleben diese Zonen, indem sie automatisch Custom Mobs innerhalb definierter Grenzen und Intervalle erscheinen lassen.

### Die Chroniken der Kreaturen: Die Seele der Bedrohung
Das Herzstück jeder RPG-Erfahrung sind die Gegner. MineLauncherRPG bietet eine detaillierte Mob-Enzyklopädie, die vom einfachen Waldschlurfer bis zum epischen Welt-Boss reicht. Jeder Mob ist in `mobs.yml` detailliert definiert, mit spezifischen Gesundheits- und Schadenswerten, Ausrüstung und einer Liste von Skills, die er einsetzen kann.

*   **Standard-Mobs (Level 1-30)**: Bieten konstanten Fortschritt, mit ansteigenden HP, Schaden und XP. Ihre Taktiken variieren, oft mit Fähigkeiten wie `divine_blessing` zur Heilung oder `frost_bolt` für Fernkampfangriffe.
*   **Spezialisten (Unique Mobs)**: Einzigartige Begegnungen wie der `Seuchenbringer` oder der `Skelettkönig` fordern mit spezifischen Skill-Kombinationen wie `ember_shot` oder `shield_wall` heraus.
*   **Dungeon- und Elite-Mobs (Level 11-30)**: Eine Steigerung der Herausforderung, oft mit besserer Ausrüstung und komplexeren Skill-Kombinationen.
*   **Boss- und Welt-Bosse**: Die ultimativen Herausforderungen, vom `WITHER_SKELETON`-Boss bis zum `WITHER`-Welt-Boss mit astronomischen HP-Werten und NETHERITE-Ausrüstung. Sie nutzen ausgeklügelte Behavior Trees, die hohen Schaden und Verteidigungs-Buffs kombinieren, und bieten massive XP-Belohnungen sowie die besten Loots im Spiel.

### Behavior-Editor (Mob KI): Intelligenz für die Gegner
Der integrierte Behavior-Editor (`/behavior edit <tree>`) ist ein mächtiges Werkzeug für Administratoren, um komplexe KI-Bäume für Mobs zu erstellen und zu testen. Über eine GUI können Knoten wie `Selector`, `Sequence`, `Cooldown`, `MeleeAttack` oder `CastSkill` miteinander verknüpft werden, um das Verhalten der Gegner präzise zu steuern. Ein Skelettkönig könnte beispielsweise so konfiguriert werden, dass er im Nahkampf angreift, aber alle 8 Sekunden einen `ember_shot` einsetzt oder bei niedriger Gesundheit einen `shield_wall` aktiviert.

### Quests und Storytelling: Das Epos entfalten
Quests (`quests.yml`) sind der rote Faden, der Spieler durch die Welt führt. Sie werden über den `/rpgadmin quest` Befehl erstellt und verwaltet. Quest-Schritte können vielfältig sein: `KILL` (Monster jagen), `COLLECT` (Items sammeln), `TALK` (mit NPCs sprechen), `EXPLORE` (Zonen erkunden), `CRAFT` (Items herstellen), `USE_ITEM` (spezifische Items nutzen), `DEFEND` (Gebiete verteidigen) oder `ESCORT` (NPCs begleiten). NPCs können als Questgeber fungieren und Quests über `/rpgadmin npc linkquest <npcId> <questId>` an sich binden.

### Welt-Events und Meta-Quests: Dynamische Herausforderungen
Das System ermöglicht die Erstellung von dynamischen Welt-Events, die an bestimmte Zonen gebunden sind. Diese Events können Quest-Schritte (z.B. `KILL ZOMBIE 50`) und Belohnungen umfassen. Quests können sogar erst nach dem Abschluss eines Welt-Events freigeschaltet werden, was eine tiefere, emergentere Storytelling-Ebene ermöglicht.

### Ressourcen-Nodes & Berufe: Handwerk und Fortschritt
Spieler können sich in verschiedenen Berufen spezialisieren. Respawnende Sammelknoten, erstellt über `/rpgadmin node create mining IRON_ORE 60 8`, ermöglichen das gezielte Farmen von Rohstoffen und das Sammeln von Beruf-XP, beispielsweise im Bergbau.

## Das Endgame: Dungeons, Gilden und PvP

### Dungeon-Generierung (Jigsaw + WFC): Abenteuer jenseits der Oberfläche
Das Dungeon-System ist eine technische Meisterleistung, die temporäre, instanziierte Welten für Gruppenabenteuer schafft. Es nutzt eine Hybrid-Generierung aus Jigsaw-Räumen für die Makro-Struktur und Wave Function Collapse (WFC) für die Mikro-Details.

1.  **Voraussetzungen**: `schem`-Dateien für Jigsaw-Räume werden in `plugins/RPGPlugin/dungeon_rooms/<theme>/` abgelegt. Jigsaw-Blöcke mit `name` NBT-Feld definieren Sockets für die Raumverbindung. WFC-Patterns für die Detailfüllung liegen in `plugins/RPGPlugin/wfc/<theme>/`.
2.  **Konfiguration**: `config.yml` steuert Jigsaw- und WFC-Modus.
3.  **Generierung**: Über `/rpgadmin` oder `/dungeon generate <theme>` wird ein Dungeon generiert. Das System teleportiert die Party, spawnt Bosse und löscht die Welt nach Abschluss.
4.  **Matchmaking**: Spieler können sich über `/dungeon queue <theme>` für Dungeons anmelden und ihre Rolle (`/dungeon role <tank|heal|dps>`) festlegen.

### Gilden und Partys: Gemeinschaft und Kooperation
Soziale Interaktion ist ein Kernaspekt. Spieler können Partys gründen (`/party invite <Name>`) und XP im Umkreis teilen. Gilden (`/guild create <ID> <Name>`) bieten eine Gildenbank, Gildenquests und die Möglichkeit, eine Gildenhalle einzurichten und aufzuwerten.

### PvP und Elo-System: Der Kampf um Ruhm
Für kompetitive Spieler bietet das System PvP-Arenen, die in `arenas.yml` konfiguriert werden. Spieler können über `/pvp join` der Warteschlange für Elo-Matches beitreten. Regelmäßige PvP-Saisons (`/pvpadmin pvp seasonstart <id> <name> <days>`) bieten zusätzliche Anreize und Belohnungen.

## Die Kunst der Verzauberung: Item-Progression

Das **EnchantSystem** (`enchantments.yml`) ermöglicht es Spielern, ihre RPG-Items zu verbessern und zu individualisieren.

1.  **Voraussetzungen**: Ein RPG-Item, Gold und ggf. Material-Items. Rezepte sind in `plugins/RPGPlugin/enchantments.yml` definiert.
2.  **Verzauberungs-GUI**: Über `/rpg enchant` wird ein Menü geöffnet, das verfügbare Rezepte, Kosten und den Verzaubern-Button anzeigt.
3.  **Rezeptauswahl**: Spieler wählen ein Rezept, das den Ziel-Slot (z.B. `HAND`, `ARMOR_CHEST`), den Typ (Stat-Upgrade oder Affix) und die Kosten festlegt.
4.  **Ablauf**: Nach Prüfung von Item-Typ, Level und Kosten werden Gold und Materialien abgezogen. Das Item wird aktualisiert, und neue Werte erscheinen im Lore-Text, wie `Affix STRENGTH: +1` oder `Affixe: Praezision`. Effekte wie Sounds begleiten den Prozess.

## Der Admin als Architekt: Werkzeuge für die Weltgestaltung

Für Administratoren bietet MineLauncherRPG eine umfassende Suite an In-Game-Werkzeugen, die über das `/rpgadmin`-Menü zugänglich sind.

### Der Bau-Manager: Kreativität ohne Grenzen
Der Bau-Manager ist ein herausragendes Feature, das das Platzieren komplexer Strukturen aus Sponge-Schematics (`.schem`-Dateien) revolutioniert.

*   **Schematic-Ordner**: `.schem`-Dateien werden in `plugins/RPGPlugin/schematics/` abgelegt.
*   **Gebäudedefinitionen (`buildings.yml`)**: Hier werden Gebäude mit ihren Schematics, möglichen Stockwerken (`minFloors`, `maxFloors`, `floorHeight`), Kellern (`basement`) und sogar Möbel-Schematics definiert.
*   **Gebäude platzieren (GUI)**: Über `/rpgadmin` -> "Bau-Manager" können Kategorien und dann spezifische Gebäude ausgewählt und per Rechtsklick platziert werden. Das System unterstützt mehrstöckige Gebäude und die Integration von Möbeln.
*   **Einzel-Schema platzieren (GUI)**: Für präzise Einzelplatzierungen kann über den Bau-Manager ein spezifischer `haus.schem`-Dateiname eingegeben und platziert werden.
*   **Platzierung rückgängig machen**: Ein wichtiges Feature ist die Undo-Funktion (`/rpgadmin build undo`), die die letzte Platzierung für den Admin rückgängig macht. Auch das Verschieben von Schematics ist über eine eigene GUI möglich.

### Admin-GUIs: Vereinfachte Verwaltung
Fast alle Aspekte des RPG-Systems können über benutzerfreundliche In-Game-GUIs verwaltet werden:

*   **RPG-Menü (`/rpg`)**: Charakterübersicht, Skills, Quests, Fraktionen.
*   **Admin-Menü (`/rpgadmin`)**: Zentraler Zugang zu allen Editoren.
*   **Zonen-Editor**: Erstellung und Bearbeitung von Regionen.
*   **NPC-Editor**: Platzierung und Konfiguration von NPCs, inklusive Shop-Vorlagen.
*   **Quest-Editor**: Erstellung und Verwaltung von Quests und deren Schritten.
*   **Loot-Tabellen-Editor**: Konfiguration von Loot-Drops.
*   **Skills & Klassen-Editor**: Verwaltung von Fähigkeiten und Charakterklassen.
*   **Permissions-GUI**: Rollen, Vererbung, Nodes, Spielerrollen und Audit-Log für das Berechtigungssystem.
*   **Behavior-Editor GUI**: Visuelle Gestaltung der Mob-KI.

### Wichtige Admin-Befehle (Auszug)
Einige der mächtigsten Befehle umfassen:

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
| `/rpgadmin perms` | Permissions-GUI. |
| `/behavior edit <tree>` | Behavior-Editor. |

## Ein Tag im Leben eines Abenteurers: Der Spieler-Walkthrough

Die Reise durch MineLauncherRPG ist ein nahtloses Erlebnis, das Spieler von den ersten Schritten bis zu epischen End-Game-Herausforderungen führt:

1.  **Start & Klasse**: Nach dem Login wird die Klasse gewählt (`/rpg class choose warrior`).
2.  **Setup**: Ein erster Skill wird gebunden (`/rpg bind 2 smash`).
3.  **Quest**: Die Wache im Dorf bietet eine erste Quest an.
4.  **Kampf**: In der `Wald-Zone` spawnen automatisch Mobs.
5.  **Action**: Ein Tastendruck löst den gebundenen Skill aus.
6.  **Loot**: Besiegte Mobs droppen Gold und seltene Items mit generierten Stats.
7.  **Handel**: Im Dorf können Items verkauft und neue Ausrüstung erworben werden.
8.  **Abschluss**: Die Quest wird abgegeben, was XP und einen Levelaufstieg bringt.
9.  **Endgame**: Mit Freunden eine Gilde gründen, Dungeons bezwingen und sich im PvP messen!

## Fazit
MineLauncherRPG ist das Ergebnis einer Vision, Minecraft in ein tiefgreifendes, komplexes MMORPG-Erlebnis zu verwandeln. Als "Der Entwickler" bin ich stolz auf die modulare Architektur und die reichhaltigen Funktionen, die sowohl eine intuitive Spielerfahrung als auch mächtige Werkzeuge für Administratoren bieten. Von der prozeduralen Dungeon-Generierung über dynamische Mob-KI bis hin zu einem flexiblen Wirtschaftssystem – dieses Projekt ist ein Beweis dafür, was mit Leidenschaft und Innovation in der Welt der Spielentwicklung erreicht werden kann. Die Zukunft verspricht weitere Erweiterungen und Optimierungen, um die Welt von MineLauncherRPG noch lebendiger und fesselnder zu gestalten.

## Quellen
*   [https://api.papermc.io/v2/projects/paper/versions/{version}](https://api.papermc.io/v2/projects/paper/versions/{version})
*   [https://download.getbukkit.org/bukkit/bukkit-{serverConfig.Version}.jar](https://download.getbukkit.org/bukkit/bukkit-{serverConfig.Version}.jar)
*   [https://download.getbukkit.org/craftbukkit/craftbukkit-{serverConfig.Version}.jar](https://download.getbukkit.org/craftbukkit/craftbukkit-{serverConfig.Version}.jar)
*   [https://piston-meta.mojang.com/mc/game/version_manifest.json](https://piston-meta.mojang.com/mc/game/version_manifest.json)
*   [https://api.papermc.io/v2/projects/paper/versions/{buildInfo.Version}/builds/{buildInfo.Build}/downloads/paper-{buildInfo.Version}-{buildInfo.Build}.jar](https://api.papermc.io/v2/projects/paper/versions/{buildInfo.Version}/builds/{buildInfo.Build}/downloads/paper-{buildInfo.Version}-{buildInfo.Build}.jar)
*   [https://repo.papermc.io/repository/maven-public/](https://repo.papermc.io/repository/maven-public/)

## MIT License

Copyright (c) 2025 Ralf Krümmel

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.


---

*Dieser Artikel wurde von Ralf Krümmel (Der Entwickler) verfasst und mit Hilfe von künstlicher Intelligenz erstellt.*