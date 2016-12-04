#ifndef SYS_MONITOR_TASK_H
#define SYS_MONITOR_TASK_H

#include <stm32f4xx.h>
#include <arm_const_structs.h>
#include <stm32f4xx_iwdg.h>
#include <TaskWrapper.h>
#include <semphr.h>
#include <task.h>

extern float STACK_LEVEL;
extern float PROCESS_STACK_LEVEL;

void checkStackLevel();

class SysMonitorTask : public TaskClass {
  public:
    explicit SysMonitorTask(TaskClass** tasks, uint8_t taskCount);
    ~SysMonitorTask();
    void init();
    void task();

  private:
    TaskClass** tasks;
    uint8_t taskCount;
    SemaphoreHandle_t* heartbeatSemaphores;
};



#endif