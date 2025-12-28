# EnchantSystem – Ausführliche Anleitung

Dieses Dokument erklärt Schritt für Schritt, wie das **EnchantSystem** genutzt wird, um RPG‑Items zu verbessern (Stats erhöhen) oder neue **Affixe** hinzuzufügen.

---

## 1) Voraussetzungen

- Du benötigst ein **RPG‑Item** (Items, die vom RPG‑Plugin generiert wurden).
- Du brauchst **Gold** (Spielerwährung).
- Du brauchst ggf. ein **Material‑Item** (z. B. `IRON_NUGGET`) als Verbrauchsgegenstand.
- Die Verzauberungen werden in `plugins/RPGPlugin/enchantments.yml` definiert.
- Admins können Rezepte **direkt im Spiel** bearbeiten (siehe Admin‑GUI unten).
- RPG‑Items kommen u. a. aus **Loot**, **NPC‑Shops**, **Dungeons** und **Welt‑Events**.

---

## 2) Verzauberungs‑GUI öffnen

**Befehl:**
```
/rpg enchant
```

Das GUI zeigt:
- **Ziel‑Item** (Slot 10) – Standard: Item in deiner Main‑Hand.
- **Verfügbare Rezepte** (Slot 0–8) – abhängig von Item‑Typ & Spielerlevel.
- **Kosten‑Anzeige** (Slots 14–15) – benötigte Materialien & Gold.
- **Verzaubern‑Button** (Slot 22).
- **Schließen** (Slot 26).

---

## 2.1 Admin‑GUI (Rezepte bearbeiten)

Im Admin‑Menü gibt es einen eigenen Bereich **„Verzauberungen“**, um Rezepte zu erstellen, zu bearbeiten oder zu löschen.

**Pfad:** `/rpgadmin` → **Verzauberungen**  
**Aktionen (Chat‑Prompt):** `type`, `slot`, `stat`, `affix`, `minlevel`, `costgold`, `costitem`, `class`, `rarity`, `tags`, `addeffect`, `cleareffects`

---

## 3) Rezept auswählen

Klicke im GUI auf ein Rezept (z. B. `RuneOfStrength`).
Das ausgewählte Rezept bestimmt:
- Ziel‑Slot (z. B. `HAND`, `ARMOR_CHEST`).
- Stat‑Upgrade (`STAT_UPGRADE`) oder Affix (`AFFIX`).
- Kosten (Gold + Material‑Item).
- Effekte (z. B. Sound beim Erfolg).

---

## 4) Verzaubern (Ablauf)

1. **Ziel‑Item prüfen**
   - Muss ein RPG‑Item sein.
   - Muss zum Ziel‑Slot passen.
     - Beispiel: `ARMOR_CHEST` funktioniert nur mit Brustplatten.
2. **Level prüfen**
   - Du brauchst mindestens `minLevel` aus dem Rezept.
3. **Kosten prüfen**
   - Genügend Gold?
   - Genügend Material‑Items?
4. **Verzaubern ausführen**
   - Klick auf **Verzaubern** (Slot 22).
   - Gold & Material werden abgezogen.
   - Item wird aktualisiert (Stats/Affixe werden im Item‑NBT gespeichert).

---

## 5) Was passiert am Item?

Die Verzauberung speichert neue Werte im **PersistentDataContainer** des Items.
Diese Werte erscheinen automatisch im Lore‑Text des Items:
- **Affix‑Stats** (z. B. `Affix STRENGTH: +1`).
- **Affixe** (z. B. `Affixe: Praezision`).

Beispiel‑Lore nach Verzauberung:
```
Stärke: 3
Krit‑Chance: 4.0%
Leben: 5
Affix STRENGTH: +1
Affixe: Praezision
```

---

## 6) Beispiel‑Rezepte (enchantments.yml)

```yaml
RuneOfStrength:
  type: STAT_UPGRADE
  targetSlot: HAND
  statToImprove: STRENGTH
  minLevel: 1
  costGold: 250
  costItem: IRON_NUGGET:1
  effects:
    - type: SOUND
      params: { sound: ENTITY_PLAYER_LEVELUP, volume: 1.0, pitch: 1.5 }

RuneOfPrecision:
  type: AFFIX
  targetSlot: HAND
  affix: Praezision
  minLevel: 2
  costGold: 300
  costItem: GOLD_NUGGET:1
```

**Wichtige Felder:**
- `type`: `STAT_UPGRADE` oder `AFFIX`
- `targetSlot`: `HAND`, `OFF_HAND`, `ARMOR_HEAD`, `ARMOR_CHEST`, `ARMOR_LEGS`, `ARMOR_FEET`, `SHIELD`
- `statToImprove`: Ein Wert aus `RPGStat` (z. B. `STRENGTH`, `DEXTERITY`, `CONSTITUTION`)
- `affix`: Text‑Affix (bei `AFFIX`)
- `costGold`: Goldkosten
- `costItem`: Material:Menge
- `effects`: Effekte (optional, z. B. SOUND)
- `class`: Klassen‑Gate (`warrior`, `mage`, `ranger`, `any`)
- `rarity`: Freies Label für Tooltips/Balance
- `scaling` / `tags`: optionale Metadaten (derzeit informativ)

---

## 7) Tipps & Troubleshooting

- **Keine Rezepte sichtbar?**
  - Prüfe, ob das Ziel‑Item ein RPG‑Item ist.
  - Prüfe, ob der Ziel‑Slot passt.
  - Prüfe dein Level.
- **Shop‑Rabatte gelten nicht?**
  - Fraktions‑Rabatte gelten nur im NPC‑Shop, nicht für die Enchant‑Kosten.
- **Gold oder Material fehlt?**
  - Im GUI werden die Kosten angezeigt.
- **Lore zeigt nichts Neues?**
  - Stelle sicher, dass das Item nicht extern überschrieben wurde.

---

## 8) Kurz‑Workflow

1. RPG‑Item in die Hand nehmen.
2. `/rpg enchant` öffnen.
3. Rezept auswählen.
4. Kosten prüfen.
5. **Verzaubern** klicken.
6. Neues Affix/Stat im Item‑Lore sehen.
