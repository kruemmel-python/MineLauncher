# 📖 MineLauncherRPG – Spielerhandbuch

Willkommen in einer Welt voller Abenteuer! Dieses Handbuch erklärt dir alle Systeme, um deinen Charakter zu meistern, Reichtümer anzuhäufen und legendäre Gegner zu bezwingen.

---

## 1. Der Anfang & Charakter
Alles beginnt mit deinem Helden.

- **Das Hauptmenü:** Tippe `/rpg`, um deine Charakter-Übersicht zu öffnen. Hier siehst du dein Level, deine XP und deine Attribute.
- **Klasse wählen:**
  - ` /rpg class list` – Zeigt alle verfügbaren Klassen.
  - ` /rpg class choose <Name>` – Wähle deinen Pfad (z.B. `warrior` oder `mage`).
  - *Hinweis: Deine Klasse bestimmt deine Start-Skills und deinen Spielstil!*
- **Das HUD (Actionbar):** Über deiner Hotbar siehst du live deine Werte:
  `❤ Leben | 🔵 Mana | 💰 Gold`

---

## 2. Skills & Kampf
Vergiss normale Minecraft-Kämpfe. Hier nutzt du mächtige Fähigkeiten.

### Skills lernen (Der Skill-Tree)
1. Öffne den Skillbaum mit `/rpg skilltree`.
2. Du siehst ein Netzwerk aus Symbolen:
   - 🟩 **Grün:** Bereits gelernt.
   - 🟨 **Gelb:** Verfügbar (kostet Skillpunkte).
   - 🟥 **Rot:** Noch gesperrt (Voraussetzung fehlt).
3. Klicke auf gelbe Skills, um sie zu lernen. Skillpunkte erhältst du bei jedem Level-Up.

### Skills einsetzen (Die Hotbar)
Du musst keine Befehle tippen, um anzugreifen.
1. **Binden:** Lege einen Skill auf einen Hotbar-Slot (1-9).
   - Befehl: `/rpg bind <Slot> <Skill-ID>`
   - *Beispiel:* `/rpg bind 2 fireball`
2. **Kämpfen:**
   - Wähle im Spiel den Slot `2` aus (halte das Item in der Hand).
   - Mache einen **Rechtsklick** (in die Luft oder auf Gegner).
   - Der Skill feuert ab!
3. **Mana & Cooldown:** Achte auf dein blaues Mana im HUD. Deine XP-Leiste zeigt visuell an, wann der Skill wieder bereit ist.

---

## 3. Berufe (Professions)
Nicht nur Kämpfer werden belohnt. Verdiene XP durch Handwerk.

- **Übersicht:** `/rpg profession list` zeigt deine Berufs-Level.
- **Bergbau (Mining):** Baue Erze ab, um Mining-XP zu erhalten.
- **Kräuterkunde (Herbalism):** Ernte Feldfrüchte und Pflanzen.
- **Schmiedekunst (Blacksmithing):** Stelle Waffen und Rüstungen her.
  - *Achtung:* Manche Rezepte benötigen ein bestimmtes Berufs-Level!

---

## 4. Quests & Abenteuer
Erkunde die Welt nicht ziellos.

- **Quests finden:** Suche nach NPCs mit Namen über dem Kopf (z.B. "Wache").
- **Interagieren:** Mache einen Rechtsklick auf den NPC, um mit ihm zu sprechen.
- **Quest-Log:** `/rpg quest list` zeigt deine offenen Aufgaben.
- **Fortschritt:** Wenn du z.B. 5 Zombies töten sollst, wird dir jeder Kill direkt angezeigt. Gehe zurück zum NPC, um die Belohnung (XP, Gold, Ruf) abzuholen.

---

## 5. Social: Party & Gilden
Gemeinsam seid ihr stärker.

### Die Party
Teilt Erfahrungspunkte und Loot.
- `/party create` – Erstelle eine Gruppe.
- `/party invite <Name>` – Lade Freunde ein.
- `/party join <Name>` – Einer Einladung folgen.
- `/p <Nachricht>` – Privater Party-Chat.

### Die Gilde
Organisiere dich mit vielen Spielern.
- **Gründung:** `/guild create <Kürzel> <Name>`
- **Gildenbank:**
  - `/guild bank balance` – Kontostand prüfen.
  - `/guild bank deposit <Menge>` – Gold einzahlen.
- **Gilden-Quests:**
  - `/guild quest list` – Zeigt riesige Aufgaben, an denen alle Mitglieder gemeinsam arbeiten (z.B. "Tötet 1000 Monster").

---

## 6. Dungeons (Instanzen)
Die ultimative Herausforderung für deine Party.

1. **Vorbereitung:** Forme eine Party und rüste dich aus.
2. **Starten:** Der Leader nutzt `/dungeon generate <Thema>` (z.B. `wfc` für zufällige Labyrinthe).
3. **Ablauf:** Ihr werdet in eine **eigene Welt** teleportiert. Niemand kann euch stören.
   - Kämpft euch durch Räume.
   - Findet und besiegt den Boss.
4. **Verlassen:** `/dungeon leave` bringt euch sicher zurück in die Hauptstadt.

---

## 7. Wirtschaft & Handel
Werde der reichste Spieler des Servers.

- **Auktionshaus (Global):**
  - `/auction sell <Preis>` – Verkauft das Item in deiner Hand.
  - `/auction list` – Durchsuche Angebote anderer Spieler.
  - `/auction buy <ID>` – Kaufe ein Schnäppchen.
- **Sicherer Handel (Direkt):**
  - `/trade request <Name>` – Sende eine Handelsanfrage an einen Spieler in der Nähe.
  - `/trade offer <Gold>` – Biete Gold an.
  - `/trade ready` – Bestätigen (beide müssen bereit sein).

---

## 8. PvP & Arena
Miss dich mit anderen Spielern.

- **Warteschlange:** Tippe `/pvp join`. Das System sucht einen Gegner für dich.
- **Fairness (Elo):** Du gewinnst oder verlierst Punkte (Rating) basierend auf dem Ausgang des Kampfes.
- **Bestenliste:** `/pvp top` zeigt die mächtigsten Krieger des Servers.

---

## 9. Legendäre Ausrüstung
Achte genau auf die Beschreibung (Lore) deiner Items!

- **Affixe:** Ein "Scharfes Eisenschwert der Titanen" ist viel stärker als ein normales. Es hat zufällige Bonus-Werte wie:
  - *Stärke* (Erhöht Schaden)
  - *Krit-Chance* (Chance auf doppelten Schaden)
  - *Leben* (Erhöht deine max. HP)
- **Set-Boni:** Sammle 4 Teile desselben Sets (steht unten auf dem Item, z.B. "Ember Set"), um permanente Trank-Effekte wie Feuerresistenz oder Geschwindigkeit freizuschalten.