################################################################################
# Automatically-generated file. Do not edit!
################################################################################

SHELL := cmd.exe
RM := rm -rf

USER_OBJS :=

LIBS := 
PROJ := 

O_SRCS := 
C_SRCS := 
S_SRCS := 
S_UPPER_SRCS := 
OBJ_SRCS := 
ASM_SRCS := 
PREPROCESSING_SRCS := 
OBJS := 
OBJS_AS_ARGS := 
C_DEPS := 
C_DEPS_AS_ARGS := 
EXECUTABLES := 
OUTPUT_FILE_PATH :=
OUTPUT_FILE_PATH_AS_ARGS :=
AVR_APP_PATH :=$$$AVR_APP_PATH$$$
QUOTE := "
ADDITIONAL_DEPENDENCIES:=
OUTPUT_FILE_DEP:=
LIB_DEP:=

# Every subdirectory with source files must be described here
SUBDIRS :=  \
../Arduino_Core \
../common


# Add inputs and outputs from these tool invocations to the build variables 
C_SRCS +=  \
../Arduino_Core/EEPROM.cpp \
../Arduino_Core/twi.c \
../Arduino_Core/Wire.cpp \
../common/SensorUtils.cpp \
../common/SHT2x.cpp \
../common/XBee.cpp \
../Lcd.cpp \
../thermostat.cpp


PREPROCESSING_SRCS += 


ASM_SRCS += 


OBJS +=  \
Arduino_Core/EEPROM.o \
Arduino_Core/twi.o \
Arduino_Core/Wire.o \
common/SensorUtils.o \
common/SHT2x.o \
common/XBee.o \
Lcd.o \
thermostat.o

OBJS_AS_ARGS +=  \
Arduino_Core/EEPROM.o \
Arduino_Core/twi.o \
Arduino_Core/Wire.o \
common/SensorUtils.o \
common/SHT2x.o \
common/XBee.o \
Lcd.o \
thermostat.o

C_DEPS +=  \
Arduino_Core/EEPROM.d \
Arduino_Core/twi.d \
Arduino_Core/Wire.d \
common/SensorUtils.d \
common/SHT2x.d \
common/XBee.d \
Lcd.d \
thermostat.d

C_DEPS_AS_ARGS +=  \
Arduino_Core/EEPROM.d \
Arduino_Core/twi.d \
Arduino_Core/Wire.d \
common/SensorUtils.d \
common/SHT2x.d \
common/XBee.d \
Lcd.d \
thermostat.d

OUTPUT_FILE_PATH +=thermostat.elf

OUTPUT_FILE_PATH_AS_ARGS +=thermostat.elf

ADDITIONAL_DEPENDENCIES:=

OUTPUT_FILE_DEP:= ./makedep.mk

LIB_DEP+=  \
C:/projects/house/Sensor/Arduino_Core/libAtMega328_core.a

# AVR32/GNU C Compiler

















Arduino_Core/%.o: ../Arduino_Core/%.c
	@echo Building file: $<
	@echo Invoking: AVR8/GNU C Compiler : 3.4.4
	$(QUOTE)C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-gcc.exe$(QUOTE)  -funsigned-char -funsigned-bitfields -nostdinc -DF_CPU=8000000UL  -I"../../Arduino_Core/libraries/Wire/utility" -I"C:\projects\house\Sensor\Arduino_Core\avr\include" -I"C:\arduino-1.0\hardware\tools\avr\lib\gcc\avr\4.3.2\include" -I"../../Arduino_Core"  -Os -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -mmcu=atmega328p -c -std=gnu99 -MD -MP -MF "$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)"   -o "$@" "$<"
	@echo Finished building: $<
	

Arduino_Core/%.o: ../Arduino_Core/%.cpp
	@echo Building file: $<
	@echo Invoking: AVR8/GNU C Compiler : 3.4.4
	$(QUOTE)C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-g++.exe$(QUOTE) -funsigned-char -funsigned-bitfields -nostdinc -DF_CPU=8000000UL  -I"C:\arduino-1.0\hardware\arduino\cores\arduino" -I"C:\arduino-1.0\hardware\arduino\variants\standard" -I"C:\arduino-1.0\hardware\tools\avr\avr\include" -I"C:\arduino-1.0\hardware\tools\avr\lib\gcc\avr\4.3.2\include" -I"C:\arduino-1.0\libraries\Wire" -I"C:\arduino-1.0\libraries\Wire\utility" -I"../Arduino_Core" -I"../../common" -I"../../Arduino_Core/libraries/EEPROM"  -Os -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -mmcu=atmega328p -c -MD -MP -MF "$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)"   -o "$@" "$<"
	@echo Finished building: $<
	

