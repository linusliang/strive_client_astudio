package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.drivestrive.Activities.Main_locker;


public class LockReceiver extends BroadcastReceiver  {
	 public static boolean wasScreenOn = true;

	@Override
	/**
	 * @author: Anmol Chanana
	 * @name: onReceive
	 * @date: Mar 26,2014
	 * @param: context,intent
	 */
	public void onReceive(Context context, Intent intent) {

		
       if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
       	

       	wasScreenOn=false;
       	// launches the lock screen when the screen is off..
       	Intent intent11 = new Intent(context,Main_locker.class);
       	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

       	context.startActivity(intent11);

         
       } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {

       	wasScreenOn=true;
        // launches the lock screen when the screen is on..
       	Intent intent11 = new Intent(context,Main_locker.class);
       	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

       
       }
      else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
       {

    	  //THIS SHOULD BE HANDLED BY SpeedTrackingService
      	// launches the lock screen when the Boot Completed..
//       	Intent intent11 = new Intent(context, Main_locker.class);
//       	intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//          context.startActivity(intent11);

      }

   }

}
