/* this app has borrowed liberally from the original sample code for the google api client samples

 */

/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.rhinox.api.client.cliniccal.calendar.android;

import android.accounts.*;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.googleapis.MethodOverride;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.*;
import com.google.api.client.util.DateTime;
import com.google.common.collect.Lists;
import net.rhinox.api.client.cliniccal.calendar.android.model.CalendarClient;
import net.rhinox.api.client.cliniccal.calendar.android.model.CalendarEntry;
import net.rhinox.api.client.cliniccal.calendar.android.model.CalendarFeed;
import net.rhinox.api.client.cliniccal.calendar.android.model.CalendarUrl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Sample for Google Calendar Data API using the Atom wire format. It shows how to authenticate, get
 * calendars, add a new calendar, update it, and delete it.
 * <p>
 * To enable logging of HTTP requests/responses, change {@link #LOGGING_LEVEL} to
 * {@link Level#CONFIG} or {@link Level#ALL} and run this command:
 * </p>
 * <p/>
 * <pre>
 * adb shell setprop log.tag.HttpTransport DEBUG
 * </pre>
 *
 * @author Yaniv Inbar
 */
public final class ClinicCalStartActivity extends ListActivity
{

    /**
     * Logging level for HTTP requests/responses.
     */
    private static Level LOGGING_LEVEL = Level.OFF;

    private static final String AUTH_TOKEN_TYPE = "cl";

    private static final String TAG = "ClinicCal";

    private static final int MENU_ADD = 0;

    private static final int MENU_ACCOUNTS = 1;

    private static final int CONTEXT_EDIT = 0;

    private static final int CONTEXT_DELETE = 1;

    private static final int REQUEST_AUTHENTICATE = 0;

    CalendarClient client;

    private final List<CalendarEntry> calendars = Lists.newArrayList();

    private final HttpTransport transport = AndroidHttp.newCompatibleTransport();

    String gsessionid;
    String authToken;
    String accountName;

    static final String PREF = TAG;
    static final String PREF_ACCOUNT_NAME = "accountName";
    static final String PREF_AUTH_TOKEN = "authToken";
    static final String PREF_GSESSIONID = "gsessionid";
    GoogleAccountManager accountManager;
    SharedPreferences settings;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        createCalendarClient();

        getListView().setTextFilterEnabled(true);



        registerForContextMenu(getListView());
        getAccount();
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

    private void getAccount()
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
            } else
            {
                executeRefreshCalendars();
            }
            return;
        }
        chooseAccount();
    }

    private void chooseAccount()
    {
        accountManager.manager.getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
                AUTH_TOKEN_TYPE,
                null,
                ClinicCalStartActivity.this,
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
                            executeRefreshCalendars();
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
                    getAccount();
                } else
                {
                    chooseAccount();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_ADD, 0, getString(R.string.new_calendar));
        if (accountManager.getAccounts().length >= 2)
        {
            menu.add(0, MENU_ACCOUNTS, 0, getString(R.string.switch_account));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_ADD:
                CalendarUrl url = CalendarUrl.forOwnCalendarsFeed();
                CalendarEntry calendar = new CalendarEntry();
                calendar.title = "Calendar " + new DateTime(new Date());
                try
                {
                    client.executeInsertCalendar(calendar, url);
                } catch (IOException e)
                {
                    handleException(e);
                }
                executeRefreshCalendars();
                return true;
            case MENU_ACCOUNTS:
                chooseAccount();
                return true;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        /* don't allow the editing or deleting of accounts from this app */

//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.add(0, CONTEXT_EDIT, 0, getString(R.string.update));
//        menu.add(0, CONTEXT_DELETE, 0, getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        CalendarEntry calendar = calendars.get((int) info.id);
        try
        {
            switch (item.getItemId())
            {
                case CONTEXT_EDIT:
                    CalendarEntry patchedCalendar = calendar.clone();
                    patchedCalendar.title = calendar.title + " UPDATED " + new DateTime(new Date());
                    client.executePatchCalendarRelativeToOriginal(patchedCalendar, calendar);
                    executeRefreshCalendars();
                    return true;
                case CONTEXT_DELETE:
                    client.executeDelete(calendar);
                    executeRefreshCalendars();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        } catch (IOException e)
        {
            handleException(e);
        } catch (CloneNotSupportedException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    @Override
    public void onListItemClick(android.widget.ListView l, android.view.View v, int position, long id)
    {
        CalendarEntry cal = calendars.get((int)id);

        //create a new activity here that shows the calendar entries
//        AppointmentListActivity.setCalendar(cal);
//        AppointmentListActivity.setClient(client);
//
//        Intent i = new Intent(ClinicCalStartActivity.this, AppointmentListActivity.class);
//        startActivity(i);
    }


    void executeRefreshCalendars()
    {
        final ProgressDialog dlg = ProgressDialog.show(ClinicCalStartActivity.this, "Loading", "Loading calendars...");
        final ClinicCalStartActivity act = this;

        final android.os.Handler h = new android.os.Handler()
        {
            @Override
            public void handleMessage(Message ms)
            {
                String[] calendarNames;

                int numCalendars = calendars.size();
                calendarNames = new String[numCalendars];
                for (int i = 0; i < numCalendars; i++)
                {
                    calendarNames[i] = calendars.get(i).title;
                }
                setListAdapter(
                    new ArrayAdapter<String>(act, android.R.layout.simple_list_item_1, calendarNames));

                dlg.dismiss();
            }
        };


        new Thread(new Runnable()
        {
            public void run()
            {
                List<CalendarEntry> calendars = act.calendars;
                calendars.clear();
                try
                {
                    CalendarUrl url = CalendarUrl.forAllCalendarsFeed();
                    // page through results
                    while (true)
                    {
                        CalendarFeed feed = act.client.executeGetCalendarFeed(url);
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
                    }


                h.sendEmptyMessage(0);
            }
        }).start();


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
            // TODO(yanivi): should only try this once to avoid infinite loop
            if (statusCode == 401)
            {
                getAccount();
                return;
            }
        }
        Log.e(TAG, e.getMessage(), e);
    }
}
