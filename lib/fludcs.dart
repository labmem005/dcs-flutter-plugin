library fludcs;

import 'package:flutter/services.dart';

export 'fludcs_impl.dart';

//import 'dart:async';

export 'package:flutter/services.dart';

class Fludcs {
  static const MethodChannel _channel =
      const MethodChannel('com.roam2free/fludcs');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
