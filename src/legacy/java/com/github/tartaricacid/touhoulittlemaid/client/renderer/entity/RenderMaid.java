package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyBedrockModel;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidAccessoryModels;
import com.github.tartaricacid.touhoulittlemaid.client.chat.ClientChatBubbles;
import com.github.tartaricacid.touhoulittlemaid.client.chat.LegacyKaomojiLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.opengl.GL11;

/** Default maid Bedrock geometry renderer; pack discovery is layered on next. */
public final class RenderMaid extends RenderLiving {
    private static final String PACK_ROOT = "";
    private static final ResourceLocation FALLBACK_TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID,
            PACK_ROOT + "textures/entity/hakurei_reimu.png");
    private static final LegacyBedrockModel DEFAULT_MODEL = loadModel();

    public RenderMaid() {
        super(DEFAULT_MODEL, 0.35F);
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
        // RenderLiving's Entity/EntityLivingBase bridge methods dispatch to the
        // EntityLiving overload in 1.7.10. Overriding only EntityLivingBase is
        // silently bypassed, which left DEFAULT_MODEL active while the virtual
        // texture lookup still changed: every skin was stretched over Reimu.
        mainModel = LegacyMaidModelRegistry.INSTANCE.getModel(((EntityMaid) entity).getModelId(), DEFAULT_MODEL);
        super.doRender(entity, x, y, z, yaw, partialTicks);
        ClientChatBubbles.Entry bubble=ClientChatBubbles.get(entity.getEntityId());
        if(bubble!=null){String text=(bubble.error?"§c":"§f")+bubble.text;if(text.length()>64)text=text.substring(0,61)+"...";func_147906_a(entity,text,x,y+entity.height+.35,z,32);}
        else if(((EntityMaid)entity).isBegging())func_147906_a(entity,LegacyKaomojiLoader.INSTANCE.forEntity(entity.getEntityId()),x,y+entity.height+.25,z,24);
    }

    private static LegacyBedrockModel loadModel() {
        ResourceLocation geometry = new ResourceLocation(TouhouLittleMaid.MOD_ID,
                PACK_ROOT + "models/entity/hakurei_reimu.json");
        java.io.InputStream stream = null;
        try {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(geometry).getInputStream();
            return new LegacyBedrockModel(stream);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not load default maid Bedrock model", exception);
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) { }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return getMaidTexture((EntityMaid) entity);
    }

    protected ResourceLocation getMaidTexture(EntityMaid maid) {
        LegacyMaidModelRegistry.Entry entry = LegacyMaidModelRegistry.INSTANCE.getEntry(maid.getModelId());
        ResourceLocation selected = entry == null ? FALLBACK_TEXTURE : entry.texture;
        try {
            Minecraft.getMinecraft().getResourceManager().getResource(selected);
            return selected;
        } catch (Exception ignored) {
            return FALLBACK_TEXTURE;
        }
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        LegacyMaidModelRegistry.Entry entry = LegacyMaidModelRegistry.INSTANCE.getEntry(((EntityMaid) entity).getModelId());
        float scale = entry == null ? 1.0F : entry.scale;
        if(((EntityMaid)entity).isMaidSleeping()){GL11.glTranslatef(0,.25F,0);GL11.glRotatef(90,0,0,1);}
        GL11.glScalef(scale, scale, scale);
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase living, float partialTicks) {
        EntityMaid maid = (EntityMaid) living;
        LegacyBedrockModel model = mainModel instanceof LegacyBedrockModel ? (LegacyBedrockModel) mainModel : null;
        if (model == null) return;
        ItemStack held = maid.getMaidEquipmentInventory().getStackInSlot(0);
        ItemStack offhand = maid.getMaidEquipmentInventory().getStackInSlot(1);
        if (held != null) renderHandItem(maid, model, false, held);
        if (offhand != null) renderHandItem(maid, model, true, offhand);
        LegacyMaidModelRegistry.Entry entry = LegacyMaidModelRegistry.INSTANCE.getEntry(maid.getModelId());
        if ((entry == null || entry.showCustomHead)) renderHeadBlock(maid, model);
        if (entry == null || entry.showBackpack)
            LegacyMaidAccessoryModels.INSTANCE.renderBackpack(maid, model, partialTicks);
        // The source renderer has no generic armor-item layer. Rendering chest,
        // legs and boots through RenderItem creates detached item cuboids.
        // Baubles are capabilities/effects, not four physical items orbiting
        // the maid. Dedicated visual effects are rendered separately.
    }

    private void renderHeadBlock(EntityMaid maid, LegacyBedrockModel model) {
        ItemStack stack = maid.getEquipmentInSlot(4);
        if (stack == null || !(stack.getItem() instanceof ItemBlock)) return;
        String head = firstBone(model, "head", "Head");
        if (head == null) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            if (!model.postRenderBone(head, 0.0625F)) return;
            // Vanilla 1.7 biped-head block transform, applied after the authored
            // Bedrock head pivot so hats follow look/sleep/sitting animation.
            GL11.glTranslatef(0.0F, -0.25F, 0.0F);
            GL11.glRotatef(180.0F, 0, 1, 0);
            GL11.glScalef(0.625F, -0.625F, -0.625F);
            RenderHelper.enableStandardItemLighting();
            RenderManager.instance.itemRenderer.renderItem(maid, stack, 0);
        } finally {
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void renderHandItem(EntityMaid maid, LegacyBedrockModel model, boolean left, ItemStack stack) {
        String arm = firstBone(model, left ? "armLeft" : "armRight", left ? "LeftArm" : "RightArm");
        String positioning = firstBone(model,
                left ? "armLeftPositioningBone" : "armRightPositioningBone",
                left ? "LeftHandLocator" : "RightHandLocator");
        boolean authoredPosition = positioning != null;
        if (arm == null && !authoredPosition) return;
        renderAttached(maid, model, authoredPosition ? positioning : arm, stack,
                authoredPosition ? 0.0F : (left ? -0.0625F : 0.0625F),
                0.125F, authoredPosition ? -0.0625F : -0.525F, 0.48F);
    }

    private static String firstBone(LegacyBedrockModel model, String first, String second) {
        if (model.hasBone(first)) return first;
        return model.hasBone(second) ? second : null;
    }

    private void renderAttached(EntityMaid maid, LegacyBedrockModel model, String bone, ItemStack stack,
                                float x, float y, float z, float scale) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            if (!model.postRenderBone(bone, 0.0625F)) return;
            // Same order as LayerMaidHeldItem: X rotation, Y rotation, then
            // the authored/fallback hand offset.
            GL11.glRotatef(-90, 1, 0, 0);
            GL11.glRotatef(180, 0, 1, 0);
            GL11.glTranslatef(x, y, z);
            GL11.glScalef(scale, scale, scale);
            RenderHelper.enableStandardItemLighting();
            RenderManager.instance.itemRenderer.renderItem(maid, stack, 0);
            if (stack.getItem().requiresMultipleRenderPasses()) {
                for (int pass = 1; pass < stack.getItem().getRenderPasses(stack.getItemDamage()); pass++)
                    RenderManager.instance.itemRenderer.renderItem(maid, stack, pass);
            }
        } finally {
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }
}
