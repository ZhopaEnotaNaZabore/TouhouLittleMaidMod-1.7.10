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

    public RenderMaid() {
        super(new LegacyBedrockModel(), 0.35F);
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTicks) {
        boolean preview=com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext.active();
        if(preview){
            partialTicks=com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext.partialTicks(partialTicks);
            entity.prevRenderYawOffset=entity.renderYawOffset;entity.prevRotationYaw=entity.rotationYaw;entity.prevRotationPitch=entity.rotationPitch;
        }
        // RenderLiving's Entity/EntityLivingBase bridge methods dispatch to the
        // EntityLiving overload in 1.7.10. Overriding only EntityLivingBase is
        // silently bypassed, which left DEFAULT_MODEL active while the virtual
        // texture lookup still changed: every skin was stretched over Reimu.
        LegacyMaidModelRegistry.Resolved selected = LegacyMaidModelRegistry.INSTANCE.resolve(((EntityMaid) entity).getModelId());
        mainModel = selected.model;
        shadowSize = 0.35F * selected.entry.scale;
        super.doRender(entity, x, y, z, yaw, partialTicks);
        if(preview)return;
        ClientChatBubbles.Entry bubble=ClientChatBubbles.get(entity.getEntityId());
        if(bubble!=null){String text=(bubble.error?"§c":"§f")+bubble.text;if(text.length()>64)text=text.substring(0,61)+"...";func_147906_a(entity,text,x,y+entity.height+.35,z,32);}
        else if(((EntityMaid)entity).isBegging())func_147906_a(entity,LegacyKaomojiLoader.INSTANCE.forEntity(entity.getEntityId()),x,y+entity.height+.25,z,24);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return getMaidTexture((EntityMaid) entity);
    }

    protected ResourceLocation getMaidTexture(EntityMaid maid) {
        return LegacyMaidModelRegistry.INSTANCE.resolve(maid.getModelId()).entry.texture;
    }

    @Override
    protected void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        float scale = LegacyMaidModelRegistry.INSTANCE.resolve(((EntityMaid) entity).getModelId()).entry.scale;
        GL11.glScalef(scale, scale, scale);
    }

    @Override
    protected void rotateCorpse(EntityLivingBase entity, float age, float yaw, float partialTicks) {
        EntityMaid maid = (EntityMaid) entity;
        if (maid.isMaidSleeping() && maid.isEntityAlive()) {
            // 1.7 bed surrogate supplies synchronized yaw, not the modern sleeping pose API.
            // Rotate outside model scale, matching the living-renderer sleep transform order.
            float bedYaw=maid.ridingEntity instanceof com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit
                    ? maid.ridingEntity.rotationYaw : yaw;
            GL11.glRotatef(90 - bedYaw, 0, 1, 0);
            GL11.glRotatef(90, 0, 0, 1);
            GL11.glRotatef(270, 0, 1, 0);
        } else {
            super.rotateCorpse(entity, age, yaw, partialTicks);
            if (maid.ridingEntity instanceof net.minecraft.entity.player.EntityPlayer
                    && !LegacyMaidModelRegistry.INSTANCE.resolve(maid.getModelId()).entry.gecko) {
                GL11.glTranslatef(-.375F, .8325F, .375F);
                GL11.glRotatef(-65, 0, 0, 1); GL11.glRotatef(80, 0, 1, 0);
            }
        }
    }

    @Override
    protected void renderEquippedItems(EntityLivingBase living, float partialTicks) {
        EntityMaid maid = (EntityMaid) living;
        LegacyBedrockModel model = mainModel instanceof LegacyBedrockModel ? (LegacyBedrockModel) mainModel : null;
        if (model == null) return;
        ItemStack held = maid.getHeldItem();
        ItemStack offhand = maid.getOffhandItem();
        com.github.tartaricacid.touhoulittlemaid.entity.animation.MaidActionState action = maid.getActionState();
        ItemStack shown = action.displayItem(maid.ticksExisted + partialTicks);
        if (shown != null && !maid.isMaidSleeping()) { if (action.useLeft()) offhand = shown; else held = shown; }
        if (held != null) renderHandItem(maid, model, false, held);
        if (offhand != null) renderHandItem(maid, model, true, offhand);
        LegacyMaidModelRegistry.Entry entry = LegacyMaidModelRegistry.INSTANCE.resolve(maid.getModelId()).entry;
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
        String positioning = model.isGecko()
                ? firstBone(model, left ? "LeftHandLocator" : "RightHandLocator", left ? "LeftHand" : "RightHand")
                : firstBone(model, left ? "armLeftPositioningBone" : "armRightPositioningBone", left ? "LeftHandLocator" : "RightHandLocator");
        if (arm == null && positioning == null) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try (com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidItemContext hand =
                     com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidItemContext.enter(left)) {
            if (!model.postRenderHand(arm, positioning, .0625F)) return;
            if (model.isGecko()) {
                // Change the locator basis back from legacy C=diag(-1,-1,1)
                // before applying GeckoLayerMaidHeld's authored local transform.
                GL11.glRotatef(180, 0, 0, 1);
                GL11.glTranslatef(0, -.0625F, -.1F);
                GL11.glRotatef(-90, 1, 0, 0);
            } else {
                GL11.glRotatef(-90, 1, 0, 0); GL11.glRotatef(180, 0, 1, 0);
                GL11.glTranslatef(positioning == null ? (left ? -.0625F : .0625F) : 0, .125F, positioning == null ? -.525F : -.0625F);
            }
            // Authored JSON models already contain display scale. Do not halve them again.
            net.minecraftforge.client.IItemRenderer custom = net.minecraftforge.client.MinecraftForgeClient.getItemRenderer(
                    stack, net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED);
            if (custom == com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyItemRenderer.INSTANCE) {
                // Our renderer consumes the modern hand origin directly. Forge's
                // equipped helper would add another centering/rotation transform.
                RenderHelper.enableStandardItemLighting();
                com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyItemRenderer.INSTANCE.renderMaidHand(stack);
                return;
            } else if (custom == null && !(stack.getItem() instanceof ItemBlock
                    && net.minecraft.client.renderer.RenderBlocks.renderItemIn3d(
                            net.minecraft.block.Block.getBlockFromItem(stack.getItem()).getRenderType()))) {
                com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyVanillaHandTransform.apply(stack,left);
            } else {
                // Keep Forge's own convention for 3D blocks and external custom renderers.
                GL11.glTranslatef(left ? -.0625F : .0625F, 0, 0);
                GL11.glScalef(.625F, .625F, .625F);
            }
            RenderHelper.enableStandardItemLighting();
            RenderManager.instance.itemRenderer.renderItem(maid, stack, 0);
            if (stack.getItem().requiresMultipleRenderPasses()) for (int pass = 1; pass < stack.getItem().getRenderPasses(stack.getItemDamage()); pass++)
                RenderManager.instance.itemRenderer.renderItem(maid, stack, pass);
        } finally {
            RenderHelper.disableStandardItemLighting(); GL11.glPopMatrix(); GL11.glPopAttrib();
        }
    }

    private static String firstBone(LegacyBedrockModel model, String first, String second) {
        if (model.hasBone(first)) return first;
        return model.hasBone(second) ? second : null;
    }

}
