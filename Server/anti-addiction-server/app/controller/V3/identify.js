'use strict';
const Controller = require('egg').Controller;
const lodash = require('lodash');

class IdentifyController extends Controller {
  async real_user_info(ctx){
    let params = ctx.request.body;
    let userInfo = ctx.user;
    let name = params.name;
    let identify = params.identify;
    if(userInfo.identify_state === 1){
      //return ctx.body = {'code': 400,'msg': '不能重复提交实名','data':{}};
      return ctx.helper.result(400,'不能重复提交实名');
    }
    if(lodash.isEmpty(name) || lodash.isEmpty(identify)){
      return ctx.helper.result(400,'缺少参数');
    }
    if(!this.ctx.helper.validRealid(identify) || !this.ctx.helper.validRealname(name)){
      return ctx.helper.result(400,'请填写真实有效证件信息');
    }
    await ctx.service.userInfoService.submitIdentify(userInfo.user_id,name,identify);
    //return ctx.body = {'code': 200,'msg': '实名成功','data':{}};
    return ctx.helper.result(200,'实名成功');
  }

  async submit_pay(ctx){
    let params = ctx.request.body;
    let userInfo = ctx.user;
    let amount = params.amount;
    let game = params.game;
    if (lodash.isEmpty(game)) {
      return ctx.helper.result(400, '未上传正确的game');
    }
    if(amount === undefined || !lodash.isNumber(amount) && amount<0){
      return ctx.helper.result(400, '未上传正确的金额');
    }
    try {
      await ctx.service.chargeAmountService.submitPay(userInfo,game,amount);
      return ctx.helper.result(200,'上传金额成功');
    }catch (error) {
      return ctx.helper.result(400, error.message);
    }
  }

  /**
   * 参数：{"accessToken":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bnF1ZV9rZXkiOiJkZGRkIiwiYW1vdW50IjozNiwiZ2FtZSI6InN4ZCIsImlhdCI6MTU5NTYwNzY2N30.9I-xoXtLdDBwDsZTkIvNPG7b0X3OsY32hj638SoIOMU"}
   * @param ctx
   * @returns {Promise<*>}
   */
  async charge(ctx){
    let params = ctx.request.body;
    let accessToken = params.accessToken;
    try {
      if (accessToken === undefined || lodash.isEmpty(accessToken)) {
        return ctx.helper.result(400,'无效的充值回调token');
      }
      let payload = await this.app.jwt.verify(accessToken, this.app.config.jwt.secret); // 解密，获取payload
      //console.log(payload);
      if(payload.unique_id === undefined || lodash.isEmpty(payload.unique_id)){
        return ctx.helper.result(400,'unque_key不存在');
      }
      if(payload.game === undefined || lodash.isEmpty(payload.game)){
        return ctx.helper.result(400,'缺少参数game');
      }
      if(payload.amount === undefined || !lodash.isNumber(payload.amount) && payload.amount<0){
        return ctx.helper.result(400, '未上传正确的金额');
      }

      let userInfo = {identify_state:1,unique_id:payload.unique_id};
      await ctx.service.chargeAmountService.submitPay(userInfo,payload.game,payload.amount);
      return ctx.helper.result(200,'上传金额成功');

    } catch (error) {
      return ctx.helper.result(400,error.message);
    }

  }
}

module.exports = IdentifyController;
