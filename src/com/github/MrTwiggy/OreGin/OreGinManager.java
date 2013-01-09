package com.github.MrTwiggy.OreGin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;


/**
 * OreGinManager.java
 * Purpose: Manages the maintenance, creation, and destruction of OreGins
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGinManager implements ManagerInterface
{
	
	List<OreGin> oreGins; //List of current OreGins
	public OreGinPlugin plugin; //OreGinPlugin object
	
	/**
	 * Constructor
	 */
	public OreGinManager(OreGinPlugin plugin)
	{
		this.plugin = plugin;
		oreGins = new ArrayList<OreGin>();
		
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
		    @Override  
		    public void run() {
		        UpdateOreGins();
		    }
		}, 0L, OreGinPlugin.UPDATE_CYCLE);
	}
	
	/**
	 * Load OreGins from file
	 */
	public void load(File file) throws IOException 
	{
		FileInputStream fileInputStream = new FileInputStream(file);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

		String line;
		while ((line = bufferedReader.readLine()) != null) 
		{
			String parts[] = line.split(" ");
			//ORDER world loc_x loc_y loc_z tier_level block_breaks mining_distance mining broken
			
			Location oreGinLocation = new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]),
								Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));	
			int tierLevel = Integer.parseInt(parts[4]);
			int blockBreaks = Integer.parseInt(parts[5]);
			int miningDistance = Integer.parseInt(parts[6]);
			boolean mining = Boolean.parseBoolean(parts[7]);
			boolean broken = Boolean.parseBoolean(parts[8]);
			
			OreGin oreGin = new OreGin(blockBreaks, tierLevel, mining, broken, miningDistance, oreGinLocation, this);
			AddOreGin(oreGin);
		}

		Bukkit.getLogger().info("Successfully loaded " + oreGins.size() + " Ore Gins!");
		fileInputStream.close();
	}

	/**
	 * Save OreGins to file
	 */
	public void save(File file) throws IOException {
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
		
		for (OreGin oreGin : oreGins) 
		{
			//ORDER world loc_x loc_y loc_z tier_level block_breaks mining_distance mining broken
			Location location = oreGin.GetLocation();
			bufferedWriter.append(location.getWorld().getName());
			bufferedWriter.append(" ");
			bufferedWriter.append(Integer.toString(location.getBlockX()));
			bufferedWriter.append(" ");
			bufferedWriter.append(Integer.toString(location.getBlockY()));
			bufferedWriter.append(" ");
			bufferedWriter.append(Integer.toString(location.getBlockZ()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Integer.toString(oreGin.GetTierLevel()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Integer.toString(oreGin.GetBlockBreaks()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Integer.toString(oreGin.GetMiningDistance()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Boolean.toString(oreGin.GetMining()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Boolean.toString(oreGin.GetBroken()));
            bufferedWriter.append("\n");
            
        }
		
		Bukkit.getLogger().info("Successfully saved " + oreGins.size() + " Ore Gins!");

		bufferedWriter.flush();
		fileOutputStream.close();
	}
	
	/**
	 * Updates all the OreGins
	 */
	public void UpdateOreGins()
	{
		for (OreGin oreGin : oreGins)
		{
			oreGin.Update();
		}
	}
	
	/**
	 * Attempts to create an OreGin at the location and returns result message
	 */
	public String CreateOreGin(Location machineLocation)
	{
		OreGinProperties desiredTierProperties = OreGinPlugin.Ore_Gin_Properties.get(1);
		Material upgradeMaterial = desiredTierProperties.GetUpgradeMaterial();
		
		if (OreGin.ValidOreGinCreationLocation(machineLocation))
		{
			if (!OreGinExistsAt(machineLocation) && OreGin.ValidUpgrade(machineLocation, 1))
			{
				OreGin oreGin = new OreGin(machineLocation, this);
				AddOreGin(oreGin);
				oreGin.RemoveUpgradeMaterial(1);
				plugin.getLogger().info("New OreGin created!");
				return ChatColor.GREEN + "Successfully created OreGin!";
			}
			else
			{
				OreGinSoundCollection.ErrorSound().playSound(machineLocation);
				return ChatColor.RED + "Missing creation materials! " + OreGin.RequiredAvailableMaterials(desiredTierProperties.GetUpgradeAmount(),
						upgradeMaterial, machineLocation);	
			}
		}
		else
		{
			OreGinSoundCollection.ErrorSound().playSound(machineLocation);
			return ChatColor.RED + "Space above OreGin must be empty!";
		}
	}
	
	/**
	 * Attempts to create an OreGin of given OreGin data
	 */
	public boolean AddOreGin(OreGin oreGin)
	{
		if(oreGin.GetLocation().getBlock().getType().equals(Material.DISPENSER) && !OreGinExistsAt(oreGin.GetLocation()))
		{
			oreGins.add(oreGin);
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns the OreGin with a matching Location, if any
	 */
	public OreGin GetOreGin(Location machineLocation)
	{
		for (OreGin oreGin : oreGins)
		{
			if (oreGin.GetLocation().equals(machineLocation))
				return oreGin;
		}
		
		return null;
	}

	/**
	 * Returns whether an OreGin exists at the given Location
	 */
	public boolean OreGinExistsAt(Location machineLocation)
	{
		return (GetOreGin(machineLocation) != null);
	}
	
	/**
	 * Returns whether item is an OreGin
	 */
	public boolean IsOreGin(ItemStack item)
	{
		boolean result = false;
		
		if (item.getItemMeta().getDisplayName() == null)
			return false;
		
		for(int i = 1; i <= OreGinPlugin.MAX_TIERS; i++)
		{
			if (item.getItemMeta().getDisplayName().equalsIgnoreCase("T" + i + " OreGin"))
			{
				result = true;
				break;
			}
		}
		
		return result;
	}

	/**
	 * Returns whether location contains an OreGin light
	 */
	public boolean OreGinLightExistsAt(Location lightLocation)
	{
		return (OreGinExistsAt(lightLocation.getBlock().getRelative(BlockFace.DOWN).getLocation())
				&& (lightLocation.getBlock().getType().equals(OreGinPlugin.LIGHT_OFF) 
						|| lightLocation.getBlock().getType().equals(OreGinPlugin.LIGHT_ON)));
	}
	
}
