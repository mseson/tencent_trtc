import 'dart:async';

import 'package:flutter/services.dart';

class TencentTrtc {
  static const MethodChannel _channel = const MethodChannel('tencent_trtc');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  //初始化
  static Future registerTrtc() async {
    await _channel.invokeMethod("registerTrtc");
  }

  //进入聊天房间
  static Future enterRoomSign(
      {int roomId, String user_id, int appId, String userSig}) async {
    await _channel.invokeMethod("enterRoomSign", {
      "roomId": roomId,
      "user_id": user_id,
      "appId": appId,
      "userSig": userSig,
    });
  }

  //进入聊天房间
  static Future enterRoom(
      {int roomId, String user_id, int appId, String secret_key}) async {
    await _channel.invokeMethod("enterRoom", {
      "roomId": roomId,
      "user_id": user_id,
      "appId": appId,
      "secret_key": secret_key,
    });
  }

  //离开聊天房间
  static Future exitRoom() async {
    await _channel.invokeMethod("exitRoom");
  }

  //开启麦克风
  static Future startLocalAudio() async {
    await _channel.invokeMethod("startLocalAudio");
  }

  //关闭麦克风
  static Future stopLocalAudio() async {
    await _channel.invokeMethod("stopLocalAudio");
  }
}
