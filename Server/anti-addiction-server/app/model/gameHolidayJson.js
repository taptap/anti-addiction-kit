'use strict';
const moment = require('moment');
const lodash = require('lodash');

module.exports = app => {
  const table = 'fcm_game_holiday_json';
  const { STRING, INTEGER} = app.Sequelize;
  const gameHolidayJson = app.model.define(table, {
    id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
    game: {type: STRING(50),allowNull:false,defaultValue:"",comment:"游戏，通用设为：common"},
    year: {type: INTEGER(5),allowNull:false,defaultValue:0,comment:"年份，如2020"},
    holiday_dates: {type: STRING(2048),allowNull:false,defaultValue:0,comment:"当年的法定节假日，格式json"},
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  gameHolidayJson.getHoliday = async function(game) {
    let privateHoliday = await this.findOne({
      where:{
        game: game,
        year: moment().format("YYYY")
      }
    });
    if(lodash.isEmpty(privateHoliday)){
      return this.findOne({
        where:{
          game: 'common',
          year: moment().format("YYYY")
        }
      });
    }else{
      return privateHoliday;
    }
  };
  return gameHolidayJson;
};
