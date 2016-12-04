#ifndef _MAIN_H
#define _MAIN_H

#pragma GCC diagnostic ignored "-Wwrite-strings"
#pragma GCC diagnostic ignored "-Wunused-parameter"

#include <stm32f4xx.h>
#include <__cross_studio_io.h>
#include <FreeRTOS.h>
#include <semphr.h>
#include <task.h>
#include <queue.h>
#include <portable.h>
#include <TaskWrapper.h>
#include <stdio.h>
#include <stdarg.h>
#include <exception>
#include <new>
#include <arm_const_structs.h>
#include <stm32f4xx_rcc.h>
#include <stm32f4xx_conf.h>
#include <stm32f4xx_flash.h>
#include <stm32f4xx_iwdg.h>
#include <stm32f4xx_dbgmcu.h>
#include <WiFiReceiverTask.h>
#include <WiFITransmitterTask.h>
#include <XBeeTask.h>
#include <LEDs.h>
#include <delay.h>
#include <embUnit/embUnit.h>
#include <Relays.h>
#include <Clock.h>
#include <MainTask.h>
#include <WaterValve.h>
#include <LCD.h>
#include <HttpPacket.h>
#include <typeinfo>
#include <assert.h>
#include <MessageList.h>


BlockLink_t *osAllocatedHeapStart = NULL;
BlockLink_t *osAllocatedHeapPtr = NULL;

const uint16_t heapSTRUCT_SIZE	= ( ( sizeof ( BlockLink_t ) + ( portBYTE_ALIGNMENT - 1 ) ) & ~portBYTE_ALIGNMENT_MASK );


#define NEWLINE "\r\n"

TestRef Zstring_test();
TestRef Heap_test();
TestRef HttpPacket_test();
TestRef WiFiReceiverTask_test();

uint32_t sysclk;
uint32_t hclk;
uint32_t pclk1;
uint32_t pclk2;

FlagStatus watchDogResetFlag;

extern MainTask* mainTask;

bool isDebugPrintfEnabled() {
#ifdef TARGET_IS_SIMULATOR
    return true;
#endif
    return debug_enabled();
}


//extern const TestImplement TestCallerImplement;
extern "C" {
    void __cxa_pure_virtual() { 
        while (1); 
    }
    void abort(void) {
        while (1);
    }

    int __putchar(int ch) {
#ifdef STDOUT_OVER_DEBUGIO
        return debug_putchar(ch);
#endif
        return 1;
    }

    void HardFault_Handler() { 
          __asm volatile (
            " tst lr, #4                                                \n"
            " ite eq                                                    \n"
            " mrseq r0, msp                                             \n"
            " mrsne r0, psp                                             \n"
            " ldr r1, [r0, #24]                                         \n"
            " ldr r2, handler2_address_const                            \n"
            " bx r2                                                     \n"
            " handler2_address_const: .word HardFault_HandlerC    \n"
        );

    }

    void HardFault_HandlerC(unsigned long *hardfault_args) {
//        volatile unsigned int stacked_r0=0;
//        volatile unsigned int stacked_r1=0;
//        volatile unsigned int stacked_r2=0;
//        volatile unsigned int stacked_r3=0;
//        volatile unsigned int stacked_r12=0;
//        volatile unsigned int stacked_lr=0;
//        volatile unsigned int stacked_pc=0;
//        volatile unsigned int stacked_psr=0;
//        
//        (void)stacked_r0;
//        (void)stacked_r1;
//        (void)stacked_r2;
//        (void)stacked_r3;
//        (void)stacked_r12;
//        (void)stacked_lr;
//        (void)stacked_pc;
//        (void)stacked_psr;
//
//        stacked_r0 = ((unsigned long) hardfault_args[0]);
//        stacked_r1 = ((unsigned long) hardfault_args[1]);
//        stacked_r2 = ((unsigned long) hardfault_args[2]);
//        stacked_r3 = ((unsigned long) hardfault_args[3]);
//        stacked_r12 = ((unsigned long) hardfault_args[4]);
//        stacked_lr = ((unsigned long) hardfault_args[5]);
//        stacked_pc = ((unsigned long) hardfault_args[6]);
//        stacked_psr = ((unsigned long) hardfault_args[7]);

//        printf (NEWLINE NEWLINE "[Hard fault handler - all numbers in hex]" NEWLINE);
//        printf ("R0 = %x" NEWLINE, stacked_r0);
//        printf ("R1 = %x" NEWLINE, stacked_r1);
//        printf ("R2 = %x" NEWLINE, stacked_r2);
//        printf ("R3 = %x" NEWLINE, stacked_r3);
//        printf ("R12 = %x" NEWLINE, stacked_r12);
//        printf ("LR [R14] = %x  subroutine call return address" NEWLINE, stacked_lr);
//        printf ("PC [R15] = %x  program counter" NEWLINE, stacked_pc);
//        printf ("PSR = %x" NEWLINE, stacked_psr);
//        printf ("BFAR = %x" NEWLINE, (*((volatile unsigned long *)(0xE000ED38))));
//        printf ("CFSR = %x" NEWLINE, (*((volatile unsigned long *)(0xE000ED28))));
//        printf ("HFSR = %x" NEWLINE, (*((volatile unsigned long *)(0xE000ED2C))));
//        printf ("DFSR = %x" NEWLINE, (*((volatile unsigned long *)(0xE000ED30))));
//        printf ("AFSR = %x" NEWLINE, (*((volatile unsigned long *)(0xE000ED3C))));
//        printf ("SCB_SHCSR = %x" NEWLINE, SCB->SHCSR);

         while (1);
    }
}

