package aj.software.staczKolejkowy.core;

import android.util.Base64;

import retrofit.RequestInterceptor;

public class ApiRequestInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", String.format("Basic %s", Base64.encodeToString("48511111075:q9GDVfm6rX4LQuPJT".getBytes(), Base64.NO_WRAP)));
    }


}
