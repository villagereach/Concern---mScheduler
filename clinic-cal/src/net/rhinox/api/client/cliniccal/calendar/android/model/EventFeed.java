package net.rhinox.api.client.cliniccal.calendar.android.model;

import com.google.api.client.util.Key;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/21/11
 * Time: 5:40 PM
 *
 */
public class EventFeed extends Feed
{

    @Key("id")
    public String id;

    @Key("updated")
    public String updated;

    @Key("title")
    public String title;

    @Key("subtitle")
    public String subtitle;

    @Key("author")
    public Author author;

    @Key("generator")
    public String generator;


    @Override
    public String toString()
    {
        return String.format("Event Feed ID: %s - %s - %s", id, title, subtitle);
    }


    /*@Key("where")
    @Value("valueString")
    public String where;*/


    @Key("entry")
    public List<EventEntry> events = Lists.newArrayList();
}
