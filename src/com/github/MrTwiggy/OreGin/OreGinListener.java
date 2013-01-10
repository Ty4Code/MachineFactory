package com.github.MrTwiggy.OreGin;

import static com.untamedears.citadel.Utility.isReinforced;

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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
	 */
	@EventHandler
	public void OreGinInteraction(PlayerInteractEvent event)
	{
		Block clicked = event.getClickedBlock();
		Player creator = event.getPlayer();
		
		if (event.getAction().equals(Action.LEFT_CLICK_BLOCK))
		{
			if (clicked.getState() instanceof Dispenser)
			{
				//Create and/or upgrade OreGin
				if (creator.getItemInHand().getType().equals(OreGinPlugin.OREGIN_UPGRADE_WAND))
				{
					
					if (oreGinMan.OreGinExistsAt(clicked.getLocation()))
					{
						OreGin oreGin = oreGinMan.GetOreGin(clicked.getLocation());
						creator.sendMessage(oreGin.upgrade());
					}
					else
					{
						creator.sendMessage(oreGinMan.CreateOreGin(clicked.getLocation()));
					}
				} //Activate or de-activate OreGin
				else if (creator.getItemInHand().getType().equals(OreGinPlugin.OREGIN_ACTIVATION_WAND))
				{
					if (oreGinMan.OreGinExistsAt(clicked.getLocation()))
					{
						OreGin oreGin = oreGinMan.GetOreGin(clicked.getLocation());
						creator.sendMessage(oreGin.togglePower());
					}
				} //Repair OreGin
				else if (creator.getItemInHand().getType().equals(OreGinPlugin.OREGIN_REPAIR_WAND))
				{
					if (oreGinMan.OreGinExistsAt(clicked.getLocation()))
					{
						OreGin oreGin = oreGinMan.GetOreGin(clicked.getLocation());
						creator.sendMessage(oreGin.repair());
					}
				}
			}
		}
		

	}
	
	/**
	 * Checks to see if an OreGin (dispenser or light) is being destroyed (block broken)
	 */
	@EventHandler
	public void OreGinBroken(BlockBreakEvent event)
	{
		Block destroyed = event.getBlock();
		
		if ((destroyed.getState() instanceof Dispenser) || destroyed.getType().equals(OreGinPlugin.LIGHT_ON)
				|| destroyed.getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			if (oreGinMan.OreGinExistsAt(destroyed.getLocation()) || oreGinMan.OreGinLightExistsAt(destroyed.getLocation()))
			{
				OreGin oreGin = oreGinMan.GetOreGin(destroyed.getLocation());
				if (oreGinMan.OreGinLightExistsAt(destroyed.getLocation()))
					oreGin = oreGinMan.GetOreGin(destroyed.getRelative(BlockFace.DOWN).getLocation());
				
				if (!isReinforced(oreGin.getLocation().getBlock()) && 
					!isReinforced(oreGin.getLocation().getBlock().getRelative(BlockFace.UP)))
				{
					event.setCancelled(true);
					ItemStack dropItem = new ItemStack(Material.DISPENSER, 1);
					oreGin.destroyOreGin(dropItem);
					oreGinMan.oreGins.remove(oreGin);
				}
				else if (isReinforced(oreGin.getLocation().getBlock()) ||
					isReinforced(oreGin.getLocation().getBlock().getRelative(BlockFace.UP)))
				{
					event.setCancelled(true);
				}
			}
		}
	}
	
	/**
	 * Checks to see if an OreGin has been placed
	 */
	@EventHandler
	public void OreGinPlaced(BlockPlaceEvent event)
	{
		Block placed = event.getBlock();
		
		if (oreGinMan.IsOreGin(event.getItemInHand()))
		{
			if (OreGin.validOreGinCreationLocation(placed.getLocation()))
			{
				OreGin oreGin = new OreGin(placed.getLocation(), OreGin.getTierLevel(event.getItemInHand().getItemMeta().getDisplayName()), 
						OreGin.getBlockBreaksFromLore(event.getItemInHand().getItemMeta().getLore()), oreGinMan);
				oreGinMan.AddOreGin(oreGin);
				event.getPlayer().sendMessage(ChatColor.GREEN + "An OreGin of tier level " + oreGin.getTierLevel() + " was placed!");
			}
			else
			{
				OreGinSoundCollection.ErrorSound().playSound(placed.getLocation());
				event.getPlayer().sendMessage(ChatColor.RED + "Space above OreGin must be empty!");
				event.setCancelled(true);
			}
			
		}
	}

	/**
	 * Helps with organizing inventory
	 */
	@EventHandler 
	public void MovedOreGin(InventoryClickEvent event)
	{
		ItemStack cursorItem = event.getCursor();
		ItemStack slotItem = event.getCurrentItem();
		
		if (cursorItem.getType() == Material.DISPENSER)
		{
			if (slotItem.getType() == Material.DISPENSER)
			{
				if (oreGinMan.IsOreGin(cursorItem) || oreGinMan.IsOreGin(slotItem))
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
	public void KeepLightsStable(BlockRedstoneEvent event)
	{
		if (event.getBlock().getType().equals(OreGinPlugin.LIGHT_ON) || event.getBlock().getType().equals(OreGinPlugin.LIGHT_OFF))
		{
			if (oreGinMan.OreGinLightExistsAt(event.getBlock().getLocation()))
			{
				if (oreGinMan.GetOreGin(event.getBlock().getRelative(BlockFace.DOWN).getLocation()).getMining())
				{
					event.setNewCurrent(1);
				}
				else if (!oreGinMan.GetOreGin(event.getBlock().getRelative(BlockFace.DOWN).getLocation()).getBroken())
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
	public void OreGinDispense(BlockDispenseEvent event)
	{
		event.setCancelled(oreGinMan.OreGinExistsAt(event.getBlock().getLocation()));
	}


}