common/%.o: ../common/%.cpp
	@echo Building file: $<
	@echo Invoking: AVR8/GNU C Compiler : 3.4.4
	$(QUOTE)C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-g++.exe$(QUOTE) -funsigned-char -funsigned-bitfields -nostdinc -DF_CPU=8000000UL  -I"C:\arduino-1.0\hardware\arduino\cores\arduino" -I"C:\arduino-1.0\hardware\arduino\variants\standard" -I"C:\arduino-1.0\hardware\tools\avr\avr\include" -I"C:\arduino-1.0\hardware\tools\avr\lib\gcc\avr\4.3.2\include" -I"C:\arduino-1.0\libraries\Wire" -I"C:\arduino-1.0\libraries\Wire\utility" -I"../Arduino_Core" -I"../../common" -I"../../Arduino_Core/libraries/EEPROM"  -Os -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -mmcu=atmega328p -c -MD -MP -MF "$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)"   -o "$@" "$<"
	@echo Finished building: $<
	

./%.o: .././%.cpp
	@echo Building file: $<
	@echo Invoking: AVR8/GNU C Compiler : 3.4.4
	$(QUOTE)C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-g++.exe$(QUOTE) -funsigned-char -funsigned-bitfields -nostdinc -DF_CPU=8000000UL  -I"C:\arduino-1.0\hardware\arduino\cores\arduino" -I"C:\arduino-1.0\hardware\arduino\variants\standard" -I"C:\arduino-1.0\hardware\tools\avr\avr\include" -I"C:\arduino-1.0\hardware\tools\avr\lib\gcc\avr\4.3.2\include" -I"C:\arduino-1.0\libraries\Wire" -I"C:\arduino-1.0\libraries\Wire\utility" -I"../Arduino_Core" -I"../../common" -I"../../Arduino_Core/libraries/EEPROM"  -Os -ffunction-sections -fdata-sections -fpack-struct -fshort-enums -Wall -mmcu=atmega328p -c -MD -MP -MF "$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -MT"$(@:%.o=%.o)"   -o "$@" "$<"
	@echo Finished building: $<
	



# AVR32/GNU Preprocessing Assembler



# AVR32/GNU Assembler




ifneq ($(MAKECMDGOALS),clean)
ifneq ($(strip $(C_DEPS)),)
-include $(C_DEPS)
endif
endif

# Add inputs and outputs from these tool invocations to the build variables 

# All Target
all: $(OUTPUT_FILE_PATH) $(ADDITIONAL_DEPENDENCIES)

$(OUTPUT_FILE_PATH): $(OBJS) $(USER_OBJS) $(OUTPUT_FILE_DEP) $(LIB_DEP)
	@echo Building target: $@
	@echo Invoking: AVR8/GNU Linker : 3.4.4
	$(QUOTE)C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-g++.exe$(QUOTE) -o$(OUTPUT_FILE_PATH_AS_ARGS) $(OBJS_AS_ARGS) $(USER_OBJS) $(LIBS) -Wl,-s -Wl,-Map="thermostat.map" -Wl,--start-group -Wl,-lAtMega328_core -Wl,-lm  -Wl,--end-group -Wl,-L"C:\projects\house\Sensor\Arduino_Core"  -Wl,--gc-sections -mmcu=atmega328p 
	@echo Finished building target: $@
	"C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-objcopy.exe" -O ihex -R .eeprom -R .fuse -R .lock -R .signature  "thermostat.elf" "thermostat.hex"
	"C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-objcopy.exe" -j .eeprom  --set-section-flags=.eeprom=alloc,load --change-section-lma .eeprom=0  --no-change-warnings -O ihex "thermostat.elf" "thermostat.eep" || exit 0
	"C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-objdump.exe" -h -S "thermostat.elf" > "thermostat.lss"
	"C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-objcopy.exe" -O srec -R .eeprom -R .fuse -R .lock -R .signature  "thermostat.elf" "thermostat.srec"
	"C:\Program Files (x86)\Atmel\Atmel Toolchain\AVR8 GCC\Native\3.4.1056\avr8-gnu-toolchain\bin\avr-size.exe" "thermostat.elf"
	
	





# Other Targets
clean:
	-$(RM) $(OBJS_AS_ARGS) $(EXECUTABLES)  
	-$(RM) $(C_DEPS_AS_ARGS)   
	rm -rf "thermostat.elf" "thermostat.a" "thermostat.hex" "thermostat.lss" "thermostat.eep" "thermostat.map" "thermostat.srec"
	