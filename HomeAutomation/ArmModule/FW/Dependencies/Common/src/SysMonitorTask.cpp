#include <SysMonitorTask.h>


float STACK_LEVEL;
float PROCESS_STACK_LEVEL;


void checkStackLevel()  {
    uint32_t control = __get_CONTROL();
//    bool unPrivilegedMode = control & 0b001;     // probably always set
    bool processStackPointer = control & 0b010;  // else main stack
    
    uint32_t a;  // on the stack
    uint32_t *aPtr = &a;

    if (processStackPointer) {
        extern uint32_t *__stack_process_start__, *__stack_process_end__;
        uint32_t processStackSize = sizeof(uint32_t) * 
            (&__stack_process_end__ - &__stack_process_start__);
        unsigned int processStackUsed = sizeof(uint32_t) * (&__stack_process_end__ - &aPtr);
        PROCESS_STACK_LEVEL = 100.0 * (float)processStackUsed / (float)processStackSize;
    }
    extern uint32_t *__stack_start__, *__stack_end__;
    uint32_t stackSize = sizeof(uint32_t) * 
        (&__stack_end__ - &__stack_start__);
    unsigned int stackUsed = sizeof(uint32_t) * (&__stack_end__ - &aPtr);
    STACK_LEVEL = 100.0 * (float)stackUsed / (float)stackSize;
}


SysMonitorTask::SysMonitorTask(TaskClass** tasks, uint8_t taskCount) {
    this->tasks = tasks;
    this->taskCount = taskCount;
}

SysMonitorTask::~SysMonitorTask() {
}

void SysMonitorTask::init() {
    this->heartbeatSemaphores = new SemaphoreHandle_t[this->taskCount];
    for (uint8_t i=0; i<this->taskCount; i++) {
        SemaphoreHandle_t s = xSemaphoreCreateBinary();
        this->heartbeatSemaphores[i] = s;
        this->tasks[i]->setHeartbeatSemaphore(s);
    }
}

void SysMonitorTask::task() {
    
    uint8_t currentTask = 0;
    while(true) {
        if (xSemaphoreTake(this->heartbeatSemaphores[currentTask], 0) == pdPASS) {
            currentTask++;
        }
        if (currentTask >= this->taskCount) {
            patWatchDogs();
            currentTask = 0;
        }
        vTaskDelay(100 / portTICK_PERIOD_MS);
    }
}

 void SysMonitorTask::patWatchDogs() {
    IWDG_ReloadCounter();
    WWDG_SetCounter(0xFF);
}

