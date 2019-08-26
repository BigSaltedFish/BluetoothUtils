package io.ztc.bluetooth.task

import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.util.Log

import java.io.IOException
import java.io.OutputStream

class WriteTask(private val socket: BluetoothSocket,private val callBack: WriteCallBack?) : AsyncTask<String, Int, String>() {
    override fun doInBackground(vararg strings: String): String {
        val string = strings[0]
        var outputStream: OutputStream? = null
        try {
            outputStream = socket.outputStream

            outputStream!!.write(string.toByteArray())
        } catch (e: IOException) {
            Log.e(tag, "写入时发生异常.", e)
            return "发送失败"
        } finally {
            try {
                assert(outputStream != null)
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return "发送成功"


    }

    override fun onPreExecute() {
        callBack?.onStarted()
    }

    override fun onPostExecute(s: String) {
        if (callBack != null) {
            if ("发送成功" == s) {
                callBack.onFinished(true, s)
            } else {
                callBack.onFinished(false, s)
            }

        }
    }

    interface WriteCallBack {
        fun onStarted()
        fun onFinished(e: Boolean, msg: String)
    }

    companion object {
        private val tag = "Bluetooth"
    }
}

