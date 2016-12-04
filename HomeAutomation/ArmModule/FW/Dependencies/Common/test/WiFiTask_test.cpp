#include <stm32f4xx.h>
#include <embUnit/embUnit.h>
#include <WiFiReceiverTask.h>
#include <HttpPacket.h>
#include <Zstring.h>
#include <SysMonitorTask.h>

static void setUp(void) {
}

static void tearDown(void) {
}

void _testUtilCreateZstring() {
    Zstring f = Zstring("one");
    f.appendS("two");
}

void test_WiFiParseRequest() {

    WiFiReceiverTask w = WiFiReceiverTask(NULL, NULL);

    uint8_t rxPacketBuffer[] = { 0x7E, 0x00, 0x36, 
        0xB0, 0x00, 0x60, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 
        0x08, 0x47, 0x45, 0x54, 0x20, 0x2F, 0x66, 0x6F, 0x6F, 
        0x2F, 0x62, 0x61, 0x72, 0x20, 0x48, 0x54, 0x50, 0x50, 0x2F, 
        0x31, 0x2E, 0x31, 0x0D, 0x0A, 0x00, 0x48, 0x4F, 0x53, 0x54, 
        0x3A, 0x20, 0x31, 0x39, 0x32, 0x2E, 0x31, 0x36, 0x38, 0x2E, 
        0x31, 0x2E, 0x32, 0x0D, 0x0A, 0xEB, 0x00};
    uint16_t rxPacketBufferLength = 58;
        

    for (uint16_t i=0; i<rxPacketBufferLength; i++) {
        w.loadPacketByte(rxPacketBuffer[i]);
    }
    
    uint32_t httpRequestLength = rxPacketBufferLength -15;
    TEST_ASSERT_EQUAL_INT(w.workingBuffer[0], (rxPacketBuffer+14)[0]);
    TEST_ASSERT_EQUAL_INT(w.workingBuffer[httpRequestLength-1], (rxPacketBuffer+14)[httpRequestLength-1]);
    TEST_ASSERT(strncmp(w.workingBuffer, (const char *)rxPacketBuffer+14, httpRequestLength) == 0);



//
//    uint8_t txPacketBuffer[] = { 0x7E, 0x00, 0x36, 
//        0xB0, 0x00, 0x60, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 
//        0x08, 0x00, 0x47, 0x45, 0x54, 0x20, 0x2F, 0x66, 0x6F, 0x6F, 
//        0x2F, 0x62, 0x61, 0x72, 0x20, 0x48, 0x54, 0x50, 0x50, 0x2F, 
//        0x31, 0x2E, 0x31, 0x0D, 0x0A, 0x00, 0x48, 0x4F, 0x53, 0x54, 
//        0x3A, 0x20, 0x31, 0x39, 0x32, 0x2E, 0x31, 0x36, 0x38, 0x2E, 
//        0x31, 0x2E, 0x32, 0x0D, 0x0A, 0xEB, 0x00};
//    uint16_t txPacketBufferLength = 59;
}

void test_WiFiStatusResponse() {
//    WiFiTask w = WiFiTask(NULL);
//    HttpPacket packet = HttpPacket();
//    packet.setAddress(0xc0b00102);
//    packet.setDestPort(0x0102);
//    packet.setSourcePort(0x0304);
//    packet.setProtcol(0x01);
//    packet.setOptions(0x00);
//    
//    Zstring msg = Zstring("status message response\r\n");
//
//    w.addIpv4RxResponseHeaderToPayload(&packet, &msg);
//    Zstring* apiPacket = w.assembleIpV4TxPacket(&packet);
//    TEST_ASSERT(0x7e == apiPacket->getChar(0));
//    TEST_ASSERT(memcmp(apiPacket->getStr(), msg.getStr(), apiPacket->size()));
}

void test_String() {
    Zstring a("a string");
    TEST_ASSERT_EQUAL_STRING(a.getStr(), "a string");

    Zstring b = Zstring();
    b.appendS("aaa", 1);
    b.append8(NULL);
    b.appendS("b\0b",3);
    TEST_ASSERT(memcmp("a\0b\0b", b.getStr(), 6) == 0);
    TEST_ASSERT_EQUAL_INT(5, b.size());

    Zstring c = Zstring("one ");
    c.appendS("two ");
    c.appendS("three ");
    c.append8(0x41);
    c.append8(0x42);
    c.append8(0x43);
    c.append16(0x0102);
    c.append32(0x11121314);

    TEST_ASSERT(strncmp("one two three ABC", c.getStr(), strlen("one two three ABC")) == 0);
    TEST_ASSERT_EQUAL_INT(0x14, c.getChar(c.size()-1));
    TEST_ASSERT_EQUAL_INT(0x13, c.getChar(c.size()-2));
    TEST_ASSERT_EQUAL_INT(0x12, c.getChar(c.size()-3));
    TEST_ASSERT_EQUAL_INT(0x11, c.getChar(c.size()-4));
    TEST_ASSERT_EQUAL_INT(0x02, c.getChar(c.size()-5));
    TEST_ASSERT_EQUAL_INT(0x01, c.getChar(c.size()-6));

//    Zstring d = Zstring("blah");
//    Zstring e = Zstring(d);
//    TEST_ASSERT_EQUAL_STRING("blah", d.getStr());
//    TEST_ASSERT_EQUAL_STRING("blah", e.getStr());
}

TestRef WiFiTask_test(void) {
    EMB_UNIT_TESTFIXTURES(fixtures) {
        new_TestFixture("test_WiFiParserequest",test_WiFiParseRequest),
        new_TestFixture("test_WiFiStatusResponse",test_WiFiStatusResponse),
        new_TestFixture("test_String",test_String)
    };

    EMB_UNIT_TESTCALLER(WiFiTask_test, "WiFiTask_test", setUp, tearDown, fixtures);
    return (TestRef)&WiFiTask_test;
}
