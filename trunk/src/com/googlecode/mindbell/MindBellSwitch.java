package com.googlecode.mindbell;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * When triggered, turn mindbell altogether on or off depending on settings.
 * This is triggered in two circumstances:
 * <ul>
 * <li>when leaving the preferences dialogue</li>
 * <li>when the phone has finished booting</li>
 * </ul>
 * @author marc
 *
 */
public class MindBellSwitch extends BroadcastReceiver {
	private Context theContext;
	private NotificationManager theNotificationManager;
	private AlarmManager theAlarmManager;
	private SharedPreferences settings; 
	private String[] hours;

	@Override
	public void onReceive(Context context, Intent intent) {
		theContext = context;
        theAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        theNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(context);
        hours = context.getResources().getStringArray(R.array.hourStrings);
        
        deactivateBell(); // always cancel anything using any previous settings first
    	boolean isChecked = settings.getBoolean(context.getString(R.string.keyActive), false);
    	if (isChecked) {
    		activateBell();
    	}
	}

	
	
    private void activateBell() {
    	Intent intent = new Intent(theContext, MindBellScheduler.class);
    	intent.putExtra(MindBellPreferences.ACTIVATEBELL, true);
    	intent.putExtra(MindBellPreferences.RESCHEDULEBELL, true);
    	PendingIntent sender = PendingIntent.getBroadcast(theContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    	// Now determine if we are in "daytime"
		int tStart = getDaytimeStart();
    	if (isDaytime()) {
    		// start scheduler now:
        	try {
        		sender.send();
        	} catch (PendingIntent.CanceledException e) {
        		Log.e(MindBellPreferences.LOGTAG, "Could not send: "+e.getMessage());
        	}
    	} else {
    		String startTime = getDaytimeStartString();
    		Toast.makeText(theContext, "it is nighttime, MindBell will start at "+startTime, Toast.LENGTH_SHORT).show();
    		Log.d(MindBellPreferences.LOGTAG, "it is nighttime, MindBell will start at "+startTime);
    		// program start of scheduler for next morning
    		Calendar morning = Calendar.getInstance();
    		int currentHour = morning.get(Calendar.HOUR_OF_DAY);
    		int currentMinute = morning.get(Calendar.MINUTE);
    		int currentTime = 100*currentHour + currentMinute;
    		if (currentTime < tStart) {
    			// already right day
    		} else {
    			// need to add one day
    			morning.add(Calendar.DATE, 1);
    		}
    		morning.set(Calendar.HOUR_OF_DAY, tStart/100);
    		morning.set(Calendar.MINUTE, tStart%100);
    		morning.set(Calendar.SECOND, 0);
    		theAlarmManager.set(AlarmManager.RTC_WAKEUP, morning.getTimeInMillis(), sender);
    	}
        Notification notif = new Notification(R.drawable.bell_status_active, "", System.currentTimeMillis());
        CharSequence contentTitle = theContext.getText(R.string.statusTitleBellActive);
        String contentText = theContext.getText(R.string.statusTextBellActive).toString();
        contentText = contentText.replace("_STARTTIME_", getDaytimeStartString())
        	.replace("_ENDTIME_", getDaytimeEndString());
        Intent notificationIntent = new Intent(theContext, MindBellPreferences.class);
        PendingIntent contentIntent = PendingIntent.getActivity(theContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notif.setLatestEventInfo(theContext.getApplicationContext(), contentTitle, contentText, contentIntent);
        notif.flags |= Notification.FLAG_ONGOING_EVENT;
        int id = R.layout.bell; // unique ID
        theNotificationManager.notify(id, notif);
        
    }
    
    private void deactivateBell() {
    	// Day or night, we cancel the next message in stock for the scheduler.
    	Intent intent = new Intent(theContext, MindBellScheduler.class);
    	PendingIntent sender = PendingIntent.getBroadcast(theContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		theAlarmManager.cancel(sender); // filterEquals() matches irrespective of extras
		// Now we send a "deactivate" message to the scheduler irrespective of day- or nighttime
		// because the boundaries of day and night may have been changed, and even if we think it is night,
		// the scheduler may still be active.
		intent.putExtra(MindBellPreferences.ACTIVATEBELL, false);
    	intent.putExtra(MindBellPreferences.RESCHEDULEBELL, false);
    	sender = PendingIntent.getBroadcast(theContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    	try {
    		sender.send();
    	} catch (PendingIntent.CanceledException e) {
    		Log.e(MindBellPreferences.LOGTAG, "Could not send: "+e.getMessage());
    	}
    	// And finally we switch off the notification
    	int id = R.layout.bell; // unique ID
        theNotificationManager.cancel(id);
    }
    
    private boolean isDaytime() {
    	int tStart = getDaytimeStart();
    	int tEnd = getDaytimeEnd();
    	// Some people may set the end to midnight, or 1am etc.
    	// -- in such cases, simply consider the end time to be 24:00, 25:00 etc.
    	if (tEnd <= tStart) {
    		tEnd += 2400;
    	}
    	assert tEnd > tStart;
    	Calendar now = Calendar.getInstance();
    	int currentHour = now.get(Calendar.HOUR_OF_DAY);
    	int currentMinute = now.get(Calendar.MINUTE);
    	int currentTime = 100*currentHour + currentMinute;
    	return (tStart <= currentTime && currentTime < tEnd);
    }


	private int getDaytimeStart() {
		return 100 * Integer.valueOf(settings.getString(theContext.getString(R.string.keyStart), "0"));
	}
	
	private String getDaytimeStartString() {
		int startStringIndex = Integer.valueOf(settings.getString(theContext.getString(R.string.keyStart), "0"));
		return hours[startStringIndex];
	}
	
	private int getDaytimeEnd() {
		return 100 * Integer.valueOf(settings.getString(theContext.getString(R.string.keyEnd), "0"));
	}

	private String getDaytimeEndString() {
		int endStringIndex = Integer.valueOf(settings.getString(theContext.getString(R.string.keyEnd), "0"));
		return hours[endStringIndex];
	}

}
