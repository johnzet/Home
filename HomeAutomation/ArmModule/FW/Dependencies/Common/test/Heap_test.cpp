#include <stm32f4xx.h>
#include <__cross_studio_io.h>
#include <FreeRTOS.h>
#include <stdio.h>
#include <exception>
#include <new>
#include <arm_const_structs.h>
#include <stm32f4xx.h>
#include "embUnit/embUnit.h"
#include <heap_4.h>

extern "C" void testResetHeapState();

static void setUp(void) {
}

static void tearDown(void) {
//    testResetHeapState();
}

static void test_sizeofBlockLink() {
    TEST_ASSERT_EQUAL_INT(8, portBYTE_ALIGNMENT);
    TEST_ASSERT_EQUAL_INT(0,  sizeof(BlockLink_t) % portBYTE_ALIGNMENT);
}

static void test_InitialCondition() {
    int left = ( configTOTAL_HEAP_SIZE - portBYTE_ALIGNMENT ) & ~portBYTE_ALIGNMENT_MASK;
    int right =  xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(left, right);
}

static void test_Allocate() {
    uint8_t* m0 = new uint8_t[16];
    delete[] m0;
    int left = xPortGetFreeHeapSize();
    uint8_t* mem = new uint8_t[1024];
    left = left - 1024 - sizeof(BlockLink_t);
    int right = xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(left, right);
    delete[] mem;
}

static void test_AllocateAndFree() {
    uint8_t* m0 = new uint8_t[16];
    delete[] m0;
    int initial = xPortGetFreeHeapSize();
    uint8_t* m1 = new uint8_t[16];
    delete[] m1;
    uint8_t* m2 = new uint8_t[16];
    uint8_t* m3 = new uint8_t[16];
    delete[] m2;
    uint8_t* m4 = new uint8_t[16];
    uint8_t* m5 = new uint8_t[16];
    delete[] m3;
    delete[] m4;
    delete[] m5;
    int final = xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(final, initial);
}

static void test_Unaligned() {
    uint8_t* m0 = new uint8_t[16];
    delete[] m0;
    int left = xPortGetFreeHeapSize();

    uint8_t* mem = new uint8_t[1];
    left = left - 8 - sizeof(BlockLink_t);
    int right = xPortGetFreeHeapSize();
    TEST_ASSERT_EQUAL_INT(left, right);
    delete[] mem;
}

TestRef Heap_test(void) {
    EMB_UNIT_TESTFIXTURES(fixtures) {
        new_TestFixture((char*)"test_sizeofBlockLink",test_sizeofBlockLink),
        new_TestFixture((char*)"test_InitialCondition",test_InitialCondition),
        new_TestFixture((char*)"test_Allocate",test_Allocate),
        new_TestFixture((char*)"test_AllocateAndFree",test_AllocateAndFree),
        new_TestFixture((char*)"test_Unaligned",test_Unaligned)
    };

    EMB_UNIT_TESTCALLER(Heap_test, "Heap_test", setUp, tearDown, fixtures);
    return (TestRef)&Heap_test;
}
