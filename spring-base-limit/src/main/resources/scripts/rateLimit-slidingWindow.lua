local key = KEYS[1]
local window = tonumber(ARGV[1])      -- 窗口毫秒数
local threshold = tonumber(ARGV[2])   -- 请求阈值
local member = ARGV[3]               -- 唯一请求标识
local now = tonumber(ARGV[4])         -- 客户端时间（毫秒）

-- 计算窗口起始时间并清理旧数据
local windowStart = now - window
redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

-- 获取当前窗口内请求数（不包含当前请求）
local count = redis.call('ZCARD', key)

-- 流量决策
if count >= threshold then
    return 0  -- 直接拒绝
else
    -- 添加请求并设置过期时间
    redis.call('ZADD', key, now, member)
    -- 为何需要+1000ms缓冲：
    -- 1. 防止时间精度问题导致数据提前过期
    -- 2. 确保新进入的请求能完整经历窗口期
    redis.call('PEXPIRE', key, window + 1000)  -- 窗口+1000ms缓冲
    return 1  -- 允许通过
end