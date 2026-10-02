"""Read-only inventory of reference/port and packaged resources; no parity claims."""
import collections
import hashlib
import json
import pathlib
import re
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
PKG = pathlib.Path('com/github/tartaricacid/touhoulittlemaid')
result = {'note': 'File counts and matching names do not establish behavioral parity.'}
for tree in ('main', 'legacy'):
    base = ROOT / 'src' / tree / 'java'
    files = sorted(base.rglob('*.java'))
    result[tree] = {'java_files': len(files), 'packages': dict(collections.Counter(
        p.relative_to(base / PKG).parts[0] for p in files if p.is_relative_to(base / PKG))),
        'files': [{'path': p.relative_to(ROOT).as_posix(),
                   'sha256': hashlib.sha256(p.read_bytes()).hexdigest()} for p in files]}

modern_items = (ROOT / 'src/main/java' / PKG / 'init/InitItems.java').read_text(encoding='utf-8')
legacy_items = (ROOT / 'src/legacy/java' / PKG / 'init/ModItems.java').read_text(encoding='utf-8')
legacy_blocks = (ROOT / 'src/legacy/java' / PKG / 'init/ModBlocks.java').read_text(encoding='utf-8')
modern_ids = set(re.findall(r'ITEMS\.register\("([^"]+)"', modern_items))
legacy_ids = set(re.findall(r'GameRegistry\.registerItem\([^,]+,\s*"([^"]+)"', legacy_items))
legacy_ids.update(re.findall(r'register\(\w+,\s*"([^"]+)"', legacy_blocks))
legacy_ids.update(re.findall(r'GameRegistry\.registerBlock\(\w+,\s*\w+\.class,\s*"([^"]+)"', legacy_blocks))
result['item_registry'] = {'main_count': len(modern_ids), 'legacy_count_with_blocks': len(legacy_ids),
                          'main_only_ids': sorted(modern_ids - legacy_ids),
                          'legacy_only_ids': sorted(legacy_ids - modern_ids)}

for variant in ('', '-dev'):
    jar = ROOT / f'build/libs/TouhouLittleMaidMod-1.7.10-0.1.0-port{variant}.jar'
    with zipfile.ZipFile(jar) as z:
        names = z.namelist()
        known = set(names)
        summary = {'entries': len(names), 'duplicate_names': [n for n, c in collections.Counter(names).items() if c > 1],
                   'engine_classes': sum(n.endswith('.class') and ('/api/game/chess/' in n or '/api/game/xqwlight/' in n) for n in names),
                   'missing_model_resources': [], 'missing_sound_files': []}
        if not variant:
            for name in names:
                if re.fullmatch(r'assets/[^/]+/maid_model\.json', name):
                    domain = name.split('/')[1]
                    pack = json.loads(z.read(name))
                    for entry in pack.get('model_list', []):
                        mid = entry['model_id']
                        md, mp = mid.split(':', 1) if ':' in mid else (domain, mid)
                        for key, default in [('model', f'{md}:models/entity/{mp}.json'), ('texture', f'{md}:textures/entity/{mp}.png')]:
                            value = entry.get(key, default)
                            vd, vp = value.split(':', 1) if ':' in value else (md, value)
                            if f'assets/{vd}/{vp}' not in known:
                                summary['missing_model_resources'].append({'id': mid, 'kind': key, 'resource': value})
                if re.fullmatch(r'assets/[^/]+/sounds\.json', name):
                    domain = name.split('/')[1]
                    for event, data in json.loads(z.read(name)).items():
                        for sound in data.get('sounds', []):
                            if isinstance(sound, dict) and sound.get('type') == 'event':
                                continue
                            value = sound if isinstance(sound, str) else sound['name']
                            sd, sp = value.split(':', 1) if ':' in value else (domain, value)
                            if sd != 'minecraft' and f'assets/{sd}/sounds/{sp}.ogg' not in known:
                                summary['missing_sound_files'].append({'event': f'{domain}:{event}', 'sound': value})
        result[f'jar{variant}'] = summary

output = ROOT / 'docs/PORT_AUDIT_INVENTORY.json'
output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({k: {a: b for a, b in v.items() if a != 'files'} if isinstance(v, dict) else v
                  for k, v in result.items()}, ensure_ascii=False, indent=2))
