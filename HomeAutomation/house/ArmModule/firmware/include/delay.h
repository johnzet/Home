#ifndef DELAY_H
#define DELAY_H

#include <FreeRTOS.h>
#include <task.h>
#include <stdint.h>

static inline void delay_us(uint32_t us) {
    us *= 13;  // empirical

    asm volatile("   mov r0, %[us]          \n\t"
                 "1: subs r0, #1            \n\t"
                 "   bhi 1b                 \n\t"
                 :
                 : [us] "r" (us)
                 : "r0");
}

static inline void delay_ms(const uint32_t ms) {
    if (xTaskGetSchedulerState() == taskSCHEDULER_NOT_STARTED) {
        if (ms>50) {
            for (uint32_t i=0; i<ms/50UL; i++) delay_us(50*1000);
            delay_us((ms%50UL)*1000);
        } else {
            delay_us(ms*1000);
        }
    } else {
        vTaskDelay(ms);
    }
}


#endif