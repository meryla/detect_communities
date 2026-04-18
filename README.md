# README.md — Projet Algorithmique 2 (DBLP Communities)

## Structure du projet

```
project/
├── src/
│   ├── main.java                          # Point d'entrée principal
│   ├── DblpPublicationGenerator.java      # Parseur fourni (ne pas modifier)
│   ├── DblpParsingDemo.java               # Démo fournie (ne pas modifier)
│   ├── task1/
│   │   ├── UnionFind.java                 # Structure Union-Find
│   │   └── Task1Processor.java            # Logique de la tâche 1
│   ├── task2/
│   │   ├── PairCounter.java               # Comptage en ligne des paires (A → B)
│   │   ├── DirectedGraph.java             # Structure graphe orienté
│   │   ├── GraphBuilder.java              # Construction du graphe filtré (seuil >= 6)
│   │   ├── KosarajuSCC.java               # Algorithme de Kosaraju (SCC)
│   │   ├── DiameterCalculator.java        # Calcul du diamètre par BFS
│   │   └── Task2Processor.java            # Orchestrateur de la tâche 2
│   └── utils/
│       ├── AuthorUtils.java               # Nettoyage des listes d'auteurs
│       └── HistogramWriter.java           # Écriture des histogrammes
│
├── data/
│   ├── dblp-2026-01-01.xml.gz             # Snapshot DBLP (à télécharger séparément)
│   └── dblp.dtd                           # DTD officielle DBLP
│
├── output/                                # Fichiers générés (créé automatiquement)
│   ├── task1_histogram.txt
│   ├── task2_histogram.txt
│   └── task2_top10.txt
│
└── README.md
```

---

## Compilation

Créez les dossiers `bin/` et `output/` si nécessaire :

```bash
mkdir -p bin output
```

Depuis la racine du projet, compilez tous les fichiers Java :

```bash
javac -d bin \
  src/DblpPublicationGenerator.java \
  src/DblpParsingDemo.java \
  src/utils/AuthorUtils.java \
  src/utils/HistogramWriter.java \
  src/task1/UnionFind.java \
  src/task1/Task1Processor.java \
  src/task2/PairCounter.java \
  src/task2/DirectedGraph.java \
  src/task2/GraphBuilder.java \
  src/task2/KosarajuSCC.java \
  src/task2/DiameterCalculator.java \
  src/task2/Task2Processor.java \
  src/Main.java
```

Sur Windows (une seule ligne) :

```bash
javac -d bin src/DblpPublicationGenerator.java src/DblpParsingDemo.java src/utils/AuthorUtils.java src/utils/HistogramWriter.java src/task1/UnionFind.java src/task1/Task1Processor.java src/task2/PairCounter.java src/task2/DirectedGraph.java src/task2/GraphBuilder.java src/task2/KosarajuSCC.java src/task2/DiameterCalculator.java src/task2/Task2Processor.java src/main.java
```

---

## Exécution

### Run complet (tâche 1 + tâche 2)

```bash
java -Xmx4g -cp bin Main data/dblp-2026-01-01.xml.gz data/dblp.dtd
```

### Run limité aux N premières publications (test rapide)

```bash
java -Xmx4g -cp bin Main data/dblp-2026-01-01.xml.gz data/dblp.dtd --limit=500000
```

### Arguments attendus

| Position | Argument | Obligatoire |
|---|---|---|
| 1 | Chemin vers `dblp.xml.gz` | Oui |
| 2 | Chemin vers `dblp.dtd` | Oui |
| 3 | `--limit=N` (nombre de publications max) | Non |

---

## Fichiers de sortie

Tous les fichiers sont écrits dans le dossier `output/`.

| Fichier | Contenu |
|---|---|
| `output/task1_histogram.txt` | Histogramme des tailles des communautés (tâche 1) |
| `output/task2_histogram.txt` | Histogramme des tailles des SCC (tâche 2) |
| `output/task2_top10.txt` | Taille, diamètre et membres des 10 plus grandes SCC |

En plus des fichiers, le programme affiche sur le terminal :
- toutes les 100 000 publications : nombre de communautés et top 10 des tailles (tâche 1)
- à la fin : résumé complet des deux tâches

---

## Remarques

- Le fichier DBLP est traité en **mode streaming** (publication par publication) : le programme maintient un état cohérent à tout moment sans relire les publications passées.
- L'option `-Xmx4g` alloue 4 Go de mémoire JVM. Sur le fichier complet, 3–4 Go sont nécessaires.
- Le fichier `dblp-2026-01-01.xml.gz` peut être téléchargé à l'adresse suivante :
  https://drops.dagstuhl.de/storage/artifacts/dblp/xml/2026/dblp-2026-01-01.xml.gz

---

## Résumé des algorithmes

| Tâche | Algorithme | Complexité |
|---|---|---|
| Tâche 1 | Union-Find (union par taille + compression de chemin) | O(α(n)) par opération |
| Tâche 2 — online | Comptage de paires dans une HashMap | O(1) par paire |
| Tâche 2 — SCC | Algorithme de Kosaraju (2 DFS itératifs) | O(V + E) |
| Tâche 2 — diamètre | BFS depuis chaque nœud du sous-graphe induit | O(\|C\| × (\|C\| + \|E_C\|)) |

---

## Utilisation de l'IA générative

Certaines parties de ce code ont été assistées par Claude (Anthropic) pour la structure initiale et le débogage, conformément à la politique d'utilisation de l'IA du projet. L'ensemble du code a été relu, compris et peut être expliqué par les auteurs.