package com.haojiu.smartcamera;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by leehom on 2017/5/15.
 */

public class BlueTooth extends Activity {

    BluetoothAdapter mBluetoothAdapter;
    private ArrayList<Integer> rssis;
    //private LeDeviceListAdapter mLeDeviceListAdapter;

    BluetoothDevice terget_device=null;

    BluetoothGatt mBluetoothGatt=null;

    int REQUEST_ENABLE_BT=1;

    Button btn,btn_connect,btn_disconnect;
    ListView lv;

    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    List<BluetoothGattService> list_service;

    LeDeviceListAdapter mleDeviceListAdapter;
    private String smsg = "";
    private int readIdx = 0;
    private static BluetoothLeService mBluetoothLeService;
    private static BlueTooth This;
    private String status="disconnected";
    private boolean mConnected = false;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private static int target_group=3;
    private static BluetoothGattCharacteristic target_chara=null;
    private static BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothDevice device;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private Boolean isBind=false;
    private Boolean isRegist=false;
    int anTime=0 ,addTime=0,anTime1=0;
    private TextView title_text_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置成全屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.activity_bluetooth);
        ActivityCollector.addActivity(this);
        title_text_title = (TextView)findViewById(R.id.title_text_title);
        title_text_title.setText("蓝牙连接");
        LinearLayout LinearLayout1 = (LinearLayout)findViewById(R.id.LinearLayout1);
        LinearLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mHandler = new Handler();
        This = this;
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        lv=(ListView)this.findViewById(R.id.listView1);

        mleDeviceListAdapter=new LeDeviceListAdapter();

        lv.setAdapter(mleDeviceListAdapter);


