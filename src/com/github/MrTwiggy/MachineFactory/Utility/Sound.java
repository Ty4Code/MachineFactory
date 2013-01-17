package com.github.MrTwiggy.MachineFactory.Utility;

import java.util.List;

import net.minecraft.server.v1_4_6.Packet62NamedSoundEffect;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Sound.java
 * Purpose: An object for storing sounds
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public class Sound 
{
	public final int SOUND_PLAY_DISTANCE = 20; //The maximum distance for sound packet to be sent to player
	
	List<String> sounds; //List of sound effect names
	float volume; //The volume to be played at
	float pitch; //The pitch to be played at
	
	/**
	 * Constructor
	 */
	public Sound(List<String> sounds, float volume, float pitch)
	{
		this.sounds = sounds;
		this.volume = volume;
		this.pitch = pitch;
	}
	
	/**
	 * Constructor
	 */
	public Sound(List<String> sounds)
	{
		this.sounds = sounds;
		this.volume = 1.0f;
		this.pitch = 1.0f;
	}
	
	/**
	 * Play this sound
	 */
	public void playSound(Location location)
	{
		for (String sound : sounds)
		{
			Packet62NamedSoundEffect soundPacket = new Packet62NamedSoundEffect(sound,
					location.getX(), location.getY(), location.getZ(), volume, pitch);
			
			if (soundPacket != null)
			{
				for (Player player : location.getWorld().getPlayers())
				{
					if (location.distance(player.getLocation()) <= SOUND_PLAY_DISTANCE)
						((CraftPlayer)player).getHandle().playerConnection.sendPacket(soundPacket);
				}
			}
		}
	}
	
	
}
