'use strict';

module.exports = {
    up: async (queryInterface, Sequelize) => {
        /**
         * Add altering commands here.
         *
         * Example:
         * await queryInterface.createTable('users', { id: Sequelize.INTEGER });
         */
        const table = 'fcm_game_groups';
        const {INTEGER, DATE, STRING} = Sequelize;
        await queryInterface.createTable(table, {
            id: {type: INTEGER(10), primaryKey: true, autoIncrement: true},
            game: {type: STRING(50), allowNull: false, defaultValue: "", comment: "游戏，通用设为：common"},
            group: {type: INTEGER(1), allowNull: false, defaultValue: 1, comment: "文案分组 1-线上版 2-版署版"},
            enable_fcm: {type: INTEGER(3), allowNull: false, defaultValue: 1, comment: "防沉迷限制开关：0-不限制 1-限额+限时长 2-仅限额"},
            update_time: {
                type: DATE,
                allowNull: false,
                defaultValue: Sequelize.literal('CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'),
                comment: "更新时间"
            },
        })
            .then(() => queryInterface.addIndex(table, ['game'], {unique: true}));
        return queryInterface.bulkInsert(
            table,
            [
                {id: 1, game: 'common', group: 2, enable_fcm: 1},
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
