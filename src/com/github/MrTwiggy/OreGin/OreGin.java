package com.github.MrTwiggy.OreGin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.lang.Math;

import static com.untamedears.citadel.Utility.isReinforced;

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
	
	private int blockPower; //Current block power of dispenser
	
	private OreGinManager oreGinMan; //An instance of the OreGin manager
	
	private OreGinProperties oreGinProperties; //The properties for this OreGin's tier level
	
	/*
	 ----------OREGIN CONSTRUCTORS--------
	 */
	
	/**
	 * Constructor (Loading OreGins From File)
	 */
	public OreGin(int blockBreaks, int tierLevel, boolean mining, boolean broken, int miningDistance, Location oreGinLocation,
						OreGinManager oreGinMan)
	{
		setDefaultValues();
		this.blockBreaks = blockBreaks;
		this.tierLevel = tierLevel;
		this.miningDistance = miningDistance;
		this.mining = mining;
		this.oreGinLocation = oreGinLocation;
		this.broken = broken;
		this.oreGinMan = oreGinMan;
		updateOreGinProperties();
		blockPower = oreGinLocation.getBlock().getBlockPower();
	}
	
	/**
	 * Constructor (Creating Default OreGn)
	 */
	public OreGin(Location oreGinLocation, OreGinManager oreGinMan)
	{
		setDefaultValues();
		this.oreGinLocation = oreGinLocation;
		this.oreGinMan = oreGinMan;
		OreGinSoundCollection.getCreationSound().playSound(oreGinLocation);
		updateOreGinProperties();
		blockPower = oreGinLocation.getBlock().getBlockPower();
	}
	
	/**
	 * Constructor (Creating OreGin From Block Placement)
	 */
	public OreGin(Location oreGinLocation, int tierLevel, int blockBreaks, OreGinManager oreGinMan)
	{
		setDefaultValues();
		this.blockBreaks = blockBreaks;
		this.tierLevel = tierLevel;
		this.oreGinLocation = oreGinLocation;
		this.oreGinMan = oreGinMan;
		OreGinSoundCollection.getPlacementSound().playSound(oreGinLocation);
		updateOreGinProperties();
		blockPower = oreGinLocation.getBlock().getBlockPower();
	}
	
	/*
	 ----------OREGIN MAINTENANCE LOGIC--------
	 */
	
	/**
	* Updates the OreGin
	*/
	public void update()
	{
		if (!broken) 
		{
			updateRedstoneActivation();
			
			if (mining)
			{
				turnOnLight();
				if (isFuelAvailable())
				{
					miningTimer += OreGinPlugin.UPDATE_CYCLE;
					if (miningTimer >= oreGinProperties.getMiningDelay())
					{
						miningTimer = 0;
						if ((oreGinMan.getBlockBreaksDuringCycle() 
							+ (oreGinProperties.getShaftHeight()*oreGinProperties.getShaftWidth())) < OreGinPlugin.MAXIMUM_BLOCK_BREAKS_PER_CYCLE)
						{
							mineForward();
						}
						else
						{
							OreGinPlugin.sendConsoleMessage("Maximum block breaks have been reached! " 
									+ oreGinMan.getBlockBreaksDuringCycle() + " out of " + OreGinPlugin.MAXIMUM_BLOCK_BREAKS_PER_CYCLE);
						}
					}
				}
				else //OreGin is mining but doesn't have enough fuel
				{
					powerOffOreGin();
				}	
			}
			else //OreGin is not currently activated/mining
			{
				turnOffLight();
			}
		}
		else //OreGin is broken
		{
			toggleLight();
			OreGinSoundCollection.getBrokenSound().playSound(oreGinLocation);
		}

	}
	
	/**
	 * Updates the block power and checks for redstone activation/deactivation
	 */
	public void updateRedstoneActivation()
	{
		if (OreGinPlugin.REDSTONE_ACTIVATION_ENABLED)
		{
			int newBlockPower = oreGinLocation.getBlock().getBlockPower();
			
			if (newBlockPower != blockPower)
			{
				if (blockPower == 0) //OreGin has been activated
				{
					if (!mining)
					{
						powerOnOreGin();
					}
				}
				else if (newBlockPower == 0) //OreGin has been deactivated
				{
					if (mining)
					{
						powerOffOreGin();
					}
				}
			}
			
			blockPower = newBlockPower;
		}
	}

	/**
	 * Breaks the OreGin
	 */
	public void breakOreGin()
	{
		powerOffOreGin();
		broken = true;
	}
	
	/**
	 * Updates ore gin properties
	 */
	public void updateOreGinProperties()
	{
		oreGinProperties = OreGinPlugin.Ore_Gin_Properties.get(tierLevel);
	}
		
	/**
	 * Destroys the OreGin
	 */
	public void destroyOreGin(ItemStack item)
	{
		setItemMeta(item);
		oreGinLocation.getWorld().dropItemNaturally(oreGinLocation, item);
		if (oreGinLocation.getBlock().getRelative(BlockFace.UP,1).getType().equals(OreGinPlugin.LIGHT_ON) ||
				oreGinLocation.getBlock().getRelative(BlockFace.UP,1).getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			oreGinLocation.getBlock().getRelative(BlockFace.UP,1).setType(Material.AIR);
		}
		oreGinLocation.getBlock().setType(Material.AIR);
		OreGinSoundCollection.getDestructionSound().playSound(oreGinLocation);
	}
	
	/**
	 * Sets the correct name and lore for the item
	 */
	public void setItemMeta(ItemStack item)
	{
		ItemMeta meta = item.getItemMeta();
		
		String name = "T" + tierLevel + " OreGin";
		meta.setDisplayName(name);
		
		ChatColor colorCode;
		String status;
		
		if (blockBreaks >= oreGinProperties.getMaxBlockBreaks())
		{
			colorCode = ChatColor.RED;
			status = "Broken";
		}
		else if (blockBreaks >= oreGinProperties.getMaxBlockBreaks()/2)
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
	public void setDefaultValues()
	{
		blockBreaks = 0;
		tierLevel = 1;
		miningDistance = 0;
		mining = false;
		miningTimer = 0;
		oreGinLocation = null;
		broken = false;
		updateOreGinProperties();
	}

	/*
	 ----------MINING LOGIC--------
	 */
	
	/**
	 * Mines forward in the particular schematic of the OreGin
	 */
	public void mineForward()
	{
		if (miningDistance < oreGinProperties.getMaxMiningDistance())
		{
			if (blockBreaks < oreGinProperties.getMaxBlockBreaks())
			{
				Vector miningOffset = getMiningOffset();
				Location startingPos = oreGinLocation.getBlock().getLocation().add(
						(int)miningOffset.getX(), (int)miningOffset.getY(), (int)miningOffset.getZ());

				Block machineBlock = oreGinLocation.getBlock();
				BlockFace facing = getDirection(machineBlock.getState().getRawData());
				  
				for (int x = 0; x < Math.max(1, Math.abs(miningOffset.getX()*2)); x++) 
				{ 
					for (int y = 0; y < Math.max(1,  Math.abs(miningOffset.getY()*2)); y++)
					{
						for (int z = 0; z < Math.max(1,  Math.abs(miningOffset.getZ()*2)); z++)
						{
							if (mineBlock(startingPos.getBlock().getLocation().add(getBlockOffset(x,y,z)).getBlock().getRelative(facing, miningDistance + 1)))
							{
								blockBreaks++;
								oreGinMan.incrementBlockBreaksDuringCycle();
							}
						}
					}
				}
				
				if (removeFuel())
				{
					miningDistance++;
					OreGinSoundCollection.getMiningSound().playSound(oreGinLocation);
				}
			}
			else
			{
				breakOreGin();
			}		
		}
		else
		{
			powerOffOreGin();
			miningDistance = 0;
		}
	}
	
	/**
	 * Mines the block given based on the properties of this OreGin
	 */
	public boolean mineBlock(Block block)
	{
		Material blockType = block.getType();
		
		if ( oreGinMan.oreGinExistsAt(block.getLocation()) || oreGinMan.oreGinLightExistsAt(block.getLocation()))
		{
			return false;
		}
		else if ( (blockType.equals(Material.WATER) && !OreGinPlugin.WATER_MINING_ENABLED) 
			   || (blockType.equals(Material.LAVA) && !OreGinPlugin.LAVA_MINING_ENABLED) )
		{
			return false;
		}
		else if (OreGinPlugin.INDESTRUCTIBLE.contains(blockType))
		{
			return false;
		}
		else
		{
			if ((OreGinPlugin.CITADEL_ENABLED && !isReinforced(block))
					|| !OreGinPlugin.CITADEL_ENABLED)
			{
				if (oreGinProperties.getRetrieveValuables() && OreGinPlugin.VALUABLES.contains(blockType))
				{
					Collection<ItemStack> drops = block.getDrops();
						
					for (ItemStack item : drops)
					{
						HashMap<Integer,ItemStack> leftOvers = addMaterial(item);
						
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
			else
			{
				return false;
			}
		}
	}
	
	/**
	 * Block Offset for mining
	 */
	public Vector getBlockOffset(double x, double y, double z)
	{
		BlockFace facing = getDirection(oreGinLocation.getBlock().getState().getRawData());
		
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
	public Vector getMiningOffset()
	{
		BlockFace facing = getDirection(oreGinLocation.getBlock().getState().getRawData());
		
		Vector miningOffset = new Vector(0, 0, 0);
		
		//Determines the Z or X offset
		if (oreGinProperties.getShaftWidth() > 1)
		{
			if (facing.equals(BlockFace.NORTH))
			{
					miningOffset.setX((double)-oreGinProperties.getShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.SOUTH))
			{
					miningOffset.setX((double)oreGinProperties.getShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.EAST))
			{
					miningOffset.setZ((double)-oreGinProperties.getShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.WEST))
			{
					miningOffset.setZ((double)oreGinProperties.getShaftWidth() / 2);
			}
		}
		
		//Determines the Y offset
		if (oreGinProperties.getShaftHeight() > 1)
		{
			miningOffset.setY((double)-oreGinProperties.getShaftHeight() / 2);
		}
		
		return miningOffset;
	}
	
	/*
	 ----------POWER LOGIC--------
	 */
	
	/**
	 * Shuts down the Ore Gin from mining.
	 */
	public void powerOffOreGin()
	{
		miningTimer = 0;
		mining = false;
		turnOffLight();
		OreGinSoundCollection.getPowerOffSound().playSound(oreGinLocation);
	}
	
	/**
	 * Turns on the OreGin
	 */
	public void powerOnOreGin()
	{
		if (isFuelAvailable())
		{
			mining = true;
			miningTimer = 0;
			turnOnLight();
			OreGinSoundCollection.getPowerOnSound().playSound(oreGinLocation);
		}
		else
		{
			OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
		}
	}
	
	/**
	 * Toggles the OreGin
	 */
	public String togglePower()
	{
		if (!mining)
		{
			if (!broken)
			{
				if (isFuelAvailable())
				{
					powerOnOreGin();
					return ChatColor.GREEN + "OreGin activated!";
				}
				else
				{
					OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
					return ChatColor.RED + "Missing fuel! " + requiredAvailableMaterials(oreGinProperties.getFuelAmount(),
																			oreGinProperties.getFuelMaterial());
				}
			}
			else
			{
				OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
				return ChatColor.RED + "OreGin is broken! You must repair it first!";
			}
		}
		else
		{
			powerOffOreGin();
			return ChatColor.RED + "OreGin deactivated!";
		}
		
	}
	
	/*
	 ----------UPGRADE LOGIC--------
	 */
	
	/**
	 * Attempt to upgrade OreGin
	 */
	public String upgrade()
	{
		//Add logic to determine whether upgrading the machine is possible
		int desiredTier = tierLevel + 1;
		OreGinProperties desiredTierProperties = OreGinPlugin.Ore_Gin_Properties.get(desiredTier);
		if (desiredTier <= OreGinPlugin.MAX_TIERS)
		{
			Material upgradeMaterial = desiredTierProperties.getUpgradeMaterial();
			if (upgradeMaterialAvailable(desiredTier))
			{
				removeUpgradeMaterial(desiredTier);
				tierLevel++;
				updateOreGinProperties();
				OreGinSoundCollection.getUpgradeSound().playSound(oreGinLocation);
				return ChatColor.GREEN + "OreGin successfully upgraded to tier " + tierLevel + "!";
			}
			else
			{
				OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
				return ChatColor.RED + "Missing upgrade materials! " + requiredAvailableMaterials(desiredTierProperties.getUpgradeAmount(),
						upgradeMaterial);	
			}
		}
		else
		{
			OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
			return ChatColor.RED + "OreGin is already max tier level!";
		}
	}
	
	/**
	 * Attempts to remove upgrade material from dispenser
	 */
	public boolean removeUpgradeMaterial(int desiredTier)
	{
		return removeMaterial(OreGinPlugin.Ore_Gin_Properties.get(desiredTier).getUpgradeAmount(),
				OreGinPlugin.Ore_Gin_Properties.get(desiredTier).getUpgradeMaterial(), this.oreGinLocation);
	}

	/**
	 * Returns whether the material for an upgrade is available
	 */
	public boolean upgradeMaterialAvailable(int desiredTier)
	{
		return isMaterialAvailable(OreGinPlugin.Ore_Gin_Properties.get(desiredTier).getUpgradeAmount(),
				OreGinPlugin.Ore_Gin_Properties.get(desiredTier).getUpgradeMaterial(), this.oreGinLocation);
	}
	
	/**
	 * Returns whether the material for an upgrade is available
	 */
	public static boolean isUpgradeMaterialAvailable(int desiredTier, Location machineLocation)
	{
		return isMaterialAvailable(OreGinPlugin.Ore_Gin_Properties.get(desiredTier).getUpgradeAmount(),
				OreGinPlugin.Ore_Gin_Properties.get(desiredTier).getUpgradeMaterial(), machineLocation);
	}
	
	/*
	 ----------REPAIR LOGIC--------
	 */
	
	/**
	 * Attempts to repair
	 */
	public String repair()
	{
		if (broken)
		{
			if (isRepairMaterialAvailable())
			{
				removeRepairMaterials();
				broken = false;
				blockBreaks = 0;
				OreGinSoundCollection.getRepairSound().playSound(oreGinLocation);
				return ChatColor.GREEN + "OreGin has been successfully repaired!";
			}
			else
			{
				OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
				return ChatColor.RED + "Missing repair materials! " + requiredAvailableMaterials(oreGinProperties.getRepairAmount(),
																		oreGinProperties.getRepairMaterial());
			}
		}
		else
		{
			OreGinSoundCollection.getErrorSound().playSound(oreGinLocation);
			return ChatColor.RED + "The OreGin is not broken!";
		}
	}
	
	/**
	 * Returns whether there is material available for repairing
	 */
	public boolean isRepairMaterialAvailable()
	{
		return isMaterialAvailable(oreGinProperties.getRepairAmount(), oreGinProperties.getRepairMaterial(), this.oreGinLocation);
	}
	
	/**
	 * Attempts to remove materials for repair
	 */
	public boolean removeRepairMaterials()
	{
		return removeMaterial(oreGinProperties.getRepairAmount(), oreGinProperties.getRepairMaterial(), this.oreGinLocation);
	}
		
	/*
	 ----------FUEL LOGIC--------
	 */
	
	/**
	 * Returns whether there is enough fuel available for one mining operation
	 */
	public boolean isFuelAvailable()
	{
		return isMaterialAvailable(oreGinProperties.getFuelAmount(), oreGinProperties.getFuelMaterial(), this.oreGinLocation);
	}
	
	/**
	 * Removes a certain amount of fuel
	 */
	public boolean removeFuel()
	{		
		return removeMaterial(oreGinProperties.getFuelAmount(), oreGinProperties.getFuelMaterial(), this.oreGinLocation);
	}

	/*
	 ----------OREGIN LIGHT LOGIC--------
	 */
	
	/**
	 * Turns off the OreGin light
	 */
	public void turnOffLight()
	{
		if (!oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).setType(OreGinPlugin.LIGHT_OFF);
		}
	}
	
	/**
	 * Turns on the OreGin light
	 */
	public void turnOnLight()
	{
		if (!oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(OreGinPlugin.LIGHT_ON))
		{
			oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).setType(OreGinPlugin.LIGHT_ON);
		}
	}
	
	/**
	 * Toggles the OreGin light
	 */
	public void toggleLight()
	{
		if (oreGinLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			turnOnLight();
		}
		else
		{
			turnOffLight();
		}
	}
	
	/*
	 ----------DISPENSER INVENTORY LOGIC--------
	 */
	
	/**
	 * Attempts to remove a specific material of given amount from dispenser inventory
	 */
	public boolean removeMaterial(int amount, Material material)
	{
		return removeMaterial(amount, material, this.oreGinLocation);
	}
	
	/**
	 * Checks if a specific material of given amount is available in dispenser inventory
	 */
	public boolean isMaterialAvailable(int amount, Material material)
	{
		return isMaterialAvailable(amount, material, this.oreGinLocation);
	}
	
	/**
	 * Returns how much of a specified material is available in dispenser inventory
	 */
	public int getMaterialAvailableAmount(Material material)
	{
		return getMaterialAvailableAmount(material, this.oreGinLocation);
	}
	
	/**
	 * Returns the "Required: (X MATERIAL) Available: (Y MATERIAL)" message
	 */
	public String requiredAvailableMaterials(int amount, Material material)
	{
		return "Required: (" + amount + " " + material.toString() + ") Available: ("
				+ OreGin.getMaterialAvailableAmount(material, oreGinLocation) + " " + material.toString() + ")";
	}
	
	/**
     * Adds a specific material to dispenser and returns left overs
	 */
	public HashMap<Integer,ItemStack> addMaterial(ItemStack item)
	{
		return addMaterial(item.getAmount(), item.getType(), this.oreGinLocation);
	}
	
	/*
	 ----------PUBLIC ACCESSORS--------
	* */
	
	/**
	 * 'miningDistance' public accessor
	 */
	public int getMiningDistance()
	{
		return miningDistance;
	}
	
	/**
	 * 'blockBreaks' public accessor
	 */
	public int getBlockBreaks()
	{
		return blockBreaks;
	}
	
	/**
	 * 'broken' public accessor
	 */
	public boolean getBroken()
	{
		return broken;
	}
	
	/**
	 * 'mining' public accessor
	 */
	public boolean getMining()
	{
		return mining;
	}
	
	/**
	 * 'machineLocation' public accessor
	 */
	public Location getLocation()
	{
		return oreGinLocation;
	}
	
	/**
	 * 'tierLevel' public accessor
	 */
	public int getTierLevel()
	{
		return tierLevel;
	}
	
	/*
	 ----------STATIC METHODS--------
	* */
	
	/**
	 * Adds a specific material to a given dispenser and returns left overs
	 */
	public static HashMap<Integer,ItemStack> addMaterial(int amount, Material material, Location machineLocation)
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
	public static boolean removeMaterial(int amount, Material material, Location machineLocation)
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
				
				if(entry.getValue().getAmount() == materialsToRemove)
				{
					dispenserInventory.setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
					materialsToRemove = 0;
				}
				else if(entry.getValue().getAmount() > materialsToRemove)
				{
					dispenserInventory.setItem(entry.getKey(), new ItemStack(material, (entry.getValue().getAmount() - materialsToRemove)));
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
	public static boolean isMaterialAvailable(int amount, Material material, Location machineLocation)
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
	public static String getRequiredAvailableMaterials(int amount, Material material, Location machineLocation)
	{
		return "Required: (" + amount + " " + material.toString() + ") Available: ("
				+ OreGin.getMaterialAvailableAmount(material, machineLocation) + " " + material.toString() + ")";
	}
	
	/**
     * Returns how much of a specified material is available in dispenser
	 */
	public static int getMaterialAvailableAmount(Material material, Location machineLocation)
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
	public static boolean isValidUpgrade(Location machineLocation, int desiredTier)
	{
		return (desiredTier <= OreGinPlugin.MAX_TIERS) && isUpgradeMaterialAvailable(desiredTier, machineLocation);
	}
	
	/**
	 * Returns whether location is suitable for creating/placing an OreGin
	 */
	public static boolean isValidOreGinCreationLocation(Location machineLocation)
	{
		return (machineLocation.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR));
	}
	
	/**
	 * Returns the tier level of the item
	 */
	public static int getTierLevel(String name)
	{
		int tierLevel = 0;
			
		for (int i = 1; i <= OreGinPlugin.MAX_TIERS; i++)
		{
			if (name.contains(Integer.toString(i)))
				tierLevel = i;
		}
			
		return tierLevel;
	}
		
	/**
	 * Converts byte direction into BlockFace direction.
	 */
	public static BlockFace getDirection(byte direction)
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
	public static int getBlockBreaksFromLore(List<String> lore)
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