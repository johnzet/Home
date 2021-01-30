#include <LEDs.h>

LEDs::LEDs() {
}

void LEDs::init() {
    GPIO_InitTypeDef GPIO_InitStructure;

    // red, green, blue: PC1, PC2, PC3
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

    GPIO_InitStructure.GPIO_Pin = GPIO_Pin_1 | GPIO_Pin_2 | GPIO_Pin_3;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOC, &GPIO_InitStructure);

    allOff();
}
 
void LEDs::allOff() {
    GPIOC->BSRRH = RED_LED;
    GPIOC->BSRRH = GREEN_LED;
    GPIOC->BSRRH = BLUE_LED;
}

void LEDs::setRedState(bool state) {
    if (state) {
        GPIOC->BSRRL = RED_LED;
    } else {
        GPIOC->BSRRH = RED_LED;
    }
}

void LEDs::setGreenState(bool state) {
    if (state) {
        GPIOC->BSRRL = GREEN_LED;
    } else {
        GPIOC->BSRRH = GREEN_LED;
    }
}

void LEDs::setBlueState(bool state) {
    if (state) {
        GPIOC->BSRRL = BLUE_LED;
    } else {
        GPIOC->BSRRH = BLUE_LED;
    }
}
