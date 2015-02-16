package ru.tasu.directleader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
    private static final String DIRECTLEADER_SERVICE = "http://tyumbitasu-14.hosting.parking.ru/WSMock.svc";
    // methods
    private static final String POST_ADD_EXT_COMMENT = DIRECTLEADER_SERVICE + "/AddExtComments";
    private static final String GET_CHECK_AUTH = DIRECTLEADER_SERVICE + "/CheckAuth";
    private static final String POST_CHECK_FINISHED_TASKS = DIRECTLEADER_SERVICE + "/CheckFinishedTasks";
    private static final String POST_CREATE_TASK = DIRECTLEADER_SERVICE + "/CreateTask";
    private static final String GET_EXEC_JOB_ACTION = DIRECTLEADER_SERVICE + "/ExecJobAction?jobId=%s&resultText=%s&comment=%s"; //?jobId={JOBID}&resultText={RESULTTEXT}&comment={COMMENT}
    private static final String GET_EXEC_TASK_ACTION = DIRECTLEADER_SERVICE + "/ExecTaskAction?taskId=%s&actionName=%s"; //?taskId={TASKID}&actionName={ACTIONNAME}
    private static final String GET_EXPORT_DOCUMENT = DIRECTLEADER_SERVICE + "/ExportDocument?docId=%s"; //?docId={DOCID}
    private static final String GET_CLIENT_SETTINGS = DIRECTLEADER_SERVICE + "/GetClientSettings";
    private static final String GET_DOC_IMGS = DIRECTLEADER_SERVICE + "/GetDocImgs?docId=%s"; //?docId={DOCID}
    public static final String GET_DOCUMENT = DIRECTLEADER_SERVICE + "/GetDocument?docId=%s"; //?docId={DOCID}
    private static final String GET_HEADER_INFO = DIRECTLEADER_SERVICE + "/GetHeaderInfo";
    private static final String GET_MY_TASKS = DIRECTLEADER_SERVICE + "/GetMyTasks"; //?lastSyncDate={LASTSYNCDATE}&onlyInput={ONLYINPUT}&onlyMyJobs={ONLYMYJOBS}
    private static final String GET_RABOTNIC = DIRECTLEADER_SERVICE + "/GetRabotnic"; //?lastSyncDate={LASTSYNCDATE}&podr={PODR}
    private static final String GET_STRUCTURE = DIRECTLEADER_SERVICE + "/GetStructure";
    private static final String GET_TASK = DIRECTLEADER_SERVICE + "/GetTask?id=%s"; //?id={ID}
    private static final String POST_IMPORT_FROM_FILE = DIRECTLEADER_SERVICE + "/ImportFromFile";
    private static final String GET_PING = DIRECTLEADER_SERVICE + "/Ping";
    private static final String GET_SEARCH_DOCS = DIRECTLEADER_SERVICE + "/SearchDocs?criteria=%s"; //?criteria={CRITERIA}
    
    // Query's keys
    private static final String mQUERY_UdidKey = "UdidKey";
    private static final String mQUERY_UserName = "UserName";
    private static final String mQUERY_Password = "Password";
    private static final String mQUERY_Domain = "Domain";
    
    private static final String SETTINGS_USER_ID_KEY = "user_id_key";
    
    private static String mUserName = "";
    
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
    private void setUserCode(String Uid) {
        Editor e = getSettings().edit();
        e.putString(SETTINGS_USER_ID_KEY, Uid);
        e.commit();
    }
    private void clearUserCode() {
        Editor e = getSettings().edit();
        e.remove(SETTINGS_USER_ID_KEY);
        e.commit();
    }
    private String getDeviceId() {
        String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        return android_id;
    }
    public String getUserCode() {
        if (mUserName.isEmpty()) {
            mUserName = mSettings.getString(SETTINGS_USER_ID_KEY, "");
        }
        Log.v(TAG, "userName " + mUserName);
        return mUserName;
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
    public String normalizeFilename(String name) {
        return name.replace("/", "").replace("#", "");
    }
    /**
     * Проверить, есть ли документ текущей версии в файловой системе
     * @param doc
     * @return
     */
    public boolean checkDocumentExist(Attachment doc) {
        String dirPath = getDocumentPath(doc);
        // ИмяФайла состоит из ID документа. Потому что возможны длинные имена файлов со спец.символами
        String filename = String.format("%s.%s", normalizeFilename(doc.getName()), doc.getExt());
        File path = new File(dirPath, filename);
        return path.exists();
    }
    
    /**
     * Проверяет подключение к интернету
     * @return boolean
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    
    public JSONObject CheckAuth() {
        String url = GET_CHECK_AUTH;
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
                // Удалим кавычки в идентификаторе пользователя
                userId = userId.replace("\"", "");
                setUserCode(userId);
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
    public JSONObject GetMyTasks() {
        String url = GET_MY_TASKS;
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
        String url = GET_RABOTNIC;
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
    
    /**
     * Отправка данных методом GET
     * @param url Строка адреса
     * @return HttpResponse
     */
    private HttpResponse sendAuthorizeData(String url) {
        if (!isOnline()) {
            return null;
        }
        Log.v(TAG, url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpquery = new HttpGet(url);
        HttpResponse result = null;
        try {
            //sets a request header so the page receving the request
            //will know what to do with it
            httpquery.setHeader("Accept", "application/json");
            httpquery.setHeader("Content-type", "application/json");
            // Обязательные заголовки
            // При checkAuth заголовки не передавать, иначе 400 Bad Request
//            httpquery.setHeader(mQUERY_UdidKey, getDeviceId());
//            httpquery.setHeader(mQUERY_UserName, getUserName());
//            httpquery.setHeader(mQUERY_Password, "");
//            httpquery.setHeader(mQUERY_Domain, "");
            
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
        // TODO: Добавить проверку на наличие интернета
        Log.v(TAG, url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpquery = new HttpGet(url);
        HttpResponse result = null;
        try {
            //sets a request header so the page receving the request
            //will know what to do with it
            httpquery.setHeader("Accept", "application/json");
            httpquery.setHeader("Content-type", "application/json");
            // Обязательные заголовки
            // При checkAuth заголовки не передавать, иначе 400 Bad Request
            httpquery.setHeader(mQUERY_UdidKey, getDeviceId());
//            httpquery.setHeader(mQUERY_UserName, getUserName());
//            httpquery.setHeader(mQUERY_Password, "");
//            httpquery.setHeader(mQUERY_Domain, "");
            
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