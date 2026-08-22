package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.tileentity.TileEntity;

/**
 * Loaded-world index entry for the lower half of a scarecrow.
 *
 * Modern Minecraft uses a POI entry for this purpose.  Keeping a tiny tile
 * entity in 1.7.10 avoids scanning every block in a 97 x 97 square for every
 * fairy spawn attempt.
 */
public final class TileEntityScarecrow extends TileEntity {
}
