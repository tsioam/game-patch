package app.cloudgame.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.text.TextUtils;

import com.tencent.mmkv.MMKV;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class Configuration {

    public static final String ENHANCE_PC = "enhance_pc";
    public static final String V_CONSOLE = "inject_console";
    private static final String BRIDGE_INJECT = "init_bridge";
    public static final String USER_AGENT = "user_agent";
    public static final String LAUNCH_URL = "launch_url";
    public static final String FORCE_DISABLE_USER_GUIDE = "force_disable_guide";
    public static final String CHECK_UPDATE_ON_LAUNCH = "check_update_on_launch";

    public static final String YS_DOMAIN = "ys.mihoyo.com";
    public static final String DEFAULT_URL = "https://ys.mihoyo.com/cloud/?utm_source=default#/";

    private static volatile Configuration sConfiguration;

    private String[] mPageStartScripts;
    private final Map<String,String> mScriptMap = new HashMap<>();

    private final MMKV kv;

    public static Configuration getConfiguration() {
        if (sConfiguration == null) {
            synchronized (Configuration.class) {
                if (sConfiguration == null) {
                    sConfiguration = new Configuration();
                }
            }
        }
        return sConfiguration;
    }

    private Configuration() {
        kv = MMKV.defaultMMKV();
    }

    public void preloadPageStartScripts(Context context) {
        mScriptMap.put(Configuration.V_CONSOLE, readAssetFileAsString(context, "vconsole.js"));
        mScriptMap.put(Configuration.BRIDGE_INJECT, readAssetFileAsString(context, "inject.js"));
        mScriptMap.put(Configuration.FORCE_DISABLE_USER_GUIDE, readAssetFileAsString(context, "disableGuide.js"));
        mScriptMap.put(Configuration.ENHANCE_PC, readAssetFileAsString(context, "enhancePC.js"));
        mPageStartScripts = loadPageStartScripts();
    }

    public String[] getPageStartScripts() {
        return mPageStartScripts;
    }

    public void commitConfig() {
        mPageStartScripts = loadPageStartScripts();
    }

    private String[] loadPageStartScripts() {
        HashSet<String> scripts = new HashSet<>();
        if (readBooleanValue(Configuration.V_CONSOLE)) {
            scripts.add(mScriptMap.get(Configuration.V_CONSOLE));
        }
        scripts.add(mScriptMap.get(Configuration.ENHANCE_PC));
        scripts.add(mScriptMap.get(Configuration.BRIDGE_INJECT));
        return scripts.toArray(new String[0]);
    }

    public static String readAssetFileAsString(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        StringBuilder stringBuilder = new StringBuilder();

        try (InputStream inputStream = assetManager.open(fileName);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public String getStringValue(String key) {
        return kv.decodeString(key);
    }

    public String getStringValue(String key, String def) {
        return kv.decodeString(key, def);
    }

    public boolean readBooleanValue(String key, boolean def) {
        return kv.getBoolean(key, def);
    }

    public boolean readBooleanValue(String key) {
        return kv.decodeBool(key, false);
    }

    public void setBooleanValue(String key, boolean value) {
        kv.encode(key, value);
    }

    public void setStringValue(String key, String value) {
        kv.encode(key, value);
    }

    public String getUserAgent() {
        String value = kv.decodeString(USER_AGENT);
        if (TextUtils.isEmpty(value)) {
            return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36";
        }
        return value;
    }

    public String getScript(String key) {
        return mScriptMap.get(key);
    }

    public int getMouseSpeedLevel() {
        return kv.decodeInt("mouse_speed", 4);
    }

    public void setMouseSpeedLevel(int level) {
        kv.encode("mouse_speed", level);
    }

}
