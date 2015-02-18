package ru.tasu.directleader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DocumentDownloadDialogFragment extends DialogFragment {
    private static final String TAG = "DocumentDownloadDialogFragment";
    public static final String ATTACHMENT_KEY = "attachment_key";
    
    private class DownloadDocumentAsyncTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadDocumentAsyncTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonYes.setVisibility(View.GONE);
            buttonNo.setVisibility(View.GONE);
            buttonCancel.setVisibility(View.VISIBLE);
            buttonRestart.setVisibility(View.GONE);
            failedTextView.setVisibility(View.GONE);
            // take CPU lock to prevent CPU from going off if the user 
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
            mProgressBar.setVisibility(View.VISIBLE);
            
            buttonCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancel(false);
                }
            });
            buttonRestart.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "download restart ");
                    // Удалить недокачанный файл
                    new File(fileOutputPath).delete();
                    buttonYes.performClick();
                }
            });
        }
        
        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            Base64InputStream binput = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                binput = new Base64InputStream(input, 0);
                output = new FileOutputStream(fileOutputPath);
//                boutput = new Base64OutputStream(output, Base64.NO_WRAP);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = binput.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (binput != null)
                        binput.close();
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMax(100);
            mProgressBar.setProgress(progress[0]);
        }
        @Override
        protected void onPostExecute(String result) {
            Log.v(TAG, "onPostExecute");
            mWakeLock.release();
            mProgressBar.setVisibility(View.GONE);
            if (result != null) {
                failedTextView.setText(getResources().getString(R.string.document_download_fragment_failed_text));
                failedTextView.setVisibility(View.VISIBLE);
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
                // Удалить недокачанный файл
                new File(fileOutputPath).delete();
                buttonCancel.setVisibility(View.GONE);
                buttonRestart.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(context, getResources().getString(R.string.document_download_fragment_success_text), Toast.LENGTH_SHORT).show();
                final Dialog pg = getDialog();
                if (pg != null) {
                    pg.dismiss();
                }
            }
        }
        @Override
        protected void onCancelled(String result) {
            Log.v(TAG, "onCancelled");
            // Удалить недокачанный файл
            new File(fileOutputPath).delete();
            super.onCancelled(result);
            mWakeLock.release();
            final Dialog pg = getDialog();
            if (pg != null) {
                pg.dismiss();
            }
        }
    }
    
    private SharedPreferences mSettings;
    private static DirectLeaderApplication mDirect;
    
    private Attachment mAttachment;
    private ProgressBar mProgressBar;
    
    private TextView titleTextView, documentNameTextView, failedTextView;
    private Button buttonYes, buttonNo, buttonCancel, buttonRestart;
    
    private String fileOutputPath = null;
    
    static DocumentDownloadDialogFragment newInstance(Attachment doc) {
        DocumentDownloadDialogFragment f = new DocumentDownloadDialogFragment();
        
        Bundle args = new Bundle();
        args.putParcelable(ATTACHMENT_KEY, doc);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDirect = (DirectLeaderApplication) getActivity().getApplication();
        mSettings = mDirect.getSettings();
        
        mAttachment = getArguments().getParcelable(ATTACHMENT_KEY);
        
        int style = DialogFragment.STYLE_NO_TITLE;
        int theme = android.R.style.Theme_Holo_Light_Dialog;
        setCancelable(false);
        setStyle(style, theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_document_dialog, container, false);
        
        titleTextView = (TextView)v.findViewById(R.id.titleTextView);
        documentNameTextView = (TextView)v.findViewById(R.id.documentNameTextView);
        failedTextView = (TextView)v.findViewById(R.id.failedTextView);
        
        documentNameTextView.setText(String.format("%s.%s", mAttachment.getName(), mAttachment.getExt()));
        
        buttonYes = (Button)v.findViewById(R.id.buttonYes);
        buttonYes.setOnClickListener(buttonClickListener);
        buttonNo = (Button)v.findViewById(R.id.buttonNo);
        buttonNo.setOnClickListener(buttonClickListener);
        buttonCancel = (Button)v.findViewById(R.id.buttonCancel);
        buttonRestart = (Button)v.findViewById(R.id.buttonRestart);
        
        mProgressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        
        setFonts();
        
        return v;
    }
    private void setFonts() {
        titleTextView.setTypeface(mDirect.mPFDinDisplayPro_Bold);
        documentNameTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        failedTextView.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        buttonYes.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        buttonNo.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        buttonCancel.setTypeface(mDirect.mPFDinDisplayPro_Reg);
        buttonRestart.setTypeface(mDirect.mPFDinDisplayPro_Reg);
    }
    OnClickListener buttonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.buttonYes:
                    Log.v(TAG, "Yes " + mDirect.getDocumentPath(mAttachment));
                    File fileFolder = new File(mDirect.getDocumentPath(mAttachment));
                    if (!fileFolder.exists()) {
                        fileFolder.mkdirs();
                    }
                    String filename = mDirect.normalizeFilename(mAttachment.getName());
                    File file = new File(fileFolder, String.format("%s.%s", filename, mAttachment.getExt()));
                    try {
                        file.createNewFile();
                        fileOutputPath = file.getAbsolutePath();
                        Log.v(TAG, "файл " + fileOutputPath);
                        String url = String.format(mDirect.GET_DOCUMENT, mAttachment.getId());
                        new DownloadDocumentAsyncTask(getActivity()).execute(url);
                    } catch (IOException e) {
                        Log.v(TAG, "Не удалось создать файл");
                        e.printStackTrace();
                    }
                    // */
                    break;
                case R.id.buttonNo:
                    Log.v(TAG, "No");
                    getDialog().dismiss();
                    break;
            }
        }
    };
}