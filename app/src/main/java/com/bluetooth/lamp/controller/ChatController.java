package com.bluetooth.lamp.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import com.bluetooth.lamp.connect.AcceptThread;
import com.bluetooth.lamp.connect.ConnectThread;
import com.bluetooth.lamp.connect.ProtocolHandler;
import java.io.UnsupportedEncodingException;

/**
 * 作者:lhh
 * 描述:聊天的业务逻辑
 * 时间:2017/11/15.
 */
public class ChatController {
  private ConnectThread mConnectThread;//客服端
  private AcceptThread mAcceptThread;//服务端
  /**
   * 协议处理
   */
  private ChatProtocol mProtocol = new ChatProtocol();

  public static ChatController getInstance() {
    return ChatControlHolder.mInstance;
  }

  /**
   * 连接服务端(与服务器连接进行聊天)
   */
  public void connectServer(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
    mConnectThread = new ConnectThread(device, adapter, handler);
    mConnectThread.start();
  }

  /**
   * 等待客户端来连接
   */
  public void waitingClientToConnect(BluetoothAdapter adapter, Handler handler) {
    mAcceptThread = new AcceptThread(adapter, handler);
    mAcceptThread.start();
  }

  /**
   * 发出消息
   */
  public void sendMessage(String message) {
    byte[] data = mProtocol.encodePackage(message);
    if (mConnectThread != null) {//发给客户端
      mConnectThread.sendData(data);
    } else if (mAcceptThread != null) {//发给服务端
      mAcceptThread.sendData(data);
    }
  }

  /**
   * 网络数据解码
   */
  public String decodeMessage(byte[] data) {
    return mProtocol.decodePackage(data);
  }

  /**
   * 停止聊天
   */
  public void stopChat() {
    if (mConnectThread != null) {
      mConnectThread.cancel();
    } else if (mAcceptThread != null) {
      mAcceptThread.cancel();
    }
  }

  /**
   * 单例
   */
  private static class ChatControlHolder {
    private static ChatController mInstance = new ChatController();
  }

  /**
   * 网络协议的处理函数
   */
  private class ChatProtocol implements ProtocolHandler<String> {
    private static final String CHARSET_NAME = "UTF-8";

    @Override public byte[] encodePackage(String data) {
      if (data == null) {
        return new byte[0];
      } else {
        try {
          return data.getBytes(CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          return new byte[0];
        }
      }
    }

    @Override public String decodePackage(byte[] data) {
      if (data == null) {
        return null;
      } else {
        try {
          return new String(data, CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
          return null;
        }
      }
    }
  }
}
