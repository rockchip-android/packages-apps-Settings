package com.android.settings.display;

import android.hardware.fingerprint.IFingerprintDaemon;
import  android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Drm Display Setting.
 */

public class DrmDisplaySetting {

    private final static boolean DEBUG = true;

    private final static String TAG = "TvSettings";

    private final static String SUB_TAG = "DrmDisplaySetting";


    private final static String SYS_NODE_PARAM_STATUS_OFF = "off";

    private final static String SYS_NODE_PARAM_STATUS_ON = "detect";

    private final static String SYS_NODE_STATUS_CONNECTED = "connected";

    private final static String SYS_NODE_STATUS_DISCONNECTED = "disconnected";

    public final static int DISPLAY_TYPE_HDMI = 0;
    public final static int DISPLAY_TYPE_DP = 1;


    private static void logd(String text) {
        Log.d(TAG, SUB_TAG + " - " + text);
    }

    public static List<DisplayInfo> getDisplayInfoList() {
        List<DisplayInfo> displayInfoList = new ArrayList<>();
        DisplayInfo hdmiDisplayInfo = getHdmiDisplayInfo();
        if (hdmiDisplayInfo != null) {
            displayInfoList.add(hdmiDisplayInfo);
        }
        DisplayInfo dpDisplayInfo = getDpDisplayInfo();
        if (dpDisplayInfo != null) {
            displayInfoList.add(dpDisplayInfo);
        }
        return displayInfoList;
    }

    public static List<String> getDisplayModes(DisplayInfo di) {
        List<String> res = null;
        res = getHdmiModes();
        return res;
    }

    public static String getCurDisplayMode(DisplayInfo di) {
        if (di.getDisplayId() == DISPLAY_TYPE_HDMI) {
            return getCurHdmiMode();
        } else if (di.getDisplayId() == DISPLAY_TYPE_DP){
            return getCurDpMode();
        }
        return null;
    }

    public static String getCurHdmiMode() {
//        return curSetHdmiMode;
        return getHdmiMode();
    }

    public static String getCurDpMode() {
//        return curSetDpMode;
        return getDpMode();
    }

    public static boolean setDisplayModeTemp(DisplayInfo di, int index) {
        List<String> modes = getDisplayModes(di);
        if(modes != null && modes.size() > 0 && index >= 0 && index < modes.size()){
            String mode = modes.get(index);
            setDisplayModeTemp(di, mode);
            return true;
        }
        return false;
    }

    public static void setDisplayModeTemp(DisplayInfo di, String mode) {
        setHdmiModeTemp(mode);
    }

    public static void confirmSaveDisplayMode(DisplayInfo di, boolean isSave) {
        if (di == null) {
            return;
        }
        if (di.getDisplayId() == DISPLAY_TYPE_HDMI) {
            confirmSaveHdmiMode(isSave);
        } else if (di.getDisplayId() == DISPLAY_TYPE_DP) {
            confirmSaveDpMode(isSave);
        }
    }

    /**
     * ==================================================================================
     *                               HDMI Setting
     * ==================================================================================
     */

    private final static String SYS_NODE_HDMI_MODES =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/modes";

    private final static String SYS_NODE_HDMI_MODE =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/mode";

    private final static String SYS_NODE_HDMI_STATUS =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/status";

    private final static String PROP_RESOLUTION_HDMI = "persist.sys.resolution.aux";

    private static String tmpSetHdmiMode = null;
    private static String curSetHdmiMode = "1920x1080p60";

    public static DisplayInfo getHdmiDisplayInfo() {
        if (SYS_NODE_STATUS_CONNECTED.equals(getHdmiStatus())) {
            DisplayInfo di = new DisplayInfo();
            List<String> hdmiResoList = getHdmiModes();
            String[] hdmiResoStrs = hdmiResoList.toArray(new String[hdmiResoList.size()]);
            di.setModes(hdmiResoStrs);
            di.setDescription("HDMI");
            di.setDisplayId(DISPLAY_TYPE_HDMI);
            return di;
        }
        return null;
    }

