'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    /**
     * Add altering commands here.
     *
     * Example:
     * await queryInterface.createTable('users', { id: Sequelize.INTEGER });
     */
    const { INTEGER,DATE,STRING} = Sequelize;
    const table = 'fcm_user_info';
    await queryInterface.createTable(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      user_id: {type: STRING(128),allowNull:false,defaultValue:"",comment:"用户唯一id"},
      unique_id: {type: STRING(128),allowNull:false,defaultValue:"",comment:"身份标识：身份证或用户ID"},
      identify_state: {type: INTEGER(1),allowNull:false,defaultValue:0,comment:"实名状态，0=>未实名，1=>实名"},
      birthday: {type:STRING(64),allowNull:false,defaultValue:"",comment:"生日"},
      account_type: {type: INTEGER(1),allowNull:false,defaultValue:0,comment:"实名类型，0-未实名游客 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名正式用户"},
      is_temp:{type: INTEGER(1),allowNull:false,defaultValue:0,comment:"是否游客：0-不是 1-是"},
      create_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP()'),comment:"创建时间"},
      update_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP()'),comment:"更新时间"},
    })
      .then(()=>queryInterface.addIndex(table,['user_id'],{unique:true})
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
