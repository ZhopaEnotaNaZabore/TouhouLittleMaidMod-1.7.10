package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import org.lwjgl.opengl.GL11;

/** Bone-only node: cubes remain vanilla/legacy child renderers. One transform
 * implementation is shared by geometry and root-to-locator attachment traversal.
 */
final class LegacyBoneRenderer extends ModelRenderer {
    float scaleX = 1, scaleY = 1, scaleZ = 1;
    LegacyBoneRenderer(ModelBase model) { super(model); }
    boolean drawable() { return showModel && !isHidden && !(scaleX == 0 && scaleY == 0 && scaleZ == 0); }
    @Override public void render(float scale) {
        if (!drawable()) return;
        GL11.glPushMatrix();
        try {
            postRender(scale);
            if (childModels != null) for (Object child : childModels) ((ModelRenderer) child).render(scale);
        } finally { GL11.glPopMatrix(); }
    }
    @Override public void postRender(float scale) {
        GL11.glTranslatef(rotationPointX * scale + offsetX, rotationPointY * scale + offsetY, rotationPointZ * scale + offsetZ);
        final float degrees = 180F / (float) Math.PI;
        if (rotateAngleZ != 0) GL11.glRotatef(rotateAngleZ * degrees, 0, 0, 1);
        if (rotateAngleY != 0) GL11.glRotatef(rotateAngleY * degrees, 0, 1, 0);
        if (rotateAngleX != 0) GL11.glRotatef(rotateAngleX * degrees, 1, 0, 0);
        GL11.glScalef(scaleX, scaleY, scaleZ);
    }
}
