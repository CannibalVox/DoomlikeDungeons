package jaredbgreat.dldungeons.pieces.chests.loothack;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/*
 * This code file was added to Jared Blackburn's Doomlike Dungeons
 * by Stephen Baynham (Cannibalvox)
 *
 * It is licensed under the creative commons 4.0 attribution license: *
 * https://creativecommons.org/licenses/by/4.0/legalcode
*/


public class FileTable {

    private String table;
    private List<TableItem> entries;

    private transient int totalWeight;

    public FileTable() {
    }

    public static FileTable load(File file, Gson gson, int level) {
        String content;

        try {
            content = Files.toString(file, Charsets.UTF_8);
        } catch (IOException e1) {
            throw new RuntimeException("Error while loading "+file.getName()+":", e1);
        }

        FileTable table = null;
        try {
            table = gson.fromJson(content, FileTable.class);
        } catch (Exception ex) {
            throw new RuntimeException("Error while parsing "+file.getName()+":", ex);
        }

        if (table == null) {
            throw new RuntimeException("File "+file.getName()+" parsed to null.");
        }
        table.postLoadProcess(level);
        return table;
    }

    public void postLoadProcess(int level) {
        totalWeight = 0;

        try {
            for (TableItem item : entries) {
                item.postLoadProcess(level);
                totalWeight += item.getWeight();
            }
        } catch (RuntimeException ex) {
            throw new RuntimeException("Error while loading table '"+table+"'.",ex);
        }
    }

    public int getWeight() {
        return 0;
    }

    public ItemStack get(Random rand) {
        int random = rand.nextInt(totalWeight);

        if (entries.size() < 1)
            return null;

        for (TableItem item : entries) {
            if (random < item.getWeight()) {
                return item.get(rand);
            }
            random -= item.getWeight();
        }

        return entries.get(0).get(rand);
    }
}
