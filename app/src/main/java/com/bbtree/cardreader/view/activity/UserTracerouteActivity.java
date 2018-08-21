package com.bbtree.cardreader.view.activity;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bbtree.cardreader.R;
import com.bbtree.cardreader.utils.NetWorkUtil;
import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zzz on 2016/3/9.
 * 网络检测
 */
public class UserTracerouteActivity extends Activity {
    private String app_path;
    private String isConnect = "";
    private String ConnType = "";
    private String localIp = "";
    private String networkGateway = "";
    private String localDNS = "";
    private LinearLayout ll_contanter;
    private ScrollView scrollView;
    private int percent = 0;
    private TextView tv_percent;
    private List<String> urlList;
    private double count = 0;
    private StringBuilder sb ;  //完成之后需要复制的内容. sb.toString();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_check_traceroute);
        app_path = getApplicationContext().getFilesDir().getAbsolutePath();
        varifyFile(getApplicationContext(), "busybox");
        varifyFile(getApplicationContext(), "traceroute");
        isConnect = NetWorkUtil.isNetworkConnected(getApplicationContext()) ? "已连接" : "未连接";
        getConnType();
        localIp = getLocalIp();
        localDNS = NetWorkUtil.getDNS();
        networkGateway = NetWorkUtil.getMacAddress(getApplicationContext());
        ll_contanter = (LinearLayout) findViewById(R.id.ll_contanter);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        tv_percent = (TextView) findViewById(R.id.tv_percent);
        tv_percent.setClickable(false);
        tv_percent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserTracerouteActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= 11) {
                    android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setText(sb.toString());
                } else {
                    // 得到剪贴板管理器
                    android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(null, sb.toString()));
                }
            }
        });

        sb = new StringBuilder();


        //需要提供需要检测的urlList
        urlList = new ArrayList<>();
        urlList.add("www.baidu.com");
        urlList.add("www.youku.com");
        count = urlList.size() * 32;


        ObservableFrom(urlList)
