package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Position;
import com.github.tartaricacid.touhoulittlemaid.api.game.xqwlight.Search;
import net.minecraft.nbt.NBTTagCompound;

/** Xiangqi state backed by the original XiangQi Wizard Light rules/search engine. */
public final class TileEntityCChess extends TileEntityJoy {
    public static final String INITIAL = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR";
    private final Position position = new Position();
    private int selected, counter;
    private boolean checkmate, repeat, moveLimit;

    public TileEntityCChess() { position.fromFen(INITIAL); }
    public Position getPosition() { return position; }
    public int getSelected() { return selected; }
    public void select(int square) { selected = square; changed(); }
    public boolean ended() { return checkmate || repeat || moveLimit; }

    public boolean move(int destination) {
        int move = Position.MOVE(selected, destination);
        if (!position.legalMove(move) || !position.makeMove(move)) return false;
        if (position.captured()) position.setIrrev();
        selected = destination; counter++; updateOutcome(); changed(); return true;
    }

    public void makeComputerMove() {
        if (ended()) return;
        int move = new Search(position, 10).searchMain(4, 80);
        if (move != 0 && position.makeMove(move)) {
            if (position.captured()) position.setIrrev();
            selected = Position.DST(move); counter++; updateOutcome(); changed();
        }
    }

    public void reset() { position.fromFen(INITIAL); selected = counter = 0; checkmate = repeat = moveLimit = false; changed(); }
    private void changed() { markDirty(); if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); safeFen(tag.hasKey("ChessData",8) ? tag.getString("ChessData") : INITIAL); selected = tag.getInteger("SelectChessPoint"); if(selected<0||selected>=position.squares.length)selected=0; counter = Math.max(0,tag.getInteger("ChessCounter")); checkmate = tag.getBoolean("Checkmate"); repeat = tag.getBoolean("Repeat"); moveLimit = tag.getBoolean("MoveNumberLimit"); }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setString("ChessData", position.toFen()); tag.setInteger("SelectChessPoint", selected); tag.setInteger("ChessCounter", counter); tag.setBoolean("Checkmate", checkmate); tag.setBoolean("Repeat", repeat); tag.setBoolean("MoveNumberLimit", moveLimit); }
    private void updateOutcome(){repeat=position.repStatus(3)>0;moveLimit=position.moveNum>60;checkmate=position.isMate();}
    private void safeFen(String fen){try{position.fromFen(fen==null||fen.isEmpty()?INITIAL:fen);}catch(RuntimeException invalid){position.fromFen(INITIAL);}}
}
