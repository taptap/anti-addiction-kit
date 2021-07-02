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
    const table = 'fcm_identify';
    await queryInterface.createTable(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      user_id: {type: STRING(32),allowNull:false,defaultValue:0,comment:"用户id"},
      name: {type: STRING(128),allowNull:false,defaultValue:"",comment:"姓名"},
      identify: {type: STRING(128),allowNull:false,defaultValue:"",comment:"身份证"},
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
