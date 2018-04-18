package com.bzh.dytt.home;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bzh.dytt.R;
import com.bzh.dytt.SingleActivity;
import com.bzh.dytt.data.entity.VideoDetail;
import com.bzh.dytt.util.GlideApp;

import java.lang.ref.SoftReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieItemHolder> {

    protected List<VideoDetail> mItems;
    private Context mContext;
    protected int mDataVersion = 0;

    public MovieListAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public MovieListAdapter.MovieItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_child, parent, false);
        return new MovieListAdapter.MovieItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MovieListAdapter.MovieItemHolder holder, int position) {
        final VideoDetail videoDetail = mItems.get(position);

        if (videoDetail != null) {

            boolean isChinaCountry = !TextUtils.isEmpty(videoDetail.getCountry()) &&
                    videoDetail.getCountry().contains(mContext.getString(R.string.china));
            holder.VideoTitleTv.setText(isChinaCountry ? videoDetail.getName() : videoDetail.getTranslationName());

            holder.VideoPublishTv.setText(videoDetail.getPublishTime());

            if (!TextUtils.isEmpty(videoDetail.getDoubanGrade())) {
                holder.DoubanGradeTv.setText(mContext.getString(R.string.douban_grade, videoDetail.getDoubanGrade()));
            }

            if (!TextUtils.isEmpty(videoDetail.getIMDBGrade())) {
                holder.IMDBGradeTv.setText(mContext.getString(R.string.imdb_grade, videoDetail.getIMDBGrade()));
            }

            holder.VideoDescriptionTv.setText(videoDetail.getDescription());

            if (!TextUtils.isEmpty(videoDetail.getCoverUrl())) {
                GlideApp.with(mContext)
                        .load(videoDetail.getCoverUrl())
                        .placeholder(R.drawable.default_video)
                        .into(holder.VideoCover);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Activity activity = getActivityByHolder(holder);
                    if (activity != null) {
                        SingleActivity.startDetailPage(activity, videoDetail.getDetailLink());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    @MainThread
    public void replace(final List<VideoDetail> update) {
        mDataVersion++;
        if (mItems == null) {
            if (update == null) {
                return;
            }
            mItems = update;
            notifyDataSetChanged();
        } else if (update == null) {
            int oldSize = mItems.size();
            mItems = null;
            notifyItemRangeRemoved(0, oldSize);
        } else {
            new UpdateTask(this, update).execute();
        }
    }


    private Activity getActivityByHolder(MovieListAdapter.MovieItemHolder holder) {
        if (holder.itemView.getContext() instanceof Activity) {
            return ((Activity) holder.itemView.getContext());
        }
        return null;
    }

    static class UpdateTask extends AsyncTask<Void, Void, DiffUtil.DiffResult> {

        SoftReference<MovieListAdapter> mReference;
        private final int mStartVersion;
        private final List<VideoDetail> mOldItems;
        private List<VideoDetail> mUpdate;

        UpdateTask(MovieListAdapter adapter, List<VideoDetail> update) {
            mReference = new SoftReference<>(adapter);
            mStartVersion = adapter.mDataVersion;
            mOldItems = adapter.mItems;
            mUpdate = update;
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(Void... voids) {
            return DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mOldItems.size();
                }

                @Override
                public int getNewListSize() {
                    return mUpdate.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    VideoDetail oldItem = mOldItems.get(oldItemPosition);
                    VideoDetail newItem = mUpdate.get(newItemPosition);
                    return TextUtils.equals(oldItem.getDetailLink(), newItem.getDetailLink());
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    VideoDetail oldItem = mOldItems.get(oldItemPosition);
                    VideoDetail newItem = mUpdate.get(newItemPosition);
                    return TextUtils.equals(oldItem.getName(), newItem.getName());
                }
            });
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            if (mReference.get() == null) {
                return;
            }
            if (mStartVersion != mReference.get().mDataVersion) {
                // ignore update
                return;
            }
            mReference.get().mItems = mUpdate;
            diffResult.dispatchUpdatesTo(mReference.get());
        }
    }

    class MovieItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.video_title)
        TextView VideoTitleTv;

        @BindView(R.id.video_publish_time)
        TextView VideoPublishTv;

        @BindView(R.id.douban_grade)
        TextView DoubanGradeTv;

        @BindView(R.id.imdb_grade)
        TextView IMDBGradeTv;

        @BindView(R.id.video_description)
        TextView VideoDescriptionTv;

        @BindView(R.id.video_cover)
        ImageView VideoCover;

        MovieItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
