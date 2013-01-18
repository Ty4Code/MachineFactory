package com.github.MrTwiggy.MachineFactory.Listeners;

import static com.untamedears.citadel.Utility.isReinforced;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;

import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
import com.github.MrTwiggy.MachineFactory.Machines.OreGin;
import com.github.MrTwiggy.MachineFactory.Managers.OreGinManager;
import com.github.MrTwiggy.MachineFactory.SoundCollections.OreGinSoundCollection;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;

/**
 * OreGinListener.java
 * Purpose: Listens for OreGin related events and interactions
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGinListener implements Listener
{
	
	OreGinManager oreGinMan; //The OreGinManager object
	
	/**
	 * Constructor
	 */
	public OreGinListener(OreGinManager oreGinMan)
	{
		this.oreGinMan = oreGinMan;
	}
	
	/**
	 * Checks to see if an OreGin can be created
	 * @throws IOException 
	 */
	@EventHandler
	public void oreGinInteraction(PlayerInteractEvent event) throws IOException
	{
		Block clicked = event.getClickedBlock();
		Player creator = event.getPlayer();
		
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			if (clicked.getState() instanceof Dispenser)
			{
				//Create and/or upgrade OreGin
				if (creator.getItemInHand().getType().equals(MachineFactoryPlugin.OREGIN_UPGRADE_WAND))
				{
					
					if (oreGinMan.machineExistsAt(clicked.getLocation()))
					{
						OreGin oreGin = (OreGin) oreGinMan.getMachine(clicked.getLocation());
						
						//Send player success/failure message
						InteractionResponse.messagePlayerResult(creator, oreGin.upgrade());
					}
					else
					{
						//Send player success/failure message
						InteractionResponse.messagePlayerResult(creator, 
								oreGinMan.createMachine(clicked.getLocation()));
					}
				} //Activate or de-activate OreGin
				else if (creator.getItemInHand().getType().equals(MachineFactoryPlugin.OREGIN_ACTIVATION_WAND))
				{
					if (oreGinMan.machineExistsAt(clicked.getLocation()))
					{
						OreGin oreGin = (OreGin) oreGinMan.getMachine(clicked.getLocation());
						
						//Send player success/failure message
						InteractionResponse.messagePlayerResult(creator, oreGin.togglePower());
					}
				} //Repair OreGin
				else if (creator.getItemInHand().getType().equals(MachineFactoryPlugin.OREGIN_REPAIR_WAND))
				{
					if (oreGinMan.machineExistsAt(clicked.getLocation()))
					{
						OreGin oreGin = (OreGin) oreGinMan.getMachine(clicked.getLocation());
						
						//Send player success/failure message
						InteractionResponse.messagePlayerResult(creator, oreGin.repair());
					}
				}
			}
		}
		

	}
	
	/**
	 * Checks to see if an OreGin (dispenser or light) is being destroyed (block broken)
	 */
	@EventHandler
	public void oreGinBroken(BlockBreakEvent event)
	{
		Block destroyed = event.getBlock();
		
		if ((destroyed.getState() instanceof Dispenser) || destroyed.getType().equals(MachineFactoryPlugin.LIGHT_ON)
				|| destroyed.getType().equals(MachineFactoryPlugin.LIGHT_OFF))
		{
			if (oreGinMan.machineExistsAt(destroyed.getLocation()) || oreGinMan.oreGinLightExistsAt(destroyed.getLocation()))
			{
				OreGin oreGin = (OreGin) oreGinMan.getMachine(destroyed.getLocation());
				if (oreGinMan.oreGinLightExistsAt(destroyed.getLocation()))
					oreGin = (OreGin) oreGinMan.getMachine(destroyed.getRelative(BlockFace.DOWN).getLocation());
				
				event.setCancelled(true);
				
				if ( (MachineFactoryPlugin.CITADEL_ENABLED && !isReinforced(oreGin.getLocation().getBlock()) 
						&& !isReinforced(oreGin.getLocation().getBlock().getRelative(BlockFace.UP)))
					|| !MachineFactoryPlugin.CITADEL_ENABLED)
				{
					ItemStack dropItem = new ItemStack(Material.DISPENSER, 1);
					oreGin.destroy(dropItem);
					oreGinMan.removeMachine(oreGin);
				}
			}
		}
	}
	
	/**
	 * Checks to see if an OreGin has been placed
	 */
	@EventHandler
	public void oreGinPlaced(BlockPlaceEvent event)
	{
		Block placed = event.getBlock();
		
		if (oreGinMan.isOreGin(event.getItemInHand()))
		{
			if (OreGin.isValidOreGinCreationLocation(placed.getLocation()))
			{
				OreGin oreGin = new OreGin(placed.getLocation(), OreGin.getTierLevel(event.getItemInHand().getItemMeta().getDisplayName()), 
						OreGin.getBlockBreaksFromLore(event.getItemInHand().getItemMeta().getLore()), oreGinMan);
				oreGinMan.addMachine(oreGin);
				event.getPlayer().sendMessage(ChatColor.GREEN + "An OreGin of tier level " + oreGin.getTierLevel() + " was placed!");
			}
			else
			{
				OreGinSoundCollection.getErrorSound().playSound(placed.getLocation());
				event.getPlayer().sendMessage(ChatColor.RED + "Space above OreGin must be empty!");
				event.setCancelled(true);
			}
			
		}
	}

	/**
	 * Helps with organizing inventory
	 */
	@EventHandler 
	public void movedOreGin(InventoryClickEvent event)
	{
		ItemStack cursorItem = event.getCursor();
		ItemStack slotItem = event.getCurrentItem();
		
		if (cursorItem.getType() == Material.DISPENSER)
		{
			if (slotItem.getType() == Material.DISPENSER)
			{
				if (oreGinMan.isOreGin(cursorItem) || oreGinMan.isOreGin(slotItem))
				{
					if ((cursorItem.getItemMeta().getDisplayName() != slotItem.getItemMeta().getDisplayName())
							|| cursorItem.getItemMeta().getLore() != slotItem.getItemMeta().getLore())
					{
						event.setCursor(slotItem);
						event.setCurrentItem(cursorItem);
						event.setCancelled(true);
					}
				}
			}
		}
	}

	/**
	 * Maintains the OreGin lights stable
	 */
	@EventHandler
	public void keepLightsStable(BlockRedstoneEvent event)
	{
		if (event.getBlock().getType().equals(MachineFactoryPlugin.LIGHT_ON) || event.getBlock().getType().equals(MachineFactoryPlugin.LIGHT_OFF))
		{
			if (oreGinMan.oreGinLightExistsAt(event.getBlock().getLocation()))
			{
				if (((OreGin)oreGinMan.getMachine(event.getBlock().getRelative(BlockFace.DOWN).getLocation())).getMining())
				{
					event.setNewCurrent(1);
				}
				else if (!((OreGin)oreGinMan.getMachine(event.getBlock().getRelative(BlockFace.DOWN).getLocation())).getBroken())
				{
					event.setNewCurrent(0);
				}
			}	
		}						
	}
	
	/**
	 * Stops OreGins from dispensing fuel or materials
	 */
	@EventHandler
	public void oreGinDispense(BlockDispenseEvent event)
	{
		event.setCancelled(oreGinMan.machineExistsAt(event.getBlock().getLocation()));
	}

	/**
	 * Stop Piston from pushing an OreGin or it's light
	 */
	@EventHandler
	public void oreGinPistonPush(BlockPistonExtendEvent event)
	{
		List<Block> movedBlocks = event.getBlocks();
		
		for (Block movedBlock : movedBlocks)
		{
			if (oreGinMan.machineExistsAt(movedBlock.getLocation())
					|| oreGinMan.oreGinLightExistsAt(movedBlock.getLocation()))
			{
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Stop Piston from pulling an OreGin or it's light
	 */
	@EventHandler
	public void oreGinPistonPull(BlockPistonRetractEvent event)
	{
		MaterialData materialData = event.getBlock().getState().getData();
		BlockFace blockFace;
		Block movedBlock;

		if (materialData instanceof PistonBaseMaterial) 
		{
			blockFace = ((PistonBaseMaterial) materialData).getFacing();
			movedBlock = event.getBlock().getRelative(blockFace, 2);
			
			if (event.isSticky() && movedBlock != null)
			{
				if (oreGinMan.machineExistsAt(movedBlock.getLocation())
						|| oreGinMan.oreGinLightExistsAt(movedBlock.getLocation()))
				{
					event.setCancelled(true);
				}
			}
		}
	}

	/**
	 * Stop entities from exploding/destroying OreGin
	 */
	@EventHandler
	public void oreGinExploded(EntityExplodeEvent event)
	{
		List<Block> destroyedBlocks = event.blockList();
		List<Block> cancelBlocks = new ArrayList<Block>();
		
		for (Block destroyedBlock : destroyedBlocks)
		{
			if (oreGinMan.machineExistsAt(destroyedBlock.getLocation())
					|| oreGinMan.oreGinLightExistsAt(destroyedBlock.getLocation()))
			{
				destroyedBlock.getDrops().clear();
				cancelBlocks.add(destroyedBlock);
			}
		}
		
		for (Block cancelBlock : cancelBlocks)
		{
			event.blockList().remove(cancelBlock);
		}
	}
}
