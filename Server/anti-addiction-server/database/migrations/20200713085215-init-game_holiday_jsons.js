'use strict';

module.exports = {
  up: async (queryInterface, Sequelize) => {
    /**
     * Add altering commands here.
     *
     * Example:
     * await queryInterface.createTable('users', { id: Sequelize.INTEGER });
     */
    const table = 'fcm_game_holiday_json';
    const { INTEGER,STRING} = Sequelize;
    await queryInterface.createTable(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      game: {type: STRING(50),allowNull:false,defaultValue:"",comment:"游戏，通用设为：common"},
      year: {type: INTEGER(5),allowNull:false,defaultValue:0,comment:"年份，如2021"},
      holiday_dates: {type: STRING(2048),allowNull:false,defaultValue:0,comment:"当年的法定节假日，格式json"},
    });
    return queryInterface.bulkInsert(
        table,
        [
          {id:1,game:'common',year:2021,holiday_dates:'{"202101":{"01":"2"},"202102":{"12":"2","13":"2","14":"2"},"202104":{"04":"2"},"202105":{"01":"2"},"202106":{"14":"2"},"202109":{"21":"2"},"202110":{"01":"2","02":"2","03":"2"}}'},
          {id:2,game:'common',year:2022,holiday_dates:'{"202201":{"01":"2"},"202202":{"01":"2","02":"2","03":"2"},"202204":{"05":"2"},"202205":{"01":"2","02":"2","03":"2"},"202206":{"03":"2"},"202209":{"10":"2"},"202210":{"01":"2","02":"2","03":"2"}}'}
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
