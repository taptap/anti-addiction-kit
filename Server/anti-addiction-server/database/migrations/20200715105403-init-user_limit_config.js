'use strict';

module.exports = {
    up: async (queryInterface, Sequelize) => {
        /**
         * Add altering commands here.
         *
         * Example:
         * await queryInterface.createTable('users', { id: Sequelize.INTEGER });
         */
        const table = 'fcm_user_limit_config';
        const {INTEGER, DATE} = Sequelize;
        await queryInterface.createTable(table, {
            id: {type: INTEGER(10), primaryKey: true, autoIncrement: true},
            account_type: {type: INTEGER(1), allowNull: false, defaultValue: 0, comment: "实名类型，0-未实名游客 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名正式用户"},
            pay_month_limit: {type: INTEGER(8), allowNull: false, defaultValue: 0, comment: "每月消费限制，分"},
            pay_limit: {type: INTEGER(8), allowNull: false, defaultValue: 0, comment: "每笔消费限制，分"},
            update_time: {
                type: DATE,
                allowNull: false,
                defaultValue: Sequelize.literal('CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP()'),
                comment: "更新时间"
            },
        });
        return queryInterface.bulkInsert(
            table,
            [
                {id: 1, account_type: 2, pay_month_limit: 20000, pay_limit: 5000, update_time: '2020-07-27 16:47:07'},
                {id: 2, account_type: 1, pay_month_limit: 0, pay_limit: 0, update_time: '2020-07-24 14:52:52'},
                {id: 3, account_type: 3, pay_month_limit: 40000, pay_limit: 10000, update_time: '2020-07-27 16:47:12'},
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
