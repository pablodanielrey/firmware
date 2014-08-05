package ar.com.dcsys.firmware.websocket;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
import ar.com.dcsys.data.device.Device;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.log.AttLog;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.AttLogException;
import ar.com.dcsys.exceptions.DeviceException;
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
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate;
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate.ClearAllTemplateResult;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId.GetEmptyIdResult;
import ar.com.dcsys.firmware.cmd.template.ReadRawTemplate;
import ar.com.dcsys.firmware.cmd.template.ReadRawTemplate.ReadRawTemplateResult;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate.WriteTemplateResult;
import ar.com.dcsys.firmware.database.FingerprintMapping;
import ar.com.dcsys.firmware.database.FingerprintMappingDAO;
import ar.com.dcsys.firmware.database.FingerprintMappingException;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.log.AttLogsManager;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Finger;
import ar.com.dcsys.security.Fingerprint;

@ServerEndpoint(value="/cmd", configurator=WebsocketConfigurator.class)
public class CommandsEndpoint {

	private static final Logger logger = Logger.getLogger(CommandsEndpoint.class.getName());
	
	private final FingerprintSerializer fingerprintSerializer = new FingerprintSerializer();
	private final PersonSerializer personSerializer = new PersonSerializer();
	private final PersonsManager personsManager;
	private final AttLogsManager attLogsManager;
	
	private final SerialDevice sd;
	private final Identify identify;
	private final FpCancel cancel;

	private final SensorLedControl sensorLedControl;
	private final TestConnection testConnection;
	private final GetFirmwareVersion getFirmwareVersion;
	
	private final EnrollAndStoreInRam enroll;
	private final GetEmptyId getEmptyId;
	private final WriteTemplate writeTemplate;
	private final ReadRawTemplate readRawTemplate;
	private final ClearAllTemplate clearAllTemplate;
	
	private final Initialize initialize;
	
	private final Leds leds;
	
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
											 ReadRawTemplate readRawTemplate,
											 ClearAllTemplate clearAllTemplate,
											 
											 Initialize initialize,
											 
											 PersonsManager personsManager,
											 FingerprintDAO fingerprintDAO,
											 FingerprintMappingDAO fingerprintMappingDAO,
											 AttLogsManager attLogsManager,
											 
