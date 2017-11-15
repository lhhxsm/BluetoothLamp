package com.bluetooth.lamp.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import java.io.IOException;
import java.util.UUID;

/**
 * 作者:lhh
 * 描述:客户端
 * 时间:2017/11/15.
 */
public class ConnectThread extends Thread {
  private static final UUID MY_UUID = UUID.fromString(Constant.CONNECTION_UUID);
  private final BluetoothSocket mSocket;
  private final BluetoothDevice mDevice;
  private final Handler mHandler;
  private BluetoothAdapter mAdapter;
  private ConnectedThread mConnectedThread;

  public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
    this.mDevice = device;
    this.mAdapter = adapter;
    this.mHandler = handler;
    BluetoothSocket temp = null;
    //获取BluetoothSocket与给定的BluetoothDevice连接
    try {
      temp = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      e.printStackTrace();
    }
    mSocket = temp;
  }

  @Override public void run() {
    super.run();
    //取消查找蓝牙,这个会影响蓝牙之间的连接，传输效率
    mAdapter.cancelDiscovery();
    try {
      mSocket.connect();
    } catch (IOException e) {
      e.printStackTrace();
      mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
      try {
        mSocket.close();
      } catch (IOException e1) {
        e1.printStackTrace();
      }
      return;
    }
    manageConnectedSocket(mSocket);
  }

  private void manageConnectedSocket(BluetoothSocket socket) {
    mHandler.sendEmptyMessage(Constant.MSG_CONNECTED_TO_SERVER);
    mConnectedThread = new ConnectedThread(socket, mHandler);
    mConnectedThread.start();
  }

  /**
   * 将取消正在进行的连接，并关闭 Socket
   */
  public void cancel() {
    try {
      mSocket.close();
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
