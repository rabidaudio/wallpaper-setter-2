package audio.rabid.rotatingwallpapers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int JOB_ID = 1;

    JobScheduler jobScheduler;
    SharedPreferences sharedPreferences;

    Switch enabledSwitch;
    EditText periodValueText;
    Spinner unitSpinner;
    Switch wifiOnlySwitch;
    Switch idleOnlySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        sharedPreferences = getSharedPreferences("WALLPAPER_PREFERENCES", MODE_PRIVATE);

        enabledSwitch = (Switch) findViewById(R.id.enabled);
        periodValueText = (EditText) findViewById(R.id.period_value);
        unitSpinner = (Spinner) findViewById(R.id.period_unit);
        wifiOnlySwitch = (Switch) findViewById(R.id.wifi_only_switch);
        idleOnlySwitch = (Switch) findViewById(R.id.idle_only_switch);

        enabledSwitch.setChecked(sharedPreferences.getBoolean("enabled", true));
        periodValueText.setText(String.valueOf(sharedPreferences.getLong("period", 24)));
        List<String> units = Arrays.asList(getResources().getStringArray(R.array.time_intervals));
        int unitIndex = units.indexOf(sharedPreferences.getString("unit", "hours"));
        unitSpinner.setSelection(unitIndex);
        wifiOnlySwitch.setChecked(sharedPreferences.getBoolean("wifi_only", true));
        idleOnlySwitch.setChecked(sharedPreferences.getBoolean("idle_only", true));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                boolean enabled = enabledSwitch.isChecked();
                long periodValue = Long.parseLong(periodValueText.getText().toString());
                String unitValue = (String) unitSpinner.getSelectedItem();
                boolean wifiOnly = wifiOnlySwitch.isChecked();
                boolean idleOnly = idleOnlySwitch.isChecked();

                sharedPreferences.edit()
                        .putBoolean("enabled", enabled)
                        .putLong("period", periodValue)
                        .putString("unit", unitValue)
                        .putBoolean("wifi_only", wifiOnly)
                        .putBoolean("idle_only", idleOnly)
                        .apply();

                if (enabled) {
                    scheduleForInterval(getPeriod(periodValue, unitValue), wifiOnly, idleOnly);
                } else {
                    disable();
                }
                Toast.makeText(this, "Saved Changes", Toast.LENGTH_LONG).show();
                finish();
                return true;
            case R.id.action_refresh:
                runNow();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private long getPeriod(long periodValue, String unit) {
        switch (unit) {
            case "weeks":
                return periodValue * 1000 * 60 * 60 * 24 * 7;
            case "days":
                return periodValue * 1000 * 60 * 60 * 24;
            case "hours":
                return periodValue * 1000 * 60 * 60;
            case "minutes":
                return periodValue * 1000 * 60;
            default:
                throw new IllegalArgumentException("Invalid unit: " + unit);
        }
    }

    private void disable() {
        jobScheduler.cancel(JOB_ID);
    }

    private void scheduleForInterval(long periodMillis, boolean wifiOnly, boolean idleOnly) {
        ComponentName name = new ComponentName(this, WallpaperChangerJobService .class);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, name);
        builder.setPeriodic(periodMillis);
        builder.setPersisted(true);
        if (wifiOnly) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        }
        builder.setRequiresDeviceIdle(idleOnly);
        jobScheduler.schedule(builder.build());
    }

    private void runNow() {
        startService(new Intent(this, WallpaperChangerIntentService.class));
    }
}
