import network
sta = network.WLAN(network.STA_IF)
sta.active(True)
sta.connect("sistemas", "ditesisitedi")