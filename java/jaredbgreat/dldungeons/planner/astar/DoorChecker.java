package jaredbgreat.dldungeons.planner.astar;

/* 
 * This mod is the creation and copyright (c) 2015 
 * of Jared Blackburn (JaredBGreat).
 * 
 * It is licensed under the creative commons 4.0 attribution license: * 
 * https://creativecommons.org/licenses/by/4.0/legalcode
*/	

import jaredbgreat.dldungeons.pieces.Doorway;
import jaredbgreat.dldungeons.planner.Dungeon;
import jaredbgreat.dldungeons.planner.mapping.Tile;
import jaredbgreat.dldungeons.rooms.Room;
import jaredbgreat.dldungeons.themes.ThemeFlags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * This class contains utility methods for testing the validity and viability
 * of doors.  Among other things it will remove the infamous doors to nowhere,
 * sort doors by destination room, pick a good door for each room to run the
 * A* connectively text-fix on, eliminate doors the transition between liquid 
 * and non-liquid tiles, and place liquids in doors that have liquid on both
 * sides.
 * 
 * @author JaredBGreat (Jared Blackburn)
 *
 */
public class DoorChecker {
	
	
	public static class CompareLists implements Comparator<List> {
		@Override
		public int compare(List o1, List o2) {
			return o1.size() - o2.size();
		}	
	}
	static CompareLists c = new CompareLists();

	
	/**
	 * This will check to see if a tile is a valid location in the
	 * Dungeon.  That is, it will check to see if its both inside
	 * the dungeons and has a room value other than zero.
	 * 
	 * @param tile
	 * @return boolean for if tile is inside a room in the dungeon
	 */
	public static boolean validateTile(Dungeon dungeon, Tile tile) {
		if(tile.x < 0 || tile.x >= dungeon.size.width) return false;
		if(tile.z < 0 || tile.z >= dungeon.size.width) return false;
		if(dungeon.map.room[tile.x][tile.z] <= 0) return false;
		return true;
	}

	
	/**
	 * This will check to see if a tile is a valid location in the
	 * Dungeon.  That is, it will check to see if its both inside
	 * the dungeons and has a room value other than zero.
	 * 
	 * @param tile
	 * @return boolean for if tile is inside a room in the dungeon
	 */
	public static boolean validateTile(Dungeon dungeon, int x, int z) {
		if(x < 0 || x >= dungeon.size.width) return false;
		if(z < 0 || z >= dungeon.size.width) return false;
		return (dungeon.map.room[x][z] > 0);
	}
	
	
	/**
	 * Will check if a tile representing a door location actually
	 * connects two rooms, and remove doors that do not.  Put differently
	 * this will remove a door to nowhere and inform the calling method
	 * if such an action was needed.
	 * 
	 * @param door
	 * @return boolean for if the door connects two room in the dungeon
	 */
	public static boolean validateDoor(Dungeon dungeon, Doorway door) {
		if(door.xOriented) {
			if(validateTile(dungeon, door.x - 1, door.z) 
					&& validateTile(dungeon, door.x + 1, door.z)) return true;
			dungeon.map.isDoor[door.x][door.z] = false;
			return false;
		} else {
			if(validateTile(dungeon, door.x, door.z - 1) 
					&& validateTile(dungeon, door.x, door.z + 1)) return true;
			dungeon.map.isDoor[door.x][door.z] = false;
			return false;
		}
	}
	
	
	/**
	 * This will get a list of doors to run A* on for the purpose of 
	 * ensuring room passibility.
	 * 
	 * @param room
	 * @return ArrayList of one Doorway per connected room
	 */
	public static ArrayList<Doorway> makeConnectionList(Room room, Random random) {
		ArrayList<Doorway> out = new ArrayList<Doorway>(room.connections.size());
		for(DoorQueue exits : room.connections) {
			out.add(exits.peek());
		}
		Collections.shuffle(out, random);
		return out;
	}
	
	
	/**
	 * This will return the id of the room on the other side of the door.
	 * 
	 * @param exit
	 * @param room
	 * @param dungeon
	 * @return the id of the connect room
	 */
	public static int getOtherRoom(Doorway exit, Room room, Dungeon dungeon) {
		if(exit.xOriented) {
			if(dungeon.map.room[exit.x+1][exit.z] == room.id)
				return dungeon.map.room[exit.x-1][exit.z];
			if(dungeon.map.room[exit.x-1][exit.z] == room.id)
				return dungeon.map.room[exit.x+1][exit.z];
		} else {
			if(dungeon.map.room[exit.x][exit.z+1] == room.id)
				return dungeon.map.room[exit.x][exit.z-1];
			if(dungeon.map.room[exit.x][exit.z-1] == room.id)
				return dungeon.map.room[exit.x][exit.z+1];
		}
		return 0; // This will result in a error (should not be the nullRoom)
	}
	
	
	/**
	 * This will run A* on exits to ensure all can be reached. I will also
	 * ensure the same door is used in connected rooms by giving a negative
	 * value and passing it to the connected rooms DoorQueue.	 * 
	 * 
	 * @param exits
	 */
	public static void checkConnections(ArrayList<Doorway> exits, 
					Room room, Dungeon dungeon) {
		if(exits.isEmpty()) return;
		Doorway next, current;
		ArrayList<Doorway> connected = new ArrayList<Doorway>(exits.size());
		connected.add(exits.remove(exits.size() - 1));
		while(!exits.isEmpty()) {
			current = connected.get(0);
			connected.add(next = exits.remove(exits.size() - 1));
			new AStar(room, dungeon, current, next).seek();			
			Collections.shuffle(connected, dungeon.random);
		}
		for(Doorway exit : connected) {
			exit.priority = -16;
			dungeon.rooms.get(getOtherRoom(exit, room, dungeon)).addToConnections(exit);
		}
	}
	
	
	public static void retestDoors(Dungeon dungeon, Room room) {
		//System.out.println("[DLDUNGEONS] runnning retestDoors on room " 
		//		+ room.id);
		if(room.doors.isEmpty()) {
			//System.out.println("[DLDUNGEONS] List rooms was empty for " 
			//		+ "room " + room.id);
			return;
		}
		for(Doorway door : room.doors) {
			//System.out.println("[DLDUNGEONS] Testing a door");
			//if(dungeon.map.astared[door.x][door.z]) continue;
			if(door.xOriented) {
				if(dungeon.map.isWall[door.x+1][door.z] || 
						dungeon.map.isWall[door.x-1][door.z])
					dungeon.map.isDoor[door.x][door.z] = false;
				if(dungeon.map.hasLiquid[door.x+1][door.z] != 
						dungeon.map.hasLiquid[door.x-1][door.z])
					dungeon.map.isDoor[door.x][door.z] = false;
				if(dungeon.map.hasLiquid[door.x+1][door.z]) {
					dungeon.map.hasLiquid[door.x][door.z] = true;
					if(dungeon.theme.flags.contains(ThemeFlags.SWAMPY)) 
							dungeon.map.floorY[door.x][door.z] = (byte) (room.floorY - 1);
					else dungeon.map.floorY[door.x][door.z] = (byte) (room.floorY - 2);
				}
			} else {
				if(dungeon.map.isWall[door.x][door.z+1] || 
						dungeon.map.isWall[door.x][door.z-1])
					dungeon.map.isDoor[door.x][door.z] = false;
				if(dungeon.map.hasLiquid[door.x][door.z+1] != 
						dungeon.map.hasLiquid[door.x][door.z-1])
					dungeon.map.isDoor[door.x][door.z] = false;
				if(dungeon.map.hasLiquid[door.x][door.z+1]) {
					dungeon.map.hasLiquid[door.x][door.z] = true;
					if(dungeon.theme.flags.contains(ThemeFlags.SWAMPY)) 
							dungeon.map.floorY[door.x][door.z] = (byte) (room.floorY - 1);
					else dungeon.map.floorY[door.x][door.z] = (byte) (room.floorY - 2);
				}
			}
		}
	}
	
	
	public static void processDoors1(Dungeon dungeon, Room room) {
		ArrayList<Tile> invalid = new ArrayList<Tile>();
		boolean valid;	
		for(Doorway door : room.doors) {
			valid = validateDoor(dungeon, door);
			if(!valid) {
				invalid.add(door);
			} else {
				door.prioritize(dungeon, room.id); // Will only be run on valid doors
				room.addToConnections(door);
			}
		}
		room.doors.removeAll(invalid);
	}
	
	
	public static void checkConnectivity(Dungeon dungeon) {		
		ArrayList<ArrayList<Room>> sections = new RoomBFS(dungeon).check();
		while(sections.size() > 1) {
//			System.out.println("[DLDUNGEONS] Merging two sections.");
			Collections.sort(sections, c);
			new AStar2(dungeon, sections.get(0).get(0), 
					sections.get(1).get(0)).seek();
			sections.get(1).addAll(sections.get(0));
			sections.remove(0);
//			System.out.println("[DLDUNGEONS] Dungeon has " + sections.size()
//					+ " sections.");
//			if(sections.size() > 1) System.out.println("[DLDUNGEONS] Dungeon "
//					+ "is not connected!");
		}
	}
	
	
	public static void processDoors2(Dungeon dungeon, Room room) {
		checkConnections(makeConnectionList(room, dungeon.random), room, dungeon);
		retestDoors(dungeon, room);
	}
}
