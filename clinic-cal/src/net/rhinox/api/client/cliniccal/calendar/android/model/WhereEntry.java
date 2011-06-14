package net.rhinox.api.client.cliniccal.calendar.android.model;

import com.google.api.client.util.Key;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/22/11
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class WhereEntry
{
    @Key("@label")
    public String label;

    @Key("@rel")
    public String rel;

    @Key("@valueString")
    public String valueString;


}
