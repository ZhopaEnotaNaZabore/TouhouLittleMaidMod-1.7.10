package com.github.tartaricacid.touhoulittlemaid.command;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon;
import com.github.tartaricacid.touhoulittlemaid.world.backups.MaidBackupsManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidSchedule;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import com.github.tartaricacid.touhoulittlemaid.world.MaidWorldIndex;

import java.util.ArrayList;
import java.util.List;

/** Small in-world harness for validating each migrated subsystem. */
public final class CommandTlmMaid extends CommandBase {
    @Override
    public String getCommandName() {
        return "tlmmaid";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/tlmmaid <spawn|fairy|power|beaconverify|ownerverify|backup|profile|verify|uiverify|status|task|schedule|home> [value]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayer) || args.length == 0) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }
        EntityPlayer player = (EntityPlayer) sender;

        if ("beaconverify".equalsIgnoreCase(args[0])) {
            verifyNearestBeacon(sender, player);
            return;
        }
        if ("ownerverify".equalsIgnoreCase(args[0])) {
            verifyOwnerBinding(sender, player);
            return;
        }

        if ("profile".equalsIgnoreCase(args[0])) {
            if (args.length >= 2 && "reset".equalsIgnoreCase(args[1])) { EntityMaid.resetProfile(); sender.addChatMessage(new ChatComponentText("TLM maid profiler reset")); return; }
            long[] p=EntityMaid.getProfile();long average=p[0]==0?0:p[1]/p[0];int loaded=0;for(Object value:player.worldObj.loadedEntityList)if(value instanceof EntityMaid)loaded++;
            sender.addChatMessage(new ChatComponentText("TLM profile: loaded="+loaded+", samples="+p[0]+", avg="+(average/1000)+"us, max="+(p[2]/1000)+"us"));return;
        }
        if ("uiverify".equalsIgnoreCase(args[0])) {
            try {
                com.github.tartaricacid.touhoulittlemaid.test.MaidUiVerification.run(player);
                sender.addChatMessage(new ChatComponentText("TLM UI verify passed: favorability, slot gates, capacity, task NBT, portable crafting"));
            } catch (Exception error) {
                TouhouLittleMaid.LOGGER.error("TLM UI verification failed", error);
                sender.addChatMessage(new ChatComponentText("TLM UI verify FAILED: " + error));
            }
            return;
        }
        if("verify".equalsIgnoreCase(args[0])){
            try {
                int verified = 0;
                for (String id : TaskManager.getTasks().keySet()) {
                    EntityMaid probe = new EntityMaid(player.worldObj);
                    probe.setPosition(player.posX, player.posY, player.posZ);
                    probe.setTamed(true);
                    probe.func_152115_b(player.getUniqueID().toString());
                    if (verified == 0) {
                        net.minecraft.item.ItemStack weapon = new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_sword);
                        probe.getMaidEquipmentInventory().setInventorySlotContents(0, weapon);
                        if (probe.getHeldItem() != weapon)
                            throw new IllegalStateException("vanilla held-item bridge did not expose maid weapon");
                        probe.setCurrentItemOrArmor(0, null);
                        if (probe.getMaidEquipmentInventory().getStackInSlot(0) != null)
                            throw new IllegalStateException("external disarm did not clear maid weapon");
                    }
                    probe.ticksExisted = 20;
                    probe.setTaskId(id);
                    if (!id.equals(probe.getTaskId()))
                        throw new IllegalStateException("task switch failed: " + id);
                    TaskManager.get(id).tick(probe);

                    NBTTagCompound saved = new NBTTagCompound();
                    probe.writeToNBT(saved);
                    EntityMaid restored = new EntityMaid(player.worldObj);
                    restored.readFromNBT(saved);
                    restored.ticksExisted = 20;
                    if (!id.equals(restored.getTaskId()))
                        throw new IllegalStateException("task NBT mismatch: " + id);
                    if (!probe.getModelId().equals(restored.getModelId())
                            || !player.getUniqueID().toString().equals(restored.getOwnerId()))
                        throw new IllegalStateException("maid NBT mismatch: " + id);
                    TaskManager.get(id).tick(restored);
                    verified++;
                }
                EntityMaid combatProbe = new EntityMaid(player.worldObj);
                combatProbe.setPosition(player.posX + 1.0D, player.posY, player.posZ);
                combatProbe.setTamed(true);
                combatProbe.func_152115_b(player.getUniqueID().toString());
                combatProbe.setSchedule(MaidSchedule.ALL);
                combatProbe.getMaidEquipmentInventory().setInventorySlotContents(0,
                        new net.minecraft.item.ItemStack(net.minecraft.init.Items.iron_sword));
                combatProbe.setTaskId(TaskManager.ATTACK_ID);
                net.minecraft.entity.monster.EntityZombie ownerTarget =
                        new net.minecraft.entity.monster.EntityZombie(player.worldObj);
                ownerTarget.setPosition(player.posX + 2.0D, player.posY, player.posZ);
                player.setLastAttacker(ownerTarget);
                try {
                    com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidOwnerHurtTarget ownerGoal =
                            new com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidOwnerHurtTarget(combatProbe);
                    if (!ownerGoal.shouldExecute()) throw new IllegalStateException("owner attack trigger did not execute");
                    ownerGoal.startExecuting();
                    if (combatProbe.getAttackTarget() != ownerTarget)
                        throw new IllegalStateException("owner attack target was not assigned to maid");
                } finally {
                    player.setLastAttacker(null);
                }
                sender.addChatMessage(new ChatComponentText("TLM verify passed: constructor, "
                        + verified + " task ticks/NBT round-trips and owner combat trigger"));
            } catch (Exception error) {
                TouhouLittleMaid.LOGGER.error("TLM world verification failed", error);
                sender.addChatMessage(new ChatComponentText("TLM verify FAILED: " + error));
            }
            return;
        }

        if ("spawn".equalsIgnoreCase(args[0])) {
            EntityMaid maid = new EntityMaid(player.worldObj);
            maid.setPosition(player.posX + 1.0D, player.posY, player.posZ);
            maid.setTamed(true);
            maid.func_152115_b(player.getUniqueID().toString());
            player.worldObj.spawnEntityInWorld(maid);
            sender.addChatMessage(new ChatComponentText("Spawned owned maid (entity " + maid.getEntityId() + ")"));
            return;
        }

        if ("power".equalsIgnoreCase(args[0])) {
            int value = args.length >= 2 ? Math.max(1, parseInt(sender, args[1])) : 100;
            EntityPowerPoint point = new EntityPowerPoint(player.worldObj,
                    player.posX + 1.0D, player.posY + 1.0D, player.posZ, value);
            player.worldObj.spawnEntityInWorld(point);
            sender.addChatMessage(new ChatComponentText("Spawned Power Point with value " + value));
            return;
        }

        if ("fairy".equalsIgnoreCase(args[0])) {
            EntityFairy fairy = new EntityFairy(player.worldObj);
            fairy.setPosition(player.posX + 4.0D, player.posY + 1.0D, player.posZ);
            fairy.onSpawnWithEgg(null);
            player.worldObj.spawnEntityInWorld(fairy);
            sender.addChatMessage(new ChatComponentText("Spawned fairy variant " + fairy.getFairyTypeOrdinal()));
            return;
        }

        if ("backup".equalsIgnoreCase(args[0])) {
            handleBackup(sender, player, args);
            return;
        }

        EntityMaid maid = findNearestOwnedMaid(player);
        if (maid == null) {
            sender.addChatMessage(new ChatComponentText("No owned maid within 16 blocks"));
            return;
        }

        if ("status".equalsIgnoreCase(args[0])) {
            sender.addChatMessage(new ChatComponentText(
                    "task=" + maid.getTaskId() + ", hunger=" + maid.getHunger()
                            + ", favorability=" + maid.getFavorability()
                            + ", xp=" + maid.getMaidExperience() + ", sitting=" + maid.isSitting()
                            + ", schedule=" + maid.getSchedule() + ", activity=" + maid.getCurrentActivity()
                            + ", home=" + maid.isHomeMode() + ", nav=" + navigationStatus(maid)
                            + ", homeTarget=" + maid.getSchedulePos().getForActivity(maid.getCurrentActivity())
                            + ", beaconEffects=" + beaconEffectStatus(maid)
                            + ", meleeHits=" + maid.getSuccessfulMeleeHits()
                            + ", combatAllowed=" + maid.canEngageCombat()
                            + ", attackTarget=" + (maid.getAttackTarget()==null?"none":maid.getAttackTarget().getCommandSenderName())
                            + ", ownerLastTarget=" + (player.getLastAttacker()==null?"none":player.getLastAttacker().getCommandSenderName())
                            + ", lastMeleeDamage=" + maid.getLastMeleeDamage()
                            + ", lastMeleeAge=" + (maid.getLastMeleeHitTick() < 0 ? -1
                            : maid.ticksExisted - maid.getLastMeleeHitTick())));
            return;
        }

        if ("schedule".equalsIgnoreCase(args[0]) && args.length >= 2) {
            MaidSchedule schedule = MaidSchedule.byName(args[1].toUpperCase());
            maid.setSchedule(schedule);
            sender.addChatMessage(new ChatComponentText("Maid schedule changed to " + schedule));
            return;
        }

        if ("home".equalsIgnoreCase(args[0]) && args.length >= 2) {
            if ("set".equalsIgnoreCase(args[1])) {
                maid.getSchedulePos().setAll(maid);
                maid.setHomeMode(true);
                sender.addChatMessage(new ChatComponentText("Maid work/idle/sleep position set here"));
                return;
            }
            if ("clear".equalsIgnoreCase(args[1])) {
                maid.setHomeMode(false);
                maid.getSchedulePos().clear(maid);
                sender.addChatMessage(new ChatComponentText("Maid home restriction cleared"));
                return;
            }
        }

        if ("task".equalsIgnoreCase(args[0]) && args.length >= 2) {
            String id = normalizeTaskId(args[1]);
            if (!TaskManager.getTasks().containsKey(id)) {
                sender.addChatMessage(new ChatComponentText("Unknown task. Available: " + TaskManager.getTasks().keySet()));
                return;
            }
            maid.setTaskId(id);
            sender.addChatMessage(new ChatComponentText("Maid task changed to " + id));
            return;
        }

        sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "spawn", "fairy", "power", "beaconverify", "ownerverify", "backup", "profile", "verify", "uiverify", "status", "task", "schedule", "home");
        }
        if (args.length == 2 && "task".equalsIgnoreCase(args[0])) {
            List<String> ids = new ArrayList<String>();
            for (String id : TaskManager.getTasks().keySet()) {
                ids.add(id.substring(id.indexOf(':') + 1));
            }
            return getListOfStringsFromIterableMatchingLastWord(args, ids);
        }
        if (args.length == 2 && "schedule".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "day", "night", "all");
        }
        if (args.length == 2 && "home".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "set", "clear");
        }
        if (args.length == 2 && "backup".equalsIgnoreCase(args[0])) {
            return getListOfStringsMatchingLastWord(args, "list", "restore");
        }
        if(args.length==2&&"profile".equalsIgnoreCase(args[0]))return getListOfStringsMatchingLastWord(args,"reset");
        if (args.length == 3 && "backup".equalsIgnoreCase(args[0]) && "restore".equalsIgnoreCase(args[1])
                && sender instanceof EntityPlayer) {
            return getListOfStringsFromIterableMatchingLastWord(args,
                    MaidBackupsManager.listMaidIds(((EntityPlayer) sender).getUniqueID().toString()));
        }
        return null;
    }

    private void handleBackup(ICommandSender sender, EntityPlayer player, String[] args) {
        String ownerId = player.getUniqueID().toString();
        if (args.length >= 2 && "list".equalsIgnoreCase(args[1])) {
            sender.addChatMessage(new ChatComponentText("Maid backups: " + MaidBackupsManager.listMaidIds(ownerId)));
            return;
        }
        if (args.length >= 3 && "restore".equalsIgnoreCase(args[1])) {
            try {
                NBTTagCompound tag = MaidBackupsManager.loadLatest(ownerId, args[2]);
                if (tag == null) {
                    sender.addChatMessage(new ChatComponentText("No backup found for " + args[2]));
                    return;
                }
                if(!MaidWorldIndex.canRestore(tag)){sender.addChatMessage(new ChatComponentText("That maid UUID is already loaded in another dimension"));return;}
                EntityMaid restored = new EntityMaid(player.worldObj);
                restored.readFromNBT(tag);
                restored.setPosition(player.posX + 1.0D, player.posY, player.posZ);
                restored.setHealth(restored.getMaxHealth());
                player.worldObj.spawnEntityInWorld(restored);
                sender.addChatMessage(new ChatComponentText("Restored maid " + args[2] + " from latest backup"));
            } catch (IOException exception) {
                sender.addChatMessage(new ChatComponentText("Could not restore backup: " + exception.getMessage()));
            }
            return;
        }
        sender.addChatMessage(new ChatComponentText("/tlmmaid backup <list|restore UUID>"));
    }

    @SuppressWarnings("unchecked")
    private void verifyNearestBeacon(ICommandSender sender, EntityPlayer player) {
        TileEntityMaidBeacon beacon = null;
        double nearestDistance = 16.0D * 16.0D;
        for (Object value : player.worldObj.loadedTileEntityList) {
            if (!(value instanceof TileEntityMaidBeacon)) continue;
            TileEntity tile = (TileEntity) value;
            double dx = player.posX - (tile.xCoord + 0.5D);
            double dy = player.posY - (tile.yCoord + 0.5D);
            double dz = player.posZ - (tile.zCoord + 0.5D);
            double distance = dx * dx + dy * dy + dz * dz;
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                beacon = (TileEntityMaidBeacon) value;
            }
        }
        if (beacon == null) {
            EntityMaid maid = findNearestOwnedMaid(player);
            String maidState = maid == null ? "no owned maid nearby"
                    : "maid nav=" + navigationStatus(maid) + ", beaconEffects=" + beaconEffectStatus(maid);
            sender.addChatMessage(new ChatComponentText("Shrine Lamp test: no lamp within 16 blocks; " + maidState));
            return;
        }
        int potionId = beacon.getSelectedPotionId();
        if (potionId < 0) {
            sender.addChatMessage(new ChatComponentText("Shrine Lamp test: select an effect first"));
            return;
        }
        float before = beacon.getStoragePower();
        float cost = beacon.getEffectCost();
        if (before < cost) {
            sender.addChatMessage(new ChatComponentText(String.format(
                    "Shrine Lamp test: insufficient Power (stored=%.4f, cycle=%.4f)", before, cost)));
            return;
        }
        int affected = beacon.applySelectedEffect();
        PotionEffect actual = null;
        EntityMaid verifiedMaid = null;
        List<EntityMaid> maids = player.worldObj.getEntitiesWithinAABB(EntityMaid.class,
                net.minecraft.util.AxisAlignedBB.getBoundingBox(beacon.xCoord - 8, beacon.yCoord - 8, beacon.zCoord - 8,
                        beacon.xCoord + 9, beacon.yCoord + 9, beacon.zCoord + 9));
        Potion potion = potionId < Potion.potionTypes.length ? Potion.potionTypes[potionId] : null;
        for (EntityMaid maid : maids) {
            if (!maid.isEntityAlive() || potion == null) continue;
            PotionEffect candidate = maid.getActivePotionEffect(potion);
            if (candidate != null && (verifiedMaid == null
                    || maid.getDistanceSqToEntity(player) < verifiedMaid.getDistanceSqToEntity(player))) {
                verifiedMaid = maid;
                actual = candidate;
            }
        }
        float spent = before - beacon.getStoragePower();
        String actualState = actual == null ? "active=false"
                : "active=true, duration=" + actual.getDuration() + ", amplifier=" + actual.getAmplifier();
        sender.addChatMessage(new ChatComponentText(String.format(
                "Shrine Lamp test: affected=%d, potionId=%d, %s, spent=%.4f, remaining=%.4f",
                affected, potionId, actualState, spent, beacon.getStoragePower())));
    }

    @SuppressWarnings("unchecked")
    private void verifyOwnerBinding(ICommandSender sender, EntityPlayer player) {
        String playerId = player.getUniqueID().toString();
        EntityMaid nearest = null;
        double distance = Double.MAX_VALUE;
        for (Object value : player.worldObj.loadedEntityList) {
            if (!(value instanceof EntityMaid)) continue;
            EntityMaid maid = (EntityMaid) value;
            if (!playerId.equals(maid.getOwnerId())) continue;
            double candidate = maid.getDistanceSqToEntity(player);
            if (candidate < distance) { nearest = maid; distance = candidate; }
        }
        String gameMode = player instanceof net.minecraft.entity.player.EntityPlayerMP
                ? ((net.minecraft.entity.player.EntityPlayerMP) player).theItemInWorldManager.getGameType().getName()
                : (player.capabilities.isCreativeMode ? "creative" : "survival");
        if (nearest == null) {
            sender.addChatMessage(new ChatComponentText("Maid owner test: mode=" + gameMode
                    + ", no loaded maid stores player UUID " + playerId));
            return;
        }
        net.minecraft.entity.EntityLivingBase resolved = nearest.getOwner();
        String resolvedId = resolved == null ? "none" : resolved.getUniqueID().toString();
        sender.addChatMessage(new ChatComponentText("Maid owner test: mode=" + gameMode
                + ", tamed=" + nearest.isTamed() + ", stored=" + nearest.getOwnerId()
                + ", resolved=" + resolvedId + ", samePlayer=" + (resolved == player)));
    }

    @SuppressWarnings("unchecked")
    private EntityMaid findNearestOwnedMaid(EntityPlayer player) {
        List<EntityMaid> maids = player.worldObj.getEntitiesWithinAABB(
                EntityMaid.class, player.boundingBox.expand(16.0D, 8.0D, 16.0D));
        EntityMaid nearest = null;
        double distance = Double.MAX_VALUE;
        for (EntityMaid maid : maids) {
            double candidateDistance = player.getDistanceSqToEntity(maid);
            if (maid.getOwner() == player && candidateDistance < distance) {
                nearest = maid;
                distance = candidateDistance;
            }
        }
        return nearest;
    }

    private String normalizeTaskId(String id) {
        return id.indexOf(':') >= 0 ? id : "touhou_little_maid:" + id;
    }

    private String navigationStatus(EntityMaid maid) {
        net.minecraft.pathfinding.PathEntity path = maid.getNavigator().getPath();
        if (path == null || path.isFinished()) return "none";
        net.minecraft.pathfinding.PathPoint end = path.getFinalPathPoint();
        return end == null ? "active"
                : end.xCoord + "," + end.yCoord + "," + end.zCoord;
    }

    private String beaconEffectStatus(EntityMaid maid) {
        int[] ids = {Potion.moveSpeed.id, Potion.fireResistance.id, Potion.damageBoost.id,
                Potion.resistance.id, Potion.regeneration.id};
        StringBuilder result = new StringBuilder();
        for (int id : ids) {
            Potion potion = Potion.potionTypes[id];
            PotionEffect effect = potion == null ? null : maid.getActivePotionEffect(potion);
            if (effect == null) continue;
            if (result.length() > 0) result.append('|');
            result.append(id).append(':').append(effect.getDuration()).append(':').append(effect.getAmplifier());
        }
        return result.length() == 0 ? "none" : result.toString();
    }
}
