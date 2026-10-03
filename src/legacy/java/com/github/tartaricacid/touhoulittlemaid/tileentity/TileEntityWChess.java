package com.github.tartaricacid.touhoulittlemaid.tileentity;

import com.github.tartaricacid.touhoulittlemaid.api.game.chess.Position;
import com.github.tartaricacid.touhoulittlemaid.api.game.chess.Search;
import net.minecraft.nbt.NBTTagCompound;

/** Western chess state backed by the original Mobile Chess rules/search engine. */
public final class TileEntityWChess extends TileEntityJoy {
    public static final String INITIAL = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private final Position position = new Position();
    private int selected;
    private int counter;
    private boolean checkmate, repeat, moveLimit;

    public TileEntityWChess() { position.fromFen(INITIAL); }
    public Position getPosition() { return position; }
    public int getSelected() { return selected; }
    public void select(int square) { if (square < 0 || square >= position.squares.length) return; selected = square; changed(); }
    public boolean ended() { return checkmate || repeat || moveLimit; }

    public boolean move(int destination) {
        if (ended() || destination < 0 || destination >= position.squares.length) return false;
        int move = Position.MOVE(selected, destination);
        if (!position.legalMove(move) || !position.makeMove(move)) return false;
        int movedPiece = Position.PIECE_TYPE(position.squares[Position.DST(move)]);
        if (position.captured() || movedPiece == Position.PIECE_PAWN) position.setIrrev();
        selected = destination; counter++;
        updateOutcome();
        changed(); return true;
    }

    public void makeComputerMove() {
        if (ended()) return;
        int move = new Search(position, 10).searchMain(4, 80);
        if (move != 0 && position.makeMove(move)) {
            int movedPiece = Position.PIECE_TYPE(position.squares[Position.DST(move)]);
            if (position.captured() || movedPiece == Position.PIECE_PAWN) position.setIrrev();
            selected = Position.DST(move); counter++; updateOutcome(); changed();
        }
    }

    public void reset() { position.fromFen(INITIAL); selected = counter = 0; checkmate = repeat = moveLimit = false; changed(); }
    private void changed() { markDirty(); if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); }

    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag);
        tag = LegacyTileNbt.data(tag); safeFen(tag.hasKey("ChessData",8) ? tag.getString("ChessData") : INITIAL); selected = tag.getInteger("SelectChessPoint"); if(selected<0||selected>=position.squares.length)selected=0; counter = Math.max(0,tag.getInteger("ChessCounter")); checkmate = tag.getBoolean("Checkmate"); repeat = tag.getBoolean("Repeat"); moveLimit = tag.getBoolean("MoveNumberLimit"); }
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); tag.setString("ChessData", position.toFen()); tag.setInteger("SelectChessPoint", selected); tag.setInteger("ChessCounter", counter); tag.setBoolean("Checkmate", checkmate); tag.setBoolean("Repeat", repeat); tag.setBoolean("MoveNumberLimit", moveLimit); }
    private void updateOutcome(){repeat=position.isRep(2);moveLimit=position.moveNum>100;checkmate=position.isMate();}
    private void safeFen(String fen){try{position.fromFen(fen==null||fen.isEmpty()?INITIAL:fen);}catch(RuntimeException invalid){position.fromFen(INITIAL);}}
}
