package audio.rabid.rotatingwallpapers;

import android.app.IntentService;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class WallpaperChangerIntentService extends IntentService {

    private static final String URL = "https://random-image.x.rabid.audio/random-image";

    public WallpaperChangerIntentService() {
        super("WallpaperChangerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            if (connection.getResponseCode() >= 400) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = br.readLine();
                    if (line == null) break;
                    sb.append(line).append("\n");
                }
                throw new IOException(sb.toString());
            }
            wallpaperManager.setStream(connection.getInputStream());
        } catch (IOException e) {
            Toast.makeText(this, "Problem setting wallpaper", Toast.LENGTH_LONG).show();
            Log.e("WALLPAPER", "problem setting wallpaper", e);
        }
    }
}
