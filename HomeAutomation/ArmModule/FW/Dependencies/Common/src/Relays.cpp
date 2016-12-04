#include <Relays.h>

Relays::Relays() {

}

void Relays::init() {
    GPIO_InitTypeDef GPIO_InitStructure;

    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOB, ENABLE);

    GPIO_InitStructure.GPIO_Pin = RELAY_1_PIN | RELAY_2_PIN | RELAY_3_PIN | RELAY_4_PIN | RELAY_5_PIN | RELAY_6_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOB, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin =  RELAY_7_PIN | RELAY_8_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOC, &GPIO_InitStructure);

    allOff();
}

void Relays::setState(Relay relay, bool state) {
    switch(relay) {
        case(RELAY_1):
            if (state) GPIO_SetBits(RELAY_1_PORT, RELAY_1_PIN);
            else       GPIO_ResetBits(RELAY_1_PORT, RELAY_1_PIN);
            break;
        case(RELAY_2):
            if (state) GPIO_SetBits(RELAY_2_PORT, RELAY_2_PIN);
            else       GPIO_ResetBits(RELAY_2_PORT, RELAY_2_PIN);
            break;
        case(RELAY_3):
            if (state) GPIO_SetBits(RELAY_3_PORT, RELAY_3_PIN);
            else       GPIO_ResetBits(RELAY_3_PORT, RELAY_3_PIN);
            break;
        case(RELAY_4):
            if (state) GPIO_SetBits(RELAY_4_PORT, RELAY_4_PIN);
            else       GPIO_ResetBits(RELAY_4_PORT, RELAY_4_PIN);
            break;
        case(RELAY_5):
            if (state) GPIO_SetBits(RELAY_5_PORT, RELAY_5_PIN);
            else       GPIO_ResetBits(RELAY_5_PORT, RELAY_5_PIN);
            break;
        case(RELAY_6):
            if (state) GPIO_SetBits(RELAY_6_PORT, RELAY_6_PIN);
            else       GPIO_ResetBits(RELAY_6_PORT, RELAY_6_PIN);
            break;
        case(RELAY_7):
            if (state) GPIO_SetBits(RELAY_7_PORT, RELAY_7_PIN);
            else       GPIO_ResetBits(RELAY_7_PORT, RELAY_7_PIN);
            break;
        case(RELAY_8):
            if (state) GPIO_SetBits(RELAY_8_PORT, RELAY_8_PIN);
            else       GPIO_ResetBits(RELAY_8_PORT, RELAY_8_PIN);
            break;
    }
}

bool Relays::getState(Relay relay) {
    bool state = NULL;

    switch(relay) {
        case(RELAY_1):
            state = RELAY_1_PORT->IDR & RELAY_1_PIN;
            break;
        case(RELAY_2):
            state = RELAY_2_PORT->IDR & RELAY_2_PIN;
            break;
        case(RELAY_3):
            state = RELAY_3_PORT->IDR & RELAY_3_PIN;
            break;
        case(RELAY_4):
            state = RELAY_4_PORT->IDR & RELAY_4_PIN;
            break;
        case(RELAY_5):
            state = RELAY_5_PORT->IDR & RELAY_5_PIN;
            break;
        case(RELAY_6):
            state = RELAY_6_PORT->IDR & RELAY_6_PIN;
            break;
        case(RELAY_7):
            state = RELAY_7_PORT->IDR & RELAY_7_PIN;
            break;
        case(RELAY_8):
            state = RELAY_8_PORT->IDR & RELAY_8_PIN;
            break;
    }
    return state;
}

void Relays::allOff() {
    for (int relay = RELAY_1; relay <= RELAY_8; relay++) {
        setState(static_cast<Relay>(relay), false);
    }
}