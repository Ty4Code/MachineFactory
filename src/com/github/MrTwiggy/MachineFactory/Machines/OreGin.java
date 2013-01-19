package com.github.MrTwiggy.MachineFactory.Machines;

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

import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
import com.github.MrTwiggy.MachineFactory.MachineObject;
import com.github.MrTwiggy.MachineFactory.Interfaces.Machine;
import com.github.MrTwiggy.MachineFactory.Managers.MachinesManager;
import com.github.MrTwiggy.MachineFactory.Managers.OreGinManager;
import com.github.MrTwiggy.MachineFactory.Properties.OreGinProperties;
import com.github.MrTwiggy.MachineFactory.SoundCollections.OreGinSoundCollection;
import com.github.MrTwiggy.MachineFactory.Utility.CitadelInteraction;
import com.github.MrTwiggy.MachineFactory.Utility.Dimensions;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse.InteractionResult;

/**
 * OreGin.java
 * Purpose: Functionality for OreGin objects
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGin extends MachineObject implements Machine
{

	private int blockBreaks; //Number of blocks broken by OreGin
	private boolean broken; //Whether OreGin is currently broken
	private int miningDistance; //The current mining distance achieved by OreGin
	private int miningTimer; //The current time elapsed since last mining operation
	
	public static final MachineType MACHINE_TYPE = MachineType.OREGIN; // The type this machine is
	
	private int blockPower; //Current block power of dispenser
	
	private OreGinManager oreGinMan; //An instance of the OreGin manager
	
	/*
	 ----------OREGIN CONSTRUCTORS--------
	 */
	
	/**
	 * Constructor (Loading OreGins From File)
	 */
	public OreGin(int blockBreaks, int tierLevel, boolean mining, boolean broken, int miningDistance, Location oreGinLocation,
						OreGinManager oreGinMan)
	{
		super(oreGinLocation, new Dimensions(1,1,1), mining, tierLevel, OreGin.MACHINE_TYPE);
		setDefaultValues();
		this.blockBreaks = blockBreaks;
		this.miningDistance = miningDistance;
		this.broken = broken;
		this.oreGinMan = oreGinMan;
		blockPower = oreGinLocation.getBlock().getBlockPower();
	}
	
	/**
	 * Constructor (Creating Default OreGn)
	 */
	public OreGin(Location oreGinLocation, OreGinManager oreGinMan)
	{
		super(oreGinLocation, new Dimensions(1,1,1), OreGin.MACHINE_TYPE);
		setDefaultValues();
		this.oreGinMan = oreGinMan;
		OreGinSoundCollection.getCreationSound().playSound(oreGinLocation);
		blockPower = oreGinLocation.getBlock().getBlockPower();
	}
	
	/**
	 * Constructor (Creating OreGin From Block Placement)
	 */
	public OreGin(Location oreGinLocation, int tierLevel, int blockBreaks, OreGinManager oreGinMan)
	{
		super(oreGinLocation, new Dimensions(1,1,1), OreGin.MACHINE_TYPE);
		setDefaultValues();
		this.blockBreaks = blockBreaks;
		this.tierLevel = tierLevel;
		this.oreGinMan = oreGinMan;
		OreGinSoundCollection.getPlacementSound().playSound(oreGinLocation);
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
			
			if (active)
			{
				turnOnLight();
				if (isFuelAvailable())
				{
					miningTimer += MachineFactoryPlugin.OREGIN_UPDATE_CYCLE;
					if (miningTimer >= getProperties().getMiningDelay())
					{
						miningTimer = 0;
						if ((oreGinMan.getBlockBreaksDuringCycle() 
							+ (getProperties().getShaftHeight()*getProperties().getShaftWidth())) < MachineFactoryPlugin.MAXIMUM_BLOCK_BREAKS_PER_CYCLE)
						{
							mineForward();
						}
						else
						{
							MachineFactoryPlugin.sendConsoleMessage("Maximum block breaks have been reached! " 
									+ oreGinMan.getBlockBreaksDuringCycle() + " out of " + MachineFactoryPlugin.MAXIMUM_BLOCK_BREAKS_PER_CYCLE);
						}
					}
				}
				else //OreGin is mining but doesn't have enough fuel
				{
					powerOff();
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
			OreGinSoundCollection.getBrokenSound().playSound(machineLocation);
		}

	}
	
	/**
	 * Updates the block power and checks for redstone activation/deactivation
	 */
	public void updateRedstoneActivation()
	{
		if (MachineFactoryPlugin.REDSTONE_ACTIVATION_ENABLED)
		{
			int newBlockPower = machineLocation.getBlock().getBlockPower();
			
			if (newBlockPower != blockPower)
			{
				if (blockPower == 0) //OreGin has been activated
				{
					if (!active)
					{
						powerOn();
					}
				}
				else if (newBlockPower == 0) //OreGin has been deactivated
				{
					if (active)
					{
						powerOff();
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
		powerOff();
		broken = true;
	}
	
	/**
	 * Destroys the OreGin
	 */
	public void destroy(ItemStack item)
	{
		setItemMeta(item);
		machineLocation.getWorld().dropItemNaturally(machineLocation, item);
		if (machineLocation.getBlock().getRelative(BlockFace.UP,1).getType().equals(MachineFactoryPlugin.LIGHT_ON) ||
				machineLocation.getBlock().getRelative(BlockFace.UP,1).getType().equals(MachineFactoryPlugin.LIGHT_OFF))
		{
			machineLocation.getBlock().getRelative(BlockFace.UP,1).setType(Material.AIR);
		}
		machineLocation.getBlock().setType(Material.AIR);
		OreGinSoundCollection.getDestructionSound().playSound(machineLocation);
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
		
		if (blockBreaks >= getProperties().getMaxBlockBreaks())
		{
			colorCode = ChatColor.RED;
			status = "Broken";
		}
		else if (blockBreaks >= getProperties().getMaxBlockBreaks()/2)
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
		miningTimer = 0;
		broken = false;
	}

	/*
	 ----------MINING LOGIC--------
	 */
	
	/**
	 * Mines forward in the particular schematic of the OreGin
	 */
	public void mineForward()
	{
		if (miningDistance < getProperties().getMaxMiningDistance())
		{
			if (blockBreaks < getProperties().getMaxBlockBreaks())
			{
				Vector miningOffset = getMiningOffset();
				Location startingPos = machineLocation.getBlock().getLocation().add(
						(int)miningOffset.getX(), (int)miningOffset.getY(), (int)miningOffset.getZ());

				Block machineBlock = machineLocation.getBlock();
				BlockFace facing = getDirection(machineBlock.getState().getRawData());
				  
				for (int x = 0; x < Math.max(1, Math.abs(miningOffset.getX()*2)); x++) 
				{ 
					for (int y = 0; y < Math.max(1,  Math.abs(miningOffset.getY()*2)); y++)
					{
						for (int z = 0; z < Math.max(1,  Math.abs(miningOffset.getZ()*2)); z++)
						{
							if (!active)
							{
								removeFuel();
								return;
							}
							
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
					OreGinSoundCollection.getMiningSound().playSound(machineLocation);
				}
			}
			else
			{
				breakOreGin();
			}		
		}
		else
		{
			powerOff();
			miningDistance = 0;
		}
	}
	
	/**
	 * Mines the block given based on the properties of this OreGin
	 */
	public boolean mineBlock(Block block)
	{
		Material blockType = block.getType();
		
		if (blockType.equals(Material.AIR))
		{
			return false;
		}
		else if (MachinesManager.machineMan.macineExistsAt(block.getLocation()))
		{
			return false;
		}
		else if ( (blockType.equals(Material.WATER) && !MachineFactoryPlugin.WATER_MINING_ENABLED) 
			   || (blockType.equals(Material.LAVA) && !MachineFactoryPlugin.LAVA_MINING_ENABLED) )
		{
			return false;
		}
		else if (MachineFactoryPlugin.INDESTRUCTIBLE.contains(blockType))
		{
			return false;
		}
		else
		{
			Block oreGinBlock = machineLocation.getBlock(); //If the reinforcements match
			if (CitadelInteraction.blockReinforced(block) && CitadelInteraction.reinforcementsMatch(block, oreGinBlock)
					|| !CitadelInteraction.blockReinforced(block))
			{
				if (getProperties().getRetrieveValuables() && MachineFactoryPlugin.VALUABLES.contains(blockType))
				{
					if (CitadelInteraction.breakBlock(block))
					{
						Collection<ItemStack> drops = block.getDrops();
						
						for (ItemStack item : drops)
						{
							HashMap<Integer,ItemStack> leftOvers = addMaterial(item);
							
							for (Entry<Integer,ItemStack> entry : leftOvers.entrySet())
							{
								block.getWorld().dropItemNaturally(this.machineLocation, entry.getValue());
							}
						}

						block.setType(Material.AIR);
					}
				}
				else if (MachineFactoryPlugin.JUNK_DESTRUCTION_ENABLED && MachineFactoryPlugin.JUNK.contains(blockType))
				{
					if (CitadelInteraction.breakBlock(block))
					{
						block.setType(Material.AIR);
					}
				}
				else 
				{
					if (CitadelInteraction.breakBlock(block))
					{
						block.breakNaturally();
					}
				}
				return true;
			}
			else
			{
				powerOff();
				return false;
			}
		}
	}
	
	/**
	 * Block Offset for mininge
	 */
	public Vector getBlockOffset(double x, double y, double z)
	{
		BlockFace facing = getDirection(machineLocation.getBlock().getState().getRawData());
		
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
		BlockFace facing = getDirection(machineLocation.getBlock().getState().getRawData());
		
		Vector miningOffset = new Vector(0, 0, 0);
		
		//Determines the Z or X offset
		if (getProperties().getShaftWidth() > 1)
		{
			if (facing.equals(BlockFace.NORTH))
			{
					miningOffset.setX((double)-getProperties().getShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.SOUTH))
			{
					miningOffset.setX((double)getProperties().getShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.EAST))
			{
					miningOffset.setZ((double)-getProperties().getShaftWidth() / 2);
			}
			else if (facing.equals(BlockFace.WEST))
			{
					miningOffset.setZ((double)getProperties().getShaftWidth() / 2);
			}
		}
		
		//Determines the Y offset
		if (getProperties().getShaftHeight() > 1)
		{
			miningOffset.setY((double)-getProperties().getShaftHeight() / 2);
		}
		
		return miningOffset;
	}
	
	/*
	 ----------POWER LOGIC--------
	 */
	
	/**
	 * Shuts down the Ore Gin from mining.
	 */
	public void powerOff()
	{
		miningTimer = 0;
		active = false;
		turnOffLight();
		OreGinSoundCollection.getPowerOffSound().playSound(machineLocation);
	}
	
	/**
	 * Turns on the OreGin
	 */
	public void powerOn()
	{
		if (isFuelAvailable())
		{
			active = true;
			miningTimer = 0;
			turnOnLight();
			OreGinSoundCollection.getPowerOnSound().playSound(machineLocation);
		}
		else
		{
			OreGinSoundCollection.getErrorSound().playSound(machineLocation);
		}
	}
	
	/**
	 * Toggles the OreGin
	 */
	public InteractionResponse togglePower()
	{
		if (!active)
		{
			if (!broken)
			{
				if (isFuelAvailable())
				{
					powerOn();
					return new InteractionResponse(InteractionResult.SUCCESS, "OreGin activated!");
				}
				else
				{
					OreGinSoundCollection.getErrorSound().playSound(machineLocation);
					return new InteractionResponse(InteractionResult.FAILURE, 
							"Missing fuel! " + getRequiredAvailableMaterials(getProperties().getFuelAmount(),
									getProperties().getFuelMaterial()));
				}
			}
			else
			{
				OreGinSoundCollection.getErrorSound().playSound(machineLocation);
				return new InteractionResponse(InteractionResult.FAILURE, "OreGin is broken! You must repair it first!");
			}
		}
		else
		{
			powerOff();
			return new InteractionResponse(InteractionResult.FAILURE, "OreGin deactivated!");
		}
		
	}

	/*
	 ----------REPAIR LOGIC--------
	 */
	
	/**
	 * Attempts to repair
	 */
	public InteractionResponse repair()
	{
		if (broken)
		{
			if (isRepairMaterialAvailable())
			{
				removeRepairMaterials();
				broken = false;
				blockBreaks = 0;
				OreGinSoundCollection.getRepairSound().playSound(machineLocation);
				
				return new InteractionResponse(InteractionResult.SUCCESS,
						"OreGin has been successfully repaired!");
			}
			else
			{
				OreGinSoundCollection.getErrorSound().playSound(machineLocation);
				
				return new InteractionResponse(InteractionResult.FAILURE,
												"Missing repair materials! " 
												+ getRequiredAvailableMaterials(getProperties().getRepairAmount(),
														getProperties().getRepairMaterial()));
			}
		}
		else
		{
			OreGinSoundCollection.getErrorSound().playSound(machineLocation);
			
			return new InteractionResponse(InteractionResult.FAILURE,
					"The OreGin is not broken!");
		}
	}
	
	/**
	 * Returns whether there is material available for repairing
	 */
	public boolean isRepairMaterialAvailable()
	{
		return isMaterialAvailable(getProperties().getRepairAmount(), getProperties().getRepairMaterial(), this.machineLocation);
	}
	
	/**
	 * Attempts to remove materials for repair
	 */
	public boolean removeRepairMaterials()
	{
		return removeMaterial(getProperties().getRepairAmount(), getProperties().getRepairMaterial(), this.machineLocation);
	}
		
	/*
	 ----------FUEL LOGIC--------
	 */
	
	/**
	 * Returns whether there is enough fuel available for one mining operation
	 */
	public boolean isFuelAvailable()
	{
		return isMaterialAvailable(getProperties().getFuelAmount(), getProperties().getFuelMaterial(), this.machineLocation);
	}
	
	/**
	 * Removes a certain amount of fuel
	 */
	public boolean removeFuel()
	{		
		return removeMaterial(getProperties().getFuelAmount(), getProperties().getFuelMaterial(), this.machineLocation);
	}

	/*
	 ----------OREGIN LIGHT LOGIC--------
	 */
	
	/**
	 * Turns off the OreGin light
	 */
	public void turnOffLight()
	{
		if (!machineLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(MachineFactoryPlugin.LIGHT_OFF))
		{
			machineLocation.getBlock().getRelative(BlockFace.UP, 1).setType(MachineFactoryPlugin.LIGHT_OFF);
		}
	}
	
	/**
	 * Turns on the OreGin light
	 */
	public void turnOnLight()
	{
		if (!machineLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(MachineFactoryPlugin.LIGHT_ON))
		{
			machineLocation.getBlock().getRelative(BlockFace.UP, 1).setType(MachineFactoryPlugin.LIGHT_ON);
		}
	}
	
	/**
	 * Toggles the OreGin light
	 */
	public void toggleLight()
	{
		if (machineLocation.getBlock().getRelative(BlockFace.UP, 1).getType().equals(MachineFactoryPlugin.LIGHT_OFF))
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
     * Adds a specific material to dispenser and returns left overs
	 */
	public HashMap<Integer,ItemStack> addMaterial(ItemStack item)
	{
		return addMaterial(item.getAmount(), item.getType(), this.machineLocation);
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
		return active;
	}
	
	/**
	 * 'machineLocation' public accessor
	 */
	public Location getLocation()
	{
		return machineLocation;
	}
	
	/**
	 * 'tierLevel' public accessor
	 */
	public int getTierLevel()
	{
		return tierLevel;
	}
	
	/**
	 * Returns the properties for this machine
	 */
	public OreGinProperties getProperties()
	{
		return (OreGinProperties)machineProperties;
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
	public boolean isValidUpgrade(int desiredTier)
	{
		return (desiredTier <= MachineFactoryPlugin.MAX_OREGIN_TIERS) && upgradeMaterialAvailable(desiredTier);
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
			
		for (int i = 1; i <= MachineFactoryPlugin.MAX_OREGIN_TIERS; i++)
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