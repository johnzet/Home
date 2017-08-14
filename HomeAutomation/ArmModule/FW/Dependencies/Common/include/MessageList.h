#ifndef ERROR_LIST_H
#define ERROR_LIST_H

#include <MessageRecord.h>
#include <FreeRTOS.h>
#include <semphr.h>
#include <task.h>
#include <Clock.h>

extern Clock* clock;

class MessageList {
    private:
        MessageRecord* headPtr;
        MessageRecord* endPtr;
        uint32_t listLength;
        uint32_t listMaxLength;
        SemaphoreHandle_t messageMutex;

    public:
        MessageList();
        ~MessageList();
        void init();
        void addMessage(char* msg);

        MessageRecord* getFirst();
        MessageRecord* getLast();
        MessageRecord* getNext(MessageRecord* currentRecord);
        MessageRecord* popMessage();
};


#endif
