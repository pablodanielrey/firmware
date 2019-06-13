#This file is executed on every boot (including wake-boot from deepsleep)
#import esp
#esp.osdebug(None)
import connect
import main
import uos, machine
#uos.dupterm(None, 1) # disable REPL on UART(0)
import gc
import webrepl
webrepl.start()
gc.collect()


if __name__ == "__main__":
    connect.conectar_wifi()
    main.subscriber(server)