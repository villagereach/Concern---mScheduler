package net.rhinox.api.client.cliniccal.calendar.android;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/21/11
 * Time: 5:24 PM
 */


public class AppointmentListActivity extends ListActivity
{
    Level LOGGING_LEVEL = Level.OFF;

    private final int MENU_ADD = 0;
    private final int MENU_REFRESH = 3;

    private final int CONTEXT_EDIT = 1;
    private final int CONTEXT_DELETE = 2;

    private boolean showAllDates = false;

    private final int REQUEST_EDIT = 0xBEEF;
    private final int REQUEST_ADD = 0xF00D;

    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCEL = 0;

    private ProgressBar progress = null;

    static StorageManager storageManager;
    private Date selectedDate;
    Appointment[] appointments;

    @Override
    public void onCreate(Bundle state)
    {
        super.onCreate(state);
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        setupUi();
    }

    private void setupUi()
    {

        setContentView(R.layout.appointment_list);
        registerForContextMenu(getListView());

        selectedDate = new Date(getIntent().getLongExtra("selectedDate", new Date().getTime()));
        showAllDates = getIntent().getBooleanExtra("showAllAppointments", false);

        Button add = (Button)findViewById(R.id.appointmentListAdd);

        if( !showAllDates )
        {

            //add.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(android.R.drawable.ic_menu_add), null, null, null);
            //add.setPadding(0, 0, 20, 0);

            add.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    AppointmentListActivity.this.addAppointment();
                }
            });

            setTitle(String.format(getString(R.string.appointment_list_act_title_format) + " %s-%s-%s", selectedDate.getDate(), selectedDate.getMonth() + 1, selectedDate.getYear() + 1900));
        }
        else
        {
            LinearLayout l = (LinearLayout)findViewById(R.id.apptListRoot);

            if( null != add )
            {
                l.removeView(add);
            }

            setTitle("All Appointments");
        }

        executeRefreshEvents(showAllDates);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        if( !showAllDates )
        {
            menu.add(Menu.NONE, MENU_ADD, Menu.NONE, "Add").setIcon(android.R.drawable.ic_menu_add);
        }

        menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, "Refresh").setIcon(R.drawable.ic_menu_refresh);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

 //only one dialog to worry about
        if( AddEditEventActivity.RESULT_CANCEL == resultCode )
        {
            return;
        }

        Appointment appt = AddEditEventActivity.getAppointment();

        switch( requestCode )
        {
            case AddEditEventActivity.REQUEST_ADD:

                Log.d("AddEditEventActivity", "Event created.");

                storageManager.addAppointment(appt, true);

                break;

            case AddEditEventActivity.REQUEST_EDIT:

                storageManager.updateAppointment(appt);
                break;
        }

        executeRefreshEvents(showAllDates);

    }

    protected void executeRefreshEvents(boolean showAllAppointments )
    {
        if( null != storageManager )
        {
            Date min = new Date(selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDate());


            Calendar c = Calendar.getInstance();

            c.set(selectedDate.getYear(), selectedDate.getMonth(), selectedDate.getDate());
            c.add(Calendar.DAY_OF_YEAR, 1);

            Date max = c.getTime();

            max.setYear(max.getYear() + 1900);
            max.setHours(0);
            max.setMinutes(0);
            max.setSeconds(0);


            String where = "ACTION <> 'D'";

            if( !showAllAppointments )
            {
                where += " AND DateTime > " + min.getTime() + " AND DateTime < " + max.getTime();
            }

            Appointment[] appts = storageManager.getAppointments(where);

            appointments = appts;

            if( null != appts )
            {
                for( Appointment a : appts )
                {
                    a.toStringIncludeDate = showAllAppointments;
                }

                setListAdapter(new ArrayAdapter<Appointment>(this, android.R.layout.simple_list_item_1, appts));

            }
            else
            {
                setListAdapter(new ArrayAdapter<Appointment>(this, android.R.layout.simple_expandable_list_item_1, new Appointment[0]));
                Toast.makeText(getBaseContext(), String.format( showAllAppointments ? getString(R.string.no_appointments) : getString(R.string.no_appointments, selectedDate.getDate(), selectedDate.getMonth(), selectedDate.getYear() + 1900)), 5000).show();
            }
        }
    }

    /**
    private void executeRefreshEvents(boolean queryNetwork)
    {
        final ProgressDialog dlg = ProgressDialog.show(AppointmentListActivity.this, "Loading", "Loading appointments...");
        final AppointmentListActivity act = this;


        final android.os.Handler updateListHandler = new android.os.Handler()
                {
                    @Override
                    public void handleMessage(Message ms)
                    {
                        if( null == feed )
                        {
                            return;
                        }

                        String[] titles = new String[feed.events.size()];

                        for( int i = 0; i < feed.events.size(); i++)
                        {
                            titles[i] = feed.events.get(i).title;
                        }

                        setListAdapter(new ArrayAdapter<String>(act, android.R.layout.simple_list_item_1, titles));
                    }
                };


        final android.os.Handler dismissDlgHandler = new android.os.Handler()
                    {
                        @Override
                        public void handleMessage(Message m)
                        {
                            dlg.dismiss();
                        }
                    };


        new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    feed = client.executeGetEventFeed(new CalendarUrl(calendar.getEventFeedLink()));
                    updateListHandler.sendEmptyMessage(0);

                } catch (IOException e)
                {
                    e.printStackTrace();

                }
                finally
                {

                    dismissDlgHandler.sendEmptyMessage(0);

                }

            }
        }).start();
    }

    */

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        //menu.add(0, MENU_ADD, 0, getString(R.string.add)).setIcon(android.R.drawable.ic_menu_add);
        menu.add(0, CONTEXT_EDIT, 0, getString(R.string.edit)).setIcon(android.R.drawable.ic_menu_edit);
        menu.add(0, CONTEXT_DELETE, 0, getString(R.string.delete)).setIcon(android.R.drawable.ic_menu_delete);
    }

     @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if( null == appointments )
        {
            return super.onContextItemSelected(item);
        }

        Appointment appt = appointments[((int) info.id)];


        switch (item.getItemId())
        {
            case CONTEXT_EDIT:
                editAppointment(appt);
                executeRefreshEvents(showAllDates);

                return true;
            case CONTEXT_DELETE:
                deleteAppointment(appt);
                executeRefreshEvents(showAllDates);
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    private void deleteAppointment(Appointment appt)
    {
        storageManager.deleteAppointment(appt, false);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch( item.getItemId() )
        {
            case CONTEXT_EDIT:
                editAppointment(appointments[item.getItemId()]);
                break;

            case CONTEXT_DELETE:
                break;
        }

        return true;
    }

    private void editAppointment(Appointment appt)
    {
        Intent i = new Intent(AppointmentListActivity.this, AddEditEventActivity.class);

        i.putExtra("selectedDate", selectedDate.getTime());
        AddEditEventActivity.setAppointment(appt);

        startActivityForResult(i, REQUEST_EDIT);
    }

    private void addAppointment()
    {
        Intent i = new Intent(AppointmentListActivity.this, AddEditEventActivity.class);

        i.putExtra("selectedDate", selectedDate.getTime());
        AddEditEventActivity.setAppointment(new Appointment());

        startActivityForResult(i, REQUEST_ADD);
    }

    public static void setStorage(StorageManager storageManager)
    {
        AppointmentListActivity.storageManager = storageManager;
    }
}
