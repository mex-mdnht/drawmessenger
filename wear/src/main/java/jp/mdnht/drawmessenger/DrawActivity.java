package jp.mdnht.drawmessenger;

import android.app.Activity;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DrawActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, DataApi.DataListener, GoogleApiClient.OnConnectionFailedListener, NodeApi.NodeListener {
    private static  final String TAG = "DrawActivity";

    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_KEY = "photo";
    private static final String COUNT_PATH = "/count";
    private static final String COUNT_KEY = "count";
    private int count = 0;

    private DrawSurfaceView drawSurfaceView;
    private ProgressBar progressBar;
    private Button sendDataButton;

    private static final int EXIT_DELAY = 5000;
    private CountDownTimer timer = null;

    /** Request code for launching the Intent to resolve Google Play services errors. */
    private static final int REQUEST_RESOLVE_ERROR = 1000;
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {

                drawSurfaceView = (DrawSurfaceView) stub.findViewById(R.id.drawSurfaceView);
                progressBar = (ProgressBar) stub.findViewById(R.id.progressBar);
                progressBar.setMax(EXIT_DELAY);
                timer = new CountDownTimer(EXIT_DELAY, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        progressBar.setProgress((int)millisUntilFinished);
                    }

                    @Override
                    public void onFinish() {
                       // sendPhoto(toAsset(makeBitmapFromSurfaceView()));
                    }
                };
                timer.start();


                sendDataButton = (Button)findViewById(R.id.send_data_button);
                sendDataButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_plusone_medium_off_client);
                        Asset asset= toAsset(drawSurfaceView.getmBitmap());
                        //Asset asset= toAsset(bitmap);
                        sendPhoto(asset);
                    }
                });

            }
        });
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private Bitmap makeBitmapFromSurfaceView() {
        Bitmap b = Bitmap.createBitmap(270, 270, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        drawSurfaceView.draw(c);
        return b;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Sends the asset that was created form the photo we took by adding it to the Data Item store.
     */
    private void sendPhoto(Asset asset) {
        PutDataMapRequest dataMapReq = PutDataMapRequest.create(IMAGE_PATH);
        dataMapReq.getDataMap().putAsset(IMAGE_KEY, asset);
        dataMapReq.getDataMap().putLong("time", new Date().getTime());
        PutDataRequest request = dataMapReq.asPutDataRequest();
        LOGD(TAG, "Generating Asset Image : " + request);
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        LOGD(TAG, "Sending image was successful: " + dataItemResult.getStatus()
                                .isSuccess());

                       /* if (dataItemResult.getStatus().isSuccess()) {
                            finish();
                        }*/

                    }
                });

    }

    /**
     * As simple wrapper around Log.d
     */
    private static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.INFO)) {
            Log.d(tag, message);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "Google API Client was connected");
        mResolvingError = false;
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        //Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.NodeApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        LOGD(TAG, "Connection to Google API client was suspended");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        LOGD(TAG, "onDataChanged(): " + dataEvents);

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (COUNT_PATH.equals(path)) {
                    LOGD(TAG, "Count: " + count);
                } else if(IMAGE_PATH.equals(path))
                {
                    LOGD(TAG, "image");
                }
                else
                {
                    LOGD(TAG, "Unrecognized path: " + path);
                }

            }
        }
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
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            //Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            //Wearable.NodeApi.removeListener(mGoogleApiClient, this);
        }
    }

    /**
     * Builds an {@link com.google.android.gms.wearable.Asset} from a bitmap. The image that we get
     * back from the camera in "data" is a thumbnail size. Typically, your image should not exceed
     * 320x320 and if you want to have zoom and parallax effect in your app, limit the size of your
     * image to 640x400. Resize your image before transferring to your wearable device.
     */
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

    @Override
    public void onPeerConnected(final Node peer) {
        LOGD(TAG, "onPeerConnected: " + peer);
    }

    @Override
    public void onPeerDisconnected(final Node peer) {
        LOGD(TAG, "onPeerDisconnected: " + peer);
    }

    private Collection<String> getNodes() {
        LOGD(TAG,"nnnnnnodes gewt");
        HashSet<String> results= new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }
        LOGD(TAG,"nnnnnnodes2 gewt"+results.toString());
        return results;
    }
}
