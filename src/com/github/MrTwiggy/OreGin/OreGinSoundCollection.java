package com.github.MrTwiggy.OreGin;

import java.util.ArrayList;
import java.util.List;

/**
 * OreGinSoundCollection.java
 * Purpose: Holds all the OreGin sounds
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class OreGinSoundCollection
{
	/**
	 * Power On Sound
	 */
	public static OreGinSound PowerOnSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("mob.slime.small3");
		soundNames.add("mob.wither.shoot");
		soundNames.add("mob.skeleton.hurt2");
		soundNames.add("mob.silverfish.kill");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Power Off Sound
	 */
	public static OreGinSound PowerOffSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("note.pling");
		soundNames.add("note.bass");
		soundNames.add("fireworks.blast");
		soundNames.add("mob.blaze.hit2");
		soundNames.add("random.anvil_land");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Creation Sound
	 */
	public static OreGinSound CreationSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("fireworks.blast");
		soundNames.add("random.burp");
		soundNames.add("mob.irongolem.death");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Destruction Sound
	 */
	public static OreGinSound DestructionSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("mob.skeleton.death");
		soundNames.add("mob.slime.big3");
		soundNames.add("mob.silverfish.say4");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Upgrade Sound
	 */
	public static OreGinSound UpgradeSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.levelup");
		soundNames.add("fire.ignite");
		soundNames.add("fire.fire");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Mining Sound
	 */
	public static OreGinSound MiningSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.break");
		soundNames.add("random.click");
		soundNames.add("tile.piston.in");
		soundNames.add("mob.wolf.shake");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Broken Sound
	 */
	public static OreGinSound BrokenSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("note.pling");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Placement Sound
	 */
	public static OreGinSound PlacementSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.anvil_land");
		soundNames.add("dig.snow1");
		soundNames.add("random.click");
		soundNames.add("random.eat1");
		
		return new OreGinSound(soundNames);
	}

	/**
	 * Repair Sound
	 */
	public static OreGinSound RepairSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.eat1");
		
		return new OreGinSound(soundNames);
	}
	
	/**
	 * Error Sound
	 */
	public static OreGinSound ErrorSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.anvil_land");
		
		return new OreGinSound(soundNames);
	}
}
