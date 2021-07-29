package com.taptap.tds.registration.server.util;

import com.taptap.tds.registration.server.domain.TdsMessage;
import com.taptap.tds.registration.server.dto.CloseCommand;
import org.springframework.stereotype.Component;


/**
 * @Author guyu
 * @create 2021/1/18 6:47 下午
 */
@Component
public class PushGatewayMessageBuilder {

    public TdsMessage<CloseCommand> buildCloseMessage(boolean shouldRetry, String path){
        TdsMessage tdsMessage = new TdsMessage();
        tdsMessage.getHeader().put("path", path);
        CloseCommand closeCommand = new CloseCommand();
        closeCommand.setShouldRetry(shouldRetry);
        tdsMessage.setBody(closeCommand);
        return tdsMessage;
    }

}
