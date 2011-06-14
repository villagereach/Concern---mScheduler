package net.rhinox.api.client.cliniccal.calendar.android.model;

import android.provider.ContactsContract;
import com.google.api.client.util.Data;
import com.google.api.client.util.Key;

/**
 * Created by IntelliJ IDEA.
 * User: nils
 * Date: 5/21/11
 * Time: 6:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Author implements Cloneable
{
    @Key("name")
    public String name;

    @Key("email")
    public String email;

    @Override
    public Author clone() throws CloneNotSupportedException
    {
        Author result = (Author)super.clone();
        Data.deepCopy(this, result);

        return result;
    }
}
