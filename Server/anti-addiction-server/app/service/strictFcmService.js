'use strict';

const Service = require('egg').Service;
const lodash = require('lodash');
const moment = require('moment');

class StrictFcmService extends Service {
    async newFcmPolicy(game, userInfo, is_login){
        const GameGroupModel = this.ctx.model.GameGroup;
        let FcmSwitch = await GameGroupModel.getEnableFcmByGame(game);
        if(FcmSwitch === 0 || FcmSwitch === 2){
            return {remainTime: 3600, costTime: 0, restrictType: 0, title: "", description: ""};
        }
        // 特殊处理：消灭病毒某个特殊版本的userid返回未实名状态
        if (userInfo.user_id == "2222") {
            return {
                remainTime: 0, costTime: 3600, restrictType: 2, title: "重要更新提醒",
                description: "您好，当前版本存在故障无法运行，请在Appstore下载最新版本消灭病毒，对此我们深表歉意，敬请谅解。"
            };
        }

        if(userInfo.identify_state === 0){
            return {remainTime: 0, costTime: 3600, restrictType: 2, title: "健康游戏提示",
                description: "您的账号未完成实名认证，为了符合国家相关规定，不影响您的游戏体验，请尽快完善实名信息。"
            };
        }
        let stime = "20:00";
        let etime = "21:00";
        let can_play = await this.playTime(game,stime,etime);
        if(can_play){
            let needWarning = await this.warningTime(etime);
            let costTime = 3600 - can_play;
            if(needWarning !== false || is_login === 1){
                return {remainTime: can_play, costTime: costTime, restrictType: 1, title: "健康游戏提示",
                    description:"您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。您今日游戏时间还剩余 "+ Math.ceil(can_play /60) +" 分钟，请注意适当休息。"};
            }
            return {remainTime: can_play, costTime: costTime, restrictType: 0, title: "", description: ""};
        }else{
            return {remainTime: 0, costTime: 3600, restrictType: 1, title: "健康游戏提示",
                description: "您当前为未成年账号，已被纳入防沉迷系统。根据国家相关规定，周五、周六、周日及法定节假日 20 点 -  21 点之外为健康保护时段。当前时间段无法游玩，请合理安排时间。"
            };
        }
    }

    async isHoliday(game){
        const HolidayModel = this.ctx.model.GameHolidayJson;
        let holiday_config = await HolidayModel.getHoliday(game);
        if (lodash.isEmpty(holiday_config)) {
            throw new Error('请先行配置节假日');
        }
        //是否节假日
        let month = this.ctx.helper.getMonth();
        let day = this.ctx.helper.getDay();
        let holiday_json = JSON.parse(holiday_config.holiday_dates);
        if (holiday_json[month] !== undefined && holiday_json[month].length !== 0) {//实名用户节假日有额外时长
            if (holiday_json[month][day] !== undefined && holiday_json[month][day].length !== 0) {
                return true;
            }
        }
        return false;
    }

    async playTime(game,start,end){
        //星期几
        let date = new Date();
        let day  = date.getDay();
        let isHoliday = await this.isHoliday(game);
        if ([0,5,6].includes(day) || isHoliday){
            let current_time = ~~(Date.now() / 1000);
            let stime = ~~(new Date(moment().format("YYYY-MM-DD "+ start)).getTime()/ 1000);
            let etime = ~~(new Date(moment().format("YYYY-MM-DD "+ end)).getTime()/ 1000);
            if(current_time > stime && current_time < etime){
                return etime - current_time;
            }
        }
        return false;
    }

    async warningTime(curfew_end){
        let current_time = this.ctx.helper.getNow();
        //剩余时间，单位秒
        let curfew_diff_time = this.ctx.helper.getTimeStamp(curfew_end) - current_time;
        //剩余时间提示 触发条件：剩余时间15分钟
        return curfew_diff_time < 900 && curfew_diff_time > 0;
    }
}
module.exports = StrictFcmService;
