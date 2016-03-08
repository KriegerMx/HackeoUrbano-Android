package mx.krieger.hackeourbano.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mx.krieger.hackeourbano.R;

public class GenericListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<UISimpleListElement> mData;
    private View.OnClickListener listener;

    public GenericListAdapter(ArrayList<UISimpleListElement> pendingUploads, View.OnClickListener listener) {
        mData = pendingUploads;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_category, parent, false);
        return new CategoryViewHolder(v, listener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UISimpleListElement mCurrentElement = mData.get(position);
        CategoryViewHolder categoryViewHolder = (CategoryViewHolder) holder;
        categoryViewHolder.tvTitle.setText(mCurrentElement.title);
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

    private class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public View parent;

        public CategoryViewHolder(View v, View.OnClickListener listener) {
            super(v);
            parent = v;
            tvTitle = (TextView) v.findViewById(R.id.view_list_category_tv_title);
            parent.setOnClickListener(listener);
        }
    }
}
