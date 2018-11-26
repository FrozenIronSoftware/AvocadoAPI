package com.frozenironsoftware.avocado.cacher.util;

import com.frozenironsoftware.avocado.cacher.data.Constants;
import com.goebl.david.Webb;

public class WebbUtil {
    public static Webb getWebb() {
        Webb webb = Webb.create();
        webb.setDefaultHeader("User-Agent", String.format("Avocado/%s (Java/%s)",
                Constants.VERSION, System.getProperty("java.version")));
        return webb;
    }
}
