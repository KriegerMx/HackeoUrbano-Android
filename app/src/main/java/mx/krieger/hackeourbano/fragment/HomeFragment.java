package mx.krieger.hackeourbano.fragment;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.adapter.GenericListAdapter;
import mx.krieger.hackeourbano.object.UISimpleListElement;
import mx.krieger.hackeourbano.utils.Properties;
import mx.krieger.hackeourbano.utils.Utils;
import mx.krieger.internal.commons.androidutils.adapter.UpdateableAdapter;
import mx.krieger.internal.commons.androidutils.fragment.NavDrawerFragment;
import mx.krieger.internal.commons.androidutils.view.AsyncTaskRecyclerView;
import mx.krieger.labplc.clients.dashboardAPI.model.AreaWrapper;
import mx.krieger.labplc.clients.dashboardAPI.model.GPSLocation;
import mx.krieger.labplc.clients.dashboardAPI.model.TrailDetails;

public class HomeFragment extends NavDrawerFragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, //GoogleMap.OnCameraChangeListener,
        GoogleApiClient.OnConnectionFailedListener, AsyncTaskRecyclerView.TaskEventHandler, View.OnClickListener, GoogleMap.OnCameraChangeListener {
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AsyncTaskRecyclerView atrv;
    private View listContainer;
    private View listToggle;
    private ImageView ivToggle;
    private int listHeight;
    private int mapPadding;
    private boolean locationUnavailable = false;
    private Toolbar toolbar;
    private boolean mapHasLoaded = false;

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
        //ivToggle = (ImageView) v.findViewById(R.id.frag_home_toggle_icon);
        listToggle.setOnClickListener(this);

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

        mapPadding = (int) getResources().getDimension(R.dimen.app_keyline_text_small);
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

        if(mLastLocation != null) {
            moveToUserLocation();
        }else
            mapHasLoaded = true;
    }

    private void moveToUserLocation() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 15));
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            locationUnavailable = true;
            Toast.makeText(getContext(), R.string.app_location_unavailable, Toast.LENGTH_LONG).show();
        }else {
            if(mapHasLoaded){
                moveToUserLocation();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        locationUnavailable = true;
        Toast.makeText(getContext(), R.string.app_location_suspended, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        locationUnavailable = true;
        Toast.makeText(getContext(), R.string.app_location_failed, Toast.LENGTH_LONG).show();
    }

    private void obtainUserLocation() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if(mMap != null)
            atrv.requestNewTaskUpdate(
                    new AsyncTaskRecyclerView.TaskEventBundle(
                            this,
                            mMap.getProjection().getVisibleRegion().latLngBounds)
            );

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

            LatLngBounds bounds = (LatLngBounds) input;
            AreaWrapper area = new AreaWrapper();
            GPSLocation nePoint = new GPSLocation();
            nePoint.setLatitude(bounds.northeast.latitude);
            nePoint.setLongitude(bounds.northeast.longitude);
            area.setNorthEastCorner(nePoint);
            GPSLocation swPoint = new GPSLocation();
            nePoint.setLatitude(bounds.southwest.latitude);
            nePoint.setLongitude(bounds.southwest.longitude);
            area.setSouthWestCorner(swPoint);
            List<TrailDetails> trails = Utils.getMapatonPublicAPI().trailsNearPoint(area).execute().getItems();

            for(TrailDetails trail : trails){
                UISimpleListElement element = new UISimpleListElement();
                element.title = trail.getOriginStationName() + " - " + trail.getDestinationStationName();
                if (trail.getBranchName() != null)
                    element.title += (" (" + trail.getBranchName() + ")");
                element.id = trail.getTrailId();
                data.add(element);
            }

            if (data.size() == 0) {
                result.errorMessage = context.getString(R.string.error_empty_list);
            } else {
                result.data = data;
                result.errorMessage = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorMessage = getString(R.string.generic_server_error_message);
        }
        return result;
    }

    @Override
    public UpdateableAdapter buildAdapter(ArrayList<?> dataFromSuccessfulTask) {
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
            /*case R.id.view_list_office:
                FormalityElement.DetailOffice office = (FormalityElement.DetailOffice) ((UISimpleListElement) tag).data;
                goToDependencyDetail(office);
                break;*/
        }
    }
}
