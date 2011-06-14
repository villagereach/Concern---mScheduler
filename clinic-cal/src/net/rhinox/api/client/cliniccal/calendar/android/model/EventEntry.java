package net.rhinox.api.client.cliniccal.calendar.android.model;

import com.google.api.client.util.Data;
import com.google.api.client.util.Key;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/21/11
 * Time: 5:42 PM
 */
public class EventEntry implements Cloneable
{

    @Key("id")
    public String id;

    @Key("updated")
    public String updated;

    @Key("summary")
    public String summary;

    @Key("published")
    public String published;

    @Key("author")
    public String author;

    //TODO: nab - this breaks adding events so we'll ignore it until we need it
//    @Key("category")
//    public String category;

    @Key("content")
    public String content;

    @Key("title")
    public String title;

    @Key("link")
    public List<Link> links = Lists.newArrayList();

    @Key("gd:when")
    public WhenEntry when = new WhenEntry();

    @Key("gd:where")
    public WhereEntry where = new WhereEntry();

    /*@Key("gd:who")
    public WhoEntry who = new WhoEntry();*/

    @Override
    public EventEntry clone() throws CloneNotSupportedException
    {
        @SuppressWarnings("unchecked")
        EventEntry result = (EventEntry)super.clone();
        Data.deepCopy(this, result);
        return result;

    }

     public String getEditLink()
    {
        return Link.find(links, "edit");
    }
}
