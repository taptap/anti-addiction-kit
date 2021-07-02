'use strict';

module.exports = {
    up: async (queryInterface, Sequelize) => {
        /**
         * Add altering commands here.
         *
         * Example:
         * await queryInterface.createTable('users', { id: Sequelize.INTEGER });
         */
        const {DATE,STRING,BIGINT,INTEGER} = Sequelize;
        const table = 'fcm_user_play_durations';
        await queryInterface.createTable(table, {
            id: { type: BIGINT(16), primaryKey: true, autoIncrement: true },
            day: {type: STRING(10),allowNull:false,defaultValue:0,comment:"当天日期YYmmdd"},
            duration: {type: INTEGER(8),allowNull:false,defaultValue:0,comment:"当日已消耗时长-单位:秒"},
            game: {type: STRING(20),allowNull:false,defaultValue:"",comment:"游戏名称"},
            duration_key: {type: STRING(128),allowNull:false,defaultValue:"",comment:"身份标识：身份证或用户ID"},
            last_timestamp: {type: INTEGER(10),allowNull:false,defaultValue:0,comment:"最后一次游戏时间戳"},
            create_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP()'),comment:"创建时间"},
            update_time: {type: DATE,allowNull:false,defaultValue:Sequelize.literal('CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP()'),comment:"更新时间"},
        })
            .then(()=>queryInterface.addIndex(table,['day','game','duration_key'],{unique:true}))
                .then(()=>queryInterface.addIndex(table,['duration_key'])
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
