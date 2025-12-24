# RPG-Handbuch

Dieses Handbuch beschreibt die verfügbaren Befehle des RPG-Plugins für Spieler und Admins.
Alle Beispiele sind Ingame-Kommandos (Chat/Console im Spiel).

## Spieler-Befehle

### `/rpg`
Öffnet das Hauptmenü des RPG-Plugins.

**Beispiel:**
```
/rpg
```

---

### `/rpg quests`
Öffnet das Questlog bzw. zeigt an, dass das Questlog geöffnet wird.

**Beispiel:**
```
/rpg quests
```

---

### `/rpg skills`
Öffnet den Skilltree bzw. zeigt an, dass der Skilltree geöffnet wird.

**Beispiel:**
```
/rpg skills
```

---

### `/rpg stats`
Zeigt Charakterwerte bzw. lädt die Statistik-Ansicht.

**Beispiel:**
```
/rpg stats
```

---

### `/rpg help`
Zeigt eine kurze Hilfe-Liste zu den Spieler-Befehlen.

**Beispiel:**
```
/rpg help
```

---

## Admin-Befehle

> Hinweis: Alle Admin-Befehle nutzen den Root-Befehl `/rpgadmin`.

### `/rpgadmin`
Öffnet das Admin-Panel (Platzhalter-Feedback im Chat).

**Beispiel:**
```
/rpgadmin
```

---

### `/rpgadmin wand`
Gibt dem Admin die **RPG-Editor-Wand** (Blaze Rod) zum Markieren von Zonen.

**Beispiel:**
```
/rpgadmin wand
```

**Editor-Wand Aktionen:**
- **Linksklick Block:** Pos1 setzen
- **Rechtsklick Block:** Pos2 setzen
- **Shift + Rechtsklick:** Editor-GUI (Platzhalter)
- **Shift + Linksklick (in die Luft):** Auswahl zurücksetzen

---

### `/rpgadmin zone create <id>`
Startet das Erstellen einer Zone mit der angegebenen ID.

**Beispiel:**
```
/rpgadmin zone create forest_1
```

---

### `/rpgadmin zone edit <id>`
Öffnet den Zonen-Editor für eine bestehende Zone.

**Beispiel:**
```
/rpgadmin zone edit forest_1
```

---

### `/rpgadmin npc create <id>`
Erstellt einen NPC mit der angegebenen ID.

**Beispiel:**
```
/rpgadmin npc create blacksmith
```

---

### `/rpgadmin quest create <id>`
Erstellt eine neue Quest mit der angegebenen ID.

**Beispiel:**
```
/rpgadmin quest create intro_quest
```

---

### `/rpgadmin quest edit <id>`
Bearbeitet eine bestehende Quest.

**Beispiel:**
```
/rpgadmin quest edit intro_quest
```

---

### `/rpgadmin quest publish <id>`
Veröffentlicht eine Quest (Status → published).

**Beispiel:**
```
/rpgadmin quest publish intro_quest
```

---

### `/rpgadmin loottable edit <id>`
Bearbeitet eine Loot-Tabelle.

**Beispiel:**
```
/rpgadmin loottable edit starter_loot
```

---

### `/rpgadmin player inspect <name>`
Zeigt Debug-Informationen für einen Spieler.

**Beispiel:**
```
/rpgadmin player inspect Steve
```

---

### `/rpgadmin player fix <name>`
Repariert/validiert die Daten eines Spielers.

**Beispiel:**
```
/rpgadmin player fix Steve
```
