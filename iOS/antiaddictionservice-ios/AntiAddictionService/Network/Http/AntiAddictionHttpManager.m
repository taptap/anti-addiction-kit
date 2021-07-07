#import "AntiAddictionHttpManager.h"

@interface AntiAddictionHttpManager()

@property (nonatomic) NSInteger taskIndex;

@property (nonatomic) NSMutableArray *taskPool;
@property (nonatomic) NSMutableArray *taskQueue;

@end

static AntiAddictionHttpManager *_instance;
NSString *ANTI_ADDICTION_HTTP_FINISH_NOTIFICATION = @"ANTI_ADDICTION_HTTP_FINISH_NOTIFICATION";
@implementation AntiAddictionHttpManager
// 并行任务数量
+ (NSInteger)concurrentTaskNumber {
    return 1;
}

+ (AntiAddictionHttpManager *)shareInstance {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        _instance = nil;
        _instance = [[AntiAddictionHttpManager alloc] init];
    });
    
    return _instance;
}

- (id)init {
    self = [super init];
    if (self) {
        self.taskIndex = 0;
        self.taskQueue = [NSMutableArray array];
        self.taskPool = [NSMutableArray array];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(removeTask:) name:ANTI_ADDICTION_HTTP_FINISH_NOTIFICATION object:nil];
    }
    
    return self;
}

- (void)removeTask:(NSNotification *)notification {
    NSNumber *taskIndex = notification.object;
    if (!taskIndex) {
        return;
    }
    @synchronized ([AntiAddictionHttpManager shareInstance]) {
        for (AntiAddictionAsyncHttp *httpTask in [AntiAddictionHttpManager shareInstance].taskPool) {
            if ([httpTask httpTaskIdentify] == [taskIndex integerValue]) {
                [[AntiAddictionHttpManager shareInstance].taskPool removeObject:httpTask];
            }
        }
        
        [AntiAddictionHttpManager addNextTask];
    }
}

+ (void)addHttpTask:(AntiAddictionAsyncHttp *)httpTask index:(NSInteger)taskIndex {
    if (!httpTask) {
        return;
    }
    @synchronized ([AntiAddictionHttpManager shareInstance]) {
        [[AntiAddictionHttpManager shareInstance].taskQueue addObject:httpTask];
        [AntiAddictionHttpManager addNextTask];
    }
}

+ (void)addNextTask {
    AntiAddictionHttpManager *httpManager = [AntiAddictionHttpManager shareInstance];
    if (httpManager.taskPool.count >= [AntiAddictionHttpManager concurrentTaskNumber]) {
        return;
    }
    
    if (httpManager.taskQueue.count <= 0) {
        return;
    }
    
    AntiAddictionAsyncHttp *nextTask = httpManager.taskQueue.firstObject;
    [httpManager.taskPool addObject:nextTask];
    [httpManager.taskQueue removeObject:nextTask];
    [nextTask startTask];
}

+ (NSInteger)httpTaskIndex {
    return [AntiAddictionHttpManager shareInstance].taskIndex++;
}

@end
