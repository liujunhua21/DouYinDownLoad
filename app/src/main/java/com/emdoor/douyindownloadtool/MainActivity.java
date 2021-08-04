package com.emdoor.douyindownloadtool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
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
    private ArrayList<String> data = new ArrayList<String>();
    private ListView listView = null;
    private ArrayAdapter<String> adapter = null;
    private static final int MSG_REFRESH_UI = 130;
    private static final int MSG_REFRESH_ENABLED = 131;
    private static final int MSG_REFRESH_DISENABLED = 132;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpsAdress = findViewById(R.id.HttpsAdress);
        search_button = findViewById(R.id.search_button);
        next_button = findViewById(R.id.next_button);
        listView = findViewById(R.id.listview);
        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.down_list, data);
        listView.setAdapter(adapter);
        next_button.setEnabled(false);
        HttpsAdress.setText("https://v.douyin.com/ecDrYfC/");

        listView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView textView = view.findViewById(R.id.text);
                String httplink = textView.getText().toString();
                Intent intent = new Intent();
                intent.setData(Uri.parse(httplink));
                intent.setAction(Intent.ACTION_VIEW);
                startActivity(intent);
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