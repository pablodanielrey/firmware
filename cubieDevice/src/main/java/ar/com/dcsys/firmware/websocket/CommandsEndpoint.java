package ar.com.dcsys.firmware.websocket;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.xml.bind.DatatypeConverter;

import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.App;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.cmd.GetFirmwareVersion;
import ar.com.dcsys.firmware.cmd.GetFirmwareVersion.GetFirmwareVersionResult;
import ar.com.dcsys.firmware.cmd.Identify;
import ar.com.dcsys.firmware.cmd.Identify.IdentifyResult;
import ar.com.dcsys.firmware.cmd.SensorLedControl;
import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.cmd.TestConnection.TestConnectionResult;
import ar.com.dcsys.firmware.cmd.enroll.DefaultEnrollData;
import ar.com.dcsys.firmware.cmd.enroll.EnrollAndStoreInRam;
import ar.com.dcsys.firmware.cmd.enroll.EnrollData;
import ar.com.dcsys.firmware.cmd.enroll.EnrollResult;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.Fingerprint;

@ServerEndpoint(value="/cmd", configurator=WebsocketConfigurator.class)
public class CommandsEndpoint {

	private static final Logger logger = Logger.getLogger(CommandsEndpoint.class.getName());
	
	private final PersonSerializer personSerializer = new PersonSerializer();
	private final PersonsManager personsManager;
	
	private final SerialDevice sd;
	private final Identify identify;
	private final FpCancel cancel;
	private final EnrollAndStoreInRam enroll;
	
	@Inject
	public CommandsEndpoint(SerialDevice sd, Identify i, FpCancel cancel, EnrollAndStoreInRam enroll, 
							PersonsManager personsManager) {
		this.sd = sd;
		this.identify = i;
		this.cancel = cancel;
		this.enroll = enroll;
		
		this.personsManager = personsManager;
	}
	
	
	@OnOpen
	public void onOpen(Session s, EndpointConfig config) throws IOException {
		logger.fine("onOpen");
	}
	
