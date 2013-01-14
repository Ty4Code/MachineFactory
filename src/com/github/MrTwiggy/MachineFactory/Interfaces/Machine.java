package com.github.MrTwiggy.MachineFactory.Interfaces;

import org.bukkit.inventory.ItemStack;

import com.github.MrTwiggy.MachineFactory.InteractionResponse;

/**
 * Machine.java
 * Purpose: An interface for machines to implement with basic functionality
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public interface Machine
{
	/**
	 * Updates the machine
	 */
	public void update();
	
	/**
	 * Destroys the machine and drops the given item
	 */
	public void destroy(ItemStack item);
	
	/**
	 * Powers on the machine
	 */
	public void powerOn();
	
	/**
	 * Powers off the machine
	 */
	public void powerOff();
	
	/**
	 * Toggles the current power state and returns interaction response
	 */
	public InteractionResponse togglePower();
}
