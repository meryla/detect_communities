import os
import re
import json
import time
import requests
from collections import Counter
import matplotlib.pyplot as plt

# =========================
# CONFIG
# =========================

# Read the OpenAlex API key from the environment variables.
# This avoids hardcoding the key directly in the script.
API_KEY = os.getenv("OPENALEX_API_KEY")

# If the API key is missing, stop the program immediately.
if not API_KEY:
    raise ValueError("OPENALEX_API_KEY is not set in the environment.")

# Input file that contains community information
INPUT_FILE = "output/task2_top10.txt"

# File used to store cached author-country results
# so the program does not call the API again for the same author
CACHE_FILE = "output/openalex_cache.json"

# Folder where output charts and summary files will be saved
OUTPUT_DIR = "output/country_stats"

# Base URL for the OpenAlex authors API
BASE_URL = "https://api.openalex.org/authors"

# =========================
# CACHE FUNCTIONS
# =========================

def load_cache():
    """
    Load the cache file if it exists.

    The cache is a dictionary like:
    {
        "Author Name": "US",
        "Another Author": "GB"
    }

    This helps avoid repeating API requests for authors
    we have already searched before.
    """
    if os.path.exists(CACHE_FILE):
        with open(CACHE_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    return {}

def save_cache(cache):
    """
    Save the cache dictionary into a JSON file.

    This keeps the results for future runs of the script.
    """
    with open(CACHE_FILE, "w", encoding="utf-8") as f:
        json.dump(cache, f, indent=2, ensure_ascii=False)

# =========================
# HELPERS
# =========================

def clean_author_name(name):
    """
    Remove DBLP numeric suffixes like:
    'John Smith 0001' -> 'John Smith'

    The regex means:
    \\s+      = one or more spaces
    \\d{4}    = exactly 4 digits
    $         = only if it appears at the end of the string
    """
    return re.sub(r"\s+\d{4}$", "", name).strip()

# =========================
# PARSE task2_top10.txt
# =========================

def parse_task2_file(filepath):
    """
    Read the task2_top10.txt file and extract community information.

    Expected format example:

    ## Community 1
    Size : 30
    Diameter : 5
    Members  :
      Alice
      Bob
      Carol

    This function returns a list like:
    [
        {
            "community_id": 1,
            "size": 30,
            "diameter": 5,
            "authors": ["Alice", "Bob", "Carol"]
        },
        ...
    ]
    """
    communities = []              # final list of all communities
    current_community = None      # stores the community currently being read
    reading_members = False       # tells us whether we are inside the "Members" section

    with open(filepath, "r", encoding="utf-8") as f:
        for raw_line in f:
            # Remove only the newline character at the end
            line = raw_line.rstrip("\n")

            # Check if the line starts a new community, for example:
            # "## Community 3"
            match = re.match(r"## Community (\d+)", line)
            if match:
                # If we were already building a previous community,
                # save it before starting the new one
                if current_community is not None:
                    communities.append(current_community)

                # Start a new community dictionary
                current_community = {
                    "community_id": int(match.group(1)),  # extracted number
                    "size": 0,
                    "diameter": 0,
                    "authors": []
                }

                # We are not yet reading members until "Members :" appears
                reading_members = False
                continue

            # If no community has started yet, skip lines
            if current_community is None:
                continue

            # Try to match a line like: "Size : 42"
            match = re.match(r"Size\s*:\s*(\d+)", line.strip())
            if match:
                current_community["size"] = int(match.group(1))
                continue

            # Try to match a line like: "Diameter : 7"
            match = re.match(r"Diameter\s*:\s*(\d+)", line.strip())
            if match:
                current_community["diameter"] = int(match.group(1))
                continue

            # Detect the start of the member list
            if line.strip() == "Members  :":
                reading_members = True
                continue

            # If we are currently reading members, each non-empty line
            # is treated as an author name
            if reading_members:
                stripped = line.strip()
                if stripped:
                    current_community["authors"].append(stripped)

    # After finishing the file, do not forget to save the last community
    if current_community is not None:
        communities.append(current_community)

    return communities

# =========================
# OPENALEX QUERY
# =========================

def get_author_country(author_name, cache):
    """
    Search OpenAlex for an author and try to get their country
    from the first last_known_institution.

    Returns a country code such as:
    'US', 'GB', 'DE', ...
    or 'UNKNOWN' if not found.
    """
    # Clean the name first so searches are more accurate
    clean_name = clean_author_name(author_name)

    # If this author was already searched before, return cached result
    if clean_name in cache:
        return cache[clean_name]

    # Parameters sent to the OpenAlex API
    params = {
        "search": clean_name,                          # author name to search
        "api_key": API_KEY,                           # API key
        "per_page": 5,                                # get up to 5 results
        "select": "display_name,last_known_institutions"  # only ask for needed fields
    }

    max_retries = 3  # retry up to 3 times if request fails

    for attempt in range(max_retries):
        try:
            # Send the request to OpenAlex
            # timeout=(5,20) means:
            #   wait max 5 seconds to connect
            #   wait max 20 seconds for response data
            response = requests.get(BASE_URL, params=params, timeout=(5, 20))

            # If the API key is wrong, OpenAlex may return 401
            if response.status_code == 401:
                raise RuntimeError("401 Unauthorized: invalid OpenAlex API key.")

            # Raise an exception for other HTTP errors (404, 500, etc.)
            response.raise_for_status()

            # Convert the JSON response into a Python dictionary
            data = response.json()

            # OpenAlex stores results inside the "results" list
            results = data.get("results", [])

            # If no matching authors are found
            if not results:
                cache[clean_name] = "UNKNOWN"
                return "UNKNOWN"

            # Take the first result as the best guess
            author = results[0]

            # Get the author's known institutions
            institutions = author.get("last_known_institutions", [])

            # If institution data exists, use the country code from the first institution
            if institutions:
                country = institutions[0].get("country_code", "UNKNOWN")
            else:
                country = "UNKNOWN"

            # Save result in cache
            cache[clean_name] = country

            # Small sleep to avoid hitting the API too fast
            time.sleep(0.15)

            return country

        except requests.exceptions.RequestException as e:
            # If network/API request fails, print message and retry
            print(f"Attempt {attempt + 1}/{max_retries} failed for '{clean_name}': {e}")
            time.sleep(2)

    # If all retries fail, store UNKNOWN in cache
    cache[clean_name] = "UNKNOWN"
    return "UNKNOWN"

# =========================
# ANALYSIS
# =========================

def analyze_communities():
    """
    Main workflow:
    1. Create output folder
    2. Read community data from file
    3. Load cache
    4. For each author in each community, find country
    5. Count countries
    6. Save charts and summary files
    """
    # Make sure output folder exists
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # Read communities from input file
    communities = parse_task2_file(INPUT_FILE)

    # Load previously saved author-country results
    cache = load_cache()

    # This will store overall summary data for every community
    summary = []

    print(f"Found {len(communities)} communities in {INPUT_FILE}")

    # Process each community one by one
    for community in communities:
        community_id = community["community_id"]
        authors = community["authors"]

        print(f"\nProcessing Community {community_id} ({len(authors)} authors)")

        countries = []

        # Look up each author's country
        for i, author in enumerate(authors, start=1):
            country = get_author_country(author, cache)
            countries.append(country)

            # Print progress every 20 authors, or at the end
            if i % 20 == 0 or i == len(authors):
                print(f"  {i}/{len(authors)} authors processed")

        # Count how many authors belong to each country
        # Example: Counter({'US': 10, 'GB': 5, 'UNKNOWN': 2})
        counts = Counter(countries)

        # Total number of authors in this community
        total = len(authors)

        # Convert counts into percentages
        # Example: if US = 10 and total = 20, percentage = 50.0
        proportions = {
            country: round((count / total) * 100, 2)
            for country, count in counts.items()
        }

        # Save community summary into list
        summary.append({
            "community_id": community_id,
            "size": community["size"],
            "diameter": community["diameter"],
            "total_authors_found": total,
            "country_counts": dict(counts),
            "country_proportions_percent": proportions
        })

        # Sort countries by number of authors descending
        sorted_items = sorted(counts.items(), key=lambda x: x[1], reverse=True)

        # Separate into labels and values for plotting
        labels = [item[0] for item in sorted_items]
        values = [item[1] for item in sorted_items]

        # Create bar chart
        plt.figure(figsize=(10, 6))
        plt.bar(labels, values)
        plt.title(f"Community {community_id} - Authors by Country")
        plt.xlabel("Country")
        plt.ylabel("Number of Authors")
        plt.xticks(rotation=45)
        plt.tight_layout()

        # Save chart as PNG
        plt.savefig(f"{OUTPUT_DIR}/community_{community_id}_countries.png")
        plt.close()

        # Save cache after each community so progress is not lost
        save_cache(cache)

    # Save full summary as JSON
    with open(f"{OUTPUT_DIR}/country_summary.json", "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2, ensure_ascii=False)

    # Save a readable text summary
    with open(f"{OUTPUT_DIR}/country_summary.txt", "w", encoding="utf-8") as f:
        for item in summary:
            f.write(f"Community {item['community_id']}\n")
            f.write(f"Size: {item['size']}\n")
            f.write(f"Diameter: {item['diameter']}\n")
            f.write("Country proportions:\n")

            # Sort percentages from highest to lowest
            sorted_props = sorted(
                item["country_proportions_percent"].items(),
                key=lambda x: x[1],
                reverse=True
            )

            for country, pct in sorted_props:
                f.write(f"  {country}: {pct}%\n")
            f.write("\n")

    print("\nDone.")
    print(f"Results saved in: {OUTPUT_DIR}")

# =========================
# MAIN
# =========================

# This makes sure the script only runs automatically
# when executed directly, not when imported as a module
if __name__ == "__main__":
    analyze_communities()