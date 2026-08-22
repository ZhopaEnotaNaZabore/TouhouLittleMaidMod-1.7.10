package com.github.tartaricacid.touhoulittlemaid.client.renderer.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Renders the original 1.20 Blockbench JSON instead of an approximate cuboid placeholder. */
final class LegacyScarecrowModel {
    private static Model lower;
    private static Model upper;
    private static Model beaconDown;
    private static Model beaconUp;

    private LegacyScarecrowModel() { }

    static void renderWorld(Tessellator tessellator, IIcon icon, boolean upperHalf, int facing,
                            double x, double y, double z, int brightness) {
        Model model = get(upperHalf);
        if (model != null) model.render(tessellator, icon, facing, x, y, z, brightness);
    }

    static void renderInventory(Tessellator tessellator, IIcon lowerIcon, IIcon upperIcon) {
        Model bottom = get(false), top = get(true);
        tessellator.startDrawingQuads();
        if (bottom != null) bottom.render(tessellator, lowerIcon, 0, 0, 0, 0, 0xF000F0);
        if (top != null) top.render(tessellator, upperIcon, 0, 0, 1, 0, 0xF000F0);
        tessellator.draw();
    }

    static void renderBeaconWorld(Tessellator tessellator, IIcon icon, boolean upperHalf, int rotation,
                                  double x, double y, double z, int brightness) {
        Model model = getBeacon(upperHalf);
        if (model != null) model.render(tessellator, icon, rotation, x, y, z, brightness);
    }

    static void renderBeaconInventory(Tessellator tessellator, IIcon icon) {
        Model bottom = getBeacon(false), top = getBeacon(true);
        tessellator.startDrawingQuads();
        if (bottom != null) bottom.render(tessellator, icon, 0, 0, 0, 0, 0xF000F0);
        if (top != null) top.render(tessellator, icon, 0, 0, 1, 0, 0xF000F0);
        tessellator.draw();
    }

    private static Model get(boolean upperHalf) {
        if (upperHalf && upper == null) upper = load("scarecrow_upper");
        if (!upperHalf && lower == null) lower = load("scarecrow_lower");
        return upperHalf ? upper : lower;
    }

    private static Model getBeacon(boolean upperHalf) {
        if (upperHalf && beaconUp == null) beaconUp = load("maid_beacon_up");
        if (!upperHalf && beaconDown == null) beaconDown = load("maid_beacon_down");
        return upperHalf ? beaconUp : beaconDown;
    }

