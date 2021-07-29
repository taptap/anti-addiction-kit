'use strict';

const Service = require('egg').Service;
const lodash = require('lodash');

class FcmService extends Service {
  /**
   * 上传时长
   * @param game  //游戏名称
   * @param userInfo   //用户信息
   * @param local_times //客户端时间戳组
   * @param server_times    //服务端时间戳组
   * @param is_login //是否登陆调用
   * @returns {Promise<{costTime: *, remainTime: *}>}
   */
  async uploadPlayLogs(game, userInfo, local_times, server_times,is_login) {
    const ChildProtectedModel = this.ctx.model.ChildProtectedConfig;
    const userPlayDuration = this.ctx.model.UserPlayDuration;
    let playLogs;
    let duration = 0;//本次上传的游戏时间
    let lastTimestamp = 0;
    let use_time = 0;
    let switches = await ChildProtectedModel.getSwitch();
    if (switches.use_time_switch === 0) {
      playLogs = local_times;
    } else {
      playLogs = server_times;
    }

    if(playLogs !== undefined && playLogs.length !== 0){
      for(let playLog of playLogs){
        playLog[0] = ~~(playLog[0]);
        playLog[1] = ~~(playLog[1]);
        if (playLog[0] > playLog[1]) {//开始时间比结束大 忽略
          continue;
        }

        if (playLog[0] < this.ctx.helper.getDayStartTime() || playLog[0] > this.ctx.helper.getDayEndTime()) {//不是今天的时间戳 忽略
          continue;
        }
        if (playLog[1] < this.ctx.helper.getDayStartTime() || playLog[1] > this.ctx.helper.getDayEndTime()) {//不是今天的时间戳 忽略
          continue;
        }
        duration += playLog[1] - playLog[0];
        if(playLog[1] > lastTimestamp){
          lastTimestamp = playLog[1];
        }
      }
    }
    //console.log(userInfo);
    let day = userInfo.identify_state === 0 ? 0 : this.ctx.helper.getToday();//当日时间yyyymmmdd 0-未实名时间不按天计算
    let unique_id = (lodash.isEmpty(userInfo.unique_id) && !lodash.isNumber(userInfo.unique_id)) ? userInfo.user_id : userInfo.unique_id;

    //账号是否首次登陆：0-不是 1-是
    let first_login = 0;
    if(is_login === 1){
      let play_time = await userPlayDuration.findPlayInfoByKey(unique_id, game);
      first_login = lodash.isEmpty(play_time) ? 1 :0;
    }
    await userPlayDuration.updatePlayInfo(day, duration, unique_id, game, lastTimestamp);
    if (switches.share_time_switch === 0){//0:分游戏计算时长，1:统一计算时长
      let playInfo = await userPlayDuration.findPlayInfoByGame(day,unique_id, game);
      use_time = lodash.isEmpty(playInfo) ? 0 : playInfo.duration;
    }else{
      use_time = await userPlayDuration.getTotalTime(day,unique_id);
    }
    //开关
    //console.log(user_duration);
    let max_time = await this.getMaxTime(game, userInfo,switches);
    let remain_time = (max_time - use_time) >= 0 ? max_time - use_time : 0;
    let costTime = use_time > max_time ? max_time : use_time;
    let tipService = await this.getTips(game,userInfo,switches,remain_time,costTime,is_login,first_login);
    const GameGroupModel = this.ctx.model.GameGroup;
    let FcmSwitch = await GameGroupModel.getEnableFcmByGame(game);
    if(FcmSwitch === 0 || FcmSwitch === 2){
      tipService.restrictType = 0;
    }
    return {
      remainTime: tipService.remainTime,
      costTime: costTime,
      restrictType: tipService.restrictType,
      title: tipService.title,
      description: tipService.description
    };
  }
  /**
   * 获取最大娱乐时间
   * @param game 游戏名称
   * @param userInfo 用户信息
   * @param switches 防沉迷配置信息
   * @returns {Promise<{duration: *, is_holiday: *}>}
   */
  async getMaxTime(game, userInfo,switches){
    const HolidayModel = this.ctx.model.GameHolidayJson;
    let holiday_config = await HolidayModel.getHoliday(game);
    if (lodash.isEmpty(holiday_config)) {
      throw new Error('请先行配置节假日');
    }
    //是否节假日
    let month = this.ctx.helper.getMonth();
    let day = this.ctx.helper.getDay();
    let holiday_json = JSON.parse(holiday_config.holiday_dates);
    let shiming_user_duration;
    shiming_user_duration = userInfo.identify_state === 0 ? switches.no_identify_time : switches.child_common_time;
    if (holiday_json[month] !== undefined && holiday_json[month].length !== 0 && userInfo.identify_state === 1) {//实名用户节假日有额外时长
      if (holiday_json[month][day] !== undefined && holiday_json[month][day].length !== 0) {
        shiming_user_duration = switches.child_holiday_time;
      }
    }
    return shiming_user_duration;
  }

