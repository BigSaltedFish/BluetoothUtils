package io.ztc.bluetooth.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ReceiveBlueReceiver(private val callBack: ReceiveBlueCallBack) : BroadcastReceiver() {

    //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val tag = "ReceiveBlue"
        Log.d(tag, "action:" + action!!)
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        when (action) {
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                Log.d(tag, "开始扫描...")
                callBack.receiveStarted()
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                Log.d(tag, "结束扫描...")
                callBack.receiveFinished()
            }
            BluetoothDevice.ACTION_FOUND -> {
                Log.d(tag, "发现设备...")
                callBack.receiving(device)
            }
        }
    }


    /** 定义监听接口  */
    interface ReceiveBlueCallBack {
        fun receiveStarted()
        fun receiveFinished()
        fun receiving(device: BluetoothDevice?)
    }

}
