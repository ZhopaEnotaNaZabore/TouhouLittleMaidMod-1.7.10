package com.github.tartaricacid.touhoulittlemaid.client.renderer;

import net.minecraft.item.*;
import net.minecraft.init.Items;
import org.lwjgl.opengl.GL11;

/** Adapts 1.7 ItemRenderer's icon-space transform to the modern locator grip.
 * Display values are the Minecraft 1.20.1 bow/handheld/generated item models. */
public final class LegacyVanillaHandTransform {
    public enum Kind { BOW, HANDHELD, GENERATED }
    private LegacyVanillaHandTransform() { }
    public static void apply(ItemStack stack, boolean left) {
        Item item=stack.getItem();
        apply(item==Items.bow?Kind.BOW:item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemHoe?Kind.HANDHELD:Kind.GENERATED,left);
    }
    public static void apply(Kind kind, boolean left) {
        if(kind==Kind.BOW) {
            GL11.glTranslatef((left?1:-1)/16F,-2/16F,2.5F/16F);
            GL11.glRotatef(-80,1,0,0);GL11.glRotatef(left?280:260,0,1,0);GL11.glRotatef(-40,0,0,1);
            GL11.glScalef(.9F,.9F,.9F);
        } else if(kind==Kind.HANDHELD) {
            GL11.glTranslatef(0,4/16F,.5F/16F);
            GL11.glRotatef(-90,0,1,0);GL11.glRotatef(55,0,0,1);GL11.glScalef(.85F,.85F,.85F);
        } else {
            GL11.glTranslatef(0,3/16F,1/16F);GL11.glScalef(.55F,.55F,.55F);
        }
        // 1.7 renderItemIn2D puts maxU at x=0, whereas generated modern
        // item geometry puts minU there. Turn the centered icon around Y:
        // this reverses U and thickness without reflecting its winding/normals.
        // Without this conversion swords point down and bows face the wearer.
        GL11.glRotatef(180,0,1,0);
        // The icon is a unit quad with thickness [-1/16,0], centered in the hand.
        GL11.glTranslatef(-.5F,-.5F,.03125F);
        undoEquippedIconTransform();
    }
    private static void undoEquippedIconTransform() {
        // Inverse of ItemRenderer.renderItem's T(0,-.3,0) S(1.5)
        // Ry(50) Rz(335) T(-.9375,-.0625,0), in reverse order.
        GL11.glTranslatef(.9375F,.0625F,0);
        GL11.glRotatef(25,0,0,1);GL11.glRotatef(-50,0,1,0);
        GL11.glScalef(2F/3F,2F/3F,2F/3F);GL11.glTranslatef(0,.3F,0);
    }
}
