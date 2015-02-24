package jaredbgreat.dldungeons.pieces.chests.loothack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ChestGenHooks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootHack {
    private static Map<Integer, FileTable> equipmentTables = new HashMap<Integer, FileTable>();
    private static Map<Integer, FileTable> junkTables = new HashMap<Integer, FileTable>();
    private static Map<Integer, FileTable> supplyTables = new HashMap<Integer, FileTable>();

    public static int getEnchantLevel(int depth, Random rand) {
        int range = rand.nextInt((depth+1)*(depth+1));
        int minimum = depth*depth;

        return minimum + range;
    }

    public static void dyeItem(int red, int green, int blue, ItemStack item) {
        red = (red & 0xFF) << 16;
        green = (green & 0xFF) << 8;
        blue = (blue & 0xFF);

        int color = red | green | blue;

        if (item.getTagCompound() == null) {
            item.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound compound = item.getTagCompound();

        NBTTagCompound displayTag = compound.getCompoundTag("display");

        if (!compound.hasKey("display"))
        {
            compound.setTag("display", displayTag);
        }

        displayTag.setInteger("color", color);
    }

    public static void enchantItem(int maxLevel, Random rand, ItemStack item) {
        List<EnchantmentData> enchants = EnchantmentHelper.buildEnchantmentList(rand, item, maxLevel);

        boolean isBook = item.getItem() == Items.book;

        if (isBook){
            item.func_150996_a(Items.enchanted_book);
            while(enchants.size() > 1){
                enchants.remove(rand.nextInt(enchants.size()));
            }
        }

        for (EnchantmentData toAdd : enchants){
            if (isBook){
                Items.enchanted_book.addEnchantment(item, toAdd);
            } else {
                item.addEnchantment(toAdd.enchantmentobj, toAdd.enchantmentLevel);
            }
        }
    }

    public static void initLoothack() {
        File directory = new File("config/roguelike_dungeons", "loot");
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        equipmentTables.put(0, FileTable.load(new File(directory, "equipment-level0.json"), gson, 0));
        equipmentTables.put(1, FileTable.load(new File(directory, "equipment-level1.json"), gson, 1));
        equipmentTables.put(2, FileTable.load(new File(directory, "equipment-level2.json"), gson, 2));
        equipmentTables.put(3, FileTable.load(new File(directory, "equipment-level3.json"), gson, 3));
        equipmentTables.put(4, FileTable.load(new File(directory, "equipment-level4.json"), gson, 4));

        junkTables.put(0, FileTable.load(new File(directory, "junk.json"), gson, 0));
        junkTables.put(1, FileTable.load(new File(directory, "junk.json"), gson, 1));
        junkTables.put(2, FileTable.load(new File(directory, "junk.json"), gson, 2));
        junkTables.put(3, FileTable.load(new File(directory, "junk.json"), gson, 3));
        junkTables.put(4, FileTable.load(new File(directory, "junk.json"), gson, 4));

        supplyTables.put(0, FileTable.load(new File(directory, "supplies.json"), gson, 0));
        supplyTables.put(1, FileTable.load(new File(directory, "supplies.json"), gson, 1));
        supplyTables.put(2, FileTable.load(new File(directory, "supplies.json"), gson, 2));
        supplyTables.put(3, FileTable.load(new File(directory, "supplies.json"), gson, 3));
        supplyTables.put(4, FileTable.load(new File(directory, "supplies.json"), gson, 4));
    }

    public static ItemStack getEquipment(int level, Random rand) {
        return equipmentTables.get(level).get(rand);
    }

    public static ItemStack getJunk(int level, Random rand) {
        if(level > 0 && rand.nextInt(20 / (1 + level)) == 0){

            if(level == 4 && rand.nextInt(10) == 0){
                ChestGenHooks hook = rand.nextBoolean() ?
                        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST):
                        ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST);
                ItemStack toReturn = hook.getOneItem(rand);
                if(toReturn != null) return toReturn;
            }

            ChestGenHooks hook = ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST);
            ItemStack toReturn = hook.getOneItem(rand);
            if(toReturn != null) return toReturn;
        }

        return junkTables.get(level).get(rand);
    }

    public static ItemStack getSupplies(int level, Random rand) {
        return supplyTables.get(level).get(rand);
    }

    public static int getLevel(int level) {
        switch(level) {
            case 0:
                return -1;
            case 1:
                return 0;
            case 2:
            case 3:
                return 1;
            case 4:
            case 5:
                return 2;
            case 6:
            case 7:
                return 3;
            default:
                return 4;
        }
    }

}
