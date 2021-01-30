id  = 0
sda = 4
scl = 5
devAddr = 0x3A;

i2c.setup(id, sda, scl, i2c.SLOW)

function getRelayState() 
    local stateStr = read_reg(0x01)
    --print("relay state = " .. string.format("0x%0x", string.byte(stateStr)))
    return (string.byte(stateStr))
end

function setRelayState(relay) 
    write_reg(0x00, 0x6E)
    if relay > 0 then
    	write_reg(0x00, 0x64 + relay)
    end
end

function read_reg(reg_addr)
    i2c.start(id)
    i2c.address(id, devAddr, i2c.TRANSMITTER)
    i2c.write(id, reg_addr)
    i2c.stop(id)
    i2c.start(id)
    i2c.address(id, devAddr, i2c.RECEIVER)
    c = i2c.read(id, 1)
    i2c.stop(id)
    return c
end

function write_reg(reg_addr, data)
    i2c.start(id)
    i2c.address(id, devAddr, i2c.TRANSMITTER)
    i2c.write(id, reg_addr)
    i2c.write(id, data)
    i2c.stop(id)
    return c
end
