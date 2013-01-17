package com.github.MrTwiggy.MachineFactory.Managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
import com.github.MrTwiggy.MachineFactory.Interfaces.Manager;
import com.github.MrTwiggy.MachineFactory.Listeners.CloakerListener;
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
	List<Listener> listeners;
	List<Manager> managers;
	
	MachineFactoryPlugin plugin; //The plugin object
	
	
	/**
	 * Constructor
	 */
	public MachinesManager(MachineFactoryPlugin plugin)
	{
		this.plugin = plugin;
		
		initializeManagers();
		loadManagers();
		
		periodicSaving();
	}
	
	/**
	 * Initializes the necassary managers for enabled machines
	 */
	private void initializeManagers()
	{
		managers = new ArrayList<Manager>();
		listeners = new ArrayList<Listener>();
		
		if (MachineFactoryPlugin.OREGIN_ENABLED)
		{
			initializeOreGinManager();
		}
		
		if (MachineFactoryPlugin.SMELTER_ENABLED)
		{
			initializeSmelterManager();
		}
		if (MachineFactoryPlugin.CLOAKER_ENABLED)
		{
			initializeCloakerManager();
		}
	}
	
	/**
	 * Initializes the Smelter Manager
	 */
	private void initializeSmelterManager()
	{
		SmelterManager smelterMan = new SmelterManager(plugin);
		Listener smelterListener = new SmelterListener(smelterMan);
		plugin.getServer().getPluginManager().registerEvents(smelterListener, plugin);
		
		managers.add(smelterMan);
		listeners.add(smelterListener);
	}
	
	/**
	 * Initializes the Ore Gin Manager
	 */
	private void initializeOreGinManager()
	{
		OreGinManager oreGinMan = new OreGinManager(plugin);
		OreGinListener oreGinListener = new OreGinListener(oreGinMan);
		plugin.getServer().getPluginManager().registerEvents(oreGinListener, plugin);
		
		managers.add(oreGinMan);
		listeners.add(oreGinListener);
	}
	
	/**
	 * Initializes the Cloaker Manager
	 */
	private void initializeCloakerManager()
	{
		CloakerManager cloakerMan = new CloakerManager(plugin);
		CloakerListener cloakerListener = new CloakerListener(cloakerMan);
		plugin.getServer().getPluginManager().registerEvents(cloakerListener, plugin);
		
		managers.add(cloakerMan);
		listeners.add(cloakerListener);
		//LOAD
	}
	
	/**
	 * When plugin disabled, this is called.
	 */
	public void onDisable()
	{
		saveManagers();
	}
	
	/**
	 * Saves all managers
	 */
	private void saveManagers()
	{
		for (Manager manager : managers)
		{
			save(manager, getSavesFile(manager.getSavesFileName()));
		}
	}
	
	/**
	 * Loads all managers
	 */
	private void loadManagers()
	{
		for (Manager manager : managers)
		{
			load(manager, getSavesFile(manager.getSavesFileName()));
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Manager getManager(Class managerType)
	{
		for (Manager manager : managers)
		{
			if (managerType.isInstance(manager))
			{
				return manager;
			}
		}
		
		return null;
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
	private static void save(Manager manager, File file) 
	{	
		try
		{
			File newFile = new File(file.getAbsolutePath() + ".new");
			File bakFile = new File(file.getAbsolutePath() + ".bak");
			
			manager.save(newFile);
			
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
		    	MachineFactoryPlugin.sendConsoleMessage("Saving Machine data...");
		    	saveManagers();
		    }
		}, (MachineFactoryPlugin.SAVE_CYCLE * MachineFactoryPlugin.TICKS_PER_SECOND * 60), 
		MachineFactoryPlugin.SAVE_CYCLE * MachineFactoryPlugin.TICKS_PER_SECOND * 60);
	}
	
	/**
	 * Returns the OreGin Saves file
	 */
	public File getSavesFile(String fileName)
	{
		return new File(plugin.getDataFolder(), fileName + ".txt");
	}

}
