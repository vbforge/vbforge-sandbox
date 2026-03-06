#!/bin/bash

OUTPUT="README.md"

echo "# VBForge Sandbox" > $OUTPUT
echo "" >> $OUTPUT
echo "Collection of experiments, demos, and investigations." >> $OUTPUT
echo "" >> $OUTPUT

echo "| Domain | Project | Link |" >> $OUTPUT
echo "|------|------|------|" >> $OUTPUT

for domain in */ ; do
  domain=${domain%/}

  if [[ "$domain" == ".github" || "$domain" == "scripts" ]]; then
      continue
  fi

  for project in $domain/* ; do
    if [ -d "$project" ]; then
      name=$(basename "$project")
      link="https://github.com/vbforge/vbforge-sandbox/tree/main/$project"
      echo "| $domain | $name | [Open]($link) |" >> $OUTPUT
    fi
  done
done