//                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(new DefaultObserver<String>() {
                    @Override
                    public void onStart() {
//                        request(1);
                        Logger.d("TAG", "onStart");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.d("TAG", "onError" + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Logger.d("TAG", "onCompleted            onCompleted ");
                        tv_percent.setClickable(true);
                        tv_percent.setText("复制诊断报告");
                    }

                    @Override
                    public void onNext(String s) {
                        Logger.d("MainActivity", "byOnNext" + s);
//                        request(1);
                        addTextView(s);
                    }
                });
    }


    private Observable<String> ObservableFrom(List<String> list) {
        return Observable.fromIterable(list)
                .subscribeOn(Schedulers.io())
                .map(new Function<String, Map>() {
                    @Override
                    public Map apply(String s) {
                        return getIp(s);
                    }
                })
                .map(new Function<Map, Map>() {
                    @Override
                    public Map apply(Map mapInfos) {

                        return addPing(mapInfos);
                    }
                })
                .concatMap(new Function<Map, Observable<String>>() {
                    @Override
                    public Observable<String> apply(final Map map) {
                        return creatMessage(map);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(@NonNull String s) throws Exception {
                        sb.append(s +"\n");
                        percent++;
                        double v = percent / count;
                        tv_percent.setText((int) (v * 100) + "%");
                        return true;
                    }
                });
    }

    private Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            int off = ll_contanter.getMeasuredHeight() - scrollView.getHeight();
            if (off > 0) {
                scrollView.scrollTo(0, off);
            }
        }
    };

    private Observable<String> creatMessage(final Map map) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> subscriber) throws Exception {
                Runtime runtime = Runtime.getRuntime();
                try {
                    Process process = runtime.exec("." + app_path + "/traceroute " + (String) map.get("ip"));
                    InputStream is = process.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line = "";
                    while ((line = br.readLine()) != null) {
                        if (map.containsKey("pingInfoString")) {
                            subscriber.onNext((String) map.get("pingInfoString"));
                            map.remove("pingInfoString");
                        }
                        Logger.d("TAG", line);
                        subscriber.onNext(line);
                    }
                    br.close();
                    subscriber.onComplete();

                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        });
    }

    private void addTextView(String s) {
        Logger.d("TAG", "onNext" + s);
        new Handler().post(mScrollToBottom);

        TextView textView = new TextView(UserTracerouteActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textView.setText(s);
        ll_contanter.addView(textView, params);
        int height = ll_contanter.getHeight();
        Logger.d("TAG", height + "height");
    }


    private Map addPing(Map mapInfos) {
        Map map = new HashMap();
        String pingInfoString = "开始诊断\n诊断域名 " + mapInfos.get("url") + "\n \n当前是否联网：" + isConnect + "\n当前联网类型：" +
                ConnType + "\n当前本机IP：" + localIp + "\n本地网关：" + networkGateway + "\n本地DNS：" +
                localDNS + "\n远端域名：" + mapInfos.get("url") + "\nDNS解析结果：" + mapInfos.get("ip") + " (" + mapInfos.get("time") + "ms" + ")";
        int time = 0;
        int times = 0;
        try {
            Process p = Runtime.getRuntime().exec("ping -c 4 -w 5 " + mapInfos.get("ip"));
            InputStream inputStream = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            StringBuffer buffer = new StringBuffer();
            while ((line = in.readLine()) != null) {
                buffer.append(line + "\n");
            }
            String s = buffer.toString();
            String[] split = s.split("\\n");
            for (String info : split) {
                if (info.contains("time=")) {
                    int i = Double.valueOf(info.substring(info.lastIndexOf("time=") + 5, info.length() - 3)).intValue();
                    time += i;
                    times++;
                    if (times == 1) {
                        pingInfoString += "\n";
                    }
                    pingInfoString += times + "'s " + "time=" + i + "ms, ";
                }
            }
            int i = time / times;
            pingInfoString += "average=" + i + "ms";
            map.put("ip", mapInfos.get("ip"));
            map.put("pingInfoString", pingInfoString);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    private void getConnType() {
        int currentNetworkType = NetWorkUtil.getCurrentNetworkType(getApplicationContext());
        switch (currentNetworkType) {
            case NetWorkUtil.NETWORK_2G:
                ConnType = "2G";
                break;
            case NetWorkUtil.NETWORK_3G:
                ConnType = "3G";
                break;
            case NetWorkUtil.NETWORK_4G:
                ConnType = "4G";
                break;
            case NetWorkUtil.NETWORK_WIFI:
                ConnType = "WIFI";
                break;
            default:
                ConnType = "未知";
                break;
        }
    }

    private String getLocalIp() {
        if (ConnType.equals("WIFI")) {
            return NetWorkUtil.getWIFILocalIpAdress(getApplicationContext());
        } else {
            return NetWorkUtil.getGPRSLocalIpAddress();
        }
    }


    public Map getIp(String url) {
        try {
            long l = System.currentTimeMillis();
            Map map = new HashMap();
            InetAddress name = InetAddress.getByName(url);
            String hostAddress = name.getHostAddress();
            long l1 = System.currentTimeMillis();
            map.put("ip", hostAddress);
            map.put("url", url);
            map.put("time", l1 - l);
            return map;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验证文件是否存在, 如果不存在就拷贝
     */
    private void varifyFile(Context context, String fileName) {
        try {
            /* 查看文件是否存在, 如果不存在就会走异常中的代码 */
            context.openFileInput(fileName);
        } catch (FileNotFoundException notfoundE) {
            try {
            	/* 拷贝文件到app安装目录的files目录下 */
                copyFromAssets(context, fileName, fileName);
                /* 修改文件权限脚本 */
                String script = "chmod 700 " + app_path + "/" + fileName;
                /* 执行脚本 */
                exe(script);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将文件从assets目录中拷贝到app安装目录的files目录下
     */
    private void copyFromAssets(Context context, String source,
                                String destination) throws IOException {
		/* 获取assets目录下文件的输入流 */
        InputStream is = context.getAssets().open(source);
		/* 获取文件大小 */
        int size = is.available();
		/* 创建文件的缓冲区 */
        byte[] buffer = new byte[size];
		/* 将文件读取到缓冲区中 */
        is.read(buffer);
		/* 关闭输入流 */
        is.close();
		/* 打开app安装目录文件的输出流 */
        FileOutputStream output = context.openFileOutput(destination,
                Context.MODE_PRIVATE);
		/* 将文件从缓冲区中写出到内存中 */
        output.write(buffer);
		/* 关闭输出流 */
        output.close();
    }

    /**
     * 执行 shell 脚本命令
     */
    private List<String> exe(String cmd) {
		/* 获取执行工具 */
        Process process = null;
		/* 存放脚本执行结果 */
        List<String> list = new ArrayList<String>();
        try {
        	/* 获取运行时环境 */
            Runtime runtime = Runtime.getRuntime();
        	/* 执行脚本 */
            process = runtime.exec(cmd);
            /* 获取脚本结果的输入流 */
            InputStream is = process.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            /* 逐行读取脚本执行结果 */
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
