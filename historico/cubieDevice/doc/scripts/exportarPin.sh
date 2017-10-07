#!/bin/bash
# este es el pin que se usa para apagar y prender el lector del cubie
echo "9" > /sys/class/gpio/export
echo "out" > /sys/class/gpio/gpio9_pg0/direction

