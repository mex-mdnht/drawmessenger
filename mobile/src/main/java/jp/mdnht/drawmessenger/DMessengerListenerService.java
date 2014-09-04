package jp.mdnht.drawmessenger;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;

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
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static jp.mdnht.drawmessenger.common.CommonConstants.*;
import static jp.mdnht.drawmessenger.common.GeneralUtil.*;

public class DMessengerListenerService extends WearableListenerService {

    private static final String TAG = "DMessengerListener";



    private GoogleApiClient mGoogleApiClient;
    //private Handler mHandler;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOGD(TAG,"onStartCommand");
        if(intent != null) {
            String action = intent.getAction();
            if(action == ACTION_SEND_NOTIFICATION)
            {
                String imageUrl = intent.getExtras().getString("url");
                new DownloadImageTask().execute(imageUrl);
                //sendCreateNotificatuinMessage();
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendCreateNotificatuinMessage() {
        Collection<String> nodes = getNodes();
        for (String node : nodes) {
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
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                LOGD(TAG, path);
                if (SENDING_IMAGE_PATH.equals(path)) {
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
                }else if(RECEIVED_IMAGE_PATH.equals(path)){



                }else {
                    LOGD(TAG, "Unrecognized path: " + path);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //mHandler = new Handler();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
    }


    private Collection<String> getNodes() {
        HashSet<String> results= new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        return results;
    }

    private static Asset toAsset(Bitmap bitmap) {
        ByteArrayOutputStream byteStream = null;
        try {
            byteStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            return Asset.createFromBytes(byteStream.toByteArray());
        } finally {
            if (null != byteStream) {
                try {
                    byteStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
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
            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
            data.put("fromId", installation.getInstallationId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        push.setData(data);
        push.sendInBackground();
    }

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
            PutDataMapRequest dataMapReq = PutDataMapRequest.create(RECEIVED_IMAGE_PATH);
            dataMapReq.getDataMap().putAsset(IMAGE_KEY, toAsset(result));
            dataMapReq.getDataMap().putLong("time", new Date().getTime());
            PutDataRequest request = dataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                    .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                        @Override
                        public void onResult(DataApi.DataItemResult dataItemResult) {
                            LOGD(TAG, "Sending image was successful: " + dataItemResult.getStatus()
                                    .isSuccess());
                        }
                    });
        }
    }
}
