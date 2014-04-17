package info.guardianproject.onionkit.web.proxySetters;

import android.os.Build;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ICSProxySetter implements ProxySetter {

    private final String host;
    private final int port;

    public ICSProxySetter(String host, int port) {

        this.host = host;
        this.port = port;
    }

    public boolean setProxy()
    {

        // PSIPHON: added support for Android 4.x WebView proxy
        try
        {
            Class webViewCoreClass = Class.forName("android.webkit.WebViewCore");

            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            if (webViewCoreClass != null && proxyPropertiesClass != null)
            {
                Method m = webViewCoreClass.getDeclaredMethod("sendStaticMessage", Integer.TYPE,
                        Object.class);
                Constructor c = proxyPropertiesClass.getConstructor(String.class, Integer.TYPE,
                        String.class);

                if (m != null && c != null)
                {
                    m.setAccessible(true);
                    c.setAccessible(true);
                    Object properties = c.newInstance(host, port, null);

                    // android.webkit.WebViewCore.EventHub.PROXY_CHANGED = 193;
                    m.invoke(null, 193, properties);


                    return true;
                }


            }
        } catch (Exception e)
        {
            Log.e("ProxySettings",
                    "Exception setting WebKit proxy through android.net.ProxyProperties: "
                            + e.toString()
            );
        } catch (Error e)
        {
            Log.e("ProxySettings",
                    "Exception setting WebKit proxy through android.webkit.Network: "
                            + e.toString());
        }

        return false;

    }

    public boolean resetProxy() throws Exception {
        try
        {
            Class webViewCoreClass = Class.forName("android.webkit.WebViewCore");
            Class proxyPropertiesClass = Class.forName("android.net.ProxyProperties");
            if (webViewCoreClass != null && proxyPropertiesClass != null)
            {
                Method m = webViewCoreClass.getDeclaredMethod("sendStaticMessage", Integer.TYPE,
                        Object.class);

                if (m != null)
                {
                    m.setAccessible(true);

                    // android.webkit.WebViewCore.EventHub.PROXY_CHANGED = 193;
                    m.invoke(null, 193, null);
                }
            }
            return true;
        } catch (Exception e)
        {
            Log.e("ProxySettings",
                    "Exception setting WebKit proxy through android.net.ProxyProperties: "
                            + e.toString());
            throw e;
        } catch (Error e)
        {
            Log.e("ProxySettings",
                    "Exception setting WebKit proxy through android.webkit.Network: "
                            + e.toString());
            throw e;
        }
    }

    public boolean canApply() {
        return Build.VERSION.SDK_INT < 19;
    }
}
