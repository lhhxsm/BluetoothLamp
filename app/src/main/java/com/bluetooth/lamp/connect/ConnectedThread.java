package com.bluetooth.lamp.connect;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 作者:lhh
 * 描述:
 * 时间:2017/11/15.
 */
public class ConnectedThread extends Thread {
  private final BluetoothSocket mSocket;
  private final InputStream mInputStream;
  private final OutputStream mOutputStream;
  private final Handler mHandler;

  public ConnectedThread(BluetoothSocket socket, Handler handler) {
    this.mSocket = socket;
    this.mHandler = handler;
    InputStream tempIn = null;
    OutputStream tempOut = null;

    try {
      tempIn = socket.getInputStream();
      tempOut = socket.getOutputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }
    mInputStream = tempIn;
    mOutputStream = tempOut;
  }

  @Override public void run() {
    super.run();
    byte[] buffer = new byte[1024];
    int bytes;
    //持续监听InputStream，直到发生异常
    while (true) {
      try {
        bytes = mInputStream.read(buffer);
        //将获得的字节发送到UI
        if (bytes > 0) {
          //String content = new String(buffer, 0, bytes, "utf-8");
          Message message = mHandler.obtainMessage(Constant.MSG_GOT_DATA, buffer);
          mHandler.sendMessage(message);
        }
        Log.e("Bluetooth", "message size " + bytes);
      } catch (IOException e) {
        e.printStackTrace();
        mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
        break;
      }
    }
  }

  /**
   * 将数据发送到远程设备
   */
  public void write(byte[] data) {
    try {
      mOutputStream.write(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 关闭连接
   */
  public void cancel() {
    try {
      mSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
