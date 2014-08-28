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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class MessageReceiver extends BroadcastReceiver {
    public MessageReceiver() {
    }
    private static final String TAG = "MessageReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

            try {

                Bundle b = intent.getExtras();
                String channel = b.getString("com.parse.Channel");
                JSONObject json = new JSONObject(b.getString("com.parse.Data"));

                Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
                String imageUrl = null;
                imageUrl = json.getString("url");
                /*DownloadImageTask loadImageTask= new DownloadImageTask(context);
                loadImageTask.doInBackground(imageUrl);*/
                Intent aintent = new Intent(context.getApplicationContext(), MockActivity.class);
                aintent.setAction(MockActivity.ACTION_SEND_NOTIFICATION);
                aintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                aintent.putExtra("url", imageUrl);
                context.startActivity(aintent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

    }

    private void createNotificationAndSend(Context context,Bitmap imageBitmap){
        int notificationId = 001;



        //main notification
        NotificationCompat.Builder notifBulder = new NotificationCompat.Builder(context)
                .setContentTitle("メッセージ")
                .setContentText("ひらく")
                .setSmallIcon(R.drawable.common_signin_btn_icon_dark);

        //extender for page2
        NotificationCompat.WearableExtender extender2 = new NotificationCompat.WearableExtender()
                .setHintShowBackgroundOnly(true)
                .setBackground(imageBitmap);
        // Create second page notification
        Notification secondPageNotification = new NotificationCompat.Builder(context)
                .setContentTitle("pege2")
                .setContentText("test")
                .extend(extender2)
                .build();

        // Create an intent for the reply action
        Intent actionIntent = new Intent(context, MockActivity.class);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(context, 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.common_signin_btn_icon_disabled_focus_light,"launch wear app", actionPendingIntent)
                        .build();


        // Create a WearableExtender to add functionality for wearables
        Notification notif =
                new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification)
                        .addAction(action)
                        .extend(notifBulder)
                        .build();

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notif);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Context mContext;

        public DownloadImageTask(Context context) {
            mContext = context;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bmp = null;

            InputStream in = null;
            try {
                in = new URL(urldisplay).openStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bmp = BitmapFactory.decodeStream(in);

            return bmp;
        }

        protected void onPostExecute(Bitmap result) {
            createNotificationAndSend(mContext, result);
        }
    }
}
