package com.github.tartaricacid.touhoulittlemaid.world.backups;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.DimensionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Java-8/1.7-compatible rolling NBT backup writer. */
public final class MaidBackupsManager {
    private static final int MAX_BACKUPS = 10;
    private static final ExecutorService WRITER = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "TLM-Maid-Backup");
        thread.setDaemon(true);
        return thread;
    });

    private MaidBackupsManager() {
    }

    public static void save(final EntityMaid maid) {
        if (maid == null || maid.worldObj.isRemote || !maid.isTamed() || maid.func_152113_b().isEmpty()) return;
        final File worldRoot = DimensionManager.getCurrentSaveRootDirectory();
        if (worldRoot == null) return;
        final NBTTagCompound entityData = new NBTTagCompound();
        maid.writeToNBT(entityData);
        entityData.setFloat("Health", maid.getMaxHealth());
        entityData.setShort("DeathTime", (short) 0);
        entityData.setShort("HurtTime", (short) 0);
        final String owner = maid.func_152113_b();
        final String maidId = maid.getUniqueID().toString();
        final String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

        WRITER.execute(new Runnable() {
            @Override
            public void run() {
                File folder = new File(new File(new File(worldRoot, "touhou_little_maid/maid_backups"), owner), maidId);
                if (!folder.exists() && !folder.mkdirs()) {
                    TouhouLittleMaid.LOGGER.warn("Could not create maid backup directory {}", folder);
                    return;
                }
                File destination = new File(folder, timestamp + ".dat");
                try {
                    FileOutputStream output = new FileOutputStream(destination);
                    try {
                        CompressedStreamTools.writeCompressed(entityData, output);
                    } finally {
                        output.close();
                    }
                    prune(folder);
                } catch (IOException exception) {
                    TouhouLittleMaid.LOGGER.error("Could not write maid backup " + destination, exception);
                }
            }
        });
    }

    private static void prune(File folder) {
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".dat"));
        if (files == null || files.length <= MAX_BACKUPS) return;
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File left, File right) { return left.getName().compareTo(right.getName()); }
        });
        for (int i = 0; i < files.length - MAX_BACKUPS; i++) {
            if (!files[i].delete()) TouhouLittleMaid.LOGGER.warn("Could not remove old maid backup {}", files[i]);
        }
    }

    public static List<String> listMaidIds(String ownerId) {
        File root = backupRoot(ownerId);
        File[] folders = root.listFiles(File::isDirectory);
        if (folders == null) return Collections.emptyList();
        List<String> result = new ArrayList<String>();
        for (File folder : folders) result.add(folder.getName());
        Collections.sort(result);
        return result;
    }

    public static NBTTagCompound loadLatest(String ownerId, String maidId) throws IOException {
        if (!maidId.matches("[0-9a-fA-F-]{36}")) throw new IOException("Invalid maid UUID");
        File folder = new File(backupRoot(ownerId), maidId);
        File[] files = folder.listFiles((dir, name) -> name.matches("[0-9-]+\\.dat"));
        if (files == null || files.length == 0) return null;
        Arrays.sort(files, Comparator.comparing(File::getName).reversed());
        FileInputStream input = new FileInputStream(files[0]);
        try {
            return CompressedStreamTools.readCompressed(input);
        } finally {
            input.close();
        }
    }

    private static File backupRoot(String ownerId) {
        File worldRoot = DimensionManager.getCurrentSaveRootDirectory();
        return new File(new File(worldRoot, "touhou_little_maid/maid_backups"), ownerId);
    }
}
