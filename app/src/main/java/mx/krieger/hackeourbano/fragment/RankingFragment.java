package mx.krieger.hackeourbano.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.activity.TrailDetailActivity;
import mx.krieger.hackeourbano.adapter.GenericListAdapter;
import mx.krieger.hackeourbano.adapter.RankingListAdapter;
import mx.krieger.hackeourbano.object.UISimpleListElement;
import mx.krieger.hackeourbano.object.UITrail;
import mx.krieger.hackeourbano.utils.Utils;
import mx.krieger.internal.commons.androidutils.adapter.UpdateableAdapter;
import mx.krieger.internal.commons.androidutils.fragment.NavDrawerFragment;
import mx.krieger.internal.commons.androidutils.view.AsyncTaskRecyclerView;
import mx.krieger.mapaton.clients.hackeoUrbanoAPI.model.RouteStatsParameter;
import mx.krieger.mapaton.clients.hackeoUrbanoAPI.model.RouteStatsWrapper;

public class RankingFragment extends NavDrawerFragment implements AsyncTaskRecyclerView.TaskEventHandler, View.OnClickListener {
    private Toolbar toolbar;
    private AsyncTaskRecyclerView atrv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ranking, container, false);
        toolbar = (Toolbar) v.findViewById(R.id.frag_ranking_toolbar);

        atrv = (AsyncTaskRecyclerView) v.findViewById(R.id.frag_ranking_atrv);
        AsyncTaskRecyclerView.PlaceholderViewContainer ph = new AsyncTaskRecyclerView.PlaceholderViewContainer();
        ph.view = inflater.inflate(R.layout.view_placeholder, atrv, false);
        ph.imageViewForPlaceholderId = R.id.view_placeholder_iv;
        ph.textViewForMessageId = R.id.view_placeholder_tv;
        View loader = inflater.inflate(R.layout.view_loading, atrv, false);
        atrv.init(ph, loader, new AsyncTaskRecyclerView.TaskEventBundle(this, null));

        return v;
    }

    @Override
    public void onStop() {
        atrv.cancelAsyncTask();
        super.onStop();
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
            RouteStatsParameter wsInput = new RouteStatsParameter();
            wsInput.setNumberOfElements(20);
            wsInput.setCursor(null);
            wsInput.setDescending(true);
            List<RouteStatsWrapper> rawData = Utils.getHackeoUrbanoPublicAPI().getAllStats(wsInput).execute().getItems();

            if (rawData == null || rawData.size() == 0) {
                result.errorMessage = context.getString(R.string.error_empty_list);
            } else {
                for(RouteStatsWrapper currentElement : rawData){
                    UISimpleListElement uiElement = new UISimpleListElement();
                    uiElement.id = currentElement.getId();
                    uiElement.originName = currentElement.getOriginStation();
                    uiElement.destinationName = currentElement.getDestinyStation();
                    uiElement.title = currentElement.getOriginStation() + " - " + currentElement.getDestinyStation();
                    uiElement.rating = currentElement.getRating();
                    data.add(uiElement);
                }
                result.data = data;
                result.errorMessage = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorMessage = Utils.manageAPIException(getContext(), e);
        }
        return result;
    }

    @Override
    public UpdateableAdapter buildAdapter(ArrayList<?> dataFromSuccessfulTask) {
        return new RankingListAdapter((ArrayList<UISimpleListElement>) dataFromSuccessfulTask, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.view_list_ranking:
                UISimpleListElement tag = (UISimpleListElement) v.getTag();
                Intent i = new Intent(getContext(), TrailDetailActivity.class);
                i.putExtra(TrailDetailActivity.EXTRA_TRAIL, tag);
                startActivity(i);
                break;
        }
    }
}
