package com.bluetooth.lamp.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import java.io.IOException;
import java.util.UUID;

/**
 * 作者:lhh
 * 描述:服务端
 * 时间:2017/10/23.
 */
public class AcceptThread extends Thread {
  private static final String NAME = "Bluetooth";
  private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);
  private final BluetoothServerSocket mServerSocket;
  private final BluetoothAdapter mAdapter;
  private final Handler mHandler;
  private ConnectedThread mConnectedThread;

  public AcceptThread(BluetoothAdapter adapter, Handler handler) {
    this.mAdapter = adapter;
    this.mHandler = handler;
    BluetoothServerSocket temp = null;
    try {
      //MY_UUID是应用程序的UUID字符串，也被客户端代码使用
      temp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
    } catch (IOException e) {
      e.printStackTrace();
    }
    mServerSocket = temp;
  }

  @Override public void run() {
    super.run();
    BluetoothSocket socket = null;
    //继续监听，直到发生异常或返回Socket
    while (true) {
      try {
        mHandler.sendEmptyMessage(Constant.MSG_START_LISTENING);
        socket = mServerSocket.accept();
      } catch (IOException e) {
        e.printStackTrace();
        mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
        break;
      }
      //如果连接被接受
      if (socket != null) {
        //在一个单独的线程进行管理
        manageConnectedSocket(socket);
        try {
          mServerSocket.close();
          mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
        } catch (IOException e) {
          e.printStackTrace();
        }
        break;
      }
    }
  }

  private void manageConnectedSocket(BluetoothSocket socket) {
    //只支持同时处理一个连接
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
    }
    mHandler.sendEmptyMessage(Constant.MSG_GOT_A_CLINET);
    mConnectedThread = new ConnectedThread(socket, mHandler);
    mConnectedThread.start();
  }

  /**
   * 取消Socket监听，线程结束
   */
  public void cancel() {
    try {
      mServerSocket.close();
      mHandler.sendEmptyMessage(Constant.MSG_FINISH_LISTENING);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void sendData(byte[] data) {
    if (mConnectedThread != null) {
      mConnectedThread.write(data);
    }
  }
}
