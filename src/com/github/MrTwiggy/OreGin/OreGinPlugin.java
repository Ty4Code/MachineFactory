package com.github.MrTwiggy.OreGin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * OreGinPlugin.java
 * Purpose: Main class for OreGin plugin
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGinPlugin extends JavaPlugin
{
	OreGinManager oreGinMan; //The OreGin manager
	OreGinListener oreGinListener; //The OreGin listener
	public static HashMap<Integer,OreGinProperties> Ore_Gin_Properties; //Map of properties for all tiers
	
	public static final String VERSION = "v0.1"; //Current version of plugin
	public static final String PLUGIN_NAME = "OreGin"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": "; //The prefix used for console outputs
	public static final String ORE_GIN_SAVES_DIRECTORY = "OreGinSaves";
	public static final int TICKS_PER_SECOND = 20; //The number of ticks per second
	
	public static int UPDATE_CYCLE; //Update time in ticks
	public static int SAVE_CYCLE; //The time between periodic saves in minutes
	public static Material OREGIN_UPGRADE_WAND; //The wand used for creating and upgrading OreGins
	public static Material OREGIN_ACTIVATION_WAND; //The wand used for powering OreGins
	public static Material OREGIN_REPAIR_WAND; //The wand used for repairing OreGins
	public static int MAX_TIERS; //The maximum number of tiers available for OreGins
	public static boolean REDSTONE_ACTIVATION_ENABLED; //Whether OreGins can be activated via redstone
	public static boolean LAVA_MINING_ENABLED; //Whether lava mining is enabled
	public static boolean WATER_MINING_ENABLED; //Whether water mining is enabled
	public static boolean JUNK_DESTRUCTION_ENABLED; //Whether junk destruction is enabled
	public static List<Material> VALUABLES; //List of valuables
	public static List<Material> JUNK; //List of junk
	public static List<Material> INDESTRUCTIBLE; //List of blocks indestructible from OreGin
	public static Material LIGHT_ON; //Material for light_on
	public static Material LIGHT_OFF; //Material for light_off

	/**
	 * Enabled Function
	 */
	public void onEnable()
	{
		getLogger().info(PLUGIN_NAME + " " + VERSION + " has been enabled!");
		
		getConfig().options().copyDefaults(true);
		Ore_Gin_Properties = new HashMap<Integer,OreGinProperties>();
		InitializeOreGinProperties();
		
		oreGinMan = new OreGinManager(this);
		oreGinListener = new OreGinListener(oreGinMan);
		getServer().getPluginManager().registerEvents(oreGinListener, this);
		
		load(oreGinMan, getOreGinSavesFile());
		periodicSaving();
	}
	
	/**
	 * Disabled Function
	 */
	public void onDisable()
	{
		save(oreGinMan, getOreGinSavesFile());
		getLogger().info(PLUGIN_NAME + " " + VERSION + " has been disabled!");
	}
	
	/*
	 ----------CONFIG LOGIC--------
	 */
	
	/**
	 * Initializes the default OreGinProperties from config
	 */
	@SuppressWarnings("unchecked")
	public void InitializeOreGinProperties()
	{
		//Load general config values
		OreGinPlugin.UPDATE_CYCLE = getConfig().getInt("general.update_cycle");
		OreGinPlugin.SAVE_CYCLE = getConfig().getInt("general.save_cycle");
		OreGinPlugin.OREGIN_UPGRADE_WAND = Material.valueOf(getConfig().getString("general.oregin_upgrade_wand"));
		OreGinPlugin.OREGIN_ACTIVATION_WAND = Material.valueOf(getConfig().getString("general.oregin_activation_wand"));
		OreGinPlugin.OREGIN_REPAIR_WAND = Material.valueOf(getConfig().getString("general.oregin_repair_wand"));
		OreGinPlugin.LIGHT_ON = Material.valueOf(getConfig().getString("general.oregin_light_on"));
		OreGinPlugin.LIGHT_OFF = Material.valueOf(getConfig().getString("general.oregin_light_off"));
		OreGinPlugin.MAX_TIERS = getConfig().getInt("general.max_tiers");
		OreGinPlugin.REDSTONE_ACTIVATION_ENABLED = getConfig().getBoolean("general.redstone_activation_enabled");
		OreGinPlugin.LAVA_MINING_ENABLED = getConfig().getBoolean("general.lava_mining_enabled");
		OreGinPlugin.WATER_MINING_ENABLED = getConfig().getBoolean("general.water_mining_enabled");
		OreGinPlugin.JUNK_DESTRUCTION_ENABLED = getConfig().getBoolean("general.junk_destruction_enabled");
		
		//Load valuables
		List<String> valuablesMaterialStrings = (List<String>) getConfig().getList("general.valuables");
		VALUABLES = new ArrayList<Material>();
		for (String string : valuablesMaterialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				VALUABLES.add(Material.valueOf(string));
			}			
		}
		
		//Load junk
		List<String> junkMaterialStrings = (List<String>) getConfig().getList("general.junk");
		JUNK = new ArrayList<Material>();
		for (String string : junkMaterialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				JUNK.add(Material.valueOf(string));
			}			
		};
		
		//Load indestructible
		List<String> indestructibleMaterialStrings = (List<String>) getConfig().getList("general.indestructible");
		INDESTRUCTIBLE = new ArrayList<Material>();
		for (String string : indestructibleMaterialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				INDESTRUCTIBLE.add(Material.valueOf(string));
			}			
		};
		

		//Load OreGin tier properties
		for (int i = 1; i <= OreGinPlugin.MAX_TIERS; i++)
		{
			int max_mining_distance = getConfig().getInt(OreGinPropertiesPathStart(i) + "max_mining_distance"); 
			int max_block_breaks= getConfig().getInt(OreGinPropertiesPathStart(i) + "max_block_breaks"); 
			int mining_delay= getConfig().getInt(OreGinPropertiesPathStart(i) + "mining_delay"); 
			boolean retrieve_valuables = getConfig().getBoolean(OreGinPropertiesPathStart(i) + "retrieve_valuables");
			Material fuel_type = Material.valueOf(getConfig().getString(OreGinPropertiesPathStart(i) + "fuel_type"));
			int fuel_amount= getConfig().getInt(OreGinPropertiesPathStart(i) + "fuel_amount");
			Material upgrade_material = Material.valueOf(getConfig().getString(OreGinPropertiesPathStart(i) + "upgrade_material")); 
			int upgrade_amount = getConfig().getInt(OreGinPropertiesPathStart(i) + "upgrade_amount");
			Material repair_material = Material.valueOf(getConfig().getString(OreGinPropertiesPathStart(i) + "repair_material")); 
			int repair_amount = getConfig().getInt(OreGinPropertiesPathStart(i) + "repair_amount");
			int shaft_width = getConfig().getInt(OreGinPropertiesPathStart(i) + "shaft_width");
			int shaft_height = getConfig().getInt(OreGinPropertiesPathStart(i) + "shaft_height");
			
			Ore_Gin_Properties.put(i, new OreGinProperties(max_mining_distance, max_block_breaks,
									mining_delay, retrieve_valuables, fuel_amount, fuel_type, shaft_width, shaft_height,
									upgrade_material, upgrade_amount, repair_material, repair_amount));
		}
		
		OreGinPlugin.sendConsoleMessage("Config values successfully loaded!");
		saveConfig();
	}

	/**
	 * Returns the path for tier level starts
	 */
	public String OreGinPropertiesPathStart(int tierLevel)
	{
		return "oregin_tier_properties.tier" + tierLevel + ".";
	}
	
	
	/*
	 ----------SAVING/LOADING LOGIC--------
	 */
	
	/**
	 * Load file
	 */
	private static void load(ManagerInterface managerInterface, File file) 
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
	private static void save(ManagerInterface managerInterface, File file) 
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
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
		    @Override  
		    public void run() {
		    	OreGinPlugin.sendConsoleMessage("Saving OreGin data...");
		    	save(oreGinMan, getOreGinSavesFile());
		    }
		}, (OreGinPlugin.SAVE_CYCLE * OreGinPlugin.TICKS_PER_SECOND * 60), 
		OreGinPlugin.SAVE_CYCLE * OreGinPlugin.TICKS_PER_SECOND * 60);
	}
	
	/**
	 * Returns the OreGin Saves file
	 */
	public File getOreGinSavesFile()
	{
		return new File(getDataFolder(), ORE_GIN_SAVES_DIRECTORY + ".txt");
	}
	
	/**
	 * Sends a message to the console with appropriate prefix
	 */
	public static void sendConsoleMessage(String message)
	{
		Bukkit.getLogger().info(OreGinPlugin.PLUGIN_PREFIX + message);
	}
}
