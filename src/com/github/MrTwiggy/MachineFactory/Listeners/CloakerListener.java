package com.github.MrTwiggy.MachineFactory.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
import com.github.MrTwiggy.MachineFactory.Machines.Cloaker;
import com.github.MrTwiggy.MachineFactory.Managers.CloakerManager;
import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;

/**
 * CloakerListener.java
 * Purpose: Listener for Cloaker related events
 *
 * @author MrTwiggy
 * @version 0.1 1/17/13
 */
public class CloakerListener implements Listener
{
	
	CloakerManager cloakerMan; // Cloaker Manager object
	
	/**
	 * Constructor
	 */
	public CloakerListener(CloakerManager cloakerMan)
	{
		this.cloakerMan = cloakerMan;
	}
	
	/**
	 * Event for creating/upgrading Cloakers and opening Cloaker Inventory.
	 */
	@EventHandler
	public void cloakerInteraction(PlayerInteractEvent event)
	{
		if (event.getClickedBlock() == null)
		{
			return;
		}
		
		Block clicked = event.getClickedBlock();
		Material type = clicked.getType();
		Player player = event.getPlayer();
		Action action = event.getAction();
		Material inHand = player.getItemInHand().getType();
		
		if (type.equals(MachineFactoryPlugin.CLOAKER_ACTIVATED) 
				|| type.equals(MachineFactoryPlugin.CLOAKER_DEACTIVATED))
		{
			switch (action)
			{
				case RIGHT_CLICK_BLOCK:
					if (cloakerMan.machineExistsAt(clicked.getLocation()))
					{
						Cloaker cloaker = (Cloaker)cloakerMan.getMachine(clicked.getLocation());
						cloaker.openInventory(player);
						event.setCancelled(true);
					}
					break;
				case LEFT_CLICK_BLOCK:
					if (!cloakerMan.machineExistsAt(clicked.getLocation()))
					{
						if (inHand.equals(MachineFactoryPlugin.CLOAKER_UPGRADE_WAND))
						{
							InteractionResponse.messagePlayerResult(player, cloakerMan.createMachine(clicked.getLocation()));
							event.setCancelled(true);
						}
					}
					else
					{
						if (inHand.equals(MachineFactoryPlugin.CLOAKER_UPGRADE_WAND))
						{
							Cloaker cloaker = (Cloaker)cloakerMan.getMachine(clicked.getLocation());
							InteractionResponse.messagePlayerResult(player, cloaker.upgrade());
							event.setCancelled(true);
						}
						else if (inHand.equals(MachineFactoryPlugin.CLOAKER_ACTIVATION_WAND))
						{
							Cloaker cloaker = (Cloaker)cloakerMan.getMachine(clicked.getLocation());
							InteractionResponse.messagePlayerResult(player, cloaker.togglePower());
							event.setCancelled(true);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Event for destroying a Cloaker
	 */
	@EventHandler
	public void cloakerBroken(BlockBreakEvent event)
	{
		Block destroyed = event.getBlock();
		
		if (Cloaker.isCloakerType(destroyed.getType()))
		{
			if (cloakerMan.machineExistsAt(destroyed.getLocation()))
			{
				Cloaker cloaker = (Cloaker)cloakerMan.getMachine(destroyed.getLocation());
				
				event.setCancelled(true);
				ItemStack dropItem = new ItemStack(MachineFactoryPlugin.CLOAKER_DEACTIVATED, 1);
				cloaker.destroy(dropItem);
				cloakerMan.removeMachine(cloaker);
			}
		}
	}
	
	/**
	 * Event for placing a Cloaker
	 */
	@EventHandler
	public void cloakerPlaced(BlockPlaceEvent event)
	{
		Block placed = event.getBlock();
		
		if (cloakerMan.isCloaker(event.getItemInHand()))
		{
			Cloaker cloaker = new Cloaker(Cloaker.getTierLevel(
					event.getItemInHand().getItemMeta().getDisplayName()),placed.getLocation());
			
			InteractionResponse.messagePlayerResult(event.getPlayer(), cloakerMan.addMachine(cloaker));
		}
	}
	
}
