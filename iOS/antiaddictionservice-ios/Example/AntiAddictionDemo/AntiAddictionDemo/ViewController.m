//
//  ViewController.m
//  AntiAddictionDemo
//
//  Created by JiangJiahao on 2021/1/7.
//

#import "ViewController.h"
@import AntiAddictionService;

@interface ViewController ()<AntiAddictionServiceCallback>

@property (nonatomic) UILabel    *resultLabel;

@end

static NSString *testUserId = @"792";

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [AntiAddictionService setHost:@"http://172.19.56.86:7005"];
    [AntiAddictionService setIdentifyHost:@"http://172.19.101.76"];
    
//    [AntiAddictionService setWebsocketAddress:@""];
    [AntiAddictionService setFunctionConfig:YES :YES];
    [AntiAddictionService init:self gameIdentifier:@"demo"];
    
    if (@available(iOS 11.0, *)) {
    }else {
        [self addButtons];
    }
}

- (void)viewSafeAreaInsetsDidChange {
    [super viewSafeAreaInsetsDidChange];
    NSArray *subviews = self.view.subviews;
    [subviews enumerateObjectsUsingBlock:^(UIView *subview, NSUInteger idx, BOOL * _Nonnull stop) {
        [subview removeFromSuperview];
    }];
    button_count = 0;
    [self addResultLabel];
    [self addButtons];
}

- (void)login {
    [AntiAddictionService login:testUserId];
}

- (void)logout {
    [AntiAddictionService logout];
}

- (void)enterGame {
    [AntiAddictionService enterGame];
}

- (void)leaveGame {
    [AntiAddictionService leaveGame];
}

- (void)realName {
    [AntiAddictionService realNameAuthWithUserToken:testUserId name:@"name" idCard:@"idcard" phone:@"135xxxxxx" completion:^(enum AntiAddictionRealNameAuthState identifyState, NSString * _Nonnull errorMessage) {
        [self.resultLabel setText:[NSString stringWithFormat:@"实名结果：%ld,失败原因：%@", (long)identifyState,errorMessage]];
    }];
}

- (void)checkRealname {
    [AntiAddictionService checkRealnameStateWithUserToken:testUserId completion:^(enum AntiAddictionRealNameAuthState state, NSString * _Nonnull userToken, NSString * _Nonnull idCard, NSString * _Nonnull name) {
        [self.resultLabel setText:[NSString stringWithFormat:@"实名状态：%ld,姓名：%@，身份证：%@",(long)state,name,idCard]];
    } failureHandler:^(NSString * _Nonnull errorMsg) {
        [self.resultLabel setText:errorMsg];
    }];
}

- (void) checkPayLimit {
    [AntiAddictionService checkPayLimit:10];
}

- (void) paySuccess {
    [AntiAddictionService paySuccess:10];
}

- (void)addButtons {
    [self addButton:@"登录" selector:@selector(login)];
    [self addButton:@"进入游戏" selector:@selector(enterGame)];
    [self addButton:@"离开游戏" selector:@selector(leaveGame)];
    [self addButton:@"实名认证" selector:@selector(realName)];
    [self addButton:@"检查实名" selector:@selector(checkRealname)];
    
    [self addButton:@"检查付费" selector:@selector(checkPayLimit)];
    [self addButton:@"上报付费结果" selector:@selector(paySuccess)];
    
    [self addButton:@"登出" selector:@selector(logout)];
}

static int button_count = 0;
- (void)addButton:(NSString *)title selector:(SEL)selector {
    UIEdgeInsets safeInsets = UIEdgeInsetsZero;
    if (@available(iOS 11.0, *)) {
        safeInsets = self.view.safeAreaInsets;
    }
    
    button_count++;
    static CGFloat buttonHeight = 40.0;
    static CGFloat buttonWidth = 100.0;
    static CGFloat buttonVerticalSpace = 15.0;
    static CGFloat buttonHorizonSpace = 30.0;
    // caculate
    CGSize screenSize = [UIScreen mainScreen].bounds.size;
    NSUInteger index = floor(button_count * (buttonVerticalSpace + buttonHeight) / screenSize.height);
    NSUInteger oneColumCount = floor(screenSize.height / (buttonVerticalSpace + buttonHeight));
    CGFloat buttonX = (index + 1) * (buttonWidth + buttonHorizonSpace) - buttonWidth + safeInsets.left;
    CGFloat buttonY = index > 0 ? (button_count - index * oneColumCount - 1) * (buttonHeight + buttonVerticalSpace) + 10.0 + safeInsets.top: (button_count - 1) * (buttonHeight + buttonVerticalSpace) + 10.0 + safeInsets.top;
    // button
    UIButton *button = [[UIButton alloc] init];
    [button setFrame:CGRectMake(buttonX, buttonY, buttonWidth, buttonHeight)];
    [button setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
    [button setTitle:title forState:UIControlStateNormal];
    [button addTarget:self action:selector forControlEvents:UIControlEventTouchUpInside];
    [button.layer setCornerRadius:16.0];
    [button.layer setBorderColor:[UIColor lightGrayColor].CGColor];
    [button.layer setBorderWidth:0.5];
    [self.view addSubview:button];
}

- (void)addResultLabel {
    self.resultLabel = [[UILabel alloc] init];
    [self.resultLabel setTextAlignment:NSTextAlignmentCenter];
    [self.resultLabel setNumberOfLines:0];
    [self.resultLabel setFont:[UIFont systemFontOfSize:22]];
    [self.resultLabel setTextColor:[UIColor redColor]];
    [self.view addSubview:self.resultLabel];
    [self.resultLabel setTranslatesAutoresizingMaskIntoConstraints:NO];
    
    [[NSLayoutConstraint constraintWithItem:self.view attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.resultLabel attribute:NSLayoutAttributeLeft multiplier:1.0 constant:0] setActive:YES];
    [[NSLayoutConstraint constraintWithItem:self.view attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.resultLabel attribute:NSLayoutAttributeRight multiplier:1.0 constant:0] setActive:YES];
    [NSLayoutConstraint constraintWithItem:self.view attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:50.0f];
    [[NSLayoutConstraint constraintWithItem:self.view attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self.resultLabel attribute:NSLayoutAttributeBottom multiplier:1.0 constant:15] setActive:YES];
}

#pragma mark - delegate
- (void)onCallbackWithCode:(NSInteger)code extra:(NSString * _Nullable)extra {
    NSString *resultString = [NSString stringWithFormat:@"callback code:%ld,extra:%@",(long)code,extra];
    NSLog(@"%@", resultString);
    if (code == AntiAddictionServiceResultLoginSuccess) {
        // 登录成功 展示协议等
        [AntiAddictionService enterGame];
    }else if (code == AntiAddictionServiceResultPlayTimeLimitNoTime) {
        // 时长限制 弹窗提醒等
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"温馨提示" message:@"时长剩余不多，请注意游戏" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *cancelAct = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
           // do something
        }];
        [alertController addAction:cancelAct];
        UIAlertAction *enterAct = [UIAlertAction actionWithTitle:@"进入游戏" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [AntiAddictionService enterGame];
        }];
        [alertController addAction:enterAct];
        [self.navigationController presentViewController:alertController animated:YES completion:nil];
    }
    if (code != 1100) {
        [self.resultLabel setText:resultString];
    }
}
@end
