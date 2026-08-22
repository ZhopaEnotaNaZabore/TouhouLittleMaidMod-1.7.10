package com.github.tartaricacid.touhoulittlemaid.entity.task;

import com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyMiningToolCompat;
import com.github.tartaricacid.touhoulittlemaid.compat.miner.LegacyOreClassifier;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.BlockEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/** Optional mod-aware mining profession added by the 1.7.10 port. */
public final class TaskMiner implements com.github.tartaricacid.touhoulittlemaid.api.task.IMaidTask {
    private static final Map<EntityMaid, Integer> SCAN_CURSOR = new WeakHashMap<EntityMaid, Integer>();

    @Override public String getId() { return TaskManager.MINER_ID; }

    @Override public void tick(EntityMaid maid) {
        if (maid.worldObj.isRemote || maid.isSitting() || !maid.isPeriodicTick(10)) return;
        if (!LegacyTaskEquipUtil.ensureMiningTool(maid)) return;
        Target target = findTarget(maid);
        if (target == null) return;
        if (maid.getDistanceSq(target.x + 0.5D, target.y + 0.5D, target.z + 0.5D) > 6.25D) {
            maid.getNavigator().tryMoveToXYZ(target.x + 0.5D, target.y, target.z + 0.5D, 0.65D);
            return;
        }
        ItemStack tool = maid.getMaidEquipmentInventory().getStackInSlot(0);
        int[][] plane = LegacyConfig.minerAreaMining && LegacyMiningToolCompat.isAreaTool(tool)
                ? miningPlane(maid, target) : new int[][]{{target.x, target.y, target.z}};
        for (int[] pos : plane) {
            if (tool == null || tool.stackSize <= 0) break;
            mine(maid, pos[0], pos[1], pos[2], tool);
        }
        if (tool != null && tool.stackSize <= 0) maid.getMaidEquipmentInventory().setInventorySlotContents(0, null);
        maid.getMaidEquipmentInventory().markDirty();
    }

    private Target findTarget(EntityMaid maid) {
        int radius = LegacyConfig.minerSearchRadius, vertical = LegacyConfig.minerVerticalRange;
        int width = radius * 2 + 1, height = vertical * 2 + 1, volume = width * width * height;
        Integer previous = SCAN_CURSOR.get(maid); int start = previous == null ? 0 : previous;
        int budget = Math.min(volume, LegacyConfig.minerScanBudget);
        int cx = floor(maid.posX), cy = floor(maid.posY), cz = floor(maid.posZ);
        Target nearest = null; double nearestDistance = Double.MAX_VALUE;
        for (int checked = 0; checked < budget; checked++) {
            int index = (start + checked) % volume;
            int dx = index % width - radius;
            int dz = index / width % width - radius;
            int dy = index / (width * width) - vertical;
            int x = cx + dx, y = cy + dy, z = cz + dz;
            if (y < 1 || y >= maid.worldObj.getHeight() || !maid.worldObj.blockExists(x, y, z)
                    || !maid.isPositionWithinRestriction(x + 0.5D, y, z + 0.5D)) continue;
            Block block = maid.worldObj.getBlock(x, y, z); int meta = maid.worldObj.getBlockMetadata(x, y, z);
            ItemStack tool = maid.getMaidEquipmentInventory().getStackInSlot(0);
            if (!LegacyOreClassifier.isOre(maid.worldObj, x, y, z, block, meta)
                    || block.getBlockHardness(maid.worldObj, x, y, z) < 0
                    || !LegacyMiningToolCompat.canHarvest(tool, block, meta)) continue;
            double distance = maid.getDistanceSq(x + 0.5D, y + 0.5D, z + 0.5D);
            if (distance < nearestDistance) { nearest = new Target(x, y, z); nearestDistance = distance; }
        }
        SCAN_CURSOR.put(maid, (start + budget) % volume);
        return nearest;
    }

