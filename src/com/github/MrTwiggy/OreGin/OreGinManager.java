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
	
	private List<OreGin> oreGins; //List of current OreGins
	private OreGinPlugin plugin; //OreGinPlugin object
	
	private int blockBreaksDuringCycle = 0;//The number of blocks broken during the current cycle.
	
	/**
	 * Constructor
	 */
	public OreGinManager(OreGinPlugin plugin)
	{
		this.plugin = plugin;
		oreGins = new ArrayList<OreGin>();
		
		updateOreGins();
	}

	/**
	 * Updates all the active OreGins
	 */
	public void updateOreGins()
	{
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable()
		{
		    @Override  
		    public void run() 
		    {
		    	blockBreaksDuringCycle = 0;
		        UpdateOreGins();
		    }
		}, 0L, OreGinPlugin.UPDATE_CYCLE);
	}
	
	/**
	 * 'blockBreaksDuringCycle' public accessor
	 */
	public int getBlockBreaksDuringCycle()
	{
		return blockBreaksDuringCycle;
	}
	
	/**
	 * Increments the blockBreaksDuringCycle member by 1.
	 */
	public void incrementBlockBreaksDuringCycle()
	{
		blockBreaksDuringCycle++;
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

		OreGinPlugin.sendConsoleMessage("Successfully loaded " + oreGins.size() + " Ore Gins!");
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
			Location location = oreGin.getLocation();
			bufferedWriter.append(location.getWorld().getName());
			bufferedWriter.append(" ");
			bufferedWriter.append(Integer.toString(location.getBlockX()));
			bufferedWriter.append(" ");
			bufferedWriter.append(Integer.toString(location.getBlockY()));
			bufferedWriter.append(" ");
			bufferedWriter.append(Integer.toString(location.getBlockZ()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Integer.toString(oreGin.getTierLevel()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Integer.toString(oreGin.getBlockBreaks()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Integer.toString(oreGin.getMiningDistance()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Boolean.toString(oreGin.getMining()));
            bufferedWriter.append(" ");
            bufferedWriter.append(Boolean.toString(oreGin.getBroken()));
            bufferedWriter.append("\n");
            
        }
		
		OreGinPlugin.sendConsoleMessage("Successfully saved " + oreGins.size() + " Ore Gins!");

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
			oreGin.update();
		}
	}
	
	/**
	 * Attempts to create an OreGin at the location and returns result message
	 */
	public String CreateOreGin(Location machineLocation)
	{
		OreGinProperties desiredTierProperties = OreGinPlugin.Ore_Gin_Properties.get(1);
		Material upgradeMaterial = desiredTierProperties.GetUpgradeMaterial();
		
		if (OreGin.validOreGinCreationLocation(machineLocation))
		{
			if (!OreGinExistsAt(machineLocation) && OreGin.validUpgrade(machineLocation, 1))
			{
				OreGin oreGin = new OreGin(machineLocation, this);
				AddOreGin(oreGin);
				oreGin.removeUpgradeMaterial(1);
				plugin.getLogger().info("New OreGin created!");
				return ChatColor.GREEN + "Successfully created OreGin!";
			}
			else
			{
				OreGinSoundCollection.ErrorSound().playSound(machineLocation);
				return ChatColor.RED + "Missing creation materials! " + OreGin.requiredAvailableMaterials(desiredTierProperties.GetUpgradeAmount(),
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
		if(oreGin.getLocation().getBlock().getType().equals(Material.DISPENSER) && !OreGinExistsAt(oreGin.getLocation()))
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
			if (oreGin.getLocation().equals(machineLocation))
				return oreGin;
		}
		
		return null;
	}

	/**
	 * Removes a specific OreGin from the list
	 */
	public void removeOreGin(OreGin oreGin)
	{
		oreGins.remove(oreGin);
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
