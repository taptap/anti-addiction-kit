#import "AntiAddictionAsyncHttp.h"
#import "AntiAddictionHttpRequest.h"
#import "AntiAddictionHttpManager.h"

NSString *const ANTI_TIMEOUTKEY = @"timeoutInterval";
NSString *const ANTI_HTTPMETHODKEY = @"HTTPMethod";
NSString *const ANTI_HTTPBODYKEY = @"HTTPBody";
NSString *const ANTI_DATAFORMAT = @"ANTI_DATAFORMAT";
NSString *const ANTI_CACHE_POLICY_KEY = @"ANTI_CACHE_POLICY_KEY";

/**
 header
 */
NSString *const ANTI_AUTH_KEY = @"Authorization";



@interface AntiAddictionAsyncHttp ()<NSURLSessionDelegate>

@property (nonatomic) NSURLSessionTask *task;
@property (nonatomic) AntiAddictionAsyncHttp *httpSelf;
@property (nonatomic) NSInteger taskIdentify;

@end

@implementation AntiAddictionAsyncHttp
- (NSInteger)httpTaskIdentify {
    return self.taskIdentify;
}

- (void)startTask {
    if (self.task) {
        [self.task resume];
    }
}
- (void)stopTask {
    self.httpSelf = nil;
    
    if(self.task) {
        [self.task cancel];
    }
    
    if(self.callBackBlock) {
        self.callBackBlock = nil;
    }
}

- (void)retryTask {
    [self.task resume];
}

+ (NSString *)getRequestUrl:(NSString *)url params:(NSDictionary *)params{
    NSString *realUrl = url;
    NSMutableDictionary *regulerParams = [NSMutableDictionary dictionary];
    if (params && params.count > 0) {
        [regulerParams addEntriesFromDictionary:params];
    }
    realUrl = [AntiAddictionHttpRequest connectUrl:realUrl params:regulerParams];
    return realUrl;
}

#pragma mark - Request POST/GET
// GET
+ (void)httpGet:(NSString *)urlStr
            requestParams:(NSDictionary *)requestParams
             customHeader:(NSDictionary *)customHeaderParams
                   params:(NSDictionary *)params
                 callBack:(CallBackBlock)callBackBlock failedCallback:(CallBackBlock)failedCallback{

    NSString *realUrl = [AntiAddictionAsyncHttp getRequestUrl:urlStr params:params];
    
    __block AntiAddictionAsyncHttp *http = [[[self class] alloc] init];
    http.callBackBlock = callBackBlock;
    http.failedCallback = failedCallback;
    
    NSURL *url = [NSURL URLWithString:realUrl];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    [request setHTTPMethod:@"GET"];
    if ([requestParams objectForKey:ANTI_TIMEOUTKEY]) {
        NSNumber *timeOut = [requestParams objectForKey:ANTI_TIMEOUTKEY];
        [request setTimeoutInterval:[timeOut doubleValue]];
    }else {
        [request setTimeoutInterval:15];
    }
    
    if ([requestParams objectForKey:ANTI_AUTH_KEY]) {
        [request setValue:[requestParams objectForKey:ANTI_AUTH_KEY] forHTTPHeaderField:ANTI_AUTH_KEY];
    }
    
    [customHeaderParams enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        [request addValue:obj forHTTPHeaderField:key];
    }];
    
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    
    __weak typeof (http) weakHttp = http;
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    if ([requestParams objectForKey:ANTI_CACHE_POLICY_KEY]) {
        configuration.requestCachePolicy = [[requestParams objectForKey:ANTI_CACHE_POLICY_KEY] integerValue];
    }else {
        configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalAndRemoteCacheData;
    }
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        
        AntiAddictionHttpResult *result = [[AntiAddictionHttpResult alloc] init];
        [result setData:data];
        [result setResponse:response];
        [result setError:error];
        [result setOriginUrl:urlStr];

        dispatch_async(dispatch_get_main_queue(), ^{
            if (!error && ([(NSHTTPURLResponse *)response statusCode] >= 200 && [(NSHTTPURLResponse *)response statusCode] < 300)) {
                // success
                [weakHttp handleSuccessResult:result];
            }else {
                // fail
                [weakHttp handleFailResult:result];
            }
            [[NSNotificationCenter defaultCenter] postNotificationName:ANTI_ADDICTION_HTTP_FINISH_NOTIFICATION object:@(weakHttp.taskIdentify)];
        });
        
    }];
    
