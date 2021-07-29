-- local session_key = KEYS[1]
-- local partition = tonumber(ARGV[4])
local ttl = tonumber(ARGV[4])
local expected_last_sessionId = ARGV[5]
local last_serverId = ""
local is_success = "false"

-- key不存在会返回false
local last_sessionId = redis.call("hget", KEYS[1], "id")
if last_sessionId == false then
  last_sessionId = ""
end

-- cas
if last_sessionId == expected_last_sessionId then
  redis.call("hmset", KEYS[1], "id" ,ARGV[1] ,"serverId" ,ARGV[2] ,"createdAt" ,ARGV[3])
  redis.call("expire", KEYS[1], ttl)
  is_success = "true"
else
  if last_sessionId ~= "" then
    last_serverId = redis.call("hget", KEYS[1], "serverId")
  end
end
return { is_success, last_sessionId, last_serverId }
