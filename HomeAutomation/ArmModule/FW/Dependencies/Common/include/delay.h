#ifndef DELAY_H
#define DELAY_H

#include <FreeRTOS.h>
#include <task.h>
#include <stdint.h>

static inline void delay_us(uint32_t us) {
    us *= 60;  // empirical

    asm volatile("   mov r0, %[us]          \n\t"
                 "1: subs r0, #1            \n\t"
                 "   bhi 1b                 \n\t"
                 :
                 : [us] "r" (us)
                 : "r0");
}

static inline void delay_ms(const uint32_t ms) {
    if (xTaskGetSchedulerState() == taskSCHEDULER_NOT_STARTED) {
        delay_us(ms*1000);
    } else {
        vTaskDelay(ms);
    }
}


#endif