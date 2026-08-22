package com.github.tartaricacid.touhoulittlemaid.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/** Persistent 15x15 gomoku board. 0=empty, 1=black/player, 2=white/opponent. */
public final class TileEntityGomoku extends TileEntityJoy {
    private final int[] board = new int[225];
    private int moves;
    private int winner;
    private int latest = -1;

    public int get(int x, int y) { return board[y * 15 + x]; }
    public int getMoves() { return moves; }
    public int getWinner() { return winner; }
    public int getLatest() { return latest; }

    public boolean place(int x, int y) {
        if (winner != 0 || x < 0 || y < 0 || x >= 15 || y >= 15 || get(x, y) != 0) return false;
        int stone = (moves & 1) == 0 ? 1 : 2;
        latest = y * 15 + x;
        board[latest] = stone;
        moves++;
        if (hasFive(x, y, stone)) winner = stone;
        else if (moves == board.length) winner = 3;
        changed();
        return true;
    }

    /** Deterministic opponent: win, block an immediate win, then prefer the centre/nearby cells. */
    public void makeComputerMove() {
        if (winner != 0 || (moves & 1) == 0) return;
        int best = -1;
        for (int stone = 2; stone >= 1 && best < 0; stone--) {
            for (int i = 0; i < board.length; i++) {
                if (board[i] != 0) continue;
                board[i] = stone;
                if (hasFive(i % 15, i / 15, stone)) best = i;
                board[i] = 0;
                if (best >= 0) break;
            }
        }
        if (best < 0) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < board.length; i++) if (board[i] == 0) {
                int x = i % 15, y = i / 15;
                int score = 20 - Math.abs(x - 7) - Math.abs(y - 7);
                for (int yy = Math.max(0, y - 1); yy <= Math.min(14, y + 1); yy++)
                    for (int xx = Math.max(0, x - 1); xx <= Math.min(14, x + 1); xx++) if (get(xx, yy) != 0) score += 4;
                if (score > bestScore) { bestScore = score; best = i; }
            }
        }
        if (best >= 0) place(best % 15, best / 15);
    }

    private boolean hasFive(int x, int y, int stone) {
        int[][] dirs = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] d : dirs) {
            int count = 1 + count(x, y, d[0], d[1], stone) + count(x, y, -d[0], -d[1], stone);
            if (count >= 5) return true;
        }
        return false;
    }

    private int count(int x, int y, int dx, int dy, int stone) {
        int result = 0;
        for (x += dx, y += dy; x >= 0 && y >= 0 && x < 15 && y < 15 && get(x, y) == stone; x += dx, y += dy) result++;
        return result;
    }

    public void reset() {
        java.util.Arrays.fill(board, 0); moves = 0; winner = 0; latest = -1; changed();
    }

    private void changed() {
        markDirty();
        if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        java.util.Arrays.fill(board, 0);
        if (tag.hasKey("ChessData", 11)) {
            int[] saved = tag.getIntArray("ChessData");
            if (saved.length == board.length) for (int i = 0; i < board.length; i++) board[i] = validStone(saved[i]);
        } else if (tag.hasKey("ChessData", 9)) {
            // 1.20 stores 15 X-major int-array rows; the 1.7 renderer uses a flat Y-major array.
            NBTTagList rows = tag.getTagList("ChessData", 11);
            for (int x = 0; x < rows.tagCount() && x < 15; x++) {
                int[] row = rows.func_150306_c(x);
                for (int y = 0; y < row.length && y < 15; y++) board[y * 15 + x] = validStone(row[y]);
            }
        }
        moves = Math.max(0, Math.min(board.length, tag.getInteger("ChessCounter")));
        int savedState = tag.getInteger("Statue");
        if (tag.hasKey("PlayerTurn")) {
            winner = savedState == 2 ? 3 : savedState == 1 ? (tag.getBoolean("PlayerTurn") ? 1 : 2) : 0;
        } else winner = Math.max(0, Math.min(3, savedState));
        if (tag.hasKey("LatestChessPoint", 10)) {
            NBTTagCompound point = tag.getCompoundTag("LatestChessPoint");
            int px = point.getInteger("x"), py = point.getInteger("y");
            latest = px >= 0 && px < 15 && py >= 0 && py < 15 ? py * 15 + px : -1;
        } else {
            latest = tag.getInteger("LatestChessPoint");
            if (latest < -1 || latest >= board.length) latest = -1;
        }
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag); tag.setIntArray("ChessData", board); tag.setInteger("ChessCounter", moves);
        tag.setInteger("Statue", winner); tag.setInteger("LatestChessPoint", latest);
    }

    private int validStone(int value) { return value >= 0 && value <= 2 ? value : 0; }
}
