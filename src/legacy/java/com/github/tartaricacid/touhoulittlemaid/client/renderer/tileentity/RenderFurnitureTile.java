package com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity;

import com.github.tartaricacid.touhoulittlemaid.tileentity.*;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

/** Selects the matching original Bedrock model for ordinary furniture tiles. */
public final class RenderFurnitureTile extends TileEntitySpecialRenderer {
    @Override public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        String name;
        String texture;
        if (tile instanceof TileEntityKeyboard) name = texture = "keyboard";
        else if (tile instanceof TileEntityBookshelf) name = texture = "bookshelf";
        else if (tile instanceof TileEntityComputer) name = texture = "computer";
        else if (tile instanceof TileEntityShrine) name = texture = "shrine";
        else if (tile instanceof TileEntityPicnicMat) {
            if(!((TileEntityPicnicMat)tile).isCenter())return;
            name = texture = "picnic_mat";
        }
        else if (tile instanceof TileEntitySnackCabinet) name = texture = "snack_cabinet";
        else if (tile instanceof TileEntityMaidBed) {
            String colour = bedColour(((TileEntityMaidBed) tile).getColor());
            name = "maid_bed/" + colour; texture = name;
        }
        else return;
        LegacyBedrockTileModels.INSTANCE.render(tile, x, y, z, name, texture);
        if(tile instanceof TileEntityPicnicMat)renderPicnicFood((TileEntityPicnicMat)tile,x,y,z);
    }

    private void renderPicnicFood(TileEntityPicnicMat tile,double x,double y,double z){
        if(tile.getWorldObj()==null)return;
        float[][] positions={{-.6F,-1.5F,1.4125F},{.15F,-1.2F,1.4125F},{.55F,-1.6F,1.4125F},{-.5F,1.65F,1.4125F},{.375F,1.575F,1.4125F},{-.05F,1.2F,1.25F}};
        org.lwjgl.opengl.GL11.glPushAttrib(org.lwjgl.opengl.GL11.GL_ALL_ATTRIB_BITS);org.lwjgl.opengl.GL11.glPushMatrix();
        try{
            org.lwjgl.opengl.GL11.glTranslated(x+.5,y+1.5,z+.5);org.lwjgl.opengl.GL11.glScalef(-1,-1,1);
            org.lwjgl.opengl.GL11.glRotatef((tile.getBlockMetadata()&3)*90-180,0,1,0);
            org.lwjgl.opengl.GL11.glRotatef(-90,1,0,0);
            for(int slot=3;slot<9;slot++){
                net.minecraft.item.ItemStack stack=tile.getStackInSlot(slot);if(stack==null)continue;
                org.lwjgl.opengl.GL11.glPushMatrix();
                try{
                    float[] p=positions[slot-3];org.lwjgl.opengl.GL11.glTranslatef(p[0],p[1],p[2]);org.lwjgl.opengl.GL11.glScalef(.4F,.4F,.4F);
                    net.minecraft.entity.item.EntityItem item=new net.minecraft.entity.item.EntityItem(tile.getWorldObj(),0,0,0,stack.copy());item.hoverStart=0;
                    net.minecraft.client.renderer.entity.RenderManager.instance.renderEntityWithPosYaw(item,0,0,0,0,0);
                }finally{org.lwjgl.opengl.GL11.glPopMatrix();}
            }
        }finally{org.lwjgl.opengl.GL11.glPopMatrix();org.lwjgl.opengl.GL11.glPopAttrib();}
    }

    private static String bedColour(int dye) {
        switch (dye) {
            case 15: return "black";
            case 13: return "green";
            case 11: return "blue";
            case 10: return "purple";
            case 6: return "pink";
            case 4: return "yellow";
            default: return "white";
        }
    }
}
