package project.connection;

import android.com.i3center.rooholamini.mohsen.App;
import android.util.Log;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

public class WebServiceModule {

    private final String IO_EXCEPTION = "IO EXCEPTION";
    private final String UNKNOWN_EXCEPTION = "UNKNOWN EXCEPTION";
    private String data = null;
    private boolean timeOut;
    private boolean sentData;



    public String getData(){
        return data;
    }


    public interface Listener {
        void onSuccess(String data);

        void onFail(String error);
    }

    private Listener listener;
    private String[] params;
    private String stringUrl;
    private int connectionTimeout = 10000;
    private int readTimeout;

    public WebServiceModule url(String url) {
        this.stringUrl = url;
        return this;
    }

    public WebServiceModule params(String... params) {
        this.params = params;
        return this;
    }

    public WebServiceModule listener(Listener listener) {
        this.listener = listener;
        return this;
    }


    public WebServiceModule connectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout *1000;
        return this;
    }

    public WebServiceModule readTimeout(int readTimeout) {
        this.readTimeout = readTimeout * 1000;
        return this;
    }

    public void connect() {
        timeOut = false;
        sentData = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                readFromNet();
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(connectionTimeout);
                    if(!sentData && listener!=null) {
                        timeOut = true;
                        App.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("LOGGG","connection timeOut problem");
                                listener.onFail("connection timeOut problem");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void readFromNet() {
        try {
            URL url = new URL(stringUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            if(params !=null){
                String urlParams = convertToParamsFormat(params);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                byte[] buffer = urlParams.getBytes("UTF-8");
                outputStream.write(buffer,0,buffer.length);
                outputStream.flush();
                outputStream.close();
            }
            int code = connection.getResponseCode();
            InputStream inputStream = connection.getInputStream();
            data = inputStreamToString(inputStream);
            connection.disconnect();
            sentData = true;

            if (listener != null && !timeOut) {
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSuccess(data);
                        return;
                    }
                });

            }
        } catch (IOException e) {
            if (listener != null && !timeOut) {
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("LOGGG","IOException. url : \n"+stringUrl);

                        sentData = true;
                        listener.onFail(IO_EXCEPTION);
                    }
                });

            }
            e.printStackTrace();
        } catch (Exception e) {
            if (listener != null && !timeOut) {
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("LOGGG","UNKNOWN_EXCEPTION");
                        sentData = true;
                        listener.onFail(UNKNOWN_EXCEPTION);
                    }
                });
            }

        }
    }

    private String inputStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();

        String line = "";
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
        } catch (IOException e) {
            App.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null && !timeOut) {
                        Log.i("LOGGG", "IOException");
                        sentData = true;
                        listener.onFail(IO_EXCEPTION);
                    }
                }
            });
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                App.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (listener != null && !timeOut) {
                            Log.i("LOGGG", "IOException");
                            sentData = true;
                            listener.onFail(IO_EXCEPTION);
                        }
                    }
                });
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }


    private String convertToParamsFormat(String[] params) {
        String standardedParamsFormat = "";
        boolean isFirstParameter = true;
        for (int i = 0; i <= params.length - 2; i = i + 2) {
            if (!isFirstParameter) {
                standardedParamsFormat += "&";
            }
            standardedParamsFormat += params[i];
            standardedParamsFormat += "=";
            standardedParamsFormat += params[i + 1];
            isFirstParameter = false;
        }
        return standardedParamsFormat;

    }



}
