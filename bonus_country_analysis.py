import os
import re
import json
import time
import requests
from collections import Counter
import matplotlib.pyplot as plt

INPUT_FILE = "output/task2_top10.txt"
CACHE_FILE = "output/openalex_cache.json"
OUTPUT_DIR = "output/country_stats"
BASE_URL   = "https://api.openalex.org/authors"


def load_cache():
    if os.path.exists(CACHE_FILE):
        with open(CACHE_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    return {}


def save_cache(cache):
    with open(CACHE_FILE, "w", encoding="utf-8") as f:
        json.dump(cache, f, indent=2, ensure_ascii=False)


def clean_author_name(name):
    """Remove DBLP numeric suffixes: 'John Smith 0001' -> 'John Smith'."""
    return re.sub(r"\s+\d{4}$", "", name).strip()


def parse_task2_file(filepath):
    communities     = []
    current         = None
    reading_members = False

    with open(filepath, "r", encoding="utf-8") as f:
        for raw_line in f:
            line = raw_line.rstrip("\n")

            match = re.match(r"## Community (\d+)", line)
            if match:
                if current is not None:
                    communities.append(current)
                current = {"community_id": int(match.group(1)), "size": 0, "diameter": 0, "authors": []}
                reading_members = False
                continue

            if current is None:
                continue

            match = re.match(r"Size\s*:\s*(\d+)", line.strip())
            if match:
                current["size"] = int(match.group(1))
                continue

            match = re.match(r"Diameter\s*:\s*(\d+)", line.strip())
            if match:
                current["diameter"] = int(match.group(1))
                continue

            if line.strip() == "Members  :":
                reading_members = True
                continue

            if reading_members and line.strip():
                current["authors"].append(line.strip())

    if current is not None:
        communities.append(current)

    return communities


def get_author_country(author_name, cache):
    clean_name = clean_author_name(author_name)

    if clean_name in cache:
        return cache[clean_name]

    params = {
        "search":  clean_name,
        "per_page": 5,
        "select":  "display_name,last_known_institution"
    }

    for attempt in range(3):
        try:
            response = requests.get(BASE_URL, params=params, timeout=(5, 20))
            response.raise_for_status()
            results = response.json().get("results", [])

            if not results:
                cache[clean_name] = "UNKNOWN"
                return "UNKNOWN"

            institution = results[0].get("last_known_institution") or {}
            country = institution.get("country_code", "UNKNOWN")

            cache[clean_name] = country
            time.sleep(0.15)
            return country

        except requests.exceptions.RequestException as e:
            print(f"Attempt {attempt + 1}/3 failed for '{clean_name}': {e}")
            time.sleep(2)

    cache[clean_name] = "UNKNOWN"
    return "UNKNOWN"


def analyze_communities():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    communities = parse_task2_file(INPUT_FILE)
    cache       = load_cache()
    summary     = []

    print(f"Found {len(communities)} communities in {INPUT_FILE}")

    for community in communities:
        community_id = community["community_id"]
        authors      = community["authors"]

        print(f"\nProcessing Community {community_id} ({len(authors)} authors)")

        countries = []
        for i, author in enumerate(authors, start=1):
            countries.append(get_author_country(author, cache))
            if i % 20 == 0 or i == len(authors):
                print(f"  {i}/{len(authors)} authors processed")

        counts = Counter(countries)
        total  = len(authors)

        proportions = {
            country: round((count / total) * 100, 2)
            for country, count in counts.items()
        }

        summary.append({
            "community_id":                community_id,
            "size":                        community["size"],
            "diameter":                    community["diameter"],
            "total_authors_found":         total,
            "country_counts":              dict(counts),
            "country_proportions_percent": proportions
        })

        sorted_items = sorted(counts.items(), key=lambda x: x[1], reverse=True)
        labels = [item[0] for item in sorted_items]
        values = [item[1] for item in sorted_items]

        plt.figure(figsize=(10, 6))
        plt.bar(labels, values)
        plt.title(f"Communauté {community_id} — Auteurs par pays")
        plt.xlabel("Pays")
        plt.ylabel("Nombre d'auteurs")
        plt.xticks(rotation=45)
        plt.tight_layout()
        plt.savefig(f"{OUTPUT_DIR}/community_{community_id}_countries.png")
        plt.close()

        save_cache(cache)

    with open(f"{OUTPUT_DIR}/country_summary.json", "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2, ensure_ascii=False)

    with open(f"{OUTPUT_DIR}/country_summary.txt", "w", encoding="utf-8") as f:
        for item in summary:
            f.write(f"Community {item['community_id']}\n")
            f.write(f"Size: {item['size']}\n")
            f.write(f"Diameter: {item['diameter']}\n")
            f.write("Country proportions:\n")
            for country, pct in sorted(item["country_proportions_percent"].items(),
                                       key=lambda x: x[1], reverse=True):
                f.write(f"  {country}: {pct}%\n")
            f.write("\n")

    print(f"\nDone. Results saved in: {OUTPUT_DIR}")


if __name__ == "__main__":
    analyze_communities()