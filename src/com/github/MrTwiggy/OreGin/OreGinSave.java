package com.github.MrTwiggy.OreGin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

/**
 * OreGinSave.java
 * Purpose: Used for serialization of OreGin objects
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGinSave implements ConfigurationSerializable
{
	
	private int blockBreaks; //Number of blocks broken by OreGin
	private int tierLevel; //Current tier level of OreGin
	private boolean mining; //Whether OreGin is currently mining
	private boolean broken; //Whether OreGin is currently broken
	private int miningDistance; //The current mining distance achieved by OreGin
	private double locX, locY, locZ; //Coordinates of OreGin location
	private String world; //World name of OreGin's location
	
	/**
	 * Constructor (Loading OreGins From File)
	 */
	public OreGinSave(Map<String,Object> map)
	{
		blockBreaks = (int)map.get("block_breaks");
		tierLevel = (int)map.get("tier_level");
		mining = (boolean)map.get("mining");
		broken = (boolean)map.get("broken");
		miningDistance = (int)map.get("mining_distance");
		locX = (double)map.get("loc_x");
		locY = (double)map.get("loc_y");
		locZ = (double)map.get("loc_z");
		world = (String)map.get("world");
	}
	
	/**
	 * Constructor (Loading data from OreGin for saving)
	 */
	public OreGinSave(OreGin oreGin)
	{
		blockBreaks = oreGin.GetBlockBreaks();
		tierLevel = oreGin.GetTierLevel();
		mining = oreGin.GetMining();
		broken = oreGin.GetBroken();
		miningDistance = oreGin.GetMiningDistance();
		locX = oreGin.GetLocation().getX();
		locY = oreGin.GetLocation().getY();
		locZ = oreGin.GetLocation().getZ();
		world = oreGin.GetLocation().getWorld().getName();
	}
	
	/**
	 * Returns respective OreGn object for this save
	 */
	public OreGin ReturnSave(OreGinPlugin plugin)
	{
		return new OreGin(blockBreaks, tierLevel, mining, broken,
				miningDistance, new Location(plugin.getServer().getWorld(world), locX, locY, locZ));
	}
	
	/**
	 * Serializing OreGinSave
	 */
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> resultMap = new HashMap<String,Object>();
		
		resultMap.put("block_breaks", blockBreaks);
		resultMap.put("tier_level", tierLevel);
		resultMap.put("mining", mining);
		resultMap.put("broken", broken);
		resultMap.put("mining_distance", miningDistance);
		resultMap.put("loc_x", locX);
		resultMap.put("loc_y", locY);
		resultMap.put("loc_z", locZ);
		resultMap.put("world", world);
		
		return resultMap;
	}

}
