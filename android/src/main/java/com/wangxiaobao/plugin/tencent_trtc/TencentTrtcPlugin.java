package com.wangxiaobao.plugin.tencent_trtc;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.common.StringCodec;

/** TencentTrtcPlugin */
public class TencentTrtcPlugin implements FlutterPlugin, MethodCallHandler {

  private MethodChannel channel;
    private final static String TAG = "TencentTrtcPlugin";
    private static BasicMessageChannel tencent_trtc_enter;


     TRTCCloud mTRTCCloud;
    static Context context;

  @Override
  public void onAttachedToEngine( FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "tencent_trtc");
    channel.setMethodCallHandler(this);
      context = flutterPluginBinding.getApplicationContext();
      tencent_trtc_enter = new BasicMessageChannel(flutterPluginBinding.getBinaryMessenger(), "tencent_trtc_android", StringCodec.INSTANCE);
      Log.d(TAG,"onAttachedToEngine");
  }


  public static void registerWith(Registrar registrar) {

    final MethodChannel channel = new MethodChannel(registrar.messenger(), "tencent_trtc");
    channel.setMethodCallHandler(new TencentTrtcPlugin());
      tencent_trtc_enter = new BasicMessageChannel(registrar.messenger(), "tencent_trtc_enter_android", StringCodec.INSTANCE);
      context = registrar.activeContext();
      Log.d(TAG,"registerWith");
  }




  @Override
  public void onMethodCall( MethodCall call,  Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else if (call.method.equals("registerTrtc")){
        registerTrtc();

    }else if (call.method.equals("enterRoom")){
        int roomId = call.argument("roomId");
        String user_id = call.argument("user_id");
        int appId = call.argument("appId");
        String userSig = call.argument("userSig");
        enterRoom(roomId,user_id,appId,userSig);
    }else if (call.method.equals("exitRoom")){
        exitRoom();

    }else if (call.method.equals("startLocalAudio")){
        startLocalAudio();
    }else if (call.method.equals("stopLocalAudio")){
        stopLocalAudio();
    }else {
        result.notImplemented();
    }
  }



    //初始化
    private void registerTrtc() {
        Log.d(TAG,"registerTrtc");
        mTRTCCloud = TRTCCloud.sharedInstance(context);
        initConfig();
    }

    //进入聊天房间
    private void enterRoom(int roomId,String user_id,int appId,String userSig) {
        Log.d(TAG,"enterRoom  roomId  : "+roomId  +"   user_id  : "+user_id +"   appId : "+appId  +"   userSig : "+userSig);
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = appId;
        trtcParams.userId = user_id;
        trtcParams.roomId = roomId;
        trtcParams.userSig = userSig;
        trtcParams.role = TRTCCloudDef.TRTCRoleAnchor;

        mTRTCCloud.enterRoom(trtcParams,TRTCCloudDef.TRTC_APP_SCENE_VOICE_CHATROOM);

    }
    //离开聊天房间
    private void exitRoom() {
        Log.d(TAG,"exitRoom");
        mTRTCCloud.exitRoom();

    }
    //开启麦克风
    private void startLocalAudio() {
        Log.d(TAG,"startLocalAudio");
        mTRTCCloud.muteLocalAudio(false);
    }

    //关闭麦克风
    private void stopLocalAudio() {
        Log.d(TAG,"stopLocalAudio");
        mTRTCCloud.muteLocalAudio(true);
    }


    @Override
  public void onDetachedFromEngine( FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

    private void initConfig() {
        mTRTCCloud.enableAudioVolumeEvaluation(800);
        mTRTCCloud.setListener(mChatRoomTRTCListener);
        mTRTCCloud.startLocalAudio();
        mTRTCCloud.muteAllRemoteAudio(false);
        enable16KSampleRate(true);
    }

    /**
     * 声音采样率
     *
     * @param enable true 开启16k采样率 false 开启48k采样率
     */
    public void enable16KSampleRate(boolean enable) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("api", "setAudioSampleRate");
            JSONObject params = new JSONObject();
            params.put("sampleRate", enable ? 16000 : 48000);
            jsonObject.put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 用于监听TRTC事件
     */
    private TRTCCloudListener mChatRoomTRTCListener = new TRTCCloudListener() {
        @Override
        public void onExitRoom(int reason) {
            super.onExitRoom(reason);

            Log.d(TAG,"onExitRoom   : "+reason);
//            tencent_trtc_exit.send("通信成功   onExitRoom  离开房间  "+reason);
        }

        @Override
        public void onSwitchRole(int errCode, String errMsg) {

        }

        @Override
        public void onEnterRoom(long result) {
            Log.d(TAG,"onEnterRoom   : "+result);
            // 处理现在是否播放声音
            if (result > 0) {
                tencent_trtc_enter.send("0");
            }else {
                tencent_trtc_enter.send("1");
            }
            startLocalAudio();
        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {

            Log.d(TAG,"进房失败  ： "+errCode+"  ---   "+ errMsg );
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            Log.d(TAG,"通信成功  onEnterRoom 有人进房了"+userId );
            //tencent_trtc_enter.send(userId );
        }

        @Override
        public void onUserAudioAvailable(String userId, boolean available) {

        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            Log.d(TAG,"onRemoteUserLeaveRoom   : "+userId +"     "+reason);
            //tencent_trtc_enter.send("通信成功   onRemoteUserLeaveRoom  有人进房了"+userId );
        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
            if (userVolumes!=null&&userVolumes.size()>0){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("action","voiceVolume");
                    JSONArray jsonArray = new JSONArray();
                    for (TRTCCloudDef.TRTCVolumeInfo volumeInfo:userVolumes){
                        JSONObject object = new JSONObject();
                        object.put("userId",volumeInfo.userId);
                        object.put("volume",volumeInfo.volume);
                        jsonArray.put(object);
                    }
                    jsonObject.put("result",jsonArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"onUserVoiceVolume  :  "+jsonObject.toString());
            }
        }

        @Override
        public void onAudioEffectFinished(int effectId, int code) {

        }
    };


}
