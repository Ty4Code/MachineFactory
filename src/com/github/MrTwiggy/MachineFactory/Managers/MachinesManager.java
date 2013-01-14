package com.github.MrTwiggy.MachineFactory.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.bukkit.Bukkit;

import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
import com.github.MrTwiggy.MachineFactory.Interfaces.Manager;
import com.github.MrTwiggy.MachineFactory.Listeners.OreGinListener;
import com.github.MrTwiggy.MachineFactory.Listeners.SmelterListener;

/**
 * MachinesManager.java
 * Purpose: Manages the initialization and updating of all managers.
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class MachinesManager 
{
	OreGinManager oreGinMan; // Manager object for OreGin
	SmelterManager smelterMan; // Manager object for Smelter
	OreGinListener oreGinListener; // Listener object for OreGin
	SmelterListener smelterListener; //Listener object for Smelter
	
	MachineFactoryPlugin plugin; //The plugin object
	
	
	/**
	 * Constructor
	 */
	public MachinesManager(MachineFactoryPlugin plugin)
	{
		this.plugin = plugin;
		
		initializeManagers();
		
		periodicSaving();
	}
	
	/**
	 * Initializes the necassary managers for enabled machines
	 */
	private void initializeManagers()
	{
		if (MachineFactoryPlugin.OREGIN_ENABLED)
		{
			initializeOreGinManager();
		}
		
		if (MachineFactoryPlugin.SMELTER_ENABLED)
		{
			initializeSmelterManager();
		}
	}
	
	/**
	 * Initializes the Smelter Manager
	 */
	private void initializeSmelterManager()
	{
		smelterMan = new SmelterManager(plugin);
		smelterListener = new SmelterListener(smelterMan);
		plugin.getServer().getPluginManager().registerEvents(smelterListener, plugin);
		load(smelterMan, getSmelterSavesFile());
	}
	
	/**
	 * Initializes the Ore Gin Manager
	 */
	private void initializeOreGinManager()
	{
		oreGinMan = new OreGinManager(plugin);
		oreGinListener = new OreGinListener(oreGinMan);
		plugin.getServer().getPluginManager().registerEvents(oreGinListener, plugin);
		load(oreGinMan, getOreGinSavesFile());
	}
	
	/**
	 * When plugin disabled, this is called.
	 */
	public void onDisable()
	{
		if (oreGinMan != null)
			save(oreGinMan, getOreGinSavesFile());
	}
	
	/**
	 * Load file
	 */
	private static void load(Manager managerInterface, File file) 
	{
		try
		{
			managerInterface.load(file);
		}
		catch (FileNotFoundException exception)
		{
			Bukkit.getServer().getLogger().info(file.getName() + " does not exist! Creating file!");
		}
		catch (IOException exception)
		{
			throw new RuntimeException("Failed to load " + file.getPath(), exception);
		}
		
		try
		{
			managerInterface.save(file);
		}
		catch (IOException exception)
		{
			throw new RuntimeException("Failed to create " + file.getPath(), exception);
		}
	}

	/**
	 * Save file
	 */
	private static void save(Manager managerInterface, File file) 
	{	
		try
		{
			File newFile = new File(file.getAbsolutePath() + ".new");
			File bakFile = new File(file.getAbsolutePath() + ".bak");
			
			managerInterface.save(newFile);
			
			if (bakFile.exists())
			{
				bakFile.delete();
			}
			
			if (file.exists() && !file.renameTo(bakFile))
			{
				throw new IOException("Failed to rename " + file.getAbsolutePath() + " to " + bakFile.getAbsolutePath());
			}
			
			if (!newFile.renameTo(file))
			{
				throw new IOException("Failed to rename " + newFile.getAbsolutePath() + " to " + file.getAbsolutePath());
			}
		}
		catch (IOException exception)
		{
			throw new RuntimeException("Failed to save to " + file.getAbsolutePath(), exception);
		}
	}
	
	/**
	 * Save OreGins to file every SAVE_CYCLE minutes.
	 */
	private void periodicSaving()
	{
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
		    @Override  
		    public void run() {
		    	MachineFactoryPlugin.sendConsoleMessage("Saving OreGin data...");
		    	save(oreGinMan, getOreGinSavesFile());
		    }
		}, (MachineFactoryPlugin.SAVE_CYCLE * MachineFactoryPlugin.TICKS_PER_SECOND * 60), 
		MachineFactoryPlugin.SAVE_CYCLE * MachineFactoryPlugin.TICKS_PER_SECOND * 60);
	}
	
	/**
	 * Returns the OreGin Saves file
	 */
	public File getOreGinSavesFile()
	{
		return new File(plugin.getDataFolder(), MachineFactoryPlugin.ORE_GIN_SAVES_FILE + ".txt");
	}
	
	/**
	 * Returns the Smelter Saves file
	 */
	public File getSmelterSavesFile()
	{
		return new File(plugin.getDataFolder(), MachineFactoryPlugin.SMELTER_SAVES_FILE + ".txt");
	}

}
