
#ifndef TaskWrapperP_H
#define TaskWrapperP_H

#include "FreeRTOS.h"
#include "task.h"

class TaskBase {
public:
  xTaskHandle handle;

  ~TaskBase() {
#if INCLUDE_vTaskDelete
    vTaskDelete(handle);
#endif
    return;
  }
};

class Task : public TaskBase {
public:
    Task(const char *name, void (*taskfun)(void *), unsigned portBASE_TYPE priority,
       unsigned portSHORT stackDepth=configMINIMAL_STACK_SIZE) {
    
    xTaskCreate(taskfun, name, stackDepth, this, priority, &handle);
  }

};

class TaskClass : public TaskBase {
public:
    TaskClass() {}
    bool isAlive = false;
    void startTask(const char *name, unsigned portBASE_TYPE priority,
           unsigned portSHORT stackDepth=configMINIMAL_STACK_SIZE) {
        
        xTaskCreate(&taskfun, name, stackDepth, this, priority, &handle);
    }
    virtual void task() = 0;
    static void taskfun(void* param) {
        static_cast<TaskClass*>(param)->task();
#if INCLUDE_vTaskDelete
        xTaskDelete(handle);
#else
        while(1)
            vTaskDelay(10000);
#endif
   }
};

#endif