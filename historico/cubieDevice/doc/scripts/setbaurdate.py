import array
import serial

ser = serial.Serial("/dev/ttyS1",115200,timeout=0)
setbaud = array.array('B', [0x55,0xaa,0x14,0x01,0x02,0x0,0x01,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x17,0x01]).tostring()
ser.write(setbaud);
response = ser.readlines(None);
print response