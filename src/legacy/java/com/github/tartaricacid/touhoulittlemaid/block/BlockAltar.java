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
    boolean restoringStructure;
    private boolean suppressDrops;
    public BlockAltar() { super(Material.rock); setBlockName(TouhouLittleMaid.MOD_ID + ".altar"); setBlockTextureName(TouhouLittleMaid.MOD_ID + ":altar"); setHardness(2); setResistance(2); setCreativeTab(CreativeTabs.tabDecorations); }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityAltar(); }
    @Override public boolean isOpaqueCube(){return false;} @Override public boolean renderAsNormalBlock(){return false;} @Override public int getRenderType(){return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE;}
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z); if (!(tile instanceof TileEntityAltar)) return false;
        TileEntityAltar altar = (TileEntityAltar) tile;
        if(altar.isFormed()) {
            if(!world.isRemote)useFormed(world,player,altar);
            return true;
        }
        if (!player.isSneaking()) { if (!world.isRemote) player.openGui(TouhouLittleMaid.instance, com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy.ALTAR_GUI_ID, world, x, y, z); return true; }
        if (!world.isRemote) craft(world, x, y, z, player, altar); return true;
    }

    private void useFormed(World world,EntityPlayer player,TileEntityAltar altar){
        if(!altar.isPedestal()||altar.getStructureTiles().size()!=38)return;
        ItemStack held=player.getCurrentEquippedItem();
        if(player.isSneaking()||held==null){
            ItemStack out=altar.decrStackSize(0,1);
            if(out!=null&&!player.inventory.addItemStackToInventory(out))player.entityDropItem(out,0);
        }else if(altar.getStackInSlot(0)==null){
            ItemStack one=held.copy();one.stackSize=1;altar.setInventorySlotContents(0,one);
            if(!player.capabilities.isCreativeMode&&--held.stackSize==0)player.inventory.setInventorySlotContents(player.inventory.currentItem,null);
        }else return;
        java.util.List<TileEntityAltar> pedestals=new java.util.ArrayList<TileEntityAltar>();
        for(TileEntityAltar t:altar.getStructureTiles())if(t.isPedestal())pedestals.add(t);
        if(pedestals.size()!=6)return;
        ItemStack[] inputs=new ItemStack[6];ItemStack film=null;
        for(int i=0;i<6;i++){inputs[i]=pedestals.get(i).getStackInSlot(0);if(ItemFilm.hasMaidData(inputs[i]))film=inputs[i];}
        boolean rebirth=film!=null&&matchesRebirth(inputs);
        LegacyAltarRecipes.Recipe recipe=rebirth?null:LegacyAltarRecipes.find(inputs);
        if(!rebirth&&recipe==null)return;
        float cost=rebirth?.5F:recipe.cost()/1000F;
        com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower power=com.github.tartaricacid.touhoulittlemaid.capability.LegacyPlayerPower.get(player);
        if(power.get()<cost){player.addChatMessage(new net.minecraft.util.ChatComponentTranslation("message.touhou_little_maid.altar.not_enough_power"));return;}
        int x=altar.getOriginX()+3,y=altar.getOriginY()+2,z=altar.getOriginZ()+3;
        boolean spawned;
        if(rebirth)spawned=ItemFilm.filmToMaid(film.copy(),world,x,y,z,player);
        else if(recipe.isLightning())spawned=world.addWeatherEffect(new net.minecraft.entity.effect.EntityLightningBolt(world,x+.5,y,z+.5));
        else if(recipe.output().getItem()==com.github.tartaricacid.touhoulittlemaid.init.ModItems.SPAWN_BOX)
            spawned=com.github.tartaricacid.touhoulittlemaid.item.ItemSpawnBox.spawnWithMaid(world,x+.5,y,z+.5);
        else spawned=world.spawnEntityInWorld(new EntityItem(world,x+.5,y,z+.5,recipe.output()));
        if(!spawned)return;
        power.take(cost);
        if(player instanceof net.minecraft.entity.player.EntityPlayerMP)
            com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler.channel.sendTo(new com.github.tartaricacid.touhoulittlemaid.network.message.MessagePlayerPower(power.get()),(net.minecraft.entity.player.EntityPlayerMP)player);
        for(TileEntityAltar t:pedestals)t.decrStackSize(0,1);
        world.playSoundEffect(x+.5,y,z+.5,"touhou_little_maid:block.altar_craft",1,1);
    }

    public void restoreOrphan(TileEntityAltar altar){
        World world=altar.getWorldObj();if(world==null||world.isRemote)return;
        suppressDrops=true;
        try{world.setBlock(altar.xCoord,altar.yCoord,altar.zCoord,altar.getOriginalBlock(),altar.getOriginalMeta(),3);}
        finally{suppressDrops=false;}
    }

    @Override public boolean removedByPlayer(World world,EntityPlayer player,int x,int y,int z,boolean harvest){
        suppressDrops=player.capabilities.isCreativeMode;
        try{return super.removedByPlayer(world,player,x,y,z,harvest);}finally{suppressDrops=false;}
    }
    @Override public net.minecraft.item.Item getItemDropped(int meta,java.util.Random random,int fortune){return null;}
    @Override public ItemStack getPickBlock(net.minecraft.util.MovingObjectPosition hit,World world,int x,int y,int z){
        TileEntity t=world.getTileEntity(x,y,z);
        if(t instanceof TileEntityAltar&&((TileEntityAltar)t).isFormed()){
            TileEntityAltar a=(TileEntityAltar)t;return new ItemStack(a.getOriginalBlock(),1,a.getOriginalMeta());
        }return new ItemStack(this);
    }

    private void craft(World world, int x, int y, int z, EntityPlayer player, TileEntityAltar altar) {
        ItemStack[] inputs = new ItemStack[altar.getSizeInventory()];
        ItemStack film = null;
        for (int n = 0; n < inputs.length; n++) { inputs[n] = altar.getStackInSlot(n); if (ItemFilm.hasMaidData(inputs[n])) film = inputs[n]; }
        if (film != null && matchesRebirth(inputs)) {
            if(!ItemFilm.canRestore(film)){status(player,altar,"This maid is already loaded");return;}
            if (altar.getPower() < 500) { status(player, altar, "Not enough Power (500 required)"); return; }
            // Restore from a copy: commit the real offerings only after spawning succeeds.
            if (ItemFilm.filmToMaid(film.copy(), world, x, y + 1, z, player)) {
                altar.consumePower(500); altar.consumeOfferings(); success(world,x,y,z,player);
            }
            return;
        }
        LegacyAltarRecipes.Recipe recipe = LegacyAltarRecipes.find(inputs);
        if (recipe == null) { status(player, altar, "No matching altar recipe"); return; }
        if (altar.getPower() < recipe.cost()) { status(player, altar, "Not enough Power (" + recipe.cost() + " required)"); return; }
        boolean spawned = recipe.isLightning()
                ? world.addWeatherEffect(new net.minecraft.entity.effect.EntityLightningBolt(world,x+.5D,y+1,z+.5D))
                : world.spawnEntityInWorld(new EntityItem(world,x+.5D,y+1,z+.5D,recipe.output()));
        if (!spawned) return;
        altar.consumePower(recipe.cost()); altar.consumeOfferings(); success(world, x, y, z, player);
    }

    private boolean matchesRebirth(ItemStack[] in) {
        boolean film=false, lapis=false, gold=false, red=false, iron=false, coal=false;
        for (ItemStack s : in) if (s != null) {
            if (ItemFilm.hasMaidData(s)) film=true; else if (s.getItem()==Items.dye && s.getItemDamage()==4) lapis=true;
            else if (s.getItem()==Items.gold_ingot) gold=true; else if (s.getItem()==Items.redstone) red=true;
            else if (s.getItem()==Items.iron_ingot) iron=true; else if (s.getItem()==Items.coal && s.getItemDamage()==0) coal=true; else return false;
        }
        return film && lapis && gold && red && iron && coal;
    }
    private void success(World world,int x,int y,int z,EntityPlayer player) { world.playSoundEffect(x+.5,y+.5,z+.5,"random.levelup",1,1); status(player,(TileEntityAltar)world.getTileEntity(x,y,z),"Craft complete"); }
    private void status(EntityPlayer player, TileEntityAltar altar, String text) { player.addChatMessage(new ChatComponentText("Altar: " + text + "; Power=" + (int)altar.getPower())); }

    @Override public void breakBlock(World world,int x,int y,int z,net.minecraft.block.Block block,int meta) {
        TileEntity current=world.getTileEntity(x,y,z);
        if(restoringStructure||world.restoringBlockSnapshots){super.breakBlock(world,x,y,z,block,meta);return;}
        if(!world.isRemote&&current instanceof TileEntityAltar&&((TileEntityAltar)current).isFormed()){
            TileEntityAltar altar=(TileEntityAltar)current;
            restoringStructure=true;
            try {
                for(int[] p:LegacyAltarStructure.template(altar.getFacing())){
                    int px=altar.getOriginX()+p[0],py=altar.getOriginY()+p[1],pz=altar.getOriginZ()+p[2];
                    if(!world.blockExists(px,py,pz))continue;
                    TileEntity te=world.getTileEntity(px,py,pz);
                    if(!(te instanceof TileEntityAltar)||!altar.sameStructure((TileEntityAltar)te))continue;
                    TileEntityAltar part=(TileEntityAltar)te;
                    for(int s=0;s<part.getSizeInventory();s++){ItemStack item=part.getStackInSlot(s);if(item!=null)world.spawnEntityInWorld(new EntityItem(world,px+.5,py+.5,pz+.5,item.copy()));}
                    if(px!=x||py!=y||pz!=z)world.setBlock(px,py,pz,part.getOriginalBlock(),part.getOriginalMeta(),3);
                }
                if(!suppressDrops&&altar.getOriginalBlock()!=net.minecraft.init.Blocks.air)
                    world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,new ItemStack(altar.getOriginalBlock(),1,altar.getOriginalBlock().damageDropped(altar.getOriginalMeta()))));
            } finally {restoringStructure=false;}
            super.breakBlock(world,x,y,z,block,meta);return;
        }
        if(!world.isRemote&&!suppressDrops)world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,new ItemStack(this)));
        TileEntity tile=world.getTileEntity(x,y,z); if(!world.isRemote && tile instanceof TileEntityAltar) for(int n=0;n<6;n++){ ItemStack stack=((TileEntityAltar)tile).getStackInSlot(n); if(stack!=null) world.spawnEntityInWorld(new EntityItem(world,x+.5,y+.5,z+.5,stack)); }
        super.breakBlock(world,x,y,z,block,meta);
    }
}
