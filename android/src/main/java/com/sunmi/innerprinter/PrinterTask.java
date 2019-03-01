package com.sunmi.innerprinter;

import com.facebook.react.bridge.Promise;

import woyou.aidlservice.jiuiv5.IWoyouService;
import woyou.aidlservice.jiuiv5.ICallback;

import android.util.Log;
import android.graphics.Bitmap;

public class PrinterTask implements Runnable {

    public enum Name {  PRINT_TEXT, ENTER_BUFFER, EXIT_BUFFER, 
                        COMMIT_BUFFER, PRINT_ORIGINAL_TEXT, PRINT_QR_CODE, 
                        PRINT_BAR_CODE, PRINT_BITMAP, PRINT_COLUMNS_TEXT, 
                        PRINT_TEXT_WITH_FONT, SET_FONT_SIZE, SET_FONT_NAME,
                        SET_ALIGNMENT, SEND_RAW_DATA, LINE_WRAP,
                        GET_PRINTED_LENGTH, PRINTER_SELF_CHECKING, PRINTER_INIT
                    };

    
    private static final String TAG = "PrinterTask";
    
    private Name task;

    private final Promise p;
    private final IWoyouService printerService;

    // Parameters passed to IWoyouService methods
    private int count, height, width, textposition, modulesize, errorlevel, symbology, alignment;
    private byte[] rawData;    
    private String typeface, data, message;
    private float fontSize;    
    private String[] clst;
    private int[] clsw, clsa;    
    private Bitmap bitmap;
    private boolean clean, commit;

    private ICallback.Stub callback = new ICallback.Stub() {
        @Override
        public void onRunResult(boolean isSuccess) {
            if (isSuccess) {
                p.resolve(null);
            } else {
                p.reject("0", isSuccess + "");
            }
        }

        @Override
        public void onReturnString(String result) {
            p.resolve(result);
        }

        @Override
        public void onRaiseException(int code, String msg) {
            p.reject("" + code, msg);
        }
    };

    public PrinterTask(Name _task, final Promise _p, final IWoyouService _printerService) {
        task = _task;
        p = _p;
        printerService = _printerService;
    }

    @Override
    public void run() {  
        
        try {
            switch (task) {
                case PRINT_TEXT: printerService.printText(message, callback); break;
                case ENTER_BUFFER: printerService.enterPrinterBuffer(clean); break;
                case EXIT_BUFFER: printerService.exitPrinterBuffer(commit); break;
                case COMMIT_BUFFER: printerService.commitPrinterBuffer(); break;
                case PRINT_ORIGINAL_TEXT: printerService.printOriginalText(message, callback); break;
                case PRINT_QR_CODE: printerService.printQRCode(data, modulesize, errorlevel, callback); break;
                case PRINT_BAR_CODE: printerService.printBarCode(data, symbology, height, width, textposition, callback); break;
                case PRINT_BITMAP: printerService.printBitmap(bitmap, callback); break;
                case PRINT_COLUMNS_TEXT: printerService.printColumnsText(clst, clsw, clsa, callback); break;
                case PRINT_TEXT_WITH_FONT: printerService.printTextWithFont(message, typeface, fontSize, callback); break;
                case SET_FONT_SIZE: printerService.setFontSize(fontSize, callback); break;
                case SET_FONT_NAME: printerService.setFontName(typeface, callback); break;
                case SET_ALIGNMENT: printerService.setAlignment(alignment, callback); break;
                case SEND_RAW_DATA: printerService.sendRAWData(rawData, callback); break;
                case LINE_WRAP: printerService.lineWrap(count, callback); break;
                case GET_PRINTED_LENGTH: printerService.getPrintedLength(callback); break;
                case PRINTER_SELF_CHECKING: printerService.printerSelfChecking(callback); break;
                case PRINTER_INIT: printerService.printerInit(callback); break;
            }    
        } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "ERROR: " + e.getMessage());
                p.reject("" + 0, e.getMessage());
        }    
    }

    // Setters
    public void setCount(int count) {
        this.count = count;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setTextposition(int textposition) {
        this.textposition = textposition;
    }

    public void setModulesize(int modulesize) {
        this.modulesize = modulesize;
    }

    public void setErrorlevel(int errorlevel) {
        this.errorlevel = errorlevel;
    }

    public void setSymbology(int symbology) {
        this.symbology = symbology;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public void setTypeface(String typeface) {
        this.typeface = typeface;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    public void setClst(String[] clst) {
        this.clst = clst;
    }

    public void setClsw(int[] clsw) {
        this.clsw = clsw;
    }

    public void setClsa(int[] clsa) {
        this.clsa = clsa;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setClean(boolean clean) {
        this.clean = clean;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }   
}