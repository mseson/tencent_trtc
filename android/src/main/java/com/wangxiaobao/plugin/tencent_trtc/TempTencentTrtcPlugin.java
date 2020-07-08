package com.wangxiaobao.plugin.tencent_trtc;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StringCodec;

public class TempTencentTrtcPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler {

    private MethodChannel channel;
    private final static String TAG = "maixin Plugin~1";
    private static BasicMessageChannel tencentTrtcMessage;

    TRTCCloud mTRTCCloud;
    static Context context;

    @Override
    public void onAttachedToEngine( FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "tencent_trtc");
        channel.setMethodCallHandler(this);
//      mTRTCCloud = TRTCCloud.sharedInstance(flutterPluginBinding.getApplicationContext());
        context = flutterPluginBinding.getApplicationContext();
        tencentTrtcMessage = new BasicMessageChannel(flutterPluginBinding.getBinaryMessenger(), "tencent_trtc_message", StringCodec.INSTANCE);

        Log.d(TAG,"onAttachedToEngine");
    }




    @Override
    public void onMethodCall(MethodCall call, MethodChannel.Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("registerTrtc")){
            registerTrtc();

        }else if (call.method.equals("enterRoom")){
            int roomId = call.argument("roomId");
            String user_id = call.argument("user_id");
            int appId = call.argument("appId");
            String secret_key = call.argument("secret_key");
            enterRoom(roomId,user_id,appId,secret_key);
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
    private void enterRoom(int roomId,String user_id,int appId,String secret_key) {
        Log.d(TAG,"enterRoom  roomId  : "+roomId  +"   user_id  : "+user_id +"   appId : "+appId  +"   secret_key : "+secret_key);
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = appId;
        trtcParams.userId = user_id;
        trtcParams.roomId = roomId;
        trtcParams.userSig = TrtcUtils.genTestUserSig(user_id,appId,secret_key);
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
        mTRTCCloud.startLocalAudio();
    }

    //关闭麦克风
    private void stopLocalAudio() {
        Log.d(TAG,"stopLocalAudio");
        mTRTCCloud.stopLocalAudio();
    }


    @Override
    public void onDetachedFromEngine( FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    private void initConfig() {
        mTRTCCloud.enableAudioVolumeEvaluation(800);
        mTRTCCloud.setListener(mChatRoomTRTCListener);
        mTRTCCloud.startLocalAudio();
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

        }

        @Override
        public void onSwitchRole(int errCode, String errMsg) {

        }

        @Override
        public void onEnterRoom(long time) {
            Log.d(TAG,"onEnterRoom   : "+time);
            String result ="1";
            if (time > 0) {
                result = "0";
//                tencent_trtc_enter.send("0");
            }else {
                result ="1";
//                tencent_trtc_enter.send("1");
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("action","enterRoom");

                jsonObject.put("result",result);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            tencentTrtcMessage.send(jsonObject.toString());


        }

        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {

            Log.d(TAG,"进房失败  ： "+errCode+"  ---   "+ errMsg );
        }

        @Override
        public void onRemoteUserEnterRoom(String userId) {
            Log.d(TAG,"通信成功  onEnterRoom 有人进房了"+userId );


        }

        @Override
        public void onUserAudioAvailable(String userId, boolean available) {

        }

        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            Log.d(TAG,"onRemoteUserLeaveRoom   : "+userId +"     "+reason);
            tencentTrtcMessage.send("通信成功   onRemoteUserLeaveRoom  有人进房了"+userId );
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
                tencentTrtcMessage.send(jsonObject.toString());
            }
        }

        @Override
        public void onAudioEffectFinished(int effectId, int code) {

        }
    };

}
