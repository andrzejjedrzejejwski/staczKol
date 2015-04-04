package aj.software.staczKolejkowy.core;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.security.KeyStore;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import dagger.Module;
import dagger.Provides;
import retrofit.Callback;
import retrofit.MockRestAdapter;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import aj.software.staczKolejkowy.DepartmentsActivity;
import aj.software.staczKolejkowy.InfoActivity;
import aj.software.staczKolejkowy.StandsActivity;

@Module(injects = {
        App.class,
        DepartmentsActivity.class,
        InfoActivity.class,
        RemoteApiService.class,
        StandsActivity.class
}, library = true)
public class AppModule {

    private Context applicationContext;

    public AppModule(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Provides
    public Context providesApplicationContext(){
        return applicationContext;
    }

    @Provides @Singleton
    public UserPreferences providesUserPreferences(){
        return new UserPreferences(applicationContext);
    }

    @Provides
    @Singleton
    public NavigationController providesNavigationController(NavigationControllerImpl navigationController){
        return navigationController;
    }

    @Provides
    public Application.ActivityLifecycleCallbacks providesActivityLifecycleCallbacks(NavigationControllerImpl navigationController){
        return navigationController;
    }

    @Provides @Singleton
    public Bus providesBus(){
        return new Bus(ThreadEnforcer.MAIN);
    }

    @Provides
    public RemoteApi providesRemoteApi(){

        OkHttpClient client = new OkHttpClient();
        try {
            KeyStore keyStore = SSLUtils.getKeyStore(applicationContext);
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null,trustManagerFactory.getTrustManagers(), new SecureRandom());
            client.setSslSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            Log.d("QueueSleuth", "cannot create http client", e);
        }
        OkClient okClient = new OkClient(client);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.um.warszawa.pl/")
                .setClient(okClient)
//                .setRequestInterceptor(new ApiRequestInterceptor())
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();
        return restAdapter.create(RemoteApi.class);
    }

    @Provides @Mock
    public RemoteApi providesMockRemoteApi(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://api.bihapi.pl")
                .setRequestInterceptor(new ApiRequestInterceptor())
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .build();
        final MockRestAdapter adapter = MockRestAdapter.from(restAdapter);
        adapter.setDelay(200);

        return adapter.create(RemoteApi.class, new RemoteApi() {
            @Override
            public void getDepartment(Callback<Department> callback) {
                Department mocked = new Department();
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
//                mocked.setDate(sdf.format(new Date()));
//                mocked.setHour("");
//                final Department.Group group = new Department.Group();
//                group.setName("Paszporty");
//                group.setAvargeServiceTime("33");
//                group.setCurrentNumber("AG 123");
//                group.setQueueLength("4");
//                group.setOpenStandsCount("0");
//                group.setLetter("-69");
//                mocked.setGroups(new ArrayList<Department.Group>(){{add(group);}});
                callback.success(mocked, null);
            }

            @Override
            public void getDepartment2(Callback<Department> callback) {

            }
        });
    }

}
