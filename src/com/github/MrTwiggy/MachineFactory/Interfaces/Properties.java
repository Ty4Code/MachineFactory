package com.github.MrTwiggy.MachineFactory.Interfaces;

import org.bukkit.Material;

/**
 * Properties.java
 * Purpose: Interface for Properties objects for basic properties functionality
 *
 * @author MrTwiggy
 * @version 0.1 1/17/13
 */
public interface Properties 
{
	/**
	 * Returns the amount of upgrade materials required to reach this tier
	 */
	public int getUpgradeAmount();
	
	/**
	 * Returns the material used to upgrade to this tier
	 */
	public Material getUpgradeMaterial();
}
