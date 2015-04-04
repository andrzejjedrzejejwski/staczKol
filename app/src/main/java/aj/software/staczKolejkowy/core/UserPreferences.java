package aj.software.staczKolejkowy.core;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPreferences {

    private Context context;
    private static final String NAME = "UserPreferences";
    private static final String USER_NUMBER = "USER_NUMBER";

    public UserPreferences(Context context) {
        this.context = context;
    }

    public String getUserNumberOrEmpty(){
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(USER_NUMBER, "");
    }

    public void setUserNumber(String userNumber){
        final SharedPreferences.Editor edit = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).edit();
        edit.putString(USER_NUMBER, userNumber);
        edit.commit();
    }
}
