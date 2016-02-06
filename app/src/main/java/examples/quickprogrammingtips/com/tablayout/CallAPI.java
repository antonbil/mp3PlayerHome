package examples.quickprogrammingtips.com.tablayout;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

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
    public CallAPI(OnTaskCompleted listener,String call){
        this.listener=listener;
        this.call=call;
    }

    /**
     * do http-request in background
     * can be get or post-request
     * @param params input parameters
     * @return String json-result of request
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected String doInBackground(String... params) {

        String urlString=params[0]; // URL to call
        String request=params[1];//get or post

        String resultToDisplay;

        InputStream in = null;

        HttpURLConnection urlConnection;

        //no Context, use OnTaskCompleted
        OnTaskCompleted context = (OnTaskCompleted)this.listener;//context used to get string resources
        // HTTP Get
        try {

            URL url = new URL(urlString);

            urlConnection = (HttpURLConnection) url.openConnection();
            //http POST
            if (request.equals("POST")){
                String postParameters=params[2];
                urlConnection.setDoInput (true);
                urlConnection.setDoOutput (true);
                urlConnection.setUseCaches (false);
                urlConnection.setRequestProperty("Host", "hz.nl");
                urlConnection.setRequestMethod("POST");
                urlConnection.setFixedLengthStreamingMode(
                        postParameters.getBytes().length);
                try (PrintWriter out = new PrintWriter(urlConnection.getOutputStream())) {
                    out.print(postParameters);
                    Log.v("Post-parameters:",postParameters);
                    out.close();
                }
            }//http POST


        } catch (Exception e ) {

            System.out.println(e.getMessage());

            return e.getMessage();

        }
        try {
            in = new BufferedInputStream(urlConnection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultToDisplay = convertStreamToString(in);
        Log.v("Result in CallApi:",resultToDisplay);
        listener.onTaskCompleted(resultToDisplay,call);
        return resultToDisplay;//resultToDisplay;
    }

    /**
     * inform listener that request is finished
     * @param result String with json-result of request
     */
    protected void onPostExecute(String result) {

        //listener.onTaskCompleted(result,call);
    }


} // end CallAPI
