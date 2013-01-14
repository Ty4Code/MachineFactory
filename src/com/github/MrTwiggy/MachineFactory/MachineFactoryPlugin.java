package com.github.MrTwiggy.MachineFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.MrTwiggy.MachineFactory.Managers.MachinesManager;

/**
 * MachineFactoryPlugin.java
 * Purpose: Main class for MachineFactory plugin
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class MachineFactoryPlugin extends JavaPlugin
{
	MachinesManager manager; //The complete manager
	public static HashMap<Integer,OreGinProperties> Ore_Gin_Properties; //Map of properties for all tiers
	
	public static final String VERSION = "v0.1"; //Current version of plugin
	public static final String PLUGIN_NAME = "MachineFactory"; //Name of plugin
	public static final String PLUGIN_PREFIX = PLUGIN_NAME + " " + VERSION + ": "; //The prefix used for console outputs
	public static final String ORE_GIN_SAVES_FILE = "OreGinSaves"; //The ore gin saves file name
	public static final String SMELTER_SAVES_FILE = "SmelterSaves"; //The smelter saves file name
	public static final int TICKS_PER_SECOND = 20; //The number of ticks per second
	
	public static final String CITADEL_NAME = "Citadel"; //The plugin name for 'Citadel'
	
	public static int UPDATE_CYCLE; //Update time in ticks
	public static int MAXIMUM_BLOCK_BREAKS_PER_CYCLE; //The maximum number of block breaks per update cycle.
	public static int SAVE_CYCLE; //The time between periodic saves in minutes
	public static boolean CITADEL_ENABLED; //Whether the plugin 'Citadel' is enabled on this server
	public static boolean OREGIN_ENABLED; //Whether the machine 'Ore Gin' is enabled
	public static boolean SMELTER_ENABLED; //Whether the machine 'Smelter' is enabled
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
		initializeOreGinProperties();
		
		if (properPluginsLoaded())
		{
			getLogger().info(PLUGIN_NAME + " " + VERSION + " has been enabled!");
			getConfig().options().copyDefaults(true);
			manager = new MachinesManager(this);	
		}
		else
		{
			MachineFactoryPlugin.sendConsoleMessage("The Citadel config value is not correct for loaded plugins! Disabling OreGin now!");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	/**
	 * Disabled Function
	 */
	public void onDisable()
	{
		manager.onDisable();
		
		getLogger().info(PLUGIN_NAME + " " + VERSION + " has been disabled!");
	}
	
	/*
	 ----------CONFIG LOGIC--------
	 */
	
	/**
	 * Initializes the default OreGinProperties from config
	 */
	@SuppressWarnings("unchecked")
	public void initializeOreGinProperties()
	{
		Ore_Gin_Properties = new HashMap<Integer,OreGinProperties>();
		
		//Load general config
		MachineFactoryPlugin.CITADEL_ENABLED = getConfig().getBoolean("general.citadel_enabled");
		MachineFactoryPlugin.OREGIN_ENABLED = getConfig().getBoolean("general.oregin_enabled");
		MachineFactoryPlugin.SMELTER_ENABLED = getConfig().getBoolean("general.smelter_enabled");
		MachineFactoryPlugin.SAVE_CYCLE = getConfig().getInt("general.save_cycle");
		
		//Load general config values
		MachineFactoryPlugin.UPDATE_CYCLE = getConfig().getInt("oregin_general.update_cycle");
		MachineFactoryPlugin.MAXIMUM_BLOCK_BREAKS_PER_CYCLE = getConfig().getInt("oregin_general.maximum_block_breaks_per_cycle");
		MachineFactoryPlugin.OREGIN_UPGRADE_WAND = Material.valueOf(getConfig().getString("oregin_general.oregin_upgrade_wand"));
		MachineFactoryPlugin.OREGIN_ACTIVATION_WAND = Material.valueOf(getConfig().getString("oregin_general.oregin_activation_wand"));
		MachineFactoryPlugin.OREGIN_REPAIR_WAND = Material.valueOf(getConfig().getString("oregin_general.oregin_repair_wand"));
		MachineFactoryPlugin.LIGHT_ON = Material.valueOf(getConfig().getString("oregin_general.oregin_light_on"));
		MachineFactoryPlugin.LIGHT_OFF = Material.valueOf(getConfig().getString("oregin_general.oregin_light_off"));
		MachineFactoryPlugin.MAX_TIERS = getConfig().getInt("oregin_general.max_tiers");
		MachineFactoryPlugin.REDSTONE_ACTIVATION_ENABLED = getConfig().getBoolean("oregin_general.redstone_activation_enabled");
		MachineFactoryPlugin.LAVA_MINING_ENABLED = getConfig().getBoolean("oregin_general.lava_mining_enabled");
		MachineFactoryPlugin.WATER_MINING_ENABLED = getConfig().getBoolean("oregin_general.water_mining_enabled");
		MachineFactoryPlugin.JUNK_DESTRUCTION_ENABLED = getConfig().getBoolean("oregin_general.junk_destruction_enabled");
		
		//Load valuables
		List<String> valuablesMaterialStrings = (List<String>) getConfig().getList("oregin_general.valuables");
		VALUABLES = new ArrayList<Material>();
		for (String string : valuablesMaterialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				VALUABLES.add(Material.valueOf(string));
			}			
		}
		
		//Load junk
		List<String> junkMaterialStrings = (List<String>) getConfig().getList("oregin_general.junk");
		JUNK = new ArrayList<Material>();
		for (String string : junkMaterialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				JUNK.add(Material.valueOf(string));
			}			
		};
		
		//Load indestructible
		List<String> indestructibleMaterialStrings = (List<String>) getConfig().getList("oregin_general.indestructible");
		INDESTRUCTIBLE = new ArrayList<Material>();
		for (String string : indestructibleMaterialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				INDESTRUCTIBLE.add(Material.valueOf(string));
			}			
		};
		

		//Load OreGin tier properties
		for (int i = 1; i <= MachineFactoryPlugin.MAX_TIERS; i++)
		{
			int max_mining_distance = getConfig().getInt(getOreGinPropertiesPathStart(i) + "max_mining_distance"); 
			int max_block_breaks= getConfig().getInt(getOreGinPropertiesPathStart(i) + "max_block_breaks"); 
			int mining_delay= getConfig().getInt(getOreGinPropertiesPathStart(i) + "mining_delay"); 
			boolean retrieve_valuables = getConfig().getBoolean(getOreGinPropertiesPathStart(i) + "retrieve_valuables");
			Material fuel_type = Material.valueOf(getConfig().getString(getOreGinPropertiesPathStart(i) + "fuel_type"));
			int fuel_amount= getConfig().getInt(getOreGinPropertiesPathStart(i) + "fuel_amount");
			Material upgrade_material = Material.valueOf(getConfig().getString(getOreGinPropertiesPathStart(i) + "upgrade_material")); 
			int upgrade_amount = getConfig().getInt(getOreGinPropertiesPathStart(i) + "upgrade_amount");
			Material repair_material = Material.valueOf(getConfig().getString(getOreGinPropertiesPathStart(i) + "repair_material")); 
			int repair_amount = getConfig().getInt(getOreGinPropertiesPathStart(i) + "repair_amount");
			int shaft_width = getConfig().getInt(getOreGinPropertiesPathStart(i) + "shaft_width");
			int shaft_height = getConfig().getInt(getOreGinPropertiesPathStart(i) + "shaft_height");
			
			Ore_Gin_Properties.put(i, new OreGinProperties(max_mining_distance, max_block_breaks,
									mining_delay, retrieve_valuables, fuel_amount, fuel_type, shaft_width, shaft_height,
									upgrade_material, upgrade_amount, repair_material, repair_amount));
		}
		
		MachineFactoryPlugin.sendConsoleMessage("Config values successfully loaded!");
		saveConfig();
	}

	/**
	 * Returns the path for tier level starts
	 */
	public String getOreGinPropertiesPathStart(int tierLevel)
	{
		return "oregin_general.oregin_tier_properties.tier" + tierLevel + ".";
	}
	
	/**
	 * Returns whether the proper plugins are loaded based on config values
	 */
	public boolean properPluginsLoaded()
	{
		return ( (getServer().getPluginManager().getPlugin(CITADEL_NAME) != null && MachineFactoryPlugin.CITADEL_ENABLED)
				|| (getServer().getPluginManager().getPlugin(CITADEL_NAME) == null && !MachineFactoryPlugin.CITADEL_ENABLED));
	}
	
	/*
	 ----------SAVING/LOADING LOGIC--------
	 */
	
	/**
	 * Sends a message to the console with appropriate prefix
	 */
	public static void sendConsoleMessage(String message)
	{
		Bukkit.getLogger().info(MachineFactoryPlugin.PLUGIN_PREFIX + message);
	}
}
