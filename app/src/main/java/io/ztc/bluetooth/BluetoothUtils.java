package io.ztc.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import io.ztc.bluetooth.task.ConnectBlueTask;
import java.lang.reflect.Method;


@SuppressLint("MissingPermission")
public class BluetoothUtils {

    private String tag = "Bluetooth";
    private BluetoothAdapter bta;



    private BluetoothSocket bst;
    private Context context;

    public BluetoothUtils(Context context, BluetoothAdapter bta) {
        this.context = context;
        this.bta = bta;
    }

    /**
     * 设备是否支持蓝牙  true为支持
     *
     * @return
     */
    public boolean isSupportBlue() {
        return bta != null;
    }


    /**
     * 蓝牙是否打开   true为打开
     *
     * @return
     */
    public boolean isBlueEnable() {
        return isSupportBlue() && bta.isEnabled();
    }


    /**
     * 自动打开蓝牙（异步：蓝牙不会立刻就处于开启状态）
     * 这个方法打开蓝牙不会弹出提示
     */
    public void openBlueAsyn() {
        if (isSupportBlue()) {
            bta.enable();
        }
    }


    /**
     * 自动打开蓝牙（同步）
     * 这个方法打开蓝牙会弹出提示
     * 需要在onActivityResult 方法中判断resultCode == RESULT_OK  true为成功
     */
    public void openBlueSync(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }


    /**
     * 扫描的方法 返回true 扫描成功
     * 通过接收广播获取扫描到的设备
     *
     * @return
     */
    public boolean scanBlue() {
        if (!isBlueEnable()) {
            //ToastUtils.s(context, 2, "蓝牙未启动").show();
            return false;
        }

        //当前是否在扫描，如果是就取消当前的扫描，重新扫描
        if (bta.isDiscovering()) {
            bta.cancelDiscovery();
        }

        //此方法是个异步操作，一般搜索12秒
        return bta.startDiscovery();
    }


    /**
     * 取消扫描蓝牙
     *
     * @return true 为取消成功
     */

    public boolean cancelScanBule() {
        if (isSupportBlue()) {
            return bta.cancelDiscovery();
        }
        return true;
    }

    /**
     * 配对（配对成功与失败通过广播返回）
     *
     * @param device
     */
    public void matching(BluetoothDevice device) {
        if (device == null) {
            Log.e(tag, "bond device null");
            return;
        }
        if (!isBlueEnable()) {
            Log.e(tag, "Bluetooth not enable!");
            return;
        }
        //配对之前把扫描关闭
        if (bta.isDiscovering()) {
            bta.cancelDiscovery();
        }
        //判断设备是否配对，没有配对在配，配对了就不需要配了
        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            Log.d(tag, "attemp to bond:" + device.getName());
            try {
                Method createBondMethod = device.getClass().getMethod("createBond");
                Boolean returnValue = (Boolean) createBondMethod.invoke(device);
                assert returnValue != null;
                returnValue.booleanValue();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(tag, "尝试连接失败!");
            }
        }
    }

    /**
     * 取消配对（取消配对成功与失败通过广播返回 也就是配对失败）
     *
     * @param device
     */
    public void cancelMatching(BluetoothDevice device) {
        if (device == null) {
            Log.d(tag, "cancel bond device null");
            return;
        }
        if (!isBlueEnable()) {
            Log.e(tag, "Bluetooth not enable!");
            return;
        }
        //判断设备是否配对，没有配对就不用取消了
        if (device.getBondState() != BluetoothDevice.BOND_NONE) {
            Log.d(tag, "attemp to cancel bond:" + device.getName());
            try {
                Method removeBondMethod = device.getClass().getMethod("removeBond");
                Boolean returnValue = (Boolean) removeBondMethod.invoke(device);
                returnValue.booleanValue();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(tag, "尝试取消绑定失败!");
            }
        }
    }

    /**
     * 连接 （在配对之后调用）
     *
     * @param device
     */
    public void connect(BluetoothDevice device, ConnectBlueTask.ConnectBlueCallBack callBack) {
        if (device == null) {
            Log.d(tag, "设备为空");
            return;
        }
        if (!isBlueEnable()) {
            Log.e(tag, "蓝牙未启用!");
            return;
        }
        //连接之前把扫描关闭
        if (bta.isDiscovering()) {
            bta.cancelDiscovery();
            Log.e(tag, "关闭搜索!");
        }
        new ConnectBlueTask(callBack).execute(device);

    }


    /**
     * 输入mac地址进行自动配对
     * 前提是系统保存了该地址的对象
     *
     * @param address
     * @param callBack
     */
    public void connectMAC(String address, ConnectBlueTask.ConnectBlueCallBack callBack) {
        if (!isBlueEnable()) {
            return;
        }
        BluetoothDevice btDev = bta.getRemoteDevice(address);
        connect(btDev, callBack);
    }


    public void addBluetoothSocket(BluetoothSocket socket) {
        this.bst = socket;
    }


    /**
     * 蓝牙是否连接
     *
     * @return
     */
    public boolean isConnectBlue() {
        return bst != null && bst.isConnected();
    }



    /**
     * 断开连接
     * @return
     */
    public boolean cancelConnect(){
        if (bst != null && bst.isConnected()){
            try {
                bst.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        bst = null;
        //ToastUtils.s(context,1,"断开连接").show();
        return true;
    }

    public BluetoothSocket getBst() {
        return bst;
    }

    public void setBst(BluetoothSocket bst) {
        this.bst = bst;
    }

    /**
     * socket初始化
     * @param device 设备
     * @return 可用的socket
     */
    public static BluetoothSocket socket(BluetoothDevice device){
       BluetoothSocket socket = null;
        try {
            socket = (BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }
}
