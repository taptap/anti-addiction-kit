'use strict';
const lodash = require('lodash');

module.exports = app => {
    const table = 'fcm_game_groups';
    const {STRING, INTEGER, DATE} = app.Sequelize;
    const GameGroup = app.model.define(table, {
            id: {type: INTEGER(10), primaryKey: true, autoIncrement: true},
            game: {type: STRING(50), allowNull: false, defaultValue: "", comment: "游戏，通用设为：common"},
            group: {type: INTEGER(1), allowNull: false, defaultValue: 1, comment: "文案分组 1-线上版 2-线上版"},
            enable_fcm: {type: INTEGER(3), allowNull: false, defaultValue: 1, comment: "防沉迷限制开关：0-不限制 1-限额+限时长 2-仅限额"},
            update_time: DATE,
        }, {
            freezeTableName: true, // Model tableName will be the same as the model name
            timestamps: false,
        }
    );

    GameGroup.findByGame = async function (game) {
        return await this.findOne({
            where: {
                game: game
            }
        });
    };

    /**
     * 根据游戏名称返回0-不限制 1-限额+限时长 2-仅限额
     * @param game
     * @returns {Promise<*>}
     */
    GameGroup.getEnableFcmByGame = async function (game) {
        let game_group = await this.findByGame(game);
        return lodash.isNull(game_group) ? 1 : game_group.enable_fcm;
    };

    GameGroup.isBanShu = async function (game) {
        let game_group = await this.findByGame(game);
        if(game_group && game_group.group === 2){
            return true;
        }
        return false;
    };

    return GameGroup;
};
