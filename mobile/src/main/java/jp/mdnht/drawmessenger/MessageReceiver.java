package jp.mdnht.drawmessenger;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.ImageView;

import com.parse.ParseInstallation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import static jp.mdnht.drawmessenger.common.CommonConstants.*;

public class MessageReceiver extends BroadcastReceiver {
    public MessageReceiver() {
    }
    private static final String TAG = "MessageReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        Log.d(TAG, "instID " + installation.getInstallationId());
        String action = intent.getAction();
        Log.d(TAG, "onReceiveIntent" + action);
            try {

                Bundle b = intent.getExtras();
                String channel = b.getString("com.parse.Channel");



                JSONObject json = new JSONObject(b.getString("com.parse.Data"));

                Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                String imageUrl = null;
                imageUrl = json.getString("url");
                String fromId = json.getString("fromId");
                Log.d(TAG, "fromID " + fromId);
                if(fromId.equals(installation.getInstallationId())) {

                    Intent aintent = new Intent(context.getApplicationContext(), DMessengerListenerService.class);
                    aintent.setAction(ACTION_SEND_NOTIFICATION);
                    //aintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    aintent.putExtra("url", imageUrl);
                    //context.startActivity(aintent);
                    context.startService(aintent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

}
