package com.github.MrTwiggy.MachineFactory.Listeners;

import org.bukkit.event.Listener;

import com.github.MrTwiggy.MachineFactory.Managers.SmelterManager;


/**
 * SmelterListener.java
 * Purpose: Listens for Smelter related events
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class SmelterListener implements Listener
{
	
	SmelterManager smelterMan; //The SmelterManager object
	
	/**
	 * Constructor
	 */
	public SmelterListener(SmelterManager smelterMan)
	{
		this.smelterMan = smelterMan;
	}

}
