# Die Chroniken der Kreaturen – Mob-Enzyklopädie

**Datum der Erstellung:** 27. Dezember 2025
**Version:** 2.0 (Anreiz-Edition)

## 1. Zielsetzung: Wissen ist Macht

Dieses Dokument ist der ultimative Leitfaden zu den Kreaturen, denen sich Spieler und Admins stellen müssen. Wir enthüllen die versteckten Stärken, die taktischen Muster und die enormen Belohnungen, die mit jedem Sieg verbunden sind.

**Anreiz:** Das Wissen um die genauen Fähigkeiten jedes Mobs ermöglicht es Spielern, sich optimal vorzubereiten, und Admins, die Herausforderungen strategisch zu gestalten. **Jeder Mob ist ein Schlüssel zu besserer Beute und mehr Erfahrung.**

## 2. Grundlegende Mob-Fähigkeiten (Skill-Lexikon)

Die Mobs nutzen ein dynamisches Skill-System. Hier ist eine kurze Übersicht der identifizierten Zauber und Spezialangriffe:

| Skill-Name | Typische Auswirkung (Annahme) |
| :--- | :--- |
| **`divine_blessing`** | Heilung oder kurzzeitige Stärkung (Buff). |
| **`frost_bolt`** | Projektil-Angriff mit potenzieller Verlangsamung (Debuff). |
| **`execute`** | Ein starker Schadens-Skill, oft bei niedriger Gesundheit eingesetzt. |
| **`fortify`** | Erhöht die Verteidigung oder Rüstung für kurze Zeit. |
| **`power_strike`** | Ein mächtiger Nahkampf- oder Zauber-Schadenstoß. |
| **`heal_pulse`** | Eine Flächenheilung (Self-Heal) für den Mob. |
| **`ember_shot`** | Ein feuriger Projektil-Angriff. |
| **`shield_wall`** | Starke defensive Haltung oder kurzzeitige Unverwundbarkeit. |
| **`bleed_strike`** | Ein Angriff, der einen Blutungsschaden über Zeit (DoT) verursacht. |
| **`poison_cloud`** | Erzeugt einen Schadens- oder Vergiftungsbereich. |
| **`swamp_snare`** | Verlangsamt oder fesselt den Spieler. |
| **`venom_bite`** | Ein Giftangriff mit Schaden über Zeit. |
| **`frost_howl`** | Ein Wolf-Spezial, wahrscheinlich ein AoE-Kalt-Debuff. |
| **`tidal_strike`** | Ein Drowned-Spezial, möglicherweise ein mächtiger Nahkampfangriff. |
| **`screech`** | Ein Phantom-Angriff, wahrscheinlich mit Furcht- oder Betäubungseffekt. |
| **`arcane_burst`** | Ein magischer Flächenangriff. |
| **`deflect`** | Reduziert eingehenden Schaden oder lenkt Projektile ab. |
| **`mining_focus`** | Ein einzigartiger Skill, der auf das Abbauen von Blöcken oder einen Resilienz-Buff hindeuten könnte. |

## 3. Die Stufen der Herausforderung: Standard-Mobs (Level 1 - 30)

Diese Mobs bieten konstanten Fortschritt. Sie sind in thematisch sortierte Stufen unterteilt, wobei **HP, Schaden und XP pro Level ansteigen.**

| Level | HP-Basis | DMG-Basis | XP-Basis | Ausrüstung (Typisch) | LootTable |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | 24 | 3 | 26 | Gold/Eisen-Ausrüstung (Gemischt) | `forest_mobs` |
| **10** | 60 | 8 | 80 | Leder/Gold-Kopfbedeckung, Eisen/Diamant-Waffen (Gemischt) | `forest_mobs` |
| **20** | 100 | 13 | 140 | Leder/Gold-Kopfbedeckung, Eisen/Diamant-Waffen (Gemischt) | `dungeon_mobs` |
| **30** | 140 | 18 | 200 | Leder/Gold-Kopfbedeckung, Eisen/Diamant-Waffen (Gemischt) | `elite_mobs` |

**Beispiel-Taktik (Level 1 ZOMBIE, `lvl01_behavior_001.yml`):**
Dieser Zombie setzt bei unter **16% seiner HP** seine **`divine_blessing`** ein und heilt sich um **4.3** zurück, bevor er flieht. Er nutzt **`frost_bolt`** oder **`divine_blessing`** als Fernkampfwaffe, wenn der Spieler über 8 Blöcke entfernt ist.

*(Diese Struktur zieht sich durch alle Level, wobei die genutzte Fähigkeit und die Distanz-Trigger für das Auslösen der Skills variieren – siehe **Abschnitt 6** für die vollständige Aufschlüsselung der individuellen Mobs.)*

## 4. Die Spezialisten (Unique Mobs)

Diese Mobs sind einzigartige Begegnungen mit dedizierten Skills und Verhaltensweisen.