	private void end(final RemoteEndpoint.Basic remote) {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					remote.sendText("finalizando App");
				} catch (IOException e) {
					e.printStackTrace();
				}
				App.setEnd();
			}
		};
		App.addCommand(r);
		
	}
	
	
	private void persistPerson(String json, RemoteEndpoint.Basic remote) throws CmdException {
		Person person = personSerializer.read(json);
		try {
			personsManager.persist(person);
			remote.sendText("OK");
			
		} catch (PersonException | IOException e) {
			e.printStackTrace();
			
			try {
				remote.sendText("ERROR");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			throw new CmdException(e);
			
		}
	}
	
	
	private void controlLed(final boolean v, final RemoteEndpoint.Basic remote) throws CmdException {
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					SensorLedControl slc = new SensorLedControl();
					slc.execute(sd, v, new SensorLedControl.SensorLedControlResult() {
						@Override
						public void onSuccess() {
							try {
								remote.sendText("Led " + String.valueOf(v));
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						@Override
						public void onFailure() {
							try {
								remote.sendText("Error en comando");
							} catch (IOException e) {
								e.printStackTrace();
							}						
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
				}					
			}
		};
		App.addCommand(r);
		
	}

	private void enroll(final EnrollData ed, final RemoteEndpoint.Basic remote) throws CmdException {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					enroll.execute(sd, new EnrollResult() {
						
						@Override
						public void onSuccess(Fingerprint fp) {
							try {
								
								StringBuilder sb = new StringBuilder();
								sb.append(fp.getAlgorithm()).append("\n");
								sb.append(fp.getCodification()).append("\n");
								sb.append(fp.getPersonId()).append("\n");
								
								String template = DatatypeConverter.printBase64Binary(fp.getTemplate());
								sb.append(template);
								
								remote.sendText(sb.toString());
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText(String.valueOf(errorCode));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("comando cancelado");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void releaseFinger() {
							try {
								remote.sendText("levantar el dedo del lector");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onTimeout() {
							try {
								remote.sendText("timeout");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onBadQuality() {
							try {
								remote.sendText("mala calidad");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needThirdSweep() {
							try {
								remote.sendText("necesita tercera huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needSecondSweep() {
							try {
								remote.sendText("necesita segunda huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needFirstSweep() {
							try {
								remote.sendText("necesita primera huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}, ed);
				} catch (CmdException e) {
					e.printStackTrace();
				}
			}
		};
		App.addCommand(r);
	}
	
	private void cancel(final RemoteEndpoint.Basic remote) throws CmdException {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					cancel.execute(sd, new FpCancelResult() {
						@Override
						public void onSuccess() {
							logger.fine("Cancelado exitosamente");
							try {
								remote.sendText("Cancelado exitosamente");
							} catch (IOException e) {
								e.printStackTrace();
							}				
						}
					});
				} catch (CmdException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		App.addCommand(r);
		
	}

	
	private void identify(final RemoteEndpoint.Basic remote) throws CmdException {

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					identify.execute(sd, new IdentifyResult() {
						
						@Override
						public void releaseFinger() {
							try {
								remote.sendText("liberar dedo");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onSuccess(int fpNumber) {
							try {
								remote.sendText("huella encontrada : " + String.valueOf(fpNumber));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onNotFound() {
							try {
								remote.sendText("huella no encontrada");
							
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("Error : " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("identificación cancelada");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}					
		};
		App.addCommand(r);
		
	}
	
	private void test(final RemoteEndpoint.Basic remote) {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				TestConnection tc = new TestConnection();
				try {
					tc.execute(sd, new TestConnectionResult() {
						@Override
						public void onSuccess() {
							try {
								remote.sendText("Test de conección exitoso");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure() {
							try {
								remote.sendText("error ejecutando test");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					try {
						remote.sendText("Error ejecutando test : " + e.getMessage());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		};
		App.addCommand(r);
		
	}
	
	
	private void getFirmwareVersion(final RemoteEndpoint.Basic remote) throws CmdException {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				
				GetFirmwareVersion gfv = new GetFirmwareVersion();
				try {
					gfv.execute(sd, new GetFirmwareVersionResult() {
						@Override
						public void onSuccess(String version) {
							try {
								remote.sendText(version);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						@Override
						public void onFailure() {
							try {
								remote.sendText("error get firmware version");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					try {
						remote.sendText("Error : " + e.getMessage());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			}
		};
		App.addCommand(r);
		
	}
	
	
	@OnMessage
	public void onMessage(String m, final Session session) {
		logger.fine("Mensaje recibido : " + m);
		
		RemoteEndpoint.Basic remote = session.getBasicRemote();

		try {
			if (m.startsWith("persistPerson;")) {
				
				String json = m.substring(m.indexOf(";") + 1);
				persistPerson(json, remote);
				
			} else if ("firmware".equals(m)) {
				
				getFirmwareVersion(remote);
				
			} else if ("ledOn".equals(m)) {
				
				controlLed(true,remote);
				
			} else if ("ledOff".equals(m)) {
					
				controlLed(false,remote);
				
			} else if ("test".equals(m)) {
				
				test(remote);
				
			} else if (m.startsWith("enroll;")) {
				String id = m.substring(7);
				
				logger.fine("enrolando usuario : " + id);

				DefaultEnrollData ed = new DefaultEnrollData();
				ed.setFinger(Finger.LEFT_INDEX);
				ed.setPersonId(id);
				
				enroll(ed, remote);
				
			} else if ("identify".equals(m)) {
			
				logger.fine("Mensaje de identificación");
				identify(remote);
				
			} else if ("cancel".equals(m)) {
				
				logger.fine("Cancelando comando");
				cancel(remote);
				
			} else if ("end".equals(m)) {
				
				logger.fine("finalizando app");
				end(remote);
				
			} else if (m.startsWith("person;")) {
				
				String data = m.substring(7);
				persistPerson(data, remote);
				
			}
		
		} catch (CmdException e) {
			try {
				remote.sendText(e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}
	
	@OnError
	public void onError(Throwable t) {
	}

	@OnClose
	public void onClose(Session s, CloseReason reason) {
		logger.fine("onClose");
	}
}
