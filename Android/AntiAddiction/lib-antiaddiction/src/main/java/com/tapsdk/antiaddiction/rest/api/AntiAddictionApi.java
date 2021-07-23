package com.tapsdk.antiaddiction.rest.api;

import com.tapsdk.antiaddiction.entities.CommonConfig;
import com.tapsdk.antiaddiction.entities.SubmitPlayLogResult;
import com.tapsdk.antiaddiction.entities.UserInfo;
import com.tapsdk.antiaddiction.entities.request.AuthenticateRequestParams;
import com.tapsdk.antiaddiction.entities.request.PlayLogRequestParams;
import com.tapsdk.antiaddiction.entities.response.ServerTime;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.skynet.retrofit2.Call;
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
    Call<CommonConfig> fetchConfig(@Query("game") String gameIdentifier);

    /**
     * Get server unix time
     * Method get
     * Path（/v3/fcm/get_server_time）
     *
     * @return {timestamp}
     */
    @GET("/v3/fcm/get_server_time")
    Call<String> fetchServerTimeSync();

    /**
     * Get server unix time
     * Method get
     * Path（/v3/fcm/get_server_time）
     *
     * @return {timestamp}
     */
    @GET("/v3/fcm/get_server_time")
    Observable<ServerTime> fetchServerTimeASync();


    /**
     * Report game duration period
     * Method post
     * Path(/v3/fcm/set_play_log)
     *
     * @param playLogRequestParams include play logs & game
     * @return {code,restrictType,remainTime}
     */
    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("/v3/fcm/set_play_log")
    Call<SubmitPlayLogResult> uploadPlayLogSync(@Body PlayLogRequestParams playLogRequestParams);


}
