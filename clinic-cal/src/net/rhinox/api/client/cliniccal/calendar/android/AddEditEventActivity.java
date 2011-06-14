package net.rhinox.api.client.cliniccal.calendar.android;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Date;


/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/21/11
 * Time: 8:35 PM
 */
public class AddEditEventActivity extends Activity
{

    public static final int REQUEST_EDIT = 0xBEEF;
    public static final int REQUEST_ADD = 0xF00D;

    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCEL = 0;

    private static Appointment event;

    private Date when;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_edit_event);

        Button ok = (Button)findViewById(R.id.okButton);
        Button cancel = (Button)findViewById(R.id.cancelButton);

        ((TimePicker)findViewById(R.id.whenTime)).setIs24HourView(true);

        updateData(false);

        ok.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                updateData(true);
                setResult(AddEditEventActivity.RESULT_OK, null);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(AddEditEventActivity.RESULT_CANCEL, null);
                finish();
            }
        });

        when = new Date(getIntent().getLongExtra("selectedDate", (new Date()).getDate()));
    }

    private void updateData(boolean saveAndValidate)
    {
        EditText name = (EditText)findViewById(R.id.name);
        //DatePicker when = (DatePicker)findViewById(R.id.when);
        TimePicker time = (TimePicker)findViewById(R.id.whenTime);
        EditText cell = (EditText)findViewById(R.id.cell);


        if( saveAndValidate )
        {
            //write to backing fields from controls
            event.patient = name.getText().toString();

            Date startTime = new Date(when.getYear(), when.getMonth(), when.getDate());


            startTime.setHours(time.getCurrentHour());
            startTime.setMinutes(time.getCurrentMinute());

            //event.date = new Date(when.toGMTString());
            event.date = startTime;

            event.patient_phone = cell.getText().toString();

            event.clinic = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.name_pref_clinic), "");

        }
        else
        {
            //load controls from backing fields
            name.setText(event.patient);
            //when.updateDate(event.date.getYear() + 1900, event.date.getMonth(), event.date.getDate());

            time.setCurrentHour(event.date.getHours());
            time.setCurrentMinute(event.date.getMinutes());

            cell.setText(event.patient_phone);
        }

    }

    public static void setAppointment(Appointment eventEntry)
    {
        event = eventEntry;
    }

    public static Appointment getAppointment()
    {
        return event;
    }
}