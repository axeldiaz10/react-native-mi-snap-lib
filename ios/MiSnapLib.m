#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(MiSnapLib, NSObject)

RCT_EXTERN_METHOD(openCamera:(NSString)type withLicense:(NSString)license
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

@end
