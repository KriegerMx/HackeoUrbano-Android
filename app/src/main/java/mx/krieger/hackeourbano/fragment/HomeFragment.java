package mx.krieger.hackeourbano.fragment;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.activity.TrailDetailActivity;
import mx.krieger.hackeourbano.adapter.GenericListAdapter;
import mx.krieger.hackeourbano.object.UIPoint;
import mx.krieger.hackeourbano.object.UISimpleListElement;
import mx.krieger.hackeourbano.object.UITrail;
import mx.krieger.hackeourbano.utils.Properties;
import mx.krieger.hackeourbano.utils.Utils;
import mx.krieger.internal.commons.androidutils.adapter.UpdateableAdapter;
import mx.krieger.internal.commons.androidutils.fragment.NavDrawerFragment;
import mx.krieger.internal.commons.androidutils.view.AsyncTaskRecyclerView;
import mx.krieger.mapaton.clients.mapatonPublicAPI.model.NearTrails;

public class HomeFragment extends NavDrawerFragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        AsyncTaskRecyclerView.TaskEventHandler, View.OnClickListener, GoogleMap.OnCameraChangeListener {
    private static final float MINIMUM_ZOOM = 15;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AsyncTaskRecyclerView atrv;
    private View listContainer;
    private View listToggle;
    private int listHeight;
    private Toolbar toolbar;
    private boolean mapHasLoaded = false;
    private TextView listToggleLabel;
    private boolean firstTime = true;
    private View btnSearchArea;
    private ArrayList<UITrail> trailList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        toolbar = (Toolbar) v.findViewById(R.id.frag_home_toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        listContainer = v.findViewById(R.id.frag_home_container_list);
        listToggle = v.findViewById(R.id.frag_home_toggle_list);
        listToggleLabel = (TextView) v.findViewById(R.id.frag_home_toggle_label);
        btnSearchArea = v.findViewById(R.id.frag_home_btn_reload);

        listToggle.setOnClickListener(this);
        btnSearchArea.setOnClickListener(this);

        atrv = (AsyncTaskRecyclerView) v.findViewById(R.id.frag_home_atrv);
        AsyncTaskRecyclerView.PlaceholderViewContainer ph = new AsyncTaskRecyclerView.PlaceholderViewContainer();
        ph.view = inflater.inflate(R.layout.view_placeholder, atrv, false);
        ph.imageViewForPlaceholderId = R.id.view_placeholder_iv;
        ph.textViewForMessageId = R.id.view_placeholder_tv;
        View loader = inflater.inflate(R.layout.view_loading, atrv, false);
        atrv.init(ph, loader, new AsyncTaskRecyclerView.TaskEventBundle(this, null));

        listToggle.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                listToggle.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int toggleHeight = listToggle.getHeight();
                getView().findViewById(R.id.frag_home_container_map).setPadding(0, 0, 0, toggleHeight);
            }
        });

        atrv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                atrv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                listHeight = atrv.getHeight();
                listContainer.animate().translationY(listHeight).setDuration(0).start();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.frag_home_map);
        mapFragment.getMapAsync(this);
        obtainUserLocation();
        return v;
    }

    @Override
    protected ToolbarConfiguration onToolbarConfiguration() {
        ToolbarConfiguration config = new ToolbarConfiguration();
        config.toolbar = toolbar;
        config.screenTitle = getString(R.string.section_name_home);
        config.drawerOpenStringID = R.string.drawer_open;
        config.drawerCloseStringID = R.string.drawer_close;
        return config;
    }

    public void onStart() {
        if(mGoogleApiClient != null && !(mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()))
            mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        if(mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
        atrv.cancelAsyncTask();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        mMap.setOnCameraChangeListener(this);
        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setZoomControlsEnabled(true);

        if(mLastLocation != null) {
            moveToUserLocation();
        }else
            mapHasLoaded = true;
    }

    private void moveToUserLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 17));
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            Toast.makeText(getContext(), R.string.app_location_unavailable, Toast.LENGTH_LONG).show();
        }else {
            if(mapHasLoaded){
                moveToUserLocation();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getContext(), R.string.app_location_suspended, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getContext(), R.string.app_location_failed, Toast.LENGTH_LONG).show();
    }

    private void obtainUserLocation() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(mMap != null) {
            if (firstTime) {
                if(cameraPosition.zoom < MINIMUM_ZOOM){
                    Toast.makeText(getContext(), R.string.error_zoom_too_low, Toast.LENGTH_LONG).show();
                }else {
                    reloadMapData();
                    firstTime = false;
                }
            } else
                showReloadButton();
        }
    }

    @Override
    public AsyncTaskRecyclerView.ResultBundle performTask(AsyncTask runningTask, Object input) {
        AsyncTaskRecyclerView.ResultBundle result = new AsyncTaskRecyclerView.ResultBundle(
                null, null, 0);
        Context context;
        try {
            context = getContext();
            result = new AsyncTaskRecyclerView.ResultBundle(
                    getString(R.string.error_unknown), null, 0);

            ArrayList<UISimpleListElement> data = new ArrayList<>();
            if(input == null){
                result.shouldShowErrorIfEmpty = false;
                result.data = data;
                return result;
            }

            if(runningTask.isCancelled()) return result;

            LatLngBounds bounds = (LatLngBounds) input;
            Log.d("DBG", "NE: " + bounds.northeast.latitude + " | " + bounds.northeast.longitude);
            Log.d("DBG", "SW: " + bounds.southwest.latitude + " | " + bounds.southwest.longitude);

            JSONObject sData = new JSONObject();
            JSONObject neCorner = new JSONObject();
            neCorner.put("latitude", bounds.northeast.latitude);
            neCorner.put("longitude", bounds.northeast.longitude);
            sData.put("northEastCorner", neCorner);
            JSONObject swCorner = new JSONObject();
            swCorner.put("latitude", bounds.southwest.latitude);
            swCorner.put("longitude", bounds.southwest.longitude);
            sData.put("southWestCorner", swCorner);

            if(runningTask.isCancelled()) return result;

            String stringResponse = Utils.doSimpleRequest(Utils.SERVICE_TRAILS_NEAR_POINT, sData);

            if(runningTask.isCancelled()) return result;

            JSONObject resultingData = new JSONObject(stringResponse);
            JSONArray rawTrails = resultingData.getJSONArray("items");
            List<NearTrails> trails = new ArrayList<>();
            for(int i = 0, size = rawTrails.length(); i < size; i++){
                JSONObject current = rawTrails.getJSONObject(i);
                NearTrails temp = new NearTrails();
                temp.setTrailId(current.getLong("trailId"));
                temp.setOriginName(current.getString("originName"));
                temp.setDestinationName(current.getString("destinationName"));
                if(current.has("branchName"))
                    temp.setBranchName(current.getString("branchName"));
                trails.add(temp);
            }

            if(runningTask.isCancelled()) return result;

            if (trails.size() == 0) {
                result.errorMessage = context.getString(R.string.error_empty_list);
            } else {
                trailList = new ArrayList<>();
                for(NearTrails trail : trails){
                    try {
                        UITrail uiTrail = new UITrail();
                        uiTrail.id = trail.getTrailId();
                        uiTrail.originName = trail.getOriginName();
                        uiTrail.destinationName = trail.getDestinationName();
                        uiTrail.branchName = trail.getBranchName();

                        UISimpleListElement element = new UISimpleListElement();
                        element.title = uiTrail.originName + " - " + uiTrail.destinationName;
                        element.originName = uiTrail.originName;
                        element.destinationName = uiTrail.destinationName;
                        if (uiTrail.branchName != null)
                            element.title += (" (" + uiTrail.branchName + ")");
                        element.id = uiTrail.id;

                        if(runningTask.isCancelled()) return result;

                        uiTrail.points = Utils.getTrailPoints(context, uiTrail.id);
                        trailList.add(uiTrail);
                        data.add(element);

                        if(runningTask.isCancelled()) return result;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, R.string.error_obtain_trail, Toast.LENGTH_SHORT).show();
                    }
                }
                if (trailList.size() == 0) {
                    result.errorMessage = context.getString(R.string.error_empty_list);
                } else {
                    result.data = data;
                    result.errorMessage = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorMessage = Utils.manageAPIException(getContext(), e);
        }
        return result;
    }

    @Override
    public UpdateableAdapter buildAdapter(ArrayList<?> dataFromSuccessfulTask) {
        listToggleLabel.setText(getString(R.string.frag_home_toggle_text_preffix) + " " + dataFromSuccessfulTask.size());
        int[] colors = Utils.generateColorsByDividingSpectrum(trailList.size());

        if(mMap != null){
            mMap.clear();
            for(int i = 0, size = trailList.size(); i < size; i++){
                UITrail trail = trailList.get(i);
                PolylineOptions line = new PolylineOptions();
                line.color(colors[i]);
                for(UIPoint point : trail.points)
                    line.add(new LatLng(point.latitude, point.longitude));
                mMap.addPolyline(line);
            }
        }

        return new GenericListAdapter((ArrayList<UISimpleListElement>) dataFromSuccessfulTask, this);
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        switch (v.getId()){
            case R.id.frag_home_toggle_list:
                boolean isOpened = tag != null && (boolean) tag;
                if(isOpened){
                    //ivToggle.setImageResource(R.drawable.ic_more);
                    listContainer.animate().translationY(listHeight).setDuration(Properties.ANIMATION_DURATION).start();
                }else{
                    //ivToggle.setImageResource(R.drawable.ic_less);
                    listContainer.animate().translationY(0).setDuration(Properties.ANIMATION_DURATION).start();
                }
                v.setTag(!isOpened);
                break;
            case R.id.frag_home_btn_reload:
                if(mMap != null) {
                    if (mMap.getCameraPosition().zoom < MINIMUM_ZOOM) {
                        Toast.makeText(getContext(), R.string.error_zoom_too_low, Toast.LENGTH_LONG).show();
                    } else {
                        reloadMapData();
                    }
                }else {
                    Toast.makeText(getContext(), R.string.error_map_has_not_loaded, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.view_list_simple:
                UISimpleListElement input = (UISimpleListElement) tag;
                Intent i = new Intent(getContext(), TrailDetailActivity.class);
                i.putExtra(TrailDetailActivity.EXTRA_TRAIL, input);
                startActivity(i);
                break;
        }
    }

    private void reloadMapData() {
        btnSearchArea.setVisibility(View.GONE);
        listToggleLabel.setText(R.string.frag_home_toggle_text_loading);
        atrv.requestNewTaskUpdate(
                new AsyncTaskRecyclerView.TaskEventBundle(
                        this,
                        mMap.getProjection().getVisibleRegion().latLngBounds)
        );
    }

    private void showReloadButton(){
        btnSearchArea.setVisibility(View.VISIBLE);
    }
}
