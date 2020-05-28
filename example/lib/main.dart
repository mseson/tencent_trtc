import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:tencent_trtc/tencent_trtc.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String tag = 'maixin flutter';

  int appId =12323; //在腾讯云实时音频注册的应用id
  String secret_key = '';//在腾讯云实时音频注册的应用获取到的secret_key

  static const BasicMessageChannel _messageChannel = const BasicMessageChannel('tencent_trtc_enter', StringCodec());
  static const BasicMessageChannel _messageChannel2 = const BasicMessageChannel('tencent_trtc_exit', StringCodec());


  Future<String> _handleMessage(message) async {
    print(tag+'收到native的消息${message}');
  }

  Future<String> _handleMessage2(message) async {
    print(tag+'收到native的消息${message}');
  }


  @override
  void initState() {
    super.initState();
    initPlatformState();
    _messageChannel.setMessageHandler(_handleMessage);
    _messageChannel2.setMessageHandler(_handleMessage2);
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      platformVersion = await TencentTrtc.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

//申请权限
  Future requestPermission(BuildContext context) async {
    await PermissionHandler().requestPermissions([PermissionGroup.storage,PermissionGroup.microphone,PermissionGroup.storage,]);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Container(
          child:Column(children: <Widget>[
            SizedBox(height: 20,),
            GestureDetector(
              onTap: (){
                print(tag+"初始化");
                requestPermission(context);
                TencentTrtc.registerTrtc();

              },
              child: Text('初始化',style: TextStyle(fontSize: 30)),),
            SizedBox(height: 20,),

            GestureDetector(
              onTap: (){
                print(tag+"离开房间");
                TencentTrtc.exitRoom();

              },
              child: Text('离开房间',style: TextStyle(fontSize: 30)),),
            SizedBox(height: 20,),
            GestureDetector(
              onTap: (){
                print(tag+"打开麦克风");
                TencentTrtc.startLocalAudio();

              },
              child: Text('打开麦克风',style: TextStyle(fontSize: 30)),),
            SizedBox(height: 20,),
            GestureDetector(
              onTap: (){
                print(tag+"关闭麦克风");
                TencentTrtc.stopLocalAudio();
              },
              child: Text('关闭麦克风',style: TextStyle(fontSize: 30)),),
            SizedBox(height: 20,),
            GestureDetector(
              onTap: (){
                print(tag+"进入房间");
                TencentTrtc.enterRoom(roomId: 123456,user_id: "user_id",appId: 1400376695,secret_key: "35cd88805babafbbee7577f965441566a9b7346bb4ea5754f14d36322b755d4e");

              },
              child: Text('进入房间',style: TextStyle(fontSize: 30),),),
            SizedBox(height: 20,),
          ],),
        ),
      ),
    );
  }
}
