"""Check registered item icons, legacy language keys and JSON render inputs in a built JAR.

Run after gradlew build. This validates resource wiring, not in-game appearance.
"""
import collections
import json
import pathlib
import re
import struct
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/legacy/java/com/github/tartaricacid/touhoulittlemaid'
JAR = ROOT / 'build/libs/TouhouLittleMaidMod-1.7.10-0.1.0-port.jar'
ASSETS = 'assets/touhou_little_maid/'
errors = []


def check(condition, message):
    if not condition:
        errors.append(message)


with zipfile.ZipFile(JAR) as jar:
    names = set(jar.namelist())
    check(len(names) == len(jar.namelist()), 'Duplicate JAR entries')

    def png(path):
        check(path in names, f'Missing texture: {path}')
        if path not in names:
            return
        data = jar.read(path)
        check(data[:8] == b'\x89PNG\r\n\x1a\n', f'Invalid PNG: {path}')
        width, height = struct.unpack('>II', data[16:24])
        check(width > 0 and height > 0, f'Empty PNG: {path}')
        if '/textures/items/' in path and height != width:
            check(path + '.mcmeta' in names, f'Missing animation metadata: {path}')
            if path + '.mcmeta' in names:
                animation = json.loads(jar.read(path + '.mcmeta'))['animation']
                check(height % width == 0, f'Invalid frame strip: {path}')
                check(animation.get('frametime', 1) > 0, f'Invalid frame time: {path}')

    registry = (JAVA / 'init/ModItems.java').read_text(encoding='utf-8')
    items = re.findall(r'GameRegistry.registerItem\(\w+,\s*"([^"]+)"', registry)
    icon_aliases = {'chair': 'chair_show', 'smart_slab_init': 'smart_slab_has_maid'}
    # Wine Fox uses the vanilla painting icon, as in its legacy registration.
    for name in items:
        if name != 'wine_fox_painting':
            png(ASSETS + 'textures/items/' + icon_aliases.get(name, name) + '.png')

    renderer = (JAVA / 'client/renderer/LegacyItemRenderer.java').read_text(encoding='utf-8')
    sprites = re.findall(r'sprite\(ModBlocks\.\w+, "([^"]+)"\)', renderer)
    models = re.findall(r'add\([^;\n]+, "([^"]+)", "([^"]+)"\)', renderer)
    face_count = 0
    for icon in sprites + [icon for icon, _ in models]:
        png(ASSETS + 'textures/items/' + icon + '.png')
    for _, name in models:
        model = json.loads(jar.read(ASSETS + 'models/item/' + name + '.json'))
        check(bool(model.get('elements')), f'Empty item model: {name}')
        for element in model['elements']:
            for face in element['faces'].values():
                face_count += 1
                check(len(face.get('uv', [])) == 4, f'Explicit UV required: {name}')
                check(face.get('rotation', 0) in (0, 90, 180, 270), f'Invalid UV rotation: {name}')
                texture = face['texture']
                seen = set()
                while texture.startswith('#'):
                    check(texture not in seen, f'Cyclic texture reference: {name}')
                    if texture in seen:
                        break
                    seen.add(texture)
                    texture = model['textures'][texture[1:]]
                if ':' in texture:
                    domain, path = texture.split(':', 1)
                    png(f'assets/{domain}/textures/{path}.png')

    languages = {}
    for path in sorted(n for n in names if n.startswith(ASSETS + 'lang/') and n.endswith('.lang')):
        pairs = [line.split('=', 1) for line in jar.read(path).decode('utf-8-sig').splitlines()
                 if '=' in line and not line.startswith('#')]
        counts = collections.Counter(k for k, _ in pairs)
        check(all(n == 1 for n in counts.values()), f'Duplicate language keys: {path}')
        check('\ufffd' not in jar.read(path).decode('utf-8'), f'Broken Unicode: {path}')
        languages[pathlib.PurePosixPath(path).stem] = dict(pairs)

    required = {'item.touhou_little_maid.' + ('smart_slab' if n.startswith('smart_slab_') else n) + '.name' for n in items}
    blocks = (JAVA / 'init/ModBlocks.java').read_text(encoding='utf-8')
    block_ids = re.findall(r'(?:register\(\w+,|GameRegistry.registerBlock\(\w+,\s*\w+\.class,)\s*"([^"]+)"', blocks)
    required.update('tile.touhou_little_maid.' + n + '.name' for n in block_ids if n != 'board_proxy')
    for file in JAVA.rglob('*.java'):
        required.update(k for k in re.findall(r'(?:translateToLocal(?:Formatted)?|I18n\.format|ChatComponentTranslation)\(\s*"([^"]+)"', file.read_text(encoding='utf-8'))
                        if 'touhou_little_maid' in k and not k.endswith('.'))
    tasks = (JAVA / 'entity/task/TaskManager.java').read_text(encoding='utf-8')
    required.update('task.touhou_little_maid.' + n for n in re.findall(r'"touhou_little_maid:([^"]+)"', tasks))
    required.update('message.touhou_little_maid.' + n for n in (
        'bell_unbound', 'bell_unloaded', 'owned_maids', 'owned_tombstones',
        'wireless_export', 'wireless_import', 'point.Work', 'point.Idle', 'point.Sleep'))
    for locale in ('en_US', 'ru_RU'):
        for key in sorted(required):
            check(key in languages[locale], f'Missing {locale} translation: {key}')
    for key in languages['ru_RU'].keys() & languages['en_US'].keys():
        # Detect dropped or added substitutions in translated runtime messages.
        if key.startswith('message.touhou_little_maid.'):
            pattern = r'%(?:\d+\$)?[sd]'
            check(len(re.findall(pattern, languages['en_US'][key])) == len(re.findall(pattern, languages['ru_RU'][key])), f'Placeholder mismatch: {key}')

print(json.dumps({'items': len(items), 'sprite_renderers': len(sprites), 'json_models': len(models),
                  'model_faces': face_count, 'languages': len(languages), 'required_keys': len(required),
                  'errors': errors}, ensure_ascii=True, indent=2))
raise SystemExit(bool(errors))
