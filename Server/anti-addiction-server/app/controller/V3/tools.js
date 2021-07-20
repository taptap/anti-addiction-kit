'use strict';
const Controller = require('egg').Controller;
const lodash = require('lodash');

class ToolsController extends Controller {
    async reset_time(ctx) {
        try {
            let params = ctx.request.body;
            let game = params.game;
            let user_id = params.user_id;
            ctx.logger.info(JSON.stringify(params));
            if (lodash.isEmpty(game) || lodash.isEmpty(user_id)) {
                return ctx.helper.result(400, '未上传正确game或user_id');
            }
            const UserInfoModel = ctx.model.UserInfo;
            const userPlayDuration = ctx.model.UserPlayDuration;

            let userInfo = await UserInfoModel.findByUserId(user_id);
            if(lodash.isEmpty(userInfo)){
                return ctx.helper.result(400, 'user_id不存在');
            }
            let unique_id = (lodash.isEmpty(userInfo.unique_id) && !lodash.isNumber(userInfo.unique_id)) ? userInfo.user_id : userInfo.unique_id;
            let day = userInfo.identify_state === 0 ? 0 : ctx.helper.getToday();//当日时间yyyymmmdd 0-未实名时间不按天计算
            let effect = await userPlayDuration.resetTime(day,unique_id,game);
            let msg ="要重置的数据不存在，用户id:"+user_id;
            if(effect){
                msg ="防沉迷时间重置成功，用户id:"+user_id;
            }
            return ctx.helper.result(200,msg);
        }catch (error){
            return ctx.helper.result(400,error.message);

        }
    }
}

module.exports = ToolsController;
