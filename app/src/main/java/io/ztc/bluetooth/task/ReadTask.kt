package io.ztc.bluetooth.task

import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

import java.io.BufferedInputStream
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.CUPCAKE)
class ReadTask(private val socket: BluetoothSocket, private val callBack: ReadCallBack?) : AsyncTask<String, Int, String>() {
    override fun doInBackground(vararg strings: String): String {
        var `in`: BufferedInputStream? = null
        try {
            val sb = StringBuffer()
            `in` = BufferedInputStream(socket.inputStream)

            var length: Int
            val buf = ByteArray(1024)
            while ((`in`.read().also { length = it }) != -1) {
                sb.append(String(buf, 0, length))
            }
            return sb.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                assert(`in` != null)
                `in`!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        return "读取失败"
    }

    override fun onPreExecute() {
        Log.d(tag, "开始读取数据")
        callBack?.onStarted()
    }

    override fun onPostExecute(s: String) {
        Log.d(tag, "完成读取数据")
        if (callBack != null) {
            if ("读取失败" == s) {
                callBack.onFinished(false, s)
            } else {
                callBack.onFinished(true, s)
            }
        }
    }

    interface ReadCallBack {
        fun onStarted()
        fun onFinished(e: Boolean, msg: String)
    }

    companion object {
        private val tag = "Bluetooth"
    }
}
