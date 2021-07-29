package com.taptap.tds.registration.server.util;

/**
 * @Author guyu
 * @create 2020/12/30 2:35 下午
 */
public interface Constants {

    String TDS_SESSION_KEY = "tds_session";

    String KAFKA_CLOSE_COMMAND_BODY = "{\"closeCommand\":{\"shouldRetry\":false}}";

    String CLOSE_MESSAGE = "{\"header\":{\"path\":\"/session/kick\"},\"body\":{\"closeCommand\":{\"shouldRetry\":true}}}";

    String KICK_TOPIC = "tds:kick:topic";
}
