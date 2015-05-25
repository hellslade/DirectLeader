package ru.tasu.directleader;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class SearchDirectumActivity extends Activity implements OnClickListener {
    private static final String TAG = "SearchDirectumActivity";
    
    public final int IMAGE_PICK_REQUEST_CODE = 0x000001;
    public final String CHECKED_ATTACHMENT_IDS = "checked_attachment_ids";
    
    class SearchDirectumAsyncTask extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pg = new ProgressDialog(SearchDirectumActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pg.setMessage(getResources().getString(R.string.search_direcrum_searchprocess_task_message));
            pg.setCancelable(false);
            pg.show();
        }
        @Override
        protected JSONObject doInBackground(String... params) {
            return mDirect.SearchDirectum(params[0]);
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            if (pg != null) {
                pg.dismiss();
            }
            JSONArray data = new JSONArray();
            if (result != null) {
                data = result.optJSONArray("data");
            }
            if (data.length() > 0) {
                Log.v(TAG, "search Directum ok");
                JSONObject attachJson;
                mDataSet.clear();
                mAdapter.notifyDataSetChanged();
                for (int i = 0; i < data.length(); i++) {
                    attachJson = data.optJSONObject(i);
                    if (attachJson != null) {
                        final Attachment attach = new Attachment(attachJson);
                        mDataSet.add(attach);
                    }
                }
                mAdapter.notifyDataSetChanged();
            } else {
                String text = getResources().getString(R.string.search_direcrum_searchfailed_message);
                Toast.makeText(mDirect, text, Toast.LENGTH_LONG).show();
                Log.v(TAG, "Почему-то не удалось получить данные. Обновление не произошло");
            }
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    
    private EditText searchEditText;
    
    private RecyclerView mRecyclerView;
    private SearchDirectumAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Attachment> mDataSet;
    
    private ArrayList<Long> checkedIds;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        mDirect = (DirectLeaderApplication) getApplication();
        mSettings = mDirect.getSettings();
        
        setContentView(R.layout.search_directum_activity);
        
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        // specify an adapter (see also next example)
        mDataSet = new ArrayList<Attachment>();
        mAdapter = new SearchDirectumAdapter((DirectLeaderApplication)getApplication(), mDataSet, clickListener);
        mRecyclerView.setAdapter(mAdapter);
        
        initData();
        setFonts();
    }
    private void setFonts() {
        searchEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void initData() {
    	checkedIds = new ArrayList<Long>();
        searchEditText = (EditText)findViewById(R.id.searchEditText);
        
        ((ImageButton)findViewById(R.id.cancelButton)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.okButton)).setOnClickListener(this);
        ((ImageButton)findViewById(R.id.searchButton)).setOnClickListener(this);
        
        setResult(Activity.RESULT_CANCELED, null);
    }
    OnClickListener clickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int itemPosition = mRecyclerView.getChildAdapterPosition(v);
			Attachment attach = mDataSet.get(itemPosition);
			Log.v(TAG, "clickListener " + itemPosition);
			if (attach != null) {
				if (checkedIds.contains(attach.getId())) {
					checkedIds.remove(attach.getId());
				} else {
					checkedIds.add(attach.getId());
				}
			}
			mAdapter.updateCheckedItems(checkedIds);
			mAdapter.notifyItemChanged(itemPosition);
		}
	};
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelButton:
                finish();
                break;
            case R.id.okButton:
            	Intent attachments = new Intent();
            	Bundle b = new Bundle();
            	attachments.putParcelableArrayListExtra(CHECKED_ATTACHMENT_IDS, checkedIds);
            	b.putParcelableArrayList(CHECKED_ATTACHMENT_IDS, checkedIds);
            	attachments.putExtras(b);
                setResult(Activity.RESULT_OK, attachments);
                break;
            case R.id.searchButton:
                String searchString = searchEditText.getText().toString();
                if (!searchString.isEmpty()) {
                    new SearchDirectumAsyncTask().execute(searchString);
                }
                break;
        }
    }
}