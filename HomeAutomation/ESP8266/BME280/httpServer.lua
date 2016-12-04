print("Creating server");
srv=net.createServer(net.TCP);

print(wifi.sta.getip());

bme280.init(7,6,5,5,5,3);

print("Ready");
srv:listen(80,function(conn)
    conn:on("receive", function(client,request)

	local buf = "";

	local _, _, method, path, vars = string.find(request, "([A-Z]+) (.+)?(.+) HTTP");
	if(method == nil)then
	    _, _, method, path = string.find(request, "([A-Z]+) (.+) HTTP");
	end
	local _GET = {}
	if (vars ~= nil)then
	    for k, v in string.gmatch(vars, "(%w+)=(%w+)&*") do
		_GET[k] = v
	    end
	end
   	
	if string.match(path, "/data") ~= nil then
	    buf = handleDataRequest();
	elseif path == "/" then
	    buf = handleRootRequest(_GET);
	end


	client:send(buf);
	client:close();
	collectgarbage();
    end);
end)

function handleDataRequest()
    local p = bme280.baro();
    local t = bme280.temp();
    local h = bme280.humi();

    local buf = string.format("{\"temperature\":%2.2f, \"humidity\":%2.2f, \"baro\":%2.4f}", t/100, h/1000, p/10000);
    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: application/json;charset=UTF-8\n";
    header = header.."Content-Length: "..string.len(buf).."\n\n";
    return header..buf;
end

function handleRootRequest() 
    local p = bme280.baro();
    local t = bme280.temp();
    local h = bme280.humi();
    local humiStr = string.format("Humidity = %2.2f %%<br/>", h/1000);
    local tempStr = string.format("Temperature = %2.2f &deg;C<br/>", t/100);
    local baroStr = string.format("Barometric pressure = %2.4f hPa<br/>", p/10000);
    local buf = "<!doctype html><html><h1>Bosch BME280 Sensor</h1><be/>"..tempStr..humiStr..baroStr.."</html>"
    
    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: text/html\n";
    header = header.."Content-Length: "..string.len(buf).."\n\n";
    return header..buf;
end
