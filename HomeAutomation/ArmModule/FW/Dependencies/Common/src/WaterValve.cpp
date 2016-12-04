#include <WaterValve.h>


WaterValve::WaterValve() {
    this->relays = new Relays();
    pvPortSetTypeName(this->relays, "Relays");
}

WaterValve::~WaterValve() {}

void WaterValve::init() {
    GPIO_InitTypeDef GPIO_InitStructure;
    
    // PC7 - valve is closed,      PC8 - valve is open
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

    GPIO_InitStructure.GPIO_Pin = SENSE_VALVE_CLOSED | SENSE_VALVE_OPEN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_OD;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
    GPIO_Init(GPIOC, &GPIO_InitStructure);

    delay_ms(10);

    if (this->isValveClosed()) {
        this->state = VALVE_CLOSED;
    } else {
        this->state = VALVE_OPEN;
    }
    relays->init();
    relays->setState(RELAY_1, false);
    relays->setState(RELAY_2, false);
}

bool WaterValve::isValveOpen() {
    return ((GPIOC->IDR & SENSE_VALVE_OPEN) == 0);
}

bool WaterValve::isValveClosed() {
    return ((GPIOC->IDR & SENSE_VALVE_CLOSED) == 0);
}

void WaterValve::closeValve() {
    this->state = VALVE_CLOSED;
}

void WaterValve::openValve() {
    this->state = VALVE_OPEN;
}
        
void WaterValve::closeBypassValve() {
    this->relays->setState(RELAY_OPEN_BYPASS, false);
}

void WaterValve::openBypassValve() {
    this->relays->setState(RELAY_OPEN_BYPASS, true);
}

void WaterValve::soundAlarm() {
    this->relays->setState(RELAY_ALARM, true);
}

void WaterValve::silenceAlarm() {
    this->relays->setState(RELAY_ALARM, false);
}


void WaterValve::checkState() {
    bool isValveOpen = this->isValveOpen();
    bool isValveClosed = this->isValveClosed();
    bool isRelayOpen = relays->getState(RELAY_OPEN);
    bool isRelayClosed = relays->getState(RELAY_CLOSE);
    bool cmdValveOpen = false;
    bool cmdValveClosed = false;

    if (this->state == VALVE_OPEN && !isValveOpen) {
        cmdValveOpen = true;
    }

    if (this->state == VALVE_CLOSED && !isValveClosed) {
        cmdValveClosed = true;
    }

    if (cmdValveOpen != isRelayOpen) {
        relays->setState(RELAY_OPEN, cmdValveOpen);
    }

    if (cmdValveClosed != isRelayClosed) {
        relays->setState(RELAY_CLOSE, cmdValveClosed);
    }
}