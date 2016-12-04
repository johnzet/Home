#include <LEDs.h>

LEDs::LEDs() {
}

void LEDs::init() {
    GPIO_InitTypeDef GPIO_InitStructure;

    // red, green, blue: PC0, PC1, PC2
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

    GPIO_InitStructure.GPIO_Pin = RED_LED | GREEN_LED | BLUE_LED;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOC, &GPIO_InitStructure);

    allOff();
}
 
void LEDs::allOff() {
    GPIO_SetBits(GPIOC, RED_LED);
    GPIO_SetBits(GPIOC, GREEN_LED);
    GPIO_SetBits(GPIOC, BLUE_LED);
}

void LEDs::setRedState(bool state) {
    if (state) {
        GPIO_ResetBits(GPIOC, RED_LED);
    } else {
        GPIO_SetBits(GPIOC, RED_LED);
    }
}

void LEDs::setGreenState(bool state) {
    if (state) {
        GPIO_ResetBits(GPIOC, GREEN_LED);
    } else {
        GPIO_SetBits(GPIOC, GREEN_LED);
    }
}

void LEDs::setBlueState(bool state) {
    if (state) {
        GPIO_ResetBits(GPIOC, BLUE_LED);
    } else {
        GPIO_SetBits(GPIOC, BLUE_LED);
    }
}
