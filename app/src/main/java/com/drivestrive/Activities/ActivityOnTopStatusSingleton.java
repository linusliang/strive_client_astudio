package com.drivestrive.Activities;

class ActivityOnTopStatusSingleton{
	private static ActivityOnTopStatusSingleton instance= new ActivityOnTopStatusSingleton();
	public Boolean isActivityOnTop = false;
	public Boolean isRed = false;

	public static ActivityOnTopStatusSingleton getInstance(){
		return instance;
	}
}