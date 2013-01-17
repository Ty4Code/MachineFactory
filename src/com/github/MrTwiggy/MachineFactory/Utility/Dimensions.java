package com.github.MrTwiggy.MachineFactory.Utility;

/**
 * Dimensions.java
 * Purpose: Stores dimensions for machine objects
 *
 * @author MrTwiggy
 * @version 0.1 1/14/13
 */
public class Dimensions 
{
	private int xLength;
	private int yLength;
	private int zLength;
	
	/**
	 * Constructor
	 */
	public Dimensions(int xLength, int yLength, int zLength)
	{
		this.xLength = xLength;
		this.yLength = yLength;
		this.zLength = zLength;
	}

	/**
	 * 'xLength' public accessor
	 */
	public int getXLength()
	{
		return xLength;
	}
	
	/**
	 * 'yLength' public accessor
	 */
	public int getYLength()
	{
		return yLength;
	}
	
	/**
	 * 'zLength' public accessor
	 */
	public int getZLength()
	{
		return zLength;
	}
	
}
