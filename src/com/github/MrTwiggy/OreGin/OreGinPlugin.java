package com.github.MrTwiggy.OreGin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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
	
	public static int UPDATE_CYCLE; //Update time in ticks
	public static Material OREGIN_UPGRADE_WAND; //The wand used for creating and upgrading OreGins
	public static Material OREGIN_ACTIVATION_WAND; //The wand used for powering OreGins
	public static Material OREGIN_REPAIR_WAND; //The wand used for repairing OreGins
	public static int MAX_TIERS; //The maximum number of tiers available for OreGins
	public static boolean LAVA_MINING_ENABLED; //Whether lava mining is enabled
	public static boolean WATER_MINING_ENABLED; //Whether water mining is enabled
	public static boolean JUNK_DESTRUCTION_ENABLED; //Whether junk destruction is enabled
	public static List<Material> VALUABLES; //List of valuables
	public static List<Material> JUNK; //List of junk
	public static Material LIGHT_ON = Material.REDSTONE_LAMP_ON; //Material for light_on
	public static Material LIGHT_OFF = Material.REDSTONE_LAMP_OFF; //Material for light_off

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
		ConfigurationSerialization.registerClass(OreGinSave.class);
		
		LoadOreGins();
	}
	
	/**
	 * Disabled Function
	 */
	public void onDisable()
	{
		SaveOreGins();
		getLogger().info(PLUGIN_NAME + " " + VERSION + " has been disabled!");
	}
	
	/**
	 * Initializes the default OreGinProperties from config
	 */
	@SuppressWarnings("unchecked")
	public void InitializeOreGinProperties()
	{
		//Load general config values
		OreGinPlugin.UPDATE_CYCLE = getConfig().getInt("general.update_cycle");
		OreGinPlugin.OREGIN_UPGRADE_WAND = Material.valueOf(getConfig().getString("general.oregin_upgrade_wand"));
		OreGinPlugin.OREGIN_ACTIVATION_WAND = Material.valueOf(getConfig().getString("general.oregin_activation_wand"));
		OreGinPlugin.OREGIN_REPAIR_WAND = Material.valueOf(getConfig().getString("general.oregin_repair_wand"));
		OreGinPlugin.MAX_TIERS = getConfig().getInt("general.max_tiers");
		OreGinPlugin.LAVA_MINING_ENABLED = getConfig().getBoolean("general.lava_mining_enabled");
		OreGinPlugin.WATER_MINING_ENABLED = getConfig().getBoolean("general.water_mining_enabled");
		OreGinPlugin.JUNK_DESTRUCTION_ENABLED = getConfig().getBoolean("general.junk_destruction_enabled");
		
		//Load valuables
		List<String> materialStrings = (List<String>) getConfig().getList("general.valuables");
		VALUABLES = new ArrayList<Material>();
		for (String string : materialStrings)
		{
			if (Material.valueOf(string) != null)
			{
				VALUABLES.add(Material.valueOf(string));
			}			
		}
		
		//Load junk
		JUNK = new ArrayList<Material>();
		JUNK.add(Material.STONE);
		JUNK.add(Material.DIRT);
		JUNK.add(Material.GRAVEL);

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
	}

	/**
	 * Saves all the OreGin data to file
	 */
	public void SaveOreGins()
	{
		List<OreGinSave> oreGinSaves = new ArrayList<OreGinSave>();
		for (OreGin oreGin : oreGinMan.oreGins)
		{
			oreGinSaves.add(new OreGinSave(oreGin));
		}
		getConfig().set("oregins", oreGinSaves);
		saveConfig();
		getLogger().info(PLUGIN_PREFIX + "Successfully saved " + oreGinSaves.size() + " OreGins!");
	}
	
	/**
	 * Loads all the OreGin data from file
	 */
	@SuppressWarnings("unchecked")
	public void LoadOreGins()
	{
		List<OreGinSave> oreGinSaves = (List<OreGinSave>) getConfig().getList("oregins");
		
		if (oreGinSaves != null)
		{
			for (OreGinSave oreGinSave : oreGinSaves)
			{
				oreGinMan.CreateOreGin(oreGinSave.ReturnSave(this));
			}
			getLogger().info(PLUGIN_PREFIX + "Successfully loaded " + oreGinSaves.size() + " OreGins!");
		}
		else
		{
			getLogger().info(PLUGIN_PREFIX + "No OreGins available to load!");
		}
	}
	
	/**
	 * Returns the path for tier level starts
	 */
	public String OreGinPropertiesPathStart(int tierLevel)
	{
		return "oregin_tier_properties.tier" + tierLevel + ".";
	}
	
}
