#!/usr/bin/env sh
set -eu

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <preset-name>" >&2
  exit 1
fi

PRESET="$1"

case "$PRESET" in
  ""|*/*|*..*)
    echo "Invalid preset name: $PRESET" >&2
    exit 1
    ;;
esac

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
ROOT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/.." && pwd)
PRESET_DIR="$ROOT_DIR/domain-presets/$PRESET"

if [ ! -d "$PRESET_DIR" ]; then
  echo "Preset not found: $PRESET" >&2
  echo "Available presets:" >&2
  find "$ROOT_DIR/domain-presets" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort >&2
  exit 1
fi

require_file() {
  if [ ! -f "$1" ]; then
    echo "Missing preset file: $1" >&2
    exit 1
  fi
}

require_file "$PRESET_DIR/backend/DomainProfileProvider.java"
require_file "$PRESET_DIR/backend/DataInitializer.java"
require_file "$PRESET_DIR/backend/DefaultDomainAlgorithm.java"
require_file "$PRESET_DIR/frontend/domain.config.ts"

BACKUP_DIR="$ROOT_DIR/.preset-backups/$(date +%Y%m%d-%H%M%S)"
mkdir -p "$BACKUP_DIR/backend" "$BACKUP_DIR/frontend"

cp "$ROOT_DIR/backend/src/main/java/pl/zpo/app/domain/config/DomainProfileProvider.java" "$BACKUP_DIR/backend/DomainProfileProvider.java"
cp "$ROOT_DIR/backend/src/main/java/pl/zpo/app/config/DataInitializer.java" "$BACKUP_DIR/backend/DataInitializer.java"
cp "$ROOT_DIR/backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java" "$BACKUP_DIR/backend/DefaultDomainAlgorithm.java"
cp "$ROOT_DIR/frontend/src/config/domain.config.ts" "$BACKUP_DIR/frontend/domain.config.ts"

cp "$PRESET_DIR/backend/DomainProfileProvider.java" "$ROOT_DIR/backend/src/main/java/pl/zpo/app/domain/config/DomainProfileProvider.java"
cp "$PRESET_DIR/backend/DataInitializer.java" "$ROOT_DIR/backend/src/main/java/pl/zpo/app/config/DataInitializer.java"
cp "$PRESET_DIR/backend/DefaultDomainAlgorithm.java" "$ROOT_DIR/backend/src/main/java/pl/zpo/app/domain/algorithm/DefaultDomainAlgorithm.java"
cp "$PRESET_DIR/frontend/domain.config.ts" "$ROOT_DIR/frontend/src/config/domain.config.ts"

echo "Applied preset: $PRESET"
echo "Backup saved in: $BACKUP_DIR"
echo "Next:"
echo "  cd backend && ./mvnw test"
echo "  cd frontend && npm run build"
