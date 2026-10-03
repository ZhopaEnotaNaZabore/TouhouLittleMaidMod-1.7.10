package com.github.tartaricacid.touhoulittlemaid.compat;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import java.util.ArrayList;

/** Optional adapter for the original Avaritia 1.7.10 API (SpitefulFox).
 * Player-only flight controls and food stats are not copied to the entity.
 */
public final class LegacyAvaritia {
    private LegacyAvaritia() { }
    private static boolean type(Item item, String name) {
        if (item == null) return false;
        for (Class<?> c = item.getClass(); c != null; c = c.getSuperclass())
            if (name.equals(c.getName())) return true;
        return false;
    }
    public static boolean isBow(Item item) {
        return type(item, "fox.spiteful.avaritia.items.tools.ItemBowInfinity");
    }
    public static boolean isBow(ItemStack stack) {
        return stack != null && stack.stackSize > 0 && isBow(stack.getItem());
    }
    public static boolean isSword(ItemStack stack) {
        return stack != null && stack.stackSize > 0
                && type(stack.getItem(), "fox.spiteful.avaritia.items.tools.ItemSwordInfinity");
    }
    public static boolean armor(ItemStack stack, int armorType) {
        return stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemArmor
                && ((ItemArmor) stack.getItem()).armorType == armorType
                && type(stack.getItem(), "fox.spiteful.avaritia.items.ItemArmorInfinity");
    }
    public static boolean fullArmor(EntityMaid maid) {
        for (int slot = 1; slot <= 4; slot++)
            if (!armor(maid.getEquipmentInSlot(slot), 4 - slot)) return false;
        return true;
    }
    public static boolean protects(EntityMaid maid, DamageSource source) {
        // Same exception as Avaritia's player attack/hurt/death handlers.
        return !"infinity".equals(source.getDamageType()) && fullArmor(maid);
    }
    public static void tickArmor(EntityMaid maid) {
        if (maid.worldObj.isRemote) return;
        if (armor(maid.getEquipmentInSlot(4), 0)) {
            maid.setAir(300);
            maid.setHunger(100);
        }
        if (armor(maid.getEquipmentInSlot(2), 2)) maid.extinguish();
        if (armor(maid.getEquipmentInSlot(1), 3) && !maid.isSitting() && !maid.isMaidSleeping()
                && (maid.onGround || maid.isInWater())) {
            try {
                if (Class.forName("fox.spiteful.avaritia.Config").getField("fast").getBoolean(null)) {
                    float speed = maid.isSneaking() ? 0.015F : 0.15F;
                    if (maid.moveForward > 0) maid.moveFlying(0, 1, speed);
                    else if (maid.moveForward < 0) maid.moveFlying(0, 1, -speed * 0.3F);
                    if (maid.moveStrafing != 0) maid.moveFlying(1, 0, speed * 0.5F * Math.signum(maid.moveStrafing));
                }
            } catch (ReflectiveOperationException | LinkageError error) { warn(error); }
        }
        if (armor(maid.getEquipmentInSlot(3), 1)) {
            try {
                Class<?> helper = Class.forName("fox.spiteful.avaritia.PotionHelper");
                java.lang.reflect.Method bad = helper.getMethod("badPotion", Potion.class);
                for (Object value : new ArrayList<Object>(maid.getActivePotionEffects())) {
                    PotionEffect effect = (PotionEffect) value;
                    if (effect.getPotionID() >= 0 && effect.getPotionID() < Potion.potionTypes.length
                            && Boolean.TRUE.equals(bad.invoke(null, Potion.potionTypes[effect.getPotionID()])))
                        maid.removePotionEffect(effect.getPotionID());
                }
            } catch (ReflectiveOperationException | LinkageError error) { warn(error); }
        }
    }
    public static boolean attack(EntityMaid maid, EntityLivingBase target) {
        ItemStack sword = maid.getHeldItem();
        if (maid.worldObj.isRemote || !isSword(sword)) return false;
        float health = target.getHealth(), absorption = target.getAbsorptionAmount();
        // The native callback implements Infinity damage and the opponent's Infinity armor exception.
        sword.getItem().hitEntity(sword, target, maid);
        maid.setLastAttacker(target);
        maid.getMaidEquipmentInventory().markDirty();
        return target.getHealth() < health || target.getAbsorptionAmount() < absorption || target.isDead;
    }
    public static boolean fire(EntityMaid maid, EntityLivingBase target) {
        ItemStack bow = maid.getHeldItem();
        if (maid.worldObj.isRemote || !isBow(bow) || target == null
                || maid.getActionState().useElapsed(maid.ticksExisted) < bow.getMaxItemUseDuration()) return false;
        try {
            // ItemBowInfinity.fire only accepts a player. Its native arrow accepts any living shooter,
            // retaining heaven barrage behavior and excluding the real maid from self-collision.
            Class<?> arrowType = Class.forName("fox.spiteful.avaritia.entity.EntityHeavenArrow");
            EntityArrow arrow = (EntityArrow) arrowType.getConstructor(World.class, EntityLivingBase.class,
                    EntityLivingBase.class, float.class, float.class).newInstance(maid.worldObj, maid, target, 2.0F, 0.0F);
            arrow.setDamage(60.0D);
            arrow.setIsCritical(true);
            int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
            if (power > 0) arrow.setDamage(arrow.getDamage() + power + 1);
            int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
            if (punch > 0) arrow.setKnockbackStrength(punch);
            if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) arrow.setFire(100);
            arrow.canBePickedUp = 2;
            if (!maid.worldObj.spawnEntityInWorld(arrow)) return false;
            bow.damageItem(1, maid);
            maid.getMaidEquipmentInventory().markDirty();
            maid.playSound("random.bow", 1.0F, 1.2F);
            return true;
        } catch (ReflectiveOperationException | LinkageError error) { warn(error); return false; }
    }
    private static boolean warned;
    private static void warn(Throwable error) {
        if (!warned) { warned = true;
            org.apache.logging.log4j.LogManager.getLogger("TLM-Avaritia").warn("Unsupported Avaritia API", error);
        }
    }
}
