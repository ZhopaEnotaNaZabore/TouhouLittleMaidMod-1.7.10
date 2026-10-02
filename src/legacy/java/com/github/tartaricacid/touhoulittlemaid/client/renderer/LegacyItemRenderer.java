package com.github.tartaricacid.touhoulittlemaid.client.renderer;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.IIcon;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.opengl.GL11;

import java.io.InputStreamReader;
import java.util.IdentityHashMap;
import java.util.Map;

/** Source item icons and authored JSON hand models, independent of world block rendering. */
public final class LegacyItemRenderer implements IItemRenderer, IResourceManagerReloadListener {
    public static final LegacyItemRenderer INSTANCE = new LegacyItemRenderer();
    private final Map<Item, Entry> entries = new IdentityHashMap<Item, Entry>();

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        sprite(ModBlocks.MAID_BED, "maid_bed");
        sprite(ModBlocks.GOMOKU, "gomoku");
        sprite(ModBlocks.CCHESS, "cchess");
        sprite(ModBlocks.WCHESS, "wchess");
        sprite(ModBlocks.KEYBOARD, "keyboard");
        sprite(ModBlocks.BOOKSHELF, "bookshelf");
        sprite(ModBlocks.COMPUTER, "computer");
        sprite(ModBlocks.SHRINE, "shrine");
        sprite(ModBlocks.SCARECROW, "scarecrow");
        sprite(ModBlocks.MODEL_SWITCHER, "model_switcher");
        sprite(ModBlocks.PICNIC_MAT, "picnic_basket");
        add(Item.getItemFromBlock(ModBlocks.MAID_BEACON), "maid_beacon", "maid_beacon_in_hand");
        add(Item.getItemFromBlock(ModBlocks.SNACK_CABINET), "snack_cabinet", "snack_cabinet_in_hand");
        add(ModItems.CAMERA, "camera", "camera_in_hand");
        add(ModItems.EXTINGUISHER, "extinguisher", "extinguisher_in_hand");
        add(ModItems.HAKUREI_GOHEI, "hakurei_gohei", "hakurei_gohei_in_hand");
        add(ModItems.SANAE_GOHEI, "sanae_gohei", "sanae_gohei_in_hand");
        onResourceManagerReload(Minecraft.getMinecraft().getResourceManager());
    }

    private void sprite(Block block, String name) { add(Item.getItemFromBlock(block), name, null); }

    private void add(Item item, String icon, String model) {
        entries.put(item, new Entry(icon, model));
        MinecraftForgeClient.registerItemRenderer(item, this);
    }

    @SubscribeEvent
    public void stitchIcons(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() != 1) return;
        for (Entry entry : entries.values())
            entry.sprite = event.map.registerIcon(TouhouLittleMaid.MOD_ID + ":" + entry.iconName);
    }

    @Override public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
        return entries.containsKey(stack.getItem()) && type != ItemRenderType.FIRST_PERSON_MAP;
    }

    @Override public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
        // JSON display transforms already define the held pose. Do not also
        // apply Forge's generated-sprite rotations and 1.5x scale.
        if (helper == ItemRendererHelper.EQUIPPED_BLOCK)
            return entries.get(stack.getItem()).model != null;
        return type == ItemRenderType.ENTITY
                && (helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION);
    }

    @Override public void onResourceManagerReload(IResourceManager resources) {
        for (Entry entry : entries.values()) {
            entry.model = null;
            if (entry.modelName == null) continue;
            ResourceLocation path = new ResourceLocation(TouhouLittleMaid.MOD_ID,
                    "models/item/" + entry.modelName + ".json");
            try (InputStreamReader reader = new InputStreamReader(resources.getResource(path).getInputStream(), "UTF-8")) {
                entry.model = new JsonParser().parse(reader).getAsJsonObject();
            } catch (Exception error) {
                TouhouLittleMaid.LOGGER.error("Could not load item model " + path, error);
            }
        }
    }

    @Override public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
        Entry entry = entries.get(stack.getItem());
        if (entry.sprite == null) return;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_NORMALIZE);
            if (type == ItemRenderType.INVENTORY) {
                GL11.glDisable(GL11.GL_LIGHTING);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
                IIcon icon = entry.sprite;
                Tessellator t = Tessellator.instance;
                t.startDrawingQuads();
                t.addVertexWithUV(0, 16, 0, icon.getMinU(), icon.getMaxV());
                t.addVertexWithUV(16, 16, 0, icon.getMaxU(), icon.getMaxV());
                t.addVertexWithUV(16, 0, 0, icon.getMaxU(), icon.getMinV());
                t.addVertexWithUV(0, 0, 0, icon.getMinU(), icon.getMinV());
                t.draw();
            } else if (entry.model != null) {
                if (type != ItemRenderType.ENTITY) GL11.glTranslatef(0.5F, 0.5F, 0.5F);
                String context = type == ItemRenderType.ENTITY ? "ground"
                        : type == ItemRenderType.EQUIPPED_FIRST_PERSON ? "firstperson_righthand" : "thirdperson_righthand";
                transform(entry.model, context);
                GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
                renderModel(entry.model);
            } else {
                if (type == ItemRenderType.ENTITY) GL11.glTranslatef(-0.5F, 0, 0);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationItemsTexture);
                IIcon icon = entry.sprite;
                ItemRenderer.renderItemIn2D(Tessellator.instance, icon.getMaxU(), icon.getMinV(), icon.getMinU(), icon.getMaxV(),
                        icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
            }
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void transform(JsonObject model, String context) {
        JsonObject display = model.getAsJsonObject("display");
        if (display == null || !display.has(context)) return;
        JsonObject transform = display.getAsJsonObject(context);
        double[] move = vector(transform, "translation", new double[]{0, 0, 0});
        double[] rotate = vector(transform, "rotation", new double[]{0, 0, 0});
        double[] scale = vector(transform, "scale", new double[]{1, 1, 1});
        GL11.glTranslated(move[0] / 16, move[1] / 16, move[2] / 16);
        GL11.glRotated(rotate[0], 1, 0, 0);
        GL11.glRotated(rotate[1], 0, 1, 0);
        GL11.glRotated(rotate[2], 0, 0, 1);
        GL11.glScaled(scale[0], scale[1], scale[2]);
    }

    private static void renderModel(JsonObject model) {
        for (JsonElement value : model.getAsJsonArray("elements")) {
            JsonObject element = value.getAsJsonObject();
            double[] from = vector(element, "from", null), to = vector(element, "to", null);
            GL11.glPushMatrix();
            try {
                JsonObject rotation = element.getAsJsonObject("rotation");
                if (rotation != null) {
                    double[] origin = vector(rotation, "origin", new double[]{8, 8, 8});
                    String axis = rotation.get("axis").getAsString();
                    double angle = rotation.get("angle").getAsDouble();
                    GL11.glTranslated(origin[0] / 16, origin[1] / 16, origin[2] / 16);
                    GL11.glRotated(angle, axis.equals("x") ? 1 : 0, axis.equals("y") ? 1 : 0, axis.equals("z") ? 1 : 0);
                    if (rotation.has("rescale") && rotation.get("rescale").getAsBoolean()) {
                        double s = 1 / Math.cos(Math.toRadians(angle));
                        GL11.glScaled(axis.equals("x") ? 1 : s, axis.equals("y") ? 1 : s, axis.equals("z") ? 1 : s);
                    }
                    GL11.glTranslated(-origin[0] / 16, -origin[1] / 16, -origin[2] / 16);
                }
                for (Map.Entry<String, JsonElement> face : element.getAsJsonObject("faces").entrySet()) {
                    JsonObject f = face.getValue().getAsJsonObject();
                    String texture = f.get("texture").getAsString();
                    for (int depth = 0; texture.startsWith("#") && depth < 16; depth++)
                        texture = model.getAsJsonObject("textures").get(texture.substring(1)).getAsString();
                    ResourceLocation resource = new ResourceLocation(texture);
                    Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation(resource.getResourceDomain(), "textures/" + resource.getResourcePath() + ".png"));
                    double[][] vertices = vertices(face.getKey(), from, to);
                    double[] uv = vector(f, "uv", new double[]{0, 0, 16, 16});
                    double[][] coords = {{uv[0], uv[3]}, {uv[2], uv[3]}, {uv[2], uv[1]}, {uv[0], uv[1]}};
                    int shift = f.has("rotation") ? Math.floorMod(f.get("rotation").getAsInt() / 90, 4) : 0;
                    Tessellator t = Tessellator.instance;
                    t.startDrawingQuads();
                    double[] normal = normal(vertices);
                    t.setNormal((float) normal[0], (float) normal[1], (float) normal[2]);
                    for (int i = 0; i < 4; i++) t.addVertexWithUV(vertices[i][0] / 16, vertices[i][1] / 16, vertices[i][2] / 16,
                            coords[(i + shift) & 3][0] / 16, coords[(i + shift) & 3][1] / 16);
                    t.draw();
                }
            } finally { GL11.glPopMatrix(); }
        }
    }

    private static double[] vector(JsonObject object, String name, double[] fallback) {
        if (!object.has(name)) return fallback;
        JsonArray array = object.getAsJsonArray(name);
        double[] result = new double[array.size()];
        for (int i = 0; i < result.length; i++) result[i] = array.get(i).getAsDouble();
        return result;
    }

    private static double[][] vertices(String face, double[] a, double[] b) {
        if ("north".equals(face)) return new double[][]{{b[0],a[1],a[2]},{a[0],a[1],a[2]},{a[0],b[1],a[2]},{b[0],b[1],a[2]}};
        if ("south".equals(face)) return new double[][]{{a[0],a[1],b[2]},{b[0],a[1],b[2]},{b[0],b[1],b[2]},{a[0],b[1],b[2]}};
        if ("west".equals(face)) return new double[][]{{a[0],a[1],a[2]},{a[0],a[1],b[2]},{a[0],b[1],b[2]},{a[0],b[1],a[2]}};
        if ("east".equals(face)) return new double[][]{{b[0],a[1],b[2]},{b[0],a[1],a[2]},{b[0],b[1],a[2]},{b[0],b[1],b[2]}};
        if ("down".equals(face)) return new double[][]{{a[0],a[1],a[2]},{b[0],a[1],a[2]},{b[0],a[1],b[2]},{a[0],a[1],b[2]}};
        return new double[][]{{a[0],b[1],b[2]},{b[0],b[1],b[2]},{b[0],b[1],a[2]},{a[0],b[1],a[2]}};
    }

    private static double[] normal(double[][] v) {
        double ax=v[1][0]-v[0][0], ay=v[1][1]-v[0][1], az=v[1][2]-v[0][2];
        double bx=v[2][0]-v[0][0], by=v[2][1]-v[0][1], bz=v[2][2]-v[0][2];
        double x=ay*bz-az*by, y=az*bx-ax*bz, z=ax*by-ay*bx, length=Math.sqrt(x*x+y*y+z*z);
        return length == 0 ? new double[]{0,1,0} : new double[]{x/length,y/length,z/length};
    }

    private static final class Entry {
        final String iconName;
        IIcon sprite;
        final String modelName;
        JsonObject model;
        Entry(String icon, String modelName) {
            this.iconName = icon;
            this.modelName = modelName;
        }
    }
}
