package ru.tasu.directleader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DocumentsFragment extends Fragment {
    private static final String TAG = "DocumentsFragment";
    
    class GetAttachmentAsyncTask extends AsyncTask<Void, Void, List<Attachment>> {
        ProgressDialog pg;
        protected void onPreExecute() {
            super.onPreExecute();
            pg = new ProgressDialog(getActivity(), ProgressDialog.THEME_HOLO_LIGHT);
            pg.setMessage(getResources().getString(R.string.task_loading_data_text));
            if (!mRetained) {
                pg.show();
            }
        };
        @Override
        protected List<Attachment> doInBackground(Void... params) {
            AttachmentDataSource ads = new AttachmentDataSource(mDirect);
            ads.open();
            List<Attachment> attachments = ads.getAllAttachments();
            ads.close();
            return attachments;
        }
        @Override
        protected void onPostExecute(List<Attachment> result) {
            if (pg != null) {
                pg.dismiss();
            }
            mAdapter.clear();
            mAdapter.addAll(result);
            mAdapter.sort(comp);
            mAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }
    }
    //*/
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    private OnOpenFragmentListener mListener;
    
    private ListView docsListView;
    private DocumentsListAdapter mAdapter;
    private RelativeLayout listViewHeader;
    private CheckedTextView sortDateView, sortTitleView, sortAuthorView, sortTypeView;
    private ImageButton sortDirectionView;
    private boolean sortDesc = false;
    private EditText searchEditText;
    
    private boolean mRetained;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_documents, container, false);

        ((ImageView)rootView.findViewById(R.id.homeImageView)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    getFragmentManager().popBackStack(getFragmentManager().getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });
        ((ImageView)rootView.findViewById(R.id.newTaskButton)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnTaskCreate();
                }
            }
        });

        sortDateView = (CheckedTextView) rootView.findViewById(R.id.sortDateView);
        sortTitleView = (CheckedTextView) rootView.findViewById(R.id.sortTitleView);
        sortAuthorView = (CheckedTextView) rootView.findViewById(R.id.sortAuthorView);
        sortTypeView = (CheckedTextView) rootView.findViewById(R.id.sortTypeView);
        sortDirectionView = (ImageButton) rootView.findViewById(R.id.sortDirectionView);
        
        searchEditText = (EditText)rootView.findViewById(R.id.searchEditText);
        
        docsListView = (ListView) rootView.findViewById(R.id.docsListView);

//        listViewHeader = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.list_header_documents_layout, null);
//        docsListView.addHeaderView(listViewHeader, null, false);
//        docsListView.setHeaderDividersEnabled(false);
        
        mRetained = true;
        if (mAdapter == null) {
            mAdapter = new DocumentsListAdapter(getActivity(), new ArrayList<Attachment>(), docsListView);
            mRetained = false;
        }
        docsListView.setAdapter(mAdapter);
        
        setFonts();
        
        sortDateView.setOnClickListener(sortClickListener);
        sortTitleView.setOnClickListener(sortClickListener);
        sortAuthorView.setOnClickListener(sortClickListener);
        sortTypeView.setOnClickListener(sortClickListener);
        sortDirectionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDesc = !sortDesc;
                mAdapter.sort(comp);
                mAdapter.notifyDataSetChanged();
            }
        });
        
        docsListView.setOnItemClickListener(itemClickListener);
        initSearch();
        retainSortState();
        new GetAttachmentAsyncTask().execute();
        return rootView;
    }
    private void setFonts() {
//        int count = listViewHeader.getChildCount();
//        for (int i=0; i<count; i++) {
//            final View view = listViewHeader.getChildAt(i);
//            if (view.getClass().isInstance(TextView.class)) {
//                ((TextView)view).setTypeface(mDirect.mPFDinDisplayPro_Reg);
//            }
//        }
        sortDateView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTitleView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortAuthorView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        sortTypeView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        searchEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void initSearch() {
        // Add Text Change Listener to EditText
        searchEditText.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call back the Adapter with current character to Filter
                mAdapter.getFilter().filter(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
            // Загрузка и открытие документа.
            Attachment doc = mAdapter.getItem(pos-docsListView.getHeaderViewsCount());
//            Log.v(TAG, "documentClickListener " + doc.getName());
            boolean exist = mDirect.checkDocumentExist(doc);
//            Log.v(TAG, "doc exist " + exist);
            if (exist) {
//                Log.v(TAG, "open document");
                File myFile = mDirect.getDocumentFile(doc);
                try {
                    FileOpen.openFile(getActivity(), myFile);
                } catch (IOException e) {
                    Log.v(TAG, "Неудалось открыть документ " + e.getMessage());
                }
            } else {
                showDownloadDialog(doc);
            }
        }
    };
    private void showDownloadDialog(Attachment doc) {
        Log.v(TAG, "showDownloadDialog " + doc.getName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = DocumentDownloadDialogFragment.newInstance(doc);
        newFragment.show(ft, "download_dialog");
    }
    OnClickListener sortClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckedTextView view = (CheckedTextView)v;
            // Если кликнули по тому, который уже выделен, то ничего не делать.
            if (view.isChecked()) {
                return;
            }
            //Снять выделение со всех
            for (CheckedTextView cv : new CheckedTextView[]{sortDateView, sortTitleView, sortAuthorView, sortTypeView}) {
                cv.setChecked(false);
            }
            view.setChecked(true);
            mAdapter.sort(comp);
            mAdapter.notifyDataSetChanged();
        }
    };
    final private Comparator<Attachment> comp = new Comparator<Attachment>() {
        public int compare(Attachment a1, Attachment a2) {
            int result = 0;
            if (sortDateView.isChecked()) {
                result = a1.getModified().compareTo(a2.getModified());
            }
            if (sortTitleView.isChecked()) {
                result = a1.getCTitle().compareTo(a2.getCTitle());
            }
            if (sortAuthorView.isChecked()) {
                result = a1.getAuthorName().compareTo(a2.getAuthorName());
            }
            if (sortTypeView.isChecked()) {
                result = a1.getExt().compareTo(a2.getExt());
            }
            if (sortDesc) {
                return -result;
            } else {
                return result;
            }
        }
    };
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnOpenFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnOpenFragmentListener");
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveSortState();
    }
    private void saveSortState() {
        Editor e = mSettings.edit();
        e.putBoolean("documents_sort_date", sortDateView.isChecked());
        e.putBoolean("documents_sort_title", sortTitleView.isChecked());
        e.putBoolean("documents_sort_author", sortAuthorView.isChecked());
        e.putBoolean("documents_sort_type", sortTypeView.isChecked());
        e.commit();
    }
    private void retainSortState() {
        sortDateView.setChecked(mSettings.getBoolean("documents_sort_date", false));
        sortTitleView.setChecked(mSettings.getBoolean("documents_sort_title", false));
        sortAuthorView.setChecked(mSettings.getBoolean("documents_sort_author", false));
        sortTypeView.setChecked(mSettings.getBoolean("documents_sort_type", false));
    }
}
