# Architektur-Spezifikation: Projekt "Myco-Swarm"
**Bio-Digitales Hybridsystem für emergente Intelligenz**

**Autor:** Lead Systems Architect (Artificial Life Division)  
**Status:** Entwurf / Spezifikation  
**Technologie-Stack:** C++ (Host), OpenCL (Device/Compute), Embedded Driver Loader

---

## 1. Exekutive Zusammenfassung
Wir entwerfen ein massiv-paralleles, agentenbasiertes System, das Intelligenz nicht durch prä-trainierte Gewichte (wie bei Deep Learning), sondern durch **dynamische Selbstorganisation** erzeugt. Das System emuliert die Synergie zwischen mobilen Einheiten (Ameisen-Analogie) und stationären Infrastrukturen (Mycel-Analogie).

Technisch wird dies als **Standalone C++ Applikation** realisiert, die einen **integrierten OpenCL-Loader** verwendet. Dies eliminiert die Notwendigkeit für den Endnutzer, ein OpenCL SDK zu installieren; die Anwendung kommuniziert direkt mit den vorhandenen GPU-Treibern (NVIDIA/AMD/Intel) über dynamisches Laden der Bibliotheken (`OpenCL.dll` / `libOpenCL.so`) zur Laufzeit.

---

## 2. Technische Systemarchitektur (C++ & Embedded OpenCL)

Um die Anforderung "keine Extra-Installation" zu erfüllen, nutzen wir eine **Dynamic Dispatch Architecture**.

### 2.1 Der "Embedded" OpenCL-Treiber
Anstatt gegen statische Bibliotheken zu linken, implementiert der C++ Host einen **Function Pointer Loader**.
*   **Mechanismus:** Der C++ Code beinhaltet einen minimalen Header-Only-Loader (ähnlich wie `glad` für OpenGL oder `clew`).
*   **Initialisierung:** Beim Start sucht die `main()` nach systemweiten GPU-Treibern, lädt die Shared Library dynamisch und mappt die Funktionszeiger (`clCreateContext`, `clEnqueueNDRangeKernel`, etc.).
*   **Vorteil:** Die Binary ist portabel ("Portable Executable"). Solange ein Grafikkartentreiber existiert, läuft das System.

### 2.2 Speicher-Management (Host-Device)
Da wir Millionen von Agenten simulieren, ist die Speicherbandbreite der Flaschenhals.
*   **Datenstruktur:** **Struct-of-Arrays (SoA)** statt Array-of-Structs (AoS). Dies ermöglicht Coalesced Memory Access auf der GPU.
*   **Zero-Copy:** Wo möglich, nutzen wir `CL_MEM_USE_HOST_PTR` oder Shared Virtual Memory (SVM), um unnötige Kopieroperationen zwischen CPU und GPU zu vermeiden.

---

## 3. Bio-Architektur & Subsysteme

Das System besteht aus vier interagierenden Schichten, die in separaten OpenCL-Kerneln berechnet werden.

### 3.1 Die Umwelt (Das Substrat)
Ein 2D/3D-Gitter, das als Träger für chemische Signale und Ressourcen dient.
*   **Funktion:** Speicherung von Pheromon-Konzentrationen und Nährstoffen.
*   **Physik:** Diffusion und Verdunstung (Evaporation). Dies stellt das **Kurzzeitgedächtnis** des Systems dar. Informationen, die nicht erneuert werden, verblassen.

### 3.2 Die Agenten (Die Schwarm-Aktuatoren)
Autonome Einheiten ohne globales Wissen.
*   **Logik:** Keine neuronalen Netze. Stattdessen probabilistische Zustandsautomaten (Finite State Machines), moduliert durch DNA-Parameter.
*   **Sensoren:** Lesen lokale Gradienten (Pheromone, Mycel-Dichte, Nahrung) in einem 3x3 (oder 5x5) Radius.
*   **Aktion:** Bewegung, Nahrungsaufnahme, Pheromonabgabe, Mycel-Stimulation.

### 3.3 Das Mycel (Das Struktur-Gedächtnis)
Ein statisches, wachsendes Netzwerk, das durch Agentenaktivität entsteht.
*   **Analogie:** Langzeitpotenzierung (LTP) im Gehirn oder Straßenbau.
*   **Funktion:**
    *   **Transport:** Ressourcen fließen entlang von Mycel-Strängen fast instantan (Tunneling).
    *   **Effizienz:** Agenten verbrauchen weniger Energie, wenn sie sich auf Mycel bewegen.
*   **Wachstumsregel:** Mycel wächst dort, wo der Pheromon-Fluss einen Schwellenwert übersteigt (Hebbsche Regel: "Fire together, wire together"). Es zerfällt bei Nichtnutzung.

### 3.4 DNA & Evolution (Der Meta-Optimierer)
*   **Speicher:** Ein Genom pro Agent (Bitstring oder Float-Array).
*   **Mutation:** Findet auf dem Host (C++) statt, wenn Agenten sich replizieren.
*   **Selektion:** Nur Agenten mit genug Energie können sich teilen.

---

## 4. Datenstrukturen (OpenCL C Definitionen)

Um maximale Performance zu garantieren, sind die Datenstrukturen flach und packed.

