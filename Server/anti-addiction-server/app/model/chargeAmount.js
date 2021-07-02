'use strict';
const lodash = require('lodash');

module.exports = app => {
  const table = 'fcm_charge_amounts';
  const {STRING,INTEGER,DATE} = app.Sequelize;
  const ChargeAmount = app.model.define(table, {
    id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
    unique_id: {type: STRING(255),allowNull:false,defaultValue:"",comment:"身份标识：身份证或用户ID"},
    game: {type: STRING(50),allowNull:false,defaultValue:"",comment:"游戏名称"},
    month: {type: STRING(20),allowNull:false,defaultValue:"",comment:"充值月份，yyyymm"},
    amount: {type: INTEGER(8),allowNull:false,defaultValue:0,comment:"总金额，单位分"},
    create_time: DATE,
    update_time: DATE,
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  /**
   * 查询一条唯一数据
   * @param unique_id
   * @param game
   * @param month
   * @returns {Bluebird<TInstance | null>}
   */
  ChargeAmount.findUniqueData = async function(unique_id, game, month){
    return this.findOne({
      where:{
        unique_id:unique_id,
        game:game,
        month:month
      }
    });
  };

  /**
   * 更新充值金额
   * @param unique_id  //身份证token
   * @param game        //游戏名称
   * @param month       //月份 yyyymm
   * @param amount      //充值金额
   * @returns {Promise<any|Thenable<any>|void>}
   */
  ChargeAmount.saveAmount = async function(unique_id, game, month, amount) {
    let charge = await this.findUniqueData(unique_id, game, month);
    if(lodash.isEmpty(charge)){
      return this.create({unique_id, game, month, amount}).then( function (result) {
        return result;
      });
    }else{
      await this.update({amount: app.Sequelize.literal('amount +'+ amount)}, {
        where: {
          unique_id: unique_id,
          game: game,
          month: month,
        }
      });
      return this.findUniqueData(unique_id, game, month);
    }
  };

  /**
   * 不分游戏获取充值总金额
   * @param unique_id
   * @param month
   * @returns {Bluebird<unknown>}
   */
  ChargeAmount.getTotalAmountByMonth = async function(unique_id,month) {
    return this.sum('amount',{
      where:{
        unique_id: unique_id,
        month: month,
      }
    }).then(function(sum) {
      return lodash.isNaN(sum) ? 0: sum;;
    })
  };

  /**
   * 分游戏获取充值总金额
   * @param unique_id
   * @param game
   * @param month
   * @returns {Bluebird<unknown>}
   */
  ChargeAmount.getTotalAmountByGameAndMonth = async function(unique_id,game,month) {
    return this.sum('amount',{
      where:{
        unique_id: unique_id,
        game: game,
        month: month,
      }
    }).then(function(sum) {
      return lodash.isNaN(sum) ? 0: sum;;
    })
  };
  return ChargeAmount;
};
