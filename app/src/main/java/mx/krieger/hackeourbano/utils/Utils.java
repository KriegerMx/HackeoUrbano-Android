package mx.krieger.hackeourbano.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;

import mx.krieger.hackeourbano.R;
import mx.krieger.mapaton.clients.hackeoUrbanoAPI.HackeoUrbanoAPI;
import mx.krieger.mapaton.clients.mapatonPublicAPI.MapatonPublicAPI;

public class Utils {

    public static MapatonPublicAPI getMapatonPublicAPI(){
        MapatonPublicAPI.Builder builder = new MapatonPublicAPI.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) {
                httpRequest.setConnectTimeout(60 * 1000);
                httpRequest.setReadTimeout(60 * 1000);
            }
        });
        return builder.build();
    }

    public static HackeoUrbanoAPI getHackeoUrbanoPublicAPI(){
        HackeoUrbanoAPI.Builder builder = new HackeoUrbanoAPI.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), new HttpRequestInitializer() {
            public void initialize(HttpRequest httpRequest) {
                httpRequest.setConnectTimeout(60 * 1000);
                httpRequest.setReadTimeout(60 * 1000);
            }
        });
        return builder.build();
    }

    /*
    * Note: This horrible hack was done due to a bug on Google App Engine's auto-generated clients,
    * please excuse the mess. (╯°□°）╯︵ ┻━┻
    * */
    private static final String PUBLIC_API_BASE_URL = "https://public-api-dot-mapaton-public.appspot.com/_ah/api/mapatonPublicAPI/v1/";
    private static final String[][] PUBLIC_API_WEB_SERVICE = {
            {"trailsNearPoint", "POST"}
    };
    public static final int SERVICE_TRAILS_NEAR_POINT = 0;

    public static String doSimpleRequest(int service, JSONObject data) throws Exception {
        URL url = new URL(PUBLIC_API_BASE_URL + PUBLIC_API_WEB_SERVICE[service][0]);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setUseCaches(false);
        String result = null;

        try {
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setRequestMethod(PUBLIC_API_WEB_SERVICE[service][1]);

            OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
            out.write(data.toString());
            out.flush();
            out.close();

            StringBuilder sb = new StringBuilder();
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                result = sb.toString();
            } else {
                throw new Exception("Error " + responseCode + "\n" + urlConnection.getResponseMessage());
            }
        } finally {
            urlConnection.disconnect();
        }
        return result;
    }

    public static String manageAPIException(Context context, Exception e){
        String error = "Illegal State while handling Exception";
        if(context == null || e == null)
            return error;
        error = context.getString(R.string.error_generic_asynctask);
        e.printStackTrace();
        if(e instanceof GoogleJsonResponseException){
            error = context.getString(R.string.error_internal_server);
            GoogleJsonResponseException exception = (GoogleJsonResponseException) e;
            if(exception.getDetails() != null) {
                error = exception.getDetails().getMessage();
                if (error.contains(context.getString(R.string.api_common_exception_package_name))
                        || error.contains(context.getString(R.string.api_mapaton_exception_package_name))
                        || error.contains(context.getString(R.string.commons_exception_package_name)))
                    error = exception.getDetails().getMessage().split(": ")[1];
            }
        } else if (e instanceof HttpResponseException) {
            HttpResponseException exception = (HttpResponseException) e;
            error = context.getString(R.string.error_code) + exception.getStatusCode();
        } else if (e instanceof UnknownHostException) {
            error = context.getString(R.string.error_connection);
        } else if(e instanceof IOException){
            error = context.getString(R.string.error_connection);
        }
        return error;
    }

    public static int[] generateColorsByDividingSpectrum(int numOfColors){
        int[] colors = new int[numOfColors];
        float hueSegment = 360 / numOfColors;

        for(int i = 0; i < numOfColors; i++){
            float[] hsv = new float[3];

            hsv[0] =  hueSegment * (i + 1); //HUE
            hsv[1] =  0.70f; //SAT
            hsv[2] =  0.95f; //VAL

            colors[i] = Color.HSVToColor(hsv);
        }
        return colors;
    }
}
