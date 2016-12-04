#ifndef RELAYS_H
#define RELAYS_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <string.h>

#define RELAY_1_PORT GPIOB
#define RELAY_2_PORT GPIOB
#define RELAY_3_PORT GPIOB
#define RELAY_4_PORT GPIOB
#define RELAY_5_PORT GPIOB
#define RELAY_6_PORT GPIOB
#define RELAY_7_PORT GPIOC
#define RELAY_8_PORT GPIOC

#define RELAY_1_PIN GPIO_Pin_12
#define RELAY_2_PIN GPIO_Pin_11
#define RELAY_3_PIN GPIO_Pin_10
#define RELAY_4_PIN GPIO_Pin_2
#define RELAY_5_PIN GPIO_Pin_1
#define RELAY_6_PIN GPIO_Pin_0
#define RELAY_7_PIN GPIO_Pin_5
#define RELAY_8_PIN GPIO_Pin_4

enum Relay {RELAY_1, RELAY_2, RELAY_3, RELAY_4, RELAY_5, RELAY_6, RELAY_7, RELAY_8};
class Relays {

    public:
        Relays();
        void init();
        void setState(Relay relay, bool state);
        bool getState(Relay relay);
        void allOff();

};

#endif