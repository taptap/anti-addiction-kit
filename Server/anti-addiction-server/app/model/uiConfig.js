'use strict';

module.exports = app => {
  const table = 'fcm_ui_config';
  const { STRING, INTEGER, DATE } = app.Sequelize;
  const UiConfig = app.model.define(table, {
      id: { type: INTEGER(10), primaryKey: true, autoIncrement: true },
      group: { type: INTEGER(1), allowNull: false, defaultValue: 1, comment: '文案分组 1-线上版 2-线上版' },
      type: {
        type: INTEGER(3),
        allowNull: false,
        defaultValue: 1,
        comment: 'ui类型:1-宵禁剩余时间提示 2-时长剩余提示 3-单笔消费限制 4-月消费限制 5-已经处于宵禁提示 6-时长耗尽提示 7-非宵禁时段第一次登陆 8-非宵禁时段非第一次登陆,剩余时长大于0，9-非宵禁时段非第一次登陆,剩余时长小于等于0，10-宵禁时段，登陆成功 11-非第一次登录，且剩余时长＞0，且＜20min时 12-气泡剩余时长 13-气泡距离宵禁时间'
      },
      account_type: {
        type: INTEGER(1),
        allowNull: false,
        defaultValue: 1,
        comment: '账户类型：0-未实名游客 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名正式用户'
      },
      title: { type: STRING(255), allowNull: false, defaultValue: '', comment: '对话框标题 eg:健康消费提示' },
      description: { type: STRING(1024), allowNull: false, defaultValue: '', comment: '对话框内容 eg:老板你已经氪了一座金山，请冷静' },
      update_time: DATE,
    }, {
      freezeTableName: true, // Model tableName will be the same as the model name
      timestamps: false,
    }
  );

  UiConfig.getUiByTypeAndAccountType = async function(group, type, account_type) {
    return await this.findOne({
      where: {
        group:group,
        type: type,
        account_type: account_type
      }
    });
  };

  UiConfig.getPayLimitUi = async function() {
    return this.findAll({
      where: {
        type: [ 3, 4 ],
      }
    })
      .then(function(result) {
        let data = [];
        for (let key in result) {
          data[key] = result[key].dataValues;
        }
        return data;
      });
  };

  UiConfig.getHealthUi = async function(group) {
    return this.findAll({
      attributes: ['type', 'account_type', 'title', 'description' ],
      where: {
        group: group,
        type: [1,2,5,6,7,8,9,10,11,12,13],
      },

    })
      .then(function(result) {
        let data = [];
        for (let key in result) {
          data[key] = result[key].dataValues;
        }
        return data;
      });
  };
  return UiConfig;
};