| Mob-Name | Typ | HP | Schaden | XP | Skills | Verhalten/Anreiz |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **§cSeuchenbringer** (Boss) | ZOMBIE | 60 | 8 | 120 | `ember_shot`, `whirlwind` | Ein mächtigerer Zombie-Boss. Das Mitführen einer **IRON\_HELMET** deutet auf höhere Verteidigung hin. |
| **§cSkelettkönig** (Boss) | SKELETON | 80 | 10 | 180 | `shield_wall`, `ember_shot` | **Boss-Erfahrung (180 XP)!** Nutzt **`shield_wall`**, was hohe Schadensresistenz bedeutet. |
| **§eWaldbandit** | PILLAGER | 36 | 6 | 70 | `power_strike` | Der erste Mob mit einer **Armbrust** (MainHand: CROSSBOW), was auf Fernkampf-Fokus hindeutet. |
| **§aWaldspäher** | ZOMBIE | 28 | 5 | 55 | `bleed_strike` | Nutzt **`bleed_strike`**, was langanhaltenden Schaden über Zeit verursachen kann. |
| **§5Sumpfhexe** | WITCH | 30 | 4 | 85 | `poison_cloud`, `swamp_snare` | Elite-Hexe mit CC-Fähigkeiten. **Wichtig:** Vorsicht vor Flächenschaden und Verlangsamung! |
| **§2Höhlenspinne** | CAVE\_SPIDER | 24 | 5 | 50 | `venom_bite` | Nutzt **`venom_bite`** – Gift-Schaden-über-Zeit-Fokus! |
| **§bFrostwolf** | WOLF | 34 | 7 | 75 | `frost_howl` | Elite-Wolf, der vermutlich Spieler mit Kälte-Effekten belegt. |
| **§3Ertrunkener Freibeuter** | DROWNED | 32 | 6 | 80 | `tidal_strike` | Ein starker Drowned mit einer einzigartigen Wasser-Attacke. |
| **§dPhantomfürst** (Boss) | PHANTOM | 40 | 7 | 110 | `screech` | **Boss!** `screech` könnte ein Reichweiten-Schockeffekt sein. |

*(Für die Mobs `mob_010` bis `mob_200` gelten die generischen Level-Werte (HP/DMG) der Mobs, die sie laut ihrer Level-Zuweisung zugeordnet bekommen, während sie ihre einzigartigen Skill-Kombinationen nutzen.)*

## 5. Die Gipfelstürmer: Dungeon- und Elite-Mobs (Level 11 - 30)

Diese Mobs repräsentieren eine deutliche Steigerung der Herausforderung und werden mit besserer Ausrüstung (teilweise Gold/Diamant-Kopfbedeckung, Leder/Diamant-Waffen) ausgestattet.

*   **Level 11-15 (Elite Dungeon Mobs):** Basis-HP ca. 64-80, DMG ca. 8-10. Sie nutzen häufig Kombinationen aus **`fortify`, `frost_bolt`, `divine_blessing`** und **`execute`**.
*   **Level 21-25 (Höheres Elite-Tier):** Basis-HP ca. 104-120, DMG ca. 13-15. Hier dominieren **`power_strike`** und **`heal_pulse`** in Kombination.
*   **Level 26-30 (Höchste Elite-Stufe):** Basis-HP ca. 124-140, DMG ca. 16-18. Die Mobs werden aggressiver und nutzen oft stärkere Heil- oder Schadens-Kombinationen, häufig mit **`power_strike`** oder wiederkehrenden Heil-Skills.

## 6. Die Unbesiegbaren: Boss- und Welt-Boss-Statistiken

Dies sind die ultimativen Herausforderungen.

### A. Standard-Level-Bosse (Level 5 - 100)

Diese Bosse sind meist Varianten von **`WITHER_SKELETON`** und nutzen die Skills **`execute`** und **`shield_wall`** (oder `fortify`) in ihren Behavior Trees.

| Boss-Level | HP-Bereich | DMG-Bereich | XP-Belohnung | LootTable | Taktischer Fokus |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Level 5 - 10** | 240 - 280 | 13 - 15 | 600 - 700 | `boss_loot` | Erlernen der Boss-Mechanik mit **`shield_wall`**. |
| **Level 51 - 60** | 608 - 680 | 29 - 32 | 1520 - 1700 | `boss_loot` | Hohe HP-Werte erfordern effizientes Schadensmanagement. |
| **Level 91 - 100** | 928 - 1000 | 42 - 45 | 2320 - 2500 | `boss_loot` | **Massive XP-Belohnungen!** Sehr hohe Grundwerte. |

### B. Welt-Bosse (Level 120 - 169)

Diese Bosse sind die epische Endstufe, alle vom Typ **`WITHER`** mit **NETHERITE-Ausrüstung**.

*   **HP-Bereich:** 2000 bis 2490 HP.
*   **DMG-Bereich:** 50 bis 62 Schaden.
*   **XP-Belohnung:** Zwischen 5600 und 7070 XP pro Besieg.
*   **Behavior Tree:** Sie nutzen die Behavior Trees niedrigerer Level (z.B. **`lvl01_behavior_001.yml`**) **mit den Skills `execute` und `fortify`**, was bedeutet, dass ihre Standard-Strategie **hoher Schaden bei niedrigem Leben** und **Verteidigungs-Buffs** ist, skaliert auf ihre astronomischen Werte.
*   **Anreiz:** Die **`world_boss_loot`** garantiert die besten Belohnungen im Spiel.

***
**FAZIT:** Jeder Mob bietet eine einzigartige Herausforderung. Spieler sollten die spezifischen Skills ihrer Gegner (z.B. `bleed_strike` beim Waldspäher oder `poison_cloud` bei der Sumpfhexe) studieren, um ihre Ausrüstung und Heiltränke optimal anzupassen. Bosse und Welt-Bosse bieten die höchsten Erfahrungspunkte und die besten Loots und sind der ultimative Test für jede Abenteurergruppe!