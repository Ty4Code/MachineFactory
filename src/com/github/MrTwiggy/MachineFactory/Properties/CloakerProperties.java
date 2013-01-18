package com.github.MrTwiggy.MachineFactory.Properties;

import org.bukkit.Material;

import com.github.MrTwiggy.MachineFactory.Interfaces.Properties;

/**
 * CloakerProperties.java
 * Purpose: Holds data for Cloaker Tier Properties
 *
 * @author MrTwiggy
 * @version 0.1 1/17/13
 */
public class CloakerProperties implements Properties
{
	Material upgrade_type; // The material type used to upgrade to this tier
	int upgrade_amount; // The amount of upgrade_type used to upgrade to this tier
	int fuel_time_duration; // The duration a single cloaking operation lasts
	Material fuel_type; // The material type used to fuel this tier
	int fuel_amount; // The amount of fuel_type used to fuel this tier for one cloaking operation
	int cloak_width; // The width of the cloaking field
	int cloak_height; // The height of the cloaking field
	int cloak_depth; // The depth of the cloaking field
	int visibility_range; // The range of visibility for the cloaker
	
	/**
	 * Constructor
	 */
	public CloakerProperties(Material upgrade_type, int upgrade_amount,
			int fuel_time_duration, Material fuel_type, int fuel_amount, 
			int cloak_width, int cloak_height, int cloak_depth, int visibility_range)
	{
		this.upgrade_type = upgrade_type;
		this.upgrade_amount = upgrade_amount;
		this.fuel_time_duration = fuel_time_duration;
		this.fuel_type = fuel_type;
		this.fuel_amount = fuel_amount;
		this.cloak_width = cloak_width;
		this.cloak_height = cloak_height;
		this.cloak_depth = cloak_depth;
		this.visibility_range = visibility_range;
	}
	
	/**
	 * 'upgrade_type' public accessor
	 */
	public Material getUpgradeMaterial()
	{
		return upgrade_type;
	}
	
	/**
	 * 'upgrade_amount' public accessor
	 */
	public int getUpgradeAmount()
	{
		return upgrade_amount;
	}
	
	/**
	 * 'fuel_amount' public accessor
	 */
	public int getFuelAmount()
	{
		return fuel_amount;
	}
	
	/**
	 * 'fuel_type' public accessor
	 */
	public Material getFuelMaterial()
	{
		return fuel_type;
	}
	
	/**
	 * 'visibility_range' public accessor
	 */
	public int getVisibilityRange()
	{
		return visibility_range;
	}
	
	/**
	 * 'cloak_depth' public accessor
	 */
	public int getDepth()
	{
		return cloak_depth;
	}
	
	/**
	 * 'cloak_width' public accessor
	 */
	public int getWidth()
	{
		return cloak_width;
	}
	
	/**
	 * 'cloak_height' public accessor
	 */
	public int getHeight()
	{
		return cloak_height;
	}
	
	/**
	 * 'fuel_time_duration' public accessor
	 */
	public int getFuelTimeDuration()
	{
		return fuel_time_duration;
	}

}
