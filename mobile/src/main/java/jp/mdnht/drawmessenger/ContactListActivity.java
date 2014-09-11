package jp.mdnht.drawmessenger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import static jp.mdnht.drawmessenger.common.CommonConstants.*;
import static jp.mdnht.drawmessenger.common.GeneralUtil.*;

import com.parse.ParseUser;


public class ContactListActivity extends Activity {

    private static  final String TAG = "ContactListActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            ParseUser.getCurrentUser().logOut();
            Intent startLoginActivityIntent = new Intent(this, LoginActivity.class);
            startActivity(startLoginActivityIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(ParseUser.getCurrentUser() != null)
        {
            LOGD(TAG,"UserFound");
        }else
        {
            LOGD(TAG,"UserNotFound");
            Intent startLoginActivityIntent = new Intent(this, LoginActivity.class);
            startActivity(startLoginActivityIntent);
        }
    }
}
