#include <MessageList.h>

MessageList::MessageList() {
    this->headPtr = NULL;
    this->endPtr = NULL;
    listLength = 0;;
    listMaxLength = 10;

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
    if (msg == NULL || strlen(msg) == 0) return;
    MessageRecord* mr = new MessageRecord(msg);
    pvPortSetTypeName(mr, "MsgRecrd");
    if (headPtr == NULL) {
        headPtr = mr;
        headPtr->next = NULL;
        endPtr = headPtr;
    } else {
        if (strcmp(endPtr->text, msg) == 0) {
            delete mr;
            return;
        }
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

MessageRecord* MessageList::getMostRecentMessage(uint32_t minutesOld) {
    if (endPtr == NULL) return NULL;
    uint32_t secondsAgo = clock->getSecondsAgo(&endPtr->date, &endPtr->time);
    if (secondsAgo < minutesOld * 60.0f) return endPtr;
    return NULL;
}
