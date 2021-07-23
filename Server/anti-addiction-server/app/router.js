'use strict';

/**
 * @param {Egg.Application} app - egg application
 */
module.exports = app => {
  const { router, controller } = app;

  const fcm = app.middleware.fcm(app);
  const checkToken = app.middleware.checkToken(app);
  // authorizations
  router.post('/v3/fcm/authorizations', controller.v3.authorizations.authorizations);
  router.get('/v3/fcm/get_server_time', controller.v3.authorizations.get_server_time);
  router.get('/v3/fcm/get_config', controller.v3.authorizations.get_config);
  // fcm
  router.post('/v3/fcm/set_play_log', checkToken, fcm, controller.v3.fcm.upload_play_logs);
  router.post('/v3/fcm/check_pay', checkToken, fcm, controller.v3.fcm.check_pay);
  // identify
  router.post('/v3/fcm/submit_pay', checkToken, fcm, controller.v3.identify.submit_pay);
  router.post('/v3/fcm/real_user_info', checkToken, controller.v3.identify.real_user_info);
  router.post('/v3/fcm/charge', controller.v3.identify.charge);
  //tools,测试阶段重置用户防沉迷时间
  router.post('/tools/cost_time',controller.v3.tools.cost_time);
  router.post('/tools/set_curfew',controller.v3.tools.set_curfew);

};
