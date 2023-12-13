package com.example.testserviceandroidvn;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private EditText edtDataIntent;
    private Button btnStartService;
    private Button btnStopService;
    private RelativeLayout layoutBottom;
    private ImageView imgSong, imgPlayOrPause, imgClear;
    private TextView tvTitleSong, tvSingleSong;
    private Song mSong;
    private boolean isPlaying;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                return;
            }

            mSong = (Song) bundle.get("object_song");
            isPlaying = bundle.getBoolean("status_player");
            int actionMusic = bundle.getInt("action_music");

            handleLayoutMusic(actionMusic);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("send_data_to_activity"));

        btnStartService = (Button) findViewById(R.id.btn_start_service);
        btnStopService = (Button) findViewById(R.id.btn_stop_service);
        layoutBottom = (RelativeLayout) findViewById(R.id.layout_bottom);
        imgSong =(ImageView) findViewById(R.id.img_song);
        imgPlayOrPause = (ImageView) findViewById(R.id.img_play_or_pause);
        imgClear = (ImageView) findViewById(R.id.img_clear);
        tvSingleSong = (TextView) findViewById(R.id.tv_single_song);
        tvTitleSong = (TextView) findViewById(R.id.tv_title_song);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickStartService();
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickStopService();
            }
        });
    }

    private void clickStopService() {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void clickStartService() {
        // Kiểm tra trạng thái của dịch vụ
        if (mSong != null && isPlaying) {
            // Dừng bài hát hiện tại nếu đang phát
            sendActionToService(MyService.ACTION_CLEAR);
        }
            // Tạo danh sách tạm thời cho việc chọn bài hát
            Song[] temporarySongList = new Song[] {
                    new Song("Vì yêu cứ đâm đầu", "Min", R.drawable.moi, R.raw.viyeucudamdau),
                    new Song("Đắng môi", "Phạm Trưởng", R.drawable.moi, R.raw.dangmoi),
                    new Song("Bên trên tầng lầu", "Tăng Duy Tân", R.drawable.moi, R.raw.bentrentanglau),
                    new Song("Cô nương xinh đẹp phải đi lấy chồng", "Long Mei Zi", R.drawable.moi, R.raw.conuongxinhdepphaidilaychong),
                    new Song("Dấu hiệu tình yêu", "Phạm Trưởng", R.drawable.moi, R.raw.dauhieutinhyeu),
                    new Song("Đưa nhau đi trốn", "Đen", R.drawable.moi, R.raw.duanhauditron),
                    new Song("Nàng kiều lỡ bước", "HKT", R.drawable.moi, R.raw.nangkieulobuoc),
                    new Song("Ngắm hoa lệ rơi", "Châu Khải Phong", R.drawable.moi, R.raw.ngamhoaleroi),
                    new Song("Ở trong thành phố", "Masew", R.drawable.moi, R.raw.otrongthanhpho),
                    new Song("Xem như em chẳng may", "Trung Ngon", R.drawable.moi, R.raw.xemnhuemchangmay)
            };

            // Hiển thị danh sách bài hát và cho phép người dùng chọn
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn bài hát");

            String[] songTitles = new String[temporarySongList.length];
            for (int i = 0; i < temporarySongList.length; i++) {
                songTitles[i] = temporarySongList[i].getTitle();
            }

            builder.setItems(songTitles, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Lấy bài hát được chọn từ danh sách tạm thời
                    Song selectedSong = temporarySongList[which];

                    // Gọi phương thức để bắt đầu dịch vụ với bài hát được chọn
                    startServiceWithSelectedSong(selectedSong);
                }
            });

            builder.show();
        }


    private void startServiceWithSelectedSong(Song song) {
        Intent intent = new Intent(this, MyService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song", song);
        intent.putExtras(bundle);

        startService(intent);
    }
    private void handleLayoutMusic(int action){
        switch (action){
            case MyService.ACTION_START:
                layoutBottom.setVisibility(View.VISIBLE);
                showInforSong();
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_PAUSE:
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_RESUME:
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_CLEAR:
                layoutBottom.setVisibility(View.GONE);
                break;
        }
    }
    private void showInforSong(){
        if (mSong == null) {
            return;
        }
        imgSong.setImageResource(mSong.getImage());
        tvTitleSong.setText(mSong.getTitle());
        tvSingleSong.setText(mSong.getSingle());

        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    sendActionToService(MyService.ACTION_PAUSE);
                }
                else {
                    sendActionToService(MyService.ACTION_RESUME);
                }
            }
        });
        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendActionToService(MyService.ACTION_CLEAR);
            }
        });

    }
    private void setStatusButtonPlayOrPause(){
        if (isPlaying) {
            imgPlayOrPause.setImageResource(R.drawable.pause);
        }
        else {
            imgPlayOrPause.setImageResource(R.drawable.iconstart);
        }
    }
    private void sendActionToService(int action){
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("action_music_service", action);

        startService(intent);
    }
}