//        mHandler=new Handler();

        btn=(Button)this.findViewById(R.id.button1);

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                scanLeDevice(true);
            }
        });




        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0,  View v, int position, long id) {
                // TODO Auto-generated method stub
                device = mleDeviceListAdapter.getDevice(position);
                Intent gattServiceIntent = new Intent(BlueTooth.this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                isBind = true;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:

                break;
        }
    }
    private MyGattCallback bleGattCallback=new MyGattCallback();
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    // TODO Auto-generated method stub
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mleDeviceListAdapter.addDevice(device,rssi);
                            mleDeviceListAdapter.notifyDataSetChanged();
                        }
                    });

                    System.out.println("Address:"+device.getAddress());
                    System.out.println("Name:"+device.getName());
                    System.out.println("rssi:"+rssi);

                }
            };



    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;

        private LayoutInflater mInflator;


        public LeDeviceListAdapter() {
            super();
            rssis=new ArrayList<Integer>();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device,int rssi) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
                rssis.add(rssi);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
            rssis.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            // General ListView optimization code.

            view = mInflator.inflate(R.layout.listitem, null);

            TextView deviceAddress = (TextView) view.findViewById(R.id.tv_deviceAddr);
            TextView deviceName = (TextView) view.findViewById(R.id.tv_deviceName);
            TextView rssi = (TextView) view.findViewById(R.id.tv_rssi);


            BluetoothDevice device = mLeDevices.get(i);
            deviceAddress.setText( device.getAddress());
            deviceName.setText(device.getName());
            rssi.setText(""+rssis.get(i));



            return view;
        }
    }
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e("zx", "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(device.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }

    };
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                status="connected";
                SharedPreferences sp=getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("status","已连接");
                edit.commit();
                startActivity(new Intent(BlueTooth.this,CameraActivity.class));
                Toast.makeText(getApplicationContext(),"连接成功！",Toast.LENGTH_SHORT).show();
                System.out.println("BroadcastReceiver :"+"device connected");

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                status="disconnected";
                System.out.println("BroadcastReceiver :"+"device disconnected");
                SharedPreferences sp=getSharedPreferences("status",Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("status","未连接");
                edit.commit();
                finish();

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                System.out.println("BroadcastReceiver :"+"device SERVICES_DISCOVERED");
            }
        }
    };

    private static BluetoothLeService.OnDataAvailableListener mOnDataAvailable = new com.haojiu.smartcamera.BluetoothLeService.OnDataAvailableListener(){

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e("zx","onCharacteristicRead "+gatt.getDevice().getName()
                        +" read "
                        +characteristic.getUuid().toString()
                        +" -> "
                        +BlueToothUtils.bytesToHexString(characteristic.getValue()));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic) {
            mBluetoothLeService.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
            Log.e("zx","onCharacteristicWrite "+gatt.getDevice().getName()
                    +" write "
                    +characteristic.getUuid().toString()
                    +" -> "
                    +new String(characteristic.getValue()));

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.e("zx","onCharacteristicChanged "+gatt.getDevice().getName()
                    +" write "
                    +characteristic.getUuid().toString()
                    +" -> "
                    +new String(characteristic.getValue()));

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        if (isRegist){
            unregisterReceiver(myReceiver);
        }
        if (isBind){
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
        ActivityCollector.removeActivity(this);
    }

    //Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(device.getAddress());
            Log.d("zx", "Connect request result=" + result);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";

        //服务数据,可扩展下拉列表的第一级数据
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        //特征数据（隶属于某一级服务下面的特征值集合）
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();

        //部分层次，所有特征值集合
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {

            //获取服务列表
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            //查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            System.out.println("Service uuid:"+uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();

            //从当前循环所指向的服务中读取特征值列表
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            //对于当前循环所指向的服务中的每一个特征值
            for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                if(gattCharacteristic.getUuid().toString().equals(SampleGattAttributes.HEART_RATE_MEASUREMENT)){
                    //测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothLeService.readCharacteristic(gattCharacteristic);
                        }
                    }, 500);

                    //接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
                    mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                    //设置数据内容
                    //往蓝牙模块写入数据
                    //mBluetoothLeService.writeCharacteristic(gattCharacteristic);
                }
                List<BluetoothGattDescriptor> descriptors= gattCharacteristic.getDescriptors();
                for(BluetoothGattDescriptor descriptor:descriptors)
                {
                    System.out.println("---descriptor UUID:"+descriptor.getUuid());
                    //获取特征值的描述
                    mBluetoothLeService.getCharacteristicDescriptor(descriptor);
                    //mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                }

                gattCharacteristicGroupData.add(currentCharaData);
            }
            //按先后顺序，分层次放入特征值集合中，只有特征值
            mGattCharacteristics.add(charas);
            //构件第二级扩展列表（服务下面的特征值）
            gattCharacteristicData.add(gattCharacteristicGroupData);

        }
        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(3).get(4);

            //当前目标特征值
            target_chara = characteristic;
            target_group = 3;


            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                System.out.println("kkkkkkkkkk+=" + characteristic.getUuid());
                try {
                    mBluetoothLeService.setCharacteristicNotification(
                            characteristic, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("zx", "onChildClick: ", e);
                }
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BlueTooth.read(0);
            IntentFilter intentFilter = new IntentFilter(
                    "com.example.bluetooth.le.ACTION_DATA_AVAILABLE");
            registerReceiver(myReceiver, intentFilter);//注册广播
            isRegist = true;
        }

    }

    public static void read(int Index)
    {
        mBluetoothLeService.setOnDataAvailableListener(mOnDataAvailable);
        //mBluetoothLeService.readCharacteristic(target_chara);
        if(This.mGattCharacteristics.get(target_group).size()==5) {
            mBluetoothLeService.readCharacteristic(This.mGattCharacteristics.get(target_group).get(Index));
        }
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    BluetoothLeService.ACTION_DATA_AVAILABLE)) {
                smsg = intent.getExtras().getString(BluetoothLeService.EXTRA_DATA);
                if(readIdx==0)
                {
                    readIdx++;
                    Log.e("zx",readIdx+"");
                    BlueTooth.read(readIdx);
                }
                else if(readIdx==1)
                {
                    readIdx++;
                    Log.e("zx",readIdx+"");
                    BlueTooth.read(readIdx);
                }
                else if(readIdx==2)
                {
                    readIdx++;
                    Log.e("zx",readIdx+"");
                    BlueTooth.read(readIdx);
                }
                else if(readIdx==3)
                {
                    byte[] x = smsg.getBytes();
                    if(smsg.length()==1) {
                        SharedPreferences sp=getSharedPreferences("Power",Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putInt("power",x[0]);
                        edit.commit();
                    }
                    readIdx++;
                    BlueTooth.read(readIdx);
                }
                else if(readIdx==4)
                {
                    byte[] x = Base64.decode(smsg, Base64.DEFAULT);
                    if(x.length==3) {
                        int o = 'B' ^ x[0] ^ x[1];
                        Log.e("zx", "onReceive: " + x[0] + " " + x[1] + " " + x[2] + ",len" + smsg.length() + " Key:" + o);
                        SharedPreferences sp=getSharedPreferences("Key",Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = sp.edit();
                        if (o==3){
                            anTime++;
                        }
                        if (o==8){
                            anTime1++;
                        }
                        if (o==1){
                            addTime++;
                        }
                        edit.putInt("Key",o);
                        edit.putInt("anTime",anTime);
                        edit.putInt("anTime1",anTime1);
                        edit.putInt("addTime",addTime);
                        edit.commit();
                    }
                    else {
                        Log.e("zx", "onReceive,len" + smsg.length());
                    }
                }
            }
        }

    };
}
