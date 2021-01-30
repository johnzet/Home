print("Creating server");
srv=net.createServer(net.TCP);

print(wifi.sta.getip());

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
	-- print(method.."  "..path or "".."  "..vars or "")
	-- print(_GET["zone"])
	if string.match(path, "/getall") ~= nil then
	    buf = handleGetStateRequest();
	elseif string.match(path, "/on") ~= nil and _GET["zone"] ~= nil  then
	    buf = handleSetStateRequest(_GET["zone"] + 0);
	elseif string.match(path, "/off") ~= nil then
	    buf = handleSetStateRequest(0);
	else
	    buf = handleHelpRequest();
	end

	client:send(buf);
	client:close();
	collectgarbage();
    end);
end)

function handleGetStateRequest() 
    local state = getRelayState()
    local stateStr = string.format("%x", state);
    if (string.len(stateStr) == 1) then stateStr = "0"..stateStr end
    
    local buf = "{state: 0x" .. stateStr .. "}"
    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: application/json\n";
    header = header.."Content-Length: "..string.len(buf).."\n\n";
    return header..buf;
end

function handleSetStateRequest(relay) 
    local state = setRelayState(relay)

    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: application/json\n";
    header = header.."Content-Length: ".."4".."\n\n";
    return header.."Done";
end

function handleHelpRequest()
    local buf = "<http>"
    buf = buf.. "Usage:<ul>"
    buf = buf.. "<li>/getall (Get all relay states)</li>"
    buf = buf.. "<li>/on?zone=n  (Turn on a zone valve)</li>"
    buf = buf.. "<li>/off (Turn all zones off)</li>"
    buf = buf.. "<li>/ (This usage statement)  </li></ul></http>"
    local header = "HTTP/1.1 200 OK\n";
    header = header.."Content-Type: text/html\n";
    header = header.."Content-Length: "..string.len(buf).."\n\n";
    return header..buf;

end
