package ar.com.dcsys.firmware.websocket;

import java.io.IOException;
import java.util.logging.Level;
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

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.FingerprintException;
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
import ar.com.dcsys.firmware.cmd.template.GetEmptyId;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId.GetEmptyIdResult;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate.WriteTemplateResult;
import ar.com.dcsys.firmware.database.FingerprintMapping;
import ar.com.dcsys.firmware.database.FingerprintMappingDAO;
import ar.com.dcsys.firmware.database.FingerprintMappingException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.Fingerprint;

@ServerEndpoint(value="/cmd", configurator=WebsocketConfigurator.class)
public class CommandsEndpoint {

	private static final Logger logger = Logger.getLogger(CommandsEndpoint.class.getName());
	
	private final FingerprintSerializer fingerprintSerializer = new FingerprintSerializer();
	private final PersonSerializer personSerializer = new PersonSerializer();
	private final PersonsManager personsManager;
	
	private final SerialDevice sd;
	private final Identify identify;
	private final FpCancel cancel;

	private final SensorLedControl sensorLedControl;
	private final TestConnection testConnection;
	private final GetFirmwareVersion getFirmwareVersion;
	
	private final EnrollAndStoreInRam enroll;
	private final GetEmptyId getEmptyId;
	private final WriteTemplate writeTemplate;
	
	private final FingerprintDAO fingerprintDAO;
	private final FingerprintMappingDAO fingerprintMappingDAO;
	
