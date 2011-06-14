package net.rhinox.api.client.cliniccal.calendar.android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/25/11
 * Time: 7:00 PM

 */


public class StorageManager
{
    private Context context;
    SQLiteDatabase database;


    private class ClinicCalOpenHelper extends SQLiteOpenHelper
    {


        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "cliniccal";
        private static final String CREATE_STRING = "CREATE TABLE \"main\".\"APPOINTMENTS\" (\n" +
                "    \"ID\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                "    \"SYNCID\" TEXT,\n" +
                "    \"PATIENT\" TEXT NOT NULL,\n" +
                "    \"PATIENT_PHONE\" TEXT NOT NULL,\n" +
                "    \"CLINIC\" TEXT NOT NULL,\n" +
                "    \"DATETIME\" INTEGER NOT NULL,\n" +
                "    \"ACTION\" TEXT NOT NULL DEFAULT ('N'),\n" +
                "    \"URL\" TEXT NOT NULL DEFAULT('')\n" +
                ");";

        public ClinicCalOpenHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase)
        {
            sqLiteDatabase.execSQL(CREATE_STRING);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
        {

        }
    }


    public void open(Context ctx)
    {
        close();
        context = ctx;
        ClinicCalOpenHelper helper = new ClinicCalOpenHelper(ctx);
        database = helper.getWritableDatabase();
    }

    public void close()
    {
        if( null != database && database.isOpen())
        {
            database.close();
        }
    }

    public boolean isOpen()
    {
        return (null != database && database.isOpen());
    }

    public void addAppointment(Appointment appt, boolean isAddAction)
    {
        if(!isOpen())
        {
            return;
        }


        ContentValues values = new ContentValues();

        values.put("Patient", appt.patient);
        values.put("Patient_Phone", appt.patient_phone);
        values.put("Clinic", appt.clinic);
        values.put("DateTime", appt.date.getTime());
        if( isAddAction )
        {
            values.put("Action", "A");
        }
        else
        {
            values.put("Action", "N");
        }

        values.put("URL", appt.url);

        database.insert("appointments", null, values);
    }

    public void updateAppointment(Appointment appt)
    {
        if(!isOpen())
        {
            return;
        }

        ContentValues values = new ContentValues();

        values.put("SyncID", appt.id);
        values.put("Patient", appt.patient);
        values.put("Patient_Phone", appt.patient_phone);
        values.put("Clinic", appt.clinic);
        values.put("DateTime", appt.date.getTime());
        values.put("Action", "U");
        values.put("URL", appt.url);

        database.update("appointments", values, "ID=" + appt.id, null);

    }


    public void deleteAppointment(Appointment appt, boolean removeRecord)
    {
         if(!isOpen())
        {
            return;
        }

        ContentValues values = new ContentValues();

        if( removeRecord )
        {
            database.delete("appointments", "ID=" + appt.id, null);
            return;
        }

        values.put("Action", "D");
        database.update("appointments", values, "ID=" + appt.id, null);
    }


    public void deleteAllAppointments()
    {
        database.execSQL("delete from appointments where 1=1");
    }

    private Map<String, Integer> getColMap(Cursor c)
    {
        Map<String, Integer> m = new HashMap<String, Integer>();

        for(int i = 0; i < c.getColumnCount(); i++)
        {
            m.put(c.getColumnName(i).toUpperCase(), i);
        }

        return m;
    }

    public Appointment[] getAppointments(String where)
    {
        if( !isOpen())
        {
            return new Appointment[0];
        }

        Cursor c = database.query("appointments", new String[]{ "*" }, null != where ? where : null, null, null, null, "DateTime asc");
        Appointment[] appts = null;

        if( null != c && c.getCount() > 0)
        {
            appts  = new Appointment[c.getCount()];

            c.moveToFirst();

            int i = 0;
            Map<String, Integer> cols = getColMap(c);

            do
            {
               Appointment appt = new Appointment();

               appt.clinic = c.getString(cols.get("CLINIC"));
               appt.date = new Date(c.getLong(cols.get("DATETIME")));
               appt.patient = c.getString(cols.get("PATIENT"));
               appt.patient_phone = c.getString(cols.get("PATIENT_PHONE"));
               appt.syncID = c.getString(cols.get("SYNCID"));
               appt.id = c.getInt(cols.get("ID"));
               appt.url = c.getString(cols.get("URL"));

               appts[i++] = appt;

            }  while( c.moveToNext() );

        }

        c.close();


        return appts;

    }

}
