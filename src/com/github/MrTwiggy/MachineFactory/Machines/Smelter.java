package com.github.MrTwiggy.MachineFactory.Machines;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.github.MrTwiggy.MachineFactory.MachineObject;
import com.github.MrTwiggy.MachineFactory.Interfaces.Machine;
import com.github.MrTwiggy.MachineFactory.Utility.Dimensions;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;

/**
 * Smelter.java
 * Purpose: Functionality for Smelter objects
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class Smelter extends MachineObject implements Machine
{
	
	public static final int WIDTH = 5; // The width of a Smelter machine
	public static final int HEIGHT = 3; // The height of a Smelter machine
	public static final int DEPTH = 4; // The depth of a Smelter machine
	
	/**
	 * Constructor
	 */
	public Smelter (Location machineLocation)
	{
		super(machineLocation,
				Smelter.getDimensions(Smelter.getDirection(machineLocation.getBlock().getState().getRawData())));
	}
	
	/**
	 * Returns the input inventory for Smelter
	 */
	public Inventory getInputChestInventory()
	{
		return null;
	}
	
	/**
	 * Returns the output inventory for Smelter
	 */
	public Inventory getOutputChestInventory()
	{
		return null;
	}

	/**
	 * Updates the Smelter
	 */
	public void update() 
	{
		
	}
	
	/**
	 * Destroys the Smelter
	 */
	public void destroy(ItemStack item) 
	{

	}

	/**
	 * Power on Smelter
	 */
	public void powerOn()
	{
		active = true;
		
	}

	/**
	 * Power off Smelter
	 */
	public void powerOff() 
	{
		active = false;
		
	}
	
	/**
	 * Verifies the Smelter follows the proper schematic
	 */
	public static boolean verifySmelter(Location machineLocation)
	{
		BlockFace facing = Smelter.getDirection(machineLocation.getBlock().getState().getRawData());
		Location startingPosition = machineLocation.add(Smelter.getOffset(facing));
		Dimensions dimensions = Smelter.getDimensions(facing);
		
		for (int x = 0; x < dimensions.getXLength(); x++)
		{
			for (int y = 0; y < dimensions.getYLength(); y++)
			{
				for (int z = 0; z < dimensions.getZLength(); z++)
				{
					Location tempLoc = startingPosition.getBlock().getLocation();
					
					if (!tempLoc.getBlock().getLocation().add(Smelter.getBlockOffset(x, y, z, facing)).getBlock().getType().equals(Material.COBBLESTONE)
							&& !tempLoc.getBlock().getLocation().add(Smelter.getBlockOffset(x, y, z, facing)).getBlock().getType().equals(Material.FURNACE)
							&& !tempLoc.getBlock().getLocation().add(Smelter.getBlockOffset(x, y, z, facing)).getBlock().getType().equals(Material.CHEST))
					{
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Returns the offset for schematic verification
	 */
	public static Vector getOffset(BlockFace facing)
	{
		if (facing == null)
			return new Vector((int)(Smelter.WIDTH / 2), (int)(-Smelter.HEIGHT / 2), 0);
		
		if (facing.equals(BlockFace.NORTH))
		{
			return new Vector((int)(Smelter.WIDTH / 2), (int)(-Smelter.HEIGHT / 2), 0);
		}
		else if (facing.equals(BlockFace.EAST))
		{
			return new Vector(0, (int)(-Smelter.HEIGHT / 2), (int)(-Smelter.WIDTH / 2));
		}
		else if (facing.equals(BlockFace.SOUTH))
		{
			return new Vector((int)(-Smelter.WIDTH / 2), (int)(-Smelter.HEIGHT / 2), 0);
		}
		else if (facing.equals(BlockFace.WEST))
		{
			return new Vector(0, (int)(-Smelter.HEIGHT / 2), (int)(Smelter.WIDTH / 2));
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Block Offset for verifying schematic
	 */
	public static Vector getBlockOffset(double x, double y, double z, BlockFace facing)
	{		
		if (facing == null)
			return new Vector(-x,y,z);
		if (facing.equals(BlockFace.NORTH) || facing.equals(BlockFace.EAST))
		{
			x = -x;
		}
		else
		{
			z = -z;
		}
		
		return new Vector(x, y, z);
	}

	/**
	 * Toggles the poewr
	 */
	public InteractionResponse togglePower() 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Returns the appropriate dimensions
	 */
	public static Dimensions getDimensions(BlockFace facing)
	{
		int xLength = 0;
		int yLength = HEIGHT;
		int zLength = 0;
		
		if (facing == null)
		{
			xLength = WIDTH;
			zLength = DEPTH;
			return new Dimensions(xLength,yLength,zLength);
		}
		
		switch (facing)
		{
		case NORTH:
		case SOUTH:
			xLength = WIDTH;
			zLength = DEPTH;
			break;
		case EAST:
		case WEST:
			xLength = DEPTH;
			zLength = WIDTH;
			break;
		default:
			break;
		}
		
		return new Dimensions(xLength, yLength, zLength);
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

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

}
