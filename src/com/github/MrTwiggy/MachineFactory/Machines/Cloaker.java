package com.github.MrTwiggy.MachineFactory.Machines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
import com.github.MrTwiggy.MachineFactory.MachineObject;
import com.github.MrTwiggy.MachineFactory.Interfaces.Machine;
import com.github.MrTwiggy.MachineFactory.Properties.CloakerProperties;
import com.github.MrTwiggy.MachineFactory.SoundCollections.CloakerSoundCollection;
import com.github.MrTwiggy.MachineFactory.Utility.Dimensions;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse.InteractionResult;

/**
 * Cloaker.java
 * Purpose: Functionality for Cloaker objects
 *
 * @author MrTwiggy
 * @version 0.1 1/17/13
 */
public class Cloaker extends MachineObject implements Machine
{
	List<Block> cloakedBlocks; // List of blocks in the cloaking field
	Map<String, Boolean> cloakedClients; // List of clients within cloaking chunk range
	
	private int tierLevel; // The current tier level
	private CloakerProperties cloakerProperties; // The current properties for this tier of Cloaker
	private Inventory cloakerInventory; // The inventory of the Cloaker
	private double cloakedDuration; // The duration of the current cloaking
	
	/*
	 ----------CLOAKER CONSTRUCTORS--------
	 */

	/**
	 * Constructor
	 */
	public Cloaker(Location machineLocation) 
	{
		super(machineLocation, new Dimensions(1,1,1));
		cloakedBlocks = new ArrayList<Block>();
		cloakedClients = new HashMap<String, Boolean>();
		tierLevel = 1;
		cloakedDuration = 0;
		cloakerInventory = Bukkit.getServer().createInventory(null, 27, "Cloaker Inventory");
		updateCloakerProperties();
		InitiateCloaking();
		CloakerSoundCollection.getCreationSound().playSound(machineLocation);
	}
	
	/**
	 * Constructor
	 */
	public Cloaker(int tierLevel, Location machineLocation) 
	{
		super(machineLocation, new Dimensions(1,1,1));
		cloakedBlocks = new ArrayList<Block>();
		cloakedClients = new HashMap<String, Boolean>();
		this.tierLevel = tierLevel;
		cloakedDuration = 0;
		cloakerInventory = Bukkit.getServer().createInventory(null, 27, "Cloaker Inventory");
		updateCloakerProperties();
		InitiateCloaking();
		CloakerSoundCollection.getPlacementSound().playSound(machineLocation);
	}
	
	/**
	 * Constructor
	 */
	public Cloaker(int tierLevel, boolean active, double cloakedDuration, Inventory cloakerInventory,
			Location machineLocation) 
	{
		super(machineLocation, new Dimensions(1,1,1), active);
		cloakedBlocks = new ArrayList<Block>();
		cloakedClients = new HashMap<String, Boolean>();
		this.tierLevel = tierLevel;
		this.cloakedDuration = cloakedDuration;
		this.cloakerInventory = cloakerInventory;
		updateCloakerProperties();
		InitiateCloaking();
	}
	
	/*
	 ----------CLOAKER MAINTENENCE LOGIC--------
	 */
	
	/**
	 * Updates Cloaker logic
	 */
	public void update() 
	{
		if (active) //If cloaking
		{
			CloakBlocks();
			cloakedDuration += (MachineFactoryPlugin.CLOAKER_UPDATE_CYCLE / MachineFactoryPlugin.TICKS_PER_SECOND);
			MachineFactoryPlugin.sendConsoleMessage("Duration: " + cloakerProperties.getFuelTimeDuration());
			if (cloakedDuration >= cloakerProperties.getFuelTimeDuration())
			{
				cloakedDuration = 0;
				
				if (fuelAvailable())
				{
					removeFuel();	
				}
				else
				{
					powerOff();
				}
			}
		}
		else // If not cloaking
		{

		}
	}
	
	/**
	 * Updates the current Cloaker Properties
	 */
	public void updateCloakerProperties()
	{
		cloakerProperties = MachineFactoryPlugin.Cloaker_Properties.get(tierLevel);
	}
	
	/**
	 * Destroys the Cloaker and drops appropriate items
	 */
	public void destroy(ItemStack item) 
	{
		setItemMeta(item);
		machineLocation.getWorld().dropItemNaturally(machineLocation, item);
		machineLocation.getBlock().setType(Material.AIR);
		
		ItemStack[] contents = cloakerInventory.getContents();
		for (int i = 0; i < contents.length; i++)
		{
			if (contents[i] != null)
				machineLocation.getWorld().dropItemNaturally(machineLocation, contents[i]);
		}
		
		CloakerSoundCollection.getDestructionSound().playSound(machineLocation);
	}
	
