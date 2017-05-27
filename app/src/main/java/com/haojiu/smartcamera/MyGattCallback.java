package com.haojiu.smartcamera;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

public class MyGattCallback extends BluetoothGattCallback {

	@Override
	public void onCharacteristicChanged(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic) {
		// TODO Auto-generated method stub
		super.onCharacteristicChanged(gatt, characteristic);
	}

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
		// TODO Auto-generated method stub
		super.onCharacteristicRead(gatt, characteristic, status);
	}

	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
		// TODO Auto-generated method stub
		super.onCharacteristicWrite(gatt, characteristic, status);
	}

	@Override
	public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                        int newState) {
		// TODO Auto-generated method stub
		super.onConnectionStateChange(gatt, status, newState);
	}

	@Override
	public void onDescriptorRead(BluetoothGatt gatt,
                                 BluetoothGattDescriptor descriptor, int status) {
		// TODO Auto-generated method stub
		super.onDescriptorRead(gatt, descriptor, status);
	}

	@Override
	public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor descriptor, int status) {
		// TODO Auto-generated method stub
		super.onDescriptorWrite(gatt, descriptor, status);
	}

	@Override
	public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
		// TODO Auto-generated method stub
		super.onReadRemoteRssi(gatt, rssi, status);
	}

	@Override
	public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
		// TODO Auto-generated method stub
		super.onReliableWriteCompleted(gatt, status);
	}

	@Override
	public void onServicesDiscovered(BluetoothGatt gatt, int status) {
		// TODO Auto-generated method stub
		super.onServicesDiscovered(gatt, status);
		
		for(BluetoothGattService service :gatt.getServices() )
		{
			System.out.println(service.getUuid().toString());
		}
	}

	

}
