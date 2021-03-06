package net.rhinox.api.client.cliniccal.calendar.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/27/11
 * Time: 1:15 PM
 */
public class SelectDateDlg extends Activity
{

    public static final int RESULT_CANCEL = 0;
    public static final int RESULT_OK = 1;

    public static Date selectedDate;

    private boolean showAllDates = false;


    @Override
    public void onCreate( Bundle data )
    {
        super.onCreate(data);
        setContentView(R.layout.select_date);

        setupUi();
    }

    private void setupUi()
    {
        Button ok = (Button)findViewById(R.id.selectDateOk);
        Button cancel = (Button)findViewById(R.id.selectDateCancel);

        CheckBox allDates = (CheckBox)findViewById(R.id.selectDateAllDates);


        DatePicker dp = (DatePicker)findViewById(R.id.selectDateWhen);
        Date lastTime = new Date(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getLong(getString(R.string.pref_name_last_date), new Date().getTime()));

        if( null != dp)
        {
            dp.updateDate(lastTime.getYear() + 1900, lastTime.getMonth(), lastTime.getDate());
        }

        allDates.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
                {

                    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                    {
                        DatePicker dp = (DatePicker)SelectDateDlg.this.findViewById(R.id.selectDateWhen);
                        dp.setEnabled(!b);
                        showAllDates = b;
                    }
                });

        ok.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                DatePicker dp = (DatePicker)SelectDateDlg.this.findViewById(R.id.selectDateWhen);
                Intent i = new Intent();

                Date date = new Date(dp.getYear() - 1900, dp.getMonth(), dp.getDayOfMonth());

                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putLong(getString(R.string.pref_name_last_date), date.getTime()).commit();

                i.putExtra("selectedDate", date.getTime());
                i.putExtra("showAllAppointments", showAllDates);
                setResult( RESULT_OK, i);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                setResult(RESULT_CANCEL);
                finish();
            }
        });
    }

}
