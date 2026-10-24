"""Identify the application sources corresponding to a signed release."""
from pathlib import Path
import hashlib
import json
import sys
root = Path(__file__).resolve().parent.parent
paths = sorted([root / 'app/build.gradle', *[p for p in (root / 'app/src/main').rglob('*') if p.is_file()]])
digest = hashlib.sha256()
for path in paths:
    digest.update(path.relative_to(root).as_posix().encode() + b'\0')
    digest.update(hashlib.sha256(path.read_bytes()).digest())
value = digest.hexdigest()
if '--verify' in sys.argv:
    meta = json.loads((root / 'distribution/update-1.7.0.json').read_text())
    if meta.get('source_fingerprint') != value:
        raise SystemExit('Signed APK does not correspond to the current application sources')
print(value)