	/**
	 * Sets the correct name and lore for the item
	 */
	public void setItemMeta(ItemStack item)
	{
		ItemMeta meta = item.getItemMeta();
		
		String name = Cloaker.CloakerName(tierLevel);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
	}
		
	/*
	 ----------CLOAKER UPGRADE LOGIC--------
	 */
	
	/**
	 * Attempts to upgrade Cloaker
	 */
	public InteractionResponse upgrade()
	{
		int desiredTier = tierLevel + 1;
		CloakerProperties desiredTierProperties = MachineFactoryPlugin.Cloaker_Properties.get(desiredTier);
		
		if (desiredTier <= MachineFactoryPlugin.MAX_CLOAKER_TIERS)
		{
			Material upgradeMaterial = desiredTierProperties.getUpgradeType();
			
			if (upgradeMaterialAvailable(desiredTier))
			{
				removeUpgradeMaterial(desiredTier);
				tierLevel = desiredTier;
				updateCloakerProperties();
				CloakerSoundCollection.getUpgradeSound().playSound(machineLocation);
				return new InteractionResponse(InteractionResult.SUCCESS,
						"Cloaker successfully upgraded to tier " + tierLevel + "!");
			}
			else
			{
				CloakerSoundCollection.getErrorSound().playSound(machineLocation);
				return new InteractionResponse(InteractionResult.FAILURE,
						 "Missing upgrade materials! " 
								 + getRequiredAvailableMaterials(desiredTierProperties.getUpgradeAmount(),
									upgradeMaterial));
			}
		}
		else
		{
			CloakerSoundCollection.getErrorSound().playSound(machineLocation);
			return new InteractionResponse(InteractionResult.FAILURE,
					"Cloaker is already max tier level!");
		}
	}
	
	/**
	 * Returns whether there is enough material available for an upgrade in cloaker inventory
	 */
	public boolean upgradeMaterialAvailable(int desiredTier)
	{
		CloakerProperties properties = MachineFactoryPlugin.Cloaker_Properties.get(desiredTier);
		return (isMaterialAvailable(properties.getUpgradeAmount(), properties.getUpgradeType()));
	}
	
	/**
	 * Attempts to remove materials for upgrading from cloaker inventory
	 */
	public boolean removeUpgradeMaterial(int desiredTier)
	{
		CloakerProperties properties = MachineFactoryPlugin.Cloaker_Properties.get(desiredTier);
		return (removeMaterial(properties.getUpgradeAmount(), properties.getUpgradeType()));
	}
	
	/*
	 ----------CLOAKER FUEL LOGIC--------
	 */
	
	/**
	 * Returns whether there is enough material available for fueling one cloak operation
	 */
	public boolean fuelAvailable()
	{
		return (isMaterialAvailable(cloakerProperties.getFuelAmount(), cloakerProperties.getFuelType()));
	}
	
	/**
	 * Attempts to remove materials for fueling one cloaking operation
	 */
	public boolean removeFuel()
	{
		CloakerSoundCollection.getRefuelSound().playSound(machineLocation);
		return (removeMaterial(cloakerProperties.getFuelAmount(), cloakerProperties.getFuelType()));
	}
	
	/*
	 ----------CLOAKING LOGIC--------
	 */
	
	/**
	 * Cloaks all designated blocks
	 */
	public void CloakBlocks()
	{		
		for (Player player : machineLocation.getWorld().getPlayers())
		{
			int chunkXDiff = Math.abs(player.getLocation().getChunk().getX() 
					- machineLocation.getChunk().getX());
			int chunkZDiff = Math.abs(player.getLocation().getChunk().getZ()
					- machineLocation.getChunk().getZ());
			
			if (chunkXDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE 
					&& chunkZDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE)
			{
				double playerDist = player.getLocation().distance(machineLocation);
				
				if (!cloakedClients.containsKey(player.getName()))
				{
					cloakedClients.put(player.getName(), true);
				}
				
				if (!cloakedClients.get(player.getName())) // If cloaked
				{
					if (playerDist <= cloakerProperties.getVisibilityRange())
					{
						for (Block block : cloakedBlocks)
						{
							player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
						}
						cloakedClients.put(player.getName(), true);
					}
				}
				else // If not cloaked
				{
					if (playerDist > cloakerProperties.getVisibilityRange())
					{
						for (Block block : cloakedBlocks)
						{
							player.sendBlockChange(block.getLocation(), Material.AIR, (byte)0);
						}
						cloakedClients.put(player.getName(), false);
					}
				}
			}
			else
			{
				cloakedClients.remove(player.getName());
			}
		}
		
		CloakerSoundCollection.getCloakingSound().playSound(machineLocation);
	}
	
