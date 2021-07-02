/* eslint valid-jsdoc: "off" */

'use strict';

/**
 * @param {Egg.EggAppInfo} appInfo app info
 */
// config/config.${env}.js
const path = require('path');

module.exports = appInfo => {
  /**
   * built-in config
   * @type {Egg.EggAppConfig}
   **/
  const config = (exports = {});
  let sdk_host = "127.0.0.1";
  let sdk_port = "3306";
  let sdk_user = "root";
  let sdk_pw = "root";

  // use for cookie sign key, should change to your own and keep security
  config.keys = appInfo.name + '_1575449783796_1500';
  config.sequelize = {
    dialect: 'mysql',
    host: sdk_host,
    port: sdk_port,
    username: sdk_user,
    password: sdk_pw,
    database: 'fcm',
    dialectOptions: {
      // @see https://github.com/sequelize/sequelize/issues/8019
      decimalNumbers: true,
      maxPreparedStatements: 100
    },
    pool: {
      min: 10,
      max: 50,
      idle: 10000
    },
    timezone: '+08:00',
    logging: false,
  };
  config.view = {
    mapping: {
      '.html': 'ejs',
    },
  };
  config.proxy = true;

  config.maxProxyCount = 6;

  config.customLogger = {
    userPlayLogger: {
      file: path.join(appInfo.baseDir, '/logs/user_play.log'),
      formatter(meta) {
        return `[${meta.date}] ${meta.message}`;
      },
    },
  };
  config.logger = {
    dir: path.join(appInfo.baseDir, '/logs/', appInfo.name),
    appLogName: `${appInfo.name}-web.log`,
    coreLogName: 'egg-web.log',
    agentLogName: 'egg-agent.log',
    errorLogName: 'common-error.log',
  };

  config.jwt = {
    secret: '!OKM9ijn', // 自定义 token 的加密条件字符串
  };
  // add your middleware config here
  config.middleware = [];

  config.security = {
    csrf: {
      enable: false,
    },
  };

  return {
    ...config,
  };
};
