package com.drivestrive.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

//class to display OverloayDialog (This class is used to disable the home key by displaying an overlay dialog)
public class HomeKeyLocker {
    private OverlayDialog mOverlayDialog;

    /**
	 * @author: Anmol Chanana
	 * @name: lock
	 * @date: April 1,2014
	 * @param: Activity
	 * @description : lock the home key by displaying an overlay dialog.
	 */
    public void lock(Activity activity) {
        if (mOverlayDialog == null) {
            mOverlayDialog = new OverlayDialog(activity);
            mOverlayDialog.show();
        }
    }

    /**
	 * @author: Anmol Chanana
	 * @name: unlock
	 * @date: April 1,2014
	 * @param: null
	 * @description : unlock the home key by dismissing the dialog.
	 */
    public void unlock() {
        if (mOverlayDialog != null) {
            mOverlayDialog.dismiss();
            mOverlayDialog = null;
        }
    }

    /**
     * Class to display an overlay dialog over the screen so as to disable the home key..
     */
    private static class OverlayDialog extends AlertDialog {

    	// constructor of the overlay dialog 
        public OverlayDialog(Activity activity) {
            super(activity, R.style.OverlayDialog);
            // setting parameters of the overlay dialog..
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.type = TYPE_SYSTEM_ALERT;
            params.dimAmount = 0.0F; // adjusting transparency(0.0 fully transparent)
            params.width = 0;        //  setting width as zero 
            params.height = 0;		//	setting height as zero
            params.gravity = Gravity.BOTTOM;
            getWindow().setAttributes(params);
            getWindow().setFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_NOT_TOUCH_MODAL, 0xffffff);
            setOwnerActivity(activity);
            setCancelable(false);
        }

        /**
    	 * @author: Anmol Chanana
    	 * @name: dispatchTouchEvent
    	 * @date: April 1,2014
    	 * @param: Motion Event
    	 * @description : Returns touch event
    	 */
        public final boolean dispatchTouchEvent(MotionEvent motionevent) {
             
            return true;// return true for motion event..
        }

        /**
    	 * @author: Anmol Chanana
    	 * @name: onCreate
    	 * @date: April 1,2014
    	 * @param: Bundle
    	 * @description : Creates the OverLay Dialog..
    	 */
        protected final void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            FrameLayout framelayout = new FrameLayout(getContext());
            framelayout.setBackgroundColor(0);
            setContentView(framelayout);

        }
    }
}
