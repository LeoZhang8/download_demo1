package com.example.myapplication;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Toast.makeText(MainActivity.this, "下载完成", Toast.LENGTH_SHORT).show();
                try {
                    //跳转到显示下载内容的activity界面
                    Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                    dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(dm);
                } catch (ActivityNotFoundException ex){
                    Log.d("down",  "no activity for " + ex.getMessage());
                }
            } else if (intent.getAction().equals(DownloadManager.ACTION_NOTIFICATION_CLICKED)) {
                Toast.makeText(MainActivity.this, "用户点击了通知栏", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("download click....");
                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                String downloadurl = "https://www.zjitc.net/__local/1/AC/A4/219F7379D0541DE12E83053AD38_AFA6234A_1D5DA.jpg";
                long downloadId = downloadAPK(downloadurl,downloadManager);
            }
        });

        //注册广播，监听下载状态
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentfilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, intentfilter,RECEIVER_EXPORTED);
        }else{
            registerReceiver(receiver, intentfilter);
        }

    }

    public long downloadAPK(String downloadurl, DownloadManager downloadManager) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadurl));

        //设置Notifcaiton的标题和描述
        request.setTitle("文件下载");
        request.setDescription("下载中.....");

        //指定在WIFI状态下，执行下载操作。
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        //指定在MOBILE状态下，执行下载操作
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //下载情况是否显示在systemUI下拉状态栏中
        request.setVisibleInDownloadsUi(true);

        //设置Notification的显示，和隐藏
        /*
        在下载进行中时显示，在下载完成后就不显示了。可以设置如下三个值：
        VISIBILITY_HIDDEN 下载UI不会显示，也不会显示在通知中，如果设置该值，需要声明android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
        VISIBILITY_VISIBLE 当处于下载中状态时，可以在通知栏中显示；当下载完成后，通知栏中不显示
        VISIBILITY_VISIBLE_NOTIFY_COMPLETED 当处于下载中状态和下载完成时状态，均在通知栏中显示
        VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION 只在下载完成时显示在通知栏中。
        * */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //设置为可被媒体扫描器找到
        request.allowScanningByMediaScanner();

        //设置下载路径 sdcard/download/
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "11.jpg");

        //加入到下载的队列中。一旦下载管理器准备好执行并且连接可用，下载将自动启动。
        //一个下载任务对应唯一个ID， 此id可以用来去查询下载内容的相关信息
        long downloadID = downloadManager.enqueue(request);

        return downloadID;
    }
}