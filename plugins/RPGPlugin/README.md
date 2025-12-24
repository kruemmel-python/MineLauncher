# RPGPlugin

Autarkes Minecraft-RPG-Plugin (Paper 1.20.4) mit geschichteter Architektur.

## Build

```bash
mvn -f plugins/RPGPlugin/pom.xml package
```

Die JAR liegt danach unter `plugins/RPGPlugin/target/`.

## Architektur-Überblick

- **Player UX** → GUI/NPC/Feedback
- **Gameplay Layer** → Quests, Skills, Loot, Zonen
- **Core Services** → PlayerData, Registry, Flags
- **Persistence** → JSON/YAML (Gson + Filesystem)

Kein Layer greift nach oben.

## Wichtige Entry Points

- `de.yourname.rpg.RPGPlugin` (Main)
- `de.yourname.rpg.core.PluginContext`
- `de.yourname.rpg.command.RpgCommand`
- `de.yourname.rpg.command.RpgAdminCommand`
