package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/** Float-sized Bedrock cube using the same unfolded UV order as BedrockCubeBox. */
final class LegacyBedrockCube extends ModelRenderer {
    private static final int[][] VERTEX_ORDER = {
            {5, 4, 0, 1}, {2, 3, 7, 6}, {1, 0, 3, 2},
            {4, 5, 6, 7}, {0, 4, 7, 3}, {5, 1, 2, 6}
    };
    private static final int[][] UV_NORMAL = {
            {1, 2, 6, 7}, {2, 3, 7, 6}, {1, 2, 7, 8},
            {4, 5, 7, 8}, {0, 1, 7, 8}, {2, 4, 7, 8}
    };
    private static final int[][] UV_MIRRORED = {
            {2, 1, 6, 7}, {3, 2, 7, 6}, {2, 1, 7, 8},
            {5, 4, 7, 8}, {4, 2, 7, 8}, {1, 0, 7, 8}
    };
    private final TexturedQuad[] faces = new TexturedQuad[6];

    LegacyBedrockCube(ModelBase base, float texU, float texV,
                      float x1, float y1, float z1, float x2, float y2, float z2,
                      float width, float height, float depth, boolean mirror) {
        super(base);
        PositionTextureVertex[] vertices = {
                vertex(x1, y1, z1), vertex(x2, y1, z1), vertex(x2, y2, z1), vertex(x1, y2, z1),
                vertex(x1, y1, z2), vertex(x2, y1, z2), vertex(x2, y2, z2), vertex(x1, y2, z2)
        };
        float dx = (float) Math.floor(width), dy = (float) Math.floor(height), dz = (float) Math.floor(depth);
        float[] u = {texU, texU + dz, texU + dz + dx, texU + dz + dx + dx,
                texU + dz + dx + dz, texU + dz + dx + dz + dx};
        float[] v = {0, 0, 0, 0, 0, 0, texV, texV + dz, texV + dz + dy};
        int[][] uvOrder = mirror ? UV_MIRRORED : UV_NORMAL;
        for (int face = 0; face < faces.length; face++) {
            PositionTextureVertex[] quad = new PositionTextureVertex[4];
            int[] order = VERTEX_ORDER[face];
            int[] faceUv = uvOrder[face];
            quad[0] = textured(vertices[order[0]], u[faceUv[1]], v[faceUv[2]], base);
            quad[1] = textured(vertices[order[1]], u[faceUv[0]], v[faceUv[2]], base);
            quad[2] = textured(vertices[order[2]], u[faceUv[0]], v[faceUv[3]], base);
            quad[3] = textured(vertices[order[3]], u[faceUv[1]], v[faceUv[3]], base);
            faces[face] = new TexturedQuad(quad);
        }
    }

    private static PositionTextureVertex vertex(float x, float y, float z) {
        return new PositionTextureVertex(x, y, z, 0, 0);
    }

    private static PositionTextureVertex textured(PositionTextureVertex vertex, float u, float v, ModelBase base) {
        return vertex.setTexturePosition(u / base.textureWidth, v / base.textureHeight);
    }

    @Override
    public void render(float scale) {
        if (isHidden || !showModel) return;
        GL11.glPushMatrix();
        GL11.glTranslatef(offsetX, offsetY, offsetZ);
        GL11.glTranslatef(rotationPointX * scale, rotationPointY * scale, rotationPointZ * scale);
        if (rotateAngleZ != 0) GL11.glRotatef(rotateAngleZ * 57.29578F, 0, 0, 1);
        if (rotateAngleY != 0) GL11.glRotatef(rotateAngleY * 57.29578F, 0, 1, 0);
        if (rotateAngleX != 0) GL11.glRotatef(rotateAngleX * 57.29578F, 1, 0, 0);
        Tessellator tessellator = Tessellator.instance;
        for (TexturedQuad face : faces) face.draw(tessellator, scale);
        if (childModels != null) for (Object child : childModels) ((ModelRenderer) child).render(scale);
        GL11.glPopMatrix();
    }
}
