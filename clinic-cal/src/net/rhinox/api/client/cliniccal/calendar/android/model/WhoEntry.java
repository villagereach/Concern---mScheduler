package net.rhinox.api.client.cliniccal.calendar.android.model;

import com.google.api.client.util.Key;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/22/11
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class WhoEntry
{
    @Key("@email")
    public String email;

    @Key("@rel")
    public String rel;

    @Key("@valueString")
    public String valueString;
}
