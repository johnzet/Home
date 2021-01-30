local TEMP_MEAS_CMD = 0x03;
local HUM_MEAS_CMD	= 0x05;
local READ_STATUS_CMD = 0x07;
--local WRITE_STATUS_CMD = 0x06;
local SCL = 6;  -- GPIO12
local SDA = 7;  -- GPIO13
local lastDataPinValue;


function getTemperature()
    _init();

    local sensorTempBytes = _readFromSensor(TEMP_MEAS_CMD, 350000, 2);
    local tempWord = sensorTempBytes[1] * 256 + sensorTempBytes[2];
    return -39.6 + .01*tempWord;
end

function getHumidity()
    _init();

    local sensorHumBytes = _readFromSensor(HUM_MEAS_CMD, 100000, 2);
    local humWord = sensorHumBytes[1] * 256 + sensorHumBytes[2];
    return -2.0468 + 0.0367*humWord + -1.5955e-6*humWord^2;
end

function _init()
    gpio.mode(SCL, gpio.OUTPUT);
    gpio.mode(SDA, gpio.INPUT);
    gpio.write(SDA, gpio.HIGH);
end

function _setClockPin(state)
        gpio.write(SCL, state);
end

function _setDataPin(value)
    if value==1 and lastDataPinValue ~= 1 then
        gpio.mode(SDA, gpio.INPUT);
        lastDataPinValue = 1;
    end
    if value==0 and lastDataPinValue ~= 0 then
        gpio.mode(SDA, gpio.OUTPUT);
        gpio.write(SDA, gpio.LOW);
        lastDataPinValue = 0;
    end
end

function _readFromSensor(address, usDelay, numBytes)
    tmr.delay(15000);

    _sendStart();
    _sendCommand(address);

    tmr.delay(usDelay);

    return _readBytes(numBytes);
end

function _sendStart()
    _setClockPin(0);
    tmr.delay(100);
    _setClockPin(1);
    _setDataPin(0);
    _setClockPin(0);
    _setClockPin(1);
    _setDataPin(1);
    tmr.delay(100);
    _setClockPin(0);
    _setDataPin(0);
end

function _sendCommand(command)
    local i;
    for i=7,0,-1 do
        _setClockPin(0);
        if bit.isset(command, i) then
            _setDataPin(1);
        else
            _setDataPin(0);
        end
        _setClockPin(1);
    end
    _setDataPin(1);
    _setClockPin(0);
    _setClockPin(1);
    _setClockPin(0);
end

function _readBytes(numBytes)
    local result = {};
    for byteIndex=1,numBytes do
        result[byteIndex] = 0;
        for bitIndex=7,0,-1 do
            local b = gpio.read(SDA);
            if b == 1 then result[byteIndex] = bit.set(result[byteIndex], bitIndex); end
            _setClockPin(1);
            _setClockPin(0);
        end
        if byteIndex < numBytes then
            _setDataPin(0);
        end
        _setClockPin(1);
        _setClockPin(0);
        if byteIndex < numBytes then
            _setDataPin(1);
        end
    end
    return result;
end