	/**
	 * Uncloaks all cloaked blocks
	 */
	public void Uncloak()
	{
		for (String string : cloakedClients.keySet())
		{
			Player player = Bukkit.getServer().getPlayer(string);
			
			if (player != null)
			{
				int chunkXDiff = Math.abs(player.getLocation().getChunk().getX() 
						- machineLocation.getChunk().getX());
				int chunkZDiff = Math.abs(player.getLocation().getChunk().getZ()
						- machineLocation.getChunk().getZ());
	
				if (chunkXDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE 
						&& chunkZDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE)
				{
					for (Block block : cloakedBlocks)
					{
						player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
					}
					player.sendBlockChange(machineLocation, machineLocation.getBlock().getType(), 
							machineLocation.getBlock().getData());
				}
			}
		}
		cloakedClients.clear();
	}

	/**
	 * Initiates the designated cloaking
	 */
	public void InitiateCloaking()
	{
		cloakedBlocks.clear();
		
		BlockFace facing = getDirection(machineLocation.getBlock());
		Vector miningOffset = getCloakingOffset(facing);
		
		Location startingPos = machineLocation.getBlock().getLocation().add(
				(int)miningOffset.getX(), (int)miningOffset.getY(), (int)miningOffset.getZ());
		
		  
		for (int x = 0; x < Math.max(1, Math.abs(miningOffset.getX()*2)); x++) 
		{ 
			for (int y = 0; y < Math.max(1,  Math.abs(miningOffset.getY()*2)); y++)
			{
				for (int z = 0; z < Math.max(1,  Math.abs(miningOffset.getZ()*2)); z++)
				{
					for (int depth = 1; depth <= cloakerProperties.getDepth(); depth++)
					{
						cloakedBlocks.add(startingPos.getBlock().getLocation().add(getBlockOffset(x,y,z,facing)).getBlock().getRelative(facing, depth));
					}
				}
			}
		}
		cloakedBlocks.add(machineLocation.getBlock());
	}
	
	/**
	 * Block Offset for mininge
	 */
	public Vector getBlockOffset(double x, double y, double z, BlockFace facing)
	{
		
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
	public Vector getCloakingOffset(BlockFace facing)
	{
		Vector miningOffset = new Vector(0, 0, 0);
		
		//Determines the Z or X offset
		if (cloakerProperties.getWidth() > 1)
		{
			if (facing.equals(BlockFace.NORTH))
			{
					miningOffset.setX(-(double)cloakerProperties.getWidth() / 2);
			}
			else if (facing.equals(BlockFace.SOUTH))
			{
					miningOffset.setX((double)cloakerProperties.getWidth() / 2);
			}
			else if (facing.equals(BlockFace.EAST))
			{
					miningOffset.setZ(-(double)cloakerProperties.getWidth() / 2);
			}
			else if (facing.equals(BlockFace.WEST))
			{
					miningOffset.setZ((double)cloakerProperties.getWidth() / 2);
			}
		}
		
		//Determines the Y offset
		if (cloakerProperties.getHeight() > 1)
		{
			miningOffset.setY(-(double)cloakerProperties.getHeight() / 2);
		}
		
		return miningOffset;
	}
	
	/*
	 ----------CLOAKER POWER LOGIC--------
	 */
	
	/**
	 * Attempts to activate Cloaker
	 */
	public void powerOn()
	{
		if (removeFuel())
		{
			byte data = machineLocation.getBlock().getData();
			machineLocation.getBlock().setType(MachineFactoryPlugin.CLOAKER_ACTIVATED);
			machineLocation.getBlock().setData(data);
			InitiateCloaking();
			active = true;
			CloakerSoundCollection.getPowerOnSound().playSound(machineLocation);
		}
	}
	
	/**
	 * Attempts to deactivate Cloaker
	 */
	public void powerOff()
	{
		byte data = machineLocation.getBlock().getData();
		machineLocation.getBlock().setType(MachineFactoryPlugin.CLOAKER_DEACTIVATED);
		machineLocation.getBlock().setData(data);
		active = false;
		Uncloak();
		CloakerSoundCollection.getPowerOffSound().playSound(machineLocation);
	}
	
	/**
	 * Attempts to toggle the power state
	 */
	public InteractionResponse togglePower() 
	{
		if (active)
		{
			powerOff();
			return new InteractionResponse(InteractionResult.FAILURE,
					"Cloaker has been deactivated!");
		}
		else
		{
			if (fuelAvailable())
			{
				powerOn();
				return new InteractionResponse(InteractionResult.SUCCESS,
						"Cloaker has been activated!");
			}
			else
			{
				//PLAY SOUND
				return new InteractionResponse(InteractionResult.FAILURE, 
						"Missing fuel! " + getRequiredAvailableMaterials(cloakerProperties.getFuelAmount(),
								cloakerProperties.getFuelType()));
			}
		}
	}
	
	/*
	 ----------CLOAKER INVENTORY MANAGEMENT LOGIC--------
	 */
	
	/**
	 * Opens the Cloaker Inventory for given player
	 */
	public void openInventory(Player player)
	{
		player.openInventory(cloakerInventory);
	}
	
	/**
	 * Attempts to remove a specific material of given amount from dispenser
	 */
	public boolean removeMaterial(int amount, Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = cloakerInventory.all(material);
		
		int materialsToRemove = amount;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			if (materialsToRemove <= 0)
				break;
			
			if(entry.getValue().getAmount() == materialsToRemove)
			{
				cloakerInventory.setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove = 0;
			}
			else if(entry.getValue().getAmount() > materialsToRemove)
			{
				cloakerInventory.setItem(entry.getKey(), new ItemStack(material, (entry.getValue().getAmount() - materialsToRemove)));
				materialsToRemove = 0;
			}
			else
			{
				int inStack = entry.getValue().getAmount();
				cloakerInventory.setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove -= inStack;
			}
		}
		
		return materialsToRemove == 0;
	}
	
