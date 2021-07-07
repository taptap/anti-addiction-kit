#import <Foundation/Foundation.h>
#import "AntiAddictionAsyncHttp.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *ANTI_ADDICTION_HTTP_FINISH_NOTIFICATION;

@interface AntiAddictionHttpManager : NSObject

+ (NSInteger)httpTaskIndex;

+ (void)addHttpTask:(AntiAddictionAsyncHttp *)httpTask index:(NSInteger)taskIndex;
@end

NS_ASSUME_NONNULL_END