//    [dataTask resume];
    http.task = dataTask;
    NSInteger httpTaskIndex = [AntiAddictionHttpManager httpTaskIndex];
    http.taskIdentify = httpTaskIndex;
    [AntiAddictionHttpManager addHttpTask:http index:httpTaskIndex];
    
//    return http;
}

// POST

+ (void)httpPost:(NSString *)urlStr
             requestParams:(NSDictionary *)requestParams
              customHeader:(NSDictionary *)customHeaderParams
                    params:(NSDictionary *)params
                  callBack:(CallBackBlock)callBackBlock
  failedCallback:(CallBackBlock)failedCallback {
    [AntiAddictionAsyncHttp httpPost:urlStr requestParams:requestParams customHeader:customHeaderParams params:params paramsJson:nil callBack:callBackBlock failedCallback:failedCallback];
}

+ (void)httpPost:(NSString *)urlStr
             requestParams:(NSDictionary *)requestParams
              customHeader:(NSDictionary *)customHeaderParams
                    params:(NSDictionary *)params
                paramsJson:(NSString *)paramsJson
                  callBack:(CallBackBlock)callBackBlock
            failedCallback:(CallBackBlock)failedCallback{

    NSString *realUrl = urlStr;

    AntiAddictionAsyncHttp *http = [[[self class] alloc] init];
    http.callBackBlock = callBackBlock;
    http.failedCallback = failedCallback;
    
    NSURL *url = [NSURL URLWithString:realUrl];
    NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    [request setHTTPMethod:@"POST"];
    [request setTimeoutInterval:15];
    // 默认json
    [request setValue:@"application/json;charset=utf-8" forHTTPHeaderField:@"Content-Type"];
    
    // 参数
    NSMutableDictionary *newParams = [NSMutableDictionary dictionary];
    if (params && params.count > 0) {
        [newParams addEntriesFromDictionary:params];
    }
    
    if (newParams.count > 0) {
        NSString *paramsStr = paramsJson?:[AntiAddictionAsyncHttp jsonString:newParams];
        [request setHTTPBody:[paramsStr dataUsingEncoding:NSUTF8StringEncoding]];
    }   
    
    if ([requestParams objectForKey:ANTI_TIMEOUTKEY]) {
        NSNumber *timeOut = [requestParams objectForKey:ANTI_TIMEOUTKEY];
        [request setTimeoutInterval:[timeOut doubleValue]];
    }
    
    if ([requestParams objectForKey:ANTI_DATAFORMAT] && [[params objectForKey:ANTI_DATAFORMAT] isEqualToString:@"form"]) {
        [request setValue:@"application/x-www-form-urlencoded;charset=utf-8" forHTTPHeaderField:@"Content-Type"];
        NSString *paramsStr = [AntiAddictionHttpRequest postStringWithParams:[params objectForKey:ANTI_HTTPBODYKEY]];
        [request setHTTPBody:[paramsStr dataUsingEncoding:NSUTF8StringEncoding]];
    }
    
    if ([requestParams objectForKey:ANTI_AUTH_KEY]) {
        [request setValue:[requestParams objectForKey:ANTI_AUTH_KEY] forHTTPHeaderField:ANTI_AUTH_KEY];
    }
    
    [customHeaderParams enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        [request addValue:obj forHTTPHeaderField:key];
    }];
    
    __weak typeof (http) weakHttp = http;
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    if ([requestParams objectForKey:ANTI_CACHE_POLICY_KEY]) {
        configuration.requestCachePolicy = [[requestParams objectForKey:ANTI_CACHE_POLICY_KEY] integerValue];
    }else {
        configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalAndRemoteCacheData;
    }
    NSURLSessionDataTask *dataTask = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        
        AntiAddictionHttpResult *result = [[AntiAddictionHttpResult alloc] init];
        [result setData:data];
        [result setResponse:response];
        [result setError:error];
        [result setOriginUrl:urlStr];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (!error && [(NSHTTPURLResponse *)response statusCode] == 200) {
                // success
                [weakHttp handleSuccessResult:result];
            }else {
                // fail
                [weakHttp handleFailResult:result];
            }
            
            [[NSNotificationCenter defaultCenter] postNotificationName:ANTI_ADDICTION_HTTP_FINISH_NOTIFICATION object:@(weakHttp.taskIdentify)];
        });
        
    }];
    
