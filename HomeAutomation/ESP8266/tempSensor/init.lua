f = loadfile("SHT1x.lua");
f();
print("Loaded SHT1x.lua");

print("Connecting WIFI");
wifi.setmode(wifi.STATION);
wifi.sta.config("zhome","fydua1vare");
print(wifi.sta.getip());
led1 = 1
gpio.mode(led1, gpio.OUTPUT)

print("Creating server");
srv=net.createServer(net.TCP);


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
    local temperature = getTemperature();
    local humidity = getHumidity();

    local buf = string.format("{\"temperature\":%2.2f, \"humidity\":%2.2f}", temperature, humidity);
    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: application/json;charset=UTF-8\n";
    header = header.."Content-Length: "..string.len(buf).."\n\n";
    return header..buf;
end

function handleRootRequest(_GET)
    local temperature = getTemperature();
    local humidity = getHumidity();
    local buf;
    buf = "<!doctype html><html><h1> ESP8266 Web Server</h1>";
    buf = buf.."<p>Blue LED <a href=\"?pin=ON1\"><button>ON</button></a>&nbsp;<a href=\"?pin=OFF1\"><button>OFF</button></a></p>";
    local _on,_off = "",""
    if(_GET.pin == "ON1")then
        gpio.write(led1, gpio.LOW);
    elseif(_GET.pin == "OFF1")then
        gpio.write(led1, gpio.HIGH);
    end
    buf = buf..string.format("<p>%2.2f&deg;C&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;%i%%</p>", temperature, humidity+0.5);
    buf = buf.."</html>";

    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: text/html\n";
    header = header.."Content-Length: "..string.len(buf).."\n\n";
    return header..buf;
end

