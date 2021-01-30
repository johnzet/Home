#ifndef LEDS_H
#define LEDS_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>

// LEDs are on port C
#define RED_LED GPIO_Pin_1
#define GREEN_LED GPIO_Pin_2
#define BLUE_LED GPIO_Pin_3

class LEDs {

    public:
        LEDs();
        void init();
        void allOff();
        void setRedState(bool state);
        void setGreenState(bool state);
        void setBlueState(bool state);
};

#endif