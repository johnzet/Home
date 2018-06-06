#include <MainTask.h>


MainTask::MainTask() {
}

MainTask::~MainTask() {
}


void MainTask::init() {            
}


extern uint8_t*heap_4_heapStart;

bool MainTask::isValidHeapAddress(uint8_t* address) {
    if (address < heap_4_heapStart) return false;
    return (address < (heap_4_heapStart + configTOTAL_HEAP_SIZE));
}

//uint32_t MainTask::getRemainingHeapSize() {
//    taskENTER_CRITICAL();
//
//    uint8_t** pages = NULL;
//    uint8_t* page;
//    uint32_t heapSize = 0;
//    while(true) {
//        uint8_t** newPages = (pages == NULL? (uint8_t**)malloc(sizeof(uint32_t)) : (uint8_t**)realloc(pages, sizeof(pages) + sizeof(uint32_t)));
//        if (newPages == NULL) break;
//        page = (uint8_t*)malloc(256);
//        if (page == NULL) break;
//        newPages[sizeof(pages)] = page;
//        pages = newPages;
//    } 
//    heapSize = sizeof(pages) * 256;
//    for (uint32_t i=0; i<sizeof(pages); i++) {
//        free(pages[i]);
//    }
//    free(pages);
//
//    taskEXIT_CRITICAL();
//    return heapSize;
//}





