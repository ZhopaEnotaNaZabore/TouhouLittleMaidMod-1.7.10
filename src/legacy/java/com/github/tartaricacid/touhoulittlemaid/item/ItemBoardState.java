package com.github.tartaricacid.touhoulittlemaid.item;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.Set;

/** Portable, author-labelled snapshot for one of the three board types. */
public final class ItemBoardState extends Item {
    public static final String DATA_TAG="BoardStateData",DESC_TAG="BoardStateDesc",AUTHOR_TAG="BoardStateAuthor";
    public enum Type{GOMOKU,CCHESS,WCHESS}private final Type type;
    public ItemBoardState(String name,Type type){this.type=type;setUnlocalizedName(TouhouLittleMaid.MOD_ID+"."+name);setTextureName(TouhouLittleMaid.MOD_ID+":"+name);setMaxStackSize(1);setCreativeTab(CreativeTabs.tabMisc);}
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side,float hx,float hy,float hz){TileEntity tile=world.getTileEntity(x,y,z);if(!matches(tile))return false;if(world.isRemote)return true;
        if(stack.hasTagCompound()&&stack.getTagCompound().hasKey(DATA_TAG,10)){NBTTagCompound saved=(NBTTagCompound)stack.getTagCompound().getCompoundTag(DATA_TAG).copy();saved.setInteger("x",x);saved.setInteger("y",y);saved.setInteger("z",z);saved.setString("SitId","");tile.readFromNBT(saved);tile.markDirty();world.markBlockForUpdate(x,y,z);player.addChatMessage(new ChatComponentText("Board state restored"));}
        else{NBTTagCompound saved=new NBTTagCompound();tile.writeToNBT(saved);saved.removeTag("x");saved.removeTag("y");saved.removeTag("z");saved.removeTag("SitId");NBTTagCompound root=new NBTTagCompound();root.setTag(DATA_TAG,saved);root.setString(DESC_TAG,type.name());root.setString(AUTHOR_TAG,player.getCommandSenderName());stack.setTagCompound(root);player.addChatMessage(new ChatComponentText("Board state captured"));}return true;}
    private boolean matches(TileEntity tile){return type==Type.GOMOKU&&tile instanceof TileEntityGomoku||type==Type.CCHESS&&tile instanceof TileEntityCChess||type==Type.WCHESS&&tile instanceof TileEntityWChess;}
    @Override public boolean hasEffect(ItemStack stack,int pass){return stack.hasTagCompound()&&stack.getTagCompound().hasKey(DATA_TAG,10);}
}