  /**
   *  //1-宵禁剩余时间提示 2-时长剩余提示 5-已经处于宵禁提示 6-时长耗尽提示
   * @param game //
   * @param userInfo //用户信息
   * @param switches //防沉迷配置信息
   * @param remain_time  //剩余时间
   * @param used_duration //娱乐时间
   * @param is_login 是否登陆触发 0-不是 1-是
   * @param first_login 账号是否首次登陆触发 0-不是 1-是
   * @returns {Promise<{restrictType: *, description: *, title: *}>}
   */
  async getTips(game,userInfo,switches,remain_time,used_duration,is_login,first_login){
    const UiConfigModel = this.ctx.model.UiConfig;
    let belong_group = await this.getGroupByGame(game);
    let current_time = this.ctx.helper.getNow();
    let tips_type = 0;//触发的文案类型
    let restrictType = 0;
    let curfew_start = this.ctx.helper.getTimeStamp(switches.night_strict_start);
    let curfew_diff_time = this.ctx.helper.getTimeStamp(switches.night_strict_start) - current_time;
    let tips_time = 0;           //文案中替换的时长，单位秒
    let tip_remain_time = remain_time;//返回的剩余时长，单位秒
    //是否触发
    let isOutOfGameTime = await this.service.fcmTipsService.OutOfGameTime(belong_group, userInfo, remain_time);
    let isWarningOfGameTime = await this.service.fcmTipsService.WarningOfGameTime(belong_group, userInfo, current_time, remain_time, switches,is_login,first_login);
    let isInCurfew = await this.service.fcmTipsService.InCurfew(belong_group, userInfo, current_time, remain_time, switches);
    let isWarningCurfew = await this.service.fcmTipsService.WarningCurfew(belong_group, userInfo, current_time, remain_time, switches);

    if(isInCurfew !== false){//宵禁中
      tips_type = isInCurfew;
      restrictType = 1;
      tip_remain_time = 0;
    }else if(isOutOfGameTime !== false){//时长用尽
      tips_type = isOutOfGameTime;
      restrictType = 2;
      tips_time = used_duration;
      tip_remain_time = 0;
    }else if(isWarningCurfew !== false && isWarningOfGameTime !== false){//需比较优先级
      tips_type = curfew_diff_time > remain_time ? isWarningOfGameTime : isWarningCurfew;
      restrictType = curfew_diff_time > remain_time ? 2 : 1;
      tips_time = curfew_diff_time > remain_time ? remain_time : curfew_start-current_time;
      tip_remain_time = tips_time;
    }else if(isWarningCurfew !== false){//提醒宵禁
      tips_type = isWarningCurfew;
      restrictType = 1;
      tips_time = curfew_start-current_time;
      tip_remain_time = tips_time;
    }else if(isWarningOfGameTime !==false){//提醒时长
      tips_type = isWarningOfGameTime;
      restrictType = 2;
      tips_time = remain_time;
    }else{//不限制

    }
    let title = '';
    let description = '';
    // console.log(tips_type);
    // console.log(userInfo);
    // console.log([1,2,3].includes(userInfo.account_type));
    if(tips_type !== 0){
      let user_type = [1,2,3].includes(userInfo.account_type) === true ? 1 : userInfo.account_type; //0-游客未实名 1-已实名未成年 5-正式用户未实名
      let uiInfo = await UiConfigModel.getUiByTypeAndAccountType(belong_group,tips_type,user_type);
      if(lodash.isEmpty(uiInfo)){
        throw new Error('请先行配置防沉迷文案'+tips_type+'：'+userInfo.account_type);
      }
      title = uiInfo.title;
      description = uiInfo.description;
      description = description.replace(/# \$\{remaining\} #/g,Math.ceil(tips_time /60));
    }
    return {title: title, description: description, restrictType: restrictType, remainTime: tip_remain_time};
  }

  /**
   * 根据游戏名称返回文案文案版本
   * @param game
   * @returns {Promise<number>} 1-线上版 2-默认版署版
   */
  async getGroupByGame(game){
    const GameGroupModel = this.ctx.model.GameGroup;
    let game_group = await GameGroupModel.findByGame(game);
    return lodash.isNull(game_group) ? 2 : game_group.group;
  }

  /**
   * 防沉迷配置
   * @param game
   * @returns
   */
  async getConfig(game) {
    const ChildProtectedModel = this.ctx.model.ChildProtectedConfig;
    const UiConfigModel = this.ctx.model.UiConfig;
    const userLimitConfigModel = this.ctx.model.UserLimitConfig;
    const HolidayModel = this.ctx.model.GameHolidayJson;

    let belong_group = await this.getGroupByGame(game);
    let switches = await ChildProtectedModel.getSwitch();
    let child_data = lodash.isEmpty(switches) ? {} : switches.dataValues;
    //限额配置
    let pay_limit_words = await UiConfigModel.getPayLimitUi();
    let limit_conf = await userLimitConfigModel.getAllLimit();
    let limit_conf_arr = {};
    for (let key2 in limit_conf){
      if(!limit_conf_arr[limit_conf[key2].account_type]){
        limit_conf_arr[limit_conf[key2].account_type] = {};
      }
      limit_conf_arr[limit_conf[key2].account_type] = {pay_month_limit:limit_conf[key2].pay_month_limit,pay_limit:limit_conf[key2].pay_limit};
    }
    //console.log(limit_conf_arr);
    let data = [];
    for (let key in pay_limit_words) {
      let account_type = pay_limit_words[key].account_type;
      if (!data[account_type]) {
        data[account_type] = {};
      } else {
        data[account_type].account_type = account_type;
      }
      if (pay_limit_words[key].type === 3) {
        data[account_type].single_title = pay_limit_words[key].title;
        data[account_type].single_description = pay_limit_words[key].description;
        data[account_type].single_limit = limit_conf_arr[account_type] === undefined ? 0 : limit_conf_arr[account_type].pay_limit;
      }
      if (pay_limit_words[key].type === 4) {
        data[account_type].month_title = pay_limit_words[key].title;
        data[account_type].month_description = pay_limit_words[key].description;
        data[account_type].month_limit = limit_conf_arr[account_type] === undefined ? 0 : limit_conf_arr[account_type].pay_month_limit;
      }
    }
    data = Object.values(data);
    //时长配置
    let health_data = {};
    let health_reminder_words = await UiConfigModel.getHealthUi(belong_group);
    for (let key2 in health_reminder_words) {
      let account_type = health_reminder_words[key2].account_type;
      if (!health_data[account_type]) {
        health_data[account_type] = {account_type:account_type,tips:[]};
      }
      const type = health_reminder_words[key2].type;
      // let title_name = `title_${type}`;
      // let description_name = `description_${type}`;
      let data = {type:type,title:health_reminder_words[key2].title,description:health_reminder_words[key2].description};
      health_data[account_type].tips.push(data);
    }
    health_data = Object.values(health_data);
    //节假日
    let holiday_config = await HolidayModel.getHoliday(game);
    let holiday_json = JSON.parse(holiday_config.holiday_dates);
    let holiday_data = [];
    for(let key3 in holiday_json){
      if(!holiday_json[key3]){
        holiday_json[key3] = {};
      }
      for(let key4 in holiday_json[key3]){
        let str = key3.substring(4,6)+'.'+key4;
        holiday_data.push(str);
      }
    }

    return {
      child_protected_config: child_data,
      ui_config: { pay_limit_words: data, health_reminder_words: health_data },
      holiday: holiday_data,
    };
  }
}

module.exports = FcmService;
