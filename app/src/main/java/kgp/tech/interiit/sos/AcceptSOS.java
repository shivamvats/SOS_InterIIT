package kgp.tech.interiit.sos;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kgp.tech.interiit.sos.Utils.DateFormater;
import kgp.tech.interiit.sos.Utils.Utils;

public class AcceptSOS extends AppCompatActivity {

    private String SOSId;
    private String channelId;
    private String senderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_sos);
        Intent intent = getIntent();
        try {
            JSONObject data = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            SOSId = data.getString("sosId");
            senderId = data.getString("username");
            channelId = data.getString("chatChannel");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void action_accept_sos(View v)
    {
        ParseQuery<ParseObject> pq = ParseQuery.getQuery("SOS_Users");
        pq.include("SOSid");
        pq.include("SOSid.UserID");
        pq.whereEqualTo("UserID", ParseUser.getCurrentUser());
        ParseObject sos = new ParseObject("SOS");

        sos.setObjectId(SOSId);
        pq.whereEqualTo("SOSid",sos);
        final ProgressDialog dia = ProgressDialog.show(AcceptSOS.this, null, getString(R.string.alert_wait));
        pq.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e!=null)
                {
                    dia.dismiss();
                    Log.d("Sos","Lost");
                    e.printStackTrace();
                    return;
                }

                final ParseObject sos = parseObject.getParseObject("SOSid");
                final ParseUser user = sos.getParseUser("UserID");

                ParseObject pop = new ParseObject("SOS_Users");
                pop.put("hasAccepted", true);
                Log.d("AcceptedSOS","ID "+parseObject.getObjectId());
                parseObject.put("hasAccepted", true);
                parseObject.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e!=null)
                        {
                            dia.dismiss();
                            Utils.showDialog(AcceptSOS.this,e.getMessage());
                            e.printStackTrace();
                            return;
                        }
                        dia.dismiss();
                        Log.d("AcceptedSOS", "Saved");
                        Intent intent = new Intent(AcceptSOS.this, MessageActivity.class);
                        intent.putExtra("createdAt", DateFormater.formatTimeDate(sos.getCreatedAt()));
                        intent.putExtra("channelID", sos.getString("channelID"));
                        intent.putExtra("username", user.getUsername());
                        intent.putExtra("Description", sos.getString("Description"));
                        Log.d("",sos.getString("Description"));
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
    }

    public void action_reject_sos(final View v)
    {
        Utils.showDialog(this, getString(R.string.please_help), R.string.save, R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        Intent intent = new Intent(AcceptSOS.this, HomeActivity.class);
                        startActivity(intent);
                        finish();

                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        action_accept_sos(v);
                        //TODO call the cloud service and make it check if contact uses the app
                        break;
                }
                return;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accept_so, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
