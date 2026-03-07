#!/usr/bin/env bash
# generate-readme.sh — scans the repo and rebuilds README.md automatically

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT="$REPO_ROOT/README.md"

REPO_NAME=$(basename "$REPO_ROOT")
GITHUB_USER="${GITHUB_REPOSITORY_OWNER:-vbforge}"
GITHUB_REPO="${GITHUB_REPOSITORY:-$GITHUB_USER/$REPO_NAME}"
UPDATED=$(date -u '+%Y-%m-%d %H:%M UTC')

# ── Top-level project folders to scan ──────────────────────────────────────────
CATEGORIES=(java-core spring-boot concurrency cloud docker database kafka)

# ── Count total projects ────────────────────────────────────────────────────────
count_projects() {
  local total=0
  for cat in "${CATEGORIES[@]}"; do
    local dir="$REPO_ROOT/$cat"
    [[ -d "$dir" ]] || continue
    local n
    n=$(find "$dir" -mindepth 1 -maxdepth 1 -type d | wc -l)
    total=$((total + n))
  done
  echo "$total"
}

TOTAL_PROJECTS=$(count_projects)

# ── Build category sections ─────────────────────────────────────────────────────
build_category_sections() {
  for cat in "${CATEGORIES[@]}"; do
    local dir="$REPO_ROOT/$cat"
    [[ -d "$dir" ]] || continue

    local projects=()
    while IFS= read -r -d '' p; do
      projects+=("$(basename "$p")")
    done < <(find "$dir" -mindepth 1 -maxdepth 1 -type d -print0 | sort -z)

    [[ ${#projects[@]} -eq 0 ]] && continue

    echo ""
    echo "### 📁 \`$cat\`"
    echo ""
    echo "| Project | Description |"
    echo "|---------|-------------|"

    for proj in "${projects[@]}"; do
      local desc=""
      # Try to read description from README or pom.xml inside the project
      local proj_readme="$dir/$proj/README.md"
      local proj_pom="$dir/$proj/pom.xml"
      if [[ -f "$proj_readme" ]]; then
        desc=$(grep -m1 "^[A-Z]" "$proj_readme" 2>/dev/null | head -c 80 || true)
      elif [[ -f "$proj_pom" ]]; then
        desc=$(grep -m1 "<description>" "$proj_pom" 2>/dev/null \
          | sed 's|.*<description>\(.*\)</description>.*|\1|' | head -c 80 || true)
      fi
      [[ -z "$desc" ]] && desc="—"
      echo "| [\`$proj\`](./$cat/$proj) | $desc |"
    done
  done
}

# ── Write README ────────────────────────────────────────────────────────────────
cat > "$OUTPUT" << HEADER
# $REPO_NAME

> **Learning sandbox:** — $TOTAL_PROJECTS projects in repository

<!-- CI / Pages -->
[![Build](https://github.com/$GITHUB_REPO/actions/workflows/build.yml/badge.svg)](https://github.com/$GITHUB_REPO/actions/workflows/build.yml)
[![README](https://github.com/$GITHUB_REPO/actions/workflows/update-readme.yml/badge.svg)](https://github.com/$GITHUB_REPO/actions/workflows/update-readme.yml)
[![Site](https://github.com/$GITHUB_REPO/actions/workflows/generate-site.yml/badge.svg)](https://github.com/$GITHUB_REPO/actions/workflows/generate-site.yml)
[![Pages](https://img.shields.io/badge/GitHub%20Pages-live-brightgreen)](https://$GITHUB_USER.github.io/$REPO_NAME/)

<!-- Language & platform -->
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS-S3-FF9900?logo=amazons3&logoColor=white)

<!-- Testing -->
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-5.x-C5D9C8)

---

## 📚 Projects

HEADER

build_category_sections >> "$OUTPUT"

cat >> "$OUTPUT" << FOOTER

---

## 🔄 Automation

| Workflow | Trigger | Action |
|----------|---------|--------|
| \`build.yml\` | push / PR | Compiles all Maven projects |
| \`update-readme.yml\` | push | Regenerates this README |
| \`generate-site.yml\` | push | Rebuilds GitHub Pages site |

---

## 🌐 GitHub Pages

Visit the live site: **[https://$GITHUB_USER.github.io/$REPO_NAME/](https://$GITHUB_USER.github.io/$REPO_NAME/)**

---

*Last updated: $UPDATED — [source](.github/workflows/update-readme.yml)*
FOOTER

echo "✔ README.md generated ($TOTAL_PROJECTS projects)"
