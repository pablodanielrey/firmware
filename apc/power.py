from apcaccess import status as apc


if __name__ == '__main__':

    valores = apc.parse(apc.get(host="127.0.0.1"), strip_units=True)
    power = float(valores['NOMPOWER'])
    carga = float(valores['LOADPCT'])
    voltaje = float(valores['LINEV'])

    w = (power / 100) * carga
    amps = w / voltaje
    print(f'power: {power} carga: {carga} watts-consumidos: {w} amps: {amps}')
