'use strict';
const Controller = require('egg').Controller;
const lodash = require('lodash');

class FcmController extends Controller {

  async upload_play_logs() {
    const { ctx } = this;
    let params = ctx.request.body;
    let userInfo = ctx.user;
    let playLogs = params.play_logs;
    let game = params.game;
    let is_login = params.is_login;
    ctx.logger.info(JSON.stringify(params));
    if (lodash.isEmpty(game)) {
      return ctx.helper.result(400, '未上传正确game');
    }
    if (!playLogs || playLogs.length === 0) {
      return ctx.helper.result(400,'参数错误');
    }
    if (playLogs.local_times === undefined || !lodash.isArray(playLogs.local_times)) {
      return ctx.helper.result(400, '未上传正确local_times');
    }
    //throw new Error('response status is not 200');
    if (playLogs.server_times === undefined || !lodash.isArray(playLogs.server_times)) {
      return ctx.helper.result(400, '未上传正确server_times');
    }
    try {
      let res = await ctx.service.fcmService.uploadPlayLogs(game, userInfo, playLogs.local_times, playLogs.server_times,is_login);
      return ctx.helper.result(200, '上传时间成功',res);
    } catch (error) {
      return ctx.helper.result(400, error.message);
    }
  }
  /**
   * 防沉迷检查金额
   * @returns {Promise<*>}
   */
  async check_pay(){
    const { ctx } = this;
    let params = ctx.request.body;
    let userInfo = ctx.user;
    let amount = params.amount;
    let game = params.game;
    ctx.logger.info(JSON.stringify(params));
    if (lodash.isEmpty(game)) {
      return ctx.helper.result(400, '未上传正确game');
    }
    if(!lodash.isNumber(amount) || amount<0){
      return ctx.helper.result(400, '未上传正确的金额');
    }
    try {
      const GameGroupModel = this.ctx.model.GameGroup;
      let FcmSwitch = await GameGroupModel.getEnableFcmByGame(game);
      if(FcmSwitch === 0){
        return ctx.helper.result(200,'已关闭限额检查',{"status":true});
      }
      let res = await ctx.service.chargeAmountService.checkPay(userInfo,game,amount);
      if(res !== true){
        return ctx.helper.result(200, "限额提示",res);
      }else{
        return ctx.helper.result(200, "允许充值",{"status":true});
      }
    }catch (error) {
      return ctx.helper.result(400, error.message);
    }
  }

  /**
   * 游戏充值成功后，上报充值金额（用户防沉迷统计累加金额）
   * @param ctx
   * @returns {Promise<*>}
   */
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
}

module.exports = FcmController;
