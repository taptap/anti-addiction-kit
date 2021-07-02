'use strict';

module.exports = app => {
  const table = 'fcm_user_limit_config';
  const {INTEGER,DATE} = app.Sequelize;
  const userLimitConfig = app.model.define(table, {
    id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
    account_type: {type: INTEGER(1),allowNull:false,defaultValue:0,comment:"实名类型，0-未实名游客 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名正式用户"},
    pay_month_limit: {type: INTEGER(4),allowNull:false,defaultValue:0,comment:"每月消费限制，元"},
    pay_limit: {type: INTEGER(4),allowNull:false,defaultValue:0,comment:"每笔消费限制，元"},
    update_time: DATE,
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  userLimitConfig.getLimitByAccountType = async function(account_type) {
    return await this.findOne({
      where:{
        account_type: account_type,
      }
    });
  };
  userLimitConfig.getAllLimit = async function(){
    return this.findAll({})
      .then(function(result) {
        let data = [];
        for (let key in result) {
          data[key] = result[key].dataValues;
        }
        return data;
      });
  };
  return userLimitConfig;
};
