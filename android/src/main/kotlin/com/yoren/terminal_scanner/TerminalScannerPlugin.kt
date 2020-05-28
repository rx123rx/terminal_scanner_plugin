package com.yoren.terminal_scanner

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android_serialport_api.SerialPortFinder
import androidx.annotation.NonNull
import com.yoren.terminal_scanner.scanner.Scanner
import com.yoren.terminal_scanner.util.ScannerUtils
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import org.json.JSONArray

class TerminalScannerPlugin : FlutterPlugin, MethodCallHandler {
    private lateinit var channel: MethodChannel
    private val mHandler = Handler(Looper.getMainLooper())
    private var isUsbScanner = false
    private var isComScanner = false
    private var mScanner: Scanner? = null
    private var mScanResult = StringBuilder()
    private var canSendBackScanResult = true
    private val timer: CountDownTimer = object : CountDownTimer(700, 700) {
        override fun onFinish() {
            canSendBackScanResult = true
        }

        override fun onTick(millisUntilFinished: Long) {
        }

    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.yoren.terminal_scanner")
        channel.setMethodCallHandler(this)
    }

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.yoren.terminal_scanner")
            channel.setMethodCallHandler(TerminalScannerPlugin())
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "setupComScanner" -> {
                setupComScanner(call)
            }
            "setupUsbScanner" -> {
                setupUsbScanner()
            }
            "getAllDevicesPath" -> {
                result.success(JSONArray(SerialPortFinder().allDevicesPath).toString())
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        mScanner?.close()
    }

    private fun setupComScanner(call: MethodCall) {
        isComScanner = true
        val arguments = call.arguments as HashMap<String, String>
        mScanner = Scanner(arguments["comPath"], arguments["baudRate"])
        mScanner!!.setListener { _, _, _, dateByteArray ->
            if (canSendBackScanResult) {
                canSendBackScanResult = false
                timer.start()
                val scanResult = String(dateByteArray)
                sendScanResultToFlutter(scanResult.trim())
            }
            0
        }
    }

    private fun setupUsbScanner() {
        isUsbScanner = true
    }

    fun sendScanResultToFlutter(scanResult: String) {
        mHandler.post {
            channel.invokeMethod("sendScanResult", scanResult)
        }
    }

    open inner class TerminalScannerActivity : FlutterActivity() {


        override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
            if (isUsbScanner) {
                if (event?.action == KeyEvent.ACTION_DOWN && ScannerUtils.isInputFromScanner(this, event)) {
                    if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        if (canSendBackScanResult) {
                            canSendBackScanResult = false
                            timer.start()
                            sendScanResultToFlutter(mScanResult.toString())
                        }
                        mScanResult.clear()
                    } else {
                        val char = event.unicodeChar.toChar()
                        mScanResult.append(char)
                    }
                }
            }
            return super.dispatchKeyEvent(event)
        }
    }
}
