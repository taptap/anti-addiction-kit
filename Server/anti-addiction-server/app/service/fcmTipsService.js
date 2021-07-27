'use strict';

const Service = require('egg').Service;
const lodash = require('lodash');

/**
 * 1-宵禁剩余时间提示 2-时长剩余提示 5-已经处于宵禁提示 6-时长耗尽提示
 */
class FcmTipsService extends Service {

  async OutOfGameTime(belong_group,userInfo,remain_time){
    if(remain_time <= 0){//时长耗尽
      return 6;
    }
    return false;
  }

  async WarningOfGameTime(belong_group,userInfo,current_time,remain_time,switches,is_login,first_login){
    if(is_login === 1 && belong_group === 2 && remain_time > 0){//版署版，登陆时，剩余时长大于0，触发
      if(first_login === 1){
        return 7;
      }else{
        return 2;
      }
    }
    if(remain_time > 0 && remain_time < switches.remain_time_warn){
      return 2;
    }
    return false;
  }
  //
  async InCurfew(belong_group,userInfo,current_time,remain_time,switches){
    //当日宵禁时间戳
    let curfew_start = this.ctx.helper.getTimeStamp(switches.night_strict_start);
    let curfew_end = this.ctx.helper.getTimeStamp(switches.night_strict_end);

    //处于宵禁提示
    if(current_time > curfew_start || current_time <= curfew_end){
      return 5;
    }
    return false;
  }

  async WarningCurfew(belong_group,userInfo,current_time,remain_time,switches){
    //当日宵禁开始时间戳
    let curfew_start = this.ctx.helper.getTimeStamp(switches.night_strict_start);
    //宵禁剩余时间，单位秒
    let curfew_diff_time = curfew_start - current_time;
    //宵禁剩余时间提示 触发条件：宵禁剩余时长大于0 + 宵禁时间内有娱乐剩余时长 + 处于宵禁警告时内
    if(curfew_diff_time > 0 && ((current_time + remain_time) > curfew_start) && (curfew_diff_time <= switches.night_strict_warn)){
        return 1;
    }
    return false;
  }

}

module.exports = FcmTipsService;
