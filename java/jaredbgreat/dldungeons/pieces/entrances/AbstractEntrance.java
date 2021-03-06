package jaredbgreat.dldungeons.pieces.entrances;


/* 
 * This mod is the creation and copyright (c) 2015 
 * of Jared Blackburn (JaredBGreat).
 * 
 * It is licensed under the creative commons 4.0 attribution license: * 
 * https://creativecommons.org/licenses/by/4.0/legalcode
*/	


import jaredbgreat.dldungeons.planner.Dungeon;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public abstract class AbstractEntrance {
	
	protected static final Block ladder 
			= (Block)Block.getBlockFromName("ladder");
	protected static final Block stairSlab 
			= (Block)Block.getBlockFromName("stone_slab");	
	
	int x, y, z;
	
	public AbstractEntrance(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public abstract void build(Dungeon dungeon, World world);
}
