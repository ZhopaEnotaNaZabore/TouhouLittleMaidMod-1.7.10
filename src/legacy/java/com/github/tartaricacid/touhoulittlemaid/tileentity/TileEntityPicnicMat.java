package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.LegacyNbtMigration;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.UUID;

public final class TileEntityPicnicMat extends TileEntityInventory {
    @Override public net.minecraft.util.AxisAlignedBB getRenderBoundingBox() {
        return net.minecraft.util.AxisAlignedBB.getBoundingBox(xCoord - 3,yCoord + 0,zCoord - 3,xCoord + 3,yCoord + 1,zCoord + 3);
    }

    private final String[] sitUuids = {"", "", "", ""};
    private int centerX, centerY, centerZ;
    private boolean multiblock;
    private int integrityTicks;

    // S2DPacketOpenWindow in 1.7.10 limits inventory names to 32 characters.
    public TileEntityPicnicMat() { super(9, "container.tlm.picnic_mat"); }
    @Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return isCenter() && stack != null && stack.getItem() instanceof ItemFood; }

    public void setCenterPos(int x, int y, int z) { centerX=x;centerY=y;centerZ=z;multiblock=true;markDirty(); }
    public int getCenterX(){return centerX;} public int getCenterY(){return centerY;} public int getCenterZ(){return centerZ;}
    public boolean isCenter(){return xCoord==centerX&&yCoord==centerY&&zCoord==centerZ;}
    public int insertFood(ItemStack offered){
        if(!isCenter()||offered==null||offered.stackSize<=0||!(offered.getItem() instanceof ItemFood))return 0;
        int remaining=offered.stackSize;
        for(int pass=0;pass<2;pass++)for(int slot=0;slot<getSizeInventory()&&remaining>0;slot++){
            ItemStack old=getStackInSlot(slot);
            if(pass==0&&old!=null&&old.isItemEqual(offered)&&ItemStack.areItemStackTagsEqual(old,offered)){
                int count=Math.min(remaining,Math.max(0,Math.min(getInventoryStackLimit(),old.getMaxStackSize())-old.stackSize));old.stackSize+=count;remaining-=count;
            }else if(pass==1&&old==null){ItemStack copy=offered.copy();copy.stackSize=Math.min(remaining,Math.min(getInventoryStackLimit(),copy.getMaxStackSize()));setInventorySlotContents(slot,copy);remaining-=copy.stackSize;}
        }
        if(remaining!=offered.stackSize)markDirty();return offered.stackSize-remaining;
    }

    @Override public void updateEntity() {
        if (!multiblock || worldObj == null || worldObj.isRemote || ++integrityTicks % 20 != 0) return;
        // Wait for every affected chunk; absence in an unloaded chunk is not damage.
        for (int dx=-2;dx<=2;dx++) for(int dz=-2;dz<=2;dz++)
            if(!worldObj.blockExists(centerX+dx,centerY,centerZ+dz)) return;
        for (int dx=-2;dx<=2;dx++) for(int dz=-2;dz<=2;dz++) {
            net.minecraft.tileentity.TileEntity raw=worldObj.getTileEntity(centerX+dx,centerY,centerZ+dz);
            if(raw instanceof TileEntityPicnicMat) {
                TileEntityPicnicMat part=(TileEntityPicnicMat)raw;
                if(part.centerX==centerX && part.centerY==centerY && part.centerZ==centerZ) continue;
            }
            worldObj.setBlockToAir(xCoord,yCoord,zCoord);
            return;
        }
    }

    public boolean seatMaid(EntityMaid maid) {
        if (worldObj == null || worldObj.isRemote || !isCenter() || maid == null || maid.isRiding()) return false;
        for (int i = 0; i < sitUuids.length; i++) {
            Entity old = find(sitUuids[i]);
            if (old != null && old.isEntityAlive()) continue;
            int[] dx={2,-1,-1,2}, dz={2,2,-1,-1};
            EntitySit sit=new EntitySit(worldObj,centerX+dx[i],centerY+0.0625D,centerZ+dz[i],"OnHomeMeal",centerX,centerY,centerZ);
            sit.rotationYaw=(float)Math.toDegrees(Math.atan2(dz[i]<0?-1:1,dx[i]<0?-1:1))+90.0F;
            if (!worldObj.spawnEntityInWorld(sit)) return false;
            sitUuids[i]=sit.getUniqueID().toString(); markDirty(); maid.mountEntity(sit); return true;
        }
        return false;
    }

    public void removeSeats() {
        if (worldObj != null && worldObj.isRemote) return;
        for(int i=0;i<sitUuids.length;i++){Entity entity=find(sitUuids[i]);if(entity instanceof EntitySit){if(entity.riddenByEntity!=null)entity.riddenByEntity.mountEntity(null);entity.setDead();}sitUuids[i]="";}
        markDirty();
    }

    private Entity find(String id){if(worldObj==null||id==null||id.isEmpty())return null;for(Object value:worldObj.loadedEntityList){Entity e=(Entity)value;if(id.equals(e.getUniqueID().toString()))return e;}return null;}

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagCompound data=LegacyTileNbt.data(tag);
        multiblock=data.getBoolean("LegacyPicnicMultiblock");
        NBTTagCompound center=data.getCompoundTag("CenterPos");
        centerX=center.hasKey("X")?center.getInteger("X"):xCoord;
        centerY=center.hasKey("Y")?center.getInteger("Y"):yCoord;
        centerZ=center.hasKey("Z")?center.getInteger("Z"):zCoord;
        java.util.Arrays.fill(sitUuids,"");
        NBTTagList seats=data.getTagList("SitIds",11);
        for(int i=0;i<seats.tagCount()&&i<4;i++){int[] value=seats.func_150306_c(i);if(value.length==4){long most=((long)value[0]<<32)|(value[1]&0xffffffffL),least=((long)value[2]<<32)|(value[3]&0xffffffffL);sitUuids[i]=new UUID(most,least).toString();}}

    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("LegacyPicnicMultiblock",multiblock);
        NBTTagCompound center=new NBTTagCompound();center.setInteger("X",centerX);center.setInteger("Y",centerY);center.setInteger("Z",centerZ);tag.setTag("CenterPos",center);
        NBTTagList seats=new NBTTagList();for(String id:sitUuids){UUID uuid;try{uuid=id.isEmpty()?new UUID(0,0):UUID.fromString(id);}catch(IllegalArgumentException e){uuid=new UUID(0,0);}long most=uuid.getMostSignificantBits(),least=uuid.getLeastSignificantBits();seats.appendTag(new net.minecraft.nbt.NBTTagIntArray(new int[]{(int)(most>>32),(int)most,(int)(least>>32),(int)least}));}tag.setTag("SitIds",seats);
    }
}
