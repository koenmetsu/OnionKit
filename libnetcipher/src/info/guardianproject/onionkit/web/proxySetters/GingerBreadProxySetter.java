package info.guardianproject.onionkit.web.proxySetters;

import android.content.Context;
import android.os.Build;
import org.apache.http.HttpHost;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GingerBreadProxySetter {

    private final Context ctx;
    private final String host;
    private final int port;

    public GingerBreadProxySetter(Context ctx, String host, int port) {

        this.ctx = ctx;
        this.host = host;
        this.port = port;
    }

    public boolean setProxy() throws Exception {

        Object requestQueueObject = getRequestQueue(ctx);
        if (requestQueueObject != null) {
            // Create Proxy config object and set it into request Q
            HttpHost httpHost = new HttpHost(host, port, "http");
            setDeclaredField(requestQueueObject, "mProxyHost", httpHost);
            return true;
        }
        return false;

    }

    public boolean canApply(){
        return Build.VERSION.SDK_INT < 14;
    }

    private Object getRequestQueue(Context ctx) throws Exception {
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

    private Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        // System.out.println(obj.getClass().getName() + "." + name + " = "+
        // out);
        return out;
    }

    private void setDeclaredField(Object obj, String name, Object value)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private Object invokeMethod(Object object, String methodName, Object[] params,
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
}
