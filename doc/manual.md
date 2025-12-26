# MineLauncher & RPG Suite User Manual

_Welcome to the MineLauncher & RPG Suite! This comprehensive software package provides a custom launcher for managing and playing Minecraft, coupled with a rich RPG server framework. Whether you want to host a server with custom skills, quests, and dungeons, or simply play the game in offline mode, this application handles the technical heavy lifting. It features a built-in configuration editor, automated server downloading, and a fully integrated RPG gameplay experience._

## 1. Installation and First Launch

### Prerequisites
Before starting, ensure your system meets the following requirements:
*   **Windows OS**
*   **.NET 8 SDK** installed.
*   **Java (JDK 17+)** installed and added to your system PATH.

### Launching the Application
1.  Navigate to the application folder.
2.  Run the executable (e.g., `C_launcher.exe`) or use the command line: `dotnet run --project C_launcher`.
3.  Upon opening, you will see the main dashboard with a dark theme, a button panel at the top, a status log on the left, and a configuration editor on the right.

## 2. The Launcher Interface

The MineLauncher interface is designed for control and visibility:

*   **Top Control Panel**: Contains all primary action buttons (Create Config, Download, Launch, etc.).
*   **Status Log (Left Panel)**: Displays real-time logs, including download progress, server status, and error messages.
*   **Config Editor (Right Panel)**: A text area where you can directly modify the `launcher-config.json` file. Changes made here must be saved using the **Config speichern** button.

## 3. Core Workflow: Setting Up a Server

Follow these steps to set up your own Minecraft server:

1.  **Initialize Configuration**: Click **Config erstellen**. This generates the default settings in the editor.
2.  **Customize Settings**: In the right panel, you can modify:
    *   `server.type`: Choose `paper`, `vanilla`, or `bukkit`.
    *   `server.version`: Set the Minecraft version (e.g., "1.20.4").
    *   `server.onlineMode`: Set to `false` for offline play.
    *   `server.acceptEula`: Change this to `true` to accept the Minecraft EULA.
3.  **Save Changes**: Click **Config speichern** to apply your edits.
4.  **Download Server**: Click **Server downloaden**. The launcher will automatically fetch the correct JAR file based on your config. Watch the Status Log for completion.
5.  **Launch Server**: Click **Server starten**. The server console will open in a new window.

## 4. Core Workflow: Playing the Game

To play Minecraft using the integrated client launcher:

1.  **Configure Player**: In the Config Editor, find the `game` section. Set your `offlineUsername` (default is "Player").
2.  **Launch Client**: Click **Client starten**.
3.  **Wait for Load**: The launcher will verify game files (downloading them if necessary) and launch the Minecraft client window automatically logged in with your chosen offline username.

**Note**: You can also launch directly via command line using: `dotnet run --project C_launcher -- play <Username>`.

## 5. In-Game RPG Features

Once connected to the server, the **RPGPlugin** activates a full MMORPG experience. Here is how to play:

### The Main Menu
Type `/rpg` to open the main GUI. From here you can access:
*   **Character**: View level, XP, and class.
*   **Skills**: View and learn active/passive abilities.
*   **Quests**: Track active missions.
*   **Factions**: View reputation standings.

### Skills and Combat
*   **Learning**: Use `/rpg skilltree` or the menu to learn skills using Skill Points.
*   **Binding**: To use a skill, bind it to a hotbar slot (1-9). 
    *   Command: `/rpg bind <slot> <skillId>` (e.g., `/rpg bind 2 fireball`).
*   **Casting**: Hold an item in the bound slot and **Right-Click** to cast.

### Social & Economy
*   **Party**: Create a group with `/party create` and invite friends with `/party invite <name>`. XP is shared among members.
*   **Guilds**: Create a guild using `/guild create <id> <name>`. Guilds have a shared bank (`/guild bank`) and exclusive quests.
*   **Trading**: Securely trade items and gold with other players using `/trade request <player>`.
*   **Auction House**: Sell items by holding them and typing `/auction sell <price>`.

## 6. Admin & World Management Tools

For server administrators, the suite includes powerful content creation tools:

### World Creator
*   Command: `/worlds`
*   **Function**: Opens a GUI to instantly generate and teleport to custom world types, including **Sky Islands**, **Void**, **Water World**, **Jungle**, and **Desert**.

### RPG Admin Tools
*   Command: `/rpgadmin`
*   **Zone Editor**: Define regions with level requirements and mob difficulty modifiers.
*   **NPC Editor**: Create interactive NPCs for quests or shops.
*   **Building Manager**: Paste pre-defined schematics (buildings) into the world. Supports undo functionality via `/rpgadmin build undo`.
*   **Behavior Editor**: Configure complex AI behavior trees for custom mobs using `/behavior edit <tree>`.

## 7. Troubleshooting

### Common Issues
*   **Server won't start**: Ensure `server.acceptEula` is set to `true` in the config editor and saved.
*   **Java Error**: Verify that the `java.path` in the config matches your system's Java installation or is simply set to `java` if it's in your system PATH.
*   **Client Crash**: Check the `game.maxMemoryMb` setting. If your computer has limited RAM, try lowering this value (e.g., to 1024).
*   **"Unsupported schematic format"**: Ensure you are using Sponge v2 `.schem` files in the `plugins/RPGPlugin/schematics/` folder.

