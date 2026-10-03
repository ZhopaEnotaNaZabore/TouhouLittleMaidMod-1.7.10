package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityAltar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import java.io.InputStream;
import java.util.*;

/** Uses the four original structure templates; facing follows 1.7 horizontal metadata. */
public final class LegacyAltarStructure {
    private static final String[] FILES={"north","east","south","west"};
    private static final int[][] OFFSETS={{-3,-3,-7},{0,-3,-3},{-4,-3,0},{-7,-3,-4}};
    private static final Map<Integer,List<int[]>> TEMPLATES=new HashMap<Integer,List<int[]>>();
    private LegacyAltarStructure() { }
    /** x,y,z, material (0=log, 1=red wool). */
    public static List<int[]> template(int facing) {
        facing &= 3;
        if (!TEMPLATES.containsKey(facing)) {
            List<int[]> result=new ArrayList<int[]>();
            try (InputStream in=LegacyAltarStructure.class.getResourceAsStream("/data/touhou_little_maid/structures/altar_"+FILES[facing]+".nbt")) {
                if(in==null) throw new IllegalStateException("Missing altar template");
                NBTTagCompound tag=CompressedStreamTools.readCompressed(in);
                NBTTagList palette=tag.getTagList("palette",10), blocks=tag.getTagList("blocks",10);
                for(int i=0;i<blocks.tagCount();i++) {
                    NBTTagCompound b=blocks.getCompoundTagAt(i); NBTTagList pos=b.getTagList("pos",3);
                    String name=palette.getCompoundTagAt(b.getInteger("state")).getString("Name");
                    if(!"minecraft:oak_log".equals(name)&&!"minecraft:red_wool".equals(name)) throw new IllegalStateException("Unknown altar material: "+name);
                    NBTTagList coordinates=(NBTTagList)pos.copy();
                    int px=((NBTTagInt)coordinates.removeTag(0)).func_150287_d();
                    int py=((NBTTagInt)coordinates.removeTag(0)).func_150287_d();
                    int pz=((NBTTagInt)coordinates.removeTag(0)).func_150287_d();
                    result.add(new int[]{px,py,pz,"minecraft:oak_log".equals(name)?0:1});
                }
            } catch(Exception e) { throw new IllegalStateException("Cannot read altar template "+FILES[facing],e); }
            TEMPLATES.put(facing,Collections.unmodifiableList(result));
        }
        return TEMPLATES.get(facing);
    }
    public static int[] offset(int facing) { return OFFSETS[facing&3].clone(); }
    public static boolean isLog(World world,int x,int y,int z) {
        Block b=world.getBlock(x,y,z);if(b==Blocks.log||b==Blocks.log2)return true;
        if(net.minecraft.item.Item.getItemFromBlock(b)==null)return false;
        ItemStack stack=new ItemStack(b,1,world.getBlockMetadata(x,y,z));
        for(int id:OreDictionary.getOreIDs(stack))if("logWood".equals(OreDictionary.getOreName(id)))return true;
        return false;
    }
    public static boolean tryBuild(World world,EntityPlayer player,ItemStack tool,int x,int y,int z,int side) {
        if(world.isRemote)return false;
        int[] dx={0,-1,0,1}, dz={1,0,-1,0};
        for(int facing=0;facing<4;facing++)for(int alternate=0;alternate<2;alternate++) {
            int cx=x+alternate*dx[(facing+1)&3],cz=z+alternate*dz[(facing+1)&3];
            int[] off=offset(facing);int ox=cx+off[0],oy=y+off[1],oz=cz+off[2];
            List<int[]> layout=template(facing);boolean valid=true;
            for(int[] p:layout) {
                int px=ox+p[0],py=oy+p[1],pz=oz+p[2];
                if(!world.blockExists(px,py,pz)||world.getTileEntity(px,py,pz)!=null
                    ||!player.canPlayerEdit(px,py,pz,side,tool)||!world.canMineBlock(player,px,py,pz)
                    ||(p[3]==0?!isLog(world,px,py,pz):world.getBlock(px,py,pz)!=Blocks.wool||world.getBlockMetadata(px,py,pz)!=14)) {valid=false;break;}
            }
            if(!valid)continue;
            Block[] originals=new Block[layout.size()];int[] metas=new int[layout.size()];
            for(int i=0;i<layout.size();i++){int[] p=layout.get(i);originals[i]=world.getBlock(ox+p[0],oy+p[1],oz+p[2]);metas[i]=world.getBlockMetadata(ox+p[0],oy+p[1],oz+p[2]);}
            BlockAltar altar=(BlockAltar)ModBlocks.ALTAR;
            altar.restoringStructure=true;
            int placed=0;
            try {
                for(int i=0;i<layout.size();i++) {
                    int[] p=layout.get(i);int px=ox+p[0],py=oy+p[1],pz=oz+p[2];
                    if(!world.setBlock(px,py,pz,altar,facing,2)) throw new IllegalStateException("Altar placement refused");
                    placed=i+1;
                    TileEntityAltar tile=(TileEntityAltar)world.getTileEntity(px,py,pz);
                    if(tile==null)throw new IllegalStateException("Missing altar tile");
                    tile.configureStructure(ox,oy,oz,facing,originals[i],metas[i],p[3]==0&&p[1]==2,px==cx&&py==y&&pz==cz);
                }
            } catch(RuntimeException failure) {
                com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid.LOGGER.warn("Altar placement rolled back",failure);
                for(int i=0;i<placed;i++){int[] p=layout.get(i);world.setBlock(ox+p[0],oy+p[1],oz+p[2],originals[i],metas[i],3);}
                return false;
            } finally {altar.restoringStructure=false;}
            if(!world.captureBlockSnapshots)for(int[] p:layout)world.notifyBlocksOfNeighborChange(ox+p[0],oy+p[1],oz+p[2],altar);
            world.playSoundEffect(cx+.5,y+.5,cz+.5,"random.levelup",1.5F,1);
            return true;
        }
        return false;
    }
}
