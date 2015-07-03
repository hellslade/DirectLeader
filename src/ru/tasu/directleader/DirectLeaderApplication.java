package ru.tasu.directleader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;

public class DirectLeaderApplication extends Application {
    private static final String TAG = "DirectLeaderApplication";
    
    private SharedPreferences mSettings;
    
    // Fonts
    public Typeface mPFDinDisplayPro_Bold;
    public Typeface mPFDinDisplayPro_Italic;
    public Typeface mPFDinDisplayPro_Med;
    public Typeface mPFDinDisplayPro_Reg;
    public Typeface mPFDinTextControlPro_Medium;
    public Typeface mPFDinTextControlPro_Regular;
    
    // URL's
    private static final String PORTAL_SERVICE = "http://tyumbitasu-16.hosting.parking.ru/PortalService.svc";
    private static final String DIRECTLEADER_SERVICE_DEMO = "http://tyumbitasu-14.hosting.parking.ru/WSMock.svc";
    // methods
    private static final String ADD_NEW_DEVICE = PORTAL_SERVICE + "/AddNewDevice?id=%s&organizationKey=%s&name=%s";
    private static final String REMOVE_DEVICE = PORTAL_SERVICE + "/RemoveDevice?id=%s";
    private static final String GET_SERVICE_URL = PORTAL_SERVICE + "/GetServiceUrl?organizationKey=%s";
    private static final String PING_PORTAL_SERVICE = PORTAL_SERVICE + "/Ping";
    
    private static final String GET_CHECK_AUTH = "/CheckAuth";
    private static final String POST_CREATE_TASK = "/CreateTask";
    private static final String GET_EXEC_JOB_ACTION = "/ExecJobAction?jobId=%s&resultText=%s&comment=%s"; //?jobId={JOBID}&resultText={RESULTTEXT}&comment={COMMENT}
    private static final String GET_EXEC_TASK_ACTION = "/ExecTaskAction?taskId=%s&actionName=%s"; //?taskId={TASKID}&actionName={ACTIONNAME}
    private static final String GET_CLIENT_SETTINGS = "/GetClientSettings";
    public  static final String GET_DOCUMENT = "/GetDocument?docId=%s"; //?docId={DOCID}
    private static final String GET_MY_TASKS = "/GetMyTasks"; //?lastSyncDate=%s&onlyInput={ONLYINPUT}&onlyMyJobs={ONLYMYJOBS}
    private static final String GET_RABOTNIC = "/GetRabotnic"; //?lastSyncDate=%s&podr={PODR}
    private static final String GET_PING = "/Ping";
    private static final String GET_SEARCH_DOCS = "/SearchDocs?criteria=%s"; //?criteria={CRITERIA}
    private static final String POST_SAVE_REFERENCE = "/SaveReference";
    private static final String POST_CHECK_FINISHED_TASKS = "/CheckFinishedTasks";
//    private static final String POST_ADD_EXT_COMMENT = "/AddExtComments";
//    private static final String GET_EXPORT_DOCUMENT = "/ExportDocument?docId=%s"; //?docId={DOCID}
//    private static final String GET_DOC_IMGS = "/GetDocImgs?docId=%s"; //?docId={DOCID}
//    private static final String GET_HEADER_INFO = "/GetHeaderInfo";
//    private static final String GET_STRUCTURE = "/GetStructure";
//    private static final String GET_TASK = "/GetTask?id=%s"; //?id={ID}
//    private static final String POST_IMPORT_FROM_FILE = "/ImportFromFile";
    
    public static final String mLastSyncDateFormat = "dd.MM.yyyy HH:mm";
    // Разные ключи, потому что были случаи, когда работники обновились, а задачи нет (по таймауту)
    private static final String SETTINGS_LASTSYNCDATE_RABOTNIC_KEY = "lastsyncdate_rabotnic_key";
    private static final String SETTINGS_LASTSYNCDATE_TASK_KEY = "lastsyncdate_task_key";
    // Query's keys
    public static final String mQUERY_DeviceId = "DeviceId";
    public static final String mQUERY_UserName = "UserName";
    public static final String mQUERY_Password = "Password";
    public static final String mQUERY_Domain = "Domain";
    
    private static final String SETTINGS_USER_ID_KEY = "user_id_key";
    private static final String SETTINGS_USERNAME_KEY = "username_key";
    private static final String SETTINGS_PASSWORD_KEY = "password_key";
    private static final String SETTINGS_DOMAIN_KEY = "domain_key";
    
