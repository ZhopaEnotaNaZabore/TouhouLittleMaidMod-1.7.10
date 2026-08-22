package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal Bedrock geometry 1.12 parser backed by vanilla 1.7 ModelRenderer.
 * Supports bone hierarchy, pivots, rotations, mirrored cubes, UV offsets and inflate.
 */
public final class LegacyBedrockModel extends ModelBase {
    private final Map<String, Bone> bones = new LinkedHashMap<String, Bone>();
    private final List<Bone> allBones = new ArrayList<Bone>();
    private final List<ModelRenderer> roots = new ArrayList<ModelRenderer>();

    public LegacyBedrockModel(InputStream stream) {
        JsonObject root = new JsonParser().parse(new InputStreamReader(stream)).getAsJsonObject();
        JsonArray geometries = root.getAsJsonArray("minecraft:geometry");
        JsonObject geometry;
        if (geometries != null && geometries.size() > 0) {
            geometry = geometries.get(0).getAsJsonObject();
        } else {
            geometry = null;
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                if (entry.getKey().startsWith("geometry.") && entry.getValue().isJsonObject()) {
                    geometry = entry.getValue().getAsJsonObject();
                    break;
                }
            }
            if (geometry == null) throw new IllegalArgumentException("No Bedrock geometry object");
        }
        JsonObject description = geometry.has("description") ? geometry.getAsJsonObject("description") : geometry;
        textureWidth = description.has("texture_width") ? description.get("texture_width").getAsInt()
                : description.has("texturewidth") ? description.get("texturewidth").getAsInt() : 64;
        textureHeight = description.has("texture_height") ? description.get("texture_height").getAsInt()
                : description.has("textureheight") ? description.get("textureheight").getAsInt() : 32;

