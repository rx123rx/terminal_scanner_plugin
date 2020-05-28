package com.yoren.terminal_scanner.util;

import android.content.Context;
import android.content.res.Configuration;
import android.view.InputDevice;
import android.view.KeyEvent;

public class ScannerUtils {
    public static boolean isInputFromScanner(Context context, KeyEvent event) {
        if (event == null) return false;
        if (event.getDevice() == null) {
            return false;
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            return false;
        }
        if (event.getDevice().getSources() == (InputDevice.SOURCE_KEYBOARD | InputDevice.SOURCE_DPAD |
            InputDevice.SOURCE_CLASS_BUTTON)) {
            // 虚拟按键返回false
            return false;
        }
        Configuration cfg = context.getResources().getConfiguration();
        return cfg.keyboard != Configuration.KEYBOARD_UNDEFINED;
    }
}
