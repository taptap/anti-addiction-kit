'use strict';
const lodash = require('lodash');

module.exports = app => {
  const table = 'fcm_child_protected_config';
  const { STRING, INTEGER, DATE } = app.Sequelize;
  const ChildProtectedConfig = app.model.define(table, {
    id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
    share_time_switch: {type: INTEGER(3),allowNull:false,defaultValue:0,comment:"0:分游戏计算时长，1:统一计算时长"},
    use_time_switch: {type: INTEGER(3),allowNull:false,defaultValue:0,comment:"0:客户端时间 1:服务端时间"},
    charge_amount_switch:{type: INTEGER(3),allowNull:false,defaultValue:0,comment:"0:分游戏计算金额 1:统一计算金额"},
    no_identify_time: {type: INTEGER(10),allowNull:false,defaultValue:0,comment:"未实名用户娱乐时间，单位秒"},
    child_common_time: {type: INTEGER(10),allowNull:false,defaultValue:0,comment:"娱乐时间，单位秒"},
    child_holiday_time: {type: INTEGER(10),allowNull:false,defaultValue:0,comment:"节假日娱乐时间，单位秒"},
    night_strict_warn: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '宵禁提前预警时间，单位秒' },
    remain_time_warn: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '剩余时间提前预警时间，单位秒' },
    night_strict_start: {type: STRING(16),allowNull:false,defaultValue:"",comment:"宵禁开始时间 | 格式:hh:mm | eg 20:00"},
    night_strict_end: {type: STRING(16),allowNull:false,defaultValue:"",comment:"宵禁结束时间 | 格式:hh:mm | eg 08:00"},
    upload_all_data: {type: INTEGER(3),allowNull:false,defaultValue:0,comment:"是否需要上传所有类型用户的数据：0:不上传成年人 1:上传成年人"},
    update_time: DATE
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  ChildProtectedConfig.getSwitch = async function() {
    return this.findOne({
      attributes: ['share_time_switch', 'use_time_switch','no_identify_time','charge_amount_switch','child_common_time',
        'child_holiday_time','night_strict_start','night_strict_end','night_strict_warn','remain_time_warn','upload_all_data']
    });
  };
  return ChildProtectedConfig;
};
