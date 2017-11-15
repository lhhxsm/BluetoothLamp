package com.bluetooth.lamp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bluetooth.lamp.connect.Constant;
import com.bluetooth.lamp.controller.BluetoothController;
import com.bluetooth.lamp.controller.ChatController;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
    implements View.OnClickListener, AdapterView.OnItemClickListener {
  private static final int REQUEST_CODE = 483;
  //private ProgressDialog mLoadingDialog;
  private Toast mToast;
  private ListView mListView;//蓝牙设备列表
  private RelativeLayout mChatPanel;//聊天面板
  private Button mBtnSend;//聊天按钮
  private EditText mEtInput;//聊天输入框
  private TextView mTvContent;//聊天对话框

  private List<BluetoothDevice> mDeviceList = new ArrayList<>();
  private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();
  private BluetoothController mController = new BluetoothController();
  private DeviceAdapter mAdapter;
  private StringBuilder mChatText = new StringBuilder();
  private Handler mHandler = new MyHandler();
  private boolean isStartChat;
  private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {//蓝牙扫描过程开始
        showToast(" 蓝牙扫描过程开始");
        Log.e("tag", "start");
        //初始化数据列表
        mDeviceList.clear();
        mAdapter.refresh(mDeviceList);
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {//蓝牙扫描过程结束
        showToast("蓝牙扫描过程结束");
        Log.e("tag", "stop");
      } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {//发现蓝牙
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        Log.e("tag", "device " + device.getName());
        //找到一个，添加一个
        mDeviceList.add(device);
        mAdapter.refresh(mDeviceList);
      } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {//蓝牙扫描状态(SCAN_MODE)发生改变
        int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, 0);
        if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
          showToast(" 蓝牙开启扫描");
        } else {
          showToast("蓝牙关闭扫描");
        }
        //SCAN_MODE_CONNECTABLE 表明该蓝牙可以扫描其他蓝牙设备
        //SCAN_MODE_CONNECTABLE_DISCOVERABLE 表明该蓝牙设备同时可以扫码其他蓝牙设备，并且可以被其他蓝牙设备扫描到。
        //SCAN_MODE_NONE 蓝牙不能扫描以及被扫描。
      } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {//一个远程设备的连接状态的改变
        BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (remoteDevice == null) {
          showToast("no device");
          return;
        }
        int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
        if (status == BluetoothDevice.BOND_BONDED) {//表明蓝牙已经绑定
          showToast("Bonded " + remoteDevice.getName());
        } else if (status == BluetoothDevice.BOND_BONDING) {// 表明蓝牙正在绑定过程中
          showToast("Bonding " + remoteDevice.getName());
        } else if (status == BluetoothDevice.BOND_NONE) {//表明没有绑定
          showToast("Not bond " + remoteDevice.getName());
        }
      }
    }
  };

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mListView = (ListView) findViewById(R.id.device_list);
    mChatPanel = (RelativeLayout) findViewById(R.id.chat_panel);
    mBtnSend = (Button) findViewById(R.id.btn_send);
    mEtInput = (EditText) findViewById(R.id.chat_edit);
    mTvContent = (TextView) findViewById(R.id.chat_content);
    mBtnSend.setOnClickListener(this);
    mListView.setOnItemClickListener(this);
    mAdapter = new DeviceAdapter(this);
    mListView.setAdapter(mAdapter);
    mAdapter.refresh(mDeviceList);
    registerBluetoothReceiver();
    mController.turnOnBluetooth(this, REQUEST_CODE);
  }

  private void registerBluetoothReceiver() {
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
  }

  //private boolean showLoading(String message) {
  //  if (mLoadingDialog == null) {
  //    mLoadingDialog = new ProgressDialog(this);
  //    mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
  //    mLoadingDialog.setCancelable(false);// 设置是否可以通过点击Back键取消
  //    mLoadingDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
  //    mLoadingDialog.setMessage(message);
  //    mLoadingDialog.show();
  //    return true;
  //  } else {
  //    mLoadingDialog.setMessage(message);
  //    mLoadingDialog.show();
  //    return false;
  //  }
  //}
  //
  //private void dismissLoading(final String message) {
  //  if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
  //    mLoadingDialog.dismiss();
  //    mLoadingDialog = null;
  //    if (!TextUtils.isEmpty(message)) {
  //      runOnUiThread(new Runnable() {
  //        @Override public void run() {
  //          showToast(message);
  //        }
  //      });
  //    }
  //  }
  //}

  private void showToast(String message) {
    if (mToast == null) {
      mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
    } else {
      mToast.setText(message);
    }
    mToast.show();
  }

  @Override public void onClick(View v) {
    String input = mEtInput.getText().toString().trim();
    if (TextUtils.isEmpty(input)) {
      showToast("输入内容不能为空");
      return;
    }
    ChatController.getInstance().sendMessage(input);
    mChatText.append(input).append("\n");
    mTvContent.setText(mChatText.toString());
    mEtInput.setText("");
  }

  @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (isStartChat) {
      BluetoothDevice device = mBondedDeviceList.get(position);
      ChatController.getInstance().connectServer(device, mController.getAdapter(), mHandler);
    } else {
      BluetoothDevice device = mDeviceList.get(position);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        device.createBond();
      }
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ChatController.getInstance().stopChat();
    unregisterReceiver(mReceiver);
  }

  private void enterChatMode() {
    mListView.setVisibility(View.GONE);
    mChatPanel.setVisibility(View.VISIBLE);
  }

  private void exitChatMode() {
    mListView.setVisibility(View.VISIBLE);
    mChatPanel.setVisibility(View.GONE);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE) {
      if (resultCode != RESULT_OK) {
        finish();
      }
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.enable_visibility) {
      mController.enableVisibility(this);
    } else if (id == R.id.find_device) {
      //查找设备
      mAdapter.refresh(mDeviceList);
      mController.findTheDevice();
      isStartChat = false;
      //mListView.setOnItemClickListener(bindDeviceClick);
    } else if (id == R.id.bonded_device) {
      //查看已绑定设备
      mBondedDeviceList = mController.getBondedDevices();
      mAdapter.refresh(mBondedDeviceList);
      isStartChat = true;
      //mListView.setOnItemClickListener(bindedDeviceClick);
    } else if (id == R.id.listening) {
      ChatController.getInstance().waitingClientToConnect(mController.getAdapter(), mHandler);
    } else if (id == R.id.stop_listening) {
      ChatController.getInstance().stopChat();
      exitChatMode();
    } else if (id == R.id.disconnect) {
      exitChatMode();
    }
    return super.onOptionsItemSelected(item);
  }

  private class MyHandler extends Handler {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case Constant.MSG_START_LISTENING:
          showToast("开启聊天");
          break;
        case Constant.MSG_FINISH_LISTENING:
          showToast("关闭聊天");
          exitChatMode();
          break;
        case Constant.MSG_GOT_DATA:
          byte[] data = (byte[]) msg.obj;
          mChatText.append(ChatController.getInstance().decodeMessage(data)).append("\n");
          mTvContent.setText(mChatText.toString());
          break;
        case Constant.MSG_ERROR:
          exitChatMode();
          showToast("error: " + String.valueOf(msg.obj));
          break;
        case Constant.MSG_CONNECTED_TO_SERVER:
          enterChatMode();
          showToast("Connected to Server");
          break;
        case Constant.MSG_GOT_A_CLINET:
          enterChatMode();
          showToast("Got a Client");
          break;
      }
    }
  }
}
