package com.taptap.tds.registration.server.service;

import com.google.common.collect.Lists;
import com.taptap.tds.registration.server.configuration.PublicityProperties;
import com.taptap.tds.registration.server.core.datastore.DataStore;
import com.taptap.tds.registration.server.core.domain.FieldsExpand;
import com.taptap.tds.registration.server.core.domain.IdEntity;
import com.taptap.tds.registration.server.domain.IdentificationDetails;
import com.taptap.tds.registration.server.domain.UserAction;
import com.taptap.tds.registration.server.dto.PublicityResult;
import com.taptap.tds.registration.server.enums.IdentificationStatus;
import com.taptap.tds.registration.server.enums.UserType;
import com.taptap.tds.registration.server.manager.IdentificationDetailsManager;
import com.taptap.tds.registration.server.manager.UserActionManager;
import com.taptap.tds.registration.server.util.Collections3;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Log4j2
@EnableScheduling
@Service
public class PublicityService {

    @Autowired
    private IdentificationDetailsManager identificationDetailsManager;

    @Autowired
    private UserActionManager userActionManager;

    @Autowired
    private DataStore<UserAction> dataStore;

    @Autowired
    private PublicityClient publicityClient;

    @Autowired
    private PublicityProperties properties;

    private Executor executor = Executors.newFixedThreadPool(10);

    public void check(){
        List<IdentificationDetails> identificationDetails = identificationDetailsManager.findByStatus(IdentificationStatus.IDENTIFYING, FieldsExpand.createWithFields("ai", "id"));

        if(Collections3.isNotEmpty(identificationDetails)) {
            for (IdentificationDetails identificationDetail : identificationDetails){
                try {
                    PublicityResult publicityResult = publicityClient.checkIdentification(identificationDetail.getAi());

                    if(publicityResult.getData() == null || publicityResult.getData().getResult() == null){
                        log.error("实名认证查询失败: code is {}, id is {}" , publicityResult.getErrcode(), identificationDetail.getId());
                        identificationDetail.setStatus(IdentificationStatus.FAILED);
                        identificationDetailsManager.updateNonNull(identificationDetail);
                        continue;
                    }
                    String pi = publicityResult.getData().getResult().getPi();
                    IdentificationStatus status = publicityResult.getData().getResult().getStatus();

                    if(status!=IdentificationStatus.IDENTIFYING){
                        identificationDetail.setPi(pi);
                        identificationDetail.setStatus(status);
                        identificationDetailsManager.updateNonNull(identificationDetail);
                    }
                } catch (Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
    }


    @Scheduled(cron = "0/10 * * * * ?")
    @Transactional
    public void bulkSave() {
        Collection<UserAction> userActions = this.dataStore.bulkRetrieve();
        if (Collections3.isEmpty(userActions)) {
            return;
        }
        List<IdentificationDetails> ids = identificationDetailsManager.findByUserIdIn(Collections3.transformToList(userActions, UserAction::getUserId), FieldsExpand.createWithFields("userId", "pi"));

        Map<String, String> userId2PiMap = Collections3.transformToMap(ids, IdentificationDetails::getUserId, IdentificationDetails::getPi);

        // 每个用户行为去看有没有对应的pi 有就是实名用户，否则是游客。
        for(UserAction userAction : userActions){
            String pi;
            if((pi = userId2PiMap.get(userAction.getUserId()))!=null){
                userAction.setUserType(UserType.AUTHENTICATED_USER);
                userAction.setPi(pi);
            } else {
                userAction.setUserType(UserType.TOURIST);
                userAction.setDeviceId(userAction.getUserId());
            }
        }
        userActionManager.bulkSave(userActions);
    }

    @Transactional
    public void uploadUserActionToPublicity() {
        List<UserAction> toPush = userActionManager.findByPushSuccessAndActionTimeGreaterThanEqual(false, Instant.now().minusSeconds(180L), null);
        if (Collections3.isNotEmpty(toPush)) {
                try {
//                    long l = System.currentTimeMillis();
                    uploadOneGame(toPush);
//                    long duration = System.currentTimeMillis()-l;
                    //如果小于100毫秒 等到100毫秒 防止限流
//                    if(duration < 100){
//                        TimeUnit.MILLISECONDS.sleep(100-duration);
//                    }
                } catch(Exception e){
                    log.info("【pushed failed】 push userActions to publicity failed! e is {}", e.getMessage());
                }
            }
    }

    private void uploadOneGame(List<UserAction> userActions) throws InterruptedException {

        List<List<UserAction>> lists = Lists.partition(userActions, properties.getUserActionBatchSize());
        CountDownLatch cdl = new CountDownLatch(lists.size());
        for (List<UserAction> toPush : lists){
            executor.execute(new Task(cdl, toPush));
        }
        cdl.await();
    }

    class Task implements Runnable{

        private CountDownLatch c;

        private List<UserAction> toPush;

        public Task(CountDownLatch c, List<UserAction> toPush) {
            this.c = c;
            this.toPush = toPush;
        }

        @Override
        public void run() {

            try {
                PublicityResult result = publicityClient.uploadUserAction(toPush);

                if (result.getErrcode() == 0 && StringUtils.equalsIgnoreCase(result.getErrmsg(), "OK")) {

                    if (properties.isRemoveAfterUpload()) {
                        userActionManager.bulkDeleteByIds(Collections3.transformToList(toPush, IdEntity::getId));
                    } else {
                        userActionManager.updatePushSuccessByIdIn(Collections3.transformToList(toPush, IdEntity::getId), true);
                    }
                } else {
                    log.error("【pushed failed】push userActions to publicity failed! ErrorCode is {} , msg is {}", result.getErrcode(), result.getErrmsg());
                }
            } catch (Exception e ){
                log.error("【pushed failed】 push userActions to publicity failed! e : " + e);
            } finally {
                c.countDown();
            }
        }
    }

}
