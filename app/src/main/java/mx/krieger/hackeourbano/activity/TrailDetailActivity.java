package mx.krieger.hackeourbano.activity;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.List;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.object.UIPoint;
import mx.krieger.hackeourbano.object.UISimpleListElement;
import mx.krieger.hackeourbano.object.UITrail;
import mx.krieger.hackeourbano.utils.Utils;
import mx.krieger.mapaton.clients.hackeoUrbanoAPI.HackeoUrbanoAPI;
import mx.krieger.mapaton.clients.hackeoUrbanoAPI.model.RouteStatsWrapper;
import mx.krieger.mapaton.clients.mapatonPublicAPI.MapatonPublicAPI;
import mx.krieger.mapaton.clients.mapatonPublicAPI.model.TrailDetails;

public class TrailDetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener {
    public static final String EXTRA_TRAIL = "Extra-trail";
    private View loader;
    private TextView tvTariffLabel;
    private TextView tvTypeLabel;
    private TextView tvTariff;
    private TextView tvType;
    private TextView tvRatingLabel;
    private TextView tvRating;
    private RatingBar rbRating;
    private View btnRate;
    private UISimpleListElement inputTrail;
    private TrailDetailsGetterTask task;
    private List<UIPoint> points;
    private GoogleMap mMap;
    private boolean trailHasBeenLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputTrail = null;
        if(getIntent().hasExtra(EXTRA_TRAIL)){
            inputTrail = (UISimpleListElement) getIntent().getSerializableExtra(EXTRA_TRAIL);
        }

        if(inputTrail != null) {
            setContentView(R.layout.activity_trail_detail);

            Toolbar toolbar = (Toolbar) findViewById(R.id.act_trail_detail_toolbar);
            TextView tvOrigin = (TextView) findViewById(R.id.act_trail_detail_tv_origin);
            TextView tvDestination = (TextView) findViewById(R.id.act_trail_detail_tv_destination);
            loader = findViewById(R.id.act_trail_detail_cpv);
            tvTariffLabel = (TextView) findViewById(R.id.act_trail_detail_tv_label_tariff);
            tvTariff = (TextView) findViewById(R.id.act_trail_detail_tv_tariff);
            tvTypeLabel = (TextView) findViewById(R.id.act_trail_detail_tv_label_type);
            tvType = (TextView) findViewById(R.id.act_trail_detail_tv_type);
            tvRatingLabel = (TextView) findViewById(R.id.act_trail_detail_tv_label_rating);
            tvRating = (TextView) findViewById(R.id.act_trail_detail_tv_rating);
            rbRating = (RatingBar) findViewById(R.id.act_trail_detail_rb);
            btnRate = findViewById(R.id.act_trail_detail_btn_rate);

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            tvOrigin.setText("Origen: " + inputTrail.originName);
            tvDestination.setText("Destino: " + inputTrail.destinationName);

            btnRate.setOnClickListener(this);

            GoogleMapOptions mapOptions = new GoogleMapOptions();
            mapOptions.tiltGesturesEnabled(false);

            SupportMapFragment mapFragment = SupportMapFragment.newInstance(mapOptions);
            getSupportFragmentManager().beginTransaction().add(R.id.act_trail_detail_container_map, mapFragment, "MAP").commit();
            mapFragment.getMapAsync(this);

            task = new TrailDetailsGetterTask();
            task.execute(inputTrail.id);
            return;
        }
        Toast.makeText(getApplicationContext(), R.string.error_navigation, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onStop() {
        if(task != null)
            task.cancel(true);
        super.onStop();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(points != null){
            showTrailOnMap();
        }
    }

    private void showTrailOnMap() {
        if(trailHasBeenLoaded)
            return;
        trailHasBeenLoaded = true;
        PolylineOptions polyline = new PolylineOptions();
        LatLngBounds.Builder boundBuilder = LatLngBounds.builder();

        for (int i = 0, size = points.size(); i < size; i++){
            UIPoint pos = points.get(i);
            LatLng ll = new LatLng(pos.latitude, pos.longitude);
            polyline.add(ll);
            boundBuilder.include(ll);
        }
        int padding = (int) getResources().getDimension(R.dimen.app_keyline_text_small);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), padding));
        mMap.addPolyline(polyline);
    }


    private class TrailDetailsGetterTask extends AsyncTask<Long, Void, String> {
        private TrailDetails trailDetails;
        private RouteStatsWrapper trailStats;

        @Override
        protected String doInBackground(Long... params) {
            String errorMsg = null;
            Context context = null;
            try {
                long param = params[0];
                context = getApplicationContext();
                MapatonPublicAPI mAPI = Utils.getMapatonPublicAPI();
                trailDetails = mAPI.getTrailDetails(param).execute();

                HackeoUrbanoAPI hAPI = Utils.getHackeoUrbanoPublicAPI();
                trailStats = hAPI.getStats(param).execute();

                points = Utils.getTrailPoints(context, param);
            } catch (Exception e) {
                errorMsg = Utils.manageAPIException(context, e);
            }
            return errorMsg;
        }

        @Override
        protected void onPostExecute(String error) {
            loader.setVisibility(View.GONE);
            if(error!=null){
                Toast.makeText(getApplicationContext(), getString(R.string.error_obtain_trail) + error, Toast.LENGTH_LONG).show();
            }else{
                if(mMap != null)
                    showTrailOnMap();

                tvTypeLabel.setVisibility(View.VISIBLE);
                tvType.setVisibility(View.VISIBLE);
                tvType.setText(trailDetails.getTransportType());

                tvTariffLabel.setVisibility(View.VISIBLE);
                tvTariff.setVisibility(View.VISIBLE);
                DecimalFormat df = new DecimalFormat("0.00");
                String s = df.format(trailDetails.getMaxTariff());
                tvTariff.setText("$" + s);

                tvRatingLabel.setVisibility(View.VISIBLE);
                if(trailStats.getRating() > 0) {
                    rbRating.setVisibility(View.VISIBLE);
                    rbRating.setRating(trailStats.getRating());

                    Drawable drawable = rbRating.getProgressDrawable();
                    drawable.setColorFilter(getResources().getColor(R.color.app_accent_secondary_solid), PorterDuff.Mode.SRC_ATOP);
                }else {
                    tvRating.setVisibility(View.VISIBLE);
                    tvRating.setText("Este recorrido no ha sido calificado aún.");
                }

                btnRate.setVisibility(View.VISIBLE);
            }
        }
    }

}
