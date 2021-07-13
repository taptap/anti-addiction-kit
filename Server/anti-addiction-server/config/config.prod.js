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
    let sdk_host = process.env.DATASOURCE_HOST;
    let sdk_port = process.env.DATASOURCE_PORT;
    let sdk_user = process.env.DATASOURCE_USER;
    let sdk_pw = process.env.DATASOURCE_PW;
    let sdk_schema =process.env.DATASOURCE_SCHEMA
    let jws = process.env.JWS
    // use for cookie sign key, should change to your own and keep security
    config.keys = appInfo.name + '_1575449783796_1500';
    config.sequelize = {
        dialect: 'mysql',
        host: sdk_host,
        port: sdk_port,
        username: sdk_user,
        password: sdk_pw,
        database: sdk_schema,
        dialectOptions: {
            // @see https://github.com/sequelize/sequelize/issues/8019
            decimalNumbers: true,
            maxPreparedStatements: 500
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

    config.logger = {
        dir: path.join(appInfo.baseDir, '/logs/', appInfo.name),
        appLogName: `${appInfo.name}-web.log`,
        coreLogName: 'egg-web.log',
        agentLogName: 'egg-agent.log',
        errorLogName: 'common-error.log',
    };

    config.jwt = {
        secret: jws, // 自定义 token 的加密条件字符串
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
