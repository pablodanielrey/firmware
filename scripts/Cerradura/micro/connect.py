
import network
sta = network.WLAN(network.STA_IF)


def conectar_wifi():
    sta.active(False)
    sta.active(True)
    sta.connect("sistemas", "ditesisitedi")

def desconectar():
    sta.active(False)