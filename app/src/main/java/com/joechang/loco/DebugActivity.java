package com.joechang.loco;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.joechang.loco.fragment.LocationDebugFragment;
import com.joechang.loco.service.LocationPublishService;
import com.joechang.loco.service.Stagnancy;
import com.joechang.loco.utils.LocationUtils;
import com.joechang.loco.utils.ViewUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DebugActivity extends BaseDrawerActionBarActivity implements
        ServiceConnection, LocationPublishService.Listener {
    private static Logger log = Logger.getLogger(DebugActivity.class.getSimpleName());

    private LocationPublishService lps;
    private Integer currentTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Should this be somewhere else?
        Intent lpsIntent = new Intent(this, LocationPublishService.class);
        bindService(lpsIntent, this, BIND_AUTO_CREATE);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, LocationDebugFragment.newInstance())
                    .commit();
        }
    }


    private void refreshFields() {
        //Keep the current color for animation purposes.
        if (currentTextColor == null) {
            currentTextColor = ((EditText) findViewById(R.id.lastInterruptTime)).getCurrentTextColor();
        }

        if (this.lps == null) {
            return;
        }

        DateFormat sdf = SimpleDateFormat.getDateTimeInstance();

        updateField(R.id.currencyStagnancy, this.lps.getCurrentStagnancy());
        updateField(R.id.currentLocation, LocationUtils.toFriendlyString(this.lps.getLastLocation()));
        updateField(R.id.lastPublish, sdf.format(new Date(this.lps.getLastPublish())));
        updateField(R.id.sleepTime,
                sdf.format(this.lps.getNextLoopTime()) + " (Sleep: " + this.lps.getCurrentSleepTime() + "s)");

        updateField(R.id.lastLocationChangeTime,
                sdf.format(this.lps.getLastLocationChangeMillis()) +
                        " (Stagnant: " +
                        ((System.currentTimeMillis() - this.lps.getLastLocationChangeMillis()) / 1000) +
                        "s)");

        updateField(R.id.lastLocationEvaluatedTime, sdf.format(new Date(this.lps.getLastEvaluatedLocation())));
        updateField(R.id.activeLocationListener, this.lps.getActiveLocationListener());
        updateField(R.id.lastInterruptTime, sdf.format(new Date(this.lps.getLastInterruptedMillis())));

        setupToggleButton();
    }

    private void updateField(int field, Object value) {
        EditText g = (EditText) findViewById(field);
        if (g != null) {
            if (value != null) {
                if (g.getText() != null) {
                    if (!g.getText().toString().equalsIgnoreCase(value.toString())) {
                        g.setText(value.toString());
                        ViewUtils.animateField(this, g, this.currentTextColor);
                    }
                }
            } else {
                g.setText("");
            }
        }
    }

    private void setupToggleButton() {
        Button toggleRealtime = (Button)findViewById(R.id.toggleRealtime);
        if (toggleRealtime != null && !toggleRealtime.hasOnClickListeners()) {
            toggleRealtime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.toggleRealtime) {
                        if (Stagnancy.OVERRIDES.contains(lps.getCurrentStagnancy())) {
                            lps.removeStagnancyOverride();
                        } else {
                            lps.overrideRealtimeMode();
                        }
                        lps.resetBackgroundListener();
                        refreshFields();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this);
    }

    @Override
    public NavigationEnum getNavigationEnum() {
        return NavigationEnum.DEBUG;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocationPublishService.Binder binder = (LocationPublishService.Binder) service;
        this.lps = binder.getService();
        this.lps.addListener(this);
        refreshFields();
        log.log(Level.INFO, "Bound to LocationPublishService " + name);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        log.log(Level.INFO, "Unbound from LocationPublishService " + name);
    }

    @Override
    public void doOnSample(LocationPublishService lps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshFields();
                } catch (Exception ee) {
                    Logger.getLogger("WtF").log(Level.SEVERE, "wtf", ee);
                }
            }
        });
    }
}