    private static Model load(String name) {
        InputStream stream = null;
        try {
            stream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(
                    TouhouLittleMaid.MOD_ID, "models/block/" + name + ".json")).getInputStream();
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, "UTF-8")).getAsJsonObject();
            Model result = new Model();
            JsonArray elements = root.getAsJsonArray("elements");
            if (elements != null) for (JsonElement value : elements) result.elements.add(Element.read(value.getAsJsonObject()));
            return result;
        } catch (Exception error) {
            TouhouLittleMaid.LOGGER.error("Could not load original scarecrow model {}", name, error);
            return null;
        } finally {
            if (stream != null) try { stream.close(); } catch (Exception ignored) { }
        }
    }

    private static final class Model {
        final List<Element> elements = new ArrayList<Element>();

        void render(Tessellator t, IIcon icon, int facing, double baseX, double baseY, double baseZ, int light) {
            for (Element element : elements) element.render(t, icon, facing, baseX, baseY, baseZ, light);
        }
    }

    private static final class Element {
        final double[] min = new double[3], max = new double[3], origin = new double[]{.5, .5, .5};
        final List<Face> faces = new ArrayList<Face>();
        char axis;
        double angle;

        static Element read(JsonObject json) {
            Element result = new Element();
            double[] from = vector(json.getAsJsonArray("from")), to = vector(json.getAsJsonArray("to"));
            for (int i = 0; i < 3; i++) { result.min[i] = Math.min(from[i], to[i]) / 16.0; result.max[i] = Math.max(from[i], to[i]) / 16.0; }
            if (json.has("rotation")) {
                JsonObject rotation = json.getAsJsonObject("rotation");
                result.angle = rotation.get("angle").getAsDouble();
                result.axis = rotation.get("axis").getAsString().charAt(0);
                double[] pivot = vector(rotation.getAsJsonArray("origin"));
                for (int i = 0; i < 3; i++) result.origin[i] = pivot[i] / 16.0;
            }
            JsonObject faces = json.getAsJsonObject("faces");
            for (Map.Entry<String, JsonElement> entry : faces.entrySet())
                result.faces.add(Face.read(entry.getKey(), entry.getValue().getAsJsonObject()));
            return result;
        }

        void render(Tessellator t, IIcon icon, int facing, double bx, double by, double bz, int light) {
            for (Face face : faces) {
                double[][] vertices = vertices(face.direction, min, max);
                for (double[] vertex : vertices) {
                    rotate(vertex, origin, axis, angle);
                    rotate(vertex, new double[]{.5, 0, .5}, 'y', facing * 90.0);
                }
                double[] normal = normal(vertices);
                float shade = normal[1] > .5 ? 1.0F : normal[1] < -.5 ? .5F
                        : Math.abs(normal[2]) >= Math.abs(normal[0]) ? .8F : .6F;
                t.setBrightness(light); t.setColorOpaque_F(shade, shade, shade);
                double[][] uv = face.uv(icon);
                for (int i = 0; i < 4; i++)
                    t.addVertexWithUV(bx + vertices[i][0], by + vertices[i][1], bz + vertices[i][2], uv[i][0], uv[i][1]);
            }
        }
    }

    private static final class Face {
        String direction;
        double[] values;
        int rotation;

        static Face read(String direction, JsonObject json) {
            Face result = new Face(); result.direction = direction;
            result.values = vector(json.getAsJsonArray("uv"));
            result.rotation = json.has("rotation") ? json.get("rotation").getAsInt() : 0;
            return result;
        }

        double[][] uv(IIcon icon) {
            double u1=icon.getInterpolatedU(values[0]),v1=icon.getInterpolatedV(values[1]);
            double u2=icon.getInterpolatedU(values[2]),v2=icon.getInterpolatedV(values[3]);
            double[][] base={{u2,v2},{u1,v2},{u1,v1},{u2,v1}}, result=new double[4][2];
            int shift=Math.floorMod(rotation/90,4);
            for(int i=0;i<4;i++)result[i]=base[(i+shift)&3];
            return result;
        }
    }

    private static double[] vector(JsonArray array) {
        double[] result = new double[array.size()];
        for (int i=0;i<array.size();i++) result[i]=array.get(i).getAsDouble();
        return result;
    }

    private static double[][] vertices(String face,double[] a,double[] b){
        if("north".equals(face))return new double[][]{{b[0],a[1],a[2]},{a[0],a[1],a[2]},{a[0],b[1],a[2]},{b[0],b[1],a[2]}};
        if("south".equals(face))return new double[][]{{a[0],a[1],b[2]},{b[0],a[1],b[2]},{b[0],b[1],b[2]},{a[0],b[1],b[2]}};
        if("west".equals(face))return new double[][]{{a[0],a[1],a[2]},{a[0],a[1],b[2]},{a[0],b[1],b[2]},{a[0],b[1],a[2]}};
        if("east".equals(face))return new double[][]{{b[0],a[1],b[2]},{b[0],a[1],a[2]},{b[0],b[1],a[2]},{b[0],b[1],b[2]}};
        if("down".equals(face))return new double[][]{{a[0],a[1],a[2]},{b[0],a[1],a[2]},{b[0],a[1],b[2]},{a[0],a[1],b[2]}};
        return new double[][]{{a[0],b[1],b[2]},{b[0],b[1],b[2]},{b[0],b[1],a[2]},{a[0],b[1],a[2]}};
    }

    private static void rotate(double[] point,double[] origin,char axis,double degrees){
        if(degrees==0)return;double radians=Math.toRadians(degrees),c=Math.cos(radians),s=Math.sin(radians);
        double x=point[0]-origin[0],y=point[1]-origin[1],z=point[2]-origin[2];
        if(axis=='x'){point[1]=origin[1]+y*c-z*s;point[2]=origin[2]+y*s+z*c;}
        else if(axis=='y'){point[0]=origin[0]+x*c+z*s;point[2]=origin[2]-x*s+z*c;}
        else if(axis=='z'){point[0]=origin[0]+x*c-y*s;point[1]=origin[1]+x*s+y*c;}
    }

    private static double[] normal(double[][] v){
        double ax=v[1][0]-v[0][0],ay=v[1][1]-v[0][1],az=v[1][2]-v[0][2];
        double bx=v[2][0]-v[0][0],by=v[2][1]-v[0][1],bz=v[2][2]-v[0][2];
        double x=ay*bz-az*by,y=az*bx-ax*bz,z=ax*by-ay*bx,length=Math.sqrt(x*x+y*y+z*z);
        return length==0?new double[]{0,1,0}:new double[]{x/length,y/length,z/length};
    }
}
