--获取KEY
local key1 = KEYS[1]

--获取ARGV内的参数并打印
local max_quantity = ARGV[1]
local window_width = ARGV[2]

--这里漏桶的容量直接写死了，后续应该作为参数传入
local res = redis.call('CL.THROTTLE', key, 1000, max_quantity, window_width)

if res[1] == 0 then
    return 1
else
    return 0
end
