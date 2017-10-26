package com.bluetooth.lamp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者:lhh
 * 描述:蓝牙适配器
 * 时间:2017/10/23.
 */
public class BluetoothController {

  private BluetoothAdapter mAdapter;

  public BluetoothController() {
    mAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  /**
   * 设备是否支持蓝牙
   */
  public boolean isSupportBluetooth() {
    return mAdapter != null;
  }

  /**
   * 判断当前蓝牙状态
   *
   * @return true 打开, false 关闭
   */
  public boolean getBluetoothStatus() {
    return mAdapter != null && mAdapter.isEnabled();
  }

  /**
   * 打开蓝牙
   */
  public void turnOnBluetooth(Activity activity, int requestCode) {
    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    activity.startActivityForResult(intent, requestCode);
    //mAdapter.enable();不推荐使用这种方式打开蓝牙
  }

  /**
   * 关闭蓝牙
   */
  public void turnOffBluetooth() {
    mAdapter.disable();
  }

  /**
   * 打开蓝牙可见性
   */
  public void enableVisibility(Context context) {
    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
    context.startActivity(intent);
  }

  /**
   * 查找设备
   */
  public void findDevice() {
    if (mAdapter == null) return;
    mAdapter.startDiscovery();
  }

  /**
   * 获取绑定设备
   */
  public List<BluetoothDevice> getBondedDeviceList() {
    return new ArrayList<>(mAdapter.getBondedDevices());
  }
}
