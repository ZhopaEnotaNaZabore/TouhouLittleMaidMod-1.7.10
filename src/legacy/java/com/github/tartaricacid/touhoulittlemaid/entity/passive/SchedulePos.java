package com.github.tartaricacid.touhoulittlemaid.entity.passive;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.MaidActivity;
import net.minecraft.nbt.NBTTagCompound;

/** Work, leisure and sleep positions matching the modern MaidSchedulePos data. */
public final class SchedulePos {
    public static final int WORK_RANGE = 12;
    public static final int IDLE_RANGE = 8;
    public static final int SLEEP_RANGE = 6;

    private Point work = new Point();
    private Point idle = new Point();
    private Point sleep = new Point();
    private int dimension;
    private boolean configured;

    public void tick(EntityMaid maid) {
        if (!maid.isHomeMode() || maid.ticksExisted % 40 != 0) return;
        restrictTo(maid);
        if (!maid.isWithinRestriction() && !maid.isSitting()) {
            Point center = getForActivity(maid.getCurrentActivity());
            maid.getNavigator().tryMoveToXYZ(center.x + 0.5D, center.y, center.z + 0.5D, 0.7D);
        }
    }

    public void restrictTo(EntityMaid maid) {
        if (!maid.isHomeMode()) {
            maid.detachHome();
            return;
        }
        Point point = getForActivity(maid.getCurrentActivity());
        int radius = getRadius(maid.getCurrentActivity());
        maid.setRestriction(point.x, point.y, point.z, radius);
    }

    public void enableAt(EntityMaid maid) {
        if (!configured) {
            work = new Point(maid);
            idle = new Point(maid);
            sleep = new Point(maid);
            dimension = maid.dimension;
        }
        restrictTo(maid);
    }

    public void setAll(EntityMaid maid) {
        work = new Point(maid);
        idle = new Point(maid);
        sleep = new Point(maid);
        dimension = maid.dimension;
        configured = true;
        restrictTo(maid);
    }

    public void clear(EntityMaid maid) {
        configured = false;
        work = new Point(maid);
        idle = new Point(maid);
        sleep = new Point(maid);
        dimension = maid.dimension;
        maid.detachHome();
    }

    public Point getForActivity(MaidActivity activity) {
        if (activity == MaidActivity.WORK) return work;
        if (activity == MaidActivity.REST) return sleep;
        return idle;
    }

    public boolean isConfigured() { return configured; }
    public int getDimension() { return dimension; }

    public void setPoints(EntityMaid maid, Point newWork, Point newIdle, Point newSleep, int newDimension) {
        work = copy(newWork); idle = copy(newIdle); sleep = copy(newSleep);
        dimension = newDimension; configured = true;
        maid.setHomeMode(true); restrictTo(maid);
    }

    private static Point copy(Point source) {
        Point point = new Point(); point.x = source.x; point.y = source.y; point.z = source.z; return point;
    }

    public static Point point(int x, int y, int z) { Point point=new Point();point.x=x;point.y=y;point.z=z;return point; }

    public void writeToNBT(NBTTagCompound root) {
        NBTTagCompound data = new NBTTagCompound();
        data.setTag("Work", work.write());
        data.setTag("Idle", idle.write());
        data.setTag("Sleep", sleep.write());
        data.setInteger("DimensionId", dimension);
        data.setString("Dimension", Integer.toString(dimension));
        data.setBoolean("Configured", configured);
        root.setTag("MaidSchedulePos", data);
    }

    public void readFromNBT(NBTTagCompound root, EntityMaid maid) {
        if (!root.hasKey("MaidSchedulePos", 10)) return;
        NBTTagCompound data = root.getCompoundTag("MaidSchedulePos");
        work = Point.read(data.getCompoundTag("Work"));
        idle = Point.read(data.getCompoundTag("Idle"));
        sleep = Point.read(data.getCompoundTag("Sleep"));
        dimension = data.hasKey("DimensionId") ? data.getInteger("DimensionId") : maid.dimension;
        configured = data.getBoolean("Configured");
        if (maid.isHomeMode()) restrictTo(maid);
    }

    private int getRadius(MaidActivity activity) {
        if (activity == MaidActivity.WORK) return WORK_RANGE;
        if (activity == MaidActivity.REST) return SLEEP_RANGE;
        return IDLE_RANGE;
    }

    public static final class Point {
        public int x;
        public int y;
        public int z;

        private Point() {
        }

        private Point(EntityMaid maid) {
            x = (int) Math.floor(maid.posX);
            y = (int) Math.floor(maid.posY);
            z = (int) Math.floor(maid.posZ);
        }

        private NBTTagCompound write() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("X", x);
            tag.setInteger("Y", y);
            tag.setInteger("Z", z);
            return tag;
        }

        private static Point read(NBTTagCompound tag) {
            Point point = new Point();
            point.x = tag.getInteger("X");
            point.y = tag.getInteger("Y");
            point.z = tag.getInteger("Z");
            return point;
        }

        @Override
        public String toString() {
            return x + "," + y + "," + z;
        }
    }
}
