'use strict';
const lodash = require('lodash');
module.exports = app => {
  const table = 'fcm_user_info';
  const { STRING, INTEGER, DATE } = app.Sequelize;
  const UserInfo = app.model.define(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      user_id: STRING(255),
      unique_id: STRING(255),
      identify_state: INTEGER(1),
      account_type: INTEGER(1),
      birthday: STRING(64),
      is_temp:INTEGER(1),
      create_time: DATE,
      update_time: DATE,
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  UserInfo.findByUserId = async function(user_id) {
    user_id = lodash.toString(user_id);
    return this.findOne({
      where: {
        user_id: user_id
      }
    });
  };

  UserInfo.saveUser = async function(user_id, unique_id, identify_state, account_type,birthday,is_temp) {
    user_id = lodash.toString(user_id);
    let user = await this.findByUserId(user_id);
    if(lodash.isEmpty(user)){
      return this.create({user_id, unique_id, identify_state, account_type,birthday,is_temp}).then( function (result) {
        return result;
      });
    }else{
      await this.update({unique_id:unique_id, identify_state:identify_state, account_type:account_type,birthday:birthday,is_temp:is_temp},{
        where: {
          user_id: user_id
        }
      });
      return this.findByUserId(user_id)
    }
  };

  UserInfo.updateIdentify = async function (user_id,unique_id,name,identify,birthday,accountType) {
    user_id = lodash.toString(user_id);
    return this.update({
      name: name,
      unique_id: unique_id,
      identify: identify,
      identify_state: 1,
      birthday: birthday,
      account_type: accountType,
    }, {
      where: {
        user_id: user_id
      }
    }).then(function(result) {
        return app.model.Identify.saveIdentify(user_id,name,identify);
    });
  };

  return UserInfo;
};
