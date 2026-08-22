package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.crafting.LegacyAltarRecipes;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFilm;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

/** 1.7 altar: six-slot offering inventory, absorbed Power, crafting and film resurrection. */
public final class BlockAltar extends BlockContainer {
    public BlockAltar() { super(Material.rock); setBlockName(TouhouLittleMaid.MOD_ID + ".altar"); setBlockTextureName(TouhouLittleMaid.MOD_ID + ":altar"); setHardness(2); setResistance(2); setCreativeTab(CreativeTabs.tabDecorations); }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityAltar(); }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z); if (!(tile instanceof TileEntityAltar)) return false;
        TileEntityAltar altar = (TileEntityAltar) tile;
        if (!player.isSneaking()) { if (!world.isRemote) player.displayGUIChest(altar); return true; }
        if (!world.isRemote) craft(world, x, y, z, player, altar); return true;
    }

    private void craft(World world, int x, int y, int z, EntityPlayer player, TileEntityAltar altar) {
        ItemStack[] inputs = new ItemStack[altar.getSizeInventory()];
        ItemStack film = null;
        for (int n = 0; n < inputs.length; n++) { inputs[n] = altar.getStackInSlot(n); if (ItemFilm.hasMaidData(inputs[n])) film = inputs[n]; }
        if (film != null && matchesRebirth(inputs)) {
            if(!ItemFilm.canRestore(film)){status(player,altar,"This maid is already loaded");return;}
            if (!altar.consumePower(500)) { status(player, altar, "Not enough Power (500 required)"); return; }
            if(ItemFilm.filmToMaid(film, world, x, y + 1, z, player)){clear(altar);success(world,x,y,z,player);}return;
        }
        LegacyAltarRecipes.Recipe recipe = LegacyAltarRecipes.find(inputs);
        if (recipe == null) { status(player, altar, "No matching altar recipe"); return; }
        if (!altar.consumePower(recipe.cost())) { status(player, altar, "Not enough Power (" + recipe.cost() + " required)"); return; }
        clear(altar);if(recipe.isLightning())world.addWeatherEffect(new net.minecraft.entity.effect.EntityLightningBolt(world,x+.5D,y+1,z+.5D));else player.entityDropItem(recipe.output(), 0.5F);success(world, x, y, z, player);
    }

    private boolean matchesRebirth(ItemStack[] in) {
        boolean film=false, lapis=false, gold=false, red=false, iron=false, coal=false;
        for (ItemStack s : in) if (s != null) {
            if (ItemFilm.hasMaidData(s)) film=true; else if (s.getItem()==Items.dye && s.getItemDamage()==4) lapis=true;
            else if (s.getItem()==Items.gold_ingot) gold=true; else if (s.getItem()==Items.redstone) red=true;
            else if (s.getItem()==Items.iron_ingot) iron=true; else if (s.getItem()==Items.coal) coal=true; else return false;
        }
        return film && lapis && gold && red && iron && coal;
    }
    private void clear(TileEntityAltar altar) { for (int n=0;n<altar.getSizeInventory();n++) altar.setInventorySlotContents(n,null); }
    private void success(World world,int x,int y,int z,EntityPlayer player) { world.playSoundEffect(x+.5,y+.5,z+.5,"random.levelup",1,1); status(player,(TileEntityAltar)world.getTileEntity(x,y,z),"Craft complete"); }
    private void status(EntityPlayer player, TileEntityAltar altar, String text) { player.addChatMessage(new ChatComponentText("Altar: " + text + "; Power=" + (int)altar.getPower())); }

    @Override public void breakBlock(World world,int x,int y,int z,net.minecraft.block.Block block,int meta) {
        TileEntity tile=world.getTileEntity(x,y,z); if(tile instanceof TileEntityAltar) for(int n=0;n<6;n++){ ItemStack stack=((TileEntityAltar)tile).getStackInSlot(n); if(stack!=null) world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack)); }
        super.breakBlock(world,x,y,z,block,meta);
    }
}
