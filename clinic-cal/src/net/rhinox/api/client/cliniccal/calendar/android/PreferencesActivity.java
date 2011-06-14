package net.rhinox.api.client.cliniccal.calendar.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/25/11
 * Time: 9:47 PM

 */
public class PreferencesActivity extends PreferenceActivity
{
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}