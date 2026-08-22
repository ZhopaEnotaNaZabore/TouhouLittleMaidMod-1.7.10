# Release audit

## Verified

- Java 8 / Forge 1.7.10 production JAR is reobfuscated.
- Modern Java sources are excluded except the two Minecraft-independent board engines.
- Nested custom-pack duplication is filtered; pack namespaces are exposed directly.
- AI secrets remain server-side and are never serialized into packets or maid NBT.
- Optional integrations use no hard class references.

## Bundled resource credits discovered from manifests

- Main model pack: Succinum, Pajinyi, Hoishi, ZeniCrow, Paulzzh, Tian_mi,
  CrystalizedSun and FumoLover.
- Old model pack: Hoishi, Succinum, Pajinyi, ZeniCrow and FumoLover.
- Seihou pack: TartaricAcid and CrystalizedSun.
- Minecraft 15th pack: CrystalizedSun.
- Authors and credits / Gecko collections: TartaricAcid and the authors named
  inside their individual manifests.
- Peco voice pack: the CV named in `littlemaid_peco/maid_sound.json` and Tamemaru;
  the manifest points to `https://booth.pm/ja/items/1903163`.

## Release blocker requiring project-owner confirmation

The supplied workspace contains no root `LICENSE`, `COPYING` or `NOTICE` file.
Compilation and private testing are unaffected, but redistribution of the code,
models, textures and Peco voice files must wait until their licenses/permissions
are confirmed and the required notices are added to the release artifact.
