package com.github.MrTwiggy.MachineFactory;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.MrTwiggy.MachineFactory.Interfaces.Properties;
import com.github.MrTwiggy.MachineFactory.Managers.MachinesManager;
import com.github.MrTwiggy.MachineFactory.Utility.Dimensions;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse.InteractionResult;

/**
 * MachineObject.java
 * Purpose: Basic object base for machines to extend
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class MachineObject
{
	public enum MachineType
	{
		OREGIN,
		CLOAKER,
		SMELTER
	}
	
	
	protected Location machineLocation; // Current location of machine center
	protected boolean active; // Whether machine is currently active
	protected Dimensions dimensions; // The dimensions of the machine
	protected int tierLevel; // The tier level of the machine
	protected Inventory machineInventory; // The inventory of the machine
	protected MachineType machineType; // The type this machine is
	protected Properties machineProperties; // The properties of this machine type and tier
	
	protected boolean upgraded; // Whether the tier has recently upgraded
	
	/**
	 * Constructor
	 */
	public MachineObject(Location machineLocation, Dimensions dimensions, MachineType machineType)
	{
		this.machineLocation = machineLocation;
		this.dimensions = dimensions;
		this.active = false;
		this.tierLevel = 1;
		this.machineType = machineType;
		this.upgraded = false;
		initializeInventory();
		updateProperties();
	}
	
	/**
	 * Constructor
	 */
	public MachineObject(Location machineLocation, Dimensions dimensions, int tierLevel,
							MachineType machineType)
	{
		this.machineLocation = machineLocation;
		this.dimensions = dimensions;
		this.active = false;
		this.tierLevel = tierLevel;
		this.machineType = machineType;
		this.upgraded = false;
		initializeInventory();
		updateProperties();
	}
	
	/**
	 * Constructor
	 */
	public MachineObject(Location machineLocation, Dimensions dimensions, boolean active, int tierLevel,
							MachineType machineType)
	{
		this.machineLocation = machineLocation;
		this.dimensions = dimensions;
		this.active = active;
		this.tierLevel = tierLevel;
		this.machineType = machineType;
		this.upgraded = false;
		initializeInventory();
		updateProperties();
	}
	
	/**
	 * Constructor
	 */
	public MachineObject(Location machineLocation, Dimensions dimensions, boolean active, int tierLevel,
							MachineType machineType, Inventory machineInventory)
	{
		this.machineLocation = machineLocation;
		this.dimensions = dimensions;
		this.active = active;
		this.tierLevel = tierLevel;
		this.machineType = machineType;
		this.machineInventory = machineInventory;
		updateProperties();
	}

	/**
	 * Initializes the inventory for this machine
	 */
	public void initializeInventory()
	{
		switch(machineType)
		{
		case OREGIN:
			Dispenser dispenserBlock = (Dispenser)machineLocation.getBlock().getState();
			machineInventory = dispenserBlock.getInventory();
			break;
		case CLOAKER:
			machineInventory = Bukkit.getServer().createInventory(null, 27, "Cloaker Inventory");
			break;
		case SMELTER:
			break;
		}
	}
	
	/**
	 * Updates the current properties for the current tier level
	 */
	public void updateProperties()
	{
		machineProperties = MachineFactoryPlugin.getProperties(machineType, tierLevel);
	}
	
	/**
	 * Attempts to upgrade Machine
	 */
	public InteractionResponse upgrade()
	{
		int desiredTier = tierLevel + 1;
		Properties desiredTierProperties = MachineFactoryPlugin.getProperties(machineType, desiredTier);
		
		if (desiredTier <= MachineFactoryPlugin.getMaxTiers(machineType))
		{
			Material upgradeMaterial = desiredTierProperties.getUpgradeMaterial();
			
			if (upgradeMaterialAvailable(desiredTier))
			{
				removeUpgradeMaterial(desiredTier);
				tierLevel++;
				updateProperties();
				MachinesManager.machineMan.playUpgradeSound(machineLocation, machineType);
				upgraded = true;
				return new InteractionResponse(InteractionResult.SUCCESS,
						machineName() + " successfully upgraded to tier " + tierLevel + "!!");
			}
			else
			{
				MachinesManager.machineMan.playErrorSound(machineLocation, machineType);
				return new InteractionResponse(InteractionResult.FAILURE,
						 "Missing upgrade materials! " 
								 + getRequiredAvailableMaterials(desiredTierProperties.getUpgradeAmount(),
									upgradeMaterial));
			}
		}
		else
		{
			MachinesManager.machineMan.playUpgradeSound(machineLocation, machineType);
			return new InteractionResponse(InteractionResult.FAILURE,
					machineName() + " is already max tier level!");
		}
	}
	
	/**
	 * Returns the user-friendly name for this machine type
	 */
	public String machineName()
	{
		switch (machineType)
		{
		case OREGIN:
			return "OreGin";
		case CLOAKER:
			return "Cloaker";
		case SMELTER:
			return "Smelter";
		default: 
			return null;
		}
	}
	
	/**
	 * Returns whether there is enough material available for an upgrade in cloaker inventory
	 */
	public boolean upgradeMaterialAvailable(int desiredTier)
	{
		Properties desiredProperties = MachineFactoryPlugin.getProperties(machineType, desiredTier);
		return (isMaterialAvailable(desiredProperties.getUpgradeAmount(), desiredProperties.getUpgradeMaterial()));
	}
	
	/**
	 * Attempts to remove materials for upgrading from cloaker inventory
	 */
	public boolean removeUpgradeMaterial(int desiredTier)
	{
		Properties desiredProperties = MachineFactoryPlugin.getProperties(machineType, desiredTier);
		return (removeMaterial(desiredProperties.getUpgradeAmount(), desiredProperties.getUpgradeMaterial()));
	}
	
	/**
	 * Attempts to remove a specific material of given amount from dispenser
	 */
	public boolean removeMaterial(int amount, Material material)
	{
		HashMap<Integer,? extends ItemStack> inventoryMaterials = getInventory().all(material);
		
		int materialsToRemove = amount;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			if (materialsToRemove <= 0)
				break;
			
			if(entry.getValue().getAmount() == materialsToRemove)
			{
				getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
				materialsToRemove = 0;
			}
			else if(entry.getValue().getAmount() > materialsToRemove)
			{
				getInventory().setItem(entry.getKey(), new ItemStack(material, (entry.getValue().getAmount() - materialsToRemove)));
				materialsToRemove = 0;
			}
			else
			{
				int inStack = entry.getValue().getAmount();
				getInventory().setItem(entry.getKey(), new ItemStack(Material.AIR, 0));
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
		HashMap<Integer,? extends ItemStack> inventoryMaterials = getInventory().all(material);
		
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
		HashMap<Integer,? extends ItemStack> inventoryMaterials = getInventory().all(material);
		
		int totalMaterial = 0;
		for(Entry<Integer,? extends ItemStack> entry : inventoryMaterials.entrySet())
		{
			totalMaterial += entry.getValue().getAmount();
		}
		
		return totalMaterial;
	}

	/**
	 * 'cloakerInventory' public accessor
	 */
	public Inventory getInventory()
	{
		switch (machineType)
		{
		case OREGIN:
			Dispenser dispenserBlock = (Dispenser)machineLocation.getBlock().getState();
			machineInventory = dispenserBlock.getInventory();
			return machineInventory;
		case CLOAKER:
			return machineInventory;
		case SMELTER:
			return machineInventory;
		default:
			return machineInventory;
		}
	}

	
}
