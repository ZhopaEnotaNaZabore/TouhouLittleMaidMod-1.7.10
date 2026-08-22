package com.github.tartaricacid.touhoulittlemaid.tileentity;

public final class TileEntitySnackCabinet extends TileEntityInventory {
    // S2DPacketOpenWindow in 1.7.10 limits inventory names to 32 characters.
    public TileEntitySnackCabinet() { super(27, "container.tlm.snack_cabinet"); }
}
