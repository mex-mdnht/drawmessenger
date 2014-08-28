package jp.mdnht.drawmessenger;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;


public class MockActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener, NodeApi.NodeListener, DataApi.DataListener {

    private static final String TAG = "MockActivity";
    private static final String PUSH_CHANNEL = "M5S";

    public static final String ACTION_OPEN_WEAR_APP = "jp.mdnht.drawmessenger.OPEN_WEAR_APP";
    public static final String ACTION_SEND_NOTIFICATION = "jp.mdnht.drawmessenger.SEND_NOTIFICATION";

    /** Request code for launching the Intent to resolve Google Play services errors. */
    private static final int REQUEST_RESOLVE_ERROR = 1000;

    private static final String START_ACTIVITY_PATH = "/start-activity";
    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_KEY = "photo";
    private static final String COUNT_PATH = "/count";
    private static final String COUNT_KEY = "count";

    //google api
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_mock);


        mHandler = new Handler();


         mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mock, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSendNotificationClicked(View view) {
        LOGD(TAG,"sendNotificationClicked");
        createNotificationAndSend(BitmapFactory.decodeResource(getResources(),R.drawable.common_signin_btn_icon_dark));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        LOGD(TAG,"nnnnnnnnnnnnnnwwww intnt");
        super.onNewIntent(intent);
        String action = intent.getAction();
        if(action == ACTION_OPEN_WEAR_APP)
        {
            new StartWearableActivityTask().execute();
        }
        if(action == ACTION_SEND_NOTIFICATION)
        {
            String imageUrl = intent.getExtras().getString("url");
            new DownloadImageTask().execute(imageUrl);
        }
    }

    private void createNotificationAndSend(Bitmap imageBitmap){
        int notificationId = 001;

        //main notification
        NotificationCompat.Builder notifBulder = new NotificationCompat.Builder(this)
                .setContentTitle("メッセージ")
                .setContentText("ひらく")
                .setSmallIcon(R.drawable.common_signin_btn_icon_dark);

        //extender for page2
        WearableExtender extender2 = new WearableExtender()
                .setHintShowBackgroundOnly(true)
                .setBackground(imageBitmap);
        // Create second page notification
        Notification secondPageNotification = new NotificationCompat.Builder(this)
                .setContentTitle("pege2")
                .setContentText("test")
                .extend(extender2)
                .build();

        // Create an intent for the reply action
        Intent actionIntent = new Intent(this, MockActivity.class);
        actionIntent.setAction(ACTION_OPEN_WEAR_APP);
        PendingIntent actionPendingIntent =
                PendingIntent.getActivity(this, 0, actionIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        /*Intent actionIntent = new Intent(this, MessageReceiver.class);
        actionIntent.setAction("jp.mdnht.drawmessenger.OPEN_WEAR_APP");
        PendingIntent actionPendingIntent =
                PendingIntent.getBroadcast(this, 1, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);*/

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.common_signin_btn_icon_disabled_focus_light,"launch wear app", actionPendingIntent)
                        .build();


        // Create a WearableExtender to add functionality for wearables
        Notification notif =
                new WearableExtender()
                        .addPage(secondPageNotification)
                        .addAction(action)
                        .extend(notifBulder)
                        .build();






        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notif);
    }

    @Override
    public void onPeerConnected(final Node peer) {
        LOGD(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(final Node peer) {
        LOGD(TAG, "onPeerDisconnected: " + peer);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (IMAGE_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset photo = dataMapItem.getDataMap()
                            .getAsset(IMAGE_KEY);

                    final Bitmap bitmap = loadBitmapFromAsset(mGoogleApiClient, photo);
                    LOGD(TAG, bitmap.toString());

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    //save parse file
                    saveImageFile(byteArray);

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Setting background image..");
                            ImageView iv = (ImageView) findViewById(R.id.imageView);
                            //
                            // iv.setImageResource(R.drawable.common_signin_btn_icon_dark);
                            iv.setImageBitmap(bitmap);
                        }
                    });
                } else {
                    LOGD(TAG, "Unrecognized path: " + path);
                }
            }
        }
    }

    private void saveImageFile(byte[] data)
    {
        final ParseFile file = new ParseFile("image.bmp",data);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null)
                {}
                else{
                    LOGD(TAG,file.getUrl());
                    sendPush(file.getUrl());
                }
            }
        });
    }

    private void sendPush(String url)
    {
        ParsePush push = new ParsePush();
        push.setChannel(PUSH_CHANNEL);
        JSONObject data = new JSONObject();
        try {
            data.put("action","jp.mdnht.drawmessenger.CREATE_NOTIFICATION");
            data.put("url",url);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        push.setData(data);
        push.sendInBackground();
    }

    private class StartWearableActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            LOGD(TAG,"nnnnnnnnnnnnnnwwww dddddddddddddd");
            Collection<String> nodes = getNodes();
            LOGD(TAG,"node is"+nodes.toString());
            for (String node : nodes) {
                sendStartActivityMessage(node);
            }
            return null;
        }
    }

    private void sendStartActivityMessage(String node) {
        LOGD(TAG,"send start activity message");
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        }
                    }
                }
        );
    }

    /**
     * As simple wrapper around Log.d
     */
    private static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.d(tag, message);
        }
    }

    //GoogleApiClient
    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "Google API Client was connected");
        mResolvingError = false;
        //mStartActivityBtn.setEnabled(true);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGD(TAG, "Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;
            //mStartActivityBtn.setEnabled(false);
            //mSendPhotoBtn.setEnabled(false);
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived() A message from watch was received:" + messageEvent
                .getRequestId() + " " + messageEvent.getPath());
    }

    private Collection<String> getNodes() {
        LOGD(TAG,"nnnnnnodes gewt");
        HashSet <String>results= new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        LOGD(TAG,"nnnnnnodes2 gewt"+results.toString());
        return results;
    }

    /**
     * Extracts {@link android.graphics.Bitmap} data from the
     * {@link com.google.android.gms.wearable.Asset}
     */
    private Bitmap loadBitmapFromAsset(GoogleApiClient apiClient, Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }

        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                apiClient, asset).await().getInputStream();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }

        return BitmapFactory.decodeStream(assetInputStream);
    }

    /*private void createNotificationAndSend(Context context,Bitmap imageBitmap){
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
    }*/

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageTask() {

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
            createNotificationAndSend(result);
        }
    }
}
