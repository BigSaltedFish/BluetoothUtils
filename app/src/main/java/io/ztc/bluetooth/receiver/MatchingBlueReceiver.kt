package io.ztc.bluetooth.receiver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MatchingBlueReceiver(private val callBack: MatchingBlueCallBack) : BroadcastReceiver() {
    private val pin = "0000"  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000

    //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(tag, "action:" + action!!)
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

        if (BluetoothDevice.ACTION_PAIRING_REQUEST == action) {
            try {
                callBack.onBondRequest()
                //1.确认配对
                //                ClsUtils.setPairingConfirmation(device.getClass(), device, true);
                val setPairingConfirmation = device!!.javaClass.getDeclaredMethod("setPairingConfirmation", Boolean::class.javaPrimitiveType)
                setPairingConfirmation.invoke(device, true)
                //2.终止有序广播
                Log.d("order...", "isOrderedBroadcast:$isOrderedBroadcast,isInitialStickyBroadcast:$isInitialStickyBroadcast")
                abortBroadcast()//如果没有将广播终止，则会出现一个一闪而过的配对框。
                //3.调用setPin方法进行配对...
                //                boolean ret = ClsUtils.setPin(device.getClass(), device, pin);
                val removeBondMethod = device.javaClass.getDeclaredMethod("setPin", *arrayOf<Class<*>>(ByteArray::class.java))
                val returnValue = removeBondMethod.invoke(device, *arrayOf<Any>(pin.toByteArray())) as Boolean?
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
            assert(device != null)
            when (device!!.bondState) {
                BluetoothDevice.BOND_NONE -> {
                    Log.d(tag, "取消配对")
                    callBack.onBondFail(device)
                }
                BluetoothDevice.BOND_BONDING -> {
                    Log.d(tag, "配对中")
                    callBack.onBonding(device)
                }
                BluetoothDevice.BOND_BONDED -> {
                    Log.d(tag, "配对成功")
                    callBack.onBondSuccess(device)
                }
            }
        }
    }

    /** 定义监听接口   */
    interface MatchingBlueCallBack {
        fun onBondRequest()
        fun onBondFail(device: BluetoothDevice)
        fun onBonding(device: BluetoothDevice)
        fun onBondSuccess(device: BluetoothDevice)
    }

    companion object {
        private val tag = "Bluetooth"
    }
}
