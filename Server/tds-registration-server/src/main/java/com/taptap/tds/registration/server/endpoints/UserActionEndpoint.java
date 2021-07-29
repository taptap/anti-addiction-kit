package com.taptap.tds.registration.server.endpoints;

import com.taptap.tds.registration.server.ApiResponseDto;
import com.taptap.tds.registration.server.core.datastore.DataStore;
import com.taptap.tds.registration.server.domain.UserAction;
import com.taptap.tds.registration.server.dto.UserActionRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@Log4j2
@RestController
public class UserActionEndpoint {

    @Autowired
    private DataStore<UserAction> dataStore;

    @PostMapping(path="/api/v1/nationfcm/behavior")
    public ResponseEntity<?> uploadUserAction(UserActionRequest request){
        UserAction userAction = new UserAction();
        userAction.setUserId(request.getUserId());
        userAction.setActionType(request.getLoginflag());
        userAction.setSessionId(request.getSessionId());
        userAction.setActionTime(Instant.now());
        dataStore.store(userAction);
        return ResponseEntity.accepted().body(new ApiResponseDto(200, null, null));
    }

}
