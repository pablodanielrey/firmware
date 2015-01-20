
""" setea el checksum en el paquete de datos """
def calcChksum(data):
    sum = 0
    for i in range(len(data) - 2):
        sum = (sum + data[i]) & 0xffff
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


toHex = lambda x:''.join([hex(ord(c))[2:].zfill(2) for c in x])

def printHexString(data):
    h = toHex(data)
    print(h)
