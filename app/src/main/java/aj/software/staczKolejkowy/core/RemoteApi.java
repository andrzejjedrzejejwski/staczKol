package aj.software.staczKolejkowy.core;

import retrofit.Callback;
import retrofit.http.GET;

public interface RemoteApi {

    @GET("/queue/warszawa?location=um_starynkiewicza")
    void getDepartment(Callback<Department> callback);

    @GET("/api/action/wsstore_get/?id=95fee469-79db-4b4b-9ddc-91d49d1f0f51")
    void getDepartment2(Callback<Department> callback);
}
