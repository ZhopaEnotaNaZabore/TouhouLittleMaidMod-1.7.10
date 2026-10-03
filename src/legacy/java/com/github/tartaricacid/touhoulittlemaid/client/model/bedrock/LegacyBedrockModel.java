package com.github.tartaricacid.touhoulittlemaid.client.model.bedrock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.animation.*;
import java.util.WeakHashMap;
import org.lwjgl.opengl.GL11;

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

    private final LegacyAnimationPose pose = new LegacyAnimationPose();
    private LegacyAnimationProfile profile = new LegacyAnimationProfile(false, java.util.Collections.<String>emptyList());
    private LegacyAnimationLibrary library = new LegacyAnimationLibrary();
    private final Map<Entity, LegacyKeyframePlayer> players = new WeakHashMap<Entity, LegacyKeyframePlayer>();
    private final Map<Entity, LegacyKeyframePlayer> previewPlayers = new WeakHashMap<Entity, LegacyKeyframePlayer>();
    private final java.util.Set<String> reportedAnimationErrors = new java.util.HashSet<String>();
    /** Safe empty model when even the default pack is unavailable. No static resource load. */
    public LegacyBedrockModel() { }
    public void configureAnimations(LegacyAnimationProfile profile, LegacyAnimationLibrary library) {
        this.profile = profile; this.library = library; players.clear(); previewPlayers.clear(); reportedAnimationErrors.clear();
    }
    public boolean isGecko() { return profile.gecko; }

    public LegacyBedrockModel(InputStream stream) {
        JsonObject root = new JsonParser().parse(new InputStreamReader(stream, java.nio.charset.Charset.forName("UTF-8"))).getAsJsonObject();
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
        LegacyBoneRenderer renderer = new LegacyBoneRenderer(this);
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
        pose.bones.put(name, new LegacyAnimationPose.Bone(bone.baseX, bone.baseY, bone.baseZ, bone.baseVisible));

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
            if (parent == null && bone.parentName != null) parent = bones.get(bone.parentName);
            if (parent == bone) throw new IllegalArgumentException("Self parent: " + bone.name);
            bone.parent = parent;
            preceding.put(bone.name, bone);
        }
        for (Bone bone : allBones) {
            int depth = 0;
            for (Bone ancestor = bone.parent; ancestor != null; ancestor = ancestor.parent)
                if (ancestor == bone || ++depth > allBones.size()) throw new IllegalArgumentException("Cyclic bone parent: " + bone.name);
            Bone parent = bone.parent;
            if (parent == null) {
                bone.renderer.setRotationPoint(bone.pivot[0], 24.0F - bone.pivot[1], bone.pivot[2]);
                roots.add(bone.renderer);
            } else {
                bone.renderer.setRotationPoint(bone.pivot[0] - parent.pivot[0], -(bone.pivot[1] - parent.pivot[1]), bone.pivot[2] - parent.pivot[2]);
                parent.renderer.addChild(bone.renderer);
            }
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
            GL11.glPushMatrix();
            try { applyGlobalTransforms(); for (ModelRenderer root : roots) root.render(scale); }
            finally { GL11.glPopMatrix(); }
        } finally {
            org.lwjgl.opengl.GL11.glPopAttrib();
        }
    }

    /** Furniture uses authored rest poses, with optional inventory-dependent parts. */
    public void renderStatic(float scale,String[] parts,boolean[] visible){
        for(Bone bone:allBones)bone.reset();
        if(parts!=null)for(int i=0;i<parts.length;i++)setVisible(parts[i],visible[i]);
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ENABLE_BIT);
        try{org.lwjgl.opengl.GL11.glDisable(org.lwjgl.opengl.GL11.GL_CULL_FACE);for(ModelRenderer root:roots)root.render(scale);}
        finally{org.lwjgl.opengl.GL11.glPopAttrib();}
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                                  float netHeadYaw, float headPitch, float scale, Entity entity) {
        for (Bone bone : allBones) bone.reset();
        pose.reset();
        if (!(entity instanceof com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid)) return;
        LegacyAnimationFrame frame = LegacyAnimationController.frame(entity, limbSwing, limbSwingAmount,
                ageInTicks, netHeadYaw, headPitch, onGround);
        if (profile.gecko) {
            LegacyKeyframePlayer player = animationPlayer(entity);
            player.apply(pose, frame);
            for (Map.Entry<String, String> error : player.runtimeDiagnostics().entrySet())
                if (reportedAnimationErrors.add(error.getKey()))
                    com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.LOGGER.warn(
                            "Maid animation runtime rejection {} (profile {}): {}", error.getKey(), profile.animations, error.getValue());
        } else LegacyMaidAnimations.apply(pose, profile, frame);
        for (Map.Entry<String, LegacyAnimationPose.Bone> e : pose.bones.entrySet()) {
            Bone bone = bones.get(e.getKey()); LegacyAnimationPose.Bone value = e.getValue();
            LegacyBoneRenderer renderer = bone.renderer;
            renderer.rotateAngleX = value.x; renderer.rotateAngleY = value.y; renderer.rotateAngleZ = value.z;
            renderer.offsetX = value.offsetX; renderer.offsetY = value.offsetY; renderer.offsetZ = value.offsetZ;
            renderer.scaleX = value.scaleX; renderer.scaleY = value.scaleY; renderer.scaleZ = value.scaleZ;
            renderer.showModel = value.visible;
        }
    }

    private LegacyKeyframePlayer animationPlayer(Entity entity) {
        Map<Entity, LegacyKeyframePlayer> target =
                com.github.tartaricacid.touhoulittlemaid.client.renderer.LegacyMaidPreviewContext.active() ? previewPlayers : players;
        LegacyKeyframePlayer player = target.get(entity);
        if (player == null) { player = new LegacyKeyframePlayer(library); target.put(entity, player); }
        return player;
    }

    private void applyGlobalTransforms() {
        for (float[] transform : pose.transforms) {
            if (transform.length == 4) GL11.glTranslatef(transform[1], transform[2], transform[3]);
            else GL11.glRotatef(transform[0], transform[1], transform[2], transform[3]);
        }
    }
    /** Applies the complete root-to-bone transform for held-item layers. */
    public boolean postRenderBone(String name, float scale) {
        Bone bone = bones.get(name);
        if (bone == null || !drawableChain(bone)) return false;
        applyGlobalTransforms();
        postRenderBone(bone, scale, 0);
        return true;
    }
    private boolean drawableChain(Bone bone) {
        for (Bone b = bone; b != null; b = b.parent) if (!b.renderer.drawable()) return false;
        return true;
    }
    /** Modern Bedrock helpers are optional local arm transforms, often root bones.
     * Hierarchical locators instead traverse their authored parent chain exactly once.
     */
    public boolean postRenderHand(String armName, String locatorName, float scale) {
        Bone locator = locatorName == null ? null : bones.get(locatorName);
        if (profile.gecko || locator == null || locator.parent != null) return postRenderBone(locator == null ? armName : locatorName, scale);
        Bone arm = armName == null ? null : bones.get(armName);
        if (arm == null || !drawableChain(arm) || !locator.renderer.drawable()) return false;
        applyGlobalTransforms(); postRenderBone(arm, scale, 0);
        // Source BedrockPart helper uses its root pivot, including the 24-pixel origin.
        locator.renderer.postRender(scale);
        return true;
    }
    public boolean hasBone(String name) { return bones.containsKey(name); }

    /** Renders one authored bone subtree, used by the board-game piece atlases. */
    public boolean renderBone(String name, Entity entity, float scale) {
        Bone bone = bones.get(name);
        if (bone == null) return false;
        for (Bone item : allBones) item.reset();
        pose.reset();
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
        private final LegacyBoneRenderer renderer;
        private final float baseX;
        private final float baseY;
        private final float baseZ;
        private final boolean baseVisible;
        private Bone parent;

        private Bone(String name, String parentName, float[] pivot, LegacyBoneRenderer renderer,
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
            renderer.scaleX=renderer.scaleY=renderer.scaleZ=1;
        }
    }
}
