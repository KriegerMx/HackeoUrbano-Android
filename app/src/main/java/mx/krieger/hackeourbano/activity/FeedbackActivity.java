package mx.krieger.hackeourbano.activity;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ProgressBar;

import mx.krieger.hackeourbano.R;

public class FeedbackActivity extends AppCompatActivity {

    public static final String EXTRA_TRAIL_ID = "TRAIL_ID";
    private AppCompatRatingBar rbRating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.act_feedback_toolbar);
        rbRating = (AppCompatRatingBar) findViewById(R.id.act_feeback_rb);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.title_name_feedback);

        Drawable drawable = rbRating.getProgressDrawable();
        drawable.setColorFilter(getResources().getColor(R.color.app_accent_secondary_solid), PorterDuff.Mode.SRC_ATOP);
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
}
