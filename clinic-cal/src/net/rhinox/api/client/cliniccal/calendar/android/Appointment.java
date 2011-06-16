package net.rhinox.api.client.cliniccal.calendar.android;

import com.google.api.client.util.DateTime;
import net.rhinox.api.client.cliniccal.calendar.android.model.EventEntry;
import net.rhinox.api.client.cliniccal.calendar.android.model.Link;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/26/11
 * Time: 9:13 PM
 */
public class Appointment
    {
        public int id;
        public String syncID;
        public String patient;
        public Date date = new Date();
        public String clinic;
        public String patient_phone;
        public boolean toStringIncludeDate = false;
        public String url = "";
        public boolean toStringIncludeTime = false;

        public Appointment(){}

        public Appointment(EventEntry e)
        {
            syncID = e.id;
            patient = e.title;
            patient_phone = null != e.content ? e.content : "";

            clinic = e.where.valueString;

            if( 0 != e.when.startTime.length() )
            {
                try
                {
                    DateTime dt = DateTime.parseRfc3339(e.when.startTime);
                    date = new Date(dt.value);
                }
                catch( IllegalArgumentException ex)
                {

                }
            }

            url = e.getEditLink();

        }

        public EventEntry toEvent()
        {
            EventEntry e = new EventEntry();

            if( null != syncID)
            {
                e.id = syncID;
            }

            e.title = patient;
            e.content = patient_phone;



            //google expects UTC times formatted in a certain way
            DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("gmt"));
            e.when.startTime = df.format(date);

            e.where.valueString = clinic;

            Link l = new Link();
            l.href = url;
            l.rel = "edit";

            e.links.add(l);
            return e;
        }

        @Override
        public String toString()
        {
            if( toStringIncludeDate )
            {
                DateFormat df = new SimpleDateFormat("dd-MM-yy");
                return String.format("%s  %s %s", df.format(date), patient, patient_phone);
            }

            if( toStringIncludeTime )
            {
                DateFormat df = new SimpleDateFormat("HH:mm");
                return String.format("%s  %s", df.format(date), patient);
            }

            return String.format("%s - %s", patient, patient_phone);

        }
    }

