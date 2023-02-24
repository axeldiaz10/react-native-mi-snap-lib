#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(MiSnapLib, NSObject)

RCT_EXTERN_METHOD(openCamera:(NSString)type withLicense:(NSString)license
                 withLanguage:(NSString)language
                 withResolver:(RCTPromiseResolveBlock)resolve
                 withRejecter:(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(setLanguage:(NSString)language)

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

@end
