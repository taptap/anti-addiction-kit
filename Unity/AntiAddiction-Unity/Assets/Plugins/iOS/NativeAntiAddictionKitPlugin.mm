﻿#import <AntiAddictionService/AntiAddictionService-Swift.h>

char const *GAME_OBJECT = "PluginBridge";

@interface Utility : NSObject
@end

@implementation Utility

+ (NSString *)dictonaryToJson:(NSDictionary *) dictionary {
    NSError* error;

    NSData* jsonData = [NSJSONSerialization dataWithJSONObject:dictionary options:0 error:&error];
    if (!jsonData) {
        NSLog(@"Dictonary stringify error: %@", error);
        return @"";
    }
    return [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
}

+ (NSDictionary *)dictionaryWithJsonString:(NSString *) jsonString {
    if (jsonString == nil) return nil;
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                            options:NSJSONReadingMutableContainers
                            error:&err];
    if (err) {
        NSLog(@"json解析失败：%@", err);
        return nil;
    }
    return dic;
}

@end

@interface NativeAntiAddictionKitPlugin : NSObject<AntiAddictionServiceCallback>
@end

@implementation NativeAntiAddictionKitPlugin

static NativeAntiAddictionKitPlugin *_sharedInstance;

+(NativeAntiAddictionKitPlugin*)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _sharedInstance = [[NativeAntiAddictionKitPlugin alloc] init];
    });
    return _sharedInstance;
}

-(id)init
{
    self = [super init];
    if (self)
        [self initHelper];
    return self;
}

-(void)initHelper
{
    NSLog(@"Initialized NativeAntiAddictionKitPlugin class");
}

-(NSString *)generateUnityUnifyExtras:(NSDictionary *) extras {
    NSMutableDictionary* result = [[NSMutableDictionary alloc] init];
    if (extras) {
        if (extras[@"title"]) {
            [result setObject:extras[@"title"] forKey:@"title"];
        }
        if (extras[@"description"]) {
            [result setObject:extras[@"description"] forKey:@"description"];
        }
        if (extras[@"restrictType"]) {
            [result setObject:extras[@"userType"] forKey:@"userType"];
        }
        if (extras[@"remainTime"]) {
            [result setObject:[NSString stringWithFormat:@"%@",extras[@"remainTime"]] forKey:@"remaining_time_str"];
        }
        if (extras[@"restrictType"]) {
            [result setObject:[NSString stringWithFormat:@"%@",extras[@"restrictType"]] forKey:@"strict_type"];
        }
    }
    
    return [Utility dictonaryToJson:result];
}


-(NSString *)generateResultMessage:(NSInteger)code extras:(NSDictionary *) extras
{
    NSDictionary* result = [[NSDictionary alloc] initWithObjectsAndKeys:
                            [NSNumber numberWithUnsignedLong:code],@"code"
                            ,[self generateUnityUnifyExtras:extras], @"extras"
                            , nil];
    return [Utility dictonaryToJson:result];
}

#pragma mark - delegate
- (void)onCallbackWithCode:(NSInteger)code extra:(NSString * _Nullable)extra {
    NSString *resultString = [NSString stringWithFormat:@"callback code:%ld,extra:%@",(long)code,extra];
    NSLog(@"%@", resultString);
    NSDictionary* extraDict = [Utility dictionaryWithJsonString:extra];
    
    if (code == AntiAddictionServiceResultLoginSuccess) {
        UnitySendMessage(GAME_OBJECT, [@"HandleAntiAddictionCallbackMsg" UTF8String], [[self generateResultMessage:500 extras:extraDict]
                                                                                       UTF8String]);
    } else if (code == AntiAddictionServiceResultOpenAlertTip) {
        
        NSLog(@"dict:%@", extraDict);
        if (extraDict) {
            NSInteger extraSource = [[extraDict objectForKey:@"extraSource"] intValue];
            NSInteger remainTime = [[extraDict objectForKey:@"remainTime"] intValue];
            
            NSLog(@"%ld %ld", (long)extraSource, (long)remainTime);
            
            // 登录时提示
            if (extraSource == 3) {
                // if login success &&
                if (remainTime > 0) {
                    UnitySendMessage(GAME_OBJECT, [@"HandleAntiAddictionCallbackMsg" UTF8String]
                                                , [[self generateResultMessage:500 extras:extraDict]
                                                   UTF8String]);
                } else {
                    UnitySendMessage(GAME_OBJECT, [@"HandleAntiAddictionCallbackMsg" UTF8String]
                                                , [[self generateResultMessage:1030 extras:extraDict]
                                                   UTF8String]);
                }
            } else {
                UnitySendMessage(GAME_OBJECT, [@"HandleAntiAddictionCallbackMsg" UTF8String]
                                 , [[self generateResultMessage:1030 extras:extraDict]
                                    UTF8String]);
            }
        }
    }
}

@end

extern "C"
{
    void initSDK(const char *gameIdentifier, bool useTimeLimit, bool usePaymentLimit) {
        
        NSString *gameParam = [NSString stringWithUTF8String:gameIdentifier];
        NSLog(@"%@", [NSString stringWithFormat:@"initSDK with gameIdentifier: %@ ，useTimeLimit: %@, usePaymentLimit: %@"
        , @"demo"
        , useTimeLimit?@"YES":@"NO"
        , usePaymentLimit?@"YES":@"NO"]);

        [AntiAddictionService setHost:@"http://172.19.56.86:7005"];
        [AntiAddictionService setIdentifyHost:@"http://172.19.101.76"];

        //    [AntiAddictionService setWebsocketAddress:@""];

        [AntiAddictionService setFunctionConfig:useTimeLimit :usePaymentLimit];
        [AntiAddictionService init:[NativeAntiAddictionKitPlugin sharedInstance] gameIdentifier:gameParam];
    }

    void login(const char *userId) {
        NSString *userIdParam = [NSString stringWithUTF8String:userId];
        [AntiAddictionService login:userIdParam];
    }

    void enterGame() {
        [AntiAddictionService enterGame];
    }

    void leaveGame() {
        [AntiAddictionService leaveGame];
    }
}
