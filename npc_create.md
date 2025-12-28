# NPC‑Erstellung – Ausführliche Anleitung

Dieses Dokument erklärt, wie NPCs im RPG‑Plugin erstellt werden, **inklusive der neuen Händler‑NPCs**, die RPG‑Items verkaufen (Waffenhändler, Rüstungshändler, Gegenstandshändler, Rohstoffhändler).

---

## 1) Voraussetzungen

- Du benötigst Admin‑Rechte (`rpg.admin`).
- Nutze `/rpgadmin` oder die Befehle direkt im Chat.

---

## 2) NPC erstellen (Grundlagen)

### Befehl
```
/rpgadmin npc create <id> <role>
```

> Alternativ: `/rpgadmin` → **NPC-Editor** öffnen und dort erstellen/bearbeiten.

### Beispiel
```
/rpgadmin npc create waffi WEAPON_VENDOR
```

**Wichtige Rollen:**
- `QUESTGIVER`
- `VENDOR` (statischer Shop aus `shops.yml`)
- `WEAPON_VENDOR` (neuer Waffenhändler)
- `ARMOR_VENDOR` (neuer Rüstungshändler)
- `ITEM_VENDOR` (neuer Gegenstandshändler)
- `RESOURCE_VENDOR` (neuer Rohstoffhändler)

---

## 3) NPC positionieren

NPCs werden **an der aktuellen Spielerposition** erstellt. Stelle dich also an den gewünschten Ort, bevor du den Befehl ausführst.

---

## 4) Dialog (optional)

Du kannst NPCs eine Dialogzeile geben:
```
/rpgadmin npc dialog <id>
```
Danach eine Zeile im Chat senden (wird gespeichert).

### 4.1 Branching‑Dialoge (dialogueNodes)
Für verzweigte Dialoge (inkl. Quest‑Gating/Fraktionsruf) bearbeitest du `npcs.yml`:

```yaml
guide:
  dialogueNodes:
    - id: start
      text: "Willkommen!"
      options:
        - text: "Ich suche Arbeit."
          nextId: quest_offer
        - text: "Wer bist du?"
          nextId: about
    - id: quest_offer
      text: "Hilf uns gegen die Invasion."
      options:
        - text: "Quest annehmen"
          grantQuestId: invasion_intro
          nextId: end
```

---

## 5) NPC‑Fraktion & Rang‑Gate

NPCs können Shops/Interaktionen per Ruf‑Rang sperren:
```
/rpgadmin npc faction <npcId> <factionId>
/rpgadmin npc rank <npcId> <rankId>
```
Beispiel:
```
/rpgadmin npc faction wache adventurers
/rpgadmin npc rank wache revered
```

---

## 6) Quest‑NPC

Wenn du einen Quest‑NPC möchtest:
```
/rpgadmin npc create quest1 QUESTGIVER
/rpgadmin npc linkquest quest1 starter_quest
```

---

## 7) Statischer Shop‑NPC (bestehendes System)

Diese NPCs nutzen `shops.yml`.

```
/rpgadmin npc create shop1 VENDOR
/rpgadmin npc linkshop shop1 blacksmith
```

---

## 8) Neue Händler‑NPCs (RPG‑Items)

Diese Händler generieren **dynamische RPG‑Items**, wenn sie angeklickt werden.
Die Items enthalten RPG‑Werte, Affixe und können im **EnchantSystem** genutzt werden.

### 8.1 Waffenhändler
```
/rpgadmin npc create weaponnpc WEAPON_VENDOR
```
- Verkauf: Schwerter, Bögen (RPG‑Items)
- Preise skalieren mit Spielerlevel & Rarity

### 8.2 Rüstungshändler
```
/rpgadmin npc create armornpc ARMOR_VENDOR
```
- Verkauf: Leder-, Ketten-, Eisenrüstungen (RPG‑Items)

### 8.3 Gegenstandshändler
```
/rpgadmin npc create itemnpc ITEM_VENDOR
```
- Verkauf: Lebensmittel, Tränke, Pfeile (RPG‑Items)

### 8.4 Rohstoffhändler
```
/rpgadmin npc create resourcenpc RESOURCE_VENDOR
```
- Verkauf: Rohstoffe (kein RPG‑Item, z. B. Nuggets, Ingots, Diamanten)

---

## 9) Wie funktioniert der RPG‑Shop?

- Beim **Anklicken** generiert der NPC einen Shop‑Inventar.
- Items werden **live erzeugt**, basierend auf dem Spielerlevel.
- RPG‑Items enthalten **Affixe** und sind **Enchanting‑fähig**.
- Kaufen/Verkaufen erfolgt direkt im NPC‑Shop‑GUI.

---

## 10) FAQ / Troubleshooting

**NPC reagiert nicht?**
- Prüfe, ob der NPC korrekt erstellt wurde.
- Prüfe, ob der NPC in `npcs.yml` vorhanden ist.

**Keine Items im Shop?**
- Prüfe, ob du die richtige Rolle benutzt hast.

**RPG‑Items nicht verzauberbar?**
- Stelle sicher, dass sie vom RPG‑Shop generiert wurden (RPG‑Items tragen interne Tags).

---

## 11) Kurz‑Workflow (Beispiel)

1. `/rpgadmin npc create weaponnpc WEAPON_VENDOR`
2. Spieler klickt NPC → Shop öffnet sich.
3. Spieler kauft RPG‑Waffe.
4. Spieler nutzt `/rpg enchant` → Item kann verzaubert werden.
