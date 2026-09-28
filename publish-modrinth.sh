#!/bin/bash
# Uploads build/brainstem-<version>.jar (version from resources/fabric.mod.json) as a new version of an existing Modrinth project.
# Usage: MODRINTH_TOKEN=... ./publish-modrinth.sh <project-id-or-slug>
set -euo pipefail
cd "$(dirname "$0")"
: "${MODRINTH_TOKEN:?set MODRINTH_TOKEN to your Modrinth PAT (needs the CREATE_VERSION scope)}"
PROJECT="${1:?usage: $0 <project-id-or-slug>}"
VERSION=$(python3 -c 'import json;print(json.load(open("resources/fabric.mod.json"))["version"])')
JAR=build/brainstem-$VERSION.jar
CHANGELOG_FILE="changelog/$VERSION.md"
API=https://api.modrinth.com/v2
UA="brainstem-publish/$VERSION"

PROJECT_ID=$(curl -fsS -A "$UA" -H "Authorization: $MODRINTH_TOKEN" "$API/project/$PROJECT" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')

DATA=$(python3 - "$PROJECT_ID" "$VERSION" "$CHANGELOG_FILE" <<'EOF'
import json, sys
print(json.dumps({
    "project_id": sys.argv[1],
    "name": "Brainstem " + sys.argv[2],
    "version_number": sys.argv[2],
    "changelog": open(sys.argv[3]).read(),
    "dependencies": [],
    "game_versions": ["26.3"],
    "version_type": "beta",
    "loaders": ["fabric"],
    "featured": True,
    "status": "listed",
    "file_parts": ["jar"],
    "primary_file": "jar",
}))
EOF
)

curl -fsS -A "$UA" -H "Authorization: $MODRINTH_TOKEN" \
  -F "data=$DATA" \
  -F "jar=@$JAR;type=application/java-archive;filename=brainstem-$VERSION.jar" \
  "$API/version" | python3 -c 'import json,sys; v=json.load(sys.stdin); print("published version", v["id"], v["version_number"])'
