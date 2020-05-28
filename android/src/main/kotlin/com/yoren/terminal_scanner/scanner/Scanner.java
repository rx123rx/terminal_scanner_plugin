package com.yoren.terminal_scanner.scanner;

import android.text.TextUtils;

import android_serialport_api.DriverListener;
import android_serialport_api.SerialHelper;
import cn.lawson.terminal.serial_core.bean.ComBean;

public class Scanner {
    private static SerialControl mCom;
    private DriverListener mListener;
    private byte separateCode = 13;
    private byte[] receiveByte;
    private int receiveLen = 0;

    public Scanner(String path, String baudRate) {
        if (!TextUtils.isEmpty(path)) {
            if (mCom == null) {
                mCom = new SerialControl();
            }

            mCom.setPort(path);
            mCom.setBaudRate(baudRate);
            receiveLen = 0;
            try {
                mCom.open();
            } catch (Exception var5) {
                var5.printStackTrace();
            }
        }
    }

    public void close() {
        receiveLen = 0;
        if (mCom != null) {
            mCom.stopSend();
            mCom.close();
        }

        mCom = null;
        this.mListener = null;
    }

    public void setListener(DriverListener listener) {
        this.mListener = listener;
    }

    public void setSeparateCode(byte code) {
        this.separateCode = code;
    }

    private class SerialControl extends SerialHelper {

        @Override
        protected void onDataReceived(ComBean comBean) {
            try {
                int m_iRecValue = -1;
                int size = comBean.bRec.length;
                if (size > 0) {
                    int end = comBean.bRec[size - 1];
                    if (receiveLen == 0) {
                        receiveLen = receiveLen + comBean.bRec.length;
                        receiveByte = new byte[receiveLen];
                        System.arraycopy(comBean.bRec, 0, receiveByte, 0, receiveLen);
                    } else {
                        byte[] oldbytes = new byte[receiveLen];
                        System.arraycopy(receiveByte, 0, oldbytes, 0, receiveLen);
                        receiveLen = receiveLen + comBean.bRec.length;
                        receiveByte = new byte[receiveLen];
                        System.arraycopy(oldbytes, 0, receiveByte, 0, oldbytes.length);
                        System.arraycopy(comBean.bRec, 0, receiveByte, oldbytes.length, comBean.bRec.length);
                    }
                    if (end == separateCode) {
                        if (mListener != null) {
                            mListener.deviceStateMessage(327682, m_iRecValue, receiveLen, receiveByte);
                        }

                        receiveLen = 0;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
