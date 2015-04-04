package aj.software.staczKolejkowy.core;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.squareup.otto.Bus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RemoteApiService extends Service {

    @Inject
    RemoteApi remoteApi;

    @Inject
    Bus bus;

    private LocalBinder binder = new LocalBinder();
    private Timer timer;

    private int updateFreqInSeconds = 25;

    private static final Logger LOG = LoggerFactory.getLogger(RemoteApiService.class.getSimpleName());

    @Override
    public IBinder onBind(Intent intent) {
        LOG.debug("■■■ onBind ■■■");
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedObjectGraph.inject(this);
        makeYourAwesomeJob();
        LOG.debug("■■■ onCreate ■■■");
    }

    private void makeYourAwesomeJob() {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOG.debug("■■■ awesome service task begin ■■■");
                remoteApi.getDepartment2(new Callback<Department>() {
                    @Override
                    public void success(Department department, Response response) {
                        LOG.debug("■■■ [SUCCEED] fetch department ■■■");
                        department.getResult().setName("Starynkiewicza - Warszawa");
                        if(SimpleState.isNotificationEnabled()){
                            NotificationBuilder.createAndShow(getApplicationContext(), department.getResult().getGroups().get(SimpleState.getGroupPosition()));
                        }
                        bus.post(ResponseEvent.ok(department));
                        LOG.debug("■■■ awesome service task end ■■■");
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        LOG.error("[FAILED] fetch department", error);
                        bus.post(ResponseEvent.error());
                        LOG.debug("■■■ awesome service task end ■■■");
                    }
                });
            }
        };
        timer.schedule(task, 1000, updateFreqInSeconds * 1000);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LOG.debug("■■■ onTaskRemoved ■■■");
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        LOG.debug("■■■ onDestroy ■■■");
        timer.cancel();
        super.onDestroy();
    }

    public class LocalBinder extends Binder{
        public RemoteApiService getService(){
            return RemoteApiService.this;
        }
    }


}
