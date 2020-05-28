import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:terminal_scanner/terminal_scanner.dart';

void main() {
  const MethodChannel channel = MethodChannel('com.yoren.terminal_scanner');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
  });
}
