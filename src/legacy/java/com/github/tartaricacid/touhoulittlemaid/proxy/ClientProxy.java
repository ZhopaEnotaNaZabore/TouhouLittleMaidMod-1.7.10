package com.github.tartaricacid.touhoulittlemaid.proxy;

import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderMaid;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderDanmaku;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderFairy;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderExtinguishingAgent;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderMaidFishingHook;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderChair;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderSit;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderBroom;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderBox;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderTombstone;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderMaidPainting;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderYukkuriSlime;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.entity.RenderPointExperience;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityDanmaku;
import com.github.tartaricacid.touhoulittlemaid.entity.monster.EntityFairy;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityExtinguishingAgent;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityMaidFishingHook;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBox;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityTombstone;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityMaidPainting;
import com.github.tartaricacid.touhoulittlemaid.entity.projectile.EntityThrowPowerPoint;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.client.registry.ClientRegistry;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.RenderGameBoard;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.RenderAltar;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.RenderFurnitureTile;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.LegacyBedrockTileModels;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.RenderStatue;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.tileentity.RenderGarageKit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityModelSwitcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidModelRegistry;
import com.github.tartaricacid.touhoulittlemaid.client.model.bedrock.LegacyMaidAccessoryModels;
import com.github.tartaricacid.touhoulittlemaid.client.chat.LegacyKaomojiLoader;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.GuiMaid;
import com.github.tartaricacid.touhoulittlemaid.client.gui.GuiMaidBauble;
import com.github.tartaricacid.touhoulittlemaid.client.gui.GuiMaidEquipment;
import com.github.tartaricacid.touhoulittlemaid.client.gui.GuiMaidTask;
import com.github.tartaricacid.touhoulittlemaid.client.gui.GuiModelSwitcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.client.renderer.entity.RenderSnowball;
import com.github.tartaricacid.touhoulittlemaid.init.ModItems;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.block.RenderLegacyFurniture;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.ClientHomeAreaOverlay;
import com.github.tartaricacid.touhoulittlemaid.client.renderer.ClientMaidDebugOverlay;
import net.minecraftforge.common.MinecraftForge;

public final class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        LegacyBlockRenderIds.FURNITURE = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(new RenderLegacyFurniture());
        MinecraftForge.EVENT_BUS.register(new ClientHomeAreaOverlay());
        MinecraftForge.EVENT_BUS.register(new ClientMaidDebugOverlay());
        if (Minecraft.getMinecraft().getResourceManager() instanceof SimpleReloadableResourceManager) {
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(LegacyMaidModelRegistry.INSTANCE);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(LegacyMaidAccessoryModels.INSTANCE);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(LegacyKaomojiLoader.INSTANCE);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .registerReloadListener(LegacyBedrockTileModels.INSTANCE);
        }
        RenderingRegistry.registerEntityRenderingHandler(EntityMaid.class, new RenderMaid());
        RenderingRegistry.registerEntityRenderingHandler(EntityPowerPoint.class, new RenderPowerPoint());
        RenderingRegistry.registerEntityRenderingHandler(EntityDanmaku.class, new RenderDanmaku());
        RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, new RenderFairy());
        RenderingRegistry.registerEntityRenderingHandler(EntityExtinguishingAgent.class, new RenderExtinguishingAgent());
        RenderingRegistry.registerEntityRenderingHandler(EntityMaidFishingHook.class, new RenderMaidFishingHook());
        RenderChair chairRenderer = new RenderChair();
        RenderingRegistry.registerEntityRenderingHandler(EntityChair.class, chairRenderer);
        if (Minecraft.getMinecraft().getResourceManager() instanceof SimpleReloadableResourceManager)
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(chairRenderer);
        RenderingRegistry.registerEntityRenderingHandler(EntitySit.class, new RenderSit());
        RenderingRegistry.registerEntityRenderingHandler(EntityBroom.class, new RenderBroom());
        RenderingRegistry.registerEntityRenderingHandler(EntityBox.class, new RenderBox());
        RenderingRegistry.registerEntityRenderingHandler(EntityTombstone.class, new RenderTombstone());
        RenderingRegistry.registerEntityRenderingHandler(EntityMaidPainting.class,new RenderMaidPainting());
        RenderingRegistry.registerEntityRenderingHandler(EntityThrowPowerPoint.class, new RenderSnowball(ModItems.POWER_POINT));
        if (LegacyConfig.replaceSlimeModel) {
            RenderingRegistry.registerEntityRenderingHandler(EntitySlime.class, new RenderYukkuriSlime("reimu_yukkuri"));
            RenderingRegistry.registerEntityRenderingHandler(EntityMagmaCube.class, new RenderYukkuriSlime("marisa_yukkuri"));
        }
        if (LegacyConfig.replaceXpTexture)
            RenderingRegistry.registerEntityRenderingHandler(EntityXPOrb.class, new RenderPointExperience());
        RenderGameBoard boardRenderer = new RenderGameBoard();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGomoku.class, boardRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWChess.class, boardRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCChess.class, boardRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityAltar.class, new RenderAltar());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStatue.class, new RenderStatue());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGarageKit.class, new RenderGarageKit());
        RenderFurnitureTile furnitureRenderer = new RenderFurnitureTile();
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityKeyboard.class, furnitureRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityBookshelf.class, furnitureRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityComputer.class, furnitureRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityShrine.class, furnitureRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat.class, furnitureRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntitySnackCabinet.class, furnitureRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBed.class, furnitureRenderer);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if(id==MODEL_SWITCHER_GUI_ID&&world.getTileEntity(x,y,z) instanceof TileEntityModelSwitcher)return new GuiModelSwitcher((TileEntityModelSwitcher)world.getTileEntity(x,y,z));
        if(id==MAID_BEACON_GUI_ID&&world.getTileEntity(x,y,z) instanceof com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon)return new com.github.tartaricacid.touhoulittlemaid.client.gui.GuiMaidBeacon((com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityMaidBeacon)world.getTileEntity(x,y,z));
        Entity entity = world.getEntityByID(x);
        if (id == MAID_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) {
            return new GuiMaid(player.inventory, (EntityMaid) entity);
        }
        if (id == MAID_BAUBLE_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) return new GuiMaidBauble(player.inventory,(EntityMaid)entity);
        if (id == MAID_EQUIPMENT_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) return new GuiMaidEquipment(player.inventory,(EntityMaid)entity);
        if (id == MAID_TASK_GUI_ID && entity instanceof EntityMaid && ((EntityMaid) entity).getOwner() == player) return new GuiMaidTask(player.inventory,(EntityMaid)entity);
        return null;
    }
}
