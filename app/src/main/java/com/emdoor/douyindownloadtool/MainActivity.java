package com.emdoor.douyindownloadtool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

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
    private EditText HttpsAdress;
    private Button search_button;
    private String responseData;
    private String dataAdress;
    private ArrayList<String> data = new ArrayList<String>();
    private ListView listView = null;
    private ArrayAdapter<String> adapter = null;
    private static final int MSG_REFRESH_UI = 130;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HttpsAdress = findViewById(R.id.HttpsAdress);
        search_button = findViewById(R.id.search_button);
        listView = findViewById(R.id.listview);
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, data);
        listView.setAdapter(adapter);
        HttpsAdress.setText("https://v.douyin.com/ecDrYfC/");

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(HttpsAdress != null) {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("link",HttpsAdress.getText().toString())
                                .add("max_cursor","")
                                .build();
                        Request request = new Request.Builder()
                                .url("https://douyin.hucheng123.xin/api/other/searchDoyinList")
                                .post(requestBody)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("Fail","get请求失败");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                try {
                                    responseData = response.body().string();
                                    JSONObject jsonObject = new JSONObject (responseData);
                                    JSONObject responseList = jsonObject.getJSONObject("data");
                                    JSONArray responseAdress = responseList.getJSONArray("list");
                                    for (int i = 0; i < responseAdress.length(); i++) {
                                        dataAdress = responseAdress.getString(i);
                                        Message msg = handler.obtainMessage(MSG_REFRESH_UI);
                                        handler.sendMessage(msg);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "请输入地址链接", Toast.LENGTH_SHORT);
                    }
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            if (MSG_REFRESH_UI == msg.what) {
                data.add(dataAdress);
                adapter.notifyDataSetChanged();
            }
        }
    };
}