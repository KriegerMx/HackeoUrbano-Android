package mx.krieger.hackeourbano.adapter;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

import mx.krieger.hackeourbano.R;
import mx.krieger.hackeourbano.object.UISimpleListElement;
import mx.krieger.internal.commons.androidutils.adapter.UpdateableAdapter;

public class RankingListAdapter extends UpdateableAdapter<RecyclerView.ViewHolder> {
    private ArrayList<UISimpleListElement> mData;
    private View.OnClickListener listener;

    public RankingListAdapter(ArrayList<UISimpleListElement> pendingUploads, View.OnClickListener listener) {
        mData = pendingUploads;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_ranking, parent, false);
        return new RankingViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UISimpleListElement mCurrentElement = mData.get(position);
        RankingViewHolder categoryViewHolder = (RankingViewHolder) holder;
        categoryViewHolder.tvTitle.setText(mCurrentElement.title);
        categoryViewHolder.rbRating.setRating(mCurrentElement.rating);

        Drawable drawable = categoryViewHolder.rbRating.getProgressDrawable();
        drawable.setColorFilter(((RankingViewHolder) holder).parent.getContext().getResources().getColor(R.color.app_accent_secondary_solid), PorterDuff.Mode.SRC_ATOP);
        categoryViewHolder.parent.setTag(mCurrentElement);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void updateDataset(ArrayList<?> arrayList) {
        this.mData = (ArrayList<UISimpleListElement>) arrayList;
        notifyDataSetChanged();
    }

    private class RankingViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public RatingBar rbRating;
        public View parent;

        public RankingViewHolder(View v, View.OnClickListener listener) {
            super(v);
            parent = v;
            tvTitle = (TextView) v.findViewById(R.id.view_list_ranking_tv_title);
            rbRating = (RatingBar) v.findViewById(R.id.view_list_ranking_rb);
            parent.setOnClickListener(listener);
        }
    }
}
