#import "AntiAddictionHttpResult.h"

@implementation AntiAddictionHttpResult
- (void)setData:(NSData *)data {
    _data = data;
    [self setResultDic:[self dictionaryFromJsonData:data]];
}

- (NSDictionary *)dictionaryFromJsonData:(NSData *)data {
    if (!data) {
        return nil;
    }
    
    id result = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
    if ([result isKindOfClass:[NSDictionary class]]) {
        return result;
    }
    
    return nil;
}

@end
