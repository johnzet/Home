# 1 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.c"
# 1 "<command-line>"
# 1 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.c"
# 35 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.c"
# 1 "C:/projects/flight/firmware/../../embunit/embUnit/Test.h" 1
# 38 "C:/projects/flight/firmware/../../embunit/embUnit/Test.h"
typedef struct __TestResult TestResult;
typedef struct __TestResult* TestResultRef;

typedef struct __TestImplement TestImplement;
typedef struct __TestImplement* TestImplementRef;

typedef char*(*TestNameFunction)(void*);
typedef void(*TestRunFunction)(void*,TestResult*);
typedef int(*TestCountTestCasesFunction)(void*);

struct __TestImplement {
 TestNameFunction name;
 TestRunFunction run;
 TestCountTestCasesFunction countTestCases;
};

typedef struct __Test Test;
typedef struct __Test* TestRef;

struct __Test {
 TestImplement* isa;
};
# 36 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.c" 2
# 1 "C:/projects/flight/firmware/../../embunit/embUnit/TestCase.h" 1
# 38 "C:/projects/flight/firmware/../../embunit/embUnit/TestCase.h"
typedef struct __TestCase TestCase;
typedef struct __TestCase* TestCaseRef;

struct __TestCase {
 TestImplement* isa;
 char *name;
 void(*setUp)(void);
 void(*tearDown)(void);
 void(*runTest)(void);
};

extern const TestImplement TestCaseImplement;
# 37 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.c" 2
# 1 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.h" 1
# 38 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.h"
typedef struct __TestFixture TestFixture;
typedef struct __TestFixture* TestFixtureRef;

struct __TestFixture {
 char *name;
 void(*test)(void);
};







typedef struct __TestCaller TestCaller;
typedef struct __TestCaller* TestCallerRef;

struct __TestCaller {
 TestImplement* isa;
 char *name;
 void(*setUp)(void);
 void(*tearDown)(void);
 int numberOfFixtuers;
 TestFixture *fixtuers;
};

extern const TestImplement TestCallerImplement;
# 38 "C:/projects/flight/firmware/../../embunit/embUnit/TestCaller.c" 2

char* TestCaller_name(TestCaller* self)
{
 return self->name;
}

int TestCaller_run(TestCaller* self,TestResult* result)
{
 TestCase cs = { (TestImplement*)&TestCaseImplement, 0, 0, 0, 0, };
 int i;
 cs.setUp = self->setUp;
 cs.tearDown = self->tearDown;
 for (i=0; i<self->numberOfFixtuers; i++) {
  cs.name = self->fixtuers[i].name;
  cs.runTest = self->fixtuers[i].test;

        cs.isa->run(self,result);
 }
    return 0;
}

int TestCaller_countTestCases(TestCaller* self)
{
 return self->numberOfFixtuers;
}

const TestImplement TestCallerImplement = {
 (TestNameFunction) TestCaller_name,
 (TestRunFunction) TestCaller_run,
 (TestCountTestCasesFunction)TestCaller_countTestCases,
};
