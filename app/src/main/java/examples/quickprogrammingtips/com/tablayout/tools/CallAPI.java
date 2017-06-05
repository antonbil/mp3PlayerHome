package examples.quickprogrammingtips.com.tablayout.tools;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import examples.quickprogrammingtips.com.tablayout.OnTaskCompleted;

/**
 * makes http-requests
 *
 * Created by anton on 27-10-15.
 */
public class CallAPI extends AsyncTask<String, String, String> {
    private static final char PARAMETER_DELIMITER = '&';
    private static final char PARAMETER_EQUALS_CHAR = '=';

    /**
     *helper function to create string from key-value-pairs
     * @param parameters pairwise description - value strings
     * @return parameters converted to string
     */
    public static String createQueryStringForParameters(Map<String, String> parameters) {
        StringBuilder parametersAsQueryString = new StringBuilder();
        if (parameters != null) {
            boolean firstParameter = true;

            for (String parameterName : parameters.keySet()) {
                if (!firstParameter) {
                    parametersAsQueryString.append(PARAMETER_DELIMITER);
                }

                try {
                    parametersAsQueryString.append(parameterName)
                            .append(PARAMETER_EQUALS_CHAR)
                            .append(URLEncoder.encode(
                                    parameters.get(parameterName),"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                firstParameter = false;
            }
        }
        return parametersAsQueryString.toString();
    }

    /**
     *
     * @param is stream to be changed
     * @return stream converted to string
     */
    private static String convertStreamToString(InputStream is) {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "{\"error\":\"no output\"}";
        }
    }
    private OnTaskCompleted listener;
    private String call;

    /**
     * constructor for CallAPI
     * @param listener = can be called as callback if result must be shown
     * @param call=returned in callback so listener knows what call is processed
     */
    public CallAPI(OnTaskCompleted listener, String call){
        this.listener=listener;
        this.call=call;
    }

    /**
     * do http-request in background
     * can be get or post-request
     * @param params input parameters
     * @return String json-result of request
     */
    @Override
//    protected String doInBackground(String... params) {
//        String output="";        try {
//        String httpsURL = "https://api.spotify.com/v1/albums/19K3IHYeVkUTjcBHGfbCOi";
//        URL myurl = new URL(httpsURL);
//        HttpsURLConnection con = (HttpsURLConnection)myurl.openConnection();
//
//        InputStream ins;
//            int status = con.getResponseCode();
//            Log.v("samba","request-code:"+status);
//
//            if(status >= HttpsURLConnection.HTTP_BAD_REQUEST)
//                ins = con.getErrorStream();
//            else
//                ins = con.getInputStream();
//            //ins= con.getInputStream();
//        InputStreamReader isr = new InputStreamReader(ins);
//        BufferedReader in = new BufferedReader(isr);
//
//
//        String inputLine;
//
//        while ((inputLine = in.readLine()) != null)
//        {
//            //System.out.println(inputLine);
//            output +="\n"+inputLine;
//        }
//
//        in.close();
//    }   catch (Exception e){}
//        return output;
//    }

        protected String doInBackground(String... params) {
        String urlString="https://api.spotify.com/v1/albums/"+URLEncoder.encode("19K3IHYeVkUTjcBHGfbCOi");//params[0]; // URL to call
        String request=params[1];//get or post

        String resultToDisplay;

        InputStream in = null;

        HttpsURLConnection urlConnection;
        Context context = (Context)this.listener;//context used to get string resources
        // HTTP Get
        try {

            URL url = new URL("https://api.spotify.com/v1/albums/0sNOF9WDwhWunNAHPD3Baj");

            urlConnection = (HttpsURLConnection) url.openConnection();
            int status = urlConnection.getResponseCode();
            Log.v("samba","request-code:"+status);

            if(status >= HttpsURLConnection.HTTP_BAD_REQUEST)
                in = urlConnection.getErrorStream();
            else
                in = urlConnection.getInputStream();            //use only get-requests for Spotify
            //http POST
//            if (request.equals("POST")){
//                String postParameters=params[2];
//                urlConnection.setDoInput (true);
//                urlConnection.setDoOutput (true);
//                urlConnection.setUseCaches (false);
//                urlConnection.setRequestProperty("Host", "hz.nl");
//                urlConnection.setRequestMethod("POST");
//                urlConnection.setFixedLengthStreamingMode(
//                        postParameters.getBytes().length);
//                try (PrintWriter out = new PrintWriter(urlConnection.getOutputStream())) {
//                    out.print(postParameters);
//                    Log.v("Post-parameters:",postParameters);
//                    out.close();
//                } catch (Exception e){}
//            }//http POST


        } catch (Exception e ) {

            Log.v("samba","error request:"+(e.getMessage()));

            return e.getMessage();

        }
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultToDisplay = convertStreamToString(in);
        Log.v("Result in CallApi:",resultToDisplay);
        return resultToDisplay;//resultToDisplay;
    }

    /**
     * inform listener that request is finished
     * @param result String with json-result of request
     */
    protected void onPostExecute(String result) {
        listener.onTaskCompleted(result,call);
    }


} // end CallAPI