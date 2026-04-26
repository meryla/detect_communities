# README.md — Projet Algorithmique 2 (DBLP Communities)

## Structure du projet

```
project/
├── src/
│   ├── Main.java                        # Point d'entrée principal
│   ├── DblpPublicationGenerator.java    # Parseur fourni (ne pas modifier)
│   ├── DblpParsingDemo.java             # Démo fournie (ne pas modifier)
│   ├── task1/
│   │   ├── UnionFind.java               # Structure Union-Find
│   │   └── Task1Processor.java          # Logique de la tâche 1
│   ├── task2/
│   │   ├── PairCounter.java             # Comptage en ligne des paires (A → B)
│   │   ├── DirectedGraph.java           # Structure graphe orienté
│   │   ├── GraphBuilder.java            # Construction du graphe filtré (seuil >= 6)
│   │   ├── KosarajuSCC.java             # Algorithme de Kosaraju (SCC)
│   │   ├── DiameterCalculator.java      # Calcul du diamètre par BFS
│   │   └── Task2Processor.java          # Orchestrateur de la tâche 2
│   └── utils/
│       ├── AuthorUtils.java             # Nettoyage des listes d'auteurs
│       └── HistogramWriter.java         # Écriture des histogrammes
│
├── data/
│   ├── dblp-2026-01-01.xml.gz           # Snapshot DBLP (à télécharger séparément)
│   └── dblp.dtd                         # DTD officielle DBLP
│
├── output/                              # Fichiers générés (créé automatiquement)
│   ├── task1_histogram.txt
│   ├── task2_histogram.txt
│   ├── task2_top10.txt
│   └── country_stats/                   # Résultats du bonus
│       ├── community_X_countries.png
│       ├── country_summary.json
│       ├── country_summary.txt
│       └── openalex_cache.json
│
├── bonus_countries.py                   # Script bonus (analyse des pays)
└── README.md
```

---

## Compilation

Créer les dossiers nécessaires :

```bash
mkdir -p bin output
```

Compiler le projet :

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

Windows  :

```bash
javac -d bin src/DblpPublicationGenerator.java src/DblpParsingDemo.java src/utils/AuthorUtils.java src/utils/HistogramWriter.java src/task1/UnionFind.java src/task1/Task1Processor.java src/task2/PairCounter.java src/task2/DirectedGraph.java src/task2/GraphBuilder.java src/task2/KosarajuSCC.java src/task2/DiameterCalculator.java src/task2/Task2Processor.java src/Main.java
```

---

## Exécution Java

Run complet :

```bash
java -Xmx4g -cp bin Main data/dblp-2026-01-01.xml.gz data/dblp.dtd
```

Run avec limite (test rapide) :

```bash
java -Xmx4g -cp bin Main data/dblp-2026-01-01.xml.gz data/dblp.dtd --limit=500000
```

### Arguments

| Position | Argument | Obligatoire |
|---|---|---|
| 1 | Chemin vers `dblp.xml.gz` | Oui |
| 2 | Chemin vers `dblp.dtd` | Oui |
| 3 | `--limit=N` | Non |

---

## Fichiers de sortie Java

| Fichier | Description |
|---|---|
| `task1_histogram.txt` | Histogramme des tailles des communautés (Tâche 1) |
| `task2_histogram.txt` | Histogramme des tailles des SCC (Tâche 2) |
| `task2_top10.txt` | Top 10 des SCC : taille, diamètre, membres |

---

## Bonus — Analyse des pays

Le script `bonus_countries.py` analyse la répartition géographique des auteurs dans les 10 plus grandes SCC via l'API OpenAlex (publique et gratuite, aucune clé requise).

### Prérequis

```bash
pip install requests matplotlib
```

### Exécution

```bash
python bonus_countries.py
```

### Résultats générés dans `output/country_stats/`

| Fichier | Description |
|---|---|
| `community_X_countries.png` | Graphique des pays par communauté |
| `country_summary.json` | Données complètes en JSON |
| `country_summary.txt` | Résumé lisible |
| `openalex_cache.json` | Cache des requêtes (évite les appels répétés) |

---

## Remarques

- Traitement DBLP en **streaming** : état cohérent maintenu publication par publication, sans relecture.
- Mémoire recommandée : `-Xmx4g` (3–4 Go suffisent pour le dataset complet).
- Dataset DBLP : https://drops.dagstuhl.de/storage/artifacts/dblp/xml/2026/dblp-2026-01-01.xml.gz

---


