#import "TencentTrtcPlugin.h"
#if __has_include(<tencent_trtc/tencent_trtc-Swift.h>)
#import <tencent_trtc/tencent_trtc-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "tencent_trtc-Swift.h"
#endif

@implementation TencentTrtcPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftTencentTrtcPlugin registerWithRegistrar:registrar];
}
@end
