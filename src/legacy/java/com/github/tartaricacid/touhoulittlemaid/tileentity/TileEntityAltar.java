package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

/** Six offering slots and local Power storage for the 1.7 altar adaptation. */
public final class TileEntityAltar extends TileEntityInventory {
    public static final float MAX_POWER = 5000.0F;
    private float power;
    private int tick;
    private boolean formed, pedestal, renderCore;
    private int originX,originY,originZ,facing,originalMeta;
    private String originalBlock="minecraft:air";

    public boolean isFormed(){return formed;}
    public boolean isPedestal(){return pedestal;}
    public boolean isRenderCore(){return renderCore;}
    public int getFacing(){return facing;}
    public int getOriginX(){return originX;} public int getOriginY(){return originY;} public int getOriginZ(){return originZ;}
    public net.minecraft.block.Block getOriginalBlock(){net.minecraft.block.Block b=(net.minecraft.block.Block)net.minecraft.block.Block.blockRegistry.getObject(originalBlock);return b==null?net.minecraft.init.Blocks.air:b;}
    public int getOriginalMeta(){return originalMeta;}
    public void configureStructure(int x,int y,int z,int direction,net.minecraft.block.Block block,int meta,boolean offering,boolean core){
        formed=true;originX=x;originY=y;originZ=z;facing=direction&3;
        originalBlock=net.minecraft.block.Block.blockRegistry.getNameForObject(block);originalMeta=meta;
        pedestal=offering;renderCore=core;markDirty();
    }
    public boolean sameStructure(TileEntityAltar other){return other!=null&&formed&&other.formed&&originX==other.originX&&originY==other.originY&&originZ==other.originZ&&facing==other.facing;}
    public java.util.List<TileEntityAltar> getStructureTiles(){
        java.util.List<TileEntityAltar> result=new java.util.ArrayList<TileEntityAltar>();
        if(!formed||worldObj==null)return result;
        for(int[] p:com.github.tartaricacid.touhoulittlemaid.block.LegacyAltarStructure.template(facing)){
            int x=originX+p[0],y=originY+p[1],z=originZ+p[2];
            if(!worldObj.blockExists(x,y,z))return java.util.Collections.emptyList();
            net.minecraft.tileentity.TileEntity t=worldObj.getTileEntity(x,y,z);
            if(!(t instanceof TileEntityAltar)||!sameStructure((TileEntityAltar)t))return java.util.Collections.emptyList();
            result.add((TileEntityAltar)t);
        }return result;
    }

    public TileEntityAltar() { super(6, "container.tlm.altar"); }
    @Override public AxisAlignedBB getRenderBoundingBox() {
        // The source geometry spans a whole structure, not the tile's unit cube.
        return AxisAlignedBB.getBoundingBox(xCoord - 9, yCoord - 5, zCoord - 9,
                xCoord + 9, yCoord + 5, zCoord + 9);
    }
    public float getPower() { return power; }
    /** A recipe uses one item from each occupied pedestal, never a whole stack. */
    public void consumeOfferings() {
        for (int slot = 0; slot < getSizeInventory(); slot++) {
            if (getStackInSlot(slot) != null) decrStackSize(slot, 1);
        }
    }
    public boolean consumePower(float amount) {
        if (amount < 0 || Float.isNaN(amount) || Float.isInfinite(amount) || power < amount) return false;
        power -= amount; markDirty(); return true;
    }

    @Override public void updateEntity() {
        if(worldObj==null||worldObj.isRemote)return;
        if(formed){
            if(++tick%20!=0)return;
            for(int[] p:com.github.tartaricacid.touhoulittlemaid.block.LegacyAltarStructure.template(facing))
                if(!worldObj.blockExists(originX+p[0],originY+p[1],originZ+p[2]))return;
            if(getStructureTiles().size()!=38)((com.github.tartaricacid.touhoulittlemaid.block.BlockAltar)com.github.tartaricacid.touhoulittlemaid.init.ModBlocks.ALTAR).restoreOrphan(this);
            return;
        }
        if (++tick % 5 != 0) return;
        @SuppressWarnings("unchecked") List<EntityPowerPoint> points = worldObj.getEntitiesWithinAABB(EntityPowerPoint.class,
                AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord - 1, zCoord - 2, xCoord + 3, yCoord + 3, zCoord + 3));
        for (EntityPowerPoint point : points) {
            if (point.isDead) continue;
            int value = Math.max(1, point.getValue());
            if (power + value <= MAX_POWER) {
                power += value;
                point.setDead();
                markDirty();
            }
        }
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        power = tag.getFloat("Power");
        if (Float.isNaN(power) || Float.isInfinite(power)) power = 0;
        power = Math.max(0, Math.min(MAX_POWER, power));
        formed=tag.getBoolean("LegacyAltarFormed");pedestal=tag.getBoolean("AltarPedestal");renderCore=tag.getBoolean("AltarRenderCore");
        originX=tag.getInteger("AltarOriginX");originY=tag.getInteger("AltarOriginY");originZ=tag.getInteger("AltarOriginZ");facing=tag.getInteger("AltarFacing")&3;
        originalBlock=tag.hasKey("AltarOriginalBlock",8)?tag.getString("AltarOriginalBlock"):"minecraft:air";originalMeta=tag.getInteger("AltarOriginalMeta")&15;
    }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setFloat("Power", power);
        tag.setBoolean("LegacyAltarFormed",formed);tag.setBoolean("AltarPedestal",pedestal);tag.setBoolean("AltarRenderCore",renderCore);
        tag.setInteger("AltarOriginX",originX);tag.setInteger("AltarOriginY",originY);tag.setInteger("AltarOriginZ",originZ);tag.setInteger("AltarFacing",facing);
        tag.setString("AltarOriginalBlock",originalBlock);tag.setInteger("AltarOriginalMeta",originalMeta);
    }
}
