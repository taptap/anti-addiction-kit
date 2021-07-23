'use strict';
const Controller = require('egg').Controller;
const lodash = require('lodash');

class ToolsController extends Controller {
    async cost_time(ctx) {
        try {
            let params = ctx.request.body;
            let game = params.game;
            let user_id = params.user_id;
            let cost_time = params.cost_time;
            ctx.logger.info(JSON.stringify(params));
            if (lodash.isEmpty(game) || lodash.isEmpty(user_id)) {
                return ctx.helper.result(400, '未上传正确game或user_id');
            }
            if(!lodash.isNumber(cost_time) || cost_time<0){
                return ctx.helper.result(400, '已使用时间参数不正确');
            }
            const UserInfoModel = ctx.model.UserInfo;
            const userPlayDuration = ctx.model.UserPlayDuration;

            let userInfo = await UserInfoModel.findByUserId(user_id);
            if(lodash.isEmpty(userInfo)){
                return ctx.helper.result(400, 'user_id不存在');
            }
            let unique_id = (lodash.isEmpty(userInfo.unique_id) && !lodash.isNumber(userInfo.unique_id)) ? userInfo.user_id : userInfo.unique_id;
            let day = userInfo.identify_state === 0 ? 0 : ctx.helper.getToday();//当日时间yyyymmmdd 0-未实名时间不按天计算
            let effect = await userPlayDuration.costTime(day,unique_id,game,cost_time);
            let msg ="请先登陆游戏，用户id:"+user_id;
            if(effect){
                msg ="防沉迷时间设置成功，用户id:"+user_id;
            }
            return ctx.helper.result(200,msg);
        }catch (error){
            return ctx.helper.result(400,error.message);
        }
    }

    async set_curfew(ctx) {
        try {
            let params = ctx.request.body;
            let curfew_start = params.start;
            let curfew_end = params.end;
            let ereg = /^([0-1][0-9]|[2][0-3]):[0-5][0-9]$/
            if (!ereg.test(curfew_start) || !ereg.test(curfew_end)) {
                return ctx.helper.result(400, 'curfew_start或者curfew_end格式错误');
            }
            const ChildProtectedConfig = ctx.model.ChildProtectedConfig;

            let effect = await ChildProtectedConfig.setCurfew(curfew_start,curfew_end);
            let msg ="修改宵禁时间失败";
            if(effect){
                msg ="修改宵禁时间成功";
            }
            return ctx.helper.result(200,msg);
        }catch (error){
            return ctx.helper.result(400,error.message);
        }


    }
}

module.exports = ToolsController;
