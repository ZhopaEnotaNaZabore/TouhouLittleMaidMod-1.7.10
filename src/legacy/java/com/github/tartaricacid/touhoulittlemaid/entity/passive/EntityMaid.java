package com.github.tartaricacid.touhoulittlemaid.entity.passive;

import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.proxy.CommonProxy;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidFollowOwner;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidMeleeAttack;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidRangedAttack;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidOwnerHurtTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.EntityAIMaidOwnerHurtByTarget;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidActivity;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidSchedule;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuColor;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.DanmakuType;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityDanmaku;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityExtinguishingAgent;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityJoy;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityInventory;
import com.github.tartaricacid.touhoulittlemaid.block.BlockJoy;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.world.backups.MaidBackupsManager;
import com.github.tartaricacid.touhoulittlemaid.item.ItemFilm;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.item.ItemMaidBackpack;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.FavorabilityManager;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.init.ModEnchantments;
import com.github.tartaricacid.touhoulittlemaid.init.ModAchievements;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Items;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;

import java.util.List;
import java.util.ArrayList;

/**
 * Forge 1.7.10 runtime counterpart of the 1.20 EntityMaid.
 *
 * The stable NBT names deliberately match the modern implementation. This
 * lets individual subsystems be ported without changing their persisted data
 * contract and makes later data migration predictable.
 */
public class EntityMaid extends EntityTameable implements IRangedAttackMob {
    private static long profileSamples, profileNanos, profileMaxNanos;
    public static final String MODEL_ID_TAG = "ModelId";
    public static final String SOUND_PACK_ID_TAG = "SoundPackId";
    public static final String TASK_TAG = "MaidTask";
    public static final String HUNGER_TAG = "MaidHunger";
    public static final String FAVORABILITY_TAG = "MaidFavorability";
    public static final String EXPERIENCE_TAG = "MaidExperience";
    public static final String SCHEDULE_MODE_TAG = "MaidScheduleMode";
    public static final String MAID_INVENTORY_TAG = "MaidInventory";
    public static final String MAID_BACKPACK_TYPE = "MaidBackpackType";
    public static final String MAID_BAUBLE_INVENTORY_TAG = "MaidBaubleInventory";
    public static final String MAID_EQUIPMENT_INVENTORY_TAG = "MaidEquipmentInventory";
    public static final String MAID_HIDE_INVENTORY_TAG = "MaidHideInventory";
    public static final String MAID_TASK_INVENTORY_TAG = "MaidTaskInventory";
    public static final String MAID_TASK_DATA_TAG = "MaidTaskData";
    public static final String DEFAULT_MODEL_ID = "touhou_little_maid:hakurei_reimu";
    public static final String DEFAULT_SOUND_PACK_ID = "touhou_little_maid:default";

    private static final int WATCHER_HUNGER = 20;
    private static final int WATCHER_FAVORABILITY = 21;
    private static final int WATCHER_EXPERIENCE = 22;
    private static final int WATCHER_SCHEDULE = 23;
    private static final int WATCHER_FLAGS = 24;
    private static final int WATCHER_ACTIVITY = 25;
    private static final int WATCHER_TASK_INDEX = 26;
    private static final int WATCHER_MODEL_ID = 27;
    private static final int WATCHER_SOUND_PACK_ID = 28;
    private static final int WATCHER_FISHING_ACTIVE = 29;
    private static final int WATCHER_BACKPACK_TYPE = 30;
    private static final int WATCHER_OFFHAND = 31;

    private static final int FLAG_PICKUP = 1;
    private static final int FLAG_HOME_MODE = 2;
    private static final int FLAG_INVULNERABLE = 4;
    private static final int FLAG_BEGGING = 8;
    private static final int FLAG_SLEEPING = 16;

    private final InventoryBasic maidInventory = new InventoryBasic("MaidInventory", false, 36);
    private final InventoryBasic maidBaubleInventory = new InventoryBasic("MaidBaubleInventory", false, 30);
    private final InventoryBasic maidEquipmentInventory = new InventoryBasic("MaidEquipmentInventory", false, 6);
    private final InventoryBasic maidHideInventory = new InventoryBasic("MaidHideInventory", false, 1);
    private final InventoryBasic maidTaskInventory = new InventoryBasic("MaidTaskInventory", false, 9);
    private String modelId = DEFAULT_MODEL_ID;
    private String soundPackId = DEFAULT_SOUND_PACK_ID;
    private String taskId = TaskManager.IDLE_ID;
    private final SchedulePos schedulePos = new SchedulePos();
    private final FavorabilityManager favorabilityManager = new FavorabilityManager(this);
    private String backpackType = "empty";
    private int gomokuWins;
    private String backpackFluid="";
    private int backpackFluidAmount;
    private static final int BACKPACK_TANK_CAPACITY = 10000;
    private final List<String> aiChatRoles = new ArrayList<String>();
    private final List<String> aiChatMessages = new ArrayList<String>();
    private NBTTagCompound maidTaskData = new NBTTagCompound();
    private int openedGateX,openedGateY,openedGateZ,openedGateTicks;
    private boolean hasOpenedGate;
    private int successfulMeleeHits;
    private int lastMeleeHitTick = -1;
    private float lastMeleeDamage;

