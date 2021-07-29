'use strict';

module.exports = {
    up: async (queryInterface, Sequelize) => {
        /**
         * Add altering commands here.
         *
         * Example:
         * await queryInterface.createTable('users', { id: Sequelize.INTEGER }},
         */
        const table = 'fcm_ui_config';
        const {INTEGER, DATE, STRING} = Sequelize;
        await queryInterface.createTable(table, {
            id: {type: INTEGER(10), primaryKey: true, autoIncrement: true},
            group: {type: INTEGER(1), allowNull: false, defaultValue: 1, comment: "文案分组 1-线上版 2-版署版"},
            type: {
                type: INTEGER(3),
                allowNull: false,
                defaultValue: 1,
                comment: "ui类型:1-宵禁剩余时间提示 2-时长剩余提示 3-单笔消费限制 4-月消费限制 5-已经处于宵禁提示 6-时长耗尽提示 7-非宵禁时段第一次登陆 8-非宵禁时段非第一次登陆,剩余时长大于0，9-非宵禁时段非第一次登陆,剩余时长小于等于0，10-宵禁时段，登陆成功 11-非第一次登录，且剩余时长＞0，且＜20min时 12-气泡剩余时长 13-气泡距离宵禁时间"
            },
            account_type: {
                type: INTEGER(1),
                allowNull: false,
                defaultValue: 1,
                comment: "0-未实名游客 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：18+ 5：未实名正式用户"
            },
            title: {type: STRING(255), allowNull: false, defaultValue: "", comment: "对话框标题 eg:健康消费提示"},
            description: {type: STRING(1024), allowNull: false, defaultValue: "", comment: "对话框内容 eg:老板你已经氪了一座金山，请冷静"},
            update_time: {
                type: DATE,
                allowNull: false,
                defaultValue: Sequelize.literal('CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP'),
                comment: "更新时间"
            },

        })
            .then(() => queryInterface.addIndex(table, ['group', 'type', 'account_type'], {unique: true}));
        return queryInterface.bulkInsert(
            table,
            [
                {
                    "id": "1",
                    "group": "1",
                    "type": "3",
                    "account_type": "1",
                    "title": "健康消费提示",
                    "description": "根据国家相关规定，当前您无法使用充值相关功能。",
                },
                {
                    "id": "2",
                    "group": "1",
                    "type": "3",
                    "account_type": "2",
                    "title": "健康消费提示",
                    "description": "根据国家相关规定，您本次付费金额超过规定上限，无法购买。请适度娱乐，理性消费。",

                },
                {
                    "id": "3",
                    "group": "1",
                    "type": "3",
                    "account_type": "3",
                    "title": "健康消费提示",
                    "description": "根据国家相关规定，您本次付费金额超过规定上限，无法购买。请适度娱乐，理性消费。",

                },
                {
                    "id": "4",
                    "group": "1",
                    "type": "4",
                    "account_type": "1",
                    "title": "健康消费提示",
                    "description": "根据国家相关规定，当前您无法使用充值相关功能。",

                },
                {
                    "id": "5",
                    "group": "1",
                    "type": "4",
                    "account_type": "2",
                    "title": "健康消费提示",
                    "description": "根据国家相关规定，您当月的剩余可用充值额度不足，无法购买此商品。请适度娱乐，理性消费。",

                },
                {
                    "id": "6",
                    "group": "1",
                    "type": "4",
                    "account_type": "3",
                    "title": "健康消费提示",
                    "description": "根据国家相关规定，您当月的剩余可用充值额度不足，无法购买此商品。请适度娱乐，理性消费。",

                },
                {
                    "id": "7",
                    "group": "2",
                    "type": "3",
                    "account_type": "1",
                    "title": "健康消费提示",
                    "description": "当前账号未满 8 周岁，无法使用充值相关功能。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",

                },
                {
                    "id": "8",
                    "group": "2",
                    "type": "3",
                    "account_type": "2",
                    "title": "健康消费提示",
                    "description": "当前账号未满 16 周岁，本次单笔付费金额超过规定上限 50 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",

                },
                {
                    "id": "9",
                    "group": "2",
                    "type": "3",
                    "account_type": "3",
                    "title": "健康消费提示",
                    "description": "当前账号未满 18 周岁，本次单笔付费金额超过规定上限 100 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",

                },
                {
                    "id": "10",
                    "group": "2",
                    "type": "4",
                    "account_type": "1",
                    "title": "健康消费提示",
                    "description": "当前账号未满 8 周岁，无法使用充值相关功能。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",

                },
                {
                    "id": "11",
                    "group": "2",
                    "type": "4",
                    "account_type": "2",
                    "title": "健康消费提示",
                    "description": "当前账号未满 16 周岁，购买此商品后，您当月交易的累计总额已达上限  200 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",

                },
                {
                    "id": "12",
                    "group": "2",
                    "type": "4",
                    "account_type": "3",
                    "title": "健康消费提示",
                    "description": "当前账号未满 18 周岁，购买此商品后，您当月交易的累计总额已达上限  400 元，无法购买。根据国家相关规定，未满8周岁：不提供付费服务；8-16周岁以下：单笔付费不超过50元，每月累计不超过200元；16-18周岁以下：单笔付费不超过100元，每月累计不超过400元。",

                },
                {
                    "id": "13",
                    "group": "1",
                    "type": "1",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "14",
                    "group": "1",
                    "type": "5",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前无法进入游戏。",

                },
                {
                    "id": "15",
                    "group": "2",
                    "type": "1",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "16",
                    "group": "2",
                    "type": "1",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "17",
                    "group": "2",
                    "type": "1",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "18",
                    "group": "2",
                    "type": "5",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。",

                },
                {
                    "id": "19",
                    "group": "2",
                    "type": "5",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。",

                },
                {
                    "id": "20",
                    "group": "2",
                    "type": "5",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。",

                },
                {
                    "id": "21",
                    "group": "1",
                    "type": "2",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。",

                },
                {
                    "id": "22",
                    "group": "1",
                    "type": "2",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "23",
                    "group": "1",
                    "type": "2",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。",

                },
                {
                    "id": "24",
                    "group": "2",
                    "type": "2",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，游客账号享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟。",

                },
                {
                    "id": "25",
                    "group": "2",
                    "type": "2",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "26",
                    "group": "2",
                    "type": "2",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟。",

                },
                {
                    "id": "27",
                    "group": "1",
                    "type": "6",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时长已达# ${remaining} #分钟。登记实名信息后可深度体验。",

                },
                {
                    "id": "28",
                    "group": "1",
                    "type": "6",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间已达# ${remaining} #分钟。根据国家相关规定，今日无法再进行游戏，请注意适度游戏。",

                },
                {
                    "id": "29",
                    "group": "1",
                    "type": "6",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时长已达# ${remaining} #分钟。登记实名信息后可深度体验。",

                },
                {
                    "id": "30",
                    "group": "2",
                    "type": "6",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达# ${remaining} #分钟上限，无法再进行游戏。",

                },
                {
                    "id": "31",
                    "group": "2",
                    "type": "6",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达# ${remaining} #分钟上限，无法再进行游戏。",

                },
                {
                    "id": "32",
                    "group": "2",
                    "type": "6",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，游客账号享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达 60分钟上限，无法再进行游戏。",

                },
                {
                    "id": "33",
                    "group": "1",
                    "type": "7",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，根据国家相关规定，游客账号享有 60 分钟游戏体验时间。登记实名信息后可深度体验。",

                },
                {
                    "id": "34",
                    "group": "1",
                    "type": "8",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，游戏体验时间还剩余# ${remaining} #分钟。登记实名信息后可深度体验。",

                },
                {
                    "id": "35",
                    "group": "1",
                    "type": "9",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时长已达 60 分钟。登记实名信息后可深度体验。",

                },
                {
                    "id": "36",
                    "group": "2",
                    "type": "7",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，游客账号享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。",

                },
                {
                    "id": "37",
                    "group": "2",
                    "type": "8",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，游客账号享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟。",

                },
                {
                    "id": "38",
                    "group": "2",
                    "type": "9",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，游客账号享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达 60 分钟上限，无法再进行游戏。",

                },
                {
                    "id": "39",
                    "group": "2",
                    "type": "10",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您当前为游客账号，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。",

                },
                {
                    "id": "40",
                    "group": "1",
                    "type": "7",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，根据国家相关规定，您享有 60 分钟游戏体验时间。登记实名信息后可深度体验。",

                },
                {
                    "id": "41",
                    "group": "1",
                    "type": "8",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，游戏体验时间还剩余# ${remaining} #分钟。登记实名信息后可深度体验。",

                },
                {
                    "id": "42",
                    "group": "1",
                    "type": "9",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时长已达 60 分钟。登记实名信息后可深度体验。",

                },
                {
                    "id": "43",
                    "group": "2",
                    "type": "7",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。根据国家相关规定，您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。",

                },
                {
                    "id": "44",
                    "group": "2",
                    "type": "8",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟。",

                },
                {
                    "id": "45",
                    "group": "2",
                    "type": "9",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。您享有 60 分钟游戏体验时间，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达 60 分钟上限，无法再进行游戏。",

                },
                {
                    "id": "46",
                    "group": "2",
                    "type": "10",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您当前未提交实名信息，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。",

                },
                {
                    "id": "47",
                    "group": "1",
                    "type": "9",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间已达# ${remaining} #分钟。根据国家相关规定，今日无法再进行游戏。请注意适度游戏。",

                },
                {
                    "id": "48",
                    "group": "1",
                    "type": "11",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "49",
                    "group": "2",
                    "type": "7",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。您当前享有# ${remaining} #分钟游戏时间，请注意适度游戏。",

                },
                {
                    "id": "50",
                    "group": "2",
                    "type": "8",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "51",
                    "group": "2",
                    "type": "9",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，未成年账号非法定节假日每日游戏时长 1.5 小时，法定节假日每日游戏时长为 3 小时，且每日 22 点 - 次日 8 点为健康保护时段，不可进入游戏。该账号游戏体验时间已达# ${remaining} #分钟上限，无法再进行游戏。",

                },
                {
                    "id": "52",
                    "group": "2",
                    "type": "10",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前不可进入游戏。",

                },
                {
                    "id": "53",
                    "group": "2",
                    "type": "11",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "54",
                    "group": "1",
                    "type": "12",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。",

                },
                {
                    "id": "55",
                    "group": "2",
                    "type": "12",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。",

                },
                {
                    "id": "56",
                    "group": "1",
                    "type": "12",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。",

                },
                {
                    "id": "57",
                    "group": "2",
                    "type": "12",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "您的游戏体验时间还剩余# ${remaining} #分钟，登记实名信息后可深度体验。",

                },
                {
                    "id": "58",
                    "group": "1",
                    "type": "12",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "59",
                    "group": "2",
                    "type": "12",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "您今日游戏时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "60",
                    "group": "1",
                    "type": "13",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "61",
                    "group": "2",
                    "type": "13",
                    "account_type": "0",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "62",
                    "group": "1",
                    "type": "13",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "63",
                    "group": "2",
                    "type": "13",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "64",
                    "group": "1",
                    "type": "13",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "65",
                    "group": "2",
                    "type": "13",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "66",
                    "group": "1",
                    "type": "5",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前无法进入游戏。",

                },
                {
                    "id": "67",
                    "group": "1",
                    "type": "1",
                    "account_type": "5",
                    "title": "健康游戏提示",
                    "description": "距离健康保护时间还剩余# ${remaining} #分钟，请注意适度游戏。",

                },
                {
                    "id": "68",
                    "group": "1",
                    "type": "10",
                    "account_type": "1",
                    "title": "健康游戏提示",
                    "description": "根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前无法进入游戏。",
                },
                {
                    "id": "69",
                    "group": "1",
                    "type": "10",
                    "account_type": "2",
                    "title": "健康游戏提示",
                    "description": "根据国家相关规定，每日 22 点 - 次日 8 点为健康保护时段，当前无法进入游戏。",
                },
            ],
            {}
            );
    },


    down: async (queryInterface, Sequelize) => {
        /**
         * Add reverting commands here.
         *
         * Example:
         * await queryInterface.dropTable('users'},
         */
    }
};
