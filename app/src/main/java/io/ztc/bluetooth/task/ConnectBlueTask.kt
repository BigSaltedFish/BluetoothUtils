package io.ztc.bluetooth.task

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.util.Log
import java.util.UUID
import io.ztc.bluetooth.BluetoothUtils

class ConnectBlueTask(private val callBack: ConnectBlueCallBack?) : AsyncTask<BluetoothDevice, Int, BluetoothSocket>() {
    private var bluetoothDevice: BluetoothDevice? = null

    companion object {
        private const val tag = "Bluetooth"
        private var SPP_UUID = "00001105-0000-1000-8000-00805f9B34FB"
    }

    @SuppressLint("MissingPermission")
    override fun doInBackground(vararg bluetoothDevices: BluetoothDevice): BluetoothSocket? {
        bluetoothDevice = bluetoothDevices[0]
        var socket: BluetoothSocket? = null
        try {
            val uuid: UUID = bluetoothDevice!!.uuids[0].uuid
            Log.e(tag, "开始连接socket,uuid:$uuid")
            socket = bluetoothDevice!!.createRfcommSocketToServiceRecord(uuid)
            if (socket != null && !socket.isConnected) {
                    socket = BluetoothUtils.socket(bluetoothDevice)
                    socket!!.connect()
            }
        } catch (e: Exception) {
            Log.e(tag, "socket连接失败")
            try {
                socket!!.close()
                Log.e(tag, "socket已关闭(1)")
                val uuid: UUID = bluetoothDevice!!.uuids[0].uuid
                socket = bluetoothDevice!!.createRfcommSocketToServiceRecord(uuid)
                if (socket != null && !socket.isConnected) {
                    socket = BluetoothUtils.socket(bluetoothDevice)
                    socket!!.connect()
                    Log.e(tag, "socket尝试再次连接....")
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
                try {
                    socket!!.close()
                    Log.e(tag, "socket已关闭(2)")
                }catch (e2 :Exception){
                    e2.printStackTrace()
                    Log.e(tag, "socket关闭失败(2)")
                }
            }
        }
        return socket
    }

    override fun onPreExecute() {
        Log.e(tag, "开始连接")
        callBack?.onStartConnect()
    }

    override fun onPostExecute(bluetoothSocket: BluetoothSocket?) {
        if (bluetoothSocket != null && bluetoothSocket.isConnected) {
            Log.e(tag, "连接成功")
            callBack?.onConnectSuccess(bluetoothDevice, bluetoothSocket)
        } else {
            Log.e(tag, "连接失败")
            callBack?.onConnectFail(bluetoothDevice, "连接失败")
        }
    }

    /** 定义监听接口   */
    interface ConnectBlueCallBack {
        fun onStartConnect()
        fun onConnectSuccess(device: BluetoothDevice?, socket: BluetoothSocket)
        fun onConnectFail(device: BluetoothDevice?, msg: String)
    }


}

