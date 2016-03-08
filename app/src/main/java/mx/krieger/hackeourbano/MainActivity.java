package mx.krieger.hackeourbano;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.fragment.HomeFragment;
import mx.krieger.hackeourbano.fragment.InfoFragment;
import mx.krieger.hackeourbano.fragment.RankingFragment;
import mx.krieger.internal.commons.androidutils.activity.NavDrawerActivity;

public class MainActivity extends NavDrawerActivity {
    public static final int FRAGMENT_HOME   = 1;
    public static final int FRAGMENT_RANKING = 2;
    public static final int FRAGMENT_ABOUT = 3;

    public static final int[] ELEMENT_IDS = {0,
            R.id.navdrawer_item_1,
            R.id.navdrawer_item_2,
            R.id.navdrawer_item_3};
    private static final int PERMISSION_REQ_LOCATION = 0;

    @Override
    protected void onCreateAction(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void firstExecution() {
        goToHome();
    }

    @Override
    protected void onFragmentRestore(int fragmentID, Bundle savedInstanceState) {
        switch (fragmentID) {
            case FRAGMENT_HOME:
            case FRAGMENT_RANKING:
            case FRAGMENT_ABOUT:
                navigateToItemAfterScreenRecovery(ELEMENT_IDS[fragmentID], NAV_TYPE_MENU, ELEMENT_IDS[FRAGMENT_HOME], NAV_TYPE_MENU);
                break;
            default:
                goToHome();
                break;
        }
    }

    @Override
    protected NavDrawerConfiguration configureNavDrawer() {
        View headerView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.view_navdrawer_header, null);
        NavDrawerActivity.NavDrawerConfiguration config = new NavDrawerActivity.NavDrawerConfiguration();
        config.navViewID = R.id.act_main_navview;
        config.drawerLayoutID = R.id.act_main_drawerlayout;
        config.headerView = headerView;
        return config;
    }

    @Override
    protected boolean onNavDrawerItemSelection(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.navdrawer_item_1:
                if(isLocationAccesible()) {
                    switchToFragment(FRAGMENT_HOME, new HomeFragment(), R.id.act_main_container_root, true);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.app_location_unavailable, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.navdrawer_item_2:
                switchToFragment(FRAGMENT_RANKING, new RankingFragment(), R.id.act_main_container_root);
                break;
            case R.id.navdrawer_item_3:
                switchToFragment(FRAGMENT_ABOUT, new InfoFragment(), R.id.act_main_container_root);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    navigateToItem(ELEMENT_IDS[FRAGMENT_HOME], NAV_TYPE_MENU);
                else
                    Toast.makeText(getApplicationContext(), R.string.app_location_unavailable, Toast.LENGTH_LONG).show();
                return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isLocationAccesible() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQ_LOCATION);
            return false;
        }
        return true;
    }

    private void goToHome() {
        if(isLocationAccesible()) {
            unlockNavDrawer();
            navigateToItem(ELEMENT_IDS[FRAGMENT_HOME], NAV_TYPE_MENU);
        } else {
            Toast.makeText(getApplicationContext(), R.string.app_location_unavailable, Toast.LENGTH_LONG).show();
        }
    }
}