#include <Zstring.h>


Zstring::Zstring() {
    this->str = NULL;
    this->length = 0;
    this->buffer[0] = NULL;
}

Zstring::Zstring(char* b, uint32_t l) {
    this->str = NULL;
    this->length = 0;
    this->buffer[0] = NULL;
    this->appendS(b, l);
}

Zstring::Zstring(char* b) {
    this->str = NULL;
    this->length = 0;
    this->buffer[0] = NULL;
    this->appendS(b, (b==NULL? 0 : strlen(b)));
}


Zstring::~Zstring() {
    if (this->str != NULL) {
        delete[] this->str;
        this->str = NULL;
    }
}

void Zstring::appendS(char* b, uint32_t l) {
    if (b == NULL) return;
//    assert(isHeapAddress(b));
    assert(l < 2000);
    if (this->str == NULL) {
        this->str = new char[l+1];
        assert(this->str != NULL);
        memcpy(this->str, b, l);
        this->str[l] = NULL;
        this->length = l;
    } else {
        uint32_t totalLength = this->length + l;
        char* oldMemory = this->str;
        uint32_t oldMemoryLength = this->length;
        this->str = new char[totalLength+1];
        assert(this->str != NULL);
        memcpy(this->str, oldMemory, oldMemoryLength);
        memcpy((this->str+oldMemoryLength), b, l);
        delete[] oldMemory;
        this->str[totalLength] = NULL;
        this->length = totalLength;
    }
    pvPortSetTypeName(this->str, "ZstrPyld");
}

void Zstring::appendS(char* b) {
    if (b == NULL) return;
    this->appendS(b, strlen(b));
}

void Zstring::append8(uint8_t c) {
    this->appendS((char *)&c, 1);
}

void Zstring::append16(uint16_t bigEndianValue) {
    this->append8((bigEndianValue & 0xff00)>> 8);
    this->append8(bigEndianValue & 0xff);
} 

void Zstring::append32(uint32_t bigEndianValue) {
    this->append16((bigEndianValue & 0xffff0000)>> 16);
    this->append16(bigEndianValue & 0xffff);
} 

void Zstring::appendZ(Zstring* zstring) {
    this->appendS(zstring->getStr(), zstring->size());
}

void Zstring::appendI(int intValue, int base) {
    itoa(intValue, this->buffer, base);
    this->appendS(this->buffer);
}

void Zstring::appendI(int intValue) {
    this->appendI(intValue, 10);
}

char* Zstring::getStr() {
    return this->str;
}

uint32_t Zstring::size() {
   return this->length;
}

char Zstring::getChar(uint32_t index) {
    return this->str[index];
}

void Zstring::clear() {
    delete[] this->str;
    this->str = NULL;
}