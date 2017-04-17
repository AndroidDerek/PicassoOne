package com.example.picassoone;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/4/15 0015.
 * 使用Picasso和okhttp以及AsyncTask加载网络图片
 */

public class PicassoOne extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "GsonUtils";
    private static final int SUCCESS = 1;
    private static final int ERROR = 1;
    private ListView listView;
    private Button button;
    private Button asynctask;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picasso_one);
        initView();
        progressDialog = new ProgressDialog(PicassoOne.this);
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.listView);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        asynctask = (Button) findViewById(R.id.asynctask);
        asynctask.setOnClickListener(this);
    }

    private MyBaseAdapter myBaseAdapter;
    private MyOnScrollListener myOnScrollListener;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                Log.d(TAG, "SUCCESS");
                String json = (String) msg.obj;
                //服务器的数据获取成功
                try {
                    String iconUrlTest = new JSONObject(json).getJSONArray("info").getJSONObject(0).getString("icon");
                    Log.d(TAG, "iconUrlTest=" + iconUrlTest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                List<InfoItem> infoItemList = new ArrayList<>();
                try {
                    JSONArray infos = new JSONObject(json).getJSONArray("info");
                    for (int i = 0; i < infos.length(); i++) {
                        InfoItem infoItem = new InfoItem();
                        JSONObject info = infos.getJSONObject(i);
                        infoItem.setIcon(info.getString("icon"));
                        infoItem.setName(info.getString("name"));
                        infoItem.setSize(info.getString("size"));
                        infoItemList.add(infoItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "JSONException=" + e.toString());
                }
                //使用Picasso展示图片
                myBaseAdapter = null;
                isUtf8 = false;
                myBaseAdapter = new MyBaseAdapter(infoItemList);
                listView.setAdapter(myBaseAdapter);
                myOnScrollListener = new MyOnScrollListener();
                listView.setOnScrollListener(myOnScrollListener);
            }
        }
    };

    boolean isUtf8 = false;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                Log.d(TAG, "button");
                //开启子线程
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = "http://mobileapi.72g.com/index.php?tp=andv4/index_new&op=jifen&point=100";
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url)
                                .get()
                                .build();
                        try {
                            Response response = okHttpClient.newCall(request).execute();
                            //
                            if (response.isSuccessful()) {
                                //获取数据成功
                                String json = response.body().string();
                                Message message = Message.obtain();
                                message.obj = json;
                                message.what = SUCCESS;
                                handler.sendMessage(message);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.d(TAG, "IOException=" + e.toString());
                        }
                    }
                }).start();
                break;
            case R.id.asynctask:
                progressDialog.setTitle("loading...");
                String url_get = "http://mobileapi.72g.com/index.php?tp=andv4/index_new&op=jifen&point=100";
                //使用AsyncTask来处理网络请求
                MyAsyncTask myAsyncTask = new MyAsyncTask();
                myAsyncTask.execute(url_get);
                break;
        }
    }

    /**
     * listview的适配器
     */
    class MyBaseAdapter extends BaseAdapter {
        private List<InfoItem> infoItemList;

        public MyBaseAdapter(List<InfoItem> infoItemList) {
            this.infoItemList = infoItemList;
        }

        @Override
        public int getCount() {
            return infoItemList.size();
        }

        @Override
        public Object getItem(int i) {
            return infoItemList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (null == view) {
                view = getLayoutInflater().inflate(R.layout.list_item, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.name = (TextView) view.findViewById(R.id.name);
                viewHolder.size = (TextView) view.findViewById(R.id.size);
                viewHolder.icon = (ImageView) view.findViewById(R.id.icon);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            InfoItem infoItem = infoItemList.get(i);

            String name = infoItem.getName();

            //需要把GBK的乱码转换成UTF-8
            Log.d(TAG, "isUtf8=" + isUtf8);
            if (!isUtf8) {
                try {
                    name = new String(name.getBytes("gbk"), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.d(TAG, "UnsupportedEncodingException=" + e.toString());
                }
            }
            viewHolder.name.setText(name);
            viewHolder.size.setText(infoItem.getSize());
            //picasso展示图片;fit为图片的宽高填充父窗体
            Picasso.with(viewGroup.getContext())
                    .load(infoItem.getIcon())
                    .fit()
                    .placeholder(R.drawable.loading)
                    .into(viewHolder.icon);
            return view;
        }
    }

    private static class ViewHolder {
        TextView name;
        TextView size;
        ImageView icon;
    }

    /**
     * 当手指快速滑动的时候，Picasso不进行图片资源的加载展示，可以大大节约内存资源
     */
    class MyOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
            Picasso picasso = Picasso.with(absListView.getContext());
            //当处于触摸滑动的时候，或者处于停滞的时候
            if (i == SCROLL_STATE_IDLE || i == SCROLL_STATE_TOUCH_SCROLL) {
                picasso.resumeTag(absListView.getContext());
            } else {
                Log.d(TAG, "picasso.pauseTag");
                picasso.pauseTag(absListView.getContext());
            }
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {

        }
    }


    class MyAsyncTask extends AsyncTask<String, Void, List<InfoItem>> {

        //在准备加载前
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        /**
         * 在后台执行
         *
         * @param strings
         * @return
         */
        @Override
        protected List<InfoItem> doInBackground(String... strings) {

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                InputStream inputStream = urlConnection.getInputStream();
                int responseCode = urlConnection.getResponseCode();
                Log.d(TAG, "responseCode=" + responseCode);
                //请求服务器数据成功
                if (responseCode == 200) {
                    //InputStream 转换成 String
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    int len = 0;
                    byte[] buffers = new byte[1024];
                    while ((len = inputStream.read(buffers)) != -1) {
                        byteArrayOutputStream.write(buffers, 0, len);
                    }
                    String json = byteArrayOutputStream.toString();
                    Log.d(TAG, "json=" + json);
                    //解析json数据,保存到集合中
                    try {
                        String iconUrlTest = new JSONObject(json).getJSONArray("info").getJSONObject(0).getString("icon");
                        Log.d(TAG, "iconUrlTest=" + iconUrlTest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    List<InfoItem> infoItemList = new ArrayList<>();
                    try {
                        JSONArray infos = new JSONObject(json).getJSONArray("info");
                        for (int i = 0; i < infos.length(); i++) {
                            InfoItem infoItem = new InfoItem();
                            JSONObject info = infos.getJSONObject(i);
                            infoItem.setIcon(info.getString("icon"));
                            infoItem.setName(info.getString("name"));
                            infoItem.setSize(info.getString("size"));
                            infoItemList.add(infoItem);
                        }
                        //解析json数据成功，返回集合
                        return infoItemList;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "JSONException=" + e.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "IOException=" + e.toString());
            }
            return null;
        }

        /**
         * 在ui主线程中执行
         *
         * @param infoItems
         */
        @Override
        protected void onPostExecute(List<InfoItem> infoItems) {
            super.onPostExecute(infoItems);
            //清空之前的缓存
            myBaseAdapter = null;
            isUtf8 = true;
            if (null != infoItems) {
                //展示图片
                myBaseAdapter = new MyBaseAdapter(infoItems);
                listView.setAdapter(myBaseAdapter);
                progressDialog.dismiss();
                myOnScrollListener = new MyOnScrollListener();
                listView.setOnScrollListener(myOnScrollListener);
            }
        }
    }
}
