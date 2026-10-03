package com.github.tartaricacid.touhoulittlemaid.client.renderer;
import net.minecraft.entity.EntityLivingBase;
/** Isolates preview animation time and restores the live entity after vanilla GUI rendering. */
public final class LegacyMaidPreviewContext implements AutoCloseable {
    private static final ThreadLocal<LegacyMaidPreviewContext> CURRENT=new ThreadLocal<LegacyMaidPreviewContext>();
    private final LegacyMaidPreviewContext previous;private final EntityLivingBase entity;private final float[] angles;
    public final float partialTicks;
    private LegacyMaidPreviewContext(EntityLivingBase entity,float partial){
        this.entity=entity;partialTicks=Math.max(0,Math.min(1,partial));previous=CURRENT.get();
        angles=new float[]{entity.renderYawOffset,entity.prevRenderYawOffset,entity.rotationYaw,entity.prevRotationYaw,entity.rotationPitch,entity.prevRotationPitch,entity.rotationYawHead,entity.prevRotationYawHead};CURRENT.set(this);
    }
    public static LegacyMaidPreviewContext enter(EntityLivingBase entity,float partial){return new LegacyMaidPreviewContext(entity,partial);}
    public static boolean active(){return CURRENT.get()!=null;}
    public static float partialTicks(float fallback){return active()?CURRENT.get().partialTicks:fallback;}
    public void close(){entity.renderYawOffset=angles[0];entity.prevRenderYawOffset=angles[1];entity.rotationYaw=angles[2];entity.prevRotationYaw=angles[3];entity.rotationPitch=angles[4];entity.prevRotationPitch=angles[5];entity.rotationYawHead=angles[6];entity.prevRotationYawHead=angles[7];if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
}
