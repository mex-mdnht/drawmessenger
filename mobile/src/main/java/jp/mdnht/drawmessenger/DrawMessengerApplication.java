package jp.mdnht.drawmessenger;

import android.app.Application;

import com.parse.Parse;
import com.parse.PushService;

/**
 * Created by naohito on 2014/08/28.
 */
public class DrawMessengerApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "oLUbEltAQXiR5SpY6ByxKlAHgD4AImGacOTa6QNw", "NhAVSxAbP07XCLNXpUEhcWHkUT0ay4xJxgtBYRTY");
        PushService.setDefaultPushCallback(this, MockActivity.class);
        PushService.subscribe(this, "M5S", MockActivity.class);
    }
}
