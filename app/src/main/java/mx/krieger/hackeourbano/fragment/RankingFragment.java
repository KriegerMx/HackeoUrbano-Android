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

public class RankingFragment extends NavDrawerFragment {
    private Toolbar toolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ranking, container, false);
        toolbar = (Toolbar) v.findViewById(R.id.frag_ranking_toolbar);

        return v;
    }

    @Override
    protected ToolbarConfiguration onToolbarConfiguration() {
        ToolbarConfiguration config = new ToolbarConfiguration();
        config.toolbar = toolbar;
        config.screenTitle = getString(R.string.section_name_ranking);
        config.drawerOpenStringID = R.string.drawer_open;
        config.drawerCloseStringID = R.string.drawer_close;
        return config;
    }
}
