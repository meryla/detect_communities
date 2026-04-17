# 📘 README.md — Projet Algorithmique 2 (DBLP Communities)

## 📁 Structure du projet

Le projet est organisé de la manière suivante :

```
project/
├── src/                    # Code source Java
│   ├── task1/             # Implémentation de la tâche 1 (Union-Find)
│   ├── task2/             # Implémentation de la tâche 2 (graphe orienté, SCC)
│   └── utils/             # Fonctions utilitaires (extraction des auteurs, etc.)
│
├── data/                  # Fichiers d’entrée
│   ├── dblp.xml.gz
│   └── dblp.dtd
│
├── output/                # Fichiers générés par le programme
│   ├── task1_histogram.txt
│   └── task2_results.txt
│
├── scripts/               # Scripts éventuels (compilation, exécution)
│
└── README.md              # Documentation du projet
```

---

## ⚙️ Compilation du projet

Le projet est écrit en Java.

### Avec `javac` :

```bash
javac -d bin src/**/*.java
```

### (Optionnel) Avec un script :

```bash
bash scripts/compile.sh
```

---

## ▶️ Exécution du programme

### Tâche 1 — Communautés (Union-Find)

```bash
java -cp bin task1.Main data/dblp.xml.gz
```

### Tâche 2 — Graphe orienté + SCC

```bash
java -cp bin task2.Main data/dblp.xml.gz
```

### Arguments attendus :

* Chemin vers le fichier `dblp.xml.gz`
* (Optionnel) fréquence d’affichage des statistiques (ex: toutes les N publications)

Exemple :

```bash
java -cp bin task1.Main data/dblp.xml.gz 100000
```

---

## 📊 Fichiers de sortie

Les résultats sont écrits dans le dossier `output/`.

### Tâche 1 :

* `output/task1_histogram.txt`
  → Histogramme des tailles des communautés

---

### Tâche 2 :

* `output/task2_results.txt`
  → Contient :

  * nombre de composantes fortement connexes
  * top 10 des plus grandes communautés
  * diamètre des plus grandes composantes

---

## 📝 Remarques

* Le traitement du fichier DBLP est réalisé en **mode streaming** pour éviter les problèmes de mémoire.
* Le projet a été testé sur un dataset complet (plusieurs millions d’auteurs).
* Une option JVM peut être nécessaire pour augmenter la mémoire lors de l’exécution :

```bash
java -Xmx4G -cp bin task1.Main data/dblp.xml.gz
```

---

## 🧾 Résumé

* **Tâche 1** : détection des communautés avec Union-Find (graphe non orienté)
* **Tâche 2** : graphe orienté filtré + calcul des SCC + diamètre

---
