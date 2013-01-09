package com.github.MrTwiggy.OreGin;

import java.io.File;
import java.io.IOException;

/**
 * ManagerInterface.java
 * Purpose: Interface for Manager objects, used in loading/saving data
 *
 * @author MrTwiggy
 * @version 0.1 1/08/13
 */
public interface ManagerInterface 
{

	/**
	 * Save function
	 */
	public void save(File file) throws IOException;
	
	/**
	 * Load function
	 */
	public void load(File file) throws IOException;
	
}
