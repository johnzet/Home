#ifndef WATER_VALVE_H
#define WATER_VALVE_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <Relays.h>
#include <FreeRTOS.h>
#include <portable.h>
#include <delay.h>

#define VALVE_OPEN false
#define VALVE_CLOSED true
#define SENSE_VALVE_CLOSED GPIO_Pin_7
#define SENSE_VALVE_OPEN GPIO_Pin_8
#define RELAY_CLOSE RELAY_1
#define RELAY_OPEN RELAY_2
#define RELAY_ALARM RELAY_3
#define RELAY_OPEN_BYPASS RELAY_4


class WaterValve {
    private:
        Relays* relays;
        bool state;

    public:
        WaterValve();
        ~WaterValve();
        void init();
        void setTestMode(bool testMode);
        void closeValve();
        void openValve();
        void closeBypassValve();
        void openBypassValve();
        void checkState();
        bool isValveOpen();
        bool isValveClosed();
        void soundAlarm();
        void silenceAlarm();
};


#endif