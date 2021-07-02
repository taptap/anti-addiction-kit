'use strict';

const Service = require('egg').Service;
const lodash = require('lodash');
class UserInfoService extends Service {

  async getUser(game,userId,uniqueId,birthday,localUserInfo) {
    const UserInfoModel = this.ctx.model.UserInfo;
    let identify = '';
    let name = '';
    let identifyState = 1;//1-实名 0-未实名
    if(lodash.isEmpty(birthday)){
        identifyState = 0;
    }
    if(identifyState === 1 && !lodash.isEmpty(localUserInfo)){//保存身份信息
      localUserInfo = JSON.parse(localUserInfo);
        if(!lodash.isEmpty(localUserInfo.identify) && !lodash.isEmpty(localUserInfo.name)){
          name = this.ctx.helper.md5(name);
          identify = this.ctx.helper.md5(identify);
          const IdentifyModel = this.ctx.model.Identify;
          await IdentifyModel.saveIdentify(userId,name,identify);
          uniqueId = this.ctx.helper.md5(identify);
        }
    }
    let accountType = await this.getAccountType(game,identifyState,birthday);
    //保存授权用户
    return UserInfoModel.saveUser(userId,uniqueId,identifyState,accountType,birthday,0);
  }

  /**
   * 实名类型 1：8岁以下 ，2：8-15岁，  3：16-17岁， 4：不需要防沉迷 5：未实名正式用户
   * @param game //根据游戏判断是否启用海外防沉迷
   * @param identifyState
   * @param birthday
   * @returns {Promise.<*>}
   * @private
   */
  async getAccountType(game,identifyState,birthday){
    let accountType = 0;
    if(identifyState === 1){
      let age = this.ctx.helper.getAgeByBirthday(birthday);
      if(age < 0 ){
        accountType = 0;
      } else if(age >= 0 && age < 8 ){
        accountType = 1;
      }else if(age >= 8 && age < 16){
        accountType = 2;
      }else if(age >= 16 && age < 18){
        accountType = 3;
      }else if (age >= 18){
        accountType = 4;
      }
    }else{
      accountType = 5;
    }
    return accountType;
  }

  async submitIdentify(user_id,name,identify){
    const UserInfoModel = this.ctx.model.UserInfo;
    let birthday = this.ctx.helper.getBirthday(identify);
    let accountType = await this.getAccountType('demo', 1,1,birthday,0);//客户端上传身份证，默认国内运营商，demo。
    name = this.ctx.helper.md5(name);
    identify = this.ctx.helper.md5(identify);
    let unique_id = this.ctx.helper.md5(identify);
    await UserInfoModel.updateIdentify(user_id,unique_id,name,identify,birthday,accountType);
  }
}

module.exports = UserInfoService;
