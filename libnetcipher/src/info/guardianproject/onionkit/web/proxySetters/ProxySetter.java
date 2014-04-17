package info.guardianproject.onionkit.web.proxySetters;

import android.annotation.TargetApi;

public interface ProxySetter {
    boolean canApply();

    boolean setProxy() throws Exception;

    @TargetApi(19)
    boolean resetProxy() throws Exception;
}
