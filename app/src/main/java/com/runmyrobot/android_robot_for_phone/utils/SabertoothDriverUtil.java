package com.runmyrobot.android_robot_for_phone.utils;

/**
 * Created by Brendon on 9/7/2018.
 */
public class SabertoothDriverUtil {
    public static final byte STOP_BYTE = 0;

    public SabertoothDriverUtil() {
    }

    public static byte getDriveSpeed(byte driveSpeed, int motorNum) {
        switch(motorNum) {
            case 0:
                return (byte)((int) ValueUtil.map((float)driveSpeed, -128.0F, 127.0F, 1.0F, 127.0F, 1.0F));
            case 1:
                return (byte)((int) ValueUtil.map((float)driveSpeed, -128.0F, 127.0F, -128.0F, -1.0F, 1.0F));
            default:
                return (byte)((int) ValueUtil.map(0.0F, -128.0F, 127.0F, 1.0F, 127.0F, 1.0F));
        }
    }
}
