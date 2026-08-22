# Forge 1.7.10 regression matrix

The post-init `LegacyPortSelfTest` rejects incomplete registries, duplicate
enchantment IDs and missing required resources on every client/server start.
The following world scenarios remain the acceptance matrix for release.

## Core and security

1. Tame up to the configured owner limit; verify another player cannot open,
   configure, photograph, call or restore the maid.
2. Save/reload and unload/reload a chunk with every inventory tab populated,
   a backpack equipped, baubles damaged, task data, favorability and AI history.
3. Follow across dimensions and force a long-distance teleport onto terrain
   containing liquids, fences and two-block-high obstacles.
4. Kill, resurrect and restore a backup; attempt each operation again while the
   same UUID is alive in another dimension and verify it is rejected.

## Professions

Run one controlled scenario for every registered task: idle, attack, ranged,
danmaku, crossbow, trident, farm, sugar cane, melon/pumpkin, cocoa, grass, snow,
feed owner, feed animal, shear, milk, torch, fishing, extinguishing and honey.
Verify item consumption/durability, target ownership and save/reload midway.

## Blocks, games and client

1. Save/reload all 15 tile entities with non-default state.
2. Finish gomoku, western chess and xiangqi games; copy/restore each Board State.
3. Reload resources while several maid model namespaces are visible.
4. Exercise main/bauble/equipment/task GUI tabs and malformed/distant packets.
5. Run `/tlmchat`, timeout/failure paths and bounded WAV TTS with Mute equipped.

## Performance target

Profile 20 active maids for 10 minutes. No unbounded thread, history, TTS cache,
backup or path-search growth is acceptable; average server tick must remain below
50 ms on the release reference machine.
