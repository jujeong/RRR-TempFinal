package com.example.subwayjpgsqluse.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.subwayjpgsqluse.R;
import com.example.subwayjpgsqluse.data.DatabaseHelper;
import com.example.subwayjpgsqluse.model.Alarm;
import com.example.subwayjpgsqluse.service.AlarmReceiver;
import com.example.subwayjpgsqluse.service.LoadAlarmsService;
import com.example.subwayjpgsqluse.util.ViewUtils;

import java.util.Calendar;

public final class AddEditAlarmFragment extends Fragment {

    private TimePicker mTimePicker;
    private EditText mLabel;
    private CheckBox mMon, mTues, mWed, mThurs, mFri, mSat, mSun;
    private CheckBox mSelectAll; // New checkbox for "Select All"
    private AutoCompleteTextView mStationSelect;
    private ArrayAdapter<String> lineAdapter;

    public static AddEditAlarmFragment newInstance(Alarm alarm) {
        Bundle args = new Bundle();
        args.putParcelable(AddEditAlarmActivity.ALARM_EXTRA, alarm);

        AddEditAlarmFragment fragment = new AddEditAlarmFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_add_edit_alarm, container, false);

        setHasOptionsMenu(true);

        final Alarm alarm = getAlarm();
        String[] lineNums = getResources().getStringArray(R.array.station_name);
        lineAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, lineNums);
        mStationSelect = v.findViewById(R.id.station_select);
        mStationSelect.setAdapter(lineAdapter);

        mStationSelect.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        mTimePicker = v.findViewById(R.id.edit_alarm_time_picker);
        ViewUtils.setTimePickerTime(mTimePicker, alarm.getTime());
        mLabel = v.findViewById(R.id.edit_alarm_label2);
        mLabel.setText(alarm.getLabel());
        mStationSelect.setText(alarm.getStationSelect());
        mMon = v.findViewById(R.id.edit_alarm_mon);
        mTues = v.findViewById(R.id.edit_alarm_tues);
        mWed = v.findViewById(R.id.edit_alarm_wed);
        mThurs = v.findViewById(R.id.edit_alarm_thurs);
        mFri = v.findViewById(R.id.edit_alarm_fri);
        mSat = v.findViewById(R.id.edit_alarm_sat);
        mSun = v.findViewById(R.id.edit_alarm_sun);
        mSelectAll = v.findViewById(R.id.edit_alarm_select_all); // New checkbox


        setDayCheckboxes(alarm);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_alarm_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                save();
                break;
            case R.id.action_delete:
                delete();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Alarm getAlarm() {
        return getArguments().getParcelable(AddEditAlarmActivity.ALARM_EXTRA);
    }

    private void setDayCheckboxes(Alarm alarm) {
        mMon.setChecked(alarm.getDay(Alarm.MON));
        mTues.setChecked(alarm.getDay(Alarm.TUES));
        mWed.setChecked(alarm.getDay(Alarm.WED));
        mThurs.setChecked(alarm.getDay(Alarm.THURS));
        mFri.setChecked(alarm.getDay(Alarm.FRI));
        mSat.setChecked(alarm.getDay(Alarm.SAT));
        mSun.setChecked(alarm.getDay(Alarm.SUN));

        boolean allChecked = mMon.isChecked() && mTues.isChecked() && mWed.isChecked()
                && mThurs.isChecked() && mFri.isChecked() && mSat.isChecked() && mSun.isChecked();
        mSelectAll.setOnCheckedChangeListener(null); // Remove listener temporarily
        mSelectAll.setChecked(allChecked);
        mSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectAllCheckboxes(isChecked);
            }
        });

        // Add OnCheckedChangeListener to individual day checkboxes
        mMon.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
        mTues.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
        mWed.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
        mThurs.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
        mFri.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
        mSat.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
        mSun.setOnCheckedChangeListener(dayCheckboxCheckedChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener dayCheckboxCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            updateSelectAllCheckboxState();
        }
    };

    private void updateSelectAllCheckboxState() {
        boolean allChecked = mMon.isChecked() && mTues.isChecked() && mWed.isChecked()
                && mThurs.isChecked() && mFri.isChecked() && mSat.isChecked() && mSun.isChecked();
        mSelectAll.setOnCheckedChangeListener(null); // Remove listener temporarily
        mSelectAll.setChecked(allChecked);
        mSelectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectAllCheckboxes(isChecked);
            }
        });
    }

    private void selectAllCheckboxes(boolean checked) {
        mMon.setChecked(checked);
        mTues.setChecked(checked);
        mWed.setChecked(checked);
        mThurs.setChecked(checked);
        mFri.setChecked(checked);
        mSat.setChecked(checked);
        mSun.setChecked(checked);
    }

    private void save() {
        // Retrieve the alarm object and update its properties
        final Alarm alarm = getAlarm();
        final Calendar time = Calendar.getInstance();
        final String selectedStation = mStationSelect.getText().toString();
        time.set(Calendar.MINUTE, ViewUtils.getTimePickerMinute(mTimePicker));
        time.set(Calendar.HOUR_OF_DAY, ViewUtils.getTimePickerHour(mTimePicker));
        time.set(Calendar.SECOND, 0);
        time.set(Calendar.MILLISECOND, 0);
        alarm.setTime(time.getTimeInMillis());
        alarm.setLabel(mLabel.getText().toString());
        alarm.setDay(Alarm.MON, mMon.isChecked());
        alarm.setDay(Alarm.TUES, mTues.isChecked());
        alarm.setDay(Alarm.WED, mWed.isChecked());
        alarm.setDay(Alarm.THURS, mThurs.isChecked());
        alarm.setDay(Alarm.FRI, mFri.isChecked());
        alarm.setDay(Alarm.SAT, mSat.isChecked());
        alarm.setDay(Alarm.SUN, mSun.isChecked());
        // Check if the selected station is valid
        if (!isValidStation(selectedStation)) {
            Toast.makeText(getContext(), "Invalid station selected", Toast.LENGTH_SHORT).show();
            return; // Don't save the alarm if the station is invalid
        }
        alarm.setStationSelect(selectedStation); // Update the station select field

        // Save the alarm to the database
        final int rowsUpdated = DatabaseHelper.getInstance(getContext()).updateAlarm(alarm);
        final int messageId = (rowsUpdated == 1) ? R.string.update_complete : R.string.update_failed;

        Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();

        AlarmReceiver.setReminderAlarm(getContext(), alarm);

        getActivity().finish();
    }

    private boolean isValidStation(String selectedStation) {
        String[] lineNums = getResources().getStringArray(R.array.station_name);
        for (String station : lineNums) {
            if (station.equals(selectedStation)) {
                return true; // Valid station found
            }
        }
        return false; // No valid station found
    }

    private void delete() {
        final Alarm alarm = getAlarm();

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_Subwayjpgsqluse);
        builder.setTitle(R.string.delete_dialog_title);
        builder.setMessage(R.string.delete_dialog_content);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Cancel any pending notifications for this alarm
                AlarmReceiver.cancelReminderAlarm(getContext(), alarm);

                final int rowsDeleted = DatabaseHelper.getInstance(getContext()).deleteAlarm(alarm);
                int messageId;
                if (rowsDeleted == 1) {
                    messageId = R.string.delete_complete;
                    Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                    LoadAlarmsService.launchLoadAlarmsService(getContext());
                    getActivity().finish();
                } else {
                    messageId = R.string.delete_failed;
                    Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
}