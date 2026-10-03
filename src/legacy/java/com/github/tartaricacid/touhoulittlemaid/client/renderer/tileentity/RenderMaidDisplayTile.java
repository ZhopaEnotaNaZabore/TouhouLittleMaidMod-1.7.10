package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

/** Shared static maid preview used by statue and garage-kit tile renderers. */
final class RenderMaidDisplayTile {
    private static final Map<TileEntity, java.lang.ref.WeakReference<CachedMaid>> CACHE =
            new java.util.WeakHashMap<TileEntity, java.lang.ref.WeakReference<CachedMaid>>();

    private RenderMaidDisplayTile() { }

    static void render(TileEntity tile, NBTTagCompound data, double x, double y, double z,
                       int facing, float scale) {
        if (tile.getWorldObj() == null || data == null || data.hasNoTags()) return;
        String fingerprint = data.toString();
        java.lang.ref.WeakReference<CachedMaid> reference = CACHE.get(tile);
        CachedMaid cached = reference == null ? null : reference.get();
        if (cached == null || !cached.fingerprint.equals(fingerprint)) {
            EntityMaid maid = new EntityMaid(tile.getWorldObj());
            try {
                maid.readFromNBT((NBTTagCompound) data.copy());
            } catch (Exception ignored) {
                return;
            }
            maid.ticksExisted = 0;
            cached = new CachedMaid(fingerprint, maid);
            CACHE.put(tile, new java.lang.ref.WeakReference<CachedMaid>(cached));
        }

        EntityMaid maid = cached.maid;
        maid.prevRotationYaw = maid.rotationYaw = 0;
        maid.prevRenderYawOffset = maid.renderYawOffset = 0;
        maid.prevRotationYawHead = maid.rotationYawHead = 0;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glTranslated(x + 0.5D, y, z + 0.5D);
            GL11.glRotatef(rotation(facing), 0, 1, 0);
            GL11.glScalef(scale, scale, scale);
            RenderManager.instance.renderEntityWithPosYaw(maid, 0, 0, 0, 0, 0);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static float rotation(int facing) {
        switch (facing & 3) {
            case 1: return 90;
            case 2: return 180;
            case 3: return 270;
            default: return 0;
        }
    }

    private static final class CachedMaid {
        final String fingerprint;
        final EntityMaid maid;
        CachedMaid(String fingerprint, EntityMaid maid) { this.fingerprint = fingerprint; this.maid = maid; }
    }
}
