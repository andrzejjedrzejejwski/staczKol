package aj.software.staczKolejkowy.core;

import android.app.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import aj.software.staczKolejkowy.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends Application {

    @Inject
    ActivityLifecycleCallbacks lifecycleCallbacks;

    private RemoteApiService service;
    private Department department;
    private boolean lcdAnimating = false;

    private static final Logger LOG = LoggerFactory.getLogger(App.class.getSimpleName());

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault("fonts/advantage-book.ttf", R.attr.fontPath);
        SharedObjectGraph.create(this);
        registerActivityLifecycleCallbacks(lifecycleCallbacks);
    }

    public RemoteApiService getService() {
        return service;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public void setService(RemoteApiService service) {
        this.service = service;
    }

    public boolean isLcdAnimating() {
        return lcdAnimating;
    }

    public void setLcdAnimating(boolean lcdAnimating) {
        LOG.debug("lcdAnimating={}", lcdAnimating);
        this.lcdAnimating = lcdAnimating;
    }
}
