import 'dart:async';

import 'package:fludcs/model/directive.dart';
import 'package:flutter/services.dart';
import 'dart:convert';

/// Stream Controller
StreamController<String> _dcsAuthController = StreamController.broadcast();
StreamController<Directive> _dcsDirectiveController = StreamController.broadcast();
StreamController<String> _dcsAsrResultController = StreamController.broadcast();
StreamController<String> _dcsDialogStateController = StreamController.broadcast();
/// Response Stream
Stream<String> get responseFromAuth => _dcsAuthController.stream;
Stream<Directive> get directive => _dcsDirectiveController.stream;
Stream<String> get asrResult => _dcsAsrResultController.stream;
Stream<String> get dialogState => _dcsDialogStateController.stream;
final MethodChannel _channel = const MethodChannel('com.roam2free/fludcs')
  ..setMethodCallHandler(_handler);

Future<dynamic> _handler(MethodCall methodCall) {
  switch(methodCall.method){
    case 'onAuthSucceed':{
      _dcsAuthController.add(methodCall.arguments['accessToken']);
      break;
    }
    case 'onAuthFailed':{
      _dcsAuthController.add(methodCall.arguments['errorMessage']);
      break;
    }
    case 'onAuthCancel':{
      _dcsAuthController.add(methodCall.arguments['onAuthCancel']);
      break;
    }
    case 'onDirective':{
      _dcsDirectiveController.add(Directive.fromJsonMap(json.decode(methodCall.arguments)));
      break;
    }
    case 'onHandleAsrResult':{
      _dcsAsrResultController.add(methodCall.arguments);
      break;
    }
    case 'onDialogStateChanged':{
      _dcsDialogStateController.add(methodCall.arguments);
      break;
    }
  }
  return Future.value(true);
}

Future<String> get platformVersion async {
  final String version = await _channel.invokeMethod('getPlatformVersion');
  return version;
}

initDcs(){
  _channel.invokeMethod('initDcs');
}

dcsAuth() {
  _channel.invokeMethod('dcsAuth');
}

beginVoiceRequest(){
  _channel.invokeMethod('beginVoiceRequest');
}

speakOfflineRequest(String text){
  _channel.invokeMethod('speakOfflineRequest',text);
}

speakRequest(String text){
  _channel.invokeMethod('speakRequest',text);
}

dispose()=>_channel.invokeMethod('dispose');