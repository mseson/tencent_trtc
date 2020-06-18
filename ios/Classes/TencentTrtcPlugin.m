#import "TencentTrtcPlugin.h"



@interface TencentTrtcPlugin ()<TRTCCloudDelegate>

@property (nonatomic, strong) TRTCCloud * trtcCloud;
@property (nonatomic, strong) FlutterBasicMessageChannel * messageChannel;
@property (nonatomic, strong) FlutterBasicMessageChannel * messageChannel2;

@end


@implementation TencentTrtcPlugin

- (void)registerTrtc {
    self.trtcCloud = [TRTCCloud sharedInstance];
    self.trtcCloud.delegate = self;
    [self BasicMessageChannelFunction];
}

-(void) BasicMessageChannelFunction{
    FlutterViewController* controller = (FlutterViewController*)[UIApplication sharedApplication].keyWindow.rootViewController;
       // 初始化定义
    self.messageChannel = [FlutterBasicMessageChannel messageChannelWithName:@"tencent_trtc_enter_ios" binaryMessenger:controller];
    self.messageChannel2 = [FlutterBasicMessageChannel messageChannelWithName:@"tencent_trtc_exit" binaryMessenger:controller];
}

- (void)enterRoomWithRoomId:(NSNumber *)roomId user_id:(NSString *)user_id appId:(NSNumber *)appId secret_key:(NSString *)secret_key {
    TRTCParams * params = [[TRTCParams alloc] init];
    params.sdkAppId = [appId intValue];
    params.userId = user_id;
    params.userSig = [TencentTrtcPlugin genTestUserSig:user_id appId:[appId intValue] expiredTime:604800 secretKey:secret_key]; //线上不建议用此方法 详见方法介绍
    params.roomId = [roomId intValue];
    NSLog(@"________________ %@", params);
    [self.trtcCloud enterRoom:params appScene:TRTCAppSceneVideoCall];
}

- (void)exitRoom {
    [self.trtcCloud exitRoom];
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"tencent_trtc"
            binaryMessenger:[registrar messenger]];
  TencentTrtcPlugin* instance = [[TencentTrtcPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    NSLog(@"ios 原生 handleMethodCall %@ ", call.method);
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  }else if ([@"registerTrtc" isEqualToString:call.method]) {
      [self registerTrtc];
  } else if ([@"enterRoom" isEqualToString:call.method]) {
      NSNumber *roomId = call.arguments[@"roomId"];
      NSString *user_id = call.arguments[@"user_id"];
      NSNumber *appId = call.arguments[@"appId"];
      NSString *secret_key = call.arguments[@"secret_key"];
      [self enterRoomWithRoomId:roomId user_id:user_id appId:appId secret_key:secret_key];
  }else if ([@"exitRoom" isEqualToString:call.method]) {
      [self exitRoom];
  }else {
    result(FlutterMethodNotImplemented);
  }
}

//代理方法

- (void)onError:(TXLiteAVError)errCode errMsg:(nullable NSString *)errMsg extInfo:(nullable NSDictionary*)extInfo{
    NSLog(@"ios 原生 onError errCode : %d", errCode);
}

- (void)onEnterRoom:(NSInteger)result{
    NSLog(@"ios 原生  onEnterRoom: %ld", (long)result);
    
    [self.trtcCloud startLocalAudio];
     //NSString *msg = @0;
    [self.messageChannel sendMessage:@"0"];
    
}
- (void)onExitRoom:(NSInteger)reason{
    NSLog(@"ios 原生 onExitRoom: %ld", (long)reason);
}

//UserSig
+ (NSString *)genTestUserSig:(NSString *)identifier appId:(NSInteger)SDKAPPID expiredTime:(NSInteger)EXPIRETIME secretKey:(NSString *)SECRETKEY {
    
    CFTimeInterval current = CFAbsoluteTimeGetCurrent() + kCFAbsoluteTimeIntervalSince1970;
    long TLSTime = floor(current);
    NSMutableDictionary *obj = [@{@"TLS.ver": @"2.0",
                                  @"TLS.identifier": identifier,
                                  @"TLS.sdkappid": @(SDKAPPID),
                                  @"TLS.expire": @(EXPIRETIME),
                                  @"TLS.time": @(TLSTime)} mutableCopy];
    NSMutableString *stringToSign = [[NSMutableString alloc] init];
    NSArray *keyOrder = @[@"TLS.identifier",
                          @"TLS.sdkappid",
                          @"TLS.time",
                          @"TLS.expire"];
    for (NSString *key in keyOrder) {
        [stringToSign appendFormat:@"%@:%@\n", key, obj[key]];
    }
    NSLog(@"ios 原生 %@", stringToSign);
    //NSString *sig = [self sigString:stringToSign];
    NSString *sig = [self hmac:stringToSign SECRETKEY:SECRETKEY];

    obj[@"TLS.sig"] = sig;
    NSLog(@"ios 原生 sig: %@", sig);
    NSError *error = nil;
    NSData *jsonToZipData = [NSJSONSerialization dataWithJSONObject:obj options:0 error:&error];
    if (error) {
        NSLog(@"ios 原生 [Error] json serialization failed: %@", error);
        return @"";
    }

    const Bytef* zipsrc = (const Bytef*)[jsonToZipData bytes];
    uLongf srcLen = jsonToZipData.length;
    uLong upperBound = compressBound(srcLen);
    Bytef *dest = (Bytef*)malloc(upperBound);
    uLongf destLen = upperBound;
    int ret = compress2(dest, &destLen, (const Bytef*)zipsrc, srcLen, Z_BEST_SPEED);
    if (ret != Z_OK) {
        NSLog(@"ios 原生 [Error] Compress Error %d, upper bound: %lu", ret, upperBound);
        free(dest);
        return @"";
    }
    NSString *result = [self base64URL: [NSData dataWithBytesNoCopy:dest length:destLen]];
    return result;
}

+ (NSString *)hmac:(NSString *)plainText SECRETKEY:(NSString *)SECRETKEY
{
    const char *cKey  = [SECRETKEY cStringUsingEncoding:NSASCIIStringEncoding];
    const char *cData = [plainText cStringUsingEncoding:NSASCIIStringEncoding];

    unsigned char cHMAC[CC_SHA256_DIGEST_LENGTH];

    CCHmac(kCCHmacAlgSHA256, cKey, strlen(cKey), cData, strlen(cData), cHMAC);

    NSData *HMACData = [[NSData alloc] initWithBytes:cHMAC length:sizeof(cHMAC)];
    return [HMACData base64EncodedStringWithOptions:0];
}

+ (NSString *)base64URL:(NSData *)data
{
    NSString *result = [data base64EncodedStringWithOptions:0];
    NSMutableString *final = [[NSMutableString alloc] init];
    const char *cString = [result cStringUsingEncoding:NSUTF8StringEncoding];
    for (int i = 0; i < result.length; ++ i) {
        char x = cString[i];
        switch(x){
            case '+':
                [final appendString:@"*"];
                break;
            case '/':
                [final appendString:@"-"];
                break;
            case '=':
                [final appendString:@"_"];
                break;
            default:
                [final appendFormat:@"%c", x];
                break;
        }
    }
    return final;
}

@end
