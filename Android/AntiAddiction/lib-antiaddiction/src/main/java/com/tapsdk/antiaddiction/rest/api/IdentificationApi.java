package com.tapsdk.antiaddiction.rest.api;

import com.tapsdk.antiaddiction.entities.IdentificationInfo;
import com.tapsdk.antiaddiction.entities.request.IdentifyRequestParams;
import com.tapsdk.antiaddiction.entities.response.IdentifyResult;
import com.tapsdk.antiaddiction.reactor.Observable;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.Body;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.GET;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.POST;
import com.tapsdk.antiaddiction.skynet.retrofit2.http.Query;

public interface IdentificationApi {

    /**
     * Inquire identification info
     * Method get
     * Path(/v1/identification/info)
     *
     * @param token
     * @return {identify_state, id_card, name, anti_addiction_token}
     */
    @GET("api/v1/identification/info")
    Observable<IdentificationInfo> inquireIdentificationInfo(@Query("user_id") String token);

    /**
     * Identify user
     * Method post
     * Path(/v1/identification)
     *
     * @param identifyRequestParams
     *
     * @return {IdentifyResult}
     */
    @POST("api/v1/identification")
    Observable<IdentifyResult> identifyUser(@Body IdentifyRequestParams identifyRequestParams);
}