    private static List<String> getHdmiModes() {
        List<String> res = null;
        try {
            res = readStrListFromFile(SYS_NODE_HDMI_MODES);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processModeStr(res);
    }

    private static String getHdmiStatus() {
        String status = null;
        try {
            status = readStrFromFile(SYS_NODE_HDMI_STATUS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    private static String getHdmiMode() {
        String mode = null;
        try {
            mode = readStrFromFile(SYS_NODE_HDMI_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mode;
    }

    private static void setHdmiModeTemp(String mode) {
        setHdmiMode(mode);
        tmpSetHdmiMode = mode;
    }

    private static void confirmSaveHdmiMode(boolean isSave) {
        if (tmpSetHdmiMode == null) {
            return;
        }
        if (isSave) {
            curSetHdmiMode = tmpSetHdmiMode;
        } else {
            setHdmiMode(curSetHdmiMode);
            tmpSetHdmiMode = null;
        }
    }

    private static void setHdmiMode(String mode) {
        SystemProperties.set(PROP_RESOLUTION_HDMI, mode);
    }

    /**
     * ==================================================================================
     *                               DP Setting
     * ==================================================================================
     */

    private final static String SYS_NODE_DP_MODES =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-DP-1/modes";

    private final static String SYS_NODE_DP_MODE =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-DP-1/mode";

    private final static String SYS_NODE_DP_STATUS =
            "/sys/devices/platform/display-subsystem/drm/card0/card0-DP-1/status";

    private final static String PROP_RESOLUTION_DP = "persist.sys.resolution.aux";

    private static String tmpSetDpMode = null;
    private static String curSetDpMode = "1920x1080p60";

    public static DisplayInfo getDpDisplayInfo() {
        if (SYS_NODE_STATUS_CONNECTED.equals(getDpStatus())) {
            DisplayInfo di = new DisplayInfo();
            List<String> dpResoList = getDpModes();
            String[] dpResoStrs = dpResoList.toArray(new String[dpResoList.size()]);
            di.setModes(dpResoStrs);
            di.setDescription("DP");
            di.setDisplayId(DISPLAY_TYPE_DP);
            return di;
        }
        return null;
    }

    private static List<String> getDpModes() {
        List<String> res = null;
        try {
            res = readStrListFromFile(SYS_NODE_DP_MODES);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processModeStr(res);
    }

    private static String getDpStatus() {
        String status = null;
        try {
            status = readStrFromFile(SYS_NODE_DP_STATUS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

    private static String getDpMode() {
        String mode = null;
        try {
            mode = readStrFromFile(SYS_NODE_DP_MODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mode;
    }

    private static void setDpModeTemp(String reso) {
        setDpMode(reso);
        tmpSetDpMode = reso;
    }

    private static void confirmSaveDpMode(boolean isSave) {
        if (tmpSetDpMode == null) {
            return;
        }
        if (isSave) {
            curSetDpMode = tmpSetDpMode;
        } else {
            setDpMode(curSetDpMode);
            tmpSetDpMode = null;
        }
    }

    private static void setDpMode(String reso) {
        SystemProperties.set(PROP_RESOLUTION_DP, reso);
    }

    /**
     * ==================================================================================
     *                               Common
     * ==================================================================================
     */
    private static final String[] COMMON_RESOLUTION = {
            "3840x2160",
            "1920x1080",
            "1280x720",
            "800x600",
            "640x480"
    };

    private static List<String> processModeStr(List<String> resoStrList) {
        if (resoStrList == null) {
            return null;
        }
        List<String> processedResoStrList = new ArrayList<>();
        List<String> tmpResoStrList = new ArrayList<>();
        for (String reso : resoStrList) {
            if (reso.contains("p") || reso.contains("i")) {
                boolean hasRepeat = false;
                for (String s : tmpResoStrList) {
                    if (s.equals(reso)) {
                        hasRepeat = true;
                        break;
                    }
                }
                if (!hasRepeat) {
                    tmpResoStrList.add(reso);
                }
            }
        }
        return tmpResoStrList;
    }

    private static List<String> readStrListFromFile(String pathname) throws IOException {
        List<String> fileStrings = new ArrayList<>();
        File filename = new File(pathname);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            fileStrings.add(line);
        }
        logd("readStrListFromFile - " + fileStrings.toString());
        return fileStrings;
    }

    private static String readStrFromFile(String filename) throws IOException {
        logd("readStrFromFile - " + filename);
        File f = new File(filename);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(f));
        BufferedReader br = new BufferedReader(reader);
        String line = br.readLine();
        logd("readStrFromFile - " + line);
        return line;
    }
}
