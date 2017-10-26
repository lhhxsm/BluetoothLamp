package com.bluetooth.lamp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * 作者:lhh
 * 描述:
 * 时间:2017/10/23.
 */
public class DeviceAdapter extends BaseAdapter {
  private Context mContext;
  private List<BluetoothDevice> mData;

  public DeviceAdapter(Context context, List<BluetoothDevice> data) {
    mContext = context.getApplicationContext();
    mData = data;
  }

  @Override public int getCount() {
    return mData != null ? mData.size() : 0;
  }

  @Override public BluetoothDevice getItem(int position) {
    return mData.get(position);
  }

  @Override public long getItemId(int position) {
    return position;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    //复用View，优化性能
    if (convertView == null) {
      convertView =
          LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
    }

    TextView line1 = (TextView) convertView.findViewById(android.R.id.text1);
    TextView line2 = (TextView) convertView.findViewById(android.R.id.text2);

    //获取对应的蓝牙设备
    BluetoothDevice device = getItem(position);

    //显示名称
    line1.setText(device.getName());
    //显示地址
    line2.setText(device.getAddress());

    return convertView;
  }

  public void refresh(List<BluetoothDevice> data) {
    mData = data;
    notifyDataSetChanged();
  }
}

