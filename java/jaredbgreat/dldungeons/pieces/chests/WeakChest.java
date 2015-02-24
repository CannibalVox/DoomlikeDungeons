package jaredbgreat.dldungeons.pieces.chests;

/* 
 * This mod is the creation and copyright (c) 2015 
 * of Jared Blackburn (JaredBGreat).
 * 
 * It is licensed under the creative commons 4.0 attribution license: * 
 * https://creativecommons.org/licenses/by/4.0/legalcode
*/	

import jaredbgreat.dldungeons.ConfigHandler;
import jaredbgreat.dldungeons.builder.DBlock;

import java.util.Random;

import jaredbgreat.dldungeons.pieces.chests.loothack.LootHack;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;

public class WeakChest extends BasicChest {
	
	
	public WeakChest(int x, int y, int z, int level) {
		super(x, y, z, level);
	}
	
	@Override
	public void place(World world, int x, int y, int z, Random random) {
        level += random.nextInt(2);
        if(level >= LootCategory.LEVELS) level = LootCategory.LEVELS - 1;
        DBlock.placeChest(world, x, y, z);
		if(world.getBlock(x, y, z) != DBlock.chest) return;
		TileEntityChest contents = (TileEntityChest)world.getTileEntity(x, y, z);

        int loothackLevel = LootHack.getLevel(level);
        int num = random.nextInt(3) + 2;
        for(int i = 0; i < num; i++) {
            ItemStack treasure = LootHack.getJunk((loothackLevel<0)?0:loothackLevel, random);
            if(treasure != null) contents.setInventorySlotContents(random.nextInt(27), treasure);
        }

        num = random.nextInt(3) + 2;
        for (int i = 0; i < num; i++) {
            ItemStack treasure = null;
            if (loothackLevel < 0) {
                treasure = LootHack.getJunk(0, random);
            } else if (random.nextInt(7) < level) {
                treasure = LootHack.getSupplies(loothackLevel, random);
            } else {
                treasure = LootHack.getJunk(loothackLevel, random);
            }
            if(treasure != null) contents.setInventorySlotContents(random.nextInt(27), treasure);
        }
	}

}
