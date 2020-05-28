import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

export 'src/baud_rate.dart';
export 'src/com_path.dart';

StreamController<String> scanResultStreamController =
    StreamController.broadcast();

class TerminalScanner {
  static const MethodChannel _channel =
      const MethodChannel('com.yoren.terminal_scanner');

  static Future<bool> setupComScanner({
    @required String comPath,
    @required String baudRate,
  }) async {
    _registerScanResultHandler();
    return await _channel.invokeMethod('setupComScanner', {
      'comPath': comPath,
      'baudRate': baudRate,
    });
  }

  static Future<bool> setupUsbScanner() async {
    _registerScanResultHandler();
    return await _channel.invokeMethod('setupUsbScanner');
  }

  static Future<List<String>> getAllDevicesPath() async {
    String result = await _channel.invokeMethod('getAllDevicesPath');
    var list = jsonDecode(result) as List<dynamic>;
    return list.map((e) => e as String).toList();
  }

  static void _registerScanResultHandler() {
    _channel.setMethodCallHandler((call) {
      if (call.method == 'sendScanResult') {
        scanResultStreamController.sink.add(call.arguments);
      }
      return Future.value('');
    });
  }
}
