#!/usr/bin/env bash
# generate-site.sh — builds docs/index.html from repo structure

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCS="$REPO_ROOT/docs"
OUTPUT="$DOCS/index.html"

mkdir -p "$DOCS"

REPO_NAME=$(basename "$REPO_ROOT")
GITHUB_USER="${GITHUB_REPOSITORY_OWNER:-vbforge}"
GITHUB_REPO="${GITHUB_REPOSITORY:-$GITHUB_USER/$REPO_NAME}"
UPDATED=$(date -u '+%Y-%m-%d %H:%M UTC')

CATEGORIES=(java-core spring-boot concurrency cloud docker database kafka)

# ── Gather project data ─────────────────────────────────────────────────────────
declare -A CAT_COUNTS
declare -A CAT_PROJECTS
TOTAL=0

for cat in "${CATEGORIES[@]}"; do
  dir="$REPO_ROOT/$cat"
  if [[ -d "$dir" ]]; then
    projs=()
    while IFS= read -r -d '' p; do
      projs+=("$(basename "$p")")
    done < <(find "$dir" -mindepth 1 -maxdepth 1 -type d -print0 | sort -z)
    CAT_COUNTS[$cat]=${#projs[@]}
    CAT_PROJECTS[$cat]="${projs[*]:-}"
    TOTAL=$((TOTAL + ${#projs[@]}))
  else
    CAT_COUNTS[$cat]=0
    CAT_PROJECTS[$cat]=""
  fi
done

# ── Build JS arrays for chart ──────────────────────────────────────────────────
CHART_LABELS="["
CHART_DATA="["
for cat in "${CATEGORIES[@]}"; do
  CHART_LABELS+="\"$cat\","
  CHART_DATA+="${CAT_COUNTS[$cat]},"
done
CHART_LABELS="${CHART_LABELS%,}]"
CHART_DATA="${CHART_DATA%,}]"

# ── Build project cards HTML ───────────────────────────────────────────────────
build_cards() {
  for cat in "${CATEGORIES[@]}"; do
    local count="${CAT_COUNTS[$cat]}"
    local projs_str="${CAT_PROJECTS[$cat]}"

    local items_html=""
    if [[ -n "$projs_str" ]]; then
      for proj in $projs_str; do
        items_html+="<li><a href=\"https://github.com/$GITHUB_REPO/tree/main/$cat/$proj\" target=\"_blank\">$proj</a></li>"
      done
    else
      items_html="<li class=\"empty\">No projects yet</li>"
    fi

    cat << CARD
<div class="card" data-category="$cat">
  <div class="card-header">
    <span class="card-title">$cat</span>
    <span class="card-badge">$count</span>
  </div>
  <ul class="project-list">
    $items_html
  </ul>
</div>
CARD
  done
}

CARDS_HTML=$(build_cards)

# ── Logo ───────────────────────────────────────────────────────────────────────
if [[ -f "$DOCS/logo.svg" ]]; then
  LOGO_HTML='<img src="logo.svg" alt="logo" class="logo-img" />'
else
  LOGO_HTML='<span class="logo-icon">⬡</span>'
fi

# ── Write index.html ────────────────────────────────────────────────────────────
cat > "$OUTPUT" << HTML
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>$REPO_NAME — Learning Sandbox</title>
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;700&family=Syne:wght@400;700;800&family=Orbitron:wght@700;900&display=swap" rel="stylesheet" />
  <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
  <link rel="stylesheet" href="style.css" />
</head>
<body>

  <header>
    <div class="header-inner">
      <div class="logo-block">
        $LOGO_HTML
        <div>
          <h1>$REPO_NAME</h1>
          <p class="tagline">Java Learning Sandbox</p>
        </div>
      </div>
      <nav class="header-links">
        <a href="https://github.com/$GITHUB_REPO" target="_blank">GitHub ↗</a>
        <a href="https://github.com/$GITHUB_REPO/actions" target="_blank">Actions ↗</a>
      </nav>
    </div>
  </header>

  <main>

    <section class="stats-row">
      <div class="stat-pill">
        <span class="stat-number" id="total-count">$TOTAL</span>
        <span class="stat-label">Total Projects</span>
      </div>
      <div class="stat-pill">
        <span class="stat-number">${#CATEGORIES[@]}</span>
        <span class="stat-label">Categories</span>
      </div>
      <div class="stat-pill">
        <span class="stat-number">2</span>
        <span class="stat-label">CI Workflows</span>
      </div>
      <div class="stat-pill updated-pill">
        <span class="stat-label">Last updated</span>
        <span class="stat-number small">$UPDATED</span>
      </div>
    </section>

    <section class="chart-section">
      <h2 class="section-title">Projects by Category</h2>
      <div class="chart-wrap">
        <canvas id="barChart"></canvas>
      </div>
    </section>

    <section class="cards-section">
      <h2 class="section-title">Repository Explorer</h2>
      <div class="cards-grid">
        $CARDS_HTML
      </div>
    </section>

    <section class="pipeline-section">
      <h2 class="section-title">Automation Pipeline</h2>
      <div class="pipeline">
        <div class="pipe-step">
          <div class="pipe-icon">⬡</div>
          <div class="pipe-label">git push</div>
        </div>
        <div class="pipe-arrow">→</div>
        <div class="pipe-step">
          <div class="pipe-icon">🔨</div>
          <div class="pipe-label">build.yml<br/><small>Maven compile</small></div>
        </div>
        <div class="pipe-arrow">→</div>
        <div class="pipe-step">
          <div class="pipe-icon">📝🌐</div>
          <div class="pipe-label">update-docs.yml<br/><small>README + Pages</small></div>
        </div>
      </div>
    </section>

  </main>

  <footer>
    <p>Auto-generated · <a href="https://github.com/$GITHUB_REPO" target="_blank">$GITHUB_REPO</a></p>
  </footer>

  <script src="script.js"></script>
  <script>
    const ctx = document.getElementById('barChart').getContext('2d');
    new Chart(ctx, {
      type: 'bar',
      data: {
        labels: $CHART_LABELS,
        datasets: [{
          label: 'Projects',
          data: $CHART_DATA,
          backgroundColor: ['#e8ff47','#47ffe8','#ff6b47','#b847ff','#47b8ff','#ff47a0','#ffa047'],
          borderWidth: 0,
          borderRadius: 6,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: { callbacks: { label: ctx => \` \${ctx.parsed.y} project\${ctx.parsed.y !== 1 ? 's' : ''}\` } }
        },
        scales: {
          x: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#aaa', font: { family: 'JetBrains Mono', size: 12 } } },
          y: { grid: { color: 'rgba(255,255,255,0.05)' }, ticks: { color: '#aaa', font: { family: 'JetBrains Mono', size: 12 }, stepSize: 1 }, beginAtZero: true }
        }
      }
    });
  </script>
</body>
</html>
HTML

echo "✔ docs/index.html generated"
