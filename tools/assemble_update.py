"""Reconstruct signed APK releases from verified APKs and BSDIFF40 patches.

Python standard library only. Default reconstructs 1.6.1. Pass --video to also
reconstruct 1.7.0. This transports release files; it is not an on-device updater.
"""
from pathlib import Path
import base64
import bz2
import hashlib
import json
import runpy
import sys

root = Path(__file__).resolve().parent.parent

def assemble(version, base):
    meta = json.loads((root / f'distribution/update-{version}.json').read_text())
    old = (root / f'distribution/Open-Music-{base}.apk').read_bytes()
    if hashlib.sha256(old).hexdigest() != meta['base_sha256']:
        raise SystemExit('Base APK checksum mismatch')
    parts = sorted((root / f'distribution/update-{version}').glob('part-*.b64'))
    if [p.name for p in parts] != [f'part-{i:02d}.b64' for i in range(meta['parts'])]:
        raise SystemExit('Missing or duplicate update parts')
    patch = base64.b64decode(b''.join(p.read_bytes() for p in parts), validate=True)
    if hashlib.sha256(patch).hexdigest() != meta['patch_sha256']:
        raise SystemExit('Patch checksum mismatch')

    def number(data):
        if len(data) != 8:
            raise ValueError('Truncated patch integer')
        value = int.from_bytes(data, 'little') & ((1 << 63) - 1)
        return -value if data[7] & 128 else value

    if patch[:8] != b'BSDIFF40':
        raise SystemExit('Invalid update format')
    control_len, diff_len, size = [number(patch[i:i+8]) for i in (8, 16, 24)]
    if min(control_len, diff_len) < 0 or size != meta['bytes'] or size > 150_000_000:
        raise SystemExit('Invalid update lengths')
    control = bz2.decompress(patch[32:32+control_len])
    diff = bz2.decompress(patch[32+control_len:32+control_len+diff_len])
    extra = bz2.decompress(patch[32+control_len+diff_len:])
    result = bytearray(size)
    old_pos = new_pos = diff_pos = extra_pos = control_pos = 0
    while new_pos < size:
        add, copy, seek = [number(control[control_pos+i:control_pos+i+8]) for i in (0, 8, 16)]
        control_pos += 24
        if min(add, copy) < 0 or new_pos+add+copy > size or diff_pos+add > len(diff) or extra_pos+copy > len(extra):
            raise SystemExit('Invalid update block')
        for i in range(add):
            previous = old[old_pos+i] if 0 <= old_pos+i < len(old) else 0
            result[new_pos+i] = (diff[diff_pos+i]+previous) & 255
        new_pos += add
        old_pos += add+seek
        diff_pos += add
        result[new_pos:new_pos+copy] = extra[extra_pos:extra_pos+copy]
        new_pos += copy
        extra_pos += copy
    if hashlib.sha256(result).hexdigest() != meta['sha256']:
        raise SystemExit('Reconstructed APK checksum mismatch')
    target = root / f'distribution/Open-Music-{version}.apk'
    temporary = target.with_suffix('.tmp')
    temporary.write_bytes(result)
    temporary.replace(target)
    print(f'Verified {target.name}: {len(result)} bytes')

runpy.run_path(str(root / 'tools/assemble_release.py'))
assemble('1.6.1', '1.6.0')
if '--video' in sys.argv:
    assemble('1.7.0', '1.6.1')
