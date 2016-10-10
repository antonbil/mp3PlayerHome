package examples.quickprogrammingtips.com.tablayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by anton on 10-10-16.
 */

public class MyExceptionHandler implements
        java.lang.Thread.UncaughtExceptionHandler {
    private final Context myContext;
    private final Class<?> myActivityClass;

    public MyExceptionHandler(Context context, Class<?> c) {

        myContext = context;
        myActivityClass = c;
    }

    public void uncaughtException(Thread thread, Throwable exception) {

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));
        System.err.println(stackTrace);// You can use LogCat too
        Intent intent = new Intent(myContext, myActivityClass);
        String s = stackTrace.toString();
        //you can use this String to know what caused the exception and in which Activity
        AlertDialog.Builder dialog = new AlertDialog.Builder(myContext);
        dialog.setTitle("Exception:"+exception.getMessage());
        dialog.setMessage(s);
        dialog.setNeutralButton("Cool", null);
        dialog.create().show();
        /*intent.putExtra("uncaughtException",
                "Exception is: " + stackTrace.toString());
        intent.putExtra("stacktrace", s);
        myContext.startActivity(intent);*/
        //for restarting the Activity
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}