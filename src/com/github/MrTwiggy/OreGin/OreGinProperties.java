package com.github.MrTwiggy.OreGin;

import org.bukkit.Material;

/**
 * OreGinProperties.java
 * Purpose: Object for holding tier-specific OreGin Properties
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGinProperties 
{
	private final int max_mining_distance; //Maximum mining distance that can be reached by OreGin
	private final int max_block_breaks; //Maximum number of block breaks before OreGin breaks
	private final int mining_delay; //Delay between mining operations in ticks
	private final boolean retrieve_valuables; //Whether mined valuables are transported to OreGin automatically
	private final Material fuel_material; //The type of fuel consumed
	private final int fuel_amount; //The amount of fuel consumed per mining operation
	private final Material upgrade_material; //The type of item consumed for upgrading to this tier
	private final int upgrade_amount; //The amount of upgrade_type's required for upgrading
	private final Material repair_material; //The type of item consumed for repairing in this tier
	private final int repair_amount; //The amount of repair_material's required for repairing
	private final int shaft_width; //The width of the shaft being dug
	private final int shaft_height; //The height of the shaft being dug

	/**
	 * Constructor
	 */
	public OreGinProperties(int max_mining_distance, int max_block_breaks, int mining_delay, boolean retrieve_valuables,
			int fuel_amount, Material fuel_material, int shaft_width, int shaft_height,
			Material upgrade_material, int upgrade_amount, Material repair_material, int repair_amount)
	{
		this.max_mining_distance = max_mining_distance;
		this.max_block_breaks = max_block_breaks;
		this.mining_delay = mining_delay;
		this.retrieve_valuables = retrieve_valuables;
		this.fuel_amount = fuel_amount;
		this.fuel_material = fuel_material;
		this.shaft_width = shaft_width;
		this.shaft_height = shaft_height;
		this.upgrade_material = upgrade_material;
		this.upgrade_amount = upgrade_amount;
		this.repair_material = repair_material;
		this.repair_amount = repair_amount;
	}
	
	/*
	 ----------PUBLIC ACCESSORS--------
	 */
	
	/**
	 * 'retrieve_valuables' public accessor
	 */
	public boolean GetRetrieveValuables()
	{
		return retrieve_valuables;
	}
	
	/**
	 * 'repair_material' public accessor
	 */
	public Material GetRepairMaterial()
	{
		return repair_material;
	}
	
	/**
	 * 'repair_amount' public accessor
	 */
	public int GetRepairAmount()
	{
		return repair_amount;
	}
	
	/**
	 * 'upgrade_amount' public accessor
	 */
	public int GetUpgradeAmount()
	{
		return upgrade_amount;
	}
	
	/**
	 * 'upgrade_material' public accessor
	 */
	public Material GetUpgradeMaterial()
	{
		return upgrade_material;
	}
	
	/**
	 * 'mining_delay' public accessor
	 */
	public int GetMiningDelay()
	{
		return mining_delay;
	}
	
	/**
	 * 'fuel_amount' public accessor
	 */
	public int GetFuelAmount()
	{
		return fuel_amount;
	}
	
	/**
	 * 'fuel_material' public accessor
	 */
	public Material GetFuelMaterial()
	{
		return fuel_material;
	}
	
	/**
	 * 'max_mining_distance' public accessor
	 */
	public int GetMaxMiningDistance()
	{
		return max_mining_distance;
	}
	
	/**
	 * 'shaft_width' public accessor
	 */
	public int GetShaftWidth()
	{
		return shaft_width;
	}
	
	/**
	 * 'shaft_height' public accessor
	 */
	public int GetShaftHeight()
	{
		return shaft_height;
	}
	
	/**
	 * 'max_block_breaks' public accessor
	 */
	public int GetMaxBlockBreaks()
	{
		return max_block_breaks;
	}
}
