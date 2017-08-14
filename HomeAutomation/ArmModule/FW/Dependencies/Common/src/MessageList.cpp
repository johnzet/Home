#include <MessageList.h>

MessageList::MessageList() {
    this->headPtr = NULL;
    this->endPtr = NULL;
    listLength = 0;
    listMaxLength = 10;
    this->messageMutex = xSemaphoreCreateMutex();
}

MessageList::~MessageList() {
//    while (headPtr != NULL) {
//        headPtr = headPtr->next;
//        delete headPtr;
//    }
    headPtr = NULL;
    endPtr = NULL;
}

void MessageList::addMessage(char* msg) {
    if (xSemaphoreTake(messageMutex, portMAX_DELAY) == pdTRUE) {
        if (msg == NULL || strlen(msg) == 0) {
            xSemaphoreGive(messageMutex);
            return;
        }
        MessageRecord* mr = new MessageRecord(msg);
        pvPortSetTypeName(mr, "MsgRecrd");
        if (headPtr == NULL) {
            headPtr = mr;
            headPtr->next = NULL;
            endPtr = headPtr;
        } else {
            if (strcmp(endPtr->text, msg) == 0) {
                delete mr;
                xSemaphoreGive(messageMutex);
                return;
            }
            endPtr->next = mr;
            endPtr = mr;
            endPtr->next = NULL;
            if (headPtr->next == NULL) {
                headPtr->next = endPtr;
            }
        }
        listLength++;

        if (listLength > listMaxLength) {
            MessageRecord* first = headPtr;
            headPtr = headPtr->next;
            delete first;
            listLength--;
        }
        xSemaphoreGive(messageMutex);
    }
}

MessageRecord*  MessageList::popMessage() {
    MessageRecord* message = NULL;
    if (xSemaphoreTake(messageMutex, portMAX_DELAY) == pdTRUE) {
        if (listLength >= 1) {
            message = headPtr;
            headPtr = message->next;
            listLength--;
        }
        xSemaphoreGive(messageMutex);
    }
    return message;
}

MessageRecord* MessageList::getFirst() {
    return headPtr;
}

MessageRecord* MessageList::getLast() {
    return endPtr;
}

MessageRecord* MessageList::getNext(MessageRecord* currentRecord) {
    if (currentRecord == NULL) return NULL;
    return currentRecord->next;
}

