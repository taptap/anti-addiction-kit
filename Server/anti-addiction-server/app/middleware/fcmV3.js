/**
 * 防沉迷检查,成年直接返回
 * @param app
 * @returns {fcm}
 */
module.exports = app => {
  return async function fcm(ctx, next) {
    let userInfo = ctx.user;
    if (userInfo.account_type === 4) {//18岁及以上不需要防沉迷
      return ctx.helper.result(200,'18岁及以上不需要防沉迷',{"status":true});
    }
    await next();
  };
};
