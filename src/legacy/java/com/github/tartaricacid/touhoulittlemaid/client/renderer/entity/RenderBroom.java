package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderBroom extends RenderBedrockEntity {
    private static final ResourceLocation TEXTURE = new ResourceLocation(TouhouLittleMaid.MOD_ID,
            "textures/bedrock/entity/broom.png");

    public RenderBroom() {
        super(new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/entity/broom.json"));
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) { return TEXTURE; }
}
