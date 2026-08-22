package com.github.tartaricacid.touhoulittlemaid.client.renderer.entity;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public final class RenderTombstone extends RenderBedrockEntity {
    public RenderTombstone() {
        super(new ResourceLocation(TouhouLittleMaid.MOD_ID, "models/bedrock/entity/tombstone.json"));
    }
    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        String suffix = entity.worldObj.provider.dimensionId == -1 ? "the_nether"
                : entity.worldObj.provider.dimensionId == 1 ? "the_end" : "overworld";
        return new ResourceLocation(TouhouLittleMaid.MOD_ID,
                "textures/bedrock/entity/tombstone/tombstone_" + suffix + ".png");
    }
}
