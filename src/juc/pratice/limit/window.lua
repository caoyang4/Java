redis.replicate_commands();

--获取KEY
local key = KEYS[1]

--获取ARGV内的参数并打印
local max_quantity = ARGV[1]
local window_width = ARGV[2]

--获取当前时间及时间边界
local time = redis.call('TIME') --返回值为当前所过去的秒数，当前秒所过去的微秒数
local timestamp = time[1] * 1000 + math.floor(time[2] / 1000)

local left_border = timestamp - window_width

--移除窗口外的值
redis.call('zremrangebyscore', key, 0, left_border)

--统计窗口内元素个数
local count = redis.call('zcard', key)

if count < tonumber(max_quantity) then
    redis.call('zadd', key, timestamp, timestamp)
    return 1
else
    return 0
end
