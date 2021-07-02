'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    /**
     * Add altering commands here.
     *
     * Example:
     * await queryInterface.createTable('users', { id: Sequelize.INTEGER });
     */
    const table = 'fcm_child_protected_config';
    const { INTEGER, DATE, STRING } = Sequelize;
    await queryInterface.createTable(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      share_time_switch: { type: INTEGER(3), allowNull: false, defaultValue: 0, comment: '0:分游戏计算时长，1:统一计算时长' },
      use_time_switch: { type: INTEGER(3), allowNull: false, defaultValue: 0, comment: '0:客户端时间 1:服务端时间' },
      charge_amount_switch: { type: INTEGER(3), allowNull: false, defaultValue: 0, comment: '0:分游戏计算金额 1:统一计算金额' },
      no_identify_time: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '未实名用户娱乐时间，单位秒' },
      child_common_time: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '娱乐时间，单位秒' },
      child_holiday_time: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '节假日娱乐时间，单位秒' },
      night_strict_warn: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '宵禁提前预警时间，单位秒' },
      remain_time_warn: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '剩余时间提前预警时间，单位秒' },
      night_strict_start: {
        type: STRING(16),
        allowNull: false,
        defaultValue: '',
        comment: '宵禁开始时间 | 格式:hh:mm | eg 20:00'
      },
      night_strict_end: {
        type: STRING(16),
        allowNull: false,
        defaultValue: '',
        comment: '宵禁结束时间 | 格式:hh:mm | eg 08:00'
      },
      upload_all_data: {type: INTEGER(3),allowNull:false,defaultValue:0,comment:"是否需要上传所有类型用户的数据：0:不上传成年人 1:上传成年人"},
      update_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP()'),comment:"更新时间"},
  });
    return queryInterface.bulkInsert(
        table,
        [
          {id:1,share_time_switch:1,use_time_switch:1,charge_amount_switch:1,no_identify_time:3600,child_common_time:5400,
            child_holiday_time:10800,night_strict_warn:1200,remain_time_warn:2400,night_strict_start:'22:00',night_strict_end:'08:00',upload_all_data: 0}
        ],
        {}
    );
  },

  down: async (queryInterface, Sequelize) => {
    /**
     * Add reverting commands here.
     *
     * Example:
     * await queryInterface.dropTable('users');
     */
  }
};
