"""Reassemble the signed release APK from small transport parts and verify its SHA-256."""
from pathlib import Path
import base64
import hashlib

root = Path(__file__).resolve().parent.parent
parts = sorted((root / 'distribution/apk-1.6.0').rglob('part-*.b64'), key=lambda p: p.name)
expected_names = [f'part-{i:02d}.b64' for i in range(19)]
if [p.name for p in parts] != expected_names:
    raise SystemExit('Missing or duplicate APK parts')
encoded = b''.join(p.read_bytes() for p in parts)
apk = base64.b64decode(encoded, validate=True)
expected = '2a56321cbcaa8e7bc354b3771341097f114d1c94fb026dd9fdebf685db7b6cf0'
if hashlib.sha256(apk).hexdigest() != expected:
    raise SystemExit('APK checksum does not match the verified build')
target = root / 'distribution/Open-Music-1.6.0.apk'
temporary = target.with_suffix('.tmp')
temporary.write_bytes(apk)
temporary.replace(target)
print(f'Verified {target.name}: {len(apk)} bytes, SHA-256 {expected}')
