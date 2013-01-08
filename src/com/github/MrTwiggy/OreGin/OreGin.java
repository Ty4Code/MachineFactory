package com.github.MrTwiggy.OreGin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.lang.Math;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * OreGin.java
 * Purpose: Functionality for OreGin objects
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGin 
{

	private int blockBreaks; //Number of blocks broken by OreGin
	private int tierLevel; //Current tier level of OreGin
	private boolean mining; //Whether OreGin is currently mining
	private boolean broken; //Whether OreGin is currently broken
	private int miningDistance; //The current mining distance achieved by OreGin
	private int miningTimer; //The current time elapsed since last mining operation
	private Location oreGinLocation; //Current OreGin location
	
	private OreGinProperties oreGinProperties; //The properties for this OreGin's tier level
	
	/*
	 ----------OREGIN CONSTRUCTORS--------
	 */
	
	/**
	 * Constructor (Loading OreGins From File)
	 */
	public OreGin(int blockBreaks, int tierLevel, boolean mining, boolean broken, int miningDistance, Location oreGinLocation)
	{
		SetDefaultValues();
		this.blockBreaks = blockBreaks;
		this.tierLevel = tierLevel;
		this.miningDistance = miningDistance;
		this.mining = mining;
		this.oreGinLocation = oreGinLocation;
		this.broken = broken;
		UpdateOreGinProperties();
	}
	
	/**
	 * Constructor (Creating Default OreGn)
	 */
	public OreGin(Location oreGinLocation)
	{
		SetDefaultValues();
		this.oreGinLocation = oreGinLocation;
		UpdateOreGinProperties();
	}
	
	/**
	 * Constructor (Creating OreGin From Block Placement)
	 */
	public OreGin(Location oreGinLocation, int tierLevel, int blockBreaks)
	{
		SetDefaultValues();
		this.blockBreaks = blockBreaks;
		this.tierLevel = tierLevel;
		this.oreGinLocation = oreGinLocation;
		UpdateOreGinProperties();
	}
	
	/*
	 ----------OREGIN MAINTENANCE LOGIC--------
	 */
	
	/**
	* Updates the OreGin
	*/
	public void Update()
	{
		if (!broken) 
		{
			if (mining)
			{
				TurnOnLight();
				if (FuelAvailable())
				{
					miningTimer += OreGinPlugin.UPDATE_CYCLE;
					if (miningTimer >= oreGinProperties.GetMiningDelay())
					{
						miningTimer = 0;
						MineForward();
					}
				}
				else //OreGin is mining but doesn't have enough fuel
				{
					PowerOffOreGin();
				}	
			}
			else //OreGin is not currently activated/mining
			{
				TurnOffLight();
			}
		}
		else //OreGin is broken
		{
			ToggleLight();
		}

	}

	/**
	 * Breaks the OreGin
	 */
	public void BreakOreGin()
	{
		PowerOffOreGin();
		broken = true;
	}
	
	/**
	 * Updates ore gin properties
	 */
	public void UpdateOreGinProperties()
	{
		oreGinProperties = OreGinPlugin.Ore_Gin_Properties.get(tierLevel);
	}
		

	/**
	 * Destroys the OreGin
	 */
	public void DestroyOreGin(ItemStack item)
	{
		SetItemMeta(item);
		oreGinLocation.getWorld().dropItemNaturally(oreGinLocation, item);
		if (oreGinLocation.getBlock().getRelative(BlockFace.UP,1).getType().equals(OreGinPlugin.LIGHT_ON) ||
				oreGinLocation.getBlock().getRelative(BlockFace.UP,1).getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			oreGinLocation.getBlock().getRelative(BlockFace.UP,1).setType(Material.AIR);
		}
	}
	
	/**
	 * Sets the correct name and lore for the item
	 */
	public void SetItemMeta(ItemStack item)
	{
		ItemMeta meta = item.getItemMeta();
		
		String name = "T" + tierLevel + " OreGin";
		meta.setDisplayName(name);
		
		ChatColor colorCode;
		String status;
		
		if (blockBreaks >= oreGinProperties.GetMaxBlockBreaks())
		{
			colorCode = ChatColor.RED;
			status = "Broken";
		}
		else if (blockBreaks >= oreGinProperties.GetMaxBlockBreaks()/2)
		{
			colorCode = ChatColor.YELLOW;
			status = "Working";
		}
		else
		{
			colorCode = ChatColor.GREEN;
			status = "Working";
		}
		
		List<String> lore = new ArrayList<String>();
		lore.add(colorCode + "Status: " + status + " (" + blockBreaks + ")");
		
		meta.setLore(lore);
		
		item.setItemMeta(meta);
	}

	/**
	 * Sets the default values for OreGin fields
	 */
	public void SetDefaultValues()
	{
		blockBreaks = 0;
		tierLevel = 1;
		miningDistance = 0;
		mining = false;
		miningTimer = 0;
		oreGinLocation = null;
		broken = false;
		UpdateOreGinProperties();
	}
	
	/*
	 ----------MINING LOGIC--------
	 */
	
	/**
	 * Mines forward in the particular schematic of the OreGin
	 */
	public void MineForward()
	{
		if (miningDistance < oreGinProperties.GetMaxMiningDistance())
		{
			if (blockBreaks < oreGinProperties.GetMaxBlockBreaks())
			{
				Vector miningOffset = MiningOffset();
				Location startingPos = oreGinLocation.getBlock().getLocation().add(miningOffset);
				
				Block machineBlock = oreGinLocation.getBlock();
				BlockFace facing = GetDirection(machineBlock.getState().getRawData());

				for (int x = 0; x < Math.max(1, Math.abs(miningOffset.getX()*2)); x++) 
				{ 
					for (int y = 0; y < Math.max(1,  Math.abs(miningOffset.getY()*2)); y++)
					{
						for (int z = 0; z < Math.max(1,  Math.abs(miningOffset.getZ()*2)); z++)
						{
							if (MineBlock(startingPos.getBlock().getLocation().add(BlockOffset(x,y,z)).getBlock().getRelative(facing, miningDistance + 1)))
							{
								blockBreaks++;
							}
						}
					}
				}
				
				if (RemoveFuel())
				{
					miningDistance++;
				}
			}
			else
			{
				BreakOreGin();
			}		
		}
		else
		{
			PowerOffOreGin();
			miningDistance = 0;
		}
	}
	
	/**
	 * Mines the block given based on the properties of this OreGin
	 */
	public boolean MineBlock(Block block)
	{
		Material blockType = block.getType();
		
		if ( (blockType.equals(Material.WATER) && !OreGinPlugin.WATER_MINING_ENABLED) 
			|| (blockType.equals(Material.LAVA)&& !OreGinPlugin.LAVA_MINING_ENABLED) )
		{
			return false;
		}
		else
		{
			if (OreGinPlugin.VALUABLES.contains(blockType) && oreGinProperties.GetRetrieveValuables())
			{
				Collection<ItemStack> drops = block.getDrops();
					
				for (ItemStack item : drops)
				{
					HashMap<Integer,ItemStack> leftOvers = AddMaterial(item);
					
					for (Entry<Integer,ItemStack> entry : leftOvers.entrySet())
					{
						block.getWorld().dropItemNaturally(this.oreGinLocation, entry.getValue());
					}
				}
				
				block.setType(Material.AIR);
			}
			else if (OreGinPlugin.JUNK_DESTRUCTION_ENABLED && OreGinPlugin.JUNK.contains(blockType))
			{
				block.setType(Material.AIR);
			}
			else
			{
				block.breakNaturally();
			}
			return true;
		}
	}
	
	/**
	 * Block Offset for mining
	 */
	public Vector BlockOffset(double x, double y, double z)
	{
		BlockFace facing = GetDirection(oreGinLocation.getBlock().getState().getRawData());
		
		if (facing.equals(BlockFace.SOUTH))
		{
			x = -x;
		}
		else if (facing.equals(BlockFace.WEST))
		{
			z = -z;
		}
		
		return new Vector(x, y, z);
	}

	/**
	 * Offset for the starting mining position
	 */
	public Vector MiningOffset()
	{
		BlockFace facing = GetDirection(oreGinLocation.getBlock().getState().getRawData());
		
		Vector miningOffset = new Vector(0, 0, 0);
		
		//Determines the Z or X offset
		if (oreGinProperties.GetShaftWidth() > 1)
		{
			if (facing.equals(BlockFace.NORTH))
			{
					miningOffset.setX((double)-oreGinProperties.GetShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.SOUTH))
			{
					miningOffset.setX((double)oreGinProperties.GetShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.EAST))
			{
					miningOffset.setZ((double)-oreGinProperties.GetShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.WEST))
			{
					miningOffset.setZ((double)oreGinProperties.GetShaftWidth() / 2);
			}
		}
		
		//Determines the Y offset
		if (oreGinProperties.GetShaftHeight() > 1)
		{
			miningOffset.setY((int)-oreGinProperties.GetShaftHeight() / 2);
		}
		
		return miningOffset;
	}
	
	/*
	 ----------POWER LOGIC--------
	 */
	
	/**
	 * Shuts down the Ore Gin from mining.
	 */
	public void PowerOffOreGin()
	{
		miningTimer = 0;
		mining = false;
		TurnOffLight();
	}
	
	/**
	 * Turns on the OreGin
	 */
	public void PowerOnOreGin()
	{
		mining = true;
		miningTimer = 0;
		TurnOnLight();
	}
	
	/**
	 * Toggles the OreGin
	 */
	public String TogglePower()
	{
		if (!mining && FuelAvailable())
		{
			PowerOnOreGin();
			return ChatColor.GREEN + "OreGin activated!";
		}
		else
		{
			PowerOffOreGin();
			return ChatColor.RED + "OreGin deactivated!";
		}
		
	}
	
	/*
	 ----------UPGRADE LOGIC--------
	 */
	
	/**
	 * Attempt to upgrade OreGin
	 */
	public String Upgrade()
	{
		//Add logic to determine whether upgrading the machine is possible
		int desiredTier = tierLevel + 1;
		OreGinProperties desiredTierProperties = OreGinPlugin.Ore_Gin_Properties.get(desiredTier);
		if (desiredTier <= OreGinPlugin.MAX_TIERS)
		{
			Material upgradeMaterial = desiredTierProperties.GetUpgradeMaterial();
			if (UpgradeMaterialAvailable(desiredTier))
			{
				RemoveUpgradeMaterial(desiredTier);
				tierLevel++;
				UpdateOreGinProperties();
				return ChatColor.GREEN + "OreGin successfully upgraded to tier " + tierLevel + "!";
			}
			else
			{
				return ChatColor.RED + "Missing upgrade materials! " + RequiredAvailableMaterials(desiredTierProperties.GetUpgradeAmount(),
						upgradeMaterial);	
			}
		}
		else
		{
			return ChatColor.RED + "OreGin is already max tier level!";
		}
	}
	
	/**
	 * Attempts to remove upgrade material from dispenser
	 */
	public boolean RemoveUpgradeMaterial(int desiredTier)
	{
		return RemoveMaterial(OreGinPlugin.Ore_Gin_Properties.get(desiredTier).GetUpgradeAmount(),
				OreGinPlugin.Ore_Gin_Properties.get(desiredTier).GetUpgradeMaterial(), this.oreGinLocation);
	}

	/**
	 * Returns whether the material for an upgrade is available
	 */
	public boolean UpgradeMaterialAvailable(int desiredTier)
	{
		return MaterialAvailable(OreGinPlugin.Ore_Gin_Properties.get(desiredTier).GetUpgradeAmount(),
				OreGinPlugin.Ore_Gin_Properties.get(desiredTier).GetUpgradeMaterial(), this.oreGinLocation);
	}
	
	/**
	 * Returns whether the material for an upgrade is available
	 */
	public static boolean UpgradeMaterialAvailable(int desiredTier, Location machineLocation)
	{
		return MaterialAvailable(OreGinPlugin.Ore_Gin_Properties.get(desiredTier).GetUpgradeAmount(),
				OreGinPlugin.Ore_Gin_Properties.get(desiredTier).GetUpgradeMaterial(), machineLocation);
	}
	
	/*
	 ----------REPAIR LOGIC--------
	 */
	
	/**
	 * Attempts to repair
	 */
	public String Repair()
	{
		if (broken)
		{
			if (RepairMaterialAvailable())
			{
				RemoveRepairMaterials();
				broken = false;
				blockBreaks = 0;
				return ChatColor.GREEN + "OreGin has been successfully repaired!";
			}
			else
			{
				return ChatColor.RED + "Missing repair materials! " + RequiredAvailableMaterials(oreGinProperties.GetRepairAmount(),
																		oreGinProperties.GetRepairMaterial());
			}
		}
		else
		{
			return ChatColor.RED + "The OreGin is not broken!";
		}
	}
	
	/**
	 * Returns whether there is material available for repairing
	 */
	public boolean RepairMaterialAvailable()
	{
		return MaterialAvailable(oreGinProperties.GetRepairAmount(), oreGinProperties.GetRepairMaterial(), this.oreGinLocation);
	}
	
	/**
	 * Attempts to remove materials for repair
	 */
	public boolean RemoveRepairMaterials()
	{
		return RemoveMaterial(oreGinProperties.GetRepairAmount(), oreGinProperties.GetRepairMaterial(), this.oreGinLocation);
	}
		
	/*
	 ----------FUEL LOGIC--------
	 */
	
	/**
	 * Returns whether there is enough fuel available for one mining operation
	 */
	public boolean FuelAvailable()
	{
		return MaterialAvailable(oreGinProperties.GetFuelAmount(), oreGinProperties.GetFuelMaterial(), this.oreGinLocation);
	}
	
	/**
	 * Removes a certain amount of fuel
	 */
	public boolean RemoveFuel()
	{		
		return RemoveMaterial(oreGinProperties.GetFuelAmount(), oreGinProperties.GetFuelMaterial(), this.oreGinLocation);
	}

	/*
	 ----------OREGIN LIGHT LOGIC--------
	 */
	
	/**
	 * Turns off the OreGin light
	 */
	public void TurnOffLight()
	{
		if (!oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).setType(OreGinPlugin.LIGHT_OFF);
		}
	}
	
	/**
	 * Turns on the OreGin light
	 */
	public void TurnOnLight()
	{
		if (!oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(OreGinPlugin.LIGHT_ON))
		{
			oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).setType(OreGinPlugin.LIGHT_ON);
		}
	}
	
	/**
	 * Toggles the OreGin light
	 */
	public void ToggleLight()
	{
		if (oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			TurnOnLight();
		}
		else
		{
			TurnOffLight();
		}
	}
	
	/*
	 ----------DISPENSER INVENTORY LOGIC--------
	 */
	
	/**
	 * Attempts to remove a specific material of given amount from dispenser inventory
	 */
	public boolean RemoveMaterial(int amount, Material material)
	{
		return RemoveMaterial(amount, material, this.oreGinLocation);
	}
	
	/**
	 * Checks if a specific material of given amount is available in dispenser inventory
	 */
	public boolean MaterialAvailable(int amount, Material material)
	{
		return MaterialAvailable(amount, material, this.oreGinLocation);
	}
	
	/**
	 * Returns how much of a specified material is available in dispenser inventory
	 */
	public int MaterialAvailableAmount(Material material)
	{
		return MaterialAvailableAmount(material, this.oreGinLocation);
	}
	
	/**
	 * Returns the "Required: (X MATERIAL) Available: (Y MATERIAL)" message
	 */
	public String RequiredAvailableMaterials(int amount, Material material)
	{
		return "Required: (" + amount + " " + material.toString() + ") Available: ("
				+ OreGin.MaterialAvailableAmount(material, oreGinLocation) + " " + material.toString() + ")";
	}
	
	/**
     * Adds a specific material to dispenser and returns left overs
	 */
	public HashMap<Integer,ItemStack> AddMaterial(ItemStack item)
	{
		return AddMaterial(item.getAmount(), item.getType(), this.oreGinLocation);
	}
	
	/*
	 ----------PUBLIC ACCESSORS--------
	* */
	
	/**
	 * 'miningDistance' public accessor
	 */
	public int GetMiningDistance()
	{
		return miningDistance;
	}
	
	/**
	 * 'blockBreaks' public accessor
	 */
	public int GetBlockBreaks()
	{
		return blockBreaks;
	}
	
	/**
	 * 'broken' public accessor
	 */
	public boolean GetBroken()
	{
		return broken;
	}
	
	/**
	 * 'mining' public accessor
	 */
	public boolean GetMining()
	{
		return mining;
	}
	
	/**
	 * 'machineLocation' public accessor
	 */
	public Location GetLocation()
	{
		return oreGinLocation;
	}
	
	/**
	 * 'tierLevel' public accessor
	 */
	public int GetTierLevel()
	{
		return tierLevel;
	}
	
	/*
	 ----------STATIC METHODS--------
	* */
	
	/**
	 * Adds a specific material to a given dispenser and returns left overs
	 */
	public static HashMap<Integer,ItemStack> AddMaterial(int amount, Material material, Location machineLocation)
	{
		if (machineLocation.getBlock().getType() == Material.DISPENSER)
		{
			Dispenser dispenserBlock = (Dispenser)machineLocation.getBlock().getState();
			Inventory dispenserInventory = dispenserBlock.getInventory();
			
			HashMap<Integer,ItemStack> leftOvers = dispenserInventory.addItem(new ItemStack(material, amount));
			return leftOvers;
		}

		return null;
	}

	/**
	 * Attempts to remove a specific material of given amount from dispenser
	 */
	public static boolean RemoveMaterial(int amount, Material material, Location machineLocation)
	{
		if (machineLocation.getBlock().getType() == Material.DISPENSER)
		{
			Dispenser dispenserBlock = (Dispenser)machineLocation.getBlock().getState();
			Inventory dispenserInventory = dispenserBlock.getInventory();
			
			HashMap<Integer,? extends ItemStack> upgradeMaterials = dispenserInventory.all(material);
			
			int materialsToRemove = amount;
			for(Entry<Integer,? extends ItemStack> entry : upgradeMaterials.entrySet())
			{
				if (materialsToRemove <= 0)
					break;
				
				if(entry.getValue().getAmount() >= materialsToRemove)
				{
					dispenserInventory.setItem(entry.getKey(), new ItemStack(entry.getValue().getType(), (entry.getValue().getAmount() - materialsToRemove)));
					materialsToRemove = 0;
				}
				else
				{
					int inStack = entry.getValue().getAmount();
					dispenserInventory.setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
					materialsToRemove -= inStack;
				}
			}
			
			return materialsToRemove == 0;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Checks if a specific material of given amount is available in dispenser
	 */
	public static boolean MaterialAvailable(int amount, Material material, Location machineLocation)
	{
		if (machineLocation.getBlock().getType() == Material.DISPENSER)
		{
			Dispenser dispenserBlock = (Dispenser)((BlockState)machineLocation.getBlock().getState());
			Inventory dispenserInventory = dispenserBlock.getInventory();
			
			HashMap<Integer,? extends ItemStack> upgradeMaterials = dispenserInventory.all(material);
			
			int totalMaterial = 0;
			for(Entry<Integer,? extends ItemStack> entry : upgradeMaterials.entrySet())
			{
				totalMaterial += entry.getValue().getAmount();
			}
			
			return (totalMaterial >= amount);
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Returns the "Required: (X MATERIAL) Available: (Y MATERIAL)" message
	 */
	public static String RequiredAvailableMaterials(int amount, Material material, Location machineLocation)
	{
		return "Required: (" + amount + " " + material.toString() + ") Available: ("
				+ OreGin.MaterialAvailableAmount(material, machineLocation) + " " + material.toString() + ")";
	}
	
	/**
	 * Returns how much of a specified material is available in dispenser
	 */
	public static int MaterialAvailableAmount(Material material, Location machineLocation)
	{
		if (machineLocation.getBlock().getType() == Material.DISPENSER)
		{
			Dispenser dispenserBlock = (Dispenser)((BlockState)machineLocation.getBlock().getState());
			Inventory dispenserInventory = dispenserBlock.getInventory();
			
			HashMap<Integer,? extends ItemStack> upgradeMaterials = dispenserInventory.all(material);
			
			int totalMaterial = 0;
			for(Entry<Integer,? extends ItemStack> entry : upgradeMaterials.entrySet())
			{
				totalMaterial += entry.getValue().getAmount();
			}
			
			return totalMaterial;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Whether OreGin is upgrade-able
	 */
	public static boolean ValidUpgrade(Location machineLocation, int desiredTier)
	{
		return (desiredTier <= OreGinPlugin.MAX_TIERS) && UpgradeMaterialAvailable(desiredTier, machineLocation);
	}
	
	/**
	 * Returns the tier level of the item
	 */
	public static int GetTierLevel(String name)
	{
		int tierLevel = 0;
			
		for (int i = 0; i < OreGinPlugin.MAX_TIERS; i++)
		{
			if (name.contains(Integer.toString(i)))
				tierLevel = i;
		}
			
		return tierLevel;
	}
		
	/**
	 * Converts byte direction into BlockFace direction.
	 */
	public static BlockFace GetDirection(byte direction)
	{
		if (direction== 0x02)
		{
			return BlockFace.NORTH;
		}
		else if (direction== 0x03)
		{
			return BlockFace.SOUTH;
		}
		else if (direction== 0x04)
		{
			return BlockFace.WEST;
		}
		else if (direction== 0x05)
		{
			return BlockFace.EAST;
		}
		else
			return null;
			
	}
	
	/**
	 * Grabs the block break number from lore
	 */
	public static int GetBlockBreaksFromLore(List<String> lore)
	{
		int blockBreaks;
		
		if (lore == null)
			return 0;
		
		String loreLine = lore.get(0);
		
		int startIndex = -1,endIndex = -1;
		
		for (int i = 0; i < loreLine.length(); i++)
		{
			boolean isInt = true;
			try
			{
				Integer.parseInt(loreLine.substring(i,i+1));
			}
			catch (NumberFormatException exc)
			{
				isInt = false;
			}
			
			if (isInt)
			{
				if (startIndex == -1)
				{
					startIndex = i;
				}
			}
			else
			{
				if (startIndex != -1 && endIndex == -1)
				{
					endIndex = i;
					break;
				}
			}
		}
		
		blockBreaks = Integer.parseInt(loreLine.substring(startIndex,endIndex));
		
		return blockBreaks;
	}
	
}