package mx.krieger.hackeourbano.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.utils.Utils;
import mx.krieger.internal.commons.androidutils.fragment.GenericDialogFragment;
import mx.krieger.internal.commons.androidutils.listener.DialogResultListener;
import mx.krieger.mapaton.clients.hackeoUrbanoAPI.model.QuestionnaireWrapper;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener,
        TimePickerDialog.OnTimeSetListener, View.OnFocusChangeListener, DialogResultListener {

    public static final String EXTRA_TRAIL_ID = "TRAIL_ID";
    private EditText etTime;
    private EditText etComments;
    private RadioGroup rgType;
    private RadioGroup rgFullness;
    private AppCompatCheckBox[] cbSecurity;
    private AppCompatCheckBox[] cbVehicle;
    private AppCompatCheckBox[] cbRules;
    private AppCompatRatingBar rbRating;
    private DateTime time;
    private FeedbackTask task;
    private long trailId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getIntent().hasExtra(EXTRA_TRAIL_ID))
            trailId = getIntent().getLongExtra(EXTRA_TRAIL_ID, 0);

        if(trailId != 0) {
            setContentView(R.layout.activity_feedback);

            Toolbar toolbar = (Toolbar) findViewById(R.id.act_feedback_toolbar);
            View focusSlave = findViewById(R.id.act_feedback_slave_focus);
            etTime = (EditText) findViewById(R.id.act_feedback_et_time);
            etComments = (EditText) findViewById(R.id.act_feedback_et_comments);
            rgType = (RadioGroup) findViewById(R.id.act_feedback_rgroup_type);
            rgFullness = (RadioGroup) findViewById(R.id.act_feedback_rgroup_fullness);
            cbSecurity = new AppCompatCheckBox[]{
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_security_0),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_security_1),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_security_2),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_security_3)
            };
            cbVehicle = new AppCompatCheckBox[]{
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_vehicle_0),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_vehicle_1),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_vehicle_2),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_vehicle_3),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_vehicle_4)
            };
            cbRules = new AppCompatCheckBox[]{
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_0),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_1),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_2),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_3),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_4),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_5),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_6),
                    (AppCompatCheckBox) findViewById(R.id.act_feedback_cb_rules_7)
            };
            rbRating = (AppCompatRatingBar) findViewById(R.id.act_feedback_rb);
            findViewById(R.id.act_feedback_btn_send).setOnClickListener(this);

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setTitle(R.string.title_name_feedback);

            Drawable drawable = rbRating.getProgressDrawable();
            drawable.setColorFilter(getResources().getColor(R.color.app_accent_secondary_solid), PorterDuff.Mode.SRC_ATOP);

            etTime.setOnFocusChangeListener(this);
            etTime.setOnClickListener(this);
            focusSlave.requestFocus();
            return;
        }
        Toast.makeText(getApplicationContext(), R.string.error_navigation, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onStop() {
        if(task != null)
            task.cancel(false);
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.act_feedback_btn_send:
                QuestionnaireWrapper feedback = new QuestionnaireWrapper();
                feedback.setTransportType(rgType.getCheckedRadioButtonId());
                if(feedback.getTransportType() == -1){
                    Toast.makeText(getApplicationContext(), R.string.act_feedback_error_select_type, Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    switch (feedback.getTransportType()){
                        case R.id.act_feedback_rbtn_microbus:
                            feedback.setTransportType(0);
                            break;
                        case R.id.act_feedback_rbtn_van:
                            feedback.setTransportType(1);
                            break;
                        case R.id.act_feedback_rbtn_bus:
                            feedback.setTransportType(2);
                            break;
                        default:
                            feedback.setTransportType(-1);
                            break;
                    }
                }

                if(time == null){
                    Toast.makeText(getApplicationContext(), R.string.act_feedback_error_select_time, Toast.LENGTH_SHORT).show();
                    return;
                } else
                  feedback.setTimeTaken(time);

                feedback.setFullness(rgFullness.getCheckedRadioButtonId());
                if(feedback.getFullness() == -1){
                    Toast.makeText(getApplicationContext(), R.string.act_feedback_error_select_fullness, Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    switch (feedback.getFullness()){
                        case R.id.act_feedback_rbtn_fullness_0:
                            feedback.setFullness(0);
                            break;
                        case R.id.act_feedback_rbtn_fullness_1:
                            feedback.setFullness(1);
                            break;
                        case R.id.act_feedback_rbtn_fullness_2:
                            feedback.setFullness(2);
                            break;
                        case R.id.act_feedback_rbtn_fullness_3:
                            feedback.setFullness(3);
                            break;
                        default:
                            feedback.setFullness(-1);
                            break;
                    }
                }

                feedback.setRating(Math.round(rbRating.getRating()));
                if(feedback.getRating() < 1){
                    Toast.makeText(getApplicationContext(), R.string.act_feedback_error_select_rating, Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<Integer> list = new ArrayList<>();
                for(int i = 0, size = cbSecurity.length; i < size; i++){
                    AppCompatCheckBox cb = cbSecurity[i];
                    if(cb.isChecked())
                        list.add(i);
                }
                if(list.size() > 0)
                    feedback.setSecurity(list);

                list = new ArrayList<>();
                for(int i = 0, size = cbVehicle.length; i < size; i++){
                    AppCompatCheckBox cb = cbVehicle[i];
                    if(cb.isChecked())
                        list.add(i);
                }
                if(list.size() > 0)
                    feedback.setState(list);

                list = new ArrayList<>();
                for(int i = 0, size = cbRules.length; i < size; i++){
                    AppCompatCheckBox cb = cbRules[i];
                    if(cb.isChecked())
                        list.add(i);
                }
                if(list.size() > 0)
                    feedback.setTransitRegulation(list);

                String notes = etComments.getText().toString();
                if(notes != null && !notes.isEmpty())
                    feedback.setNotes(notes);

                feedback.setTrailId(trailId);

                task = new FeedbackTask();
                task.execute(feedback);
                break;
            case R.id.act_feedback_et_time:
                showTimePicker();
                break;
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        time = new DateTime(calendar.getTimeInMillis());
        DecimalFormat df = new DecimalFormat("00");
        etTime.setText(df.format(hourOfDay) + ":" + df.format(minute) + " hrs");
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus)
            switch (v.getId()){
                case R.id.act_feedback_et_time:
                    showTimePicker();
                    break;
            }
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tpDialog = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpDialog.show(getFragmentManager(), "TP_Dialog");
    }

    @Override
    public void onDialogResult(int eventType, int resultCode, Object data, View view) {}

    private class FeedbackTask extends AsyncTask<QuestionnaireWrapper, Void, String>{
        private GenericDialogFragment dialog;

        @Override
        protected void onPreExecute() {
            dialog = Utils.buildProgressDialog();
            dialog.show(getSupportFragmentManager(), "PROG");
        }

        @Override
        protected String doInBackground(QuestionnaireWrapper... params) {
            String errorMessage = getString(R.string.error_unknown);
            Context context = getApplicationContext();
            try {
                Utils.getHackeoUrbanoPublicAPI().registerQuestionnaire(params[0]).execute();
                errorMessage = null;
            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = Utils.manageAPIException(context, e);
            }
            return errorMessage;
        }

        @Override
        protected void onCancelled() {
            dialog.dismiss();
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            if (s == null) {
                Toast.makeText(getApplicationContext(), R.string.act_feedback_success, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(), R.string.app_error_preffix, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
