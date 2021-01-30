#include <embUnit/embUnit.h>
#include <WiFiTask.h>


static void setUp(void) {
}

static void tearDown(void) {
}

void test_WiFiParseRequest() {

    WiFiTask* w = new WiFiTask();
    char packetBuffer[] = { 0x7E, 0x00, 0x35, 
        0xB0, 0x10, 0x60, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 
        0x00, 0x47, 0x45, 0x54, 0x20, 0x2F, 0x66, 0x6F, 0x6F, 0x2F, 
        0x62, 0x61, 0x72, 0x20, 0x48, 0x54, 0x50, 0x50, 0x2F, 0x31, 
        0x2E, 0x31, 0x0D, 0x0A, 0x48, 0x4F, 0x53, 0x54, 0x3A, 0x20, 
        0x31, 0x39, 0x32, 0x2E, 0x31, 0x36, 0x38, 0x2E, 0x31, 0x2E, 
        0x32, 0x0D, 0x0A, 0xFD, 0x00};
    uint16_t packetBufferLength = 57;
        
    for (uint16_t i=0; i<=packetBufferLength; i++) {
        w->loadPacketByte(packetBuffer[i]);
    }
    
    packetBuffer[packetBufferLength-1] = NULL;
    TEST_ASSERT_EQUAL_STRING(packetBuffer+14, w->httpRequestBuffer);

    delete w;
}



TestRef WiFiTask_test(void) {
    EMB_UNIT_TESTFIXTURES(fixtures) {
        new_TestFixture("test_WiFiParserequest",test_WiFiParseRequest),
    };

    EMB_UNIT_TESTCALLER(WiFiTask_test, "WiFiTask_test", setUp, tearDown, fixtures);
    return (TestRef)&WiFiTask_test;
}
