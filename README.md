# README.md — Projet Algorithmique 2 (DBLP Communities)

## Structure du projet
project/
├── src/
│ ├── main.java # Point d'entrée principal
│ ├── DblpPublicationGenerator.java # Parseur fourni 
│ ├── DblpParsingDemo.java # Démo fournie 
│ ├── task1/
│ │ ├── UnionFind.java # Structure Union-Find
│ │ └── Task1Processor.java # Logique de la tâche 1
│ ├── task2/
│ │ ├── PairCounter.java # Comptage en ligne des paires (A -> B)
│ │ ├── DirectedGraph.java # Structure graphe orienté
│ │ ├── GraphBuilder.java # Construction du graphe filtré (seuil >= 6)
│ │ ├── KosarajuSCC.java # Algorithme de Kosaraju (SCC)
│ │ ├── DiameterCalculator.java # Calcul du diamètre par BFS
│ │ └── Task2Processor.java # Orchestrateur de la tâche 2
│ └── utils/
│ ├── AuthorUtils.java # Nettoyage des listes d'auteurs
│ └── HistogramWriter.java # Écriture des histogrammes
│
├── data/
│ ├── dblp-2026-01-01.xml.gz # Snapshot DBLP (à télécharger séparément)
│ └── dblp.dtd # DTD officielle DBLP
│
├── output/ # Fichiers générés (créé automatiquement)
│ ├── task1_histogram.txt
│ ├── task2_histogram.txt
│ └── task2_top10.txt
│
├── bonus_country_analysis.py # Script bonus (analyse des pays)
└── README.md

---

## Compilation

Créer les dossiers nécessaires :

```bash
mkdir -p bin output

Compiler le projet :
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
Windows (ligne unique) :
javac -d bin src/DblpPublicationGenerator.java src/DblpParsingDemo.java src/utils/AuthorUtils.java src/utils/HistogramWriter.java src/task1/UnionFind.java src/task1/Task1Processor.java src/task2/PairCounter.java src/task2/DirectedGraph.java src/task2/GraphBuilder.java src/task2/KosarajuSCC.java src/task2/DiameterCalculator.java src/task2/Task2Processor.java src/Main.java


Exécution Java

Run complet
java -Xmx4g -cp bin Main data/dblp-2026-01-01.xml.gz data/dblp.dtd

Run avec limite (test rapide)
java -Xmx4g -cp bin Main data/dblp-2026-01-01.xml.gz data/dblp.dtd --limit=500000


Fichiers de sortie
Tous les résultats sont dans output/ :
Fichier	Description
task1_histogram.txt	Histogramme des tailles des communautés
task2_histogram.txt	Histogramme des SCC
task2_top10.txt	Top 10 des SCC (taille, diamètre, membres)



🟢 Bonus — Analyse des pays des auteurs
Un script Python (bonus_country_analysis.py) permet d’analyser
les pays d’origine des auteurs dans les 10 plus grandes communautés.


📌 Prérequis
Python 3
Installer les dépendances :
pip install requests matplotlib


🔑 Clé API OpenAlex
Le script nécessite une clé API OpenAlex.

 Important : la clé n’est PAS incluse dans le projet.
Définir la variable d’environnement :

Linux / Mac
export OPENALEX_API_KEY="YOUR_API_KEY_HERE"

# put your api key  

Windows (PowerShell)
setx OPENALEX_API_KEY "YOUR_API_KEY_HERE"

▶️ Exécution du bonus
python bonus_country_analysis.py



⚠️ Comportement sans clé API
Si aucune clé API n’est définie :
le script s’arrête proprement
aucun appel API n’est effectué


📊 Résultats générés
Dans output/country_stats/ :
Fichier	Description
community_X.png	Graphique des pays par communauté
country_summary.json	Données complètes
country_summary.txt	Résumé lisible
openalex_cache.json	Cache des requêtes API



💡 Remarques bonus
Un cache est utilisé pour éviter les appels API répétés
Une petite pause (sleep) évite le rate limiting
Les auteurs non trouvés → "UNKNOWN"
Remarques générales
Traitement DBLP en streaming (pas de relecture complète)
Mémoire recommandée : 3–4 Go
Dataset DBLP :
https://drops.dagstuhl.de/storage/artifacts/dblp/xml/2026/dblp-2026-01-01.xml.gz




Résumé des algorithmes
Tâche	Algorithme	Complexité
Tâche 1	Union-Find	O(α(n))
Tâche 2 (online)	HashMap	O(1)
SCC	Kosaraju	O(V + E)
Diamètre	BFS	O(
Utilisation de l'IA générative
Certaines parties du projet ont été assistées par Claude (Anthropic) pour :
la structure initiale
le débogage
Le code final a été relu, compris et peut être expliqué par les auteurs.