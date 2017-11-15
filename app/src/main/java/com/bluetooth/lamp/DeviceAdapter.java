package com.bluetooth.lamp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者:lhh
 * 描述:
 * 时间:2017/10/23.
 */
public class DeviceAdapter extends BaseAdapter {
  private Context mContext;
  private List<BluetoothDevice> mData;

  public DeviceAdapter(Context context) {
    this.mContext = context.getApplicationContext();
    this.mData = new ArrayList<>();
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
    ViewHolder holder;
    if (convertView == null) {
      holder = new ViewHolder();
      convertView =
          LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
      holder.mTvName = (TextView) convertView.findViewById(android.R.id.text1);
      holder.mTvAddress = (TextView) convertView.findViewById(android.R.id.text2);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }
    //获取对应的蓝牙设备
    BluetoothDevice device = getItem(position);
    //显示名称
    holder.mTvName.setText(device.getName());
    //显示地址
    holder.mTvAddress.setText(device.getAddress());
    holder.mTvName.setTextColor(Color.BLACK);
    holder.mTvAddress.setTextColor(Color.BLACK);
    return convertView;
  }

  public void refresh(List<BluetoothDevice> data) {
    if (mData != null && mData.size() > 0) {
      mData.clear();
    }
    if (data != null && data.size() > 0) {
      mData.addAll(data);
    }
    notifyDataSetChanged();
  }

  private class ViewHolder {
    private TextView mTvName;
    private TextView mTvAddress;
  }
}

