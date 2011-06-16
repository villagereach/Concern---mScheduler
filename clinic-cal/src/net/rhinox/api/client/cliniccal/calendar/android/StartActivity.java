package net.rhinox.api.client.cliniccal.calendar.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/25/11
 * Time: 7:16 PM
 */
public class StartActivity extends Activity
{
    private final static int MENU_PREFS = 0;
    private final static int MENU_CLEAR = 1;

    StorageManager storageManager = new StorageManager();


    private final static int DIALOG_SELECT_DATE = 0;
    private final static int DIALOG_SYNC = 1;


    @Override
    protected void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        setContentView(R.layout.start_activity);

        setupUi(bundle);

        storageManager.open(this);
        AppointmentListActivity.setStorage(storageManager);
        SyncDlg.setStorageManager(storageManager);

        setOrientation(getResources().getConfiguration());

        checkPrefs();

    }

    private void checkPrefs()
    {
        if( 0 == PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.pref_name_clinic), "").length() || 0 == PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.pref_name_calendar), "").length() )
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("You must set a valid clinic name and calendar name (ex: joe@gmail.com) before the first use.").setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialogInterface, int i)
                {

                }
            }).show();

        }
    }

    private void setOrientation(Configuration cfg)
    {
         LinearLayout l = (LinearLayout)findViewById( R.id.startRoot );

        l.setOrientation( Configuration.ORIENTATION_LANDSCAPE == cfg.orientation ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);

//        l = (LinearLayout)findViewById(R.id.startBottomLayout);
//        l.setOrientation( Configuration.ORIENTATION_LANDSCAPE == cfg.orientation ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
    }

    private void setupUi(final Bundle args)
    {
        Button select = (Button)findViewById(R.id.selectDate);

        select.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(android.R.drawable.ic_menu_add), null, null);
        select.setPadding(0, 20, 0, 20);

        select.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent i = new Intent(StartActivity.this, SelectDateDlg.class);
                startActivityForResult(i, DIALOG_SELECT_DATE);
            }
        });

        Button update = (Button)findViewById(R.id.sync);

        update.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(android.R.drawable.ic_menu_upload), null, null);
        update.setPadding(0, 20, 0, 20);

        update.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent i = new Intent(StartActivity.this, SyncDlg.class);
                startActivityForResult(i, DIALOG_SYNC);

                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString(getString(R.string.pref_name_last_update), new Date().toLocaleString()).commit();
                setLastUpdate();
            }
        });

        setLastUpdate();
    }

    private void setLastUpdate()
    {
        String lastUpdate = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.pref_name_last_update), "");

        if( 0 != lastUpdate.length())
        {
            TextView tv = (TextView)findViewById(R.id.startLastUpdate);

            if( null != tv )
            {
                tv.setText(String.format(getString(R.string.last_update_format), lastUpdate));
            }
        }

    }


    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data)
    {

        if( DIALOG_SELECT_DATE != requestCode )
        {
            return;
        }

        if( SelectDateDlg.RESULT_CANCEL == resultCode )
        {
            return;
        }

        Intent i = new Intent(this, AppointmentListActivity.class);
        AppointmentListActivity.setStorage(storageManager);
        i.putExtra("selectedDate", data.getLongExtra("selectedDate", new Date().getTime()));
        i.putExtra("showAllAppointments", data.getBooleanExtra("showAllAppointments", false));
        startActivity(i);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(Menu.NONE, MENU_PREFS, Menu.NONE, "Settings").setIcon(android.R.drawable.ic_menu_preferences);
        menu.add(Menu.NONE, MENU_CLEAR, Menu.NONE, "Clear Local DB").setIcon(android.R.drawable.ic_menu_revert);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if( MENU_PREFS == item.getItemId() )
        {
            Intent i = new Intent(StartActivity.this, PreferencesActivity.class);
            startActivity(i);
        }
        else if( MENU_CLEAR == item.getItemId())
        {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        storageManager.deleteAllAppointments();

                        Toast.makeText(StartActivity.this.getBaseContext(), R.string.local_database_cleared, 3000);

                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("WARNING: This will clear all appointments from the device.\n\nAppointments on the server will not be modified.\n\nDo you want to continue?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
        }

        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration cfg)
    {
        super.onConfigurationChanged(cfg);
        setOrientation(cfg);

    }
}