	/**
	 * Checks if a specific material of given amount is available in dispenser
	 */
	public boolean isMaterialAvailable(int amount, Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = cloakerInventory.all(material);
		
		int totalMaterial = 0;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			totalMaterial += entry.getValue().getAmount();
		}
		
		return (totalMaterial >= amount);
	}
	
	/**
	 * Returns the "Required: (X MATERIAL) Available: (Y MATERIAL)" message
	 */
	public String getRequiredAvailableMaterials(int amount, Material material)
	{
		return "Required: (" + amount + " " + material.toString() + ") Available: ("
				+ getMaterialAvailableAmount(material) + " " + material.toString() + ")";
	}
	
	/**
     * Returns how much of a specified material is available in dispenser
	 */
	public int getMaterialAvailableAmount(Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = cloakerInventory.all(material);
		
		int totalMaterial = 0;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			totalMaterial += entry.getValue().getAmount();
		}
		
		return totalMaterial;
	}

	/*
	 ----------CLOAKER PUBLIC ACCESSORS--------
	 */
	
	/**
	 * 'active' public accessor
	 */
	public boolean getActive()
	{
		return active;
	}
	
	/**
	 * 'cloakedDuration' public accessor
	 */
	public double getCloakedDuration()
	{
		return cloakedDuration;
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
	 * 'cloakerInventory' public accessor
	 */
	public Inventory getInventory()
	{
		return cloakerInventory;
	}
	
	/*
	 ----------CLOAKER STATIC METHODS--------
	 */
	
	/**
	 * Converts byte direction into BlockFace direction.
	 */
	public static BlockFace getDirection(Block block)
	{
		byte north, east, south, west;
		
		if (block.getType().equals(Material.PUMPKIN) || block.getType().equals(Material.JACK_O_LANTERN))
		{
			south = 0x0;
			west = 0x01;
			north = 0x02;
			east = 0x03;
		}
		else
		{
			north = 0x02;
			south = 0x03;
			west = 0x04;
			east = 0x05;
		}
		
		byte direction = block.getData();
		
		if (direction == north)
		{
			return BlockFace.NORTH;
		}
		else if (direction == south)
		{
			return BlockFace.SOUTH;
		}
		else if (direction == west)
		{
			return BlockFace.WEST;
		}
		else if (direction == east)
		{
			return BlockFace.EAST;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Returns whether given material matches cloaker material
	 */
	public static boolean isCloakerType(Material material)
	{
		return (material.equals(MachineFactoryPlugin.CLOAKER_ACTIVATED) 
				||material.equals(MachineFactoryPlugin.CLOAKER_DEACTIVATED));
	}
	
	/**
	 * Returns appropriate display name for Cloaker with given tier level
	 */
	public static String CloakerName(int tierLevel)
	{
		return "T" + tierLevel + " Cloaker";
	}
	
	/**
	 * Returns the tier level of the item
	 */
	public static int getTierLevel(String name)
	{
		for (int i = 1; i <= MachineFactoryPlugin.MAX_OREGIN_TIERS; i++)
		{
			if (name.equalsIgnoreCase(Cloaker.CloakerName(i)))
				return i;
		}
			
		return 0;
	}
	
}
