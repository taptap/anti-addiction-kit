'use strict';
const lodash = require('lodash');

module.exports = app => {
  const { STRING, INTEGER, DATE } = app.Sequelize;
  const table = 'fcm_user_play_durations';
  const userPlayDuration = app.model.define(table, {
    id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
    day: { type: STRING(10), allowNull: false, defaultValue: '', comment: '当天日期YYmmdd' },
    duration: { type: INTEGER(8), allowNull: false, defaultValue: 0, comment: '当日已消耗时长，单位秒' },
    duration_key: { type: STRING(255), allowNull: false, defaultValue: '', comment: '身份标识：身份证或用户ID，用户存放userInfo表的unique_id或user_id' },
    game: { type: STRING(50), allowNull: false, defaultValue: '', comment: '游戏名称' },
    last_timestamp: { type: INTEGER(10), allowNull: false, defaultValue: 0, comment: '最后一次游戏时间戳' },
    create_time: DATE,
    update_time: DATE,
  }, {
    freezeTableName: true, // Model tableName will be the same as the model name
    timestamps: false,
  });

  /**
   * 某游戏某天某人的防沉迷数据
   * @param day
   * @param duration_key
   * @param game
   * @returns {Bluebird<TInstance | null>}
   */
  userPlayDuration.findPlayInfoByGame = async function(day,duration_key, game){
    return this.findOne({
      where: {
        day: lodash.toString(day),
        duration_key: duration_key,
        game: game,
      }
    });
  };

  userPlayDuration.getTotalTime = async function(day,duration_key) {
    return this.sum('duration',{
      where:{
        day: lodash.toString(day),
        duration_key: duration_key,
      }
    }).then(function(sum) {
      return lodash.isNaN(sum) ? 0 : sum;
    })
  };

  /**
   * 某人是否玩过某游戏
   * @param duration_key
   * @param game
   * @returns {Bluebird<TInstance | null>}
   */
  userPlayDuration.findPlayInfoByKey = async function(duration_key, game){
    return this.findOne({
      where: {
        duration_key: duration_key,
        game: game,
      }
    });
  };

  /**
   * 更新用户防沉迷时间
   * @param day
   * @param duration
   * @param duration_key
   * @param game
   * @param last_timestamp
   * @returns {Promise<number|Thenable<number>|void|*>}
   */
  userPlayDuration.updatePlayInfo = async function(day, duration, duration_key, game, last_timestamp) {
    if(duration > 0){
      let playInfo = await this.findPlayInfoByGame(day,duration_key, game);
      if (lodash.isEmpty(playInfo)) {
        return this.create({ day, duration, duration_key, game, last_timestamp })
          .then(function() {
            return 1;
          });
      } else {
        let timestamp = (playInfo.last_timestamp < last_timestamp) ? last_timestamp : playInfo.last_timestamp;
        let add_duration = playInfo.duration + duration;
        return playInfo.update({duration: add_duration ,last_timestamp: timestamp}).then(function () {
          return 0;
        });
      }
    }else{
      return 0;
    }
  };


  userPlayDuration.costTime = async function(day,duration_key,game,cost_time){
    return this.update({
          duration:cost_time
        },
        {
          where:{
            day: lodash.toString(day),
            duration_key: duration_key,
            game: game
          }
      }
    ).then(function(effect) {
      return effect[0] > 0;
    });
  }
  return userPlayDuration;
};
