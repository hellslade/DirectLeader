package ru.tasu.directleader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.tasu.directleader.DocumentDownloadDialogFragment.OnDocumentDownloadListener;
import ru.tasu.directleader.UsersDialogFragment.OnUserSelectListener;
import ru.tasu.directleader.UsersDialogFragment.UserType;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class TaskCreateActivity extends Activity implements OnClickListener, OnUserSelectListener, OnDocumentDownloadListener {
    private static final String TAG = "TaskCreateActivity";
    public static final String TITLE_KEY = "title_key";
    
    public static final int FILE_PICK_REQUEST_CODE = 0x000001;
    public static final int DIRECTUM_PICK_REQUEST_CODE = 0x000002;
    public static final int IMAGE_CAPTURE_REQUEST_CODE = 0x000003;
    
    private File mCurrentPhotoPath = null;
    
    class GetClientSettingsAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... params) {
            // Обновление данных Client Settings
            JSONObject result = mDirect.GetClientSettings();
            int statusCode = result.optInt("statusCode");
            result.remove("statusCode");
            if (statusCode == 200) {
                Log.v(TAG, "GetClientSettings() ok");
                // Сохранить в файл
                File path = mDirect.getApplicationCacheDir(mDirect);
                File settingsFile = new File(path, mDirect.mSettingsFilename);
                try {
                    if (!settingsFile.exists()) {
                        settingsFile.createNewFile();
                    }
                    FileOutputStream fOut = new FileOutputStream(settingsFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.write(result.toString());
                    myOutWriter.close();
                    fOut.close();
                } catch (IOException e) {
                    Log.v(TAG, "Exception in save client settings " + e.getLocalizedMessage());
                }
                return result;
            } else {
                Log.v(TAG, "Почему-то не удалось получить данные ClientSettings");
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            clientSettings = result;
        }
    }
    
    private class TaskCreateAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        ProgressDialog pg = new ProgressDialog(TaskCreateActivity.this);
        @Override
        protected void onPreExecute() {
            pg.setMessage(getResources().getString(R.string.task_create_processing_task_text));
            pg.setCancelable(false);
            pg.show();
            super.onPreExecute();
        }
        @Override
        protected JSONObject doInBackground(Void... task) {
            JSONObject taskJSON = new JSONObject();
            JSONArray observers = new JSONArray();
            JSONArray performers = new JSONArray();
            
            for (int i=1; i<observersLayout.getChildCount(); i++) {
                final Rabotnic user = (Rabotnic)observersLayout.getChildAt(i).getTag();
                observers.put(user.getCode());
            }
            for (int i=1; i<performersLayout.getChildCount(); i++) {
                final Rabotnic user = (Rabotnic)performersLayout.getChildAt(i).getTag();
                performers.put(user.getCode());
            }
            String grata = getResources().getString(R.string.create_task_dialog_fragment_importance_text);
            String nongrata = getResources().getString(R.string.create_task_dialog_fragment_noimportance_text);
            String importance = importanceView.isChecked() ? grata : nongrata;
            
            // Attachments
//            	{
//            	"Content":"Строковое содержимое",
//            	"DocId":2147483647,
//            	"Ext":"Строковое содержимое",
//            	"Name":"Строковое содержимое"
//            	}
            JSONArray attachmentsJSON = new JSONArray();
            for (Attachment att : mDataSet) {
            	final JSONObject attJSON = new JSONObject();
            	if (att.getId() == -1) {
            		// Файл с устройства
            		try {
            			File file = new File(att.getName());
            	        int length = (int) file.length();
            	        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file));
            	        byte[] bytes = new byte[length];
            	        reader.read(bytes, 0, length);
            	        reader.close();
            	        
            			String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
	            		attJSON.put("Content", base64);
	            		attJSON.put("DocId", null);
	            		attJSON.put("Ext", att.getExt());
	            		attJSON.put("Name", att.getCTitle());
	            		Log.v(TAG, "att.getCTitle() " + att.getCTitle());
	            		attachmentsJSON.put(attJSON);
            		} catch (JSONException ex) {
            			Log.v(TAG, "Не удалось прикрепить файл с устройства " + ex.getLocalizedMessage());
            		} catch (FileNotFoundException ex) {
            			Log.v(TAG, "Не удалось прикрепить файл с устройства " + ex.getLocalizedMessage());
            		} catch (IOException ex) {
             			Log.v(TAG, "Не удалось прикрепить файл с устройства " + ex.getLocalizedMessage());
             		}
            	} else {
            		// Файл из Директума
            		try {
	            		attJSON.put("Content", "");
	            		attJSON.put("DocId", att.getId());
	            		attJSON.put("Ext", "");
	            		attJSON.put("Name", "");
	            		attachmentsJSON.put(attJSON);
            		} catch (JSONException ex) {
            			Log.v(TAG, "Не удалось прикрепить документ из директума " + ex.getLocalizedMessage());
            		}
            	}
            }
            Log.v(TAG, "ATTACHMENTS " + attachmentsJSON);
            try {
            	taskJSON.put("Attachments", attachmentsJSON);
                taskJSON.put("FinalDate", (String)deadlineTextView.getTag());
                taskJSON.put("Observers", observers);
                taskJSON.put("Performers", performers);
                taskJSON.put("RightsCode", rightsTextView.getText().toString());
                taskJSON.put("RouteType", routeTypeTextView.getText().toString());
                taskJSON.put("Subject", titleEditText.getText().toString());
                taskJSON.put("Text", descriptionEditText.getText().toString());
                taskJSON.put("Importance", importance);
                return mDirect.PostCreateTask(taskJSON);
//                return null;
            } catch (JSONException e) {
                Log.v(TAG, "exception in create task asynctask" + e.getLocalizedMessage());
            }
            return null;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            Log.v(TAG, "onPostExecute " + result);
            if (pg != null) {
                pg.dismiss();
            }
            if (result == null) {
                final String errorText = getResources().getString(R.string.create_task_dialog_fragment_service_error_text);
                Toast.makeText(TaskCreateActivity.this, errorText, Toast.LENGTH_LONG).show();
            } else {
                final int statusCode = result.optInt("statusCode");
                if (statusCode == 200) {
                    final String errorText = getResources().getString(R.string.create_task_dialog_fragment_success_text);
                    Toast.makeText(TaskCreateActivity.this, errorText, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    final String errorText = getResources().getString(R.string.create_task_dialog_fragment_failed_text);
                    Toast.makeText(TaskCreateActivity.this, errorText, Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    class GetUsersAsyncTask extends AsyncTask<Void, Void, List<Rabotnic>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected List<Rabotnic> doInBackground(Void... params) {
            RabotnicDataSource rds = new RabotnicDataSource(TaskCreateActivity.this);
            rds.open();
            List<Rabotnic> rabs = rds.getAllRabotnics();
            rds.close();
            return rabs;
        }
        @Override
        protected void onPostExecute(List<Rabotnic> result) {
            super.onPostExecute(result);
            mRabotnics = result;
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    
    private String preTitle;
    
    private EditText titleEditText, descriptionEditText;
    private TextView performersLabelView, observersLabelView, rightsLabelView, routeTypeTextView, deadlineTextView, rightsTextView, performersTextView, observersTextView;
    private ImageButton attachmentButton, cancelButton, okButton, backButton, addButton;
    
    private CheckBox importanceView;
    
    private ViewGroup performersLayout, observersLayout;
    
    private JSONObject clientSettings;
    private UserType Usertype;
    private List<Rabotnic> mRabotnics = new ArrayList<Rabotnic>();
    
    private LinearLayout mTaskLayout, mAttachmentsLayout, mContentLayout;
    private RecyclerView mRecyclerView;
    private TaskAttachmentAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Attachment> mDataSet;
    
    private ArrayList<Attachment> mAttachments = new ArrayList<Attachment>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDirect = (DirectLeaderApplication) getApplication();
        mSettings = mDirect.getSettings();
        
        preTitle = getIntent().getExtras().getString(TITLE_KEY, "");
        
        setContentView(R.layout.task_create_activity);
        
        mTaskLayout = (LinearLayout)findViewById(R.id.taskLayout);
        mAttachmentsLayout = (LinearLayout)findViewById(R.id.attachmentLayout);
        mContentLayout = (LinearLayout)findViewById(R.id.contentLayout);
        
        mRecyclerView = (RecyclerView)findViewById(R.id.attachmentRecyclerView);
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
        mAdapter = new TaskAttachmentAdapter((DirectLeaderApplication)getApplication(), mDataSet);
        mRecyclerView.setAdapter(mAdapter);
        
        titleEditText = (EditText)findViewById(R.id.titleEditText);
        descriptionEditText = (EditText)findViewById(R.id.descriptionEditText);
        
        routeTypeTextView = (TextView)findViewById(R.id.routeTypeTextView);
        deadlineTextView = (TextView)findViewById(R.id.deadlineTextView);
        rightsTextView = (TextView)findViewById(R.id.rightsTextView);
        // Метки для списка исполнителей и наблюдателей
        performersTextView = (TextView)findViewById(R.id.performersTextView);
        observersTextView = (TextView)findViewById(R.id.observersTextView);
        
        performersLabelView = (TextView)findViewById(R.id.performersLabelView);
        observersLabelView = (TextView)findViewById(R.id.observersLabelView);
        rightsLabelView = (TextView)findViewById(R.id.rightsLabelView);
        
        attachmentButton = (ImageButton)findViewById(R.id.attachmentButton);
        cancelButton = (ImageButton)findViewById(R.id.cancelButton);
        okButton = (ImageButton)findViewById(R.id.okButton);
        
        backButton = (ImageButton)findViewById(R.id.backButton);
        addButton = (ImageButton)findViewById(R.id.addButton);
        
        performersLayout = (ViewGroup)findViewById(R.id.performersLayout);
        observersLayout = (ViewGroup)findViewById(R.id.observersLayout);
        
        importanceView = (CheckBox)findViewById(R.id.importanceView);
        
        setFonts();
        initData();
        this.setFinishOnTouchOutside(false);

        
        SwipeDismissRecyclerViewTouchListener touchListener = new SwipeDismissRecyclerViewTouchListener(mRecyclerView, new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
            @Override
            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
            	Log.v(TAG, "touchListener");
                for (int position : reverseSortedPositions) {
                	Attachment att = mDataSet.get(position);
                    mDataSet.remove(position);
                    Attachment to_remove = null;
                    for (Attachment attach : mAttachments) {
                    	if (attach.getId() == att.getId()) {
                    		to_remove = attach;
                    		break;
                    	}
                    }
                    mAttachments.remove(to_remove);
                    mAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public boolean canDismiss(int position) {
                return true;
            }
        });
        // */
        mRecyclerView.setOnTouchListener(touchListener);
        mRecyclerView.addOnItemTouchListener(  
        	    new RecyclerItemClickListener(this, new OnItemClickListener() {
        	        @Override 
        	        public void onItemClick(View view, int position) {
        	        	Attachment attachment = mDataSet.get(position);
        	        	if (attachment.getId() == -1) { // local file
        					File myFile = new File(attachment.getName());
        					try {
        		                FileOpen.openFile(TaskCreateActivity.this, myFile);
        		            } catch (IOException e) {
        		                Log.v(TAG, "Неудалось открыть документ " + e.getMessage());
        		            }
        				} else { // directum file
        					showDownloadDialog(attachment);
        				}
        	        }
        	        @Override
        	        public void onItemLongClick(View view, final int position) {
        	        	Log.v(TAG, "Rename the attachment");
        	        	final Attachment attachment = mDataSet.get(position);
        	        	if (attachment.getId() == -1) { // local file
        	        		AlertDialog.Builder builder = new AlertDialog.Builder(TaskCreateActivity.this);
        	                builder.setTitle("Введите новое имя файла:");

        	                // Set up the input
        	                LinearLayout ll = new LinearLayout(TaskCreateActivity.this);
        	                ll.setOrientation(LinearLayout.VERTICAL);
        	                final EditText inputName = new EditText(TaskCreateActivity.this);
        	                inputName.setText(attachment.getCTitle());
        	                ll.addView(inputName);
        	                builder.setView(ll);
        	                
        	                // Set up the buttons
        	                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
        	                    @Override
        	                    public void onClick(DialogInterface dialog, int which) {
        	                        String name = inputName.getText().toString();
        	                        attachment.setCTitle(name);
        	                        mAdapter.notifyItemChanged(position);
        	                    }
        	                });
        	                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	                    @Override
        	                    public void onClick(DialogInterface dialog, int which) {
        	                        dialog.cancel();
        	                    }
        	                });

        	                builder.show();
        	        	} else {
        	        		Toast.makeText(mDirect, "Нельзя переименовать файл из Directum", Toast.LENGTH_LONG).show();
        	        	}
        	        }
        	      })
        	  );
    }
    private SwipeDismissTouchListener.DismissCallbacks swipeDismissListener = new SwipeDismissTouchListener.DismissCallbacks() {
        @Override
        public void onDismiss(View view, Object token) {
        	Log.v(TAG, "swipeDismissListener");
            String path = ((TextView) view).getText().toString();
            if (mDataSet.contains(path)) {
                final int index = mDataSet.indexOf(path);
                mDataSet.remove(index);
                mAdapter.notifyDataSetChanged();
            }
        }
        @Override
        public boolean canDismiss(Object token) {
            return true;
        }
    };
    private void setFonts() {
        titleEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        descriptionEditText.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        routeTypeTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        deadlineTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        rightsTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        
        performersLabelView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        observersLabelView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        rightsLabelView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    private void initData() {
        titleEditText.setText(preTitle);
        
        cancelButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        addButton.setOnClickListener(this);
        
        routeTypeTextView.setOnClickListener(this);
        deadlineTextView.setOnClickListener(this);
        rightsTextView.setOnClickListener(this);
        
        performersLabelView.setOnClickListener(this);
        observersLabelView.setOnClickListener(this);
        
        attachmentButton.setOnClickListener(this);
        
        try {
            // Читаем из файла настройки
            File path = mDirect.getApplicationCacheDir(mDirect);
            File settingsFile = new File(path, mDirect.mSettingsFilename);
            FileInputStream fIn = new FileInputStream(settingsFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            clientSettings = new JSONObject(aBuffer);
            myReader.close();
            fIn.close();
        } catch (IOException e) {
            Log.v(TAG, "Неудалось прочитать настройки из файла");
        } catch (JSONException e) {
            Log.v(TAG, "Неудалось прочитать настройки из файла");
        }
        if (clientSettings == null) {
            new GetClientSettingsAsyncTask().execute();
        } else {
            try {
                JSONArray routeTypes = clientSettings.optJSONArray("Rights");
                final String type = routeTypes.getString(1);
                rightsTextView.setText(type);
            } catch (JSONException e) {
                Log.v(TAG, "Ошибка при парсинге Rights " + e.getMessage());
            }
        }
        new GetUsersAsyncTask().execute();
    }
    private void addUserToPerformers(Rabotnic user) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int padding = (int)(10 * metrics.density);
        
        final TextView userTextView = new TextView(this);
        userTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        userTextView.setText(user.getName());
        userTextView.setTag(user);
        userTextView.setPadding(padding, padding, padding, padding);
        userTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        // Create a generic swipe-to-dismiss touch listener.
        userTextView.setOnTouchListener(new SwipeDismissTouchListener(
                userTextView,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }
                    @Override
                    public void onDismiss(View view, Object token) {
                        // Добавить пользователя в список
                        mRabotnics.add((Rabotnic)userTextView.getTag());
                        performersLayout.removeView(userTextView);
                        if (performersLayout.getChildCount() == 1) {
                            // Осталась только метка, ее надо скрыть
                            performersTextView.setVisibility(View.GONE);
                        }
                    }
                }));
        performersLayout.addView(userTextView);
        performersTextView.setVisibility(View.VISIBLE);
        // Удаляем пользователя из списка
        mRabotnics.remove(user);
    }
    private void addUserToObservers(Rabotnic user) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int padding = (int)(10 * metrics.density);
        
        final TextView userTextView = new TextView(this);
        userTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        userTextView.setText(user.getName());
        userTextView.setTag(user);
        userTextView.setPadding(padding, padding, padding, padding);
        userTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        // Create a generic swipe-to-dismiss touch listener.
        userTextView.setOnTouchListener(new SwipeDismissTouchListener(
                userTextView,
                null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }
                    @Override
                    public void onDismiss(View view, Object token) {
                        // Добавить пользователя в список
                        mRabotnics.add((Rabotnic)userTextView.getTag());
                        observersLayout.removeView(userTextView);
                        if (observersLayout.getChildCount() == 1) {
                            // Осталась только метка, ее надо скрыть
                            observersTextView.setVisibility(View.GONE);
                        }
                    }
                }));
        observersLayout.addView(userTextView);
        observersTextView.setVisibility(View.VISIBLE);
        // Удаляем пользователя из списка
        mRabotnics.remove(user);
    }
    /**
     *  the callback received when the user "sets" the date in the dialog
     */
    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String myFormat = "dd MMMM yyyy"; //In which you need put here
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Utils.mLocale);
            final Calendar c = Calendar.getInstance();
            c.set(year, monthOfYear, dayOfMonth);
            deadlineTextView.setText(sdf.format(c.getTime()));
            deadlineTextView.setTag(String.format("%s-%s-%s", year, monthOfYear, dayOfMonth));
        }
    };
    @Override
    public void onClick(View v) {
        FlipAnimator animator;
        switch (v.getId()) {
            case R.id.cancelButton:
                finish();
                break;
            case R.id.okButton:
                new TaskCreateAsyncTask().execute();
                break;
            case R.id.deadlineTextView:
                // get the current date and time
                final Calendar c = Calendar.getInstance();
                new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
                break;
            case R.id.routeTypeTextView:
                showRouteTypePopup(v);
                break;
            case R.id.rightsTextView:
                showRightsPopup(v);
                break;
            case R.id.performersLabelView:
                showUsersDialog(UserType.PERFORMER);
                break;
            case R.id.observersLabelView:
                showUsersDialog(UserType.OBSERVER);
                break;
            case R.id.attachmentButton:
                mAttachmentsLayout.getLayoutParams().height = mTaskLayout.getMeasuredHeight();
                animator = new FlipAnimator(mTaskLayout, mAttachmentsLayout, mTaskLayout.getWidth() / 2, mTaskLayout.getHeight() / 2);
                if (mTaskLayout.getVisibility() == View.GONE) {
                    animator.reverse();
                }
                mContentLayout.startAnimation(animator);
                break;
            case R.id.backButton:
                animator = new FlipAnimator(mTaskLayout, mAttachmentsLayout, mTaskLayout.getWidth() / 2, mTaskLayout.getHeight() / 2);
                if (mTaskLayout.getVisibility() == View.GONE) {
                    animator.reverse();
                }
                mContentLayout.startAnimation(animator);
                break;
            case R.id.addButton:
                pickAttachment(findViewById(R.id.addButton));
                break;
        }
    }
    private void pickAttachment(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.add_attachment_popup_menu);
        popup.setOnMenuItemClickListener(actionsMenuItemClickListener);
        popup.show();
    }
    OnMenuItemClickListener actionsMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_attachment_get_photo:
            	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            	        startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
            	    	File photoFile = null;
            	        try {
            	            photoFile = createImageFile();
            	        } catch (IOException ex) {
            	        	Log.v(TAG, "" + ex.getLocalizedMessage());
            	        }
            	        // Continue only if the File was successfully created
            	        if (photoFile != null) {
            	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            	            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST_CODE);
            	        }
            	    }
                    break;
                case R.id.action_attachment_pick_device:
                    Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    photoPickerIntent.setType("*/*");
                    startActivityForResult(photoPickerIntent, FILE_PICK_REQUEST_CODE);
                    break;
                case R.id.action_attachment_search_directum:
                    Intent i = new Intent(TaskCreateActivity.this, SearchDirectumActivity.class);
                    Bundle b = new Bundle();
                    b.putParcelableArrayList(SearchDirectumActivity.CHECKED_ATTACHMENT_KEY, mAttachments);
                    i.putExtras(b);
                    startActivityForResult(i, DIRECTUM_PICK_REQUEST_CODE);
                    break;
            }
            return true;
        }
    };
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = mDirect.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image;
        return image;
    }
    private void showUsersDialog(UserType type) {
        Log.v(TAG, "showUsersDialog " + type.ordinal());
        Usertype = type;
        UsersDialogFragment newFragment = UsersDialogFragment.newInstance((ArrayList<Rabotnic>)mRabotnics, this);
        newFragment.show(getFragmentManager(), "users_dialog");
    }
    @Override
    public void onUserSelect(DialogFragment fragment, Rabotnic user) {
        switch (Usertype) {
            case PERFORMER:
                Log.v(TAG, "new Permormer " + user.getName());
                addUserToPerformers(user);
                break;
            case OBSERVER:
                Log.v(TAG, "new Observer " + user.getName());
                addUserToObservers(user);
                break;
        }
    }
    private void showRouteTypePopup(View v) {
     // Show popup menu
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        try {
            JSONArray routeTypes = clientSettings.optJSONArray("RouteType");
            for (int i=0; i<routeTypes.length(); i++) {
                final String type = routeTypes.getString(i);
                MenuItem item = menu.add(type);
            }
        } catch (JSONException e) {
            Log.v(TAG, "Ошибка при парсинге RouteType " + e.getMessage());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = (String)item.getTitle();
                routeTypeTextView.setText(title);
                return true;
            }
        });
        
        popup.show();
    }
    private void showRightsPopup(View v) {
        // Show popup menu
        PopupMenu popup = new PopupMenu(this, v);
        Menu menu = popup.getMenu();
        try {
            JSONArray routeTypes = clientSettings.optJSONArray("Rights");
            for (int i=0; i<routeTypes.length(); i++) {
                final String type = routeTypes.getString(i);
                menu.add(type);
            }
        } catch (JSONException e) {
            Log.v(TAG, "Ошибка при парсинге Rights " + e.getMessage());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = (String)item.getTitle();
                rightsTextView.setText(title);
                return true;
            }
        });
        popup.show();
    }
    
    private void showDownloadDialog(Attachment doc) {
        Log.v(TAG, "showDownloadDialog " + doc.getName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = DocumentDownloadDialogFragment.newInstance(doc);
        newFragment.show(ft, "download_dialog");
    }
	@Override
	public void onDocumentDownload(Attachment doc) {
		// открытие документа.
        boolean exist = mDirect.checkDocumentExist(doc);
        if (exist) {
            File myFile = mDirect.getDocumentFile(doc);
            try {
                FileOpen.openFile(this, myFile);
            } catch (IOException e) {
                Log.v(TAG, "Неудалось открыть документ " + e.getMessage());
            }
        }		
	}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "onActivityResult " + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK & data != null) {
                Uri selectedFile = data.getData();
                Log.v(TAG, "selectedFile " + selectedFile);
                if (selectedFile != null) {
                	String realPath = ImageFilePath.getPath(getApplicationContext(), selectedFile);
                	Log.v(TAG, "realPath " + realPath);
                	if (realPath == null) {
                		String errorAttachText = getResources().getString(R.string.create_task_dialog_fragment_attach_failed_text);
                		Toast.makeText(mDirect, errorAttachText, Toast.LENGTH_LONG).show();
                		return;
                	}
                    File fi = new File(realPath);
                    String name = fi.getAbsolutePath(); // Полный путь к файлу
                    String ctitle = fi.getName().substring(0, fi.getName().lastIndexOf("."));
                    String ext = name.substring(name.lastIndexOf(".")+1);
                    Log.v(TAG, "ext " + ext);
                    long id = -1;
                    final Attachment f = new Attachment("", ctitle, "", ext, id, "", name, false, 0, 0);
                    mDataSet.add(f);
                    mAdapter.notifyItemInserted(mDataSet.size());
                }
            }
        }
        if (requestCode == DIRECTUM_PICK_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK & data != null) {
                Log.v(TAG, "directum search ok");
                Bundle b = data.getExtras();
                mAttachments = b.getParcelableArrayList(SearchDirectumActivity.CHECKED_ATTACHMENT_KEY);
                // Удалить все аттачменты добавленные из директума, и добавить их заново
                Iterator itr = mDataSet.iterator();
                while(itr.hasNext()) {
                    Attachment att = (Attachment)itr.next();
                    if(att.getId() != -1) {
                        itr.remove();
                    }
                }
                mAdapter.notifyDataSetChanged();
                mDataSet.addAll(mAttachments);
                mAdapter.notifyDataSetChanged();
            }
        }
        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
        	if (resultCode == Activity.RESULT_OK) {
        		Log.v(TAG, "mCurrentPhotoPath " + mCurrentPhotoPath);
        		if (mCurrentPhotoPath != null) {
	        		String name = mCurrentPhotoPath.getAbsolutePath(); // Полный путь к файлу
	                String ctitle = mCurrentPhotoPath.getName().substring(0, mCurrentPhotoPath.getName().lastIndexOf("."));
	                String ext = name.substring(name.lastIndexOf(".")+1);
	                Log.v(TAG, "ext " + ext);
	                long id = -1;
	                final Attachment f = new Attachment("", ctitle, "", ext, id, "", name, false, 0, 0);
	                mDataSet.add(f);
	                mAdapter.notifyItemInserted(mDataSet.size());
	                mCurrentPhotoPath = null;
        		} else {
        			Toast.makeText(mDirect, "Неудалось прикрепить снимок. Обратитесь к разработчику.", Toast.LENGTH_LONG).show();
        		}
        	}
        }
    }
    @Override
	public void onSaveInstanceState(Bundle outState) {
		if (mCurrentPhotoPath != null) {
			outState.putSerializable("captureURI", mCurrentPhotoPath);
		}
		super.onSaveInstanceState(outState);
	}
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	if (savedInstanceState != null) {
			if (savedInstanceState.containsKey("captureURI")) {
				try {
					mCurrentPhotoPath = (File)savedInstanceState.getSerializable("captureURI");
				} catch (ClassCastException ex) {
					Log.v(TAG, "mCurrentPhotoPath ClassCastException " + ex.getMessage());
				}
			}
		}
    	super.onRestoreInstanceState(savedInstanceState);
    }
    class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {  
        private OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                	View childView = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(childView != null && mListener != null)
                    {
                        mListener.onItemLongClick(childView, mRecyclerView.getChildPosition(childView));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
    }
    interface OnItemClickListener {
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }
}