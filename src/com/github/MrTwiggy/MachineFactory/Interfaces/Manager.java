package com.github.MrTwiggy.MachineFactory.Interfaces;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;

import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;


/**
 * Manager.java
 * Purpose: Interface for Manager objects for basic manager functionality
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public interface Manager 
{

	/**
	 * Saves the machine objects list of this manager to file
	 */
	public void save(File file) throws IOException;
	
	/**
	 * Loads machine objects list of this manager from file
	 */
	public void load(File file) throws IOException;
	
	/**
	 * Updates all the machines from this manager's machine object list
	 */
	public void updateMachines();

	/**
	 * Attempts to create a new machine for this manager
	 */
	public InteractionResponse createMachine(Location machineLocation);

	/**
	 * Creates a machine from an existing machine data object
	 */
	public InteractionResponse addMachine(Machine machine);

	/**
	 * Returns the machine (if any exists) at the given location from this manager
	 */
	public Machine getMachine(Location machineLocation);

	/**
	 * Returns whether a machine exists at the given location
	 */
	public boolean machineExistsAt(Location machineLocation);
	
	/**
	 * Removes the given machine from the object list
	 */
	public void removeMachine(Machine machine);
	
	/**
	 * Returns the saves file name for this manager
	 */
	public String getSavesFileName();
	
}