        JsonArray boneArray = geometry.getAsJsonArray("bones");
        for (JsonElement element : boneArray) createBone(element.getAsJsonObject());
        attachBones();
    }

    private void createBone(JsonObject json) {
        String name = json.get("name").getAsString();
        float[] pivot = vector(json, "pivot", 0, 0, 0);
        float[] rotation = vector(json, "rotation", 0, 0, 0);
        ModelRenderer renderer = new ModelRenderer(this);
        renderer.setTextureSize((int) textureWidth, (int) textureHeight);
        // ModelRenderer and the source SimpleBedrockModel both apply Z-Y-X
        // rotations in Java model space.  Negating X/Y here mirrored every
        // nested ribbon, sleeve and hair bone away from its authored pose.
        renderer.rotateAngleX = radians(rotation[0]);
        renderer.rotateAngleY = radians(rotation[1]);
        renderer.rotateAngleZ = radians(rotation[2]);
        Bone bone = new Bone(name, json.has("parent") ? json.get("parent").getAsString() : null,
                pivot, renderer, renderer.rotateAngleX, renderer.rotateAngleY, renderer.rotateAngleZ,
                !(json.has("neverRender") && json.get("neverRender").getAsBoolean()));
        allBones.add(bone);
        bones.put(name, bone);

        JsonArray cubes = json.getAsJsonArray("cubes");
        if (cubes != null) for (JsonElement element : cubes) addCube(bone, element.getAsJsonObject(), json);
    }

    private void addCube(Bone bone, JsonObject cubeJson, JsonObject boneJson) {
        JsonElement uvElement = cubeJson.get("uv");
        if (uvElement == null) return;
        JsonArray uv = representativeUv(uvElement);
        if (uv == null || uv.size() < 2) return;
        float[] origin = vector(cubeJson, "origin", 0, 0, 0);
        float[] size = vector(cubeJson, "size", 0, 0, 0);
        float[] cubePivot = vector(cubeJson, "pivot", bone.pivot[0], bone.pivot[1], bone.pivot[2]);
        float[] cubeRotation = vector(cubeJson, "rotation", 0, 0, 0);
        float inflate = cubeJson.has("inflate") ? cubeJson.get("inflate").getAsFloat() : 0.0F;
        float x1=origin[0]-cubePivot[0]-inflate,y1=-(origin[1]+size[1]-cubePivot[1])-inflate,z1=origin[2]-cubePivot[2]-inflate;
        float x2=x1+size[0]+inflate*2,y2=y1+size[1]+inflate*2,z2=z1+size[2]+inflate*2;
        boolean mirrored=cubeJson.has("mirror")?cubeJson.get("mirror").getAsBoolean():boneJson.has("mirror")&&boneJson.get("mirror").getAsBoolean();
        ModelRenderer cube = uvElement.isJsonObject()
                ? new LegacyPerFaceCube(this, uvElement.getAsJsonObject(), x1,y1,z1,x2,y2,z2,mirrored)
                : new LegacyBedrockCube(this, uv.get(0).getAsFloat(), uv.get(1).getAsFloat(),
                x1, y1, z1, x2, y2, z2, size[0], size[1], size[2], mirrored);
        cube.setTextureSize((int) textureWidth, (int) textureHeight);
        cube.mirror = mirrored;
        cube.setRotationPoint(cubePivot[0] - bone.pivot[0], -(cubePivot[1] - bone.pivot[1]), cubePivot[2] - bone.pivot[2]);
        cube.rotateAngleX = radians(cubeRotation[0]);
        cube.rotateAngleY = radians(cubeRotation[1]);
        cube.rotateAngleZ = radians(cubeRotation[2]);
        bone.renderer.addChild(cube);
    }

    /**
     * Newer Bedrock exporters may store one UV rectangle per face. Vanilla
     * 1.7 ModelBox only accepts a single unfolded texture origin, so use a
     * stable representative face. Geometry is preserved and remains textured;
     * array-style Bedrock UVs retain their exact original mapping.
     */
    private static JsonArray representativeUv(JsonElement element) {
        if (element.isJsonArray()) return element.getAsJsonArray();
        if (!element.isJsonObject()) return null;
        JsonObject faces = element.getAsJsonObject();
        String[] preference = {"north", "south", "east", "west", "up", "down"};
        for (String face : preference) if (faces.has(face) && faces.get(face).isJsonObject()) {
            JsonElement uv = faces.getAsJsonObject(face).get("uv");
            if (uv != null && uv.isJsonArray()) return uv.getAsJsonArray();
        }
        return null;
    }

    private void attachBones() {
        Map<String, Bone> preceding = new LinkedHashMap<String, Bone>();
        for (Bone bone : allBones) {
            Bone parent = bone.parentName == null ? null : preceding.get(bone.parentName);
            bone.parent = parent;
            if (parent == null) {
                bone.renderer.setRotationPoint(bone.pivot[0], 24.0F - bone.pivot[1], bone.pivot[2]);
                roots.add(bone.renderer);
            } else {
                bone.renderer.setRotationPoint(bone.pivot[0] - parent.pivot[0],
                        -(bone.pivot[1] - parent.pivot[1]), bone.pivot[2] - parent.pivot[2]);
                parent.renderer.addChild(bone.renderer);
            }
            // A few bundled legacy models contain a repeated name (for
            // example left/right hair). Bedrock references bind to the most
            // recently declared preceding bone, not one globally overwritten
            // map entry; preserving that order prevents missing/duplicated limbs.
            preceding.put(bone.name, bone);
        }
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                       float netHeadYaw, float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        // The source model uses RenderType.entityCutoutNoCull. Many authored
        // ribbons, wings and bows are zero-depth planes and disappear in the
        // vanilla 1.7 living renderer when back-face culling remains enabled.
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ENABLE_BIT);
        try {
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
            for (ModelRenderer root : roots) root.render(scale);
        } finally {
            org.lwjgl.opengl.GL11.glPopAttrib();
        }
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        for (Bone bone : allBones) bone.reset();
        Bone head = bones.get("head");
        if (head == null) head = bones.get("Head");
        if (head != null) {
            head.renderer.rotateAngleY += radians(netHeadYaw);
            head.renderer.rotateAngleX += radians(headPitch);
        }
        // Values and phases from MaidBaseAnimation in the source mod.
        animateLimb("armLeft", -MathHelper.cos(limbSwing * 0.67F) * 0.7F * limbSwingAmount);
        animateLimb("armRight", MathHelper.cos(limbSwing * 0.67F) * 0.7F * limbSwingAmount);
        animateLimb("legLeft", MathHelper.cos(limbSwing * 0.67F) * 0.3F * limbSwingAmount);
        animateLimb("legRight", -MathHelper.cos(limbSwing * 0.67F) * 0.3F * limbSwingAmount);
        animateLimb("LeftArm", -MathHelper.cos(limbSwing * 0.67F) * 0.7F * limbSwingAmount);
        animateLimb("RightArm", MathHelper.cos(limbSwing * 0.67F) * 0.7F * limbSwingAmount);
        animateLimb("LeftLeg", MathHelper.cos(limbSwing * 0.67F) * 0.3F * limbSwingAmount);
        animateLimb("RightLeg", -MathHelper.cos(limbSwing * 0.67F) * 0.3F * limbSwingAmount);
        LegacyAnimationController.apply(this, entity, ageInTicks, onGround);
    }

    private void animateLimb(String name, float angle) {
        Bone bone = bones.get(name);
        if (bone != null) bone.renderer.rotateAngleX += angle;
    }
    /** Applies the complete root-to-bone transform for held-item layers. */
    public boolean postRenderBone(String name, float scale) {
        Bone bone = bones.get(name);
        if (bone == null) return false;
        postRenderBone(bone, scale, 0);
        return true;
    }
    public boolean hasBone(String name) { return bones.containsKey(name); }

    /** Renders one authored bone subtree, used by the board-game piece atlases. */
    public boolean renderBone(String name, Entity entity, float scale) {
        Bone bone = bones.get(name);
        if (bone == null) return false;
        for (Bone item : allBones) item.reset();
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ENABLE_BIT);
        try {
            org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);
            bone.renderer.render(scale);
        } finally {
            org.lwjgl.opengl.GL11.glPopAttrib();
        }
        return true;
    }

    /** Source LayerMaidBackpack reads this authored helper bone directly. */
    public boolean applyBackpackPositioning() {
        Bone bone = bones.get("backpackPositioningBone");
        if (bone == null) return false;
        org.lwjgl.opengl.GL11.glTranslatef(bone.renderer.rotationPointX / 16.0F,
                (bone.renderer.rotationPointY - 15.0F) / 16.0F,
                (bone.renderer.rotationPointZ + 4.0F) / 16.0F);
        return true;
    }

    private void postRenderBone(Bone bone, float scale, int depth) {
        if (depth > allBones.size()) return;
        if (bone.parent != null) postRenderBone(bone.parent, scale, depth + 1);
        bone.renderer.postRender(scale);
    }
    void addRotation(String name,float x,float y,float z){Bone bone=bones.get(name);if(bone!=null){bone.renderer.rotateAngleX+=x;bone.renderer.rotateAngleY+=y;bone.renderer.rotateAngleZ+=z;}}
    void setBaseRotation(String name,float x,float y,float z){Bone bone=bones.get(name);if(bone!=null){bone.renderer.rotateAngleX=bone.baseX+x;bone.renderer.rotateAngleY=bone.baseY+y;bone.renderer.rotateAngleZ=bone.baseZ+z;}}
    void setVisible(String name,boolean visible){Bone bone=bones.get(name);if(bone!=null)bone.renderer.showModel=bone.baseVisible&&visible;}
    void addOffset(String name,float x,float y,float z){Bone bone=bones.get(name);if(bone!=null){bone.renderer.offsetX=x/16F;bone.renderer.offsetY=-y/16F;bone.renderer.offsetZ=z/16F;}}

    private static float[] vector(JsonObject object, String key, float x, float y, float z) {
        JsonArray array = object.getAsJsonArray(key);
        return array == null ? new float[]{x, y, z}
                : new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    private static float radians(float degrees) { return degrees * (float) Math.PI / 180.0F; }

    private static final class Bone {
        private final String name;
        private final String parentName;
        private final float[] pivot;
        private final ModelRenderer renderer;
        private final float baseX;
        private final float baseY;
        private final float baseZ;
        private final boolean baseVisible;
        private Bone parent;

        private Bone(String name, String parentName, float[] pivot, ModelRenderer renderer,
                     float baseX, float baseY, float baseZ, boolean baseVisible) {
            this.name = name;
            this.parentName = parentName;
            this.pivot = pivot;
            this.renderer = renderer;
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.baseVisible = baseVisible;
        }

        private void reset() {
            renderer.rotateAngleX = baseX;
            renderer.rotateAngleY = baseY;
            renderer.rotateAngleZ = baseZ;
            renderer.offsetX=renderer.offsetY=renderer.offsetZ=0;
            renderer.showModel=baseVisible;
        }
    }
}
