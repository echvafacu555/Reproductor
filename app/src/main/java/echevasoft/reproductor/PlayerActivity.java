package echevasoft.reproductor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class PlayerActivity extends AppCompatActivity {
    Button playbtn, btnfr, btnff, btnprev, btnnext;
    TextView txtsn;
    BarVisualizer visualizer;
    ImageView imageview;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {

            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();

        }
        super.onDestroy();
    }

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    private AdManagerAdView mAdManagerAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        playbtn = findViewById(R.id.playbtn);
        btnfr = findViewById(R.id.btnfr);
        btnff = findViewById(R.id.btnff);
        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);


        txtsn = findViewById(R.id.txtsn);

        visualizer = findViewById(R.id.visualizer);
        imageview = findViewById(R.id.imageview);



        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        txtsn.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        txtsn.setText(sname.replace(".mp3", "").replace(".wav", ""));


        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();



        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, delay);
            }
        }, delay);


        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    playbtn.setBackgroundResource(R.drawable.ic_play);
                    mediaPlayer.pause();
                } else {
                    playbtn.setBackgroundResource(R.drawable.ic_pause);
                    mediaPlayer.start();

                }
            }

        });



        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
             btnnext.performClick();
            }
        });


       int audiosessionid = mediaPlayer.getAudioSessionId();

        if (audiosessionid != -1) {
            visualizer.setAudioSessionId(audiosessionid);
        }

        //next song
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position + 1) % mySongs.size());

                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsn.setText(sname.replace(".mp3", "").replace(".wav", ""));
                mediaPlayer.start();
                playbtn.setBackgroundResource(R.drawable.ic_pause);

                int audiosessionid = mediaPlayer.getAudioSessionId();
                if (audiosessionid != -1) {
                    visualizer.setAudioSessionId(audiosessionid);
                }


            }
        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position - 1) < 0) ? (mySongs.size() - 1) : (position - 1);

                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                txtsn.setText(sname.replace(".mp3", "").replace(".wav", ""));
                mediaPlayer.start();
                playbtn.setBackgroundResource(R.drawable.ic_pause);

                int audiosessionid = mediaPlayer.getAudioSessionId();
                if (audiosessionid != -1) {
                    visualizer.setAudioSessionId(audiosessionid);
                }

            }
        });


        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 10000);
                }
            }


        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 10000);
                }
            }
        });



        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });



        mAdManagerAdView = findViewById(R.id.adManagerAdView);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        mAdManagerAdView.loadAd(adRequest);
    }


    public String createTime(int duracion) {
        String time = "";
        int min = duracion / 1000 / 60;
        int seg = duracion / 1000 % 60;
        time += min + ":";
        if (seg < 10) {
            time += "0";
        }
        time += seg;
        return time;
    }

}