//    [dataTask resume];
    http.task = dataTask;
    NSInteger httpTaskIndex = [AntiAddictionHttpManager httpTaskIndex];
    http.taskIdentify = httpTaskIndex;
    [AntiAddictionHttpManager addHttpTask:http index:httpTaskIndex];
    
//    return http;
}

+ (void)httpGetAll:(NSArray *)urlStrArr
  requestParamsArr:(NSArray *)requestParamsArr
  customHeadersArr:(NSArray *)customHeaderParamsArr
            params:(NSArray *)paramsDicArr
          callback:(GetAllCallBack)callback{
    if (!urlStrArr || urlStrArr.count <= 0) {
        if (callback) {
            callback(nil,NO);
        }
        return;
    }
    
    // 初始化保存的数据
    __block NSMutableArray *resultDataArr = [NSMutableArray array];
    for (NSInteger index = 0; index < urlStrArr.count; index ++) {
        [resultDataArr addObject:@{}];
    }
    
    dispatch_group_t downloadGroup = dispatch_group_create();
    
    [urlStrArr enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *requestParams = [requestParamsArr objectAtIndex:idx];
        NSDictionary *params = [paramsDicArr objectAtIndex:idx];
        NSDictionary *customHeaders = [customHeaderParamsArr objectAtIndex:idx];
        dispatch_group_enter(downloadGroup);
        [self httpGet:obj requestParams:requestParams customHeader:customHeaders params:params callBack:^(AntiAddictionHttpResult *result) {
            // success
            NSDictionary *dataDic = result.resultDic;
            [resultDataArr replaceObjectAtIndex:idx withObject:dataDic?:@{}];
            dispatch_group_leave(downloadGroup);
        } failedCallback:^(AntiAddictionHttpResult *result) {
            // 失败
            dispatch_group_leave(downloadGroup);
        }];
    }];
    
    
    dispatch_group_notify(downloadGroup, dispatch_get_main_queue(), ^{
        BOOL success = YES;
        for (NSDictionary *dataDic in resultDataArr) {
            if (dataDic.allKeys.count <= 0) {
                success = NO;
            }
        }
        if (callback) {
            callback(resultDataArr,success);
        }
    });
}

#pragma mark - result handler
- (void)handleSuccessResult:(AntiAddictionHttpResult *)result {
    if (self.callBackBlock) {
        self.callBackBlock(result);
    }
}

- (void)handleFailResult:(AntiAddictionHttpResult *)result {
    if (self.failedCallback) {
        self.failedCallback(result);
    }
}

#pragma mark - json
+ (NSString *)jsonString:(NSDictionary *)dictionary {
    NSError *error = nil;
    NSData *jsonData = nil;
    if (!dictionary) {
        return nil;
    }
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    [dictionary enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, id  _Nonnull obj, BOOL * _Nonnull stop) {
        NSString *keyString = nil;
        NSString *valueString = nil;
        if ([key isKindOfClass:[NSString class]]) {
            keyString = key;
        }else{
            keyString = [NSString stringWithFormat:@"%@",key];
        }
        
        if ([obj isKindOfClass:[NSString class]] || [obj isKindOfClass:[NSArray class]] || [obj isKindOfClass:[NSDictionary class]]) {
            valueString = obj;
        }else if ([obj isKindOfClass:[NSNumber class]]){
            valueString = obj;
        }
        else{
            valueString = [NSString stringWithFormat:@"%@",obj];
        }
        
        [dict setObject:valueString forKey:keyString];
    }];
    jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    if ([jsonData length] == 0 || error != nil) {
        return nil;
    }
    NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    return jsonString;
}
@end
