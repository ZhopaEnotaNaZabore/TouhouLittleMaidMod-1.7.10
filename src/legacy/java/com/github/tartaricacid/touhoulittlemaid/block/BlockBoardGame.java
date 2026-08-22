package com.github.tartaricacid.touhoulittlemaid.block;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityCChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGomoku;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityWChess;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityJoy;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.config.LegacyConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.task.TaskManager;
import com.github.tartaricacid.touhoulittlemaid.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/** Interactive one-block adaptation of the modern multi-block board-game tables. */
public final class BlockBoardGame extends BlockContainer {
    public enum Type { GOMOKU, CCHESS, WCHESS }
    private final Type type;

    public BlockBoardGame(String name, Type type) {
        super(Material.wood); this.type = type;
        setBlockName(TouhouLittleMaid.MOD_ID + "." + name); setBlockTextureName(TouhouLittleMaid.MOD_ID + ":" + name);
        setHardness(2.0F); setResistance(3.0F); setCreativeTab(CreativeTabs.tabDecorations);
        setBlockBounds(0, 0, 0, 1, 0.25F, 1);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return com.github.tartaricacid.touhoulittlemaid.client.renderer.block.LegacyBlockRenderIds.FURNITURE; }
    @Override public TileEntity createNewTileEntity(World world, int meta) {
        if (type == Type.GOMOKU) return new TileEntityGomoku();
        if (type == Type.CCHESS) return new TileEntityCChess();
        return new TileEntityWChess();
    }

    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        world.setBlockMetadataWithNotify(x, y, z, MathHelper.floor_double(placer.rotationYaw * 4 / 360 + 0.5D) & 3, 3);
        if (!world.isRemote) ensureMultiblock(world, x, y, z);
    }

    @Override public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        if (!super.canPlaceBlockAt(world, x, y, z)) return false;
        for (int dx=-1;dx<=1;dx++) for(int dz=-1;dz<=1;dz++) if(dx!=0||dz!=0)
            if (!world.getBlock(x+dx,y,z+dz).isReplaceable(world,x+dx,y,z+dz)) return false;
        return true;
    }

    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) ensureMultiblock(world,x,y,z);
        if (hasFullStructure(world,x,y,z))
            return activatePart(world,x,y,z,player,side,0,0,hitX,hitY,hitZ);
        return activateBoard(world,x,y,z,player,side,hitX,hitY,hitZ);
    }

    public boolean activatePart(World world, int x, int y, int z, EntityPlayer player, int side,
                                int offsetX, int offsetZ, float hitX, float hitY, float hitZ) {
        float boardX = normalizePartHit(offsetX, hitX);
        float boardZ = normalizePartHit(offsetZ, hitZ);
        return activateBoard(world,x,y,z,player,side,boardX,hitY,boardZ);
    }

    public static float normalizePartHit(int offset, float localHit) {
        return (offset + 1 + Math.max(0.0F, Math.min(1.0F, localHit))) / 3.0F;
    }

    private boolean activateBoard(World world, int x, int y, int z, EntityPlayer player,
                                  int side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile == null) return false;
        if (world.isRemote) return true;
        if (player.isSneaking()) {
            if (tile instanceof TileEntityGomoku) ((TileEntityGomoku) tile).reset();
            else if (tile instanceof TileEntityCChess) ((TileEntityCChess) tile).reset();
            else if (tile instanceof TileEntityWChess) ((TileEntityWChess) tile).reset();
            player.addChatMessage(new ChatComponentText("Board reset")); return true;
        }
        EntityMaid maid=ensurePlayingMaid(world,x,y,z,player,(TileEntityJoy)tile);
        if(maid==null){player.addChatMessage(new ChatComponentText("No owned maid close enough to play"));return true;}
        float[] p = mapHitForFacing(hitX, hitZ, world.getBlockMetadata(x, y, z) & 3);
        boolean expanded=hasFullStructure(world,x,y,z);
        if (type == Type.GOMOKU) return gomoku((TileEntityGomoku) tile, player, maid, p,expanded);
        if (type == Type.CCHESS) return chinese((TileEntityCChess) tile, player, maid, p,expanded);
        return western((TileEntityWChess) tile, player, maid, p,expanded);
    }

    public boolean ensureMultiblock(World world,int x,int y,int z){
        if(hasFullStructure(world,x,y,z))return true;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0){
            Block block=world.getBlock(x+dx,y,z+dz);
            if(block!=ModBlocks.BOARD_PROXY&&!block.isReplaceable(world,x+dx,y,z+dz))return false;
        }
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0)
            world.setBlock(x+dx,y,z+dz,ModBlocks.BOARD_PROXY,(dx+1)+(dz+1)*3,3);
        return true;
    }

    public boolean hasFullStructure(World world,int x,int y,int z){
        if(world==null)return false;
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if(dx!=0||dz!=0)
            if(world.getBlock(x+dx,y,z+dz)!=ModBlocks.BOARD_PROXY||world.getBlockMetadata(x+dx,y,z+dz)!=(dx+1)+(dz+1)*3)return false;
        return true;
    }

    private EntityMaid ensurePlayingMaid(World world,int x,int y,int z,EntityPlayer player,TileEntityJoy board){
        EntitySit sit=board.getSitEntity();if(sit!=null&&sit.riddenByEntity instanceof EntityMaid&&(!LegacyConfig.boardOwnerOnly||((EntityMaid)sit.riddenByEntity).getOwner()==player)){updateMaidSeat(world,x,y,z,sit);return(EntityMaid)sit.riddenByEntity;}
        @SuppressWarnings("unchecked") java.util.List<EntityMaid> maids=world.getEntitiesWithinAABB(EntityMaid.class,AxisAlignedBB.getBoundingBox(x-6,y-3,z-6,x+7,y+4,z+7));
        EntityMaid nearest=null;double distance=Double.MAX_VALUE;for(EntityMaid maid:maids){double d=maid.getDistanceSq(x+.5,y+.5,z+.5);if(TaskManager.BOARD_GAMES_ID.equals(maid.getTaskId())&&(!LegacyConfig.boardOwnerOnly||maid.getOwner()==player)&&d<distance){nearest=maid;distance=d;}}
        if(nearest==null)return null;if(sit!=null)sit.setDead();startMaidGame(world,x,y,z,board,nearest);return nearest;
    }

    /**
     * The source boards are 2x2/3x3/4x4 and put the maid 1.5/2 blocks from
     * their centre.  This port renders them in one block, so one block from
     * the centre leaves the same half-maid clearance without standing on the
     * board. Gomoku retains its source-side seat; both chess boards sit across
     * the board in its facing direction.
     */
    public double[] getMaidSeatPosition(World world, int x, int y, int z) {
        int facing = world.getBlockMetadata(x, y, z) & 3;
        int[] offset = getMaidSeatOffset(facing);
        double distance=hasFullStructure(world,x,y,z)?2.0D:1.0D;
        return new double[]{x + 0.5D + offset[0]*distance, y + 0.1D, z + 0.5D + offset[1]*distance};
    }

    public int[] getMaidSeatOffset(int facing) {
        facing &= 3;
        int[] dx = {0, -1, 0, 1};
        int[] dz = {1, 0, -1, 0};
        int stepX = dx[facing], stepZ = dz[facing];
        if (type == Type.GOMOKU) {
            int rotatedX = stepZ;
            stepZ = -stepX;
            stepX = rotatedX;
        }
        return new int[]{stepX, stepZ};
    }

    public void updateMaidSeat(World world, int x, int y, int z, EntitySit sit) {
        double[] position = getMaidSeatPosition(world, x, y, z);
        sit.setPosition(position[0], position[1], position[2]);
        double outwardX = position[0] - (x + 0.5D);
        double outwardZ = position[2] - (z + 0.5D);
        sit.rotationYaw = (float) Math.toDegrees(Math.atan2(outwardX, -outwardZ));
    }

    public EntitySit startMaidGame(World world, int x, int y, int z, TileEntityJoy board, EntityMaid maid) {
        EntitySit sit = new EntitySit(world, x + 0.5D, y + 0.1D, z + 0.5D,
                "Gomoku", x, y, z);
        updateMaidSeat(world, x, y, z, sit);
        world.spawnEntityInWorld(sit);
        maid.setMaidSitting(false);
        maid.mountEntity(sit);
        board.setSitEntity(sit);
        return sit;
    }

    private boolean gomoku(TileEntityGomoku board, EntityPlayer player, EntityMaid maid, float[] p,boolean expanded) {
        int column=expanded?nearestGrid(p[0],-1.3818,.1974,15):Math.min(14,(int)(p[0]*15));
        int row=expanded?nearestGrid(p[1],-1.3818,.1974,15):Math.min(14,(int)(p[1]*15));
        if(column<0||row<0)return true;
        int before=board.getWinner();if (board.place(column, row)) board.makeComputerMove();if(before==0&&board.getWinner()==1)maid.recordBoardWin("GomokuWin");
        String state = board.getWinner() == 1 ? "player won" : board.getWinner() == 2 ? "opponent won" : board.getWinner() == 3 ? "draw" : "round " + board.getMoves();
        player.addChatMessage(new ChatComponentText("Gomoku: " + state)); return true;
    }

    private boolean western(TileEntityWChess board, EntityPlayer player, EntityMaid maid, float[] p,boolean expanded) {
        if (board.ended() || board.getPosition().sdPlayer != 0) return true;
        int file=expanded?floorGrid(p[0],-1.0,.25,8):Math.min(7,(int)(p[0]*8));
        int rank=expanded?floorGrid(p[1],-1.0,.25,8):Math.min(7,(int)(p[1]*8));
        if(file<0||rank<0)return true;
        int square = Position.COORD_XY(Position.FILE_LEFT + file, Position.RANK_TOP + rank);
        byte selected = board.getPosition().squares[board.getSelected()]; byte current = board.getPosition().squares[square];
        if ((selected & 8) == 0 || (current & 8) != 0) board.select(square);
        else if (board.move(square)){if(board.ended())maid.recordBoardWin("WChessWin");else board.makeComputerMove();}
        player.addChatMessage(new ChatComponentText("Chess: " + board.getPosition().toFen())); return true;
    }

    private boolean chinese(TileEntityCChess board, EntityPlayer player, EntityMaid maid, float[] p,boolean expanded) {
        if (board.ended() || board.getPosition().sdPlayer != 0) return true;
        int file=expanded?nearestGrid(p[0],-.912,.228,9):Math.min(8,(int)(p[0]*9));
        int rank=expanded?nearestGrid(p[1],-1.026,.228,10):Math.min(9,(int)(p[1]*10));
        if(file<0||rank<0)return true;
        int square = com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.COORD_XY(
                com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.FILE_LEFT + file,
                com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position.RANK_TOP + rank);
        byte selected = board.getPosition().squares[board.getSelected()]; byte current = board.getPosition().squares[square];
        if ((selected & 8) == 0 || (current & 8) != 0) board.select(square);
        else if (board.move(square)){if(board.ended())maid.recordBoardWin("CChessWin");else board.makeComputerMove();}
        player.addChatMessage(new ChatComponentText("Xiangqi: " + board.getPosition().toFen())); return true;
    }

    public static float[] mapHitForFacing(float x, float z, int facing) {
        // Inverse of the renderer's Sx(reflection) * R(facing) transform.
        switch (facing) {
            case 1: return new float[]{1-z,1-x};
            case 2: return new float[]{x,1-z};
            case 3: return new float[]{z,x};
            default:return new float[]{1-x,z};
        }
    }
    public static int floorGrid(float normalized,double start,double step,int count){
        double coordinate=(normalized-.5D)*3.0D;
        int index=(int)Math.floor((coordinate-start)/step);
        return index>=0&&index<count?index:-1;
    }
    public static int nearestGrid(float normalized,double start,double step,int count){
        double coordinate=(normalized-.5D)*3.0D;
        int index=(int)Math.round((coordinate-start)/step);
        if(index<0||index>=count)return-1;
        return Math.abs(coordinate-(start+index*step))<=step*.6D?index:-1;
    }
    @Override public void breakBlock(World world,int x,int y,int z,net.minecraft.block.Block block,int meta){
        TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityJoy)((TileEntityJoy)tile).removeSitEntity();
        BlockBoardProxy.setRemovingStructure(true);
        try{for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)if((dx!=0||dz!=0)&&world.getBlock(x+dx,y,z+dz)==ModBlocks.BOARD_PROXY)world.setBlockToAir(x+dx,y,z+dz);}
        finally{BlockBoardProxy.setRemovingStructure(false);}
        super.breakBlock(world,x,y,z,block,meta);
    }
}
