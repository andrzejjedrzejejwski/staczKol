package aj.software.staczKolejkowy;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import aj.software.staczKolejkowy.core.App;
import aj.software.staczKolejkowy.core.Department;
import aj.software.staczKolejkowy.core.NavigationController;
import aj.software.staczKolejkowy.core.NotificationBuilder;
import aj.software.staczKolejkowy.core.RemoteApiService;
import aj.software.staczKolejkowy.core.ResponseEvent;
import aj.software.staczKolejkowy.core.SharedObjectGraph;
import aj.software.staczKolejkowy.core.SimpleState;
import aj.software.staczKolejkowy.core.UserPreferences;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class InfoActivity extends Activity {

    @Inject
    NavigationController controller;

    @Inject
    Bus bus;

    @Inject
    UserPreferences preferences;

    @InjectView(R.id.user_number)
    EditText userNumber;

    @InjectView(R.id.time)
    TextView time;

    @InjectView(R.id.people_waiting)
    TextView peopleWaiting;

    @InjectView(R.id.notification_button)
    ImageButton button;

    @InjectView(R.id.lcd_layout)
    TextView lcd;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    private RemoteApiService service;
    private Department department;
    private Department.Result.Group group;
    private int position;

    private static final Logger LOG = LoggerFactory.getLogger(InfoActivity.class.getSimpleName());
    private boolean updatePeopleWaitingWithUserNumber;
    private int aheadNumberQueueLength;
    private App app;
    private boolean resumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedObjectGraph.inject(this);
        bus.register(this);
        setContentView(R.layout.info);
        ButterKnife.inject(this);

        app = (App) getApplication();
        department = app.getDepartment();
        position = SimpleState.getGroupPosition();
        group = department.getResult().getGroups().get(position);

        final TextView title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        title.setText(group.getName());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SimpleState.isNotificationEnabled()) {
                    button.setImageDrawable(getResources().getDrawable(R.drawable.lowerstripewhite));
                    SimpleState.setNotificationEnabled(false);
                    NotificationBuilder.cancel(getApplicationContext());
                } else {
                    button.setImageDrawable(getResources().getDrawable(R.drawable.lowerstripegold));
                    SimpleState.setNotificationEnabled(true);
                    NotificationBuilder.createAndShow(getApplicationContext(), department.getResult().getGroups().get(SimpleState.getGroupPosition()));
                }
            }
        });

        userNumber.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                    preferences.setUserNumber(userNumber.getText().toString());
                    updatePeopleWaitingWithUserNumber = false;
                    updateQueueLengthIfPossible();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userNumber.getWindowToken(), 0);
                    toolbar.requestFocus();
                    return true;
                }
                return false;
            }
        });

        userNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() < 1) {
                    preferences.setUserNumber("");
                    updatePeopleWaitingWithUserNumber = false;
                    updatePeopleWaiting();
                }
            }
        });

        bindService(new Intent(this, RemoteApiService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumed = true;
        if (SimpleState.isNotificationEnabled()) {
            button.setImageDrawable(getResources().getDrawable(R.drawable.lowerstripegold));
        } else {
            button.setImageDrawable(getResources().getDrawable(R.drawable.lowerstripewhite));
        }
        updateViews();
    }

    @Override
    protected void onPause() {
        resumed = false;
        super.onPause();
    }

    private void updateViews() {
        userNumber.setText(preferences.getUserNumberOrEmpty());
        updateTime();
        if(updatePeopleWaitingWithUserNumber){
            updatePeopleWaitingWithUserNumber();
        } else {
            updatePeopleWaiting();
        }
        updateLcd();
    }

    @Subscribe
    public void responseFromService(ResponseEvent event) {
        LOG.debug("response from service");
        if (event.getStatus() == ResponseEvent.Status.OK) {
            LOG.debug("status ok");
            department = event.getDepartment();
            group = event.getDepartment().getResult().getGroups().get(position);
            if(resumed) {
                updateViews();
            }
        } else {
            LOG.debug("status error");
        }
    }

    private void updateQueueLengthIfPossible() {
        String userNumberTxt = userNumber.getText().toString();
        int number = -1;
        int currentNumber = -1;
        try {
            int queueLength = Integer.parseInt(group.getQueueLength());
            number = Integer.parseInt(userNumberTxt.replaceAll("[\\D]", ""));
            currentNumber = Integer.parseInt(group.getCurrentNumber().replaceAll("[\\D]", ""));
            aheadNumberQueueLength = number - currentNumber;
            if(aheadNumberQueueLength > 0 && queueLength > 0 && aheadNumberQueueLength <= queueLength){
                updatePeopleWaitingWithUserNumber();
            } else{
                updatePeopleWaiting();
                throw new Exception();
            }
        } catch (NumberFormatException e) {
            LOG.warn("cannot parse int from number string", e);
        } catch (Exception e){
            LOG.warn("cannot evaluate how many people in queue between currentNumber {} and number {}", currentNumber, number);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            LOG.debug("Service connected");
            service = ((RemoteApiService.LocalBinder) binder).getService();
            App app = (App) getApplication();
            app.setService(service);
        }

        public void onServiceDisconnected(ComponentName className) {
            LOG.error("this should not ever happen");
        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    @Override
    protected void onDestroy() {
        bus.unregister(this);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        controller.onBackPressed();
    }

    private void updateLcd() {
        if(!app.isLcdAnimating()) {
            LcdAnimation lcdAnimation = new LcdAnimation(app, lcd, group.getCurrentNumber());
            lcdAnimation.execute();
        }
    }

    private void updateTime() {
        final String timeTextFormat = String.format("%s %s\n%s", group.getAvargeServiceTime(), getString(R.string.min), getString(R.string.estimated_time));
        SpannableString ss = new SpannableString(timeTextFormat);
        ss.setSpan(new RelativeSizeSpan(0.5f), 7, timeTextFormat.length(), 0);
        time.setText(ss);
    }

    private void updatePeopleWaiting() {
        final String waitingTextFormat = String.format("%s %s\n%s", group.getQueueLength(), getString(R.string.people), getString(R.string.in_queue));
        SpannableString ss = new SpannableString(waitingTextFormat);
        ss.setSpan(new RelativeSizeSpan(0.5f), 6, waitingTextFormat.length(), 0);
        peopleWaiting.setText(ss);
    }

    private void updatePeopleWaitingWithUserNumber() {
        updatePeopleWaitingWithUserNumber = true;
        final String waitingTextFormat = String.format("%s %s\n%s", aheadNumberQueueLength, getString(R.string.people), getString(R.string.in_queue_ahead));
        SpannableString ss = new SpannableString(waitingTextFormat);
        ss.setSpan(new RelativeSizeSpan(0.5f), 6, waitingTextFormat.length(), 0);
        peopleWaiting.setText(ss);
    }
}