    private boolean mine(EntityMaid maid, int x, int y, int z, ItemStack tool) {
        if (!maid.worldObj.blockExists(x, y, z) || !maid.isPositionWithinRestriction(x + .5D, y, z + .5D)) return false;
        Block block = maid.worldObj.getBlock(x, y, z); int metadata = maid.worldObj.getBlockMetadata(x, y, z);
        if (!LegacyOreClassifier.isOre(maid.worldObj, x, y, z, block, metadata)
                || block.getBlockHardness(maid.worldObj, x, y, z) < 0
                || !LegacyMiningToolCompat.canHarvest(tool, block, metadata)) return false;
        FakePlayer actor = actor(maid, tool);
        BlockEvent.BreakEvent breaking = new BlockEvent.BreakEvent(x, y, z, maid.worldObj, block, metadata, actor);
        if (MinecraftForge.EVENT_BUS.post(breaking)) return false;
        int fortune = EnchantmentHelper.getFortuneModifier(actor);
        boolean silk = EnchantmentHelper.getSilkTouchModifier(actor) && block.canSilkHarvest(maid.worldObj, actor, x, y, z, metadata);
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if (silk) {
            ItemStack silkDrop = silkDrop(block, metadata);
            if (silkDrop != null) drops.add(silkDrop);
            else silk = false;
        }
        if (!silk) drops.addAll(block.getDrops(maid.worldObj, x, y, z, metadata, fortune));
        float chance = ForgeEventFactory.fireBlockHarvesting(drops, maid.worldObj, block, x, y, z,
                metadata, fortune, 1.0F, silk, actor);
        if (!maid.worldObj.setBlockToAir(x, y, z)) return false;
        for (ItemStack drop : drops) if (maid.getRNG().nextFloat() <= chance) {
            ItemStack remaining = maid.addToMaidInventory(drop);
            if (remaining != null && remaining.stackSize > 0) maid.entityDropItem(remaining, 0.0F);
        }
        if (breaking.getExpToDrop() > 0) block.dropXpOnBlockBreak(maid.worldObj, x, y, z, breaking.getExpToDrop());
        try {
            tool.getItem().onBlockDestroyed(tool, maid.worldObj, block, x, y, z, maid);
        } catch (Throwable incompatibleOptionalMod) {
            // A few legacy tools incorrectly cast the user to EntityPlayer.
            // Keep the server alive and charge one normal durability unit until
            // their dedicated adapter can execute the native player-only hook.
            tool.damageItem(1, maid);
        }
        maid.worldObj.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (metadata << 12));
        maid.swingItem();
        return true;
    }

    private FakePlayer actor(EntityMaid maid, ItemStack tool) {
        EntityPlayer owner = maid.getOwner() instanceof EntityPlayer ? (EntityPlayer) maid.getOwner() : null;
        UUID id = owner == null ? maid.getUniqueID() : owner.getUniqueID();
        String name = owner == null ? "[TLM-Maid]" : owner.getCommandSenderName();
        FakePlayer player = FakePlayerFactory.get((WorldServer) maid.worldObj, new GameProfile(id, name));
        player.setPositionAndRotation(maid.posX, maid.posY, maid.posZ, maid.rotationYaw, maid.rotationPitch);
        player.inventory.currentItem = 0;
        player.inventory.setInventorySlotContents(0, tool);
        return player;
    }

    private ItemStack silkDrop(Block block, int metadata) {
        try {
            java.lang.reflect.Method method = ReflectionHelper.findMethod(Block.class, block,
                    new String[]{"createStackedBlock", "func_149644_j"}, Integer.TYPE);
            return (ItemStack) method.invoke(block, metadata);
        } catch (Exception ignored) {
            return null;
        }
    }

    private int[][] miningPlane(EntityMaid maid, Target target) {
        int[][] result = new int[9][3]; int index = 0;
        double dx = target.x + .5D - maid.posX, dy = target.y + .5D - (maid.posY + maid.getEyeHeight()), dz = target.z + .5D - maid.posZ;
        if (Math.abs(dy) > Math.max(Math.abs(dx), Math.abs(dz))) {
            for (int ox=-1;ox<=1;ox++) for(int oz=-1;oz<=1;oz++) result[index++]=new int[]{target.x+ox,target.y,target.z+oz};
        } else if (Math.abs(dx) > Math.abs(dz)) {
            for (int oy=-1;oy<=1;oy++) for(int oz=-1;oz<=1;oz++) result[index++]=new int[]{target.x,target.y+oy,target.z+oz};
        } else {
            for (int oy=-1;oy<=1;oy++) for(int ox=-1;ox<=1;ox++) result[index++]=new int[]{target.x+ox,target.y+oy,target.z};
        }
        return result;
    }

    private static int floor(double value) { return (int)Math.floor(value); }
    private static final class Target { final int x,y,z; Target(int x,int y,int z){this.x=x;this.y=y;this.z=z;} }
}
