package com.github.MrTwiggy.MachineFactory;

import org.bukkit.Location;

/**
 * MachineObject.java
 * Purpose: Basic object base for machines to extend
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class MachineObject
{
	protected Location machineLocation; //Current location of machine center
	protected boolean active; //Whether machine is currently active
	protected Dimensions dimensions; //The dimensions of the machine
	
	/**
	 * Constructor
	 */
	public MachineObject(Location machineLocation, Dimensions dimensions)
	{
		this.machineLocation = machineLocation;
		this.dimensions = dimensions;
		this.active = false;
	}
	
	/**
	 * Constructor
	 */
	public MachineObject(Location machineLocation, Dimensions dimensions, boolean active)
	{
		this.machineLocation = machineLocation;
		this.dimensions = dimensions;
		this.active = active;
	}

}
