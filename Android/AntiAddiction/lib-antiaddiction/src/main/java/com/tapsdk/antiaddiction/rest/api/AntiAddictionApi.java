package com.tapsdk.antiaddiction.rest.api;

import com.tapsdk.antiaddiction.entities.AntiAddictionConfig;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.AuthenticateRequestParams;
import com.tapsdk.antiaddiction.entities.request.SubmitPlayLogRequestParams;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.Body;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.GET;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.Headers;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.POST;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.Query;

public interface AntiAddictionApi {

    /**
     * Authenticate in anti-addiction system
     * Method post
     * Path(/v3/fcm/authorizations)
     *
     * @param authenticateRequestParams @see {@link AuthenticateRequestParams}
     * @return {code, access_token, age}
     */
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("/v3/fcm/authorizations")
    Observable<UserInfo> authenticate(@Body AuthenticateRequestParams authenticateRequestParams);

    /**
     * Get config
     * method get
     * Path(/v3/fcm/get_config)
     *
     * @return {code,data}
     */
    @GET("/v3/fcm/get_config")
    Observable<AntiAddictionConfig> fetchConfig(@Query("game") String gameIdentifier);

    /**
     * Get server unix time
     * Method get
     * Path（/v3/fcm/get_server_time）
     *
     * @return {timestamp}
     */
    @GET("/v3/fcm/get_server_time")
    Observable<Long> fetchServerTime();

    /**
     * Report game duration period
     * Method post
     * Path(/v3/fcm/set_play_log)
     *
     * @param submitPlayLogRequestParams include play logs & game
     * @return {code,restrictType,remainTime}
     */
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("/v3/fcm/set_play_log")
    Observable<SubmitPlayLogResult> uploadPlayLog(@Body SubmitPlayLogRequestParams submitPlayLogRequestParams);


}
