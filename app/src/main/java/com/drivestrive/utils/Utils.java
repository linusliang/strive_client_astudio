package com.drivestrive.utils;

/**
 * 
 * @author master software solutions
 * 
 *         This class is responsible of calculating key values
 * 
 */
public class Utils {

	public double random(double min, double max) {
		double diff = max - min;
		return Math.round((min + Math.random() * diff) * 100.0 / 100.0);
	}
}