	@Inject
	public CommandsEndpoint(SerialDevice sd, Identify i, 
											 FpCancel cancel, 
											 EnrollAndStoreInRam enroll, 
											 SensorLedControl sensorLedControl,
											 TestConnection testConnection,
											 GetFirmwareVersion getFirmwareVersion,
											 GetEmptyId getEmptyId,
											 WriteTemplate writeTemplate,
											 
											 PersonsManager personsManager,
											 FingerprintDAO fingerprintDAO,
											 FingerprintMappingDAO fingerprintMappingDAO) {
		this.sd = sd;
		this.identify = i;
		this.cancel = cancel;
		
		this.enroll = enroll;
		this.getEmptyId = getEmptyId;
		this.writeTemplate = writeTemplate;
		
		this.sensorLedControl = sensorLedControl;
		this.testConnection = testConnection;
		this.getFirmwareVersion = getFirmwareVersion;
		
		this.personsManager = personsManager;
		this.fingerprintDAO = fingerprintDAO;
		this.fingerprintMappingDAO = fingerprintMappingDAO;
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
					remote.sendText("OK finalizando App");
				} catch (IOException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
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
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
			
			throw new CmdException(e);
			
		}
	}
	
	
	private void controlLed(final boolean v, final RemoteEndpoint.Basic remote) throws CmdException {
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					sensorLedControl.execute(sd, v, new SensorLedControl.SensorLedControlResult() {
						@Override
						public void onSuccess() {
							try {
								remote.sendText("OK Led " + String.valueOf(v));
							} catch (IOException e) {
								e.printStackTrace();
								logger.log(Level.SEVERE,e.getMessage(),e);
							}
						}
						@Override
						public void onFailure() {
							try {
								remote.sendText("ERROR");
							} catch (IOException e) {
								e.printStackTrace();
								logger.log(Level.SEVERE,e.getMessage(),e);
							}						
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}					
				}					
			}
		};
		App.addCommand(r);
		
	}


	/**
	 * Sobreescribe el template en la ubicacion tmplNumber de la rom del lector.
	 * @param tmplNumber
	 * @param remote
	 */
	private void writeTemplate(TemplateData td, final RemoteEndpoint.Basic remote) {

		try {
			writeTemplate.execute(sd, new WriteTemplateResult() {
				
				@Override
				public void onSuccess(int tnumber) {
					
					try {
						remote.sendText("OK " + String.valueOf(tnumber));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
					
				}
				
				public void onCancel() {
					try {
						remote.sendText("ERROR cancelado");
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				};
				
				public void onFailure(int errorCode) {
					try {
						remote.sendText("ERROR " + String.valueOf(errorCode));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				};
				
				public void onInvalidTemplateNumber(int number) {
					try {
						remote.sendText("ERROR número de huella inválido " + String.valueOf(number));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				};
				
				public void onInvalidTemplateSize(int size) {
					try {
						remote.sendText("ERROR tamaño de huella inválido " + String.valueOf(size));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				};
				
			}, td);
			
		} catch (CmdException cmdE) {
			
			logger.log(Level.SEVERE,cmdE.getMessage(),cmdE);
			try {
				remote.sendText("ERROR " + cmdE.getMessage());
				
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
			}
			
		}		
	}
	
	
	/**
	 * Actualiza una huella dentro de la base.
	 * En el caso de que ya existe el mapeo de la huella dentro del lector y la base usa esa misma posicion en la rom del lector.
	 * @param fp
	 */
	private void updateFingerprint(final Fingerprint fp, final RemoteEndpoint.Basic remote) {
		
		Runnable r = new Runnable() {
			public void run() {
					
				//chequeo si ya existe el mapeo dentro del lector.
				String personId = fp.getPersonId();
				String fingerprintId = fp.getId();
				FingerprintMapping fpm = null;
				try {
					fpm = fingerprintMappingDAO.fingBy(personId, fingerprintId);
					
				} catch (FingerprintMappingException e2) {
					try {
						remote.sendText("ERROR " + e2.getMessage());
						return;
						
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
						return;
					}						
				}					
				
				try {
					// actualizo la huella en la base.
					fingerprintDAO.persist(fp);
					
				} catch (FingerprintException fe) {
					try {
						remote.sendText("ERROR " + fe.getMessage());
						return;
								
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						return;
					}						
				}
				
				
				
				if (fpm != null) {
					
					// sobre escribo la rom del lector en la posicion que ya tenía esa huella.
					
					TemplateData tedata = new TemplateData();
					tedata.setFingerprint(fp);
					tedata.setNumber(fpm.getFpNumber());

					writeTemplate(tedata, remote);						
					
				} else {
				
					try {
						
						final String fpId = fp.getId();
						getEmptyId.execute(sd, new GetEmptyIdResult() {
							
							@Override
							public void onSuccess(int tmplNumber) {
								
								try {
									
									// genero el mapeo en la base
									
									FingerprintMapping fpMapping = new FingerprintMapping();
									fpMapping.setFingerprintId(fpId);
									fpMapping.setPersonId(fp.getPersonId());
									fpMapping.setFpNumber(tmplNumber);
									
									fingerprintMappingDAO.persist(fpMapping);
									
									
									
									// escribo la rom del lector.
									
									TemplateData tedata = new TemplateData();
									tedata.setFingerprint(fp);
									tedata.setNumber(tmplNumber);

									writeTemplate(tedata, remote);
									
								} catch (FingerprintMappingException e) {
									try {
										remote.sendText("ERROR " + e.getMessage());
										
									} catch (IOException e1) {
										e1.printStackTrace();
										logger.log(Level.SEVERE,e1.getMessage(),e1);
									}
									
								}
								
							}
							
							@Override
							public void onFailure(int errorCode) {
								try {
									remote.sendText("ERROR " + String.valueOf(errorCode));
									
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
								}							}
							
							@Override
							public void onEmptyNotExistent() {
								try {
									remote.sendText("ERROR no existe lugar libre en la rom del lector");
									
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
								}							}
							
							@Override
							public void onCancel() {
								try {
									remote.sendText("ERROR comando cancelado");
									
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
								}							}
						});

						
					} catch (CmdException cmdE) {
						try {
							remote.sendText("ERROR " + cmdE.getMessage());
							
						} catch (IOException e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}						
					}
				}
						
			}
		};
		App.addCommand(r);
		
	}



	/**
	 * Captura la huella de un usuario
	 * @param ed
	 * @param remote
	 * @throws CmdException
	 */
	private void enroll(final EnrollData ed, final RemoteEndpoint.Basic remote) throws CmdException {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
						
				try {
					enroll.execute(sd, new EnrollResult() {
									
						@Override
						public void onSuccess(final Fingerprint fp) {
															
							// genero la respuesta del comando.
							
							try {
								StringBuilder sb = new StringBuilder();
								
								sb.append("OK ");
								
								String fps = fingerprintSerializer.toJson(fp);
								sb.append(fps);
//								String template = DatatypeConverter.printBase64Binary(fp.getTemplate());
								
								remote.sendText(sb.toString());
								
							} catch (IOException e) {
								logger.log(Level.SEVERE, e.getMessage(),e);
							}
															
						}
						
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("ERROR comando cancelado");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void releaseFinger() {
							try {
								remote.sendText("OK levantar el dedo del lector");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onTimeout() {
							try {
								remote.sendText("ERROR timeout");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onBadQuality() {
							try {
								remote.sendText("ERROR mala calidad");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needThirdSweep() {
							try {
								remote.sendText("OK necesita tercera huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needSecondSweep() {
							try {
								remote.sendText("OK necesita segunda huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needFirstSweep() {
							try {
								remote.sendText("OK necesita primera huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}, ed);
					
				} catch (CmdException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
					
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
								remote.sendText("OK Cancelado exitosamente");
							} catch (IOException e) {
								e.printStackTrace();
							}				
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}					
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
								remote.sendText("OK liberar dedo");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onSuccess(int fpNumber) {
							
							
							
							
							try {
								remote.sendText("OK huella encontrada : " + String.valueOf(fpNumber));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onNotFound() {
							try {
								remote.sendText("OK huella no encontrada");
							
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("ERROR identificación cancelada");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}					
				}
			}					
		};
		App.addCommand(r);
		
	}
	
	private void test(final RemoteEndpoint.Basic remote) {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					testConnection.execute(sd, new TestConnectionResult() {
						@Override
						public void onSuccess() {
							try {
								remote.sendText("OK Test de conección exitoso");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure() {
							try {
								remote.sendText("ERROR error ejecutando test");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
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
				try {
					getFirmwareVersion.execute(sd, new GetFirmwareVersionResult() {
						@Override
						public void onSuccess(String version) {
							try {
								remote.sendText("OK " + version);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						@Override
						public void onFailure() {
							try {
								remote.sendText("ERROR");
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					e.printStackTrace();
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
					} catch (IOException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.getMessage(),e1);
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
			if (m.startsWith("persistFingerprint;")) {
				
				// extraigo la huella del comando.
				String cmd = "persistFingerprint;";
				String json = m.substring(cmd.length());
				FingerprintSerializer fps = new FingerprintSerializer();
				Fingerprint fp = fps.read(json);
				
				updateFingerprint(fp, remote);
				
			} else if (m.startsWith("persistPerson;")) {
				
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
			logger.log(Level.SEVERE,e.getMessage(),e);
			try {
				remote.sendText("ERROR " + e.getMessage());
			} catch (IOException e1) {
				e1.printStackTrace();
				logger.log(Level.SEVERE,e1.getMessage(),e1);
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
