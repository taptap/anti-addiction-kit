'use strict';
const lodash = require('lodash');

/**
 * 实名表
 * @param app
 * @returns {void|sequelize.Model<any, any, TAttributes>|*}
 */
module.exports = app => {
  const table = 'fcm_identify';
  const { STRING, INTEGER, DATE } = app.Sequelize;
  const Identify = app.model.define(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      user_id: STRING(255),
      name: STRING(512),
      identify: STRING(512),
      create_time: DATE,
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  Identify.findByUserId = async function(user_id) {
    return this.findOne({
      where: {
        user_id: user_id
      }
    });
  };

  /**
   * 存储用户身份证，如果存在则返回原记录
   * @param user_id
   * @param name
   * @param identify
   * @returns {Promise<any|Thenable<any>|void>}
   */
  Identify.saveIdentify = async function(user_id, name, identify) {
    let data = await this.findByUserId(user_id);
    if(lodash.isEmpty(data)){
      return this.create({user_id,name,identify}).then( function (result) {
        return result;
      });
    }else{
      return this.update({
        name: name,
        identify: identify,
      }, {
        where: {
          user_id: user_id
        }
      });
    }
  };
  return Identify;
};
