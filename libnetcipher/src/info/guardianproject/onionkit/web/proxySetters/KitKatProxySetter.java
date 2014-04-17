package info.guardianproject.onionkit.web.proxySetters;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Proxy;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@TargetApi(19)
public class KitKatProxySetter {

    private final String TAG;
    private final String appClass;
    private final Context appContext;
    private final String host;
    private final int port;

    public KitKatProxySetter(String tag, String appClass, Context appContext, String host, int port){
        TAG = tag;
        this.appClass = appClass;
        this.appContext = appContext;
        this.host = host;
        this.port = port;
    }

    public boolean canApply(){
        return true;
    }

    public boolean setProxy() {
        //Context appContext = webView.getContext().getApplicationContext();

        if (host != null)
        {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port + "");
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port + "");
        }

        try {
            Class applictionCls = Class.forName(appClass);
            Field loadedApkField = applictionCls.getField("mLoadedApk");
            loadedApkField.setAccessible(true);
            Object loadedApk = loadedApkField.get(appContext);
            Class loadedApkCls = Class.forName("android.app.LoadedApk");
            Field receiversField = loadedApkCls.getDeclaredField("mReceivers");
            receiversField.setAccessible(true);
            ArrayMap receivers = (ArrayMap) receiversField.get(loadedApk);
            for (Object receiverMap : receivers.values()) {
                for (Object rec : ((ArrayMap) receiverMap).keySet()) {
                    Class clazz = rec.getClass();
                    if (clazz.getName().contains("ProxyChangeListener")) {
                        Method onReceiveMethod = clazz.getDeclaredMethod("onReceive", Context.class, Intent.class);
                        Intent intent = new Intent(Proxy.PROXY_CHANGE_ACTION);

                        if (host != null)
                        {
                            /*********** optional, may be need in future *************/
                            final String CLASS_NAME = "android.net.ProxyProperties";
                            Class cls = Class.forName(CLASS_NAME);
                            Constructor constructor = cls.getConstructor(String.class, Integer.TYPE, String.class);
                            constructor.setAccessible(true);
                            Object proxyProperties = constructor.newInstance(host, port, null);
                            intent.putExtra("proxy", (Parcelable) proxyProperties);
                            /*********** optional, may be need in future *************/
                        }

                        onReceiveMethod.invoke(rec, appContext, intent);
                    }
                }
            }
            return true;
        } catch (ClassNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (NoSuchFieldException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (IllegalAccessException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (IllegalArgumentException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (NoSuchMethodException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (InvocationTargetException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        } catch (InstantiationException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Log.v(TAG, e.getMessage());
            Log.v(TAG, exceptionAsString);
        }
        return false;    }


}
