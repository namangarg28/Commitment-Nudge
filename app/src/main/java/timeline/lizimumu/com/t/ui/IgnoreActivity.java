package timeline.lizimumu.com.t.ui;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timeline.lizimumu.com.t.GlideApp;
import timeline.lizimumu.com.t.R;
import timeline.lizimumu.com.t.database.DbExecutor;
import timeline.lizimumu.com.t.database.IgnoreItem;
import timeline.lizimumu.com.t.util.AppUtil;

public class IgnoreActivity extends AppCompatActivity {

    private IgnoreAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ignore);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.ignore);
        }

        RecyclerView mList = findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new IgnoreAdapter();
        mList.setAdapter(mAdapter);

        new MyAsyncTask(this).execute();
    }

    class MyAsyncTask extends AsyncTask<Void, Void, List<IgnoreItem>> {

        private WeakReference<Context> mContext;

        MyAsyncTask(Context context) {
            mContext = new WeakReference<>(context);
        }

        @Override
        protected List<IgnoreItem> doInBackground(Void... voids) {
            return DbExecutor.getInstance().getAllItems();
        }

        @Override
        protected void onPostExecute(List<IgnoreItem> ignoreItems) {
            if (mContext.get() != null && ignoreItems.size() > 0) {
                for (IgnoreItem item : ignoreItems) {
                    item.mName = AppUtil.parsePackageName(mContext.get().getPackageManager(), item.mPackageName);
                }
                mAdapter.setData(ignoreItems);
            }
        }
    }

    class IgnoreAdapter extends RecyclerView.Adapter<IgnoreAdapter.IgnoreViewHolder> {

        private List<IgnoreItem> mData;

        IgnoreAdapter() {
            mData = new ArrayList<>();
        }

        void setData(List<IgnoreItem> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public IgnoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_ignore, parent, false);
            return new IgnoreViewHolder(view);
        }

        @Override
        public void onBindViewHolder(IgnoreViewHolder holder, int position) {
            IgnoreItem item = mData.get(position);
            holder.mCreated.setText(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(new Date(item.mCreated)));
            holder.mName.setText(item.mName);
            GlideApp.with(getApplicationContext())
                    .load(AppUtil.getPackageIcon(getApplicationContext(), item.mPackageName))
                    .transition(new DrawableTransitionOptions().crossFade())
                    .into(holder.mIcon);
            holder.setOnClickListener(item);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        class IgnoreViewHolder extends RecyclerView.ViewHolder {

            private ImageView mIcon;
            private ImageView mDelete;
            private TextView mName;
            private TextView mCreated;

            IgnoreViewHolder(View itemView) {
                super(itemView);
                mIcon = itemView.findViewById(R.id.app_image);
                mDelete = itemView.findViewById(R.id.app_delete);
                mName = itemView.findViewById(R.id.app_name);
                mCreated = itemView.findViewById(R.id.app_time);
            }

            void setOnClickListener(final IgnoreItem item) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DbExecutor.getInstance().deleteItem(item);
                        new MyAsyncTask(IgnoreActivity.this).execute();
                    }
                });
            }
        }
    }


}
