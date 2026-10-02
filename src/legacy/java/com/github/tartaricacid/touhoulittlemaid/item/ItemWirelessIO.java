package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Binds an inventory and toggles chest->maid / maid->chest transfer mode. */
public final class ItemWirelessIO extends Item {
    public static final String DATA="WirelessIOData";
    public ItemWirelessIO(){setUnlocalizedName(TouhouLittleMaid.MOD_ID+".wireless_io");setTextureName(TouhouLittleMaid.MOD_ID+":wireless_io");setMaxStackSize(1);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){TileEntity tile=world.getTileEntity(x,y,z);if(!(tile instanceof IInventory)||!((IInventory)tile).isUseableByPlayer(player))return false;if(!world.isRemote){NBTTagCompound root=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();NBTTagCompound data=root.hasKey(DATA,10)?root.getCompoundTag(DATA):new NBTTagCompound();data.setInteger("X",x);data.setInteger("Y",y);data.setInteger("Z",z);data.setInteger("DimensionId",player.dimension);if(player.isSneaking())captureFilter(data,(IInventory)tile);root.setTag(DATA,data);stack.setTagCompound(root);player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("message.touhou_little_maid.wireless_bound",x,y,z,data.getTagList("Filter",10).tagCount()));}return true;}
    @Override public ItemStack onItemRightClick(ItemStack stack,World world,EntityPlayer player){if(player.isSneaking()&&!world.isRemote){NBTTagCompound root=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();NBTTagCompound data=root.hasKey(DATA,10)?root.getCompoundTag(DATA):new NBTTagCompound();data.setBoolean("MaidToChest",!data.getBoolean("MaidToChest"));root.setTag(DATA,data);stack.setTagCompound(root);player.addChatMessage(new net.minecraft.util.ChatComponentTranslation(data.getBoolean("MaidToChest")?"message.touhou_little_maid.wireless_export":"message.touhou_little_maid.wireless_import"));}return stack;}
    @Override public boolean hasEffect(ItemStack stack,int pass){return stack.hasTagCompound()&&stack.getTagCompound().hasKey(DATA,10);}
    private static void captureFilter(NBTTagCompound data,IInventory inventory){NBTTagList list=new NBTTagList();for(int slot=0;slot<inventory.getSizeInventory()&&list.tagCount()<9;slot++){ItemStack source=inventory.getStackInSlot(slot);if(source==null)continue;boolean duplicate=false;for(int n=0;n<list.tagCount();n++){ItemStack old=ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(n));if(old!=null&&old.isItemEqual(source)&&ItemStack.areItemStackTagsEqual(old,source)){duplicate=true;break;}}if(!duplicate){NBTTagCompound item=new NBTTagCompound();ItemStack one=source.copy();one.stackSize=1;one.writeToNBT(item);list.appendTag(item);}}data.setTag("Filter",list);}
    public static boolean matchesFilter(NBTTagCompound data,ItemStack stack){NBTTagList list=data.getTagList("Filter",10);if(list.tagCount()==0)return true;for(int n=0;n<list.tagCount();n++){ItemStack filter=ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(n));if(filter!=null&&filter.isItemEqual(stack)&&ItemStack.areItemStackTagsEqual(filter,stack))return true;}return false;}
}