```c
// Pseudocode für OpenCL Kernel Strukturen

// 1. UMWELT (Grid) - Texture2D oder 1D Buffer
// Jeder Pixel enthält 4 Kanäle (float4):
// x: Pheromon A (Attraktion/Nahrung)
// y: Pheromon B (Repulsion/Gefahr)
// z: Mycel-Dichte (Struktur)
// w: Ressourcen-Menge

// 2. AGENTEN (SoA Layout für GPU Performance)
// Wir nutzen separate Buffer für jede Eigenschaft:
__global float2* agent_positions;  // x, y Koordinaten
__global float2* agent_velocities; // Bewegungsvektor
__global float*  agent_energy;     // Lebensenergie
__global int*    agent_state;      // Aktueller Modus (Suchen, Ernten, Bauen)
__global uint*   agent_dna_index;  // Verweis auf Genom-Parameter

// 3. DNA (Globaler Lookup Buffer, Read-Only für Kernel)
typedef struct {
    float sensor_angle;      // Sichtfeld
    float turn_speed;        // Drehgeschwindigkeit
    float pheromone_deposit; // Menge an Pheromonabgabe
    float mycel_threshold;   // Ab wann baut der Agent Mycel?
    float mutation_rate;     // Stabilität der DNA
} AgentDNA;
```

---

## 5. Lokale Regeln & Interaktions-Dynamik

Das emergente Verhalten entsteht durch die strikte Anwendung folgender lokaler Regeln in jedem Simulationsschritt (Tick).

### Phase 1: Sensorik & Entscheidung (Kernel: `update_agents`)
Jeder Agent $A$ an Position $P$:
1.  **Sensing:** Samplet das Grid an Position $P + \text{SensorVektor}$.
    *   Input: `Pheromon_Gradient`, `Mycel_Dichte`.
2.  **Processing (DNA-moduliert):**
    *   Berechne gewünschten Winkel $\theta$.
    *   Wenn `Mycel_Dichte` > 0.8: Folge dem Mycel (High-Speed Modus).
    *   Wenn `Pheromon` > Schwellenwert: Drehe Richtung Gradient (Chemotaxis).
    *   Sonst: Random Walk (Brownsche Bewegung) basierend auf DNA-Parameter `randomness`.
3.  **Actuation:**
    *   Update Position $P_{neu} = P + v$.
    *   Verbrauche Energie $E = E - (\text{Basalkosten} - \text{MycelBonus})$.

### Phase 2: Umwelt-Interaktion (Kernel: `deposit_and_interact`)
1.  **Pheromone:** Agent hinterlässt Pheromon an $P_{neu}$. Menge korreliert mit `agent_energy` (Erfolgreiche Agenten "schreien" lauter).
2.  **Mycel-Trigger:** Wenn Agent oft denselben Pfad nutzt (hohe lokale Pheromondichte) UND Energie hat $\rightarrow$ Konvertiere Pheromon in `Mycel-Dichte` (Verfestigung).
3.  **Ernte:** Wenn Ressource an $P_{neu}$ vorhanden $\rightarrow$ Energie aufnehmen, Ressource vom Grid entfernen.

### Phase 3: Umwelt-Physik (Kernel: `diffuse_evaporate`)
Ein zellulärer Automat über das gesamte Grid:
1.  **Diffusion:** Pheromone breiten sich zu Nachbarzellen aus (Box-Blur).
2.  **Evaporation:** Alle Pheromone werden mit Faktor $\lambda$ (z.B. 0.95) multipliziert.
3.  **Mycel-Zerfall:** Mycel verliert Dichte sehr langsam (Langzeitgedächtnis), wenn keine Agenten darüber laufen.

---

## 6. Emergenz-Erwartung & Gedächtnisbildung

Durch diese Architektur erwarten wir folgende Phänomene ohne explizite Programmierung:

1.  **Dynamisches Routing:** Agenten finden den kürzesten Weg zu Ressourcen (ähnlich dem *Physarum polycephalum* Schleimpilz).
2.  **Gedächtnis-Hierarchie:**
    *   *RAM (Pheromone):* Das System "erinnert" sich kurzzeitig an aktuelle Ereignisse.
    *   *HDD (Mycel):* Etablierte Routen verfestigen sich physisch. Selbst wenn Pheromone verdampfen, bleibt die "Straße" (das Mycel) bestehen und kann später reaktiviert werden.
3.  **Adaptivität:** Ändert sich die Position der Ressource, sterben alte Mycel-Pfade ab (Energie-Mangel), und neue Pheromon-Spuren bilden neue Strukturen.

---

## 7. Implementierungs-Strategie (C++ Host Loop)

Der C++ Host agiert nur als Orchestrator ("Der Taktgeber").

```cpp
void SimulationLoop() {
    // 1. OpenCL Treiber dynamisch laden (falls noch nicht geschehen)
    // 2. Kernel Argumente setzen
    
    while(running) {
        // A. Physik-Update (Grid)
        clEnqueueNDRangeKernel(queue, kernel_diffusion, ...);
        
        // B. Agenten-Logik
        clEnqueueNDRangeKernel(queue, kernel_agents_sense_move, ...);
        
        // C. Interaktion (Schreiben in Grid)
        clEnqueueNDRangeKernel(queue, kernel_agents_deposit, ...);
        
        // D. Evolutionärer Schritt (selten, z.B. alle 1000 Ticks)
        // Kopiere Agenten-Daten auf Host -> Selektion/Mutation -> Zurück auf Device
        if (tick % 1000 == 0) PerformEvolutionOnHost();
        
        // E. Rendering (OpenGL Interop oder Texture Read)
    }
}
```

---

## 8. Abgrenzung zu Neuronalen Netzen
Dieses System nutzt keine Matrixmultiplikationen, keine Backpropagation und keine Layer. Das "Lernen" ist:
1.  **Räumlich:** Wo wächst das Mycel? (Topologisches Lernen).
2.  **Evolutionär:** Welche Parameter (DNA) überleben? (Parameter-Optimierung).

Dies ermöglicht extrem effiziente Simulationen von Millionen Agenten auf Consumer-Hardware, da die Rechenoperationen pro Agent minimal sind (nur Vergleiche und Vektor-Additionen), im Gegensatz zu rechenintensiven Dot-Products in ANNs.
