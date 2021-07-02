'use strict';
const Controller = require('egg').Controller;
const lodash = require('lodash');

class AuthorizationsController extends Controller {
  /**
   * 授权接口
   * @param ctx
   * @returns {Promise<{error_description: string, error: string}|{code: number}>}
   */
  async authorizations(ctx) {
    let body = ctx.request.body;
    let token = body.token;
    let userInfo;
    let localUserInfo = body.local_user_info;
    if (!token || token.length === 0) {
      return ctx.helper.result(400,'无效的access_token');
    }
    try{
      ctx.logger.info(JSON.stringify(body));
      userInfo = this.app.jwt.verify(token, this.app.config.jwt.secret);  //token签名
      if(userInfo.user_id === undefined || userInfo.user_id.length === 0){
        return ctx.helper.result(400,'token解析出的user_id不合法');
      }
      if(userInfo.unique_id === undefined){
        return ctx.helper.result(400,'token解析出的unique_id不合法');
      }
      if(userInfo.birthday === undefined){
        return ctx.helper.result(400,'token解析出的birthday不合法');
      }
      if(body.game === undefined || body.game .length ===0){
        return ctx.helper.result(400,'game参数不能为空');
      }
      let user = await ctx.service.userInfoService.getUser(body.game,userInfo.user_id,userInfo.unique_id,userInfo.birthday,0,localUserInfo);
      if(lodash.isEmpty(user)){
        return ctx.helper.result(10000,'生成授权失败');
      }
      let userToken = {
        user_id:user.user_id,
        birthday: user.birthday,
        accountType: user.account_type,
      };
      let accessToken = this.app.jwt.sign(userToken, this.app.config.jwt.secret);  //token签名
      return ctx.helper.result(200,'授权成功',{"access_token": accessToken,"user_id":lodash.toString(user.user_id),"type":user.account_type});
    }catch(error){
      this.logger.error(error);
      return ctx.helper.result(400,'token解析错误');
    }
  }

  async get_server_time(ctx) {
    return ctx.body = {timestamp: ctx.helper.getNow()}
  }

  async get_config(ctx){
    let body = ctx.request.query;
    let game = body.game;
    ctx.logger.info(body);
    if(game === undefined){
      game = 'default';
    }
    let res = await ctx.service.fcmService.getConfig(game);
    return ctx.helper.result(200,"",res);
  }
}

module.exports = AuthorizationsController;
