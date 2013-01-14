package com.github.MrTwiggy.MachineFactory.SoundCollections;

import java.util.ArrayList;
import java.util.List;

import com.github.MrTwiggy.MachineFactory.Sound;

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
	public static Sound getPowerOnSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("mob.slime.small3");
		soundNames.add("mob.wither.shoot");
		soundNames.add("mob.skeleton.hurt2");
		soundNames.add("mob.silverfish.kill");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Power Off Sound
	 */
	public static Sound getPowerOffSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("note.bass");
		soundNames.add("fireworks.blast");
		soundNames.add("mob.blaze.hit2");
		soundNames.add("random.anvil_land");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Creation Sound
	 */
	public static Sound getCreationSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("fireworks.blast");
		soundNames.add("random.burp");
		soundNames.add("mob.irongolem.death");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Destruction Sound
	 */
	public static Sound getDestructionSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("mob.skeleton.death");
		soundNames.add("mob.slime.big3");
		soundNames.add("mob.silverfish.say4");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Upgrade Sound
	 */
	public static Sound getUpgradeSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.levelup");
		soundNames.add("fire.ignite");
		soundNames.add("fire.fire");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Mining Sound
	 */
	public static Sound getMiningSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.break");
		soundNames.add("random.click");
		soundNames.add("tile.piston.out");
		soundNames.add("mob.wolf.shake");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Broken Sound
	 */
	public static Sound getBrokenSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("note.pling");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Placement Sound
	 */
	public static Sound getPlacementSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.anvil_land");
		soundNames.add("dig.snow1");
		soundNames.add("random.click");
		soundNames.add("random.eat1");
		
		return new Sound(soundNames);
	}

	/**
	 * Repair Sound
	 */
	public static Sound getRepairSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.eat1");
		
		return new Sound(soundNames);
	}
	
	/**
	 * Error Sound
	 */
	public static Sound getErrorSound()
	{
		List<String> soundNames = new ArrayList<String>();
		soundNames.add("random.anvil_land");
		
		return new Sound(soundNames);
	}
}
