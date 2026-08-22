package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

/** Exact 1.7 renderer counterpart of the source BedrockCubePerFace. */
final class LegacyPerFaceCube extends ModelRenderer {
    private static final int[][] VERTEX_ORDER = {
            {5, 4, 0, 1}, {2, 3, 7, 6}, {1, 0, 3, 2},
            {4, 5, 6, 7}, {0, 4, 7, 3}, {5, 1, 2, 6}
    };
    // Source FaceUVsItem swaps Bedrock up/down and east/west for Java axes.
    private static final String[] FACE_KEYS = {"up", "down", "north", "south", "east", "west"};
    private final TexturedQuad[] faces = new TexturedQuad[6];

    LegacyPerFaceCube(ModelBase base, JsonObject uv, float x1, float y1, float z1,
                      float x2, float y2, float z2, boolean ignoredMirror) {
        super(base);
        PositionTextureVertex[] vertices = {
                vertex(x1, y1, z1), vertex(x2, y1, z1), vertex(x2, y2, z1), vertex(x1, y2, z1),
                vertex(x1, y1, z2), vertex(x2, y1, z2), vertex(x2, y2, z2), vertex(x1, y2, z2)
        };
        for (int faceIndex = 0; faceIndex < faces.length; faceIndex++) {
            JsonObject face = object(uv, FACE_KEYS[faceIndex]);
            if (face == null) continue;
            JsonArray origin = array(face, "uv");
            JsonArray size = array(face, "uv_size");
            if (origin == null || size == null || origin.size() < 2 || size.size() < 2) continue;
            float su = size.get(0).getAsFloat(), sv = size.get(1).getAsFloat();
            if (Math.abs(su) < 1.0E-9F && Math.abs(sv) < 1.0E-9F) continue;
            float[] coordinates = rotated(origin.get(0).getAsFloat(), origin.get(1).getAsFloat(),
                    su, sv, face.has("uv_rotation") ? face.get("uv_rotation").getAsInt() : 0,
                    base.textureWidth, base.textureHeight);
            int[] order = VERTEX_ORDER[faceIndex];
            PositionTextureVertex[] quad = new PositionTextureVertex[4];
            for (int n = 0; n < 4; n++)
                quad[n] = vertices[order[n]].setTexturePosition(coordinates[n * 2], coordinates[n * 2 + 1]);
            faces[faceIndex] = new TexturedQuad(quad);
        }
    }

    private static JsonObject object(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : null;
    }

    private static JsonArray array(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonArray() ? parent.getAsJsonArray(key) : null;
    }

    private static PositionTextureVertex vertex(float x, float y, float z) {
        return new PositionTextureVertex(x, y, z, 0, 0);
    }

    /** Returns UVs in source vertex order: right-top, left-top, left-bottom, right-bottom. */
    private static float[] rotated(float u, float v, float width, float height, int rotation,
                                   float textureWidth, float textureHeight) {
        float u1 = u / textureWidth, v1 = v / textureHeight;
        float u2 = (u + width) / textureWidth, v2 = (v + height) / textureHeight;
        switch (rotation) {
            case 90: return new float[]{u1, v1, u1, v2, u2, v2, u2, v1};
            case 180: return new float[]{u1, v2, u2, v2, u2, v1, u1, v1};
            case 270: return new float[]{u2, v2, u2, v1, u1, v1, u1, v2};
            default: return new float[]{u2, v1, u1, v1, u1, v2, u2, v2};
        }
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
        for (TexturedQuad face : faces) if (face != null) face.draw(tessellator, scale);
        if (childModels != null) for (Object child : childModels) ((ModelRenderer) child).render(scale);
        GL11.glPopMatrix();
    }
}
