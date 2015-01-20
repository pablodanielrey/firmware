import array
import serial


""" setea el checksum en el paquete de datos """
def calcChksum(data):
    sum = 0
    for i in range(len(data) - 2):
        sum = sum + data[i]
    return sum

""" setea el checksum en el paquete de datos """
def setChksum(data):
    sum = calcChksum(data)
    data[len(data) - 2] = (sum & 0xff)
    data[len(data) - 1] = ((sum & 0xff00) >> 8)


""" chequea que el chksum del paquete este ok """
def verifyChksum(data):
    sum = calcChksum(data)
    l = len(data)
    sump = ((data[l - 1] << 8) + data[l - 2])
    print('suma calculada %s' % sum)
    print('suma del paquete %s' % sump)
    return (sum == sump)

def printArray(data):
    string = ''.join(format(x,'02x') for x in data)
    print(string)



"""
	 * 1 - 9600
	 * 2 - 19200
	 * 3 - 38400
	 * 4 - 57600
	 * 5 - 115200 (por defecto)
"""
bauds = 0x01
data = [0x55,0xaa,0x14,0x01,0x02,0x0,bauds,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x00,0x00]
setChksum(data)

print('abriendo puerto seriel')
ser = serial.Serial("/dev/ttyS1",115200,timeout=5)

print('escribiendo bytes en el puerto serie')
printArray(data)
setBauds = array.array('B', data).tostring()
ser.write(setBauds);
ser.flush()

print('tratando de leer bytes desde el puerto serie: ')
data2 = ser.read(len(data))
if data2 == None:
    print('No se leyo ningun byte')
else:
    printArray(data2)
