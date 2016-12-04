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

#pragma GCC diagnostic ignored "-Wwrite-strings"

static void setUp(void) {
}

static void tearDown(void) {
}

static void test_Alloc() {
    uint8_t* mem = new uint8_t[16];
    delete[] mem;

    const int origHeapSize =  xPortGetFreeHeapSize();
    Zstring* s = new Zstring();
    s->appendS("Hello");
    s->append8(' ');
    Zstring* s2 = new Zstring("World!");
    s->appendZ(s2);
    delete s2;

    TEST_ASSERT_EQUAL_STRING("Hello World!", s->getStr());
    delete s;

    int finalHeapSize =  xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(origHeapSize, finalHeapSize);
}

static void test_LargeAlloc() {
    uint8_t* mem = new uint8_t[16];
    delete[] mem;

    const int origHeapSize =  xPortGetFreeHeapSize();
    Zstring* s = new Zstring();
    uint32_t count = 2000;
    char* str = "0123456789";
    for (uint32_t i=0; i<count; i++) {
        s->appendS(str);
    }

    TEST_ASSERT_EQUAL_INT(strlen(str) * count, s->size());
    int finalHeapSize =  xPortGetFreeHeapSize();
    delete s;

    finalHeapSize =  xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(origHeapSize, finalHeapSize);
}


TestRef Zstring_test(void) {
    EMB_UNIT_TESTFIXTURES(fixtures) {
        new_TestFixture((char*)"test_Alloc",test_Alloc),
        new_TestFixture((char*)"test_LargeAlloc",test_LargeAlloc)
    };

    EMB_UNIT_TESTCALLER(Zstring_test, "Zstring_test", setUp, tearDown, fixtures);
    return (TestRef)&Zstring_test;
}
