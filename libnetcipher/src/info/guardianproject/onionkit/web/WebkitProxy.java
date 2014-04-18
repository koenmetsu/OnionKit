
package info.guardianproject.onionkit.web;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import info.guardianproject.onionkit.web.proxySetters.GingerBreadProxySetter;
import info.guardianproject.onionkit.web.proxySetters.ICSProxySetter;
import info.guardianproject.onionkit.web.proxySetters.KitKatProxySetter;
import info.guardianproject.onionkit.web.proxySetters.ProxySetter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WebkitProxy {

    private final static String DEFAULT_HOST = "localhost";//"127.0.0.1";
    private final static int DEFAULT_PORT = 8118;
    private final static int DEFAULT_SOCKS_PORT = 9050;

    private final static int REQUEST_CODE = 0;

    private final static String TAG = "OrbotHelpher";

    public static boolean setProxy(String appClass, Context ctx, String host, int port) throws Exception
    {
      
    	//setSystemProperties(host, port);

        for(ProxySetter proxySetter: getProxySetters(appClass, ctx, host, port)){
            if(proxySetter.canApply()){
                return proxySetter.setProxy();
            }
        }
        return false;
    }

    private static List<ProxySetter> getProxySetters(String appClass, Context ctx, String host, int port) {
        List<ProxySetter> proxySetters = new ArrayList<ProxySetter>();
        proxySetters.add(new GingerBreadProxySetter(ctx, host, port));
        proxySetters.add(new ICSProxySetter(host, port));
        proxySetters.add(new KitKatProxySetter(TAG, appClass, ctx, host, port));
        return proxySetters;
    }

    private static void setSystemProperties(String host, int port)
    {

    	System.setProperty("proxyHost", host);
        System.setProperty("proxyPort", port + "");

        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port + "");

        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port + "");

        
        System.setProperty("socks.proxyHost", host);
        System.setProperty("socks.proxyPort", DEFAULT_SOCKS_PORT + "");

        System.setProperty("socksProxyHost", host);
        System.setProperty("socksProxyPort", DEFAULT_SOCKS_PORT + "");
        
        
        /*
        ProxySelector pSelect = new ProxySelector();
        pSelect.addProxy(Proxy.Type.HTTP, host, port);
        ProxySelector.setDefault(pSelect);
        */
        /*
        System.setProperty("http_proxy", "http://" + host + ":" + port);
        System.setProperty("proxy-server", "http://" + host + ":" + port);
        System.setProperty("host-resolver-rules","MAP * 0.0.0.0 , EXCLUDE myproxy");

        System.getProperty("networkaddress.cache.ttl", "-1");
        */

    }
    

    private static boolean sendProxyChangedIntent(Context ctx, String host, int port) 
    {

        try
        {
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            if (proxyPropertiesClass != null)
            {
                Constructor c = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE,
                        String.class);
                
                if (c != null)
                {
                    c.setAccessible(true);
                    Object properties = c.newInstance(host, port, null);

                    Intent intent = new Intent(android.net.Proxy.PROXY_CHANGE_ACTION);
                    intent.putExtra("proxy",(Parcelable)properties);
                    ctx.sendBroadcast(intent);
                 
                }
                                
           }
        } catch (Exception e)
        {
            Log.e("ProxySettings",
                    "Exception sending Intent ",e);
        } catch (Error e)
        {
            Log.e("ProxySettings",
                    "Exception sending Intent ",e);
        }

        return false;

    }
    
    /**
    private static boolean setKitKatProxy0(Context ctx, String host, int port) 
    {
    	
    	try
        {
            Class cmClass = Class.forName("android.net.ConnectivityManager");

            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            if (cmClass != null && proxyPropertiesClass != null)
            {
                Constructor c = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE,
                        String.class);

                if (c != null)
                {
                    c.setAccessible(true);

                    Object proxyProps = c.newInstance(host, port, null);
                    ConnectivityManager cm =
                            (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

                    Method mSetGlobalProxy = cmClass.getDeclaredMethod("setGlobalProxy", proxyPropertiesClass);
                    
                    mSetGlobalProxy.invoke(cm, proxyProps);
                 
                    return true;
                }
                
           }
        } catch (Exception e)
        {
            Log.e("ProxySettings",
                    "ConnectivityManager.setGlobalProxy ",e);
        }

        return false;

    }
    */
	//CommandLine.initFromFile(COMMAND_LINE_FILE);
	
    /**
    private static boolean setKitKatProxy2 (Context ctx, String host, int port)
    {

    	String commandLinePath = "/data/local/tmp/orweb.conf";
    	 try
         {
             Class webViewCoreClass = Class.forName("org.chromium.content.common.CommandLine");

             if (webViewCoreClass != null)
             {
            	 for (Method method : webViewCoreClass.getDeclaredMethods())
            	 {
            		 Log.d("Orweb","Proxy methods: " + method.getName());
            	 }
            	 
                 Method m = webViewCoreClass.getDeclaredMethod("initFromFile", 
                		 String.class);
                 
                 if (m != null)
                 {
                     m.setAccessible(true);
                     m.invoke(null, commandLinePath);
                     return true;
                 }
                 else
                     return false;
             }
         } catch (Exception e)
         {
             Log.e("ProxySettings",
                     "Exception setting WebKit proxy through android.net.ProxyProperties: "
                             + e.toString());
         } catch (Error e)
         {
             Log.e("ProxySettings",
                     "Exception setting WebKit proxy through android.webkit.Network: "
                             + e.toString());
         }
    	 
    	 return false;
    }
    
    /**
    private static boolean setKitKatProxy (Context ctx, String host, int port)
    {
    	
    	 try
         {
             Class webViewCoreClass = Class.forName("android.net.Proxy");

             Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
             if (webViewCoreClass != null && proxyPropertiesClass != null)
             {
            	 for (Method method : webViewCoreClass.getDeclaredMethods())
            	 {
            		 Log.d("Orweb","Proxy methods: " + method.getName());
            	 }
            	 
                 Method m = webViewCoreClass.getDeclaredMethod("setHttpProxySystemProperty", 
                		 proxyPropertiesClass);
                 Constructor c = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE,
                         String.class);

                 if (m != null && c != null)
                 {
                     m.setAccessible(true);
                     c.setAccessible(true);
                     Object properties = c.newInstance(host, port, null);

                     m.invoke(null, properties);
                     return true;
                 }
                 else
                     return false;
             }
         } catch (Exception e)
         {
             Log.e("ProxySettings",
                     "Exception setting WebKit proxy through android.net.ProxyProperties: "
                             + e.toString());
         } catch (Error e)
         {
             Log.e("ProxySettings",
                     "Exception setting WebKit proxy through android.webkit.Network: "
                             + e.toString());
         }
    	 
    	 return false;
    }
    
    private static boolean resetProxyForKitKat ()
    {
    	
    	 try
         {
             Class webViewCoreClass = Class.forName("android.net.Proxy");

             Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
             if (webViewCoreClass != null && proxyPropertiesClass != null)
             {
            	 for (Method method : webViewCoreClass.getDeclaredMethods())
            	 {
            		 Log.d("Orweb","Proxy methods: " + method.getName());
            	 }
            	 
                 Method m = webViewCoreClass.getDeclaredMethod("setHttpProxySystemProperty", 
                		 proxyPropertiesClass);

                 if (m != null)
                 {
                     m.setAccessible(true);

                     m.invoke(null, null);
                     return true;
                 }
                 else
                     return false;
             }
         } catch (Exception e)
         {
             Log.e("ProxySettings",
                     "Exception setting WebKit proxy through android.net.ProxyProperties: "
                             + e.toString());
         } catch (Error e)
         {
             Log.e("ProxySettings",
                     "Exception setting WebKit proxy through android.webkit.Network: "
                             + e.toString());
         }
    	 
    	 return false;
    }**/

    public static void resetProxy(String appClass, Context ctx) throws Exception {
    	

        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");


        for(ProxySetter proxySetter: getProxySetters(appClass, ctx, null, 0)){
            if(proxySetter.canApply()){
                proxySetter.resetProxy();
                break;
            }
        }
         
    }

    public static Object getRequestQueue(Context ctx) throws Exception {
        Object ret = null;
        Class networkClass = Class.forName("android.webkit.Network");
        if (networkClass != null) {
            Object networkObj = invokeMethod(networkClass, "getInstance", new Object[] {
                ctx
            }, Context.class);
            if (networkObj != null) {
                ret = getDeclaredField(networkObj, "mRequestQueue");
            }
        }
        return ret;
    }

    private static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        // System.out.println(obj.getClass().getName() + "." + name + " = "+
        // out);
        return out;
    }

    private static void setDeclaredField(Object obj, String name, Object value)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private static Object invokeMethod(Object object, String methodName, Object[] params,
            Class... types) throws Exception {
        Object out = null;
        Class c = object instanceof Class ? (Class) object : object.getClass();
        if (types != null) {
            Method method = c.getMethod(methodName, types);
            out = method.invoke(object, params);
        } else {
            Method method = c.getMethod(methodName);
            out = method.invoke(object);
        }
        // System.out.println(object.getClass().getName() + "." + methodName +
        // "() = "+ out);
        return out;
    }

    public static Socket getSocket(Context context, String proxyHost, int proxyPort)
            throws IOException
    {
        Socket sock = new Socket();

        sock.connect(new InetSocketAddress(proxyHost, proxyPort), 10000);

        return sock;
    }

    public static Socket getSocket(Context context) throws IOException
    {
        return getSocket(context, DEFAULT_HOST, DEFAULT_SOCKS_PORT);

    }

    public static AlertDialog initOrbot(Activity activity,
            CharSequence stringTitle,
            CharSequence stringMessage,
            CharSequence stringButtonYes,
            CharSequence stringButtonNo,
            CharSequence stringDesiredBarcodeFormats) {
        Intent intentScan = new Intent("org.torproject.android.START_TOR");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        try {
            activity.startActivityForResult(intentScan, REQUEST_CODE);
            return null;
        } catch (ActivityNotFoundException e) {
            return showDownloadDialog(activity, stringTitle, stringMessage, stringButtonYes,
                    stringButtonNo);
        }
    }

    private static AlertDialog showDownloadDialog(final Activity activity,
            CharSequence stringTitle,
            CharSequence stringMessage,
            CharSequence stringButtonYes,
            CharSequence stringButtonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(stringTitle);
        downloadDialog.setMessage(stringMessage);
        downloadDialog.setPositiveButton(stringButtonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:org.torproject.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        });
        downloadDialog.setNegativeButton(stringButtonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }
    
    

}
