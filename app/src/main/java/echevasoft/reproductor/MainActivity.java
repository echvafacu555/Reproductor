package echevasoft.reproductor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;


import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;



import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class MainActivity extends AppCompatActivity {

    private AdManagerAdView mAdManagerAdView;

    ListView listview;
    String[] items;

    private static final int MY_PERMISSIONS_REQUEST_CODE = 123;
    AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        listview = findViewById(R.id.listViewSong);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        mAdManagerAdView = findViewById(R.id.adManagerAdView);
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        mAdManagerAdView.loadAd(adRequest);
        checkPermission();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.Calificar:

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=echevasoft.cancionesderiverplate"));
                intent.setPackage("com.android.vending");
                startActivity(intent);

                return true;

            case R.id.Compartir:
                Intent intent2 = new Intent(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, "¡Descarga la App Canciones de River Plate y disfruta de más de 40 canciones del Más Grande, Lejos https://play.google.com/store/apps/details?id=echevasoft.cancionesderiverplate");
                startActivity(Intent.createChooser(intent2, "Compartir con"));
                return true;

            case R.id.Salir:
                finishAffinity();
                System.exit(0);
                return true;
        }

        return super.onOptionsItemSelected(item);

    }
    protected void checkPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Si los permisos son denegados
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.RECORD_AUDIO)
            ) {
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Por favor, acepta los permisos para disfrutar de las mejores canciones del " +
                        "Más Grande, Lejos!");
                builder.setTitle("Solicitud de permisos");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                              MainActivity.this,

                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.RECORD_AUDIO
                                },
                                MY_PERMISSIONS_REQUEST_CODE
                        );
                    }
                });
                builder.setNeutralButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                               Manifest.permission.RECORD_AUDIO
                        },
                        MY_PERMISSIONS_REQUEST_CODE
                );
            }
        } else {
            copyAssets(this);
            displaySongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CODE: {
                // When request is cancelled, the results array are empty
                if (
                        (grantResults.length > 0) &&
                                (grantResults[0]
                                        + grantResults[1]
                                        + grantResults[2]
                                        == PackageManager.PERMISSION_GRANTED
                                )
                ) {
                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "/Android/data/echevasoft/songs");

                    if (!mediaStorageDir.exists()) {
                     //   Log.d("errrrrra", "failed to create directory");
                        mediaStorageDir.mkdirs();
                    }
                    copyAssets(this);
                    displaySongs();
                } else {
                    // Permissions are denied
                    Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public ArrayList<File> findSong(File file) {
            ArrayList<File> arrayList = new ArrayList<>();
            File[] files = file.listFiles();

        try {
            for (File singlefile : files) {

                if (singlefile.isDirectory() && singlefile.isHidden()) {
                    arrayList.addAll(findSong(singlefile));

                } else {
                    if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                        arrayList.add(singlefile);
                    }
                }
            }

        } catch (Exception e) {
        //    e.printStackTrace();
            Toast.makeText(this, R.string.error_import, Toast.LENGTH_SHORT).show();
        }
        return arrayList;
    }

    public void displaySongs() {
        final ArrayList<File> mySongs = findSong(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/echevasoft/songs"));

        items = new String[mySongs.size()];
        for (int i = 0; i < mySongs.size(); i++) {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");

        }
          /*    ArrayAdapter<String> myAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
                    listview.setAdapter(myAdapter);*/
        CustomAdapter customAdapter = new CustomAdapter();
        listview.setAdapter(customAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String songName = (String) listview.getItemAtPosition(i);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", mySongs)
                        .putExtra("songname", songName)
                        .putExtra("pos", i));
            }
        });

    }


    class CustomAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View myView = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textsong = myView.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(items[i]);
            return myView;
        }
    }


    public void copyAssets(Context context) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
           // Log.e("error", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                out = new FileOutputStream(Environment.getExternalStorageDirectory()+"/Android/data/echevasoft/songs/" + filename);

                copyFile(in, out);
            } catch(IOException e) {
             //  Log.e("error", "Failed to copy asset file: " + filename, e);

            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                        in = null;
                    } catch (IOException e) {


                    }
                }
                if (out != null) {
                    try {
                        out.flush();
                        out.close();
                        out = null;
                    } catch (IOException e) {

                    }
                }
            }
        }

    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }




}


