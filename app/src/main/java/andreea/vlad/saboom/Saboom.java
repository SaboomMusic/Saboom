package andreea.vlad.saboom;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Saboom extends AppCompatActivity implements IACRCloudListener{
    private final static int ASK_MULTIPLE_PERMISSION_REQUEST_CODE = 1268;

    private ACRCloudClient mClient;
    private ACRCloudConfig mConfig;

    private boolean mProcessing = false;
    private boolean initState = false;

    private TextView mVolume, mResult, titlu, artist;
    private Button start, stop, reset;

    private long startTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saboom);

        requestPermissions();

        mVolume = (TextView) findViewById(R.id.volume);
        mResult = (TextView) findViewById(R.id.result);
        titlu = (TextView) findViewById(R.id.Titlu);
        artist = (TextView) findViewById(R.id.Artist);
        start = (Button) findViewById(R.id.start);
        stop = (Button) findViewById(R.id.cancel);
        reset = (Button) findViewById(R.id.clear);

        titlu.setText("Title");
        artist.setText("Artist");

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
                start.setText("WAIT");
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
                start.setText("LISTEN");
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mResult.setText("");
                titlu.setText("Title");
                artist.setText("Artist");
                start.setText("LISTEN");
            }
        });

        this.mConfig = new ACRCloudConfig();
        this.mConfig.acrcloudListener = this;
        this.mConfig.context = this;
        this.mConfig.host = "identify-eu-west-1.acrcloud.com";
        this.mConfig.accessKey = "61019259e25a1b640b316eb5a4de593d";
        this.mConfig.accessSecret = "I55ZF8gzVlHsDiTgj217diEv9YAQ5tDW4JxvUvtT";
        this.mConfig.protocol = ACRCloudConfig.ACRCloudNetworkProtocol.PROTOCOL_HTTPS;
        this.mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;

        this.mClient = new ACRCloudClient();
        this.initState = this.mClient.initWithConfig(this.mConfig);
        if (this.initState) {
            this.mClient.startPreRecord(3000);
        }
    }

    public void start() {
        if (!this.initState) {
            Toast.makeText(this, "init error", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mProcessing) {
            mProcessing = true;
            mVolume.setText("");
            mResult.setText("");
            titlu.setText("Title");
            artist.setText("Artist");
            if (this.mClient == null || !this.mClient.startRecognize()) {
                mProcessing = false;
                mResult.setText("start error!");
            }
            startTime = System.currentTimeMillis();
        }
    }

    protected void cancel() {
        if (mProcessing && this.mClient != null) {
            mProcessing = false;
            this.mClient.cancel();
            mResult.setText("");
            mVolume.setText("");
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        ASK_MULTIPLE_PERMISSION_REQUEST_CODE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onResult(String result) {
        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }

        String tres = "\n";
        String[] artistul = {"","","","","","","",""};
        String[] titlul = {"","","","","","","",""};

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if(j2 == 0){
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("humming")) {
                    JSONArray hummings = metadata.getJSONArray("humming");
                    for(int i=0; i<hummings.length(); i++) {
                        JSONObject tt = (JSONObject) hummings.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = tres + (i+1) + ".  " + title + "\n";
                        titlul[i] = title;
                        artistul[i] = "- " + artist + " -";
                    }
                }
                if (metadata.has("music")) {
                    JSONArray musics = metadata.getJSONArray("music");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        titlul[i] = title;
                        artistul[i] = "- " + artist + " -";
                    }
                }
                if (metadata.has("streams")) {
                    JSONArray musics = metadata.getJSONArray("streams");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        String channelId = tt.getString("channel_id");
                        tres = tres + (i+1) + ".  Title: " + title + "    Channel Id: " + channelId + "\n";
                    }
                }
                if (metadata.has("custom_files")) {
                    JSONArray musics = metadata.getJSONArray("custom_files");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        tres = tres + (i+1) + ".  Title: " + title + "\n";
                    }
                }
                //tres = tres + "\n\n" + result;
            }else{
                tres = result;
            }
        } catch (JSONException e) {
            tres = result;
            e.printStackTrace();
        }

        mResult.setText("");
        titlu.setText(titlul[0]);
        artist.setText(artistul[0]);
        start.setText("AGAIN");
    }

    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        mVolume.setText("Volume " + volume + " - elapsed time：" + time + " s");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("Saboom", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }
}
