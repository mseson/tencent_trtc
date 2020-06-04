#import "TencentTrtcPlugin.h"
#import <TXLiteAVSDK_TRTC/TRTCCloud.h>

@interface TencentTrtcPlugin : NSObject<FlutterPlugin>

@property (strong, nonatomic) TRTCCloud* trtcCloud;

@end

@implementation TencentTrtcPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"tencent_trtc"
            binaryMessenger:[registrar messenger]];
  TencentTrtcPlugin* instance = [[TencentTrtcPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if ([@"registerTrtc" isEqualToString:call.method]) {

  }else if ([@"enterRoom" isEqualToString:call.method]) {

  }else if ([@"exitRoom" isEqualToString:call.method]) {

   }else {
    result(FlutterMethodNotImplemented);
  }
}

- (void)registerTrtc() {
    self.trtcCloud = [TRTCCloud ]
}

@end
