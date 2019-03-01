package com.sunmi.innerprinter;

import android.content.BroadcastReceiver;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import android.widget.Toast;

import java.util.Map;
import java.io.IOException;

import woyou.aidlservice.jiuiv5.IWoyouService;
import woyou.aidlservice.jiuiv5.ICallback;
import android.os.RemoteException;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;
import android.graphics.Bitmap;

import java.nio.charset.StandardCharsets;

import android.util.Log;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.sunmi.innerprinter.PrinterTask.Name;

import android.content.IntentFilter;

import java.util.Map;
import java.util.HashMap;

public class SunmiInnerPrinterModule extends ReactContextBaseJavaModule {
    public static ReactApplicationContext reactApplicationContext = null;
    private IWoyouService woyouService;
    private BitmapUtils bitMapUtils;
    private PrinterReceiver receiver = new PrinterReceiver();

    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service disconnected: " + name);
            woyouService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service connected: " + name);
            woyouService = IWoyouService.Stub.asInterface(service);
        }
    };

    private static final String TAG = "SunmiInnerPrinterModule";

    public SunmiInnerPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
       Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        reactContext.startService(intent);
        reactContext.bindService(intent, connService, Context.BIND_AUTO_CREATE);
        bitMapUtils = new BitmapUtils(reactContext);

        getReactApplicationContext().registerReceiver(receiver, Constants.getActions());
        Log.d("PrinterReceiver", "------------ init ");
    }

    @Override
    public String getName() {
        return "SunmiInnerPrinter";
    }


    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();

        constants.put("Constants", Constants.getConstants());

        constants.put("hasPrinter", hasPrinter());

        try {
            constants.put("printerVersion", getPrinterVersion());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerSerialNo", getPrinterSerialNo());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
        try {
            constants.put("printerModal", getPrinterModal());
        } catch (Exception e) {
            // Log and ignore for it is not the madatory constants.
            Log.i(TAG, "ERROR: " + e.getMessage());
        }

        return constants;
    }

    @ReactMethod
    public void printerInit(final Promise p) {
        PrinterTask task = new PrinterTask(Name.PRINTER_INIT, p, woyouService);
        ThreadPoolManager.getInstance().executeTask(task);        
    }

    @ReactMethod
    public void printerSelfChecking(final Promise p) {
        // final IWoyouService printerService = woyouService;        
        PrinterTask task = new PrinterTask(Name.PRINTER_SELF_CHECKING, p, woyouService);
        ThreadPoolManager.getInstance().executeTask(task);        
    }

    @ReactMethod
    public void getPrinterSerialNo(final Promise p) {
        try {
            p.resolve(getPrinterSerialNo());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterSerialNo() throws Exception {
        final IWoyouService printerService = woyouService;
        return printerService.getPrinterSerialNo();
    }

    @ReactMethod
    public void getPrinterVersion(final Promise p) {
        try {
            p.resolve(getPrinterVersion());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterVersion() throws Exception {
        final IWoyouService printerService = woyouService;
        return printerService.getPrinterVersion();
    }
    
    @ReactMethod
    public void getPrinterModal(final Promise p) {
        try {
            p.resolve(getPrinterModal());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private String getPrinterModal() throws Exception {
        //Caution: This method is not fully tested -- Januslo 2018-08-11
        final IWoyouService printerService = woyouService;
        return printerService.getPrinterModal();
    }

    @ReactMethod
    public void hasPrinter(final Promise p) {
        try {
            p.resolve(hasPrinter());
        } catch (Exception e) {
            Log.i(TAG, "ERROR: " + e.getMessage());
            p.reject("" + 0, e.getMessage());
        }
    }

    private boolean hasPrinter() {
        final IWoyouService printerService = woyouService;
        final boolean hasPrinterService = printerService != null;
        return hasPrinterService;
    }
    
    @ReactMethod
    public void getPrintedLength(final Promise p) {
        PrinterTask task = new PrinterTask(Name.GET_PRINTED_LENGTH, p, woyouService);        
        ThreadPoolManager.getInstance().executeTask(task);
    }
   
    @ReactMethod
    public void lineWrap(int n, final Promise p) {
        PrinterTask task = new PrinterTask(Name.LINE_WRAP, p, woyouService);
        task.setCount(n);

        ThreadPoolManager.getInstance().executeTask(task);
    }
    
    @ReactMethod
    public void sendRAWData(String base64EncriptedData, final Promise p) {
        byte[] d = Base64.decode(base64EncriptedData, Base64.DEFAULT);
            
        PrinterTask task = new PrinterTask(Name.SEND_RAW_DATA, p, woyouService);
        task.setRawData(d);

        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void setAlignment(int alignment, final Promise p) {
        PrinterTask task = new PrinterTask(Name.SET_ALIGNMENT, p, woyouService);
        task.setAlignment(alignment);

        ThreadPoolManager.getInstance().executeTask(task);
    }
    
    @ReactMethod
    public void setFontName(String typeface, final Promise p) {            
        PrinterTask task = new PrinterTask(Name.SET_FONT_NAME, p, woyouService);
        task.setTypeface(typeface);

        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void setFontSize(float fontsize, final Promise p) {            
        PrinterTask task = new PrinterTask(Name.SET_FONT_SIZE, p, woyouService);
        task.setFontSize(fontsize);
        ThreadPoolManager.getInstance().executeTask(task);        
    }

    @ReactMethod
    public void printTextWithFont(String text, String typeface, float fontsize, final Promise p) {
        PrinterTask task = new PrinterTask(Name.PRINT_TEXT_WITH_FONT, p, woyouService);
        task.setMessage(text);
        task.setTypeface(typeface);
        task.setFontSize(fontsize);

        ThreadPoolManager.getInstance().executeTask(task);        
    }

    @ReactMethod
    public void printColumnsText(ReadableArray colsTextArr, ReadableArray colsWidthArr, ReadableArray colsAlign, final Promise p) {
        
        String[] clst = new String[colsTextArr.size()];
        for (int i = 0; i < colsTextArr.size(); i++) {
            clst[i] = colsTextArr.getString(i);
        }
        int[] clsw = new int[colsWidthArr.size()];
        for (int i = 0; i < colsWidthArr.size(); i++) {
            clsw[i] = colsWidthArr.getInt(i);
        }
        int[] clsa = new int[colsAlign.size()];
        for (int i = 0; i < colsAlign.size(); i++) {
            clsa[i] = colsAlign.getInt(i);
        }
            
        PrinterTask task = new PrinterTask(Name.PRINT_COLUMNS_TEXT, p, woyouService);
        task.setClst(clst);
        task.setClsa(clsa);
        task.setClsw(clsw);

        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void printBitmap(String data, int width, int height, final Promise p) {
        try {
            byte[] decoded = Base64.decode(data, Base64.DEFAULT);
            Bitmap bitMap = bitMapUtils.decodeBitmap(decoded, width, height);
            
            PrinterTask task = new PrinterTask(Name.PRINT_BITMAP, p, woyouService);
            task.setBitmap(bitMap);

            ThreadPoolManager.getInstance().executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "ERROR: " + e.getMessage());
        }
    }

    /**
     * @param symbology:    0 -- UPC-A，
     *                      1 -- UPC-E，
     *                      2 -- JAN13(EAN13)，
     *                      3 -- JAN8(EAN8)，
     *                      4 -- CODE39，
     *                      5 -- ITF，
     *                      6 -- CODABAR，
     *                      7 -- CODE93，
     *                      8 -- CODE128
     */
    @ReactMethod
    public void printBarCode(String data, int symbology, int height, int width, int textposition, final Promise p) {
        Log.i(TAG, "come: ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.PRINT_BAR_CODE, p, woyouService);
        task.setData(data);
        task.setSymbology(symbology);
        task.setHeight(height);
        task.setWidth(width);
        task.setTextposition(textposition);

        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void printQRCode(String data, int modulesize, int errorlevel, final Promise p) {
        Log.i(TAG, "come: ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.PRINT_QR_CODE, p, woyouService);
        task.setData(data);
        task.setModulesize(modulesize);
        task.setErrorlevel(errorlevel);

        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void printOriginalText(String text, final Promise p) {
        Log.i(TAG, "come: " + text + " ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.LINE_WRAP, p, woyouService);
        task.setMessage(text);

        ThreadPoolManager.getInstance().executeTask(task);       
    }

    @ReactMethod
    public void commitPrinterBuffer() {        
        Log.i(TAG, "come: commit buffter ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.COMMIT_BUFFER, null, woyouService);        
        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void enterPrinterBuffer(boolean clean) {
        Log.i(TAG, "come: " + clean + " ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.ENTER_BUFFER, null, woyouService);
        task.setClean(clean);

        ThreadPoolManager.getInstance().executeTask(task);
    }

    @ReactMethod
    public void exitPrinterBuffer(boolean commit) {
        Log.i(TAG, "come: " + commit + " ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.EXIT_BUFFER, null, woyouService);
        task.setCommit(commit);

        ThreadPoolManager.getInstance().executeTask(task);
    }


    @ReactMethod
    public void printString(String message, final Promise p) {        
        Log.i(TAG, "come: " + message + " ss:" + woyouService);
            
        PrinterTask task = new PrinterTask(Name.PRINT_TEXT, p, woyouService);
        task.setMessage(message);

        ThreadPoolManager.getInstance().executeTask(task);
    }
}
