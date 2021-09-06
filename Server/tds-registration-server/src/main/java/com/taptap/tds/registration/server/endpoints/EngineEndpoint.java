package com.taptap.tds.registration.server.endpoints;

import com.taptap.tds.registration.server.ApiResponseDto;
import com.taptap.tds.registration.server.dto.UserActionRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
public class EngineEndpoint {
    @GetMapping(path="/")
    public ResponseEntity<?> health(){
        Map<String, String> result = new HashMap<>();
        result.put("status", "running");
        return ResponseEntity.accepted().body(new ApiResponseDto(200, null, result));
    }

}
