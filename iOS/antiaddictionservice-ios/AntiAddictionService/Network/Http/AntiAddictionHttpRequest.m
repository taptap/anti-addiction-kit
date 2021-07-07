#import "AntiAddictionHttpRequest.h"

@implementation AntiAddictionHttpRequest

+ (NSString *)urlEncodedString:(NSString *)string
{
    NSString *unencodedString = string;
    NSString *encodedString = (NSString *)
    CFBridgingRelease(CFURLCreateStringByAddingPercentEscapes(kCFAllocatorDefault,
                                                              (CFStringRef)unencodedString,
                                                              NULL,
                                                              (CFStringRef)@"!*'();:@&=+$,/?%#[]",
                                                              kCFStringEncodingUTF8));
    
    return encodedString;
}

//! GET参数拼接
+ (NSString *)connectUrl:(NSString *)url params:(NSDictionary *)params encode:(BOOL)encode {
     if (!params || params.count == 0) {
         return url;
     }
     
     // 初始化参数变量
    __block NSString *str = @"?";
     if ([url containsString:@"?"]) {
         str = @"&";
     }
     
     // 快速遍历参数数组
     [params enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
         NSString *strObj = @"";
         if ([obj isKindOfClass:[NSNumber class]]) {
             strObj = [NSString stringWithFormat:@"%@",(NSNumber *)obj];
             str = [str stringByAppendingString:key];
             str = [str stringByAppendingString:@"="];
             str = [str stringByAppendingString:strObj];
             str = [str stringByAppendingString:@"&"];
         }else if ([obj isKindOfClass:[NSDictionary class]]){
             strObj = [self paramStringForDic:(NSDictionary *)obj];
             if (strObj && strObj.length > 0) {
                 str = [str stringByAppendingString:strObj];
                 str = [str stringByAppendingString:@"&"];
             }
         }else {
             NSString *keyStr = [NSString stringWithFormat:@"%@=",key];
             if (![str containsString:keyStr] && ![url containsString:keyStr]) {
                 strObj = obj;
                 str = [str stringByAppendingString:keyStr];
                 str = [str stringByAppendingString:encode?[self urlEncodedString:strObj]:strObj];
                 str = [str stringByAppendingString:@"&"];
             }
         }
     }];

     // 处理多余的&以及返回含参url
     if (str.length > 1) {
         // 去掉末尾的&
         str = [str substringToIndex:str.length - 1];
         // 返回含参url
         url = [url stringByAppendingString:str];
//         url = [url stringByAppendingString:@"/"];
         return url;
     }
     
     return url;
}
+ (NSString *)connectUrl:(NSString *)url params:(NSDictionary *)params {
    return [self connectUrl:url params:params encode:YES];
}

// POST请求参数拼接
+ (NSString *)postStringWithParams:(NSDictionary *)params {
    return [self paramStringForDic:params];
}

+ (NSString *)cookieStringForUrl:(NSString *)url {
    NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookiesForURL:[NSURL URLWithString:url]];
    __block NSString *cookiedStr = @"";
    if (cookies.count > 0) {
        [cookies enumerateObjectsUsingBlock:^(NSHTTPCookie *oneCookie, NSUInteger idx, BOOL * _Nonnull stop) {
            if (cookiedStr.length > 0) {
                cookiedStr = [cookiedStr stringByAppendingString:[NSString stringWithFormat:@";%@=%@",oneCookie.name,oneCookie.value]];
            }else {
                cookiedStr = [NSString stringWithFormat:@"%@=%@",oneCookie.name,oneCookie.value];
            }
        }];
    }
    
    return cookiedStr;
}

+ (NSString *)paramStringForDic:(NSDictionary *)paramsDic {
    if (!paramsDic || paramsDic.count <= 0) {
        return nil;
    }
    
    // 初始化参数变量
    __block NSString *str = @"";
    
    [paramsDic enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        NSString *strObj = @"";
        if ([obj isKindOfClass:[NSNumber class]]) {
            strObj = [NSString stringWithFormat:@"%@",(NSNumber *)obj];
        }else {
            strObj = obj;
        }
        
        if ([obj isKindOfClass:[NSDictionary class]]){
            strObj = [self paramStringForDic:(NSDictionary *)obj];
            if (strObj && strObj.length > 0) {
                str = [str stringByAppendingString:strObj];
                str = [str stringByAppendingString:@"&"];
            }
        }else {
            str = [str stringByAppendingString:key];
            str = [str stringByAppendingString:@"="];
            str = [str stringByAppendingString:[self urlEncodedString:strObj]];
            str = [str stringByAppendingString:@"&"];
        }
    }];
    
    // 处理多余的&以及返回含参url
    if (str.length > 1) {
        // 去掉末尾的&
        str = [str substringToIndex:str.length - 1];
    }
    return str;
}

@end
