package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import java.util.ArrayList;

/** Vanilla farming with the source's hoe/destroy and bare-hand/reset modes. */
public final class TaskFarm implements IMaidTask {
    private static final int SEARCH_RANGE = 8;
    @Override public String getId() { return TaskManager.FARM_ID; }

    @Override public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || maid.isSitting() || maid.isMaidSleeping()
                || !maid.isWorkingNow() || !maid.isPeriodicTick(20)
                || !(maid.worldObj instanceof WorldServer)) return;
        int[] target = findTarget(maid);
        if (target == null) return;
        if (maid.getDistanceSq(target[0]+.5D,target[1],target[2]+.5D) > 6.25D) {
            maid.getNavigator().tryMoveToXYZ(target[0]+.5D,target[1],target[2]+.5D,.65D);
            return;
        }
        EntityPlayer owner = maid.getOwner() instanceof EntityPlayer ? (EntityPlayer)maid.getOwner() : null;
        FakePlayer actor = FakePlayerFactory.get((WorldServer)maid.worldObj,
                new GameProfile(owner == null ? maid.getUniqueID() : owner.getUniqueID(),
                        owner == null ? "[TLM-Maid]" : owner.getCommandSenderName()));
        actor.setPositionAndRotation(maid.posX,maid.posY,maid.posZ,maid.rotationYaw,maid.rotationPitch);
        int previousSlot = actor.inventory.currentItem;
        ItemStack previous = actor.inventory.getStackInSlot(0);
        actor.inventory.currentItem = 0;
        try { workAt(maid,target[0],target[1],target[2],actor); }
        finally { actor.inventory.setInventorySlotContents(0,previous); actor.inventory.currentItem=previousSlot; }
    }

    private int[] findTarget(EntityMaid maid) {
        int cx=(int)Math.floor(maid.posX),cy=(int)Math.floor(maid.posY),cz=(int)Math.floor(maid.posZ);
        int[] nearest=null; double best=Double.MAX_VALUE;
        for(int x=cx-SEARCH_RANGE;x<=cx+SEARCH_RANGE;x++)
            for(int y=Math.max(1,cy-3);y<=Math.min(254,cy+3);y++)
                for(int z=cz-SEARCH_RANGE;z<=cz+SEARCH_RANGE;z++) {
                    if (!available(maid,x,y,z)) continue;
                    World world=maid.worldObj;
                    if (!mature(world.getBlock(x,y,z),world.getBlockMetadata(x,y,z)) && findSeed(maid,x,y,z)==null) continue;
                    double distance=maid.getDistanceSq(x+.5D,y,z+.5D);
                    if(distance<best){best=distance;nearest=new int[]{x,y,z};}
                }
        return nearest;
    }

    private boolean available(EntityMaid maid,int x,int y,int z) {
        return y>0 && y<255 && maid.worldObj.blockExists(x,y,z) && maid.worldObj.blockExists(x,y-1,z)
                && maid.isPositionWithinRestriction(x+.5D,y,z+.5D);
    }

    // Actor is explicit so both crop mutations use the same Forge permission identity.
    private boolean workAt(EntityMaid maid,int x,int y,int z,EntityPlayer actor) {
        World world=maid.worldObj;
        if(world.isRemote || world.captureBlockSnapshots || world.restoringBlockSnapshots || !available(maid,x,y,z)) return false;
        boolean harvested=false;
        Block block=world.getBlock(x,y,z); int meta=world.getBlockMetadata(x,y,z);
        if(mature(block,meta)) {
            actor.inventory.setInventorySlotContents(0,maid.getHeldItem());
            if(!world.canMineBlock(actor,x,y,z) || !actor.canPlayerEdit(x,y,z,1,maid.getHeldItem())
                    || !block.canEntityDestroy(world,x,y,z,maid)) return false;
            BlockEvent.BreakEvent event=new BlockEvent.BreakEvent(x,y,z,world,block,meta,actor);
            if(MinecraftForge.EVENT_BUS.post(event)) return false;
            if(world.getBlock(x,y,z)!=block || world.getBlockMetadata(x,y,z)!=meta) return false;
            boolean destroy=maid.getHeldItem()!=null && maid.getHeldItem().getItem() instanceof ItemHoe;
            ArrayList<ItemStack> drops=new ArrayList<ItemStack>();
            if(block==Blocks.nether_wart && !destroy) drops.add(new ItemStack(Items.nether_wart));
            else drops.addAll(block.getDrops(world,x,y,z,meta,0));
            float chance=ForgeEventFactory.fireBlockHarvesting(drops,world,block,x,y,z,meta,0,1F,false,actor);
            if(world.getBlock(x,y,z)!=block || world.getBlockMetadata(x,y,z)!=meta) return false;
            boolean changed=destroy ? world.setBlockToAir(x,y,z) : world.setBlockMetadataWithNotify(x,y,z,0,3);
            if(!changed) return false;
            for(ItemStack drop:drops) if(drop!=null && drop.stackSize>0 && world.rand.nextFloat()<chance) {
                ItemStack remaining=maid.addToMaidInventory(drop);
                if(remaining!=null && remaining.stackSize>0) maid.entityDropItem(remaining,0);
            }
            world.playAuxSFX(2001,x,y,z,Block.getIdFromBlock(block)+(meta<<12));
            harvested=true;
        }
        SeedSlot seed=findSeed(maid,x,y,z);
        boolean planted=seed!=null && plant(maid,x,y,z,seed,actor);
        if(harvested || planted) maid.swingItem();
        return harvested || planted;
    }

    private boolean plant(EntityMaid maid,int x,int y,int z,SeedSlot slot,EntityPlayer actor) {
        World world=maid.worldObj;
        ItemStack seed=slot.inventory.getStackInSlot(slot.index);
        if(!canPlant(world,x,y,z,seed)) return false;
        Block crop=plantBlock(seed);
        actor.inventory.setInventorySlotContents(0,seed.copy());
        if(!world.canMineBlock(actor,x,y-1,z) || !world.canMineBlock(actor,x,y,z)
                || !actor.canPlayerEdit(x,y,z,1,seed)) return false;
        BlockSnapshot before=BlockSnapshot.getBlockSnapshot(world,x,y,z);
        // No neighbour notifications until Forge has accepted the placement.
        if(!world.setBlock(x,y,z,crop,0,2)) return false;
        boolean accepted=false;
        try {
            accepted=!MinecraftForge.EVENT_BUS.post(new BlockEvent.PlaceEvent(before,world.getBlock(x,y-1,z),actor));
            return accepted && world.getBlock(x,y,z)==crop && world.getBlockMetadata(x,y,z)==0;
        } finally {
            if(!accepted) {
                world.restoringBlockSnapshots=true;
                try { before.restore(true,false); }
                finally { world.restoringBlockSnapshots=false; }
            } else if(world.getBlock(x,y,z)==crop && world.getBlockMetadata(x,y,z)==0) {
                slot.inventory.decrStackSize(slot.index,1);
                slot.inventory.markDirty();
                world.notifyBlocksOfNeighborChange(x,y,z,crop);
            }
        }
    }

    private SeedSlot findSeed(EntityMaid maid,int x,int y,int z) {
        if(!maid.worldObj.isAirBlock(x,y,z)) return null;
        Block soil=maid.worldObj.getBlock(x,y-1,z);
        if(soil!=Blocks.farmland && soil!=Blocks.soul_sand) return null;
        IInventory[] inventories={maid.getMaidEquipmentInventory(),maid.getMaidTaskInventory(),maid.getMaidInventory()};
        int[] sizes={2,inventories[1].getSizeInventory(),maid.getBackpackCapacity()};
        for(int n=0;n<inventories.length;n++) for(int i=0;i<Math.min(sizes[n],inventories[n].getSizeInventory());i++)
            if(canPlant(maid.worldObj,x,y,z,inventories[n].getStackInSlot(i))) return new SeedSlot(inventories[n],i);
        return null;
    }

    private static boolean mature(Block block,int meta) {
        return block==Blocks.nether_wart ? meta>=3 : block instanceof BlockCrops && meta>=7;
    }
    private static Block plantBlock(ItemStack stack) {
        if(stack==null || stack.stackSize<=0) return null;
        if(stack.getItem()==Items.wheat_seeds) return Blocks.wheat;
        if(stack.getItem()==Items.carrot) return Blocks.carrots;
        if(stack.getItem()==Items.potato) return Blocks.potatoes;
        if(stack.getItem()==Items.nether_wart) return Blocks.nether_wart;
        if(stack.getItem()==Items.melon_seeds) return Blocks.melon_stem;
        if(stack.getItem()==Items.pumpkin_seeds) return Blocks.pumpkin_stem;
        return null;
    }
    private static boolean canPlant(World world,int x,int y,int z,ItemStack seed) {
        Block crop=plantBlock(seed);
        if(crop==null || !world.isAirBlock(x,y,z)) return false;
        return world.getBlock(x,y-1,z)==(crop==Blocks.nether_wart ? Blocks.soul_sand : Blocks.farmland)
                && crop.canBlockStay(world,x,y,z);
    }
    private static final class SeedSlot {
        final IInventory inventory; final int index;
        SeedSlot(IInventory inventory,int index){this.inventory=inventory;this.index=index;}
    }
}