											 Leds leds
			
										) {
		this.sd = sd;
		this.identify = i;
		this.cancel = cancel;
		
		this.enroll = enroll;
		this.getEmptyId = getEmptyId;
		this.writeTemplate = writeTemplate;
		
		this.sensorLedControl = sensorLedControl;
		this.testConnection = testConnection;
		this.getFirmwareVersion = getFirmwareVersion;
		this.readRawTemplate = readRawTemplate;
		this.clearAllTemplate = clearAllTemplate;
		
		this.initialize = initialize;
		
		this.leds = leds;
		
		this.personsManager = personsManager;
		this.fingerprintDAO = fingerprintDAO;
		this.fingerprintMappingDAO = fingerprintMappingDAO;
		this.attLogsManager = attLogsManager;
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
					leds.onCommand("enroll");
					
					enroll.execute(sd, new EnrollResult() {
									
						@Override
						public void onSuccess(final Fingerprint fp) {
															
							// genero la respuesta del comando.
							
							leds.onCommand("identify");
							leds.onCommand("ok");
							
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
								leds.onCommand("identify");
								leds.onCommand("error");
								
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onCancel() {
							try {
								leds.onCommand("identify");
								leds.onCommand("error");
								
								remote.sendText("ERROR comando cancelado");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void releaseFinger() {
							try {
								leds.onCommand("ok");
								
								remote.sendText("OK levantar el dedo del lector");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onTimeout() {
							try {
								leds.onCommand("identify");
								leds.onCommand("error");
								
								remote.sendText("ERROR timeout");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onBadQuality() {
							try {
								leds.onCommand("error");
								
								remote.sendText("ERROR mala calidad");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needThirdSweep() {
							try {
								leds.onCommand("ok");
								
								remote.sendText("OK necesita tercera huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needSecondSweep() {
							try {
								leds.onCommand("ok");
								
								remote.sendText("OK necesita segunda huella");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void needFirstSweep() {
							try {
								leds.onCommand("ok");
								
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
							
							leds.onCommand("identify");
							
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
	
	
	/**
	 * Se inicializan los datos del dispositivo dentro de la base local.
	 */
	private void initializeDevice(final RemoteEndpoint.Basic remote) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					String id = initialize.execute();
					remote.sendText("OK " + id);
					
				} catch (CmdException | IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
						
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
				}
			}
		};
		App.addCommand(r);
	}
	
	/**
	 * Setea el estado del dispositivo dentro de la base.
	 * @param v
	 * @param remote
	 */
	private void setEnabled(final Boolean v, final RemoteEndpoint.Basic remote) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					String enabled = initialize.getCubieDeviceData().getEnabled();
					initialize.getCubieDeviceData().setEnabled(v.toString());
					String id = initialize.execute();
					initialize.getCubieDeviceData().setEnabled(enabled);
					
					leds.onCommand("ok");
					
					remote.sendText("OK " + id);
					
				} catch (CmdException | IOException e) {
					
					leds.onCommand("error");
					
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
						
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);
					}
				}
			}
		};
		App.addCommand(r);
	}

	
	
	/**
	 * Crea el registro de assitencia para el reloj actual y para la persona y huella identificada con fpNumber
	 * @param fpNumber
	 */
	private String createAssistanceLog(int fpNumber) throws AttLogException, FingerprintMappingException, DeviceException {
		
		FingerprintMapping fpm = fingerprintMappingDAO.findBy(fpNumber);
		if (fpm == null) {
			throw new FingerprintMappingException("No existe mapping con ese id de huella " + String.valueOf(fpNumber));
		}
		
		Device d = initialize.getCurrentDevice();
		Person p = new Person();
		p.setId(fpm.getPersonId());
		
		
		AttLog log = new AttLog();
		log.setDate(new Date());
		log.setDevice(d);
		log.setId(UUID.randomUUID().toString());
		log.setPerson(p);
		log.setVerifyMode(1l);
			
		attLogsManager.persist(log);
			
		return fpm.getPersonId();
	}
	
	
	
	private void identify(final RemoteEndpoint.Basic remote) throws CmdException {

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					leds.onCommand("identify");
					
					identify.execute(sd, new IdentifyResult() {
						
						@Override
						public void releaseFinger() {
							try {
								leds.onCommand("ok");
								
								remote.sendText("OK liberar dedo");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onSuccess(int fpNumber) {
							
							try {
								String person = createAssistanceLog(fpNumber);
								
								leds.onCommand("ok");
								
								remote.sendText("OK " + person + " " + String.valueOf(fpNumber));
								

								
							} catch (AttLogException | FingerprintMappingException | DeviceException | IOException e1) {
								logger.log(Level.SEVERE,e1.getMessage(),e1);

								leds.onCommand("error");
								
								try {
									remote.sendText("ERROR " + e1.getMessage());
								
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
								}								
							}
							
						}
						
						@Override
						public void onNotFound() {
							try {
								leds.onCommand("error");
								remote.sendText("OK huella no encontrada");
															
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								leds.onCommand("error");
								remote.sendText("ERROR " + String.valueOf(errorCode));
															
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onCancel() {
							try {
								leds.onCommand("error");
								remote.sendText("ERROR identificación cancelada");
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} catch (CmdException e) {
					leds.onCommand("error");
					
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
	
	private void readRawTemplate(final int number, final RemoteEndpoint.Basic remote) throws CmdException {
		 Runnable r = new Runnable() {
			 @Override
			public void run() {
				 try {
					readRawTemplate.execute(sd, number, new ReadRawTemplateResult() {
						@Override
						public void onSuccess(byte[] templ) {
							
							String etempl = DatatypeConverter.printBase64Binary(templ);
							try {
								remote.sendText("OK " + etempl);
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
							}
						}
						
						@Override
						public void onInvalidTemplateNumber(int number) {
							try {
								remote.sendText("ERROR invalid template number " + String.valueOf(number));
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
							}							
						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("ERROR code " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
							}							
						}
						
						@Override
						public void onEmptyTemplate(int number) {
							try {
								remote.sendText("ERROR empty template " + String.valueOf(number));
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
							}							
						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("ERROR comando cancelado");
								
							} catch (IOException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
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
	
	
	private void purgeFingerprints(final RemoteEndpoint.Basic remote) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					clearAllTemplate.excecute(sd, new ClearAllTemplateResult() {
						
						@Override
						public void onSuccess(int number) {
							try {
								List<FingerprintMapping> fps = fingerprintMappingDAO.findAll();
								for (FingerprintMapping fp : fps) {
									fingerprintMappingDAO.delete(fp);
								}
								remote.sendText("OK " + String.valueOf(number) + " " + String.valueOf(fps.size()));

							} catch (IOException | FingerprintMappingException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("ERROR cancelado");
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
					});
					
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
		};
		App.addCommand(r);
	}
	
	
	private void clearAllTemplate(final RemoteEndpoint.Basic remote) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					clearAllTemplate.excecute(sd, new ClearAllTemplateResult() {
						
						@Override
						public void onSuccess(int number) {
							try {
								remote.sendText("OK " + String.valueOf(number));
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
						
						@Override
						public void onFailure(int errorCode) {
							try {
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
						
						@Override
						public void onCancel() {
							try {
								remote.sendText("ERROR cancelado");
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
					});
					
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
		};
		App.addCommand(r);
		
	}
	
	@OnMessage
	public void onMessage(String m, final Session session) {
		logger.fine("Mensaje recibido : " + m);
		
		RemoteEndpoint.Basic remote = session.getBasicRemote();

		try {
			
			if (m.startsWith("leds;")) {
				
				String subcommand = m.substring(5);
				leds.onCommand(subcommand);
				
			} else if ("initialize".equals(m)) {
				
				initializeDevice(remote);
				
			} else if ("enable".equals(m)) {
				
				setEnabled(true, remote);
				
			} else if ("disable".equals(m)) {
				
				setEnabled(false, remote);
				
			} else if ("clearAllTemplate".equals(m)) {
				
				clearAllTemplate(remote);
				
			} else if ("purgeFingerprints".equals(m)) {
				
				purgeFingerprints(remote);
				
			} else if (m.startsWith("readRawTemplate;")) {
				
				String cmd = "readRawTemplate;";
				String number = m.substring(cmd.length());
				
				try {
					int nnumber = Integer.parseInt(number);
					readRawTemplate(nnumber,remote);
					
				} catch (Exception e) {
					try {
						remote.sendText("ERROR comando inválido. ej : readRawTemplate;10");
						
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					}
				}
				
			} else if (m.startsWith("persistFingerprint;")) {
				
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
				leds.onCommand("test");
				
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
				leds.stop();
				
			} else if (m.startsWith("person;")) {
				
				String data = m.substring(7);
				persistPerson(data, remote);
				
			} else {
				
				try {
					remote.sendText("comando desconocido");
					
				} catch (IOException e1) {
					e1.printStackTrace();
					logger.log(Level.SEVERE,e1.getMessage(),e1);
				}
			
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
