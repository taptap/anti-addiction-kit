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
    const table = 'fcm_charge_amounts';
    await queryInterface.createTable(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      unique_id: {type: STRING(128),allowNull:false,defaultValue:"",comment:"身份标识：身份证或用户ID"},
      game: {type: STRING(50),allowNull:false,defaultValue:"",comment:"游戏名称"},
      month: {type: STRING(20),allowNull:false,defaultValue:"",comment:"充值月份，yyyy-mm"},
      amount: {type: INTEGER(8),allowNull:false,defaultValue:0,comment:"总金额(单位-分)"},
      create_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP()'),comment:"创建时间"},
      update_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP()'),comment:"更新时间"},
    }).then(()=>queryInterface.addIndex(table,['unique_id','month']))
        .then(()=>queryInterface.addIndex(table,['create_time'])
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
