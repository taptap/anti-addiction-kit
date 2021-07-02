'use strict';

const Service = require('egg').Service;
const lodash = require('lodash');

class ChargeAmountService extends Service {
  /**
   * 防沉迷充值限额逻辑
   * @param userInfo
   * @param game
   * @param amount
   * @returns {Promise<{description: string, title: string}|boolean>}
   */
  async checkPay(userInfo,game,amount){
    const UiConfigModel = this.ctx.model.UiConfig;
    const userLimitConfigModel = this.ctx.model.UserLimitConfig;
    const chargeAmountModel = this.ctx.model.ChargeAmount;
    const ChildProtectedModel = this.ctx.model.ChildProtectedConfig;
    if(userInfo.identify_state === 0 ){
     // throw new Error('您当前未登记实名信息。根据国家相关规定，游戏用户需使用真实有效身份信息登记。请前往用户中心-账号安全进行实名登记。');
      return {"title": '健康消费提醒', "description" : '您当前未登记实名信息。根据国家相关规定，游戏用户需使用真实有效身份信息登记。请前往用户中心-账号安全进行实名登记。'};
    }
    let payAmountLimit = await userLimitConfigModel.getLimitByAccountType(userInfo.account_type);
    if(lodash.isEmpty(payAmountLimit) || payAmountLimit.pay_limit === undefined || payAmountLimit.pay_month_limit === undefined){
      throw new Error('未配置防沉迷消费限制：用户类型'+userInfo.account_type);
    }
    //每笔消费提示
    let belong_group = await this.service.fcmService.getGroupByGame(game);

    let payLimitTip = await UiConfigModel.getUiByTypeAndAccountType(belong_group,3,userInfo.account_type);//实名类型，1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+
    let defaultLimitTip = {"title": '健康消费提醒', "description" : '根据国家相关规定，您本次付费金额超过规定上限，无法购买。请适度娱乐，理性消费'};
    if(!lodash.isEmpty(payLimitTip)){
      defaultLimitTip = {"title":payLimitTip.title,"description":payLimitTip.description};
    }
    //每月消费提示
    let payLimitByMonthTip = await UiConfigModel.getUiByTypeAndAccountType(belong_group,4,userInfo.account_type);//实名类型，1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+
    let defaultLimitMonthTip = {"title": '健康消费提醒', "description" : '根据国家相关规定，您本月付费金额超过规定上限，无法购买。请适度娱乐，理性消费'};
    if(!lodash.isEmpty(payLimitByMonthTip)){
      defaultLimitMonthTip = {"title":payLimitByMonthTip.title,"description":payLimitByMonthTip.description};
    }
    //单笔充值限额检查
    if(amount > payAmountLimit.pay_limit){
      return defaultLimitTip;
    }
    let currentMonth = this.ctx.helper.getMonth();

    let switches = await ChildProtectedModel.getSwitch();
    let isDiffGame = switches.charge_amount_switch;//0:分游戏计算金额 1:统一计算金额
    let totalAmount;
    if(isDiffGame === 1){
      totalAmount = await chargeAmountModel.getTotalAmountByMonth(userInfo.unique_id,currentMonth);
    }else{
      totalAmount = await chargeAmountModel.getTotalAmountByGameAndMonth(userInfo.unique_id,game,currentMonth);
    }
    //每月充值限额检查
    if((totalAmount+amount) > payAmountLimit.pay_month_limit){
      return defaultLimitMonthTip;
    }
    return true;
  }

  /**
   * 上传支付数据
   * @param userInfo
   * @param game
   * @param amount
   * @returns {Promise<boolean>}
   */
  async submitPay(userInfo,game,amount){
    const chargeAmountModel = this.ctx.model.ChargeAmount;

    if(userInfo.identify_state === 0 ){
      throw new Error('您当前未登记实名信息。根据国家相关规定，游戏用户需使用真实有效身份信息登记。请前往用户中心-账号安全进行实名登记。');
    }
    let month = this.ctx.helper.getMonth();
    await chargeAmountModel.saveAmount(userInfo.unique_id, game, month, amount);
    return true;
  }
}

module.exports = ChargeAmountService;

