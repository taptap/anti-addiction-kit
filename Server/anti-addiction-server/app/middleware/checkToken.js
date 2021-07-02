'use strict';

/**
 * 检查jwt token
 * @param app
 * @returns {checkToken}
 */
module.exports = app => {
  return async function checkToken(ctx, next) {
    let payload;
    let accessToken = ctx.header.authorization;
    const UserInfoModel = ctx.model.UserInfo;
    if (!accessToken || accessToken.length === 0) {
      return ctx.helper.result(9999,'无效的access_token1');
    }
    try {
      payload = await app.jwt.verify(accessToken.split(' ')[1], app.config.jwt.secret);  // // 解密，获取payload
    } catch (error) {
      return ctx.helper.result(9999,'无效的access_token2',error.message);
    }
    let user = await UserInfoModel.findByUserId(payload.user_id);
    if (!user) {
      return ctx.helper.result(200,'用户不存在',{"status":true});
    }
    ctx.user = user;
    await next();
  };
};
