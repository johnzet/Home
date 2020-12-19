EESchema Schematic File Version 4
EELAYER 30 0
EELAYER END
$Descr A4 11693 8268
encoding utf-8
Sheet 1 1
Title ""
Date ""
Rev ""
Comp ""
Comment1 ""
Comment2 ""
Comment3 ""
Comment4 ""
$EndDescr
$Comp
L JohnZPCB:Particle_Argon REF?
U 1 1 5FDE89DE
P 6650 3500
F 0 "REF?" H 6650 4865 50  0000 C CNN
F 1 "Particle_Argon" H 6650 4774 50  0000 C CNN
F 2 "JohnZPCB:ParticleArgon" H 6700 3000 50  0001 C CNN
F 3 "https://docs.particle.io/datasheets/wi-fi/argon-datasheet/" H 6650 4750 50  0001 C CNN
	1    6650 3500
	1    0    0    -1  
$EndComp
Wire Wire Line
	6200 2700 5850 2900
$Comp
L power:GND #PWR0101
U 1 1 5FDEA0C9
P 5850 2900
F 0 "#PWR0101" H 5850 2650 50  0001 C CNN
F 1 "GND" V 5855 2772 50  0000 R CNN
F 2 "" H 5850 2900 50  0001 C CNN
F 3 "" H 5850 2900 50  0001 C CNN
	1    5850 2900
	0    1    1    0   
$EndComp
$Comp
L SparkFun-Connectors:CONN_04 J?
U 1 1 5FDF72DD
P 8100 4250
F 0 "J?" H 7872 4495 45  0000 R CNN
F 1 "CONN_04" H 7872 4411 45  0000 R CNN
F 2 "1X04" H 8100 4750 20  0001 C CNN
F 3 "" H 8100 4250 50  0001 C CNN
F 4 "CONN-09696" H 7872 4316 60  0000 R CNN "Field4"
	1    8100 4250
	-1   0    0    -1  
$EndComp
$Comp
L power:GND #PWR?
U 1 1 5FDF82F0
P 7950 4350
F 0 "#PWR?" H 7950 4100 50  0001 C CNN
F 1 "GND" H 7955 4177 50  0000 C CNN
F 2 "" H 7950 4350 50  0001 C CNN
F 3 "" H 7950 4350 50  0001 C CNN
	1    7950 4350
	1    0    0    -1  
$EndComp
Wire Wire Line
	8000 4250 7950 4250
Wire Wire Line
	7950 4250 7950 4350
Wire Wire Line
	8000 4150 7350 4150
Wire Wire Line
	7350 4150 7350 3900
Wire Wire Line
	7350 3900 7100 3900
Wire Wire Line
	7100 3800 7450 3800
Wire Wire Line
	7450 3800 7450 4050
Wire Wire Line
	7450 4050 8000 4050
Text GLabel 7900 3950 0    50   Input ~ 0
3V3
Wire Wire Line
	7900 3950 8000 3950
Text GLabel 6000 2500 0    50   Input ~ 0
3V3
Wire Wire Line
	6200 2500 6000 2500
$EndSCHEMATC
