package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderBox extends RenderBedrockEntity {
    public RenderBox() {
        super(new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/entity/cake_box.json"));
    }
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        int index = ((EntityBox) entity).getTextureIndex();
        return new ResourceLocation(TouhouLittleMaid.MOD_ID,
                "textures/bedrock/entity/cake_box/cake_box_" + index + ".png");
    }
}
