local rateLimitKey = KEYS[1];
local rate = tonumber(ARGV[1]);
local rateInterval = tonumber(ARGV[2]);

local allowed = 1;
local ttlResult = 0;
local currValue = redis.call('incr', rateLimitKey);
if (currValue == 1) then
    redis.call('pexpire', rateLimitKey, rateInterval);
    allowed = 1;
else
    if (currValue > rate) then
        allowed = 0;
        ttlResult = redis.call('ttl', rateLimitKey);
    end
end
return { allowed, ttlResult }