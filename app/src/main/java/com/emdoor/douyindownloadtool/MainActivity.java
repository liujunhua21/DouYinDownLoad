package com.emdoor.douyindownloadtool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private EditText HttpsAdress;
    private Button search_button, next_button;
    private String responseData;
    private String dataAdress;
    private Boolean hasMore;
    private Long maxCursor;
    private VideoView videoView;
    private ProgressBar progressBar;
    private ArrayList<String> data = new ArrayList<String>();
    private ListView listView = null;
    private ArrayAdapter<String> adapter = null;
    private static final int MSG_REFRESH_UI = 130;
    private static final int MSG_REFRESH_ENABLED = 131;
    private static final int MSG_REFRESH_DISENABLED = 132;
    private String dirName = "/DownloadAdress/";
    //private File file = new File(dirName);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpsAdress = findViewById(R.id.HttpsAdress);
        search_button = findViewById(R.id.search_button);
        next_button = findViewById(R.id.next_button);
        listView = findViewById(R.id.listview);
        videoView = findViewById(R.id.video_view);
        progressBar = findViewById(R.id.progress_bar);
        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.down_list, data);
        listView.setAdapter(adapter);
        next_button.setEnabled(false);
        HttpsAdress.setText("https://v.douyin.com/ecDrYfC/");


        listView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = view.findViewById(R.id.text);
                String httplink = textView.getText().toString();
//                Intent intent = new Intent();
//                intent.setData(Uri.parse(httplink));
//                intent.setAction(Intent.ACTION_VIEW);
//                startActivity(intent);

                DownloadUtil.get().download(httplink, dirName, new DownloadUtil.OnDownloadListener() {
                    @Override
                    public void onDownloadSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent();
                                intent.setData(Uri.parse(httplink));
                                intent.setAction(Intent.ACTION_VIEW);
                                startActivity(intent);
                                Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onDownloading(final int progress) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                                Log.d(TAG, "progress" + progress);
                            }
                        });
                    }

                    @Override
                    public void onDownloadFailed() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.removeAll(data);
                adapter.notifyDataSetChanged();
                SearchVideo("");
            }
        });

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                data.removeAll(data);
                adapter.notifyDataSetChanged();
                SearchVideo(String.valueOf(maxCursor));
            }
        });
    }

    private void SearchVideo(String value) {
        new Thread() {
            @Override
            public void run() {
                if (!HttpsAdress.getText().toString().equals("")) {
                    if (isHttpUrl(HttpsAdress.getText().toString())) {
                        Log.d(TAG, HttpsAdress.getText().toString());
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("link", HttpsAdress.getText().toString())
                                .add("max_cursor", value)
                                .build();
                        Request request = new Request.Builder()
                                .url("https://douyin.hucheng123.xin/api/other/searchDoyinList")
                                .post(requestBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(TAG, "get请求失败");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            responseData = response.body().string();
                                            Log.d(TAG, responseData);
                                            JSONObject jsonObject = new JSONObject(responseData);
                                            JSONObject responseList = jsonObject.getJSONObject("data");
                                            JSONArray responseAdress = responseList.getJSONArray("list");
                                            hasMore = responseList.getBoolean("has_more");
                                            maxCursor = responseList.getLong("max_cursor");
                                            Log.d(TAG, "maxCursor=" + maxCursor);
                                            Message msg = handler.obtainMessage(MSG_REFRESH_UI);
                                            handler.sendMessage(msg);
                                            for (int i = 0; i < responseAdress.length(); i++) {
                                                dataAdress = responseAdress.getString(i);
                                                data.add(dataAdress);
                                                Log.d(TAG, String.valueOf(dataAdress));
                                            }
                                            if(hasMore) {
                                                Message msg1 = handler.obtainMessage(MSG_REFRESH_ENABLED);
                                                handler.sendMessage(msg1);
                                                Log.d(TAG, "hasMore = " + hasMore);
                                            } else {
                                                Message msg2 = handler.obtainMessage(MSG_REFRESH_DISENABLED);
                                                handler.sendMessage(msg2);
                                            }
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                            Log.d(TAG, "e.printStackTrace()");
                                            Looper.prepare();
                                            Toast.makeText(MainActivity.this, "网址连接失败", Toast.LENGTH_SHORT).show();
                                            Looper.loop();
                                        }
                                    }
                                }.start();
                            }
                        });
                    } else {
                        Looper.prepare();
                        Toast.makeText(MainActivity.this, "请输入有效的地址链接", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "请输入地址链接", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }
        }.start();
    }

    // 播放视频
    private void startVideo(String videoURI) {
        videoView.setVisibility(View.VISIBLE);
        //videoView.setLayoutParams(new RelativeLayout.LayoutParams(UtilsTools.getCurScreenWidth(mContext), UtilsTools.getCurScreenWidth(mContext) / 3 * 4)); // 此行代码是设置视频的宽高比是3/4,不需要就注释掉即可
        // 设置播放加载路径
        //        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.aaa));
        videoView.setVideoURI(Uri.parse(videoURI));
        // 播放
        videoView.start();
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(videoView);
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                videoView.setVisibility(View.GONE);
            }

        });
    }

    private boolean isHttpUrl(String urls) {
        boolean isurl;
        //设置正则表达式
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";
        //对比
        Pattern pat = Pattern.compile(regex.trim());
        Matcher mat = pat.matcher(urls.trim());
        //判断是否匹配
        isurl = mat.matches();
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (MSG_REFRESH_UI == msg.what) {
                adapter.notifyDataSetChanged();
            } else if(MSG_REFRESH_ENABLED == msg.what) {
                next_button.setEnabled(true);
            } else if(MSG_REFRESH_DISENABLED == msg.what) {
                next_button.setEnabled(false);
            }
        }
    };
}