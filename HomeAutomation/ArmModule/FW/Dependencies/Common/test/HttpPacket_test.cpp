#include <stm32f4xx.h>
#include <__cross_studio_io.h>
#include <FreeRTOS.h>
#include <stdio.h>
#include <exception>
#include <new>
#include <arm_const_structs.h>
#include <stm32f4xx.h>
#include <embUnit/embUnit.h>
#include <heap_4.h>
#include <Zstring.h>
#include <HttpPacket.h>

#pragma GCC diagnostic ignored "-Wwrite-strings"

static void setUp(void) {
}

static void tearDown(void) {
}

static void test_Alloc() {
    uint8_t* mem = new uint8_t[16];
    delete[] mem;

    const int origHeapSize =  xPortGetFreeHeapSize();
    HttpPacket* p = new HttpPacket();
    Zstring* z1 = new Zstring("Hello World");
    Zstring* z2 = new Zstring("again");
    p->setPayload(z1);
    TEST_ASSERT_EQUAL_STRING("Hello World", p->getPayload()->getStr());
    p->setPayload(z2);

    TEST_ASSERT_EQUAL_STRING("again", p->getPayload()->getStr());
    delete z1;
    delete p;
    delete z2;

    int finalHeapSize =  xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(origHeapSize, finalHeapSize);
}



TestRef HttpPacket_test(void) {
    EMB_UNIT_TESTFIXTURES(fixtures) {
        new_TestFixture((char*)"test_Alloc",test_Alloc)
    };

    EMB_UNIT_TESTCALLER(HttpPacket_test, "HttpPacket_test", setUp, tearDown, fixtures);
    return (TestRef)&HttpPacket_test;
}