extern "C" {

    void vApplicationMallocFailedHook( void ) {
        /* vApplicationMallocFailedHook() will only be called if
        configUSE_MALLOC_FAILED_HOOK is set to 1 in FreeRTOSConfig.h.  It is a hook
        function that will get called if a call to pvPortMalloc() fails.
        pvPortMalloc() is called internally by the kernel whenever a task, queue,
        timer or semaphore is created.  It is also called by various parts of the
        demo application.  If heap_1.c or heap_2.c are used, then the size of the
        heap available to pvPortMalloc() is defined by configTOTAL_HEAP_SIZE in
        FreeRTOSConfig.h, and the xPortGetFreeHeapSize() API function can be used
        to query the size of free heap space that remains (although it does not
        provide information on how the remaining heap might be fragmented). */
        taskDISABLE_INTERRUPTS();
        for( ;; );
    }

//    void vApplicationIdleHook( void ) {
        /* vApplicationIdleHook() will only be called if configUSE_IDLE_HOOK is set
        to 1 in FreeRTOSConfig.h.  It will be called on each iteration of the idle
        task.  It is essential that code added to this hook function never attempts
        to block in any way (for example, call xQueueReceive() with a block time
        specified, or call vTaskDelay()).  If the application makes use of the
        vTaskDelete() API function (as this demo application does) then it is also
        important that vApplicationIdleHook() is permitted to return to its calling
        function, because it is the responsibility of the idle task to clean up
        memory allocated by the kernel to any task that has since been deleted. */
//    }

    void vApplicationStackOverflowHook( TaskHandle_t pxTask, char *pcTaskName ) {
        ( void ) pcTaskName;
        ( void ) pxTask;

        /* Run time stack overflow checking is performed if
        configCHECK_FOR_STACK_OVERFLOW is defined to 1 or 2.  This hook
        function is called if a stack overflow is detected. */
        taskDISABLE_INTERRUPTS();
        for( ;; );
    }

    void vApplicationTickHook( void ) {
        /* This function will be called by each tick interrupt if
        configUSE_TICK_HOOK is set to 1 in FreeRTOSConfig.h.  User code can be
        added here, but the tick hook is called from an interrupt context, so
        code must not attempt to block, and only the interrupt safe FreeRTOS API
        functions can be used (those that end in FromISR()). */
    }

    xSemaphoreHandle heapMutex = NULL;
    xSemaphoreHandle printfMutex = NULL;
    xSemaphoreHandle guardMutex = NULL;
    xSemaphoreHandle debugMutex = NULL;

    void __heap_lock() {
        if (heapMutex == NULL) heapMutex = xSemaphoreCreateMutex();
        xSemaphoreTake(heapMutex, portMAX_DELAY);
    }

    void __heap_unlock() {
        xSemaphoreGive(heapMutex);
    }

    void __printf_lock() {
        if (printfMutex == NULL) printfMutex = xSemaphoreCreateMutex();
        xSemaphoreTake(printfMutex, portMAX_DELAY);
    }

    void __printf_unlock() {
        xSemaphoreGive(printfMutex);
    }

    void __debug_io_lock() {
        if (debugMutex == NULL) debugMutex = xSemaphoreCreateMutex();
        xSemaphoreTake(debugMutex, portMAX_DELAY);
    }

    void __debug_io_unlock() {
        xSemaphoreGive(debugMutex);
    }

    void *__aeabi_read_tp() {
        return NULL;
    }

    int __cxa_guard_acquire(int *guard_object) {
        if (guardMutex == NULL) guardMutex = xSemaphoreCreateMutex();
        xSemaphoreTake(guardMutex, portMAX_DELAY);
        return !(*guard_object & 0x1);
    }

    void __cxa_guard_release(int *guard_object) {
        *guard_object = 0x1;
        xSemaphoreGive(guardMutex);
    }

    void  __cxa_throw_bad_array_new_length() {
    }

    void vApplicationIdleHook() {
        leds->setBlueState(true);
        leds->setBlueState(false);
    }

    bool isHeapAddress(void* address);

#ifdef MODIFIED_HEAP_4
    void traceMallocImpl(void* address, size_t size) {
        assert(heapSTRUCT_SIZE >= 8 && heapSTRUCT_SIZE < 100);
        assert(isHeapAddress((uint8_t*)address - heapSTRUCT_SIZE));
        assert(size <= configTOTAL_HEAP_SIZE);

        BlockLink_t* bytePtr = (BlockLink_t*)address;
        BlockLink_t* allocatedBlock = (BlockLink_t*)((uint8_t*)bytePtr - heapSTRUCT_SIZE);
        allocatedBlock->typeName[0] = NULL;
        if (osAllocatedHeapStart == NULL) {
            osAllocatedHeapStart = allocatedBlock;
            osAllocatedHeapPtr = osAllocatedHeapStart;
            osAllocatedHeapPtr->pxPrevBlock = NULL;
            osAllocatedHeapPtr->pxNextBlock = NULL;
        } else {
            osAllocatedHeapPtr->pxNextBlock = allocatedBlock;
            allocatedBlock->pxPrevBlock = osAllocatedHeapPtr;
            osAllocatedHeapPtr = allocatedBlock;
            allocatedBlock->pxNextBlock = NULL;
        }
    }

    void traceFreeImpl(void* address, size_t size) {
        assert(heapSTRUCT_SIZE >= 8 && heapSTRUCT_SIZE < 100);
        assert(isHeapAddress((uint8_t*)address - heapSTRUCT_SIZE));
        assert(size <= configTOTAL_HEAP_SIZE);

        BlockLink_t* bytePtr = (BlockLink_t*)address;
        BlockLink_t* freedBlock = (BlockLink_t*)(((uint8_t*)bytePtr) - heapSTRUCT_SIZE);

        assert(isHeapAddress(freedBlock->pxPrevBlock) || freedBlock->pxPrevBlock == NULL);
        assert(isHeapAddress(freedBlock->pxNextBlock) || freedBlock->pxNextBlock == NULL);
        assert(isHeapAddress(osAllocatedHeapStart));
        assert(isHeapAddress(osAllocatedHeapPtr));

        if (freedBlock->pxPrevBlock == NULL) {
            osAllocatedHeapStart = freedBlock->pxNextBlock;
        } else {
            freedBlock->pxPrevBlock->pxNextBlock = freedBlock->pxNextBlock;
        }
        if (freedBlock->pxNextBlock == NULL) {
            osAllocatedHeapPtr = freedBlock->pxPrevBlock;
            freedBlock->pxPrevBlock->pxNextBlock = NULL;
        } else {
            freedBlock->pxNextBlock->pxPrevBlock = freedBlock->pxPrevBlock;
        }
        freedBlock->pxNextBlock = NULL;
        freedBlock->pxPrevBlock = NULL;
    }
#endif
}


void* operator new(size_t size) {
    __heap_lock();
    void* p = pvPortMalloc(size);

#ifdef __EXCEPTIONS
    if (p==0)
        throw std::bad_alloc(); // ANSI/ISO compliant behavior
#endif
    __heap_unlock();
    return p;
}

void operator delete(void *p) throw() {
    __heap_lock();
    vPortFree( p );
    __heap_unlock();
}

void* operator new[](unsigned int size) {
     __heap_lock();
    void* p = pvPortMalloc(size);
    __heap_unlock();
    return p;
}

void operator delete[](void *p) throw() {
    __heap_lock();
    vPortFree(p);
    __heap_unlock();
}

void operator delete(void* p, unsigned int) {
    __heap_lock();
    vPortFree(p);
    __heap_unlock();
}

void operator delete[](void* p, unsigned int) {
    __heap_lock();
    vPortFree(p);
    __heap_unlock();
}

#endif