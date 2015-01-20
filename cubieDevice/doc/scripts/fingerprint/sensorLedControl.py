import array
import serial
import camabio

on = 0x01
data = [0x55,0xaa,0x24,0x01,0x02,0x0,on,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x00,0x00]
camabio.setChksum(data)

print('abriendo puerto seriel')
ser = serial.Serial("/dev/ttyS1",115200,timeout=5)

print('escribiendo bytes en el puerto serie')
camabio.printArray(data)
setBauds = array.array('B', data).tostring()
ser.write(setBauds);
ser.flush()

print('tratando de leer bytes desde el puerto serie: ')
data2 = ser.read(len(data))
if data2 == None:
    print('No se leyo ningun byte')
else:
    camabio.printHexString(data2)
