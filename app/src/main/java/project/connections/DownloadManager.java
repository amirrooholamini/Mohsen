package project.connections;


import android.com.i3center.rooholamini.mohsen.App;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DownloadManager {
    private HttpURLConnection connection = null;
    private static ArrayList<String> urls = new ArrayList<String>();

    private final String MALFORMATED_URL_EXCEPTION = "MALFORMATED URL EXCEPTION";
    private final String IO_EXCEPTION = "IO EXCEPTION";

    private static final String DOWNLOADING_FILE_LABLE = "downloading_";
    private String destinationFileFolder;
    private String destinationFileName;
    private OnDownloadProgressListener listener;
    private String downloadPath;
    private String destinationPath;
    private int bufferSize = 500;
    private long downloadedSize = 0;
    private long totalSize = 0;
    private int percent = 0;
    private boolean isDownloadCompleted = false;
    private boolean resumeDownloadAbility = true;
    private boolean isStop = false;
    private boolean isPause = false;
    private OnStreamReceivedListener streamListener;

    public  interface OnDownloadProgressListener {
        void onProgress(float totalSize , float downloadedSize);
        void onComplete();
        void onFail(String error);
    }

    private void downloadFile(){


        BufferedOutputStream bufferedOutputStream = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(downloadPath);
            connection = (HttpURLConnection) url.openConnection();
            totalSize = connection.getContentLength();
            connection.disconnect();
            connection = (HttpURLConnection) url.openConnection();
            File file = new File(destinationPath);
            if (file.exists()) {
                if (resumeDownloadAbility) {
                    downloadedSize = (int) file.length();
                    connection.setRequestProperty("Range", "bytes=" + (file.length()) + "-");
                } else {
                    file.delete();
                }
            } else {
                connection.setRequestProperty("Range", "bytes=" + downloadedSize + "-");
            }
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            inputStream = new BufferedInputStream(connection.getInputStream());
            FileOutputStream fileOutputStream;


            if (downloadedSize == 0) {
                fileOutputStream = new FileOutputStream(destinationPath);
            } else {
                fileOutputStream = new FileOutputStream(destinationPath, true);
            }

            bufferedOutputStream = new BufferedOutputStream(fileOutputStream, bufferSize);

            byte[] buffer = new byte[bufferSize];
            int len = 0;

            while ((len = inputStream.read(buffer)) > 0) {
                if (isPause || isStop) {
                    break;
                }
                bufferedOutputStream.write(buffer, 0, len);
                downloadedSize += len;
                percent = (int) (100 * downloadedSize / totalSize);
                if (listener != null) {
                    listener.onProgress(totalSize,downloadedSize);

                }
            }
            connection.disconnect();
            if (isStop) {
                file.delete();
            }
            bufferedOutputStream.flush();
            if (listener != null && totalSize == file.length()/* && !isStop && !isPause*/) {
                File finalFile = new File(destinationFileFolder + "/" + destinationFileName);
                file.renameTo(finalFile);
                listener.onComplete();

            }
        } catch (MalformedURLException e) {
            App.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    connection.disconnect();
                    listener.onFail(MALFORMATED_URL_EXCEPTION);
                }
            });

            e.printStackTrace();
        } catch (IOException e) {
            App.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    connection.disconnect();
                    listener.onFail(IO_EXCEPTION);
                }
            });

            e.printStackTrace();
        } catch (Exception e){

        } finally{
            if (bufferedOutputStream != null) {
                try {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    App.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            connection.disconnect();
                            listener.onFail(IO_EXCEPTION);
                        }
                    });

                    e.printStackTrace();
                }

            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    App.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            connection.disconnect();
                            listener.onFail(IO_EXCEPTION);
                        }
                    });

                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public DownloadManager(String downloadPath, String destinationPath) {
        this.downloadPath = downloadPath;
        this.destinationPath = destinationPath;
    }

    public DownloadManager(String downloadPath) {
        this.downloadPath = downloadPath;
        this.destinationPath = null;
    }


    public interface OnStreamReceivedListener{
        void onStreamReceived(InputStream stream);
    }


    public DownloadManager progressListener(OnDownloadProgressListener listener) {
        this.listener = listener;
        return this;
    }

    public DownloadManager streamListener(OnStreamReceivedListener listener) {
        this.streamListener = listener;
        return this;
    }

    public DownloadManager bufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public DownloadManager resumeAbility(boolean resumeDownloadAbility) {
        this.resumeDownloadAbility = resumeDownloadAbility;
        return this;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getDownloadedSize() {
        return downloadedSize;
    }

    public int getPercent() {
        return percent;
    }

    public DownloadManager stop() {
        this.isStop = true;
        return this;
    }

    public DownloadManager pause() {
        this.isPause = true;
        return this;
    }

    public DownloadManager resume() {
        this.isPause = false;
        this.isStop = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFile();
            }
        }).start();
        return this;
    }

    public void getStream() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(downloadPath);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.connect();
                    InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                    if(streamListener!=null){
                        streamListener.onStreamReceived(inputStream);
                    }
                } catch (MalformedURLException e) {
                    App.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            connection.disconnect();
                            listener.onFail(MALFORMATED_URL_EXCEPTION);
                        }
                    });

                    e.printStackTrace();
                } catch (IOException e) {
                    App.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            connection.disconnect();
                            listener.onFail(IO_EXCEPTION);
                        }
                    });

                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void download() {
        if (destinationPath == null) {
            return;
        }
        isPause = false;
        isStop = false;
        destinationFileFolder = destinationPath.substring(0, destinationPath.lastIndexOf("/"));
        destinationFileName = destinationPath.substring(destinationPath.lastIndexOf("/") + 1);
        if (new File(destinationPath).exists()) {
            if (listener != null) {
                listener.onComplete();
            }
            return;
        } else {
            urls.remove(downloadPath);
        }
        if (!urls.contains(downloadPath)) {
            destinationPath = destinationFileFolder + "/" + DOWNLOADING_FILE_LABLE + destinationFileName;
            urls.add(downloadPath);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downloadFile();
                }
            }).start();
        }else{
            Log.i("LOG","exist");
        }
    }



}
