package com.drivestrive.utils;

/**
 * 
 * @author Strive
 * 
 * This class is responsible of providing key values to game
 * 
 */
public class Session {
	// private static double mGyroValue = new Utils().random(1.00, 4.99);
	// private static double mAccerlValue = new Utils().random(2.00, 5.99);
	// private static double mGpsValue = new Utils().random(0.00, 0.00);

	public static double getGyroValue() {
		double mGyroValue = new Utils().random(1.00, 5.99);
		return mGyroValue * 10;
	}

	public static double getGpsValue() {
		double mGpsValue = new Utils().random(0.00, 10.00);
		return mGpsValue;
	}

	public static double getAccerlValue() {
		double mAccerlValue = new Utils().random(1.00, 5.99);
		return Math.round((mAccerlValue * 10) * 100.0) / 100.0;
		// return mAccerlValue * 10;
	}

}
