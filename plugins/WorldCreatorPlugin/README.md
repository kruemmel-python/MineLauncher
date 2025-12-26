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
