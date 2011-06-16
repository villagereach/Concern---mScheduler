package net.rhinox.api.client.cliniccal.calendar.android;

import android.accounts.*;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.*;
import com.google.common.collect.Lists;
import net.rhinox.api.client.cliniccal.calendar.android.model.*;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 6/3/11
 * Time: 10:27 PM
 */
public class SyncDlg extends Activity
{

    private static final int REQUEST_AUTHENTICATE = 0;
    private static final String AUTH_TOKEN_TYPE = "cl";
    private static final String TAG = "ClinicCal";
    static final String PREF = TAG;
    static final String PREF_ACCOUNT_NAME = "accountName";
    static final String PREF_AUTH_TOKEN = "authToken";
    static final String PREF_GSESSIONID = "gsessionid";

    public static final int RESULT_FAILED = 0;
    public static final int RESULT_PASSED = 1;

    CalendarClient client;
    GoogleAccountManager accountManager;
    SharedPreferences settings;
    String authToken;
    String gsessionid;
    String accountName;
    private final List<CalendarEntry> calendars = Lists.newArrayList();

    private final HttpTransport transport = AndroidHttp.newCompatibleTransport();

    static StorageManager storageManager;

    CalendarEntry selectedCalendar;

    public static StorageManager getStorageManager()
    {
        return storageManager;
    }

    public static void setStorageManager(StorageManager mgr)
    {
        storageManager = mgr;
    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sync);

        setupUi();

