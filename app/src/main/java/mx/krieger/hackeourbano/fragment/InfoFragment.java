package mx.krieger.hackeourbano.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import mx.krieger.hackeourbano.R;
import mx.krieger.internal.commons.androidutils.fragment.NavDrawerFragment;

public class InfoFragment extends NavDrawerFragment {
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        toolbar = (Toolbar) v.findViewById(R.id.frag_info_toolbar);

        String version = "?";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        }catch (Exception e){
            e.printStackTrace();
        }
        String s = "<b>HackeoUrbano Versión: "+version+"</b><br>Este producto fue desarrollado por PIDES Innovación Social y Krieger durante HackeoUrbano";
        ((TextView)v.findViewById(R.id.frag_info_tv)).setText(Html.fromHtml(s));
        return v;
    }

    @Override
    protected ToolbarConfiguration onToolbarConfiguration() {
        ToolbarConfiguration config = new ToolbarConfiguration();
        config.toolbar = toolbar;
        config.screenTitle = getString(R.string.section_name_info);
        config.drawerOpenStringID = R.string.drawer_open;
        config.drawerCloseStringID = R.string.drawer_close;
        return config;
    }
}
