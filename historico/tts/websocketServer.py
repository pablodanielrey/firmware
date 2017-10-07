
from Ws.SimpleWebSocketServer import WebSocket, SimpleWebSocketServer
import json
import time
import datetime
import traceback
from utils import DateTimeEncoder



class NullData(Exception):

    def __init__(self):
        pass

    def __str__(self):
        return self.__class__.__name__


class NotImplemented(Exception):

    def __init__(self):
        pass

    def __str__(self):
        return self.__class__.__name__



class WebsocketServer(WebSocket):

  def setQueue(self,queue):
    self.queue = queue

  def sendException(self,e):
      msg = {'type':'Exception','name':e.__class__.__name__}
      self.sendMessage(msg)

  def sendError(self,msg,e):
      mmsg = {'id':msg['id'],'error':e.__class__.__name__}
      self.sendMessage(mmsg)

  def handleMessage(self):
    try:
      if self.data is None:
        raise NullData()

      print 'C:' + self.data
      message = json.loads(str(self.data))

      if 'say' not in message:
          raise NotImplemented()

      msg = message['say']
      print 'pronouncing : %s' % msg
      self.queue.put(msg)

      self.sendMessage({'ok':''})

    except Exception as e:
      print e.__class__.__name__ + ' ' + str(e)
      traceback.print_exc()
      self.sendError(message,e)
      raise e

      if not managed:
        raise NotImplemented()

    except Exception as e:
      print e.__class__.__name__ + ' ' + str(e)
      traceback.print_exc()
      self.sendException(e)


  def sendMessage(self,msg):
      print 'R:' + str(msg)
      jmsg = json.dumps(msg,cls=DateTimeEncoder)
      print 'RJ' + jmsg
      super(WebsocketServer,self).sendMessage(jmsg)


  def handleConnected(self):
    print("connected : ",self.address)

  def handleClose(self):
    print("closed : ",self.address)
