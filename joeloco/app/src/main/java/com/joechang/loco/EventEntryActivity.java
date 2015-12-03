package com.joechang.loco;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.joechang.loco.firebase.FirebaseManager;
import com.joechang.loco.model.Event;
import com.joechang.loco.model.Group;
import com.joechang.loco.model.PostWriteAction;
import com.joechang.loco.utils.CalendarUtils;
import com.joechang.loco.utils.DateBag;
import com.joechang.loco.utils.ViewUtils;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Author:  joechang
 * Date:    4/21/15
 * Purpose:
 */
public class EventEntryActivity extends FragmentActivity
        implements CalendarDatePickerDialog.OnDateSetListener, RadialTimePickerDialog.OnTimeSetListener, View.OnClickListener {

    private final String ORIGIN_WIDGET = "_origin_widget";
    private String selectedGroupId;
    private String eventId;
    private Integer currentTextColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Must have selected Group Id to create stuff
        selectedGroupId = getIntent().getStringExtra(Group.ID);
        if (selectedGroupId == null) {
            throw new IllegalArgumentException("GroupId cannot be null");
        }

        setContentView(R.layout.activity_event_entry);

        Calendar cStart = CalendarUtils.getNextStart();
        Calendar cEnd = CalendarUtils.getNextEnd();

        TextView b = (TextView) findViewById(R.id.fromDate);
        TextView b1 = (TextView) findViewById(R.id.toDate);
        b.setText(CalendarUtils.getDateString(cStart));
        b.setOnClickListener(this);
        b.setKeyListener(null);
        b1.setText(CalendarUtils.getDateString(cEnd));
        b1.setOnClickListener(this);
        b1.setKeyListener(null);

        TextView c = (TextView) findViewById(R.id.fromTime);
        TextView c1 = (TextView) findViewById(R.id.toTime);
        c.setOnClickListener(this);
        c.setText(CalendarUtils.getTimeString(cStart));
        c.setKeyListener(null);
        c1.setOnClickListener(this);
        c1.setText(CalendarUtils.getTimeString(cEnd));
        c1.setKeyListener(null);

        this.currentTextColor = c1.getCurrentTextColor();

        Button cancel = (Button) findViewById(R.id.cancelButton);
        Button done = (Button) findViewById(R.id.doneButton);
        cancel.setOnClickListener(this);
        done.setOnClickListener(this);

        //If we are getting an object in here, let's render that!
        Serializable ee = getIntent().getSerializableExtra("EventObject");
        if (ee != null) {
            Event e = (Event)ee;
            ((TextView)findViewById(R.id.eventName)).setText(e.getName());
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fromDate:
            case R.id.toDate:
                launchDatePicker(v.getId());
                break;
            case R.id.fromTime:
            case R.id.toTime:
                launchTimePicker(v.getId());
                break;
            case R.id.doneButton:
                doSaveEvent();
                break;
            case R.id.cancelButton:
            default:
                this.finish();
                break;

        }
    }

    protected void doSaveEvent() {
        String eventName = getTextString(R.id.eventName);
        String startDate = getTextString(R.id.fromDate);
        String endDate = getTextString(R.id.toDate);
        String startTime = getTextString(R.id.fromTime);
        String endTime = getTextString(R.id.toTime);

        //If event name is empty, do not allow save.
        if (eventName.isEmpty()) {
            findViewById(R.id.eventName).requestFocus();
            return;
        }

        Calendar startCalendar = CalendarUtils.fromDateTimeString(startDate, startTime);
        Calendar endCalendar = CalendarUtils.fromDateTimeString(endDate, endTime);

        Event addE = new Event(
                selectedGroupId,
                startCalendar.getTime(),
                endCalendar.getTime(),
                eventName);

        FirebaseManager.getInstance().addEvent(addE, new PostWriteAction<Event>(addE) {
            @Override
            public void doAction(Event event) {
                Toast.makeText(EventEntryActivity.this, "Added Event", Toast.LENGTH_LONG).show();
                EventEntryActivity.this.finish();
            }

            @Override
            public void onError(Event event) {

            }
        });
    }

    private String getTextString(int id) {
        return ((TextView)findViewById(id)).getText().toString();
    }

    protected void launchDatePicker(int originWidgetId) {
        TextView dt = (TextView)findViewById(originWidgetId);
        Calendar c = CalendarUtils.fromDateString(dt.getText().toString());
        DateBag db = DateBag.getInstance(c);

        CalendarDatePickerDialog dpb =
                CalendarDatePickerDialog.newInstance(this, db.year, db.month, db.day);
        Bundle bundle = new Bundle();
        bundle.putInt(ORIGIN_WIDGET, originWidgetId);
        dpb.setArguments(bundle);
        dpb.show(getSupportFragmentManager(), "datePicker");
    }

    protected void launchTimePicker(int originWidgetId) {
        TextView dt = (TextView)findViewById(originWidgetId);
        Calendar c = CalendarUtils.fromTimeString(dt.getText().toString());

        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        RadialTimePickerDialog rtp =
                RadialTimePickerDialog.newInstance(this, hour, minute, false);
        Bundle bundle = new Bundle();
        bundle.putInt(ORIGIN_WIDGET, originWidgetId);
        rtp.setArguments(bundle);
        rtp.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int month, int dom) {
        Calendar c = Calendar.getInstance();
        c.set(year, month, dom);

        int originId = calendarDatePickerDialog.getArguments().getInt(ORIGIN_WIDGET);
        TextView tvFromDate = (TextView) findViewById(originId);
        tvFromDate.setText(CalendarUtils.getDateString(c));

        datesRectified();
    }

    @Override
    public void onTimeSet(RadialTimePickerDialog radialTimePickerDialog, int i, int i2) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, i);
        c.set(Calendar.MINUTE, i2);
        c.set(Calendar.SECOND, 0);

        int originId = radialTimePickerDialog.getArguments().getInt(ORIGIN_WIDGET);
        TextView tvFromTime = (TextView) findViewById(originId);
        tvFromTime.setText(CalendarUtils.getTimeString(c));

        datesRectified();
    }

    public boolean datesRectified() {
        Calendar cStart = CalendarUtils.fromDateTimeString(
                getTextString(R.id.fromDate),
                getTextString(R.id.fromTime)
        );

        Calendar cEnd = CalendarUtils.fromDateTimeString(
                getTextString(R.id.toDate),
                getTextString(R.id.toTime)
        );

        Calendar modifiedEnd = CalendarUtils.normalizeCalendars(cStart, cEnd);

        if (!modifiedEnd.equals(cEnd)) {
            TextView tvDate = (TextView)findViewById(R.id.toDate);
            tvDate.setText(CalendarUtils.getDateString(modifiedEnd));

            TextView tvTime = (TextView)findViewById(R.id.toTime);
            tvTime.setText(CalendarUtils.getTimeString(modifiedEnd));

            ViewUtils.animateField(this, tvDate, this.currentTextColor);
            ViewUtils.animateField(this, tvTime, this.currentTextColor);

            return true;
        }

        return false;
    }
}
