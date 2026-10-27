#!/usr/bin/env bash
# Validación local y de CI. No toca la hoja real ni genera claves privadas.
set -euo pipefail
repo_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$repo_dir"
python_bin="${HOGAR_PYTHON:-python3}"
command -v "$python_bin" >/dev/null
command -v node >/dev/null
command -v soffice >/dev/null
"$python_bin" -c 'import openpyxl; assert openpyxl.__version__ == "3.1.5", "Se requiere openpyxl 3.1.5"'
"$python_bin" -m unittest discover -s recovered-source/tests -v
node --check recovered-source/android/assets/app.js
node --check < recovered-source/backend/Code.gs
if [[ -f Hogar_Playa_Recuperacion_Fuentes.zip ]]; then
  "$python_bin" recovered-source/scripts/create_backup.py --check Hogar_Playa_Recuperacion_Fuentes.zip
else
  # Una copia extraída del ZIP no contiene recursivamente el propio ZIP.
  backup_dir="$(mktemp -d "${TMPDIR:-/tmp}/hogar-verificacion.XXXXXX")"
  trap 'rm -rf -- "$backup_dir"' EXIT
  "$python_bin" recovered-source/scripts/create_backup.py --output "$backup_dir/respaldo.zip"
  "$python_bin" recovered-source/scripts/create_backup.py --check "$backup_dir/respaldo.zip"
fi
echo 'Comprobaciones completadas. La APK original se conserva. No se ha generado una actualización firmada.'
