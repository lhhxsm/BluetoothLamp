package com.bluetooth.lamp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_CODE = 664;
  private BluetoothController mController;
  private Toast mToast;
  private List<BluetoothDevice> mDeviceList = new ArrayList<>();
  private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();
  private ListView mListView;
  private DeviceAdapter mAdapter;

  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      //int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
      //switch (state) {
      //  case BluetoothAdapter.STATE_OFF:
      //    showToast("STATE_OFF");
      //    break;
      //  case BluetoothAdapter.STATE_ON:
      //    showToast("STATE_ON");
      //    break;
      //  case BluetoothAdapter.STATE_TURNING_ON:
      //    showToast("STATE_TURNING_ON");
      //    break;
      //  case BluetoothAdapter.STATE_TURNING_OFF:
      //    showToast("STATE_TURNING_OFF");
      //    break;
      //  default:
      //    showToast("UNKNOW STATE");
      //    break;
      //}

      String action = intent.getAction();
      if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
        showToast("加载中...");
        //初始化数据列表
        mDeviceList.clear();
        mAdapter.notifyDataSetChanged();
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        showToast("加载完成");
      } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //找到一个，添加一个
        mDeviceList.add(device);
        mAdapter.notifyDataSetChanged();
      } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
        int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
        if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
          showToast("扫描中...");
        } else {
          showToast("扫描完成");
        }
      } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
        BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (remoteDevice == null) {
          showToast("no device");
          return;
        }
        int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
        if (status == BluetoothDevice.BOND_BONDED) {
          showToast("Bonded " + remoteDevice.getName());
        } else if (status == BluetoothDevice.BOND_BONDING) {
          showToast("Bonding " + remoteDevice.getName());
        } else if (status == BluetoothDevice.BOND_NONE) {
          showToast("Not bond " + remoteDevice.getName());
        }
      }
    }
  };
  private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
    @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      BluetoothDevice device = mDeviceList.get(position);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        device.createBond();
      } else {
        showToast("API low");
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mController = new BluetoothController();
    mListView = (ListView) findViewById(R.id.lv);
    mAdapter = new DeviceAdapter(this, mDeviceList);
    mListView.setAdapter(mAdapter);
    mListView.setOnItemClickListener(bindDeviceClick);

    //IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    //registerReceiver(mReceiver, filter);

    IntentFilter filter = new IntentFilter();
    //开始查找
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    //结束查找
    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    //查找设备
    filter.addAction(BluetoothDevice.ACTION_FOUND);
    //设备扫描模式改变
    filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
    //绑定状态
    filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

    registerReceiver(mReceiver, filter);
    mController.turnOnBluetooth(this, REQUEST_CODE);
  }

  public void isSupportBluetooth(View view) {
    boolean support = mController.isSupportBluetooth();
    showToast(support ? "支持蓝牙" : "不支持蓝牙");
  }

  public void isBluetoothEnable(View view) {
    boolean status = mController.getBluetoothStatus();
    showToast(status ? "蓝牙开启中" : "蓝牙关闭中");
  }

  public void turnOnBluetooth(View view) {
    mController.turnOnBluetooth(this, REQUEST_CODE);
  }

  public void turnOffBluetooth(View view) {
    mController.turnOffBluetooth();
  }

  public void enableVisibility(View view) {
    mController.enableVisibility(this);
  }

  public void findDevice(View view) {
    //查找设备
    mAdapter.refresh(mDeviceList);
    mController.findDevice();
    mListView.setOnItemClickListener(bindDeviceClick);
  }

  public void bondedDevice(View view) {
    //查看已绑定设备
    mBondedDeviceList = mController.getBondedDeviceList();
    mAdapter.refresh(mBondedDeviceList);
    mListView.setOnItemClickListener(null);
  }

  private void showToast(String msg) {
    if (mToast == null) {
      mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
    } else {
      mToast.setText(msg);
    }
    mToast.show();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      showToast("打开成功");
    } else {
      showToast("打开失败");
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mReceiver);
  }
}
