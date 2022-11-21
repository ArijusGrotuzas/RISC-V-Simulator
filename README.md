# RISC-V-Simulator
Simulator for the RISC-V ISA

## Environment Calls
Following environment calls are supported by the simulator based on [Ripes environment calls](https://github.com/mortbopet/Ripes/blob/master/docs/ecalls.md):

|  `a7`  |          `a0`         |     *Name*     |
|:--:|:-------------------:|:------------:|
| 1  | (integer to print)  | print_int    |
| 2  | (float to print) | print_float |
| 4  | (pointer to string) | print_string |
| 10 | -                   | exit         |
| 11 | (char to print) | print_char |
| 34 | (integer to print) | print_hex |
| 35 | (integer to print) | print_bin |
| 36 | (integer to print) | print_unsigned |

## Executing JAR

```shell
java -jar RISC-V-Simulator.jar <path to binary file>
```
