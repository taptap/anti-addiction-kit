'use strict';

/**
 * @param {Egg.Application} app - egg application
 */
module.exports = app => {
  const { router, controller } = app;

  const fcmV3 = app.middleware.fcmV3(app);
  const checkToken = app.middleware.checkToken(app);
  // authorizations
  router.post('/v3/fcm/authorizations', controller.v3.authorizations.authorizations);
  router.get('/v3/fcm/get_server_time', controller.v3.authorizations.get_server_time);
  router.get('/v3/fcm/get_config', controller.v3.authorizations.get_config);
  // fcm
  router.post('/v3/fcm/set_play_log', checkToken, fcmV3, controller.v3.fcm.upload_play_logs);
  router.post('/v3/fcm/check_pay', checkToken, fcmV3, controller.v3.fcm.check_pay);
  // identify
  router.post('/v3/fcm/submit_pay', checkToken, fcmV3, controller.v3.identify.submit_pay);
  router.post('/v3/fcm/real_user_info', checkToken, controller.v3.identify.real_user_info);
  router.post('/v3/fcm/charge', controller.v3.identify.charge);
};
