#include <MessageRecord.h>

MessageRecord::MessageRecord(char* text) {
    this->date = clock->getDate();
    this->time = clock->getTime();
    this->text = new char[strlen(text) +1];
    strcpy(this->text, text); 
}

MessageRecord::~MessageRecord() {
    if (this->text != NULL) {
        delete[] this->text;
        this->text = NULL;
    }
}
