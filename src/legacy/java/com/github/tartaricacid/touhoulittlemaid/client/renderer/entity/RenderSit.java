package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderSit extends Render {
    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
    }
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) { return null; }
}
