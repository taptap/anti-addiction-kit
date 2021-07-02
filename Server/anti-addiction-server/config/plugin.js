'use strict';

/** @type Egg.EggPlugin */
module.exports = {
  // had enabled by egg
  // static: {
  //   enable: true,
  // }
    mysql: {
        enable: true,
        package: 'egg-mysql',
    },
    accesslog : {
        enable: true,
        package: 'egg-accesslog',
    },
    globalHeader :{
        enable: true,
        package: 'egg-global-header',
    },
    ejs: {
        enable: true,
        package: 'egg-view-ejs',
    },
    sequelize : {
        enable: true,
        package: 'egg-sequelize',
    },
    jwt : {
        enable: true,
        package: "egg-jwt"
    },
};

