package jp.mdnht.drawmessenger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static jp.mdnht.drawmessenger.common.CommonConstants.*;
import static jp.mdnht.drawmessenger.common.GeneralUtil.*;

public class ContanctSelectActivity extends Activity {

    private WearableListView mListView;
    private static  final String TAG = "ContanctSelectActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contanct_select);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mListView = (WearableListView) stub.findViewById(R.id.list_view);
                mListView.setAdapter(new ContactAdapter(getBaseContext()));
                mListView.setClickListener(new WearableListView.ClickListener() {
                    @Override
                    public void onClick(WearableListView.ViewHolder viewHolder) {
                        TextView v = (TextView) viewHolder.itemView.findViewById(R.id.textView);
                        LOGD(TAG,"onClick " + v.getText());
                    }

                    @Override
                    public void onTopEmptyRegionClick() {
                        LOGD(TAG, "onTopEmptyRegionClick");
                    }
                });
            }
        });
    }
}

class ContactAdapter extends WearableListView.Adapter
{
    private Context mContext;
    private LayoutInflater mInflater;
    private static  final String TAG = "ContactAdapter";

    ContactAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(
                this.mInflater.inflate(R.layout.list_view_item, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder, int i) {
        LOGD(TAG, "index: " + i);
        TextView tv = (TextView) viewHolder.itemView.findViewById(R.id.textView);
        tv.setText("index: " + i);

    }

    @Override
    public int getItemCount() {
        return 10;
    }
}