    private static final String SETTINGS_ORGANIZATION_KEY = "organization_key";
    
    private static final String SETTINGS_ACTIVATE_KEY = "activate_key";
    public static final String SETTINGS_SERVICE_URL_KEY = "direct_leader_service_url_key";
    // Для сохранения url execJobAction & execTaskAction
    public static final String SETTINGS_EXEC_KEY = "exec_key";
    
    public static String mSettingsFilename = "client_settings.json";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        mSettings = getSharedPreferences("DirectLeader", MODE_PRIVATE);
        
        // Initialize fonts
        mPFDinDisplayPro_Bold = Typeface.createFromAsset(getAssets(), "fonts/PFDinDisplayPro-Bold.ttf");
        mPFDinDisplayPro_Italic = Typeface.createFromAsset(getAssets(), "fonts/PFDinDisplayPro-Italic.ttf");
        mPFDinDisplayPro_Med = Typeface.createFromAsset(getAssets(), "fonts/PFDinDisplayPro-Med.ttf");
        mPFDinDisplayPro_Reg = Typeface.createFromAsset(getAssets(), "fonts/PFDinDisplayPro-Reg.ttf");
//        mPFDinTextControlPro_Medium = Typeface.createFromAsset(getAssets(), "fonts/PFDinTextControlPro-Medium.ttf");
//        mPFDinTextControlPro_Regular = Typeface.createFromAsset(getAssets(), "fonts/PFDinTextControlPro-Regular.ttf");
        
        
    }
    public SharedPreferences getSettings() {
        return mSettings;
    }
    static public File getApplicationCacheDir(Context context) {
        File cache = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // SD карта в наличии, будем писать на нее
            cache = context.getExternalCacheDir();
        } else {
            // Карты нет, нужно по-другому :)
            cache = context.getCacheDir();
            if (cache == null) {
                // Почему-то путь до кэша всеравно null
                cache = new File("/data/data/ru.tasu.directleader/cache/");
            }
        }
        Log.v(TAG, "Cache dir " + cache);
        return cache;
    }
    
    private void saveUserCode(String Uid) {
        Editor e = getSettings().edit();
        e.putString(SETTINGS_USER_ID_KEY, Uid);
        e.commit();
    }
    private void saveUserName(String name, String password) {
        Editor e = getSettings().edit();
        e.putString(SETTINGS_USERNAME_KEY, name);
        e.putString(SETTINGS_PASSWORD_KEY, password);
        e.commit();
    }
    private void saveServiceUrl(String url) {
        Editor e = getSettings().edit();
        e.putString(SETTINGS_SERVICE_URL_KEY, url);
        e.commit();
    }
    private void saveActivateStatus(boolean flag) {
        Editor e = getSettings().edit();
        e.putBoolean(SETTINGS_ACTIVATE_KEY, flag);
        e.commit();
    }
    private void saveDomain(String domain) {
        Editor e = getSettings().edit();
        e.putString(SETTINGS_DOMAIN_KEY, domain);
        e.commit();
    }
    private void saveOrganization(String orgKey) {
        Editor e = getSettings().edit();
        e.putString(SETTINGS_ORGANIZATION_KEY, orgKey);
        e.commit();
    }
    private void clearUserData() {
        Editor e = getSettings().edit();
        e.remove(SETTINGS_USER_ID_KEY);
        e.remove(SETTINGS_USERNAME_KEY);
        e.remove(SETTINGS_PASSWORD_KEY);
//        e.remove(SETTINGS_ACTIVATE_KEY);
//        e.remove(SETTINGS_SERVICE_URL_KEY);
        e.remove(SETTINGS_ORGANIZATION_KEY);
        e.remove(SETTINGS_LASTSYNCDATE_RABOTNIC_KEY);
        e.remove(SETTINGS_LASTSYNCDATE_TASK_KEY);
        e.commit();
        
        // Clear DB data
        TaskDataSource tds = new TaskDataSource(this);
        RabotnicDataSource rds = new RabotnicDataSource(this);
        tds.open();
        rds.open();
        tds.deleteAllTasks();
        rds.deleteAllRabotnics();
        
        // Clear Settings file
        File path = getApplicationCacheDir(this);
        File settingsFile = new File(path, mSettingsFilename);
        settingsFile.delete();
        // */
    }
    public void clearServiceURL() {
        Editor e = getSettings().edit();
        e.remove(SETTINGS_ACTIVATE_KEY);
        e.remove(SETTINGS_SERVICE_URL_KEY);
        e.commit();
    }
    public void saveLastSyncDateRabotnic(String syncdate) {
    	Editor e = getSettings().edit();
    	e.putString(SETTINGS_LASTSYNCDATE_RABOTNIC_KEY, syncdate);
    	e.commit();
    }
    public void saveLastSyncDateTask(String syncdate) {
    	Editor e = getSettings().edit();
    	e.putString(SETTINGS_LASTSYNCDATE_TASK_KEY, syncdate);
    	e.commit();
    }
    
    public String getLastSyncDateRabotnic() {
    	return getSettings().getString(SETTINGS_LASTSYNCDATE_RABOTNIC_KEY, "");
    }
    public String getLastSyncDateTask() {
    	return getSettings().getString(SETTINGS_LASTSYNCDATE_TASK_KEY, "");
    }
    public String getDeviceId() {
        String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        Log.v(TAG, "device id " + android_id);
        return android_id;
    }
    public String getUserCode() {
        return mSettings.getString(SETTINGS_USER_ID_KEY, "");
    }
    public String getUserName() {
        return mSettings.getString(SETTINGS_USERNAME_KEY, "");
//        try
//        {
//            String s = URLEncoder.encode(mUserName, "UTF-8");
//            Log.v(TAG, "s " + s);
//            return s;
//        }
//        catch (UnsupportedEncodingException e)
//        {
//            Log.e("utf8", "conversion ", e);
//        }
//        return "";
    }
    public String getPassword() {
        return mSettings.getString(SETTINGS_PASSWORD_KEY, "");
    }
    public String getDomain() {
        return mSettings.getString(SETTINGS_DOMAIN_KEY, "");
    }
    public String getOrganization() {
        return mSettings.getString(SETTINGS_ORGANIZATION_KEY, "");
    }
    
    private int getTimeout4URL(String url) {
    	String interval = "30"; // По умолчанию 30 секунд
    	List<String> timeout_task = new ArrayList<String>();
    	timeout_task.add(GET_MY_TASKS);
    	timeout_task.add(POST_CHECK_FINISHED_TASKS);
    	timeout_task.add(GET_EXEC_JOB_ACTION);
    	timeout_task.add(GET_EXEC_TASK_ACTION);
    	timeout_task.add(POST_CREATE_TASK);
    	timeout_task.add(POST_SAVE_REFERENCE);
    	
    	List<String> timeout_rabotnic = new ArrayList<String>();
    	timeout_rabotnic.add(GET_RABOTNIC);
    	timeout_rabotnic.add(GET_CLIENT_SETTINGS);
    	
    	List<String> timeout_attachments = new ArrayList<String>();
    	timeout_attachments.add(GET_DOCUMENT);
    	
//    	Log.v(TAG, "getTimeout4URL url " + url);
    	
    	for (String u : timeout_task) {
    		if (url.contains(u)) {
    			interval = getSettings().getString("timeout_task", "30");
    		}
    	}
    	for (String u : timeout_rabotnic) {
    		if (url.contains(u)) {
    			interval = getSettings().getString("timeout_rabotnic", "30");
    		}
    	}
    	for (String u : timeout_attachments) {
    		if (url.contains(u)) {
    			interval = getSettings().getString("timeout_attachments", "30");
    		}
    	}
    	
//        Log.v(TAG, "getTimeout4URL interval " + interval);
        int timeoutSocket = Integer.valueOf(interval)*1000; // В настройках интервал в секундах
    	return timeoutSocket;
    }
    public String getDirectLeaderServiceURL() {
        if (!isDeviceActivated()) {
            return DIRECTLEADER_SERVICE_DEMO;
        } else {
            return mSettings.getString(SETTINGS_SERVICE_URL_KEY, DIRECTLEADER_SERVICE_DEMO);
        }
    }
    
    /**
     * Вернет путь к каталогу переданного атачмента
     * 
     * Хранение документа осуществляется по пути
     * ApplicationCacheDir/task_id/doc_id/version/
     */
    public String getDocumentPath(Attachment doc) {
        return String.format("%s/%s/%s/%s", getApplicationCacheDir(this), doc.getTaskId(), doc.getId(), doc.getVersion());
    }
    /**
     * Нормализует имя файла, убирает недопустимые символы
     * @param name
     * @return
     */
    public String normalizeFilename(String name) {
        return name.replace("/", "").replace("#", "");
    }
    /**
     * Вернет объект File переданого документа
     * @param doc
     * @return File object
     */
    public File getDocumentFile(Attachment doc) {
        String dirPath = getDocumentPath(doc);
        String filename = String.format("%s.%s", normalizeFilename(doc.getName()), doc.getExt());
        return new File(dirPath, filename);
    }
    /**
     * Проверить, есть ли документ текущей версии в файловой системе
     * @param doc
     * @return
     */
    public boolean checkDocumentExist(Attachment doc) {
        return getDocumentFile(doc).exists();
    }
    
    public boolean isServiceAvailable() {
        return isServiceAvailable(String.format(getDirectLeaderServiceURL() + GET_PING));
    }
    /**
     * Проверяет доступность сервиса, включая проверку доступа к интернету
     * @return boolean
     */
    public boolean isServiceAvailable(String pingUrl) {
        if (isOnline()) {
            // Интернет есть, теперь пингуем сервер
            HttpResponse response = sendDataGet(pingUrl);
            if (response == null) {
                return false;
            }
            String result = ReadResponse(response);
            if (result.equalsIgnoreCase("true")) {
                return true;
            }
            
        }
        return false;
    }
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    public void Logout() {
        clearUserData();
    }
    
    public JSONObject CheckAuth(String username, String password) {
        saveUserName(username, password);
        String url = getDirectLeaderServiceURL() + GET_CHECK_AUTH;
        HttpResponse response = sendAuthorizeData(url);
        if (response == null) {
            return new JSONObject();
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        String userId = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                userId = ReadResponse(response);
                Log.v(TAG, "response " + userId);
                // Удалим кавычки в идентификаторе пользователя
                userId = userId.replace("\"", "");
                saveUserCode(userId);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject PostCheckFinishedTasks(JSONArray taskIds) {
        String url = getDirectLeaderServiceURL() + POST_CHECK_FINISHED_TASKS;
        HttpResponse response = sendDataPostJSONArray(url, taskIds);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                array = ReadResponseJSONArray(response);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject GetMyTasks() {
    	String GETParams = "";
        if (!getLastSyncDateTask().equalsIgnoreCase("")) {
        	try {
        		GETParams = String.format("?lastSyncDate=%s", URLEncoder.encode(getLastSyncDateTask(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        String url = getDirectLeaderServiceURL() + GET_MY_TASKS + GETParams;
        
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                array = ReadResponseJSONArray(response);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject getRabotnics() {
    	String GETParams = "";
        if (!getLastSyncDateRabotnic().equalsIgnoreCase("")) {
        	try {
				GETParams = String.format("?lastSyncDate=%s", URLEncoder.encode(getLastSyncDateRabotnic(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
        }
        String url = getDirectLeaderServiceURL() + GET_RABOTNIC + GETParams;
        
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        JSONArray array = new JSONArray();
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                array = ReadResponseJSONArray(response);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject GetClientSettings() {
        String url = getDirectLeaderServiceURL() + GET_CLIENT_SETTINGS;
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                data = ReadResponseJSONObject(response);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject PostCreateTask(JSONObject taskJSON) {
        if (!isServiceAvailable()) {
            return null;
        }
        String url = getDirectLeaderServiceURL() + POST_CREATE_TASK;
        HttpResponse response = sendDataPostJSONObject(url, taskJSON);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        String task_id = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                task_id = ReadResponse(response);
                task_id = task_id.replace("\"", "");
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("task_id", task_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject PostSaveReference(JSONObject json) {
        if (!isServiceAvailable()) {
            return null;
        }
        String url = getDirectLeaderServiceURL() + POST_SAVE_REFERENCE;
        HttpResponse response = sendDataPostJSONObject(url, json);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        data = ReadResponseJSONObject(response);
        
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject ExecJobAction(long jobId, String actionName, String comment) {
        try {
            comment = URLEncoder.encode(comment, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            Log.v(TAG, "Не удалось декодировать в utf-8 " + e1.getLocalizedMessage());
            comment = "";
        }
        String url = String.format(getDirectLeaderServiceURL() + GET_EXEC_JOB_ACTION, jobId, actionName, comment);
        JSONObject data = new JSONObject();
        if (!isServiceAvailable()) {
            // Записать в настройки URL
            Editor e = getSettings().edit();
            Set<String> execUrls = getSettings().getStringSet(SETTINGS_EXEC_KEY, new HashSet<String>());
            execUrls.add(url);
            e.putStringSet(SETTINGS_EXEC_KEY, execUrls);
            e.commit();
            return data;
        }
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        String result = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                result = ReadResponse(response);
                result = result.replace("\"", "");
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject ExecTaskAction(long taskId, String actionName) {
        String url = String.format(getDirectLeaderServiceURL() + GET_EXEC_TASK_ACTION, taskId, actionName);
        JSONObject data = new JSONObject();
        if (!isServiceAvailable()) {
            // Записать в настройки URL
            Editor e = getSettings().edit();
            Set<String> execUrls = getSettings().getStringSet(SETTINGS_EXEC_KEY, new HashSet<String>());
            execUrls.add(url);
            e.putStringSet(SETTINGS_EXEC_KEY, execUrls);
            e.commit();
            return data;
        }
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        String result = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                result = ReadResponse(response);
                result = result.replace("\"", "");
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    /**
     * Выполнить сохраненный url
     * @param url
     * @return
     */
    public JSONObject ExecAction(String url) {
        JSONObject data = new JSONObject();
        if (!isServiceAvailable()) {
            // Записать в настройки URL
            Editor e = getSettings().edit();
            Set<String> execUrls = getSettings().getStringSet(SETTINGS_EXEC_KEY, new HashSet<String>());
            execUrls.add(url);
            e.putStringSet(SETTINGS_EXEC_KEY, execUrls);
            e.commit();
            return data;
        }
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        String result = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                result = ReadResponse(response);
                result = result.replace("\"", "");
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("result", result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public boolean isDeviceActivated() {
        return mSettings.getBoolean(SETTINGS_ACTIVATE_KEY, false);
    }
    
    public JSONObject SearchDirectum(String criteria) {
        String url = String.format(getDirectLeaderServiceURL() + GET_SEARCH_DOCS, criteria);
        JSONObject data = new JSONObject();
        if (!isServiceAvailable()) {
            return null;
        }
        HttpResponse response = sendDataGet(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONArray array = new JSONArray();
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                array = ReadResponseJSONArray(response);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("data", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    public JSONObject GetURLForService() {
        if (!isServiceAvailable(PING_PORTAL_SERVICE)){
            return null;
        }
        String url = String.format(GET_SERVICE_URL, getOrganization());
        HttpResponse response = sendDataGetPortal(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        String serviceUrl = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                serviceUrl = ReadResponse(response);
                Log.v(TAG, "url " + serviceUrl);
                // Удалим кавычки
                serviceUrl = serviceUrl.replace("\"", "");
                saveServiceUrl(serviceUrl);
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("url", serviceUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject AddNewDevice(String orgKey, String deviceName) {
        if (!isServiceAvailable(PING_PORTAL_SERVICE)){
            return null;
        }
        String url = String.format(ADD_NEW_DEVICE, getDeviceId(), orgKey, deviceName);
        HttpResponse response = sendDataGetPortal(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        String status = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                status = ReadResponse(response);
                Log.v(TAG, "status " + status);
                // Удалим кавычки
                status = status.replace("\"", "");
                if (status.equalsIgnoreCase("true")) {
                    saveActivateStatus(true);
                    saveOrganization(orgKey);
                } else {
                    saveActivateStatus(false);
                }
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    public JSONObject RemoveDevice() {
        if (!isServiceAvailable(PING_PORTAL_SERVICE)){
            return null;
        }
        String url = String.format(REMOVE_DEVICE, getDeviceId());
        HttpResponse response = sendDataGetPortal(url);
        if (response == null) {
            return null;
        }
        // Обработка ответа
        JSONObject data = new JSONObject();
        String status = "";
        Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
        switch (response.getStatusLine().getStatusCode()) {
            case 200: // Успешно
                Log.v(TAG, "200");
                status = ReadResponse(response);
                Log.v(TAG, "status " + status);
                // Удалим кавычки
                status = status.replace("\"", "");
                if (status.equalsIgnoreCase("true")) {
                    saveActivateStatus(false);
                    clearServiceURL();
                }
                break;
            case 400: // BAD REQUEST
                Log.v(TAG, "400");
                data = ReadResponseJSONObject(response);
                break;
            default:
                Log.v(TAG, "default");
                Log.v(TAG, "response.getStatusLine().getStatusCode() " + response.getStatusLine().getStatusCode());
                data = ReadResponseJSONObject(response);
        }
        try {
            data.put("statusCode", response.getStatusLine().getStatusCode());
            data.put("status", status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;
    }
    /**
     * Отправка данных методом GET
     * @param url Строка адреса
     * @return HttpResponse
     */
    private HttpResponse sendAuthorizeData(String url) {
        if (!isServiceAvailable()) {
            return null;
        }
        Log.v(TAG, url);
        HttpClient httpclient = new DefaultHttpClient(getHttpParams(url));
        HttpGet httpquery = new HttpGet(url);
        HttpResponse result = null;
        try {
            //sets a request header so the page receving the request
            //will know what to do with it
            httpquery.setHeader("Accept", "application/json");
            httpquery.setHeader("Content-type", "application/json");
            // Обязательные заголовки
            // При checkAuth заголовки не передавать, иначе 400 Bad Request
            Log.v(TAG, mQUERY_DeviceId + " " + getDeviceId());
            Log.v(TAG, mQUERY_UserName + " " + getUserName());
            Log.v(TAG, mQUERY_Password + " " + getPassword());
            Log.v(TAG, mQUERY_Domain + " " + getDomain());
            httpquery.setHeader(mQUERY_DeviceId, getDeviceId());
            httpquery.setHeader(mQUERY_UserName, getUserName());
            httpquery.setHeader(mQUERY_Password, getPassword());
            httpquery.setHeader(mQUERY_Domain, getDomain());
            
//            Header[] hs = httpquery.getAllHeaders();
//            for (Header h : hs) {
//                Log.v(TAG, "h " + h.getName() + " " + h.getValue());
//            }
            
            result = httpclient.execute(httpquery);
        } catch (ClientProtocolException e) {
            Log.v(TAG, "ClientProtocolException " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.v(TAG, "IOException " + e.getLocalizedMessage());
        }
        httpclient = null;
        httpquery = null;
        return result;
    }
    /**
     * Отправка данных методом GET
     * @param url Строка адреса
     * @return HttpResponse
     */
    private HttpResponse sendDataGet(String url) {
        Log.v(TAG, url);
        HttpClient httpclient = new DefaultHttpClient(getHttpParams(url));
        HttpGet httpquery = new HttpGet(url);
        HttpResponse result = null;
        try {
            //sets a request header so the page receving the request
            //will know what to do with it
            httpquery.setHeader("Accept", "application/json");
            httpquery.setHeader("Content-type", "application/json");
            // Обязательные заголовки
            // При checkAuth заголовки не передавать, иначе 400 Bad Request
            Log.v(TAG, mQUERY_DeviceId + " " + getDeviceId());
            Log.v(TAG, mQUERY_UserName + " " + getUserName());
            Log.v(TAG, mQUERY_Password + " " + getPassword());
            Log.v(TAG, mQUERY_Domain + " " + getDomain());
            httpquery.setHeader(mQUERY_DeviceId, getDeviceId());
            httpquery.setHeader(mQUERY_UserName, getUserName());
            httpquery.setHeader(mQUERY_Password, getPassword());
            httpquery.setHeader(mQUERY_Domain, getDomain());
            
            result = httpclient.execute(httpquery);
        } catch (ClientProtocolException e) {
            Log.v(TAG, "ClientProtocolException " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.v(TAG, "IOException " + e.getLocalizedMessage());
        }
        httpclient = null;
        httpquery = null;
        return result;
    }
    /**
     * Отправка данных на Portal Service
     * @param url
     * @return
     */
    private HttpResponse sendDataGetPortal(String url) {
        Log.v(TAG, url);
        HttpClient httpclient = new DefaultHttpClient(getHttpParams(url));
        HttpGet httpquery = new HttpGet(url);
        HttpResponse result = null;
        try {
            //sets a request header so the page receving the request
            //will know what to do with it
            httpquery.setHeader("Accept", "application/json");
            httpquery.setHeader("Content-type", "application/json");
            // Обязательные заголовки
            // При checkAuth заголовки не передавать, иначе 400 Bad Request
//            httpquery.setHeader(mQUERY_DeviceId, getDeviceId());
//            httpquery.setHeader(mQUERY_UserName, getUserName());
//            httpquery.setHeader(mQUERY_Password, getPassword());
//            httpquery.setHeader(mQUERY_Domain, getDomain());
            
            result = httpclient.execute(httpquery);
        } catch (ClientProtocolException e) {
            Log.v(TAG, "ClientProtocolException " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.v(TAG, "IOException " + e.getLocalizedMessage());
        }
        httpclient = null;
        httpquery = null;
        return result;
    }
    /**
     * Отправка данных методом POST
     * @param url Строка адреса
     * @param json JSONObject данные
     * @return HttpResponse
     */
    private HttpResponse sendDataPostJSONObject(String url, JSONObject json) {
        return sendDataPostJSON(url, json.toString());
    }
    /**
     * Отправка данных методом POST
     * @param url Строка адреса
     * @param json JSONArray данные
     * @return HttpResponse
     */
    private HttpResponse sendDataPostJSONArray(String url, JSONArray json) {
        return sendDataPostJSON(url, json.toString());
    }
    /**
     * Отправка данных методом POST
     * @param url Строка адреса
     * @param json String данные
     * @return HttpResponse
     */
    private HttpResponse sendDataPostJSON(String url, String jsonString) {
        Log.v(TAG, url);
        HttpClient httpclient = new DefaultHttpClient(getHttpParams(url));
        HttpPost httpquery = new HttpPost(url);
        HttpResponse result = null;
        try {
            //passes the results to a string builder/entity
            Log.v(TAG, "json " + jsonString);
            StringEntity se = new StringEntity(jsonString, HTTP.UTF_8);
            //sets the post request as the resulting string
            httpquery.setEntity(se);
         // Обязательные заголовки
            httpquery.setHeader(mQUERY_DeviceId, getDeviceId());
            httpquery.setHeader(mQUERY_UserName, getUserName());
            httpquery.setHeader(mQUERY_Password, getPassword());
            httpquery.setHeader(mQUERY_Domain, getDomain());
            //sets a request header so the page receving the request
            //will know what to do with it
            httpquery.setHeader("Accept", "application/json");
            httpquery.setHeader("Content-type", "application/json");
            result = httpclient.execute(httpquery);
//            status = response.getStatusLine().getStatusCode();
        } catch (ClientProtocolException e) {
            Log.v(TAG, "ClientProtocolException " + e.getLocalizedMessage());
        } catch (IOException e) {
            Log.v(TAG, "IOException " + e.getLocalizedMessage());
        }
        httpclient = null;
        httpquery = null;
        return result;
    }
    
    private HttpParams getHttpParams(String url) {
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used. 
        int timeoutConnection = 10000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
//        int timeoutSocket = 40000;
        int timeoutSocket = getTimeout4URL(url);
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        return httpParameters;
    }
    /**
     * Чтение HttpResponse в JSON Array
     * @param response HttpResponse
     * @return JSON object
     */
    private JSONArray ReadResponseJSONArray(HttpResponse response) {
        JSONArray result = new JSONArray();
        try {
            String response_str = ReadResponse(response);
            Log.v(TAG, response_str);
            result = new JSONArray(response_str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * Чтение HttpResponse в JSON Object
     * @param response HttpResponse
     * @return JSON object
     */
    private JSONObject ReadResponseJSONObject(HttpResponse response) {
        JSONObject result = new JSONObject();
        try {
            String response_str = ReadResponse(response);
            Log.v(TAG, response_str);
            result = new JSONObject(response_str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * Чтение HttpResponse в JSON строку
     * @param response HttpResponse
     * @return JSON string
     */
    private String ReadResponse(HttpResponse response)
    {
        HttpEntity entity = response.getEntity();
        BufferedReader reader = null;
        String responseString = "";
        try {
            if (entity != null) 
            {
                InputStream is = entity.getContent();
                if (is != null) 
                {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    try {
                        reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        while ((line = reader.readLine()) != null) 
                        {
                            sb.append(line);
                        }
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                    }  finally  {
                        if (is != null) 
                        {
                            is.close();
                        }
                        if (reader != null) 
                        {
                            reader.close();
                        }
                    }
                    responseString = sb.toString();
                }
                entity.consumeContent();
            }
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return responseString;
    }
}