        createCalendarClient();
        try
        {
            getAccount();
        } catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            handleException(e);
        }


        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler()
        {
            public boolean queueIdle()
            {
                try
                {
                    SyncDlg.this.startUpdate();
                } catch (IOException e)
                {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return false;
            }
        });

    }

    private void createCalendarClient()
    {
       accountManager = new GoogleAccountManager(this);
       settings = this.getSharedPreferences(PREF, 0);
       authToken = settings.getString(PREF_AUTH_TOKEN, null);
       gsessionid = settings.getString(PREF_GSESSIONID, null);

       final MethodOverride override = new MethodOverride(); // needed for PATCH

       client = new CalendarClient(transport.createRequestFactory(new HttpRequestInitializer()
       {

           public void initialize(HttpRequest request)
           {
               GoogleHeaders headers = new GoogleHeaders();
               headers.setApplicationName("ClinicCalStartActivity/1.0");
               headers.gdataVersion = "2";
               request.headers = headers;

               client.initializeParser(request);
               request.interceptor = new HttpExecuteInterceptor()
               {

                   public void intercept(HttpRequest request) throws IOException
                   {
                       GoogleHeaders headers = (GoogleHeaders) request.headers;
                       headers.setGoogleLogin(authToken);
                       request.url.set("gsessionid", gsessionid);
                       override.intercept(request);
                   }
               };

               request.unsuccessfulResponseHandler = new HttpUnsuccessfulResponseHandler()
               {

                   public boolean handleResponse(
                           HttpRequest request, HttpResponse response, boolean retrySupported)
                   {
                       switch (response.statusCode)
                       {
                           case 302:
                               GoogleUrl url = new GoogleUrl(response.headers.location);
                               gsessionid = (String) url.getFirst("gsessionid");
                               SharedPreferences.Editor editor = settings.edit();
                               editor.putString(PREF_GSESSIONID, gsessionid);
                               editor.commit();
                               return true;
                           case 401:
                               accountManager.invalidateAuthToken(authToken);
                               authToken = null;
                               SharedPreferences.Editor editor2 = settings.edit();
                               editor2.remove(PREF_AUTH_TOKEN);
                               editor2.commit();
                               return false;
                       }
                       return false;
                   }
               };
           }
       }));

   }

    void executeRefreshCalendars()
    {

        updateProgress("Loading Calendar", false, 5);

        calendars.clear();
        try
        {
            CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
            // page through results
            while (true)
            {
                CalendarFeed feed = client.executeGetCalendarFeed(url);
                if (feed.calendars != null)
                {
                    calendars.addAll(feed.calendars);
                }

                String nextLink = feed.getNextLink();
                if (nextLink == null)
                {
                    break;
                }
            }

        } catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            handleException(e);
        }

        if( calendars.size() == 0 )
        {
            updateProgress("No calendars found.", false, 100);
            try
            {
                Thread.sleep(2000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            setResult(RESULT_FAILED);
            finish();
        }
        else
        {
            String calName = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.pref_name_calendar), "");

            for( CalendarEntry e : calendars)
            {
                if(e.title.equals(calName))
                {
                    selectedCalendar = e;
                }
            }
        }
    }

    private void getAccount() throws IOException
    {
       Account account = accountManager.getAccountByName(accountName);
       if (account != null)
       {
           // handle invalid token
           if (authToken == null)
           {
               accountManager.manager.getAuthToken(
                       account, AUTH_TOKEN_TYPE, true, new AccountManagerCallback<Bundle>()
                       {

                           public void run(AccountManagerFuture<Bundle> future)
                           {
                               try
                               {
                                   Bundle bundle = future.getResult();
                                   if (bundle.containsKey(AccountManager.KEY_INTENT))
                                   {
                                       Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                                       int flags = intent.getFlags();
                                       flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
                                       intent.setFlags(flags);
                                       startActivityForResult(intent, REQUEST_AUTHENTICATE);
                                   } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN))
                                   {
                                       setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
                                       executeRefreshCalendars();
                                   }
                               } catch (Exception e)
                               {
                                   handleException(e);
                               }
                           }
                       }, null);
           }

           return;
       }
       chooseAccount();

   }

    void setAuthToken(String authToken)
    {
       SharedPreferences.Editor editor = settings.edit();
       editor.putString(PREF_AUTH_TOKEN, authToken);
       editor.commit();
       this.authToken = authToken;
    }

    void setAccountName(String accountName)
    {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.remove(PREF_GSESSIONID);
        editor.commit();
        this.accountName = accountName;
        gsessionid = null;
    }

    void handleException(Exception e)
    {
        e.printStackTrace();
        if (e instanceof HttpResponseException)
        {
            HttpResponse response = ((HttpResponseException) e).response;
            int statusCode = response.statusCode;
            try
            {
                response.ignore();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            // TODO: NAB should only try this once to avoid infinite loop
            if (statusCode == 401)
            {
                try
                {
                    getAccount();
                } catch (IOException e1)
                {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                return;
            }
        }
        Log.e(TAG, e.getMessage(), e);
    }

    private void chooseAccount()
    {
       accountManager.manager.getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
               AUTH_TOKEN_TYPE,
               null,
               SyncDlg.this,
               null,
               null,
               new AccountManagerCallback<Bundle>()
               {

                   public void run(AccountManagerFuture<Bundle> future)
                   {
                       Bundle bundle;
                       try
                       {
                           bundle = future.getResult();
                           setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
                           setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));

                       } catch (OperationCanceledException e)
                       {
                           // user canceled
                       } catch (AuthenticatorException e)
                       {
                           handleException(e);
                       } catch (IOException e)
                       {
                           handleException(e);
                       }
                   }
               },
               null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
       super.onActivityResult(requestCode, resultCode, data);
       switch (requestCode)
       {
           case REQUEST_AUTHENTICATE:
               if (resultCode == RESULT_OK)
               {
                   try
                   {
                       getAccount();
                   } catch (IOException e)
                   {
                       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                   }
               } else
               {
                   chooseAccount();
               }
               break;
       }
    }

    private void startUpdate() throws IOException
    {
        executeRefreshCalendars();
        processAdds();
        processUpdates();
        processDeletes();

        refreshLocalContent();

        updateProgress("Update complete", false, 100);

        setResult(RESULT_PASSED);
        finish();
    }

    private void refreshLocalContent() throws IOException
    {
        updateProgress("Refreshing appointment list", false, 70);

        if( null == selectedCalendar )
        {
            return;
        }

        storageManager.deleteAllAppointments();


        EventFeed feed = client.executeGetEventFeed(new CalendarUrl(selectedCalendar.getEventFeedLink()));

        for( EventEntry e : feed.events )
        {
            if(0 !=  e.when.startTime.length())
            {
                Appointment appt = new Appointment(e);
                appt.clinic = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(getString(R.string.pref_name_clinic), "");
                storageManager.addAppointment(appt, false);
            }
        }

    }

    private void processDeletes() throws IOException
    {

        updateProgress("Removing deleted appointments", false, 30);


        if( null == selectedCalendar )
        {
            return;
        }

        Appointment appts[] = storageManager.getAppointments("ACTION = 'D'");

        if( null == appts )
        {
          return;
        }

        for( Appointment appt : appts )
        {
            //TODO: nab
            //we don't have a link here for editing or anything else. we will need to store this info somewhere
            try
            {
                client.executeDelete(appt.toEvent());
            }
            catch(Exception ex)
            {
                //I don't like doing this, but there are a couple cases where the event won't be in google yet.
            }

          storageManager.deleteAppointment(appt, true);
        }

    }

    private void processAdds() throws IOException
    {
        updateProgress("Sending new appointments", false, 10);

        if( null == selectedCalendar )
        {
            return;
        }

        Appointment appts[] = storageManager.getAppointments("ACTION = 'A'");

        if( null == appts )
        {
            return;
        }

        for( Appointment appt : appts )
        {
            client.execInsertEventEntry(new CalendarUrl(selectedCalendar.getEventFeedLink()), appt.toEvent());
            storageManager.deleteAppointment(appt, true);
        }

    }

    private void processUpdates() throws IOException
    {
        updateProgress("Sending changed appointments", false, 20);

        Appointment appts[] = storageManager.getAppointments("ACTION = 'U'");

        if( null == appts )
        {
            return;
        }

        for( Appointment appt : appts )
        {
            client.executeUpdateEvent(appt.toEvent());
            storageManager.deleteAppointment(appt, true);
        }
    }

    private void setupUi()
    {
        ProgressBar progress = (ProgressBar)findViewById(R.id.sync_progress);

        progress.setMax(100);
        progress.setIndeterminate(true);

    }

    private void updateProgress( String text, boolean indeterminate, double progress)
    {
        TextView tv = (TextView)findViewById(R.id.sync_label);
        ProgressBar bar = (ProgressBar)findViewById(R.id.sync_progress);

        tv.setText(text);
        bar.setIndeterminate(indeterminate);
        bar.setProgress((int)progress);

    }
}