    public EntityMaid(World world) {
        super(world);
        setSize(0.6F, 1.5F);
        getNavigator().setAvoidsWater(false);
        getNavigator().setBreakDoors(true);

        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAIPanic(this, 1.35D));
        tasks.addTask(2, aiSit);
        tasks.addTask(3, new EntityAIOpenDoor(this, true));
        tasks.addTask(3, new EntityAIMaidFollowOwner(this, 1.0D, 4.0F, 2.0F));
        tasks.addTask(4, new EntityAIMaidMeleeAttack(this));
        tasks.addTask(4, new EntityAIMaidRangedAttack(this));
        tasks.addTask(8, new EntityAIWander(this, 0.6D));
        tasks.addTask(7, new EntityAITempt(this, 0.8D, Items.cake, false));
        tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        tasks.addTask(10, new EntityAILookIdle(this));
        targetTasks.addTask(1, new EntityAIMaidOwnerHurtByTarget(this));
        targetTasks.addTask(2, new EntityAIMaidOwnerHurtTarget(this));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataWatcher.addObject(WATCHER_HUNGER, 100);
        dataWatcher.addObject(WATCHER_FAVORABILITY, 0);
        dataWatcher.addObject(WATCHER_EXPERIENCE, 0);
        dataWatcher.addObject(WATCHER_SCHEDULE, 0);
        dataWatcher.addObject(WATCHER_FLAGS, (byte) FLAG_PICKUP);
        dataWatcher.addObject(WATCHER_ACTIVITY, (byte) MaidActivity.IDLE.ordinal());
        dataWatcher.addObject(WATCHER_TASK_INDEX, 0);
        // Entity's constructor calls this virtual method before EntityMaid's
        // instance fields are initialized. Never read subclass fields here:
        // DataWatcher rejects null values and spawning the maid would crash.
        dataWatcher.addObject(WATCHER_MODEL_ID, DEFAULT_MODEL_ID);
        dataWatcher.addObject(WATCHER_SOUND_PACK_ID, DEFAULT_SOUND_PACK_ID);
        dataWatcher.addObject(WATCHER_FISHING_ACTIVE, (byte) 0);
        dataWatcher.addObject(WATCHER_BACKPACK_TYPE, "empty");
        dataWatcher.addObjectByDataType(WATCHER_OFFHAND, 5);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.3D);
        if (getEntityAttribute(SharedMonsterAttributes.attackDamage) == null) getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(2.0D);
    }

    @Override
    public boolean isAIEnabled() {
        return true;
    }

    @Override
    public void onLivingUpdate() {
        // AI goals run inside super.onLivingUpdate(). Clear combat state first
        // so a maid already below the retreat threshold cannot land one more
        // melee/ranged attack before its profession tick notices the health.
        if (!worldObj.isRemote && !canEngageCombat() && getAttackTarget() != null) {
            setAttackTarget(null);
            getNavigator().clearPathEntity();
        }
        super.onLivingUpdate();
        if (worldObj.isRemote) {
            tickClientParticles();
            return;
        }
        long profileStarted = System.nanoTime();

        updateScheduleActivity();
        syncEquipmentSlots();
        favorabilityManager.tick();
        tickSpecialBackpack();
        if(isPeriodicTick(20))tickWirelessIO();
        if (getCurrentActivity() == MaidActivity.WORK) {
            TaskManager.get(taskId).tick(this);
        } else if (getAttackTarget() != null) {
            setAttackTarget(null);
        }
        schedulePos.tick(this);
        updateHungerAndHealing();
        tickHomeBehaviors();
        tickFenceGate();
        if (isCollidedHorizontally && !onGround && !isSitting() && getNavigator().getPath() != null) motionY = Math.max(motionY, 0.2D);
        if (isTamed() && isPickupEnabled() && !isSitting() && ticksExisted % 5 == 0) {
            pickupNearbyItems();
        }
        if (isTamed() && isPeriodicTick(LegacyConfig.backupIntervalTicks)) MaidBackupsManager.save(this);
        if (isTamed() && isPeriodicTick(40) && !isHomeMode() && !isSitting()
                && !isRiding() && !getLeashed()) followOwnerFallback();
        if (isTamed()) tickMaidVoice();
        long elapsed=System.nanoTime()-profileStarted;profileSamples++;profileNanos+=elapsed;if(elapsed>profileMaxNanos)profileMaxNanos=elapsed;
    }

    public static synchronized long[] getProfile(){return new long[]{profileSamples,profileNanos,profileMaxNanos};}
    public static synchronized void resetProfile(){profileSamples=profileNanos=profileMaxNanos=0;}

    private void tickClientParticles() {
        if (ticksExisted % 30 != Math.floorMod(getEntityId(), 30)) return;
        String particle = isBegging() ? "heart" : isMaidSleeping() ? "enchantmenttable"
                : getCurrentActivity() == MaidActivity.WORK ? "happyVillager" : null;
        if (particle != null) worldObj.spawnParticle(particle, posX + (rand.nextDouble() - .5D) * width,
                posY + height * .75D, posZ + (rand.nextDouble() - .5D) * width, 0, .02D, 0);
    }

    @Override
    public boolean interact(EntityPlayer player) {
        if (player instanceof FakePlayer) {
            return false;
        }
        ItemStack held = player.getCurrentEquippedItem();

        if (!isTamed()) {
            if (held != null && held.getItem() == Items.cake) {
                if (!worldObj.isRemote && countOwnerMaids(player) >= LegacyConfig.maxMaidPerPlayer) {
                    player.addChatMessage(new net.minecraft.util.ChatComponentText("Maid limit reached: " + LegacyConfig.maxMaidPerPlayer)); return true;
                }
                consumeOne(player, held);
                if (!worldObj.isRemote) {
                    setTamed(true);
                    func_152115_b(player.getUniqueID().toString());
                    aiSit.setSitting(false);
                    getNavigator().clearPathEntity();
                    worldObj.setEntityState(this, (byte) 7);
                    playMaidVoice("maid.ai.tamed");
                    player.triggerAchievement(ModAchievements.TAME_MAID);
                }
                return true;
            }
            return false;
        }

        if (held != null && held.getItem() == ModItems.OWNER_CONVERSION_TOOL && player != getOwner()) {
            consumeOne(player, held);
            if (!worldObj.isRemote) {
                func_152115_b(player.getUniqueID().toString());
                setMaidSitting(false);
                setAttackTarget(null);
                getNavigator().clearPathEntity();
                worldObj.setEntityState(this, (byte) 7);
            }
            return true;
        }

        if (player != getOwner()) {
            return false;
        }

        // Source behaviour: the owner removes an equipped backpack with
        // shears.  This is also the only lossless way to recover tank NBT.
        if (held != null && held.getItem() == Items.shears && !"empty".equals(backpackType)) {
            if (!worldObj.isRemote) takeOffBackpack(player, held);
            return true;
        }
        if (held != null && held.getItem() instanceof ItemMaidBackpack) {
            if (!worldObj.isRemote) equipBackpack(player, held, (ItemMaidBackpack) held.getItem());
            return true;
        }
        if (isBauble(held)) {
            if (!worldObj.isRemote) {
                for (int slot = 0; slot < getBaubleCapacity(); slot++) if (maidBaubleInventory.getStackInSlot(slot) == null) {
                    ItemStack one = held.copy(); one.stackSize = 1; maidBaubleInventory.setInventorySlotContents(slot, one);
                    consumeOne(player, held); worldObj.playSoundAtEntity(this, "random.pop", 0.5F, 1.3F); break;
                }
            }
            return true;
        }
        if (held != null && held.getItem() == ModItems.SUBSTITUTE_JIZO && !isMaidInvulnerable()) {
            consumeOne(player, held); if (!worldObj.isRemote) setMaidInvulnerable(true); return true;
        }

        if (held != null && held.getItem() instanceof ItemFood && (getHealth() < getMaxHealth() || getHunger() < 100)) {
            ItemFood food = (ItemFood) held.getItem();
            consumeOne(player, held);
            if (!worldObj.isRemote) {
                int foodValue = food.func_150905_g(held);
                setHunger(getHunger() + foodValue * 2);
                heal(Math.max(1.0F, foodValue * 0.5F));
                favorabilityManager.apply("WorkMeal", 1, 3 * 60 * 20);
                playMaidVoice("maid.mode.feed");
            }
            return true;
        }

        if (player.isSneaking()) {
            if (!worldObj.isRemote) {
                setMaidSitting(!isSitting());
            }
            return true;
        }
        if (held == null) {
            if (!worldObj.isRemote) {
                player.openGui(TouhouLittleMaid.instance, CommonProxy.MAID_GUI_ID, worldObj, getEntityId(), 0, 0);
            }
            return true;
        }
        return super.interact(player);
    }

    @Override
    public boolean attackEntityFrom(net.minecraft.util.DamageSource source, float amount) {
        if (isMaidInvulnerable()) return false;
        ItemMaidBauble.Type protection = null;
        if (source == net.minecraft.util.DamageSource.drown) protection = ItemMaidBauble.Type.DROWN;
        else if (source == net.minecraft.util.DamageSource.fall) protection = ItemMaidBauble.Type.FALL;
        else if (source.isFireDamage()) protection = ItemMaidBauble.Type.FIRE;
        else if (source.isExplosion()) protection = ItemMaidBauble.Type.EXPLOSION;
        else if (source.isProjectile()) {
            int nimble = findBauble(ItemMaidBauble.Type.NIMBLE);
            if (nimble >= 0 && teleportAway()) { damageBauble(nimble); return false; }
            protection = ItemMaidBauble.Type.PROJECTILE;
        } else if (source.isMagicDamage()) protection = ItemMaidBauble.Type.MAGIC;
        int slot = protection == null ? -1 : findBauble(protection);
        if (slot >= 0) {
            damageBauble(slot);
            if (protection == ItemMaidBauble.Type.DROWN) setAir(300);
            if (protection == ItemMaidBauble.Type.FIRE) addPotionEffect(new PotionEffect(Potion.fireResistance.id, 300));
            worldObj.playSoundAtEntity(this, "random.glass", 0.7F, 1.4F);
            return false;
        }
        if (amount >= getHealth()) {
            int life = findBauble(ItemMaidBauble.Type.EXTRA_LIFE);
            if (life >= 0 && !source.canHarmInCreative()) {
                damageBauble(life); setHealth(getMaxHealth()); clearActivePotions();
                addPotionEffect(new PotionEffect(Potion.regeneration.id, 900, 1));
                addPotionEffect(new PotionEffect(Potion.fireResistance.id, 800));
                worldObj.setEntityState(this, (byte) 35); return false;
            }
        }
        boolean result = super.attackEntityFrom(source, amount);
        if (result && !worldObj.isRemote && isEntityAlive()) playMaidVoice(source.isFireDamage() ? "maid.ai.hurt_fire" : "maid.ai.hurt");
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean attackEntityAsMob(Entity target) {
        if (!canEngageCombat()) return false;
        // EntityMaid descends from EntityTameable. In 1.7.10 that inheritance
        // reaches EntityLivingBase.attackEntityAsMob(), whose implementation
        // only records the attacker and always returns false. Use the vanilla
        // EntityMob combat transaction explicitly so approaching/colliding
        // with a target results in a real server-side hit.
        setLastAttacker(target);
        float damage = (float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        int knockback = 0;
        float healthBefore = target instanceof EntityLivingBase ? ((EntityLivingBase) target).getHealth() : 0.0F;
        if (target instanceof EntityLivingBase) {
            damage += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase) target);
            knockback = EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase) target);
        }
        boolean result = target.attackEntityFrom(net.minecraft.util.DamageSource.causeMobDamage(this), damage);
        if (result) {
            if (knockback > 0) {
                target.addVelocity(-MathHelper.sin(rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F,
                        0.1D, MathHelper.cos(rotationYaw * (float) Math.PI / 180.0F) * knockback * 0.5F);
                motionX *= 0.6D;
                motionZ *= 0.6D;
            }
            int fireAspect = EnchantmentHelper.getFireAspectModifier(this);
            if (fireAspect > 0) target.setFire(fireAspect * 4);
            if (target instanceof EntityLivingBase)
                EnchantmentHelper.func_151384_a((EntityLivingBase) target, this);
            EnchantmentHelper.func_151385_b(this, target);
            if (!worldObj.isRemote) {
                successfulMeleeHits++;
                lastMeleeHitTick = ticksExisted;
                lastMeleeDamage = target instanceof EntityLivingBase
                        ? Math.max(0.0F, healthBefore - ((EntityLivingBase) target).getHealth()) : damage;
                if (TaskManager.ATTACK_ID.equals(getTaskId())) playMaidVoice("maid.mode.attack");
            }
        }
        if (!worldObj.isRemote && TaskManager.ATTACK_ID.equals(getTaskId())
                && target != null && target.isImmuneToFire()) {
            ItemStack offhand = maidEquipmentInventory.getStackInSlot(1);
            if (offhand != null && offhand.getItem() == ModItems.EXTINGUISHER) {
                AxisAlignedBB area = target.boundingBox.expand(1.5D, 1.0D, 1.5D);
                List<EntityExtinguishingAgent> agents = worldObj.getEntitiesWithinAABB(
                        EntityExtinguishingAgent.class, area);
                if (agents.isEmpty()) {
                    worldObj.spawnEntityInWorld(new EntityExtinguishingAgent(
                            worldObj, target.posX, target.posY, target.posZ));
                    offhand.damageItem(1, this);
                    if (offhand.stackSize <= 0)
                        maidEquipmentInventory.setInventorySlotContents(1, null);
                    maidEquipmentInventory.markDirty();
                }
            }
        }
        return result;
    }

    public int getSuccessfulMeleeHits() { return successfulMeleeHits; }
    public int getLastMeleeHitTick() { return lastMeleeHitTick; }
    public float getLastMeleeDamage() { return lastMeleeDamage; }

    @Override
    public void onDeath(net.minecraft.util.DamageSource source) {
        if (!worldObj.isRemote && isTamed()) {
            playMaidVoice("maid.ai.death");
            favorabilityManager.apply("Death", -2, 12000);
            MaidBackupsManager.save(this);
            String ownerId = func_152113_b();
            EntityTombstone tombstone = new EntityTombstone(worldObj, ownerId, posX, posY, posZ);
            tombstone.setMaidName(hasCustomNameTag() ? getCustomNameTag() : "Maid");
            tombstone.insertItem(ItemFilm.maidToFilm(this));
            ItemStack backpack = getEquippedBackpack(); if (backpack != null) tombstone.insertItem(backpack);
            for (int slot = 0; slot < maidInventory.getSizeInventory(); slot++) {
                ItemStack stack = maidInventory.getStackInSlot(slot);
                if (stack != null) {
                    tombstone.insertItem(stack);
                    maidInventory.setInventorySlotContents(slot, null);
                }
            }
            for (int slot = 0; slot < maidBaubleInventory.getSizeInventory(); slot++) {
                ItemStack stack = maidBaubleInventory.getStackInSlot(slot);
                if (stack != null) { tombstone.insertItem(stack); maidBaubleInventory.setInventorySlotContents(slot, null); }
            }
            for (int slot = 0; slot < maidEquipmentInventory.getSizeInventory(); slot++) {
                ItemStack stack = maidEquipmentInventory.getStackInSlot(slot);
                if (stack != null) { tombstone.insertItem(stack); maidEquipmentInventory.setInventorySlotContents(slot, null); }
            }
            moveInventoryToTombstone(maidHideInventory, tombstone);
            moveInventoryToTombstone(maidTaskInventory, tombstone);
            for (int equipment = 0; equipment < 5; equipment++) setCurrentItemOrArmor(equipment, null);
            worldObj.spawnEntityInWorld(tombstone);
        }
        super.onDeath(source);
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor) {
        if (!canEngageCombat()) return;
        if (TaskManager.CROSSBOW_ATTACK_ID.equals(getTaskId()) || TaskManager.TRIDENT_ATTACK_ID.equals(getTaskId())) {
            boolean crossbow=TaskManager.CROSSBOW_ATTACK_ID.equals(getTaskId());ItemStack weapon=maidEquipmentInventory.getStackInSlot(0);if(weapon==null||weapon.getItem()!=(crossbow?ModItems.CROSSBOW:ModItems.TRIDENT)||target==null)return;
            int arrowSlot=crossbow?findInventorySlot(Items.arrow):-1;if(crossbow&&arrowSlot<0)return;
            float distance=getDistanceToEntity(target);float velocity=MathHelper.clamp_float(distance/10.0F,1.6F,3.2F);float inaccuracy=1.0F-MathHelper.clamp_float(distance/100.0F,0.0F,0.9F);
            EntityArrow arrow=new EntityArrow(worldObj,this,target,crossbow?2.2F:velocity,inaccuracy);arrow.setDamage(crossbow?4.0D:6.0D);arrow.canBePickedUp=0;worldObj.spawnEntityInWorld(arrow);
            if(crossbow)consumeInventoryItem(arrowSlot);weapon.damageItem(1,this);if(weapon.stackSize<=0)maidEquipmentInventory.setInventorySlotContents(0,null);maidEquipmentInventory.markDirty();swingItem();playSound("random.bow",1,crossbow?0.8F:1.1F);playMaidVoice("maid.mode.range_attack");return;
        }
        if (TaskManager.DANMAKU_ATTACK_ID.equals(getTaskId())) {
            ItemStack gohei = maidEquipmentInventory.getStackInSlot(0);
            if (gohei == null || (gohei.getItem() != ModItems.HAKUREI_GOHEI
                    && gohei.getItem() != ModItems.SANAE_GOHEI) || target == null) return;
            int speedy=EnchantmentHelper.getEnchantmentLevel(ModEnchantments.SPEEDY.effectId,gohei),impeding=EnchantmentHelper.getEnchantmentLevel(ModEnchantments.IMPEDING.effectId,gohei);
            boolean ender=EnchantmentHelper.getEnchantmentLevel(ModEnchantments.ENDERS_ENDER.effectId,gohei)>0,multishot=EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId,gohei)>0;
            double dx = target.posX - posX;
            double dy = target.boundingBox.minY + target.height * 0.5D - (posY + getEyeHeight());
            double dz = target.posZ - posZ;
            int enemyCount = countVisibleEnemiesOfType(target, 24.0D);
            int count = enemyCount <= 1 ? (multishot ? 3 : 1) : enemyCount <= 5 ? 8 : 32;
            double yawTotal = count == 1 ? 0.0D : count == 3 ? Math.PI / 12.0D
                    : count == 8 ? Math.PI / 3.0D : Math.PI * 2.0D / 3.0D;
            float bonus = enemyCount <= 1 && !multishot ? 1.0F : enemyCount <= 5 ? 1.2F : 1.5F;
            float distance = getDistanceToEntity(target);
            float speed = 0.3F * (distanceFactor + 1.0F) * (speedy + 1)
                    + MathHelper.clamp_float(distance / 40.0F - 0.4F, 0.0F, 2.4F);
            float inaccuracy = 1.0F - MathHelper.clamp_float(distance / 100.0F, 0.0F, 0.8F);
            if (count != 3) inaccuracy /= 5.0F;
            for (int n = 0; n < count; n++) {
                double angle = count == 1 ? 0.0D : -yawTotal / 2.0D + yawTotal * n / (count - 1);
                double rx = dx * Math.cos(angle) - dz * Math.sin(angle);
                double rz = dx * Math.sin(angle) + dz * Math.cos(angle);
                EntityDanmaku shot = new EntityDanmaku(worldObj, this)
                        .setDanmakuType(DanmakuType.random(rand)).setColor(DanmakuColor.random(rand))
                        .setDamage((float) getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue()
                                * (distanceFactor + bonus))
                        .setGravity(0).setImpedingLevel(impeding).setHurtEnderman(ender);
                shot.setThrowableHeading(rx, dy, rz, speed, inaccuracy);
                worldObj.spawnEntityInWorld(shot);
            }
            gohei.damageItem(1, this);
            if (gohei.stackSize <= 0) maidEquipmentInventory.setInventorySlotContents(0, null);
            maidEquipmentInventory.markDirty();
            swingItem();
            playSound("random.bow", 0.5F, 1.2F);
            playMaidVoice("maid.mode.range_attack");
            return;
        }
        int arrowSlot = findInventorySlot(Items.arrow);
        ItemStack bow = maidEquipmentInventory.getStackInSlot(0);
        if (bow == null || bow.getItem() != Items.bow || arrowSlot < 0 || target == null) return;

        EntityArrow arrow = new EntityArrow(worldObj, this, target, 1.6F, 4.0F);
        double baseAttack = getEntityAttribute(SharedMonsterAttributes.attackDamage).getBaseValue();
        arrow.setDamage(arrow.getDamage() * Math.max(0.0D, baseAttack / 2.0D));
        float bowDistance = getDistanceToEntity(target);
        arrow.setThrowableHeading(target.posX - posX,
                target.boundingBox.minY + target.height * 0.5D - (posY + getEyeHeight()),
                target.posZ - posZ,
                MathHelper.clamp_float(bowDistance / 10.0F, 1.6F, 3.2F),
                1.0F - MathHelper.clamp_float(bowDistance / 100.0F, 0.0F, 0.9F));
        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (power > 0) arrow.setDamage(arrow.getDamage() + power * 0.5D + 0.5D);
        if (punch > 0) arrow.setKnockbackStrength(punch);
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) arrow.setFire(100);

        boolean infinite = EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, bow) > 0;
        if (!infinite) consumeInventoryItem(arrowSlot);
        arrow.canBePickedUp = infinite ? 0 : 1;
        bow.damageItem(1, this);
        if (bow.stackSize <= 0) maidEquipmentInventory.setInventorySlotContents(0, null);
        maidEquipmentInventory.markDirty();
        swingItem();
        worldObj.playSoundAtEntity(this, "random.bow", 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        worldObj.spawnEntityInWorld(arrow);
        playMaidVoice("maid.mode.range_attack");
    }

    @SuppressWarnings("unchecked")
    private int countVisibleEnemiesOfType(EntityLivingBase target, double range) {
        int count = 0;
        List<EntityLivingBase> nearby = worldObj.getEntitiesWithinAABB(
                EntityLivingBase.class, boundingBox.expand(range, 8.0D, range));
        for (EntityLivingBase candidate : nearby) {
            if (candidate.getClass() == target.getClass() && candidate.isEntityAlive()
                    && canEntityBeSeen(candidate)
                    && isPositionWithinRestriction(candidate.posX, candidate.posY, candidate.posZ)
                    && !isOnSameTeam(candidate)
                    && candidate != getOwner()
                    && !(candidate instanceof EntityMaid && ((EntityMaid) candidate).getOwner() == getOwner())) count++;
        }
        return count;
    }

    @Override
    public EntityAgeable createChild(EntityAgeable mate) {
        return null;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setString(MODEL_ID_TAG, getModelId());
        tag.setString(SOUND_PACK_ID_TAG, getSoundPackId());
        tag.setString(TASK_TAG, taskId);
        tag.setInteger(HUNGER_TAG, getHunger());
        tag.setInteger(FAVORABILITY_TAG, getFavorability());
        tag.setInteger(EXPERIENCE_TAG, getMaidExperience());
        tag.setString(SCHEDULE_MODE_TAG, getSchedule().name());
        tag.setBoolean("MaidPickup", isPickupEnabled());
        tag.setBoolean("MaidHomeMode", isHomeMode());
        tag.setBoolean("Invulnerable", isMaidInvulnerable());
        tag.setTag(MAID_TASK_DATA_TAG,maidTaskData.copy());
        NBTTagList chat=new NBTTagList();synchronized(aiChatMessages){for(int i=0;i<aiChatMessages.size();i++){NBTTagCompound line=new NBTTagCompound();line.setString("Role",aiChatRoles.get(i));line.setString("Message",aiChatMessages.get(i));chat.appendTag(line);}}tag.setTag("MaidHistoryChat",chat);
        tag.setString(MAID_BACKPACK_TYPE, backpackType);
        NBTTagCompound gameSkill=new NBTTagCompound();gameSkill.setInteger("Gomoku",gomokuWins);tag.setTag("MaidGameSkillData",gameSkill);
        tag.setString("MaidBackpackFluid",backpackFluid);tag.setInteger("MaidBackpackFluidAmount",backpackFluidAmount);
        schedulePos.writeToNBT(tag);

        NBTTagList inventory = new NBTTagList();
        for (int slot = 0; slot < maidInventory.getSizeInventory(); slot++) {
            if (maidInventory.getStackInSlot(slot) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) slot);
                maidInventory.getStackInSlot(slot).writeToNBT(item);
                inventory.appendTag(item);
            }
        }
        tag.setTag(MAID_INVENTORY_TAG, inventory);
        NBTTagList baubles = new NBTTagList();
        for (int slot = 0; slot < maidBaubleInventory.getSizeInventory(); slot++) if (maidBaubleInventory.getStackInSlot(slot) != null) {
            NBTTagCompound item = new NBTTagCompound(); item.setByte("Slot", (byte) slot);
            maidBaubleInventory.getStackInSlot(slot).writeToNBT(item); baubles.appendTag(item);
        }
        tag.setTag(MAID_BAUBLE_INVENTORY_TAG, baubles);
        NBTTagList equipment = new NBTTagList();
        for (int slot = 0; slot < maidEquipmentInventory.getSizeInventory(); slot++) if (maidEquipmentInventory.getStackInSlot(slot) != null) {
            NBTTagCompound item = new NBTTagCompound(); item.setByte("Slot", (byte) slot);
            maidEquipmentInventory.getStackInSlot(slot).writeToNBT(item); equipment.appendTag(item);
        }
        tag.setTag(MAID_EQUIPMENT_INVENTORY_TAG, equipment);
        writeInventoryTag(tag, MAID_HIDE_INVENTORY_TAG, maidHideInventory);
        writeInventoryTag(tag, MAID_TASK_INVENTORY_TAG, maidTaskInventory);
        favorabilityManager.writeToNBT(tag);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        LegacyNbtMigration.normalize(tag);
        super.readEntityFromNBT(tag);
        if (tag.hasKey(MODEL_ID_TAG)) setModelId(tag.getString(MODEL_ID_TAG));
        if (tag.hasKey(SOUND_PACK_ID_TAG)) setSoundPackId(tag.getString(SOUND_PACK_ID_TAG));
        if (tag.hasKey(TASK_TAG)) taskId = tag.getString(TASK_TAG);
        if (!TaskManager.getTasks().containsKey(taskId)) taskId = TaskManager.IDLE_ID;
        dataWatcher.updateObject(WATCHER_TASK_INDEX, TaskManager.indexOf(taskId));
        setHunger(tag.hasKey(HUNGER_TAG) ? tag.getInteger(HUNGER_TAG) : 100);
        setFavorability(tag.getInteger(FAVORABILITY_TAG));
        setMaidExperience(tag.getInteger(EXPERIENCE_TAG));
        if (tag.hasKey(SCHEDULE_MODE_TAG, 8)) {
            setSchedule(MaidSchedule.byName(tag.getString(SCHEDULE_MODE_TAG)));
        } else {
            setScheduleMode(tag.getInteger(SCHEDULE_MODE_TAG));
        }
        setPickupEnabled(!tag.hasKey("MaidPickup") || tag.getBoolean("MaidPickup"));
        setHomeMode(tag.getBoolean("MaidHomeMode"));
        setMaidInvulnerable(tag.getBoolean("Invulnerable"));
        maidTaskData=tag.hasKey(MAID_TASK_DATA_TAG,10)?tag.getCompoundTag(MAID_TASK_DATA_TAG):new NBTTagCompound();
        synchronized(aiChatMessages){aiChatRoles.clear();aiChatMessages.clear();NBTTagList chat=tag.getTagList("MaidHistoryChat",10);for(int i=0;i<chat.tagCount();i++){NBTTagCompound line=chat.getCompoundTagAt(i);aiChatRoles.add(line.getString("Role"));aiChatMessages.add(line.getString("Message"));}}
        setBackpackType(tag.hasKey(MAID_BACKPACK_TYPE, 8) ? tag.getString(MAID_BACKPACK_TYPE) : "empty");
        gomokuWins=tag.hasKey("MaidGameSkillData",10)?tag.getCompoundTag("MaidGameSkillData").getInteger("Gomoku"):0;
        backpackFluid=normalizeFluidId(tag.getString("MaidBackpackFluid"));
        backpackFluidAmount=MathHelper.clamp_int(tag.getInteger("MaidBackpackFluidAmount"),0,BACKPACK_TANK_CAPACITY);
        schedulePos.readFromNBT(tag, this);

        for (int slot = 0; slot < maidInventory.getSizeInventory(); slot++) {
            maidInventory.setInventorySlotContents(slot, null);
        }
        NBTTagList inventory = tag.getTagList(MAID_INVENTORY_TAG, 10);
        for (int index = 0; index < inventory.tagCount(); index++) {
            NBTTagCompound item = inventory.getCompoundTagAt(index);
            int slot = item.getByte("Slot") & 255;
            if (slot < maidInventory.getSizeInventory()) {
                maidInventory.setInventorySlotContents(slot, net.minecraft.item.ItemStack.loadItemStackFromNBT(item));
            }
        }
        for (int slot = 0; slot < maidBaubleInventory.getSizeInventory(); slot++) maidBaubleInventory.setInventorySlotContents(slot, null);
        NBTTagList baubles = tag.getTagList(MAID_BAUBLE_INVENTORY_TAG, 10);
        for (int index = 0; index < baubles.tagCount(); index++) {
            NBTTagCompound item = baubles.getCompoundTagAt(index); int slot = item.getByte("Slot") & 255;
            if (slot < maidBaubleInventory.getSizeInventory()) maidBaubleInventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
        }
        for (int slot = 0; slot < maidEquipmentInventory.getSizeInventory(); slot++) maidEquipmentInventory.setInventorySlotContents(slot, null);
        NBTTagList equipment = tag.getTagList(MAID_EQUIPMENT_INVENTORY_TAG, 10);
        for (int index = 0; index < equipment.tagCount(); index++) {
            NBTTagCompound item = equipment.getCompoundTagAt(index); int slot = item.getByte("Slot") & 255;
            if (slot < maidEquipmentInventory.getSizeInventory()) maidEquipmentInventory.setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
        }
        syncEquipmentSlots();
        readInventoryTag(tag, MAID_HIDE_INVENTORY_TAG, maidHideInventory);
        readInventoryTag(tag, MAID_TASK_INVENTORY_TAG, maidTaskInventory);
        favorabilityManager.readFromNBT(tag);
    }

    public InventoryBasic getMaidInventory() {
        return maidInventory;
    }
    public ItemStack getOffhandItem() { return worldObj.isRemote ? dataWatcher.getWatchableObjectItemStack(WATCHER_OFFHAND) : maidEquipmentInventory.getStackInSlot(1); }
    public int getBaubleCapacity() { int level = favorabilityManager.getLevel(); return level < 2 ? 10 : level == 2 ? 20 : 30; }
    public static boolean isBauble(ItemStack stack) { return stack != null && (stack.getItem() instanceof ItemMaidBauble || stack.getItem() == ModItems.WIRELESS_IO); }
    public InventoryBasic getMaidBaubleInventory() { return maidBaubleInventory; }
    public InventoryBasic getMaidEquipmentInventory() { return maidEquipmentInventory; }
    public InventoryBasic getMaidHideInventory() { return maidHideInventory; }
    public InventoryBasic getMaidTaskInventory() { return maidTaskInventory; }
    public boolean hasFishingHook() { return dataWatcher.getWatchableObjectByte(WATCHER_FISHING_ACTIVE) != 0; }
    public void setFishingHookActive(boolean active) { dataWatcher.updateObject(WATCHER_FISHING_ACTIVE, active ? (byte) 1 : (byte) 0); }
    public FavorabilityManager getFavorabilityManager() { return favorabilityManager; }
    public NBTTagCompound getTaskData(String taskId){if(!maidTaskData.hasKey(taskId,10))maidTaskData.setTag(taskId,new NBTTagCompound());return maidTaskData.getCompoundTag(taskId);}
    public void markTaskDataDirty(){MaidBackupsManager.save(this);}
    public synchronized void addChatHistory(String role,String message){aiChatRoles.add(role);aiChatMessages.add(message);while(aiChatMessages.size()>LegacyConfig.chatHistorySize){aiChatRoles.remove(0);aiChatMessages.remove(0);}}
    public synchronized List<String[]> getChatHistory(){List<String[]> copy=new ArrayList<String[]>();for(int i=0;i<aiChatMessages.size();i++)copy.add(new String[]{aiChatRoles.get(i),aiChatMessages.get(i)});return copy;}
    public int getGomokuWins(){return gomokuWins;}
    public void recordBoardWin(String game){if("GomokuWin".equals(game))gomokuWins++;favorabilityManager.apply(game,"GomokuWin".equals(game)?8:4,"GomokuWin".equals(game)?12000:18000);Entity owner=getOwner();if(owner instanceof EntityPlayer){((EntityPlayer)owner).triggerAchievement(ModAchievements.BOARD_WIN);if(getFavorability()>=384)((EntityPlayer)owner).triggerAchievement(ModAchievements.DEVOTED);}}

    private void syncEquipmentSlots() {
        ItemStack offhand = maidEquipmentInventory.getStackInSlot(1);
        if (!ItemStack.areItemStacksEqual(offhand, dataWatcher.getWatchableObjectItemStack(WATCHER_OFFHAND)))
            dataWatcher.updateObject(WATCHER_OFFHAND, offhand == null ? null : offhand.copy());
        // Write only the vanilla mirror here. Calling our compatibility
        // override would feed the mirror back into the authoritative maid
        // inventory and makes third-party disarm handlers re-entrant.
        super.setCurrentItemOrArmor(0, maidEquipmentInventory.getStackInSlot(0));
        super.setCurrentItemOrArmor(1, maidEquipmentInventory.getStackInSlot(2));
        super.setCurrentItemOrArmor(2, maidEquipmentInventory.getStackInSlot(3));
        super.setCurrentItemOrArmor(3, maidEquipmentInventory.getStackInSlot(4));
        super.setCurrentItemOrArmor(4, maidEquipmentInventory.getStackInSlot(5));
    }

    /**
     * Compatibility contract for InfernalMobs/Compact InfernalMobs and other
     * 1.7.10 combat mods. They inspect and disarm EntityLivingBase through the
     * vanilla equipment API, while the port keeps equipment in a dedicated
     * inventory. Returning the authoritative server stack prevents them from
     * operating on a stale mirror.
     */
    @Override
    public ItemStack getHeldItem() {
        if (maidEquipmentInventory != null && worldObj != null && !worldObj.isRemote)
            return maidEquipmentInventory.getStackInSlot(0);
        return super.getHeldItem();
    }

    /** Mirrors external equipment mutations back into MaidEquipmentInventory. */
    @Override
    public void setCurrentItemOrArmor(int vanillaSlot, ItemStack stack) {
        super.setCurrentItemOrArmor(vanillaSlot, stack);
        if (maidEquipmentInventory == null || worldObj == null) return;
        int maidSlot = vanillaSlot == 0 ? 0 : vanillaSlot >= 1 && vanillaSlot <= 4 ? vanillaSlot + 1 : -1;
        if (maidSlot >= 0 && maidEquipmentInventory.getStackInSlot(maidSlot) != stack) {
            maidEquipmentInventory.setInventorySlotContents(maidSlot, stack);
            maidEquipmentInventory.markDirty();
        }
    }
    public int getBackpackCapacity() {
        String backpackType = getBackpackType();
        if ("maid_backpack_small".equals(backpackType)) return 12;
        if ("maid_backpack_middle".equals(backpackType)) return 24;
        if ("maid_backpack_big".equals(backpackType)) return 36;
        if ("ender_chest_backpack".equals(backpackType)) return 6;
        if (!"empty".equals(backpackType)) return 18;
        return 6;
    }
    public String getBackpackType() { return worldObj != null && worldObj.isRemote
            ? dataWatcher.getWatchableObjectString(WATCHER_BACKPACK_TYPE) : backpackType; }
    public String getBackpackFluid(){return backpackFluid;}public int getBackpackFluidAmount(){return backpackFluidAmount;}

    private void equipBackpack(EntityPlayer player, ItemStack held, ItemMaidBackpack backpack) {
        String nextType=backpack.getBackpackType();
        if(nextType.equals(backpackType))return;
        ItemStack previous = getEquippedBackpack();
        setBackpackType(nextType);
        if("tank_backpack".equals(nextType)&&held.hasTagCompound()){
            NBTTagCompound data=held.getTagCompound();
            if(data.hasKey("Tanks",10))data=data.getCompoundTag("Tanks");
            backpackFluid=normalizeFluidId(data.hasKey("FluidName",8)?data.getString("FluidName"):data.getString("Fluid"));
            backpackFluidAmount=MathHelper.clamp_int(data.getInteger("Amount"),0,BACKPACK_TANK_CAPACITY);
        }else{backpackFluid="";backpackFluidAmount=0;}
        if (!player.capabilities.isCreativeMode && --held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        if (previous != null && !player.inventory.addItemStackToInventory(previous)) player.entityDropItem(previous, 0.0F);
        dropUnavailableBackpackSlots();
    }

    private void takeOffBackpack(EntityPlayer player, ItemStack shears) {
        ItemStack previous=getEquippedBackpack();
        setBackpackType("empty");
        backpackFluid="";backpackFluidAmount=0;
        if(previous!=null&&!player.inventory.addItemStackToInventory(previous))player.entityDropItem(previous,0.0F);
        dropUnavailableBackpackSlots();
        shears.damageItem(1,player);
        worldObj.playSoundAtEntity(this,"mob.horse.leather",0.5F,1.0F);
    }

    private void dropUnavailableBackpackSlots() {
        for (int slot = getBackpackCapacity(); slot < maidInventory.getSizeInventory(); slot++) {
            ItemStack overflow = maidInventory.getStackInSlot(slot);
            if (overflow != null) { entityDropItem(overflow, 0.0F); maidInventory.setInventorySlotContents(slot, null); }
        }
    }

    public ItemStack getEquippedBackpack() {
        if ("maid_backpack_small".equals(backpackType)) return new ItemStack(ModItems.MAID_BACKPACK_SMALL);
        if ("maid_backpack_middle".equals(backpackType)) return new ItemStack(ModItems.MAID_BACKPACK_MIDDLE);
        if ("maid_backpack_big".equals(backpackType)) return new ItemStack(ModItems.MAID_BACKPACK_BIG);
        if ("crafting_table_backpack".equals(backpackType)) return new ItemStack(ModItems.CRAFTING_TABLE_BACKPACK);
        if ("ender_chest_backpack".equals(backpackType)) return new ItemStack(ModItems.ENDER_CHEST_BACKPACK);
        if ("furnace_backpack".equals(backpackType)) return new ItemStack(ModItems.FURNACE_BACKPACK);
        if ("tank_backpack".equals(backpackType)) { ItemStack stack=new ItemStack(ModItems.TANK_BACKPACK);
            if(backpackFluidAmount>0){NBTTagCompound data=new NBTTagCompound();data.setString("Fluid",backpackFluid);data.setInteger("Amount",backpackFluidAmount);
                NBTTagCompound tanks=new NBTTagCompound();tanks.setString("FluidName",modernFluidId(backpackFluid));tanks.setInteger("Amount",backpackFluidAmount);data.setTag("Tanks",tanks);stack.setTagCompound(data);}return stack; }
        return null;
    }

    private void setBackpackType(String value){
        backpackType=normalizeBackpackType(value);
        dataWatcher.updateObject(WATCHER_BACKPACK_TYPE,backpackType);
    }
    private static String normalizeBackpackType(String value){
        if(value==null)return"empty";int split=value.indexOf(':');if(split>=0)value=value.substring(split+1);
        if("small_backpack".equals(value))value="maid_backpack_small";
        else if("middle_backpack".equals(value))value="maid_backpack_middle";
        else if("big_backpack".equals(value))value="maid_backpack_big";
        else if("tank".equals(value))value="tank_backpack";
        if("maid_backpack_small".equals(value)||"maid_backpack_middle".equals(value)||"maid_backpack_big".equals(value)
                ||"crafting_table_backpack".equals(value)||"ender_chest_backpack".equals(value)
                ||"furnace_backpack".equals(value)||"tank_backpack".equals(value))return value;
        return"empty";
    }

    private static String normalizeFluidId(String value){return value!=null&&value.startsWith("minecraft:")?value.substring(10):value==null?"":value;}
    private static String modernFluidId(String value){return "water".equals(value)||"lava".equals(value)?"minecraft:"+value:value;}

    private void tickSpecialBackpack(){
        if("furnace_backpack".equals(backpackType)&&ticksExisted%200==0){
            int input=-1,fuel=-1;ItemStack result=null;
            for(int slot=0;slot<getBackpackCapacity();slot++){ItemStack stack=maidInventory.getStackInSlot(slot);if(stack==null)continue;if(input<0){ItemStack smelt=FurnaceRecipes.smelting().getSmeltingResult(stack);if(smelt!=null){input=slot;result=smelt.copy();}}if(fuel<0&&TileEntityFurnace.isItemFuel(stack))fuel=slot;}
            if(input>=0&&fuel>=0&&result!=null){ItemStack in=maidInventory.getStackInSlot(input),burn=maidInventory.getStackInSlot(fuel);if(--in.stackSize<=0)maidInventory.setInventorySlotContents(input,null);if(--burn.stackSize<=0)maidInventory.setInventorySlotContents(fuel,null);ItemStack left=addToMaidInventory(result);if(left!=null)entityDropItem(left,0);playSound("random.fizz",0.4F,1.4F);}
        }
        if("tank_backpack".equals(backpackType)&&ticksExisted%20==0&&backpackFluidAmount<=BACKPACK_TANK_CAPACITY-1000){
            for(int slot=0;slot<getBackpackCapacity();slot++){ItemStack stack=maidInventory.getStackInSlot(slot);if(stack==null)continue;String fluid=stack.getItem()==Items.water_bucket?"water":stack.getItem()==Items.lava_bucket?"lava":stack.getItem()==Items.milk_bucket?"milk":"";if(!fluid.isEmpty()&&(backpackFluid.isEmpty()||backpackFluid.equals(fluid))){backpackFluid=fluid;backpackFluidAmount+=1000;if(--stack.stackSize<=0)maidInventory.setInventorySlotContents(slot,null);ItemStack left=addToMaidInventory(new ItemStack(Items.bucket));if(left!=null)entityDropItem(left,0);break;}}
        }
    }

    public boolean hasBowAndArrow() {
        ItemStack held = maidEquipmentInventory.getStackInSlot(0);
        return held != null && held.getItem() == Items.bow && findInventorySlot(Items.arrow) >= 0;
    }

    public boolean hasGohei() {
        ItemStack held = maidEquipmentInventory.getStackInSlot(0);
        return held != null && (held.getItem() == ModItems.HAKUREI_GOHEI
                || held.getItem() == ModItems.SANAE_GOHEI);
    }

    public boolean hasRangedWeaponForCurrentTask() {
        return (TaskManager.RANGED_ATTACK_ID.equals(getTaskId()) && hasBowAndArrow())
                || (TaskManager.DANMAKU_ATTACK_ID.equals(getTaskId()) && hasGohei())
                || (TaskManager.CROSSBOW_ATTACK_ID.equals(getTaskId()) && isMainhand(ModItems.CROSSBOW) && findInventorySlot(Items.arrow)>=0)
                || (TaskManager.TRIDENT_ATTACK_ID.equals(getTaskId()) && isMainhand(ModItems.TRIDENT));
    }

    private boolean isMainhand(net.minecraft.item.Item item) {
        ItemStack held = maidEquipmentInventory.getStackInSlot(0);
        return held != null && held.getItem() == item;
    }

    private int findGoheiSlot() {
        int slot = findInventorySlot(ModItems.HAKUREI_GOHEI);
        return slot >= 0 ? slot : findInventorySlot(ModItems.SANAE_GOHEI);
    }

    public int findInventorySlot(net.minecraft.item.Item item) {
        for (int slot = 0; slot < maidTaskInventory.getSizeInventory(); slot++) {
            ItemStack stack = maidTaskInventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() == item && stack.stackSize > 0) return 200 + slot;
        }
        for (int slot = 0; slot < maidInventory.getSizeInventory(); slot++) {
            ItemStack stack = maidInventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() == item && stack.stackSize > 0) return slot;
        }
        return -1;
    }

    private void consumeInventoryItem(int slot) {
        InventoryBasic inventory = logicalInventory(slot); int real = logicalIndex(slot);
        ItemStack stack = inventory.getStackInSlot(real);
        if (stack != null && --stack.stackSize <= 0) inventory.setInventorySlotContents(real, null);
        inventory.markDirty();
    }

    public ItemStack takeOneFromSlot(int slot) {
        InventoryBasic inventory = logicalInventory(slot); int real = logicalIndex(slot);
        if (real < 0 || real >= inventory.getSizeInventory()) return null;
        ItemStack stack = inventory.getStackInSlot(real);
        if (stack == null) return null;
        ItemStack result = stack.copy();
        result.stackSize = 1;
        if (--stack.stackSize <= 0) inventory.setInventorySlotContents(real, null);
        inventory.markDirty();
        return result;
    }
    public ItemStack getStackInLogicalSlot(int slot) { InventoryBasic inv = logicalInventory(slot); int real = logicalIndex(slot); return real >= 0 && real < inv.getSizeInventory() ? inv.getStackInSlot(real) : null; }
    public void clearLogicalSlot(int slot) { InventoryBasic inv=logicalInventory(slot);int real=logicalIndex(slot);if(real>=0&&real<inv.getSizeInventory())inv.setInventorySlotContents(real,null); }
    public void markLogicalInventoryDirty(int slot) { logicalInventory(slot).markDirty(); }
    private InventoryBasic logicalInventory(int slot) { return slot >= 200 ? maidTaskInventory : maidInventory; }
    private int logicalIndex(int slot) { return slot >= 200 ? slot - 200 : slot; }

    private static void writeInventoryTag(NBTTagCompound root, String key, InventoryBasic inventory) {
        NBTTagList list=new NBTTagList();for(int slot=0;slot<inventory.getSizeInventory();slot++)if(inventory.getStackInSlot(slot)!=null){NBTTagCompound item=new NBTTagCompound();item.setByte("Slot",(byte)slot);inventory.getStackInSlot(slot).writeToNBT(item);list.appendTag(item);}root.setTag(key,list);
    }
    private static void readInventoryTag(NBTTagCompound root,String key,InventoryBasic inventory){for(int slot=0;slot<inventory.getSizeInventory();slot++)inventory.setInventorySlotContents(slot,null);NBTTagList list=root.getTagList(key,10);for(int n=0;n<list.tagCount();n++){NBTTagCompound item=list.getCompoundTagAt(n);int slot=item.getByte("Slot")&255;if(slot<inventory.getSizeInventory())inventory.setInventorySlotContents(slot,ItemStack.loadItemStackFromNBT(item));}}
    private static void moveInventoryToTombstone(InventoryBasic inventory,EntityTombstone tombstone){for(int slot=0;slot<inventory.getSizeInventory();slot++){ItemStack stack=inventory.getStackInSlot(slot);if(stack!=null){tombstone.insertItem(stack);inventory.setInventorySlotContents(slot,null);}}}

    public int getHunger() { return dataWatcher.getWatchableObjectInt(WATCHER_HUNGER); }
    public void setHunger(int value) { dataWatcher.updateObject(WATCHER_HUNGER, clamp(value, 0, 100)); }
    public int getFavorability() { return dataWatcher.getWatchableObjectInt(WATCHER_FAVORABILITY); }
    public void setFavorability(int value) { dataWatcher.updateObject(WATCHER_FAVORABILITY, Math.max(0, value)); }
    public int getMaidExperience() { return dataWatcher.getWatchableObjectInt(WATCHER_EXPERIENCE); }
    public void setMaidExperience(int value) { dataWatcher.updateObject(WATCHER_EXPERIENCE, Math.max(0, value)); }
    public int getScheduleMode() { return dataWatcher.getWatchableObjectInt(WATCHER_SCHEDULE); }
    public void setScheduleMode(int value) { dataWatcher.updateObject(WATCHER_SCHEDULE, clamp(value, 0, 2)); }
    public MaidSchedule getSchedule() { return MaidSchedule.byOrdinal(getScheduleMode()); }
    public void setSchedule(MaidSchedule value) { setScheduleMode(value.ordinal()); updateScheduleActivity(); }
    public MaidActivity getCurrentActivity() { return MaidActivity.byOrdinal(dataWatcher.getWatchableObjectByte(WATCHER_ACTIVITY)); }

    public boolean isPickupEnabled() { return hasFlag(FLAG_PICKUP); }
    public void setPickupEnabled(boolean value) { setMaidFlag(FLAG_PICKUP, value); }
    public boolean isHomeMode() { return hasFlag(FLAG_HOME_MODE); }
    public void setHomeMode(boolean value) {
        boolean changed = isHomeMode() != value;
        setMaidFlag(FLAG_HOME_MODE, value);
        if (value) schedulePos.enableAt(this); else detachHome();
        // A navigator path survives removal of its restriction. Without this,
        // turning H off can still make the maid finish walking to the former
        // home point (often the place where a Shrine Lamp used to stand).
        if (changed) getNavigator().clearPathEntity();
    }
    public boolean isMaidInvulnerable() { return hasFlag(FLAG_INVULNERABLE); }
    public boolean isBegging() { return hasFlag(FLAG_BEGGING); }
    public boolean isMaidSleeping() { return hasFlag(FLAG_SLEEPING); }
    public void setMaidInvulnerable(boolean value) { setMaidFlag(FLAG_INVULNERABLE, value); }
    public void setMaidSitting(boolean value) {
        aiSit.setSitting(value);
        setSitting(value);
        if (value) {
            setAttackTarget(null);
            getNavigator().clearPathEntity();
        }
    }

    public String getModelId() {
        return worldObj != null && worldObj.isRemote ? dataWatcher.getWatchableObjectString(WATCHER_MODEL_ID) : modelId;
    }
    public void setModelId(String value) {
        modelId = value == null ? "" : value;
        dataWatcher.updateObject(WATCHER_MODEL_ID, modelId);
    }

    @Override
    public net.minecraft.entity.IEntityLivingData onSpawnWithEgg(net.minecraft.entity.IEntityLivingData data) {
        data = super.onSpawnWithEgg(data);
        setModelId(MaidModelIdCatalog.random(rand));
        return data;
    }
    public String getSoundPackId() {
        return worldObj != null && worldObj.isRemote ? dataWatcher.getWatchableObjectString(WATCHER_SOUND_PACK_ID) : soundPackId;
    }
    public void setSoundPackId(String value) {
        soundPackId = value == null ? "" : value;
        dataWatcher.updateObject(WATCHER_SOUND_PACK_ID, soundPackId);
    }
    public boolean isMuted() { return findBauble(ItemMaidBauble.Type.MUTE) >= 0; }
    public void playMaidVoice(String event) {
        if (!worldObj.isRemote && !isMuted()) worldObj.playSoundAtEntity(this, TouhouLittleMaid.MOD_ID + ":" + event, 0.9F, 0.95F + rand.nextFloat() * 0.1F);
    }
    private void tickMaidVoice() {
        long dayTime = worldObj.getWorldTime() % 24000L;
        if (dayTime == 20L) playMaidVoice("maid.environment.morning");
        else if (dayTime == 12520L) playMaidVoice("maid.environment.night");
        else if (ticksExisted % 600 == 0 && rand.nextInt(3) == 0) playMaidVoice("maid.mode.idle");
    }
    public String getTaskId() {
        return worldObj != null && worldObj.isRemote
                ? TaskManager.getByIndex(dataWatcher.getWatchableObjectInt(WATCHER_TASK_INDEX)).getId()
                : taskId;
    }
    public void setTaskId(String value) { TaskManager.switchTask(this, value); }
    public void setTaskIdInternal(String value) {
        taskId = value == null ? TaskManager.IDLE_ID : value;
        dataWatcher.updateObject(WATCHER_TASK_INDEX, TaskManager.indexOf(taskId));
        playMaidVoice(taskVoice(taskId));
    }
    private String taskVoice(String id) {
        if (TaskManager.ATTACK_ID.equals(id)) return "maid.mode.attack";
        if (TaskManager.RANGED_ATTACK_ID.equals(id) || TaskManager.CROSSBOW_ATTACK_ID.equals(id)
                || TaskManager.TRIDENT_ATTACK_ID.equals(id)) return "maid.mode.range_attack";
        if (TaskManager.DANMAKU_ATTACK_ID.equals(id)) return "maid.mode.danmaku_attack";
        if (TaskManager.FARM_ID.equals(id) || TaskManager.SUGAR_CANE_ID.equals(id)
                || TaskManager.MELON_ID.equals(id) || TaskManager.COCOA_ID.equals(id)
                || TaskManager.GRASS_ID.equals(id)) return "maid.mode.farm";
        if (TaskManager.SNOW_ID.equals(id)) return "maid.mode.snow";
        if (TaskManager.FEED_OWNER_ID.equals(id)) return "maid.mode.feed";
        if (TaskManager.FEED_ANIMAL_ID.equals(id)) return "maid.mode.feed_animal";
        if (TaskManager.SHEARS_ID.equals(id)) return "maid.mode.shears";
        if (TaskManager.MILK_ID.equals(id)) return "maid.mode.milk";
        if (TaskManager.TORCH_ID.equals(id)) return "maid.mode.torch";
        if (TaskManager.EXTINGUISHING_ID.equals(id)) return "maid.mode.extinguishing";
        if (TaskManager.HONEY_ID.equals(id)) return "maid.mode.feed";
        if (TaskManager.MINER_ID.equals(id)) return "maid.mode.farm";
        return "maid.mode.idle";
    }
    public boolean canEngageCombat() { return getHealth() >= getMaxHealth() * 0.25F; }
    public boolean isWorkingNow() { return getCurrentActivity() == MaidActivity.WORK; }
    public SchedulePos getSchedulePos() { return schedulePos; }

    public void setRestriction(int x, int y, int z, int radius) {
        setHomeArea(x, y, z, radius);
    }

    public boolean isWithinRestriction() {
        if (!isHomeMode()) return true;
        SchedulePos.Point point = schedulePos.getForActivity(getCurrentActivity());
        double dx = posX - (point.x + 0.5D);
        double dy = posY - point.y;
        double dz = posZ - (point.z + 0.5D);
        int radius = getRestrictionRadius();
        return dx * dx + dy * dy + dz * dz < radius * radius;
    }

    public boolean isPositionWithinRestriction(double x, double y, double z) {
        if (!isHomeMode()) return true;
        SchedulePos.Point point = schedulePos.getForActivity(getCurrentActivity());
        double dx = x - (point.x + 0.5D);
        double dy = y - point.y;
        double dz = z - (point.z + 0.5D);
        int radius = getRestrictionRadius();
        return dx * dx + dy * dy + dz * dz < radius * radius;
    }

    public int getRestrictionRadius() {
        if (getCurrentActivity() == MaidActivity.WORK) return SchedulePos.WORK_RANGE;
        if (getCurrentActivity() == MaidActivity.REST) return SchedulePos.SLEEP_RANGE;
        return SchedulePos.IDLE_RANGE;
    }

    private void updateScheduleActivity() {
        MaidActivity previous=getCurrentActivity();MaidActivity activity = getSchedule().getActivity(worldObj.getWorldTime());
        if (getCurrentActivity() != activity) {
            if(previous==MaidActivity.REST&&ridingEntity instanceof EntitySit&&"bed".equals(((EntitySit)ridingEntity).getJoyType()))mountEntity(null);
            dataWatcher.updateObject(WATCHER_ACTIVITY, (byte) activity.ordinal());
            setAttackTarget(null);
            if (isHomeMode()) schedulePos.restrictTo(this);
        }
    }

    private void updateHungerAndHealing() {
        // Same natural regeneration rate as SRC randomRestoreHealth.
        if (getHealth() < getMaxHealth() && rand.nextFloat() < 0.0025F) heal(1);
        // SRC has no periodic starvation damage. Work meals are throttled by
        // favorability rather than an artificial survival hunger timer.
        if (!isTamed() || isMaidSleeping() || !isPeriodicTick(50)
                || !favorabilityManager.canApply("WorkMeal")) return;
        for (int hand = 0; hand < 2; hand++) if (eatWorkMeal(maidEquipmentInventory, hand)) return;
        for (int slot = 0; slot < getBackpackCapacity(); slot++) if (eatWorkMeal(maidInventory, slot)) return;
    }

    private boolean isSafeMeal(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemFood)) return false;
        return stack.getItem() != Items.poisonous_potato && stack.getItem() != Items.rotten_flesh
                && stack.getItem() != Items.spider_eye
                && !(stack.getItem() == Items.fish && stack.getItemDamage() == 3);
    }

    private boolean eatWorkMeal(InventoryBasic inventory, int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (!isSafeMeal(stack)) return false;
        ItemFood food = (ItemFood)stack.getItem();
        ItemStack used = stack.copy(); used.stackSize = 1;
        int nutrition = food.func_150905_g(used);
        float total = nutrition + nutrition * food.func_150906_h(used) * 2;
        // ItemFood in 1.7 accepts players only. Keep its callback and remainder
        // without applying food effects to the real owner or spawning an actor.
        net.minecraftforge.common.util.FakePlayer actor = new net.minecraftforge.common.util.FakePlayer(
                (net.minecraft.world.WorldServer)worldObj, new com.mojang.authlib.GameProfile(getUniqueID(), "[TLM Meal]"));
        actor.setPosition(posX, posY, posZ);
        actor.inventory.setInventorySlotContents(0, used);
        ItemStack remainder = food.onEaten(used, worldObj, actor);
        for (Object effect : actor.getActivePotionEffects())
            addPotionEffect(new net.minecraft.potion.PotionEffect((net.minecraft.potion.PotionEffect)effect));
        inventory.decrStackSize(slot, 1);
        if (remainder != null && remainder.stackSize > 0) {
            ItemStack left = addToMaidInventory(remainder);
            if (left != null) entityDropItem(left, 0);
        }
        setHunger(getHunger() + nutrition * 2);
        favorabilityManager.apply("WorkMeal", rand.nextInt(100) < total ? 0 : 1, 3600);
        worldObj.playSoundAtEntity(this, "random.eat", 0.5F, 1);
        return true;
    }

    private void tickHomeBehaviors() {
        boolean rest = isHomeMode() && !isSitting() && getCurrentActivity() == MaidActivity.REST;
        if (ridingEntity instanceof EntitySit && "bed".equals(((EntitySit)ridingEntity).getJoyType()) && !rest) {
            Entity seat = ridingEntity; mountEntity(null); seat.setDead();
        }
        setMaidFlag(FLAG_SLEEPING, rest && ridingEntity instanceof EntitySit && "bed".equals(((EntitySit)ridingEntity).getJoyType()));
        if (isSitting()) { setMaidFlag(FLAG_BEGGING, false); return; }
        Entity owner=getOwner();ItemStack temptation=owner instanceof EntityPlayer?((EntityPlayer)owner).getCurrentEquippedItem():null;
        boolean begging=owner!=null&&getDistanceSqToEntity(owner)<36&&temptation!=null&&(temptation.getItem()==Items.cake||temptation.getItem() instanceof ItemFood);
        setMaidFlag(FLAG_BEGGING,begging);if(begging){getLookHelper().setLookPositionWithEntity(owner,20,20);if(getDistanceSqToEntity(owner)>4)getNavigator().tryMoveToEntityLiving(owner,.6D);}
        if (rest) { if (!isRiding() && isPeriodicTick(20)) seekBedAndRest(); return; }
        if (!isHomeMode()) return;
        if(getCurrentActivity()!=MaidActivity.IDLE||begging||isRiding())return;
        if(getHunger()<80&&isPeriodicTick(100)&&eatNearbyHomeMeal())return;
        if(isPeriodicTick(200))seekJoyBlock();
    }
    @SuppressWarnings("unchecked") private boolean eatNearbyHomeMeal(){for(Object value:worldObj.loadedTileEntityList){net.minecraft.tileentity.TileEntity tile=(net.minecraft.tileentity.TileEntity)value;if(!(tile instanceof TileEntityInventory)||distanceToTile(tile)>64)continue;TileEntityInventory inv=(TileEntityInventory)tile;for(int slot=0;slot<inv.getSizeInventory();slot++){ItemStack stack=inv.getStackInSlot(slot);if(isSafeMeal(stack)){if(inv instanceof com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat&&!isRiding())((com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat)inv).seatMaid(this);int food=((ItemFood)stack.getItem()).func_150905_g(stack);inv.decrStackSize(slot,1);setHunger(getHunger()+food*2);heal(Math.max(1,food*.5F));favorabilityManager.apply(inv instanceof com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat?"OnHomeMeal":"HomeMeal",1,inv instanceof com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat?24000:1200);playMaidVoice("maid.mode.feed");return true;}}}return false;}
    @SuppressWarnings("unchecked") private void seekJoyBlock(){for(Object value:worldObj.loadedTileEntityList){net.minecraft.tileentity.TileEntity tile=(net.minecraft.tileentity.TileEntity)value;if(!(tile instanceof TileEntityJoy)||distanceToTile(tile)>64||!(worldObj.getBlock(tile.xCoord,tile.yCoord,tile.zCoord) instanceof BlockJoy)||((TileEntityJoy)tile).getSitEntity()!=null)continue;int x=tile.xCoord,y=tile.yCoord,z=tile.zCoord;if(getDistanceSq(x+.5,y+.5,z+.5)>4){getNavigator().tryMoveToXYZ(x+.5,y,z+.5,.6D);return;}BlockJoy joy=(BlockJoy)worldObj.getBlock(x,y,z);EntitySit sit=new EntitySit(worldObj,x+.5,y+joy.getSitYOffset(),z+.5,joy.getJoyType(),x,y,z);sit.rotationYaw=(worldObj.getBlockMetadata(x,y,z)&3)*90.0F+joy.getSitYawOffset();worldObj.spawnEntityInWorld(sit);((TileEntityJoy)tile).setSitEntity(sit);mountEntity(sit);return;}}
    private void seekBedAndRest() {
        SchedulePos.Point p = schedulePos.getForActivity(MaidActivity.REST);
        if (schedulePos.getDimension() != dimension) return;
        for (Object value : worldObj.loadedTileEntityList) {
            net.minecraft.tileentity.TileEntity tile = (net.minecraft.tileentity.TileEntity)value;
            if (!(tile instanceof com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed)
                    || Math.abs(tile.xCoord-p.x)>8 || Math.abs(tile.yCoord-p.y)>2 || Math.abs(tile.zCoord-p.z)>8) continue;
            int x=tile.xCoord,y=tile.yCoord,z=tile.zCoord;
            boolean occupied = false;
            for (Object entity : worldObj.loadedEntityList) if (entity instanceof EntitySit && !((EntitySit)entity).isDead
                    && "bed".equals(((EntitySit)entity).getJoyType()) && ((EntitySit)entity).isAssociatedWith(x,y,z)) { occupied=true; break; }
            if (occupied) continue;
            if (getDistanceSq(x+.5,y+.5,z+.5)>4) { getNavigator().tryMoveToXYZ(x+.5,y+.5,z+.5,.6D); return; }
            EntitySit seat=new EntitySit(worldObj,x+.5,y+.8,z+.5,"bed",x,y,z);
            if (worldObj.spawnEntityInWorld(seat)) { mountEntity(seat); getNavigator().clearPathEntity(); setMaidFlag(FLAG_SLEEPING,true); }
            return;
        }
    }
    private double distanceToTile(net.minecraft.tileentity.TileEntity tile){double dx=tile.xCoord+.5D-posX,dy=tile.yCoord+.5D-posY,dz=tile.zCoord+.5D-posZ;return dx*dx+dy*dy+dz*dz;}

    @SuppressWarnings("unchecked")
    private void pickupNearbyItems() {
        double range = findBauble(ItemMaidBauble.Type.MAGNET) >= 0 ? 6.0D : 0.75D;
        List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(range, range, range));
        for (EntityItem item : items) {
            if (!item.isDead && item.delayBeforeCanPickup <= 0) {
                ItemStack remaining = addToMaidInventory(item.getEntityItem());
                if (remaining == null || remaining.stackSize <= 0) {
                    item.setDead();
                } else {
                    item.setEntityItemStack(remaining);
                }
            }
        }
        List<EntityXPOrb> orbs = worldObj.getEntitiesWithinAABB(EntityXPOrb.class, boundingBox.expand(range, range, range));
        for (EntityXPOrb orb : orbs) {
            if (!orb.isDead) {
                setMaidExperience(getMaidExperience() + orb.xpValue);
                orb.setDead();
            }
        }
    }

    private int findBauble(ItemMaidBauble.Type type) {
        for (int slot = 0; slot < getBaubleCapacity(); slot++) {
            ItemStack stack = maidBaubleInventory.getStackInSlot(slot);
            if (stack != null && stack.getItem() instanceof ItemMaidBauble
                    && ((ItemMaidBauble) stack.getItem()).getType() == type) return 100 + slot;
        }
        return -1;
    }

    private void damageBauble(int slot) {
        InventoryBasic inventory = slot >= 100 ? maidBaubleInventory : maidInventory; if (slot >= 100) slot -= 100;
        ItemStack stack = inventory.getStackInSlot(slot); if (stack == null) return;
        if (stack.isItemStackDamageable()) stack.damageItem(1, this); else --stack.stackSize;
        if (stack.stackSize <= 0) inventory.setInventorySlotContents(slot, null); inventory.markDirty();
    }

    private boolean teleportAway() {
        for (int attempt = 0; attempt < 16; attempt++) {
            int x = net.minecraft.util.MathHelper.floor_double(posX) + rand.nextInt(13) - 6;
            int z = net.minecraft.util.MathHelper.floor_double(posZ) + rand.nextInt(13) - 6;
            int y = net.minecraft.util.MathHelper.floor_double(posY) + rand.nextInt(7) - 3;
            while (y > 1 && worldObj.isAirBlock(x, y - 1, z)) y--;
            if (worldObj.isAirBlock(x, y, z) && worldObj.isAirBlock(x, y + 1, z)
                    && worldObj.getBlock(x, y - 1, z).getMaterial().blocksMovement()) {
                setPositionAndUpdate(x + 0.5D, y, z + 0.5D); getNavigator().clearPathEntity(); return true;
            }
        }
        return false;
    }

    /** Cross-dimension follow and a collision-safe fallback when pathfinding cannot reach the owner. */
    private void followOwnerFallback() {
        EntityPlayer owner = findOnlineOwner(); if (owner == null) return;
        if (owner.dimension != dimension && LegacyConfig.crossDimensionFollow) {
            travelToDimension(owner.dimension); return;
        }
        if (getDistanceSqToEntity(owner) > 24.0D * 24.0D) safeTeleportNear(owner);
    }

    private EntityPlayer findOnlineOwner() {
        EntityLivingBase local = getOwner(); if (local instanceof EntityPlayer) return (EntityPlayer) local;
        String ownerId = func_152113_b(); if (ownerId == null || ownerId.isEmpty() || MinecraftServer.getServer() == null) return null;
        @SuppressWarnings("unchecked") java.util.List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (EntityPlayerMP player : players) if (player.getUniqueID().toString().equals(ownerId)) return player;
        return null;
    }

    private int countOwnerMaids(EntityPlayer owner) {
        int count=0;if(MinecraftServer.getServer()==null)return count;
        for(net.minecraft.world.WorldServer server:MinecraftServer.getServer().worldServers){
            @SuppressWarnings("unchecked") java.util.List<Entity> entities=server.loadedEntityList;
            for(Entity entity:entities)if(entity instanceof EntityMaid&&((EntityMaid)entity).isTamed()&&owner.getUniqueID().toString().equals(((EntityMaid)entity).func_152113_b()))count++;
        }
        return count;
    }
    public boolean canOwnerAddMaid(EntityPlayer owner){return countOwnerMaids(owner)<LegacyConfig.maxMaidPerPlayer;}
    public String getOwnerId(){String id=func_152113_b();return id==null?"":id;}
    public boolean isPeriodicTick(int interval){return interval>0&&Math.floorMod(ticksExisted+getEntityId(),interval)==0;}

    private void tickFenceGate(){if(hasOpenedGate){openedGateTicks++;double distance=getDistanceSq(openedGateX+.5D,openedGateY+.5D,openedGateZ+.5D);if(openedGateTicks>40||distance>6.25D){net.minecraft.block.Block block=worldObj.getBlock(openedGateX,openedGateY,openedGateZ);if(block instanceof net.minecraft.block.BlockFenceGate){int meta=worldObj.getBlockMetadata(openedGateX,openedGateY,openedGateZ);if((meta&4)!=0){worldObj.setBlockMetadataWithNotify(openedGateX,openedGateY,openedGateZ,meta&~4,3);worldObj.playAuxSFXAtEntity(null,1003,openedGateX,openedGateY,openedGateZ,0);}}hasOpenedGate=false;}}
        if(hasOpenedGate||isSitting())return;double aheadX=posX-Math.sin(Math.toRadians(rotationYaw))*0.8D,aheadZ=posZ+Math.cos(Math.toRadians(rotationYaw))*0.8D;int centerX=(int)Math.floor(aheadX),centerY=(int)Math.floor(boundingBox.minY),centerZ=(int)Math.floor(aheadZ);for(int y=centerY;y<=centerY+1;y++)for(int x=centerX-1;x<=centerX+1;x++)for(int z=centerZ-1;z<=centerZ+1;z++){net.minecraft.block.Block block=worldObj.getBlock(x,y,z);if(!(block instanceof net.minecraft.block.BlockFenceGate))continue;int meta=worldObj.getBlockMetadata(x,y,z);if((meta&4)==0&&getDistanceSq(x+.5D,y+.5D,z+.5D)<3.0D){worldObj.setBlockMetadataWithNotify(x,y,z,meta|4,3);worldObj.playAuxSFXAtEntity(null,1003,x,y,z,0);openedGateX=x;openedGateY=y;openedGateZ=z;openedGateTicks=0;hasOpenedGate=true;return;}}}

    private int getWirelessRange(){return getCurrentActivity()==MaidActivity.REST?SchedulePos.SLEEP_RANGE:getCurrentActivity()==MaidActivity.IDLE?SchedulePos.IDLE_RANGE:SchedulePos.WORK_RANGE;}
    private void tickWirelessIO(){for(int slot=0;slot<getBaubleCapacity();slot++){ItemStack link=maidBaubleInventory.getStackInSlot(slot);if(link==null||link.getItem()!=ModItems.WIRELESS_IO||!link.hasTagCompound()||!link.getTagCompound().hasKey(com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO.DATA,10))continue;NBTTagCompound data=link.getTagCompound().getCompoundTag(com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO.DATA);if(data.getInteger("DimensionId")!=dimension)continue;int x=data.getInteger("X"),y=data.getInteger("Y"),z=data.getInteger("Z");if(!worldObj.blockExists(x,y,z)||getDistanceSq(x+.5,y+.5,z+.5)>getWirelessRange()*getWirelessRange()||!isPositionWithinRestriction(x+.5,y,z+.5))continue;net.minecraft.tileentity.TileEntity tile=worldObj.getTileEntity(x,y,z);if(!(tile instanceof net.minecraft.inventory.IInventory))continue;net.minecraft.inventory.IInventory chest=(net.minecraft.inventory.IInventory)tile;if(data.getBoolean("MaidToChest"))transferMaidToChest(chest,data);else transferChestToMaid(chest,data);return;}}
    private void transferChestToMaid(net.minecraft.inventory.IInventory chest,NBTTagCompound data){for(int slot=0;slot<chest.getSizeInventory();slot++){ItemStack source=chest.getStackInSlot(slot);if(source==null||!com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO.matchesFilter(data,source))continue;ItemStack one=source.copy();one.stackSize=1;if(addToMaidInventory(one)==null){chest.decrStackSize(slot,1);chest.markDirty();}return;}}
    private void transferMaidToChest(net.minecraft.inventory.IInventory chest,NBTTagCompound data){for(int sourceSlot=0;sourceSlot<maidInventory.getSizeInventory();sourceSlot++){ItemStack source=maidInventory.getStackInSlot(sourceSlot);if(source==null||source.getItem()==ModItems.WIRELESS_IO||!com.github.tartaricacid.touhoulittlemaid.item.ItemWirelessIO.matchesFilter(data,source))continue;for(int slot=0;slot<chest.getSizeInventory();slot++){ItemStack target=chest.getStackInSlot(slot);if(target==null&&chest.isItemValidForSlot(slot,source)){ItemStack one=source.copy();one.stackSize=1;chest.setInventorySlotContents(slot,one);takeOneFromSlot(sourceSlot);chest.markDirty();return;}if(target!=null&&target.isItemEqual(source)&&ItemStack.areItemStackTagsEqual(target,source)&&target.stackSize<Math.min(target.getMaxStackSize(),chest.getInventoryStackLimit())){target.stackSize++;takeOneFromSlot(sourceSlot);chest.markDirty();return;}}return;}}

    public boolean safeTeleportNear(EntityPlayer owner) {
        int baseX = net.minecraft.util.MathHelper.floor_double(owner.posX);
        int baseY = net.minecraft.util.MathHelper.floor_double(owner.boundingBox.minY);
        int baseZ = net.minecraft.util.MathHelper.floor_double(owner.posZ);
        for (int radius = 2; radius <= 6; radius++) for (int attempt = 0; attempt < 16; attempt++) {
            int x = baseX + rand.nextInt(radius * 2 + 1) - radius;
            int z = baseZ + rand.nextInt(radius * 2 + 1) - radius;
            int y = baseY + rand.nextInt(5) - 2;
            while (y > 1 && worldObj.isAirBlock(x, y - 1, z)) y--;
            net.minecraft.block.material.Material floor = worldObj.getBlock(x, y - 1, z).getMaterial();
            if (floor.blocksMovement() && !floor.isLiquid() && worldObj.isAirBlock(x, y, z) && worldObj.isAirBlock(x, y + 1, z)) {
                setPositionAndUpdate(x + 0.5D, y, z + 0.5D); getNavigator().clearPathEntity(); return true;
            }
        }
        return false;
    }

    public ItemStack addToMaidInventory(ItemStack source) {
        if (source == null) return null;
        ItemStack remaining = source.copy();
        for (int slot = 0; slot < getBackpackCapacity() && remaining.stackSize > 0; slot++) {
            ItemStack stored = maidInventory.getStackInSlot(slot);
            if (stored != null && stored.isItemEqual(remaining) && ItemStack.areItemStackTagsEqual(stored, remaining)) {
                int move = Math.min(remaining.stackSize, stored.getMaxStackSize() - stored.stackSize);
                stored.stackSize += move;
                remaining.stackSize -= move;
                maidInventory.markDirty();
            }
        }
        for (int slot = 0; slot < getBackpackCapacity() && remaining.stackSize > 0; slot++) {
            if (maidInventory.getStackInSlot(slot) == null) {
                int move = Math.min(remaining.stackSize, remaining.getMaxStackSize());
                ItemStack inserted = remaining.copy();
                inserted.stackSize = move;
                maidInventory.setInventorySlotContents(slot, inserted);
                remaining.stackSize -= move;
            }
        }
        return remaining.stackSize <= 0 ? null : remaining;
    }

    private static void consumeOne(EntityPlayer player, ItemStack stack) {
        if (!player.capabilities.isCreativeMode && --stack.stackSize <= 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
        }
    }

    private boolean hasFlag(int flag) {
        return (dataWatcher.getWatchableObjectByte(WATCHER_FLAGS) & flag) != 0;
    }

    private void setMaidFlag(int flag, boolean value) {
        byte flags = dataWatcher.getWatchableObjectByte(WATCHER_FLAGS);
        dataWatcher.updateObject(WATCHER_FLAGS, (byte) (value ? flags | flag : flags & ~flag));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
