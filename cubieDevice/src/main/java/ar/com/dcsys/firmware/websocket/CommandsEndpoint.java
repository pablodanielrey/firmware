package ar.com.dcsys.firmware.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
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

import ar.com.dcsys.auth.server.FingerprintSerializer;
import ar.com.dcsys.data.fingerprint.FingerprintDAO;
import ar.com.dcsys.data.person.Person;
import ar.com.dcsys.exceptions.FingerprintException;
import ar.com.dcsys.exceptions.PersonException;
import ar.com.dcsys.firmware.Firmware;
import ar.com.dcsys.firmware.MutualExclusion;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.FpCancel;
import ar.com.dcsys.firmware.cmd.FpCancel.FpCancelResult;
import ar.com.dcsys.firmware.cmd.GetFirmwareVersion;
import ar.com.dcsys.firmware.cmd.SensorLedControl;
import ar.com.dcsys.firmware.cmd.TestConnection;
import ar.com.dcsys.firmware.cmd.enroll.EnrollAndStoreInRam;
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate;
import ar.com.dcsys.firmware.cmd.template.ClearAllTemplate.ClearAllTemplateResult;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId;
import ar.com.dcsys.firmware.cmd.template.GetEmptyId.GetEmptyIdResult;
import ar.com.dcsys.firmware.cmd.template.ReadRawTemplate;
import ar.com.dcsys.firmware.cmd.template.ReadRawTemplate.ReadRawTemplateResult;
import ar.com.dcsys.firmware.cmd.template.TemplateData;
import ar.com.dcsys.firmware.cmd.template.WriteTemplate;
import ar.com.dcsys.firmware.cmd.template.WriteTemplateResult;
import ar.com.dcsys.firmware.database.FingerprintMapping;
import ar.com.dcsys.firmware.database.FingerprintMappingDAO;
import ar.com.dcsys.firmware.database.FingerprintMappingException;
import ar.com.dcsys.firmware.database.Initialize;
import ar.com.dcsys.firmware.leds.Leds;
import ar.com.dcsys.firmware.model.Model;
import ar.com.dcsys.firmware.model.Response;
import ar.com.dcsys.firmware.reader.Reader;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.model.PersonsManager;
import ar.com.dcsys.model.log.AttLogsManager;
import ar.com.dcsys.person.server.PersonSerializer;
import ar.com.dcsys.security.Fingerprint;

@ServerEndpoint(value="/cmd", configurator=WebsocketConfigurator.class)
public class CommandsEndpoint {

	private static final Logger logger = Logger.getLogger(CommandsEndpoint.class.getName());
	
	private final FingerprintSerializer fingerprintSerializer = new FingerprintSerializer();
	private final PersonSerializer personSerializer = new PersonSerializer();
	private final PersonsManager personsManager;
	private final AttLogsManager attLogsManager;
	
	private final SerialDevice sd;
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
		
	private final FingerprintDAO fingerprintDAO;
	private final FingerprintMappingDAO fingerprintMappingDAO;

	
	private final Firmware app;
	private final Leds leds;
	private final Model model;
	private final Reader reader;
	
	@Inject
	public CommandsEndpoint(SerialDevice sd, FpCancel cancel, 
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
											 
											 Firmware app,
											 Leds leds,
											 Model model,
											 Reader reader
			
										) {
		
		this.app = app;
		this.model = model;
		this.reader = reader;
		
		
		this.sd = sd;
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
	
	
	
	private void end(final Response remote) {
		
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
				app.setEnd();
			}
		};
		app.addCommand(r);
		
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
	
	



	/**
	 * Sobreescribe el template en la ubicacion tmplNumber de la rom del lector.
	 * @param tmplNumber
	 * @param remote
	 */
	private void writeTemplate(TemplateData td, final Response remote) {

		try {
			writeTemplate.execute(sd, new WriteTemplateResult() {
				
				@Override
				public void onSuccess(int tnumber) {
					
					try {
						remote.sendText("OK " + String.valueOf(tnumber));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					} finally {
						leds.onCommand("ok");						
					}
					
				}
				
				public void onCancel() {
					try {
						remote.sendText("ERROR cancelado");
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					} finally {
						leds.onCommand("error");						
					}
					
				};
				
				public void onFailure(int errorCode) {
					try {
						remote.sendText("ERROR " + String.valueOf(errorCode));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						
					} finally {
						leds.onCommand("error");						
					}
				};
				
				public void onInvalidTemplateNumber(int number) {
					try {
						remote.sendText("ERROR número de huella inválido " + String.valueOf(number));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					} finally {
						leds.onCommand("error");						
					}
				};
				
				public void onInvalidTemplateSize(int size) {
					try {
						remote.sendText("ERROR tamaño de huella inválido " + String.valueOf(size));
						
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
					} finally {
						leds.onCommand("error");						
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
				
			} finally {
				leds.onCommand("error");				
			}
			
		}		
	}
	
	
	/**
	 * Regenera las huellas de la base dentro del lector.
	 * deja solo las huellas que existen en la base y crea nuevamente todos los mapeos.
	 */
	private void consistency(final Response remote) {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					leds.onCommand("blocked");
					
					// elimino todos los mappings.
					fingerprintMappingDAO.deleteAll();
					
					leds.onCommand("subOk");
					
					// elimino todas las huellas del lector
					clearAllTemplate.excecute(sd, new ClearAllTemplateResult() {
						
						@Override
						public void onSuccess(int number) {
							
							leds.onCommand("subOk");

							
							try {
								// genero todas las huellas nuevamente.
								List<String> fids = fingerprintDAO.findAll();
								
								final Semaphore sem = new Semaphore(1);
								final Queue<String> qfids = new ConcurrentLinkedQueue<String>();
								qfids.addAll(fids);
								
								while (!qfids.isEmpty()) {
						
									// espero que finalice el procesamiento de la huella anterior
									try {
										sem.acquire();
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}

									leds.onCommand("subOk");

									
									// obtengo la huella
									final String id = qfids.poll();
									final Fingerprint fp = fingerprintDAO.findById(id);

									getEmptyId.execute(sd, new GetEmptyIdResult() {
										@Override
										public void onSuccess(int tmplNumber) {

											try {
												TemplateData td = new TemplateData();
												td.setFingerprint(fp);
												td.setNumber(tmplNumber);
												writeTemplate.execute(sd, new WriteTemplateResult() {
													
													@Override
													public void onSuccess(int tnumber) {
														
														leds.onCommand("subOk");
													
														
														FingerprintMapping fpm = new FingerprintMapping();
														fpm.setFingerprintId(fp.getId());
														fpm.setPersonId(fp.getPersonId());
														fpm.setFpNumber(tnumber);
														try {
															fingerprintMappingDAO.persist(fpm);
															
														} catch (FingerprintMappingException e1) {
															
															logger.log(Level.SEVERE,e1.getMessage(),e1);
															try {
																remote.sendText("ERROR creating fingerprintmapping for " + String.valueOf(tnumber));
																
															} catch (IOException e2) {
																logger.log(Level.SEVERE,e2.getMessage(),e2);

															} finally {
																sem.release();
																
															}															
														
														}
														
														try {
															remote.sendText("OK " + String.valueOf(tnumber));
															
														} catch (IOException e) {
															logger.log(Level.SEVERE,e.getMessage(),e);
															
														} finally {
															sem.release();
															
														}
														
													}
													
													public void onCancel() {
														leds.onCommand("error");
														
														try {
															remote.sendText("ERROR cancelado");
															
														} catch (IOException e) {
															logger.log(Level.SEVERE,e.getMessage(),e);

														} finally {
															sem.release();
															
														}
													};
													
													public void onFailure(int errorCode) {
														leds.onCommand("error");
														
														try {
															remote.sendText("ERROR " + String.valueOf(errorCode));
															
														} catch (IOException e) {
															logger.log(Level.SEVERE,e.getMessage(),e);
														
														} finally {
															sem.release();
															
														}
													};
													
													public void onInvalidTemplateNumber(int number) {
														leds.onCommand("error");
														
														try {
															remote.sendText("ERROR número de huella inválido " + String.valueOf(number));
															
														} catch (IOException e) {
															logger.log(Level.SEVERE,e.getMessage(),e);
														
														} finally {
															sem.release();
															
														}
													};
													
													public void onInvalidTemplateSize(int size) {
														
														leds.onCommand("error");
														
														try {
															remote.sendText("ERROR tamaño de huella inválido " + String.valueOf(size));
															
														} catch (IOException e) {
															logger.log(Level.SEVERE,e.getMessage(),e);
														
														} finally {
															sem.release();
															
														}
													};
													
												}, td);
												
											} catch (CmdException e) {
												
												sem.release();
												
											}
											
											
										}
										
										@Override
										public void onFailure(int errorCode) {
											
											leds.onCommand("error");
											
											logger.log(Level.SEVERE,"getEmptyId error");
											try {
												remote.sendText("ERROR " + String.valueOf(errorCode));
												
											} catch (IOException e) {
												logger.log(Level.SEVERE,e.getMessage(),e);
												
											} finally {
												sem.release();
																								
											}
										}
										
										@Override
										public void onEmptyNotExistent() {
											
											leds.onCommand("error");
											
											logger.log(Level.SEVERE,"no space left");
											try {
												remote.sendText("ERROR no space left");
												
											} catch (IOException e) {
												logger.log(Level.SEVERE,e.getMessage(),e);
											
											} finally {
												sem.release();
												
											}
										}
										
										@Override
										public void onCancel() {
											
											leds.onCommand("error");
											
											logger.log(Level.SEVERE,"canceled");
											try {
												remote.sendText("ERROR consistency canceled");
												
											} catch (IOException e) {
												logger.log(Level.SEVERE,e.getMessage(),e);
											
											} finally {
												sem.release();
												
											}
										}
									});
										
								}
								
								
								
								
							} catch (CmdException | FingerprintException e1) {
								
								leds.onCommand("error");
								
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
								
								try {
									remote.sendText("ERROR creating mappings");
									
								} catch (IOException e2) {
									e1.printStackTrace();
									logger.log(Level.SEVERE,e2.getMessage(),e2);
								}								
								
							}

						}
						
						@Override
						public void onFailure(int errorCode) {
							
							leds.onCommand("error");
							
							try {
								remote.sendText("ERROR clearing templates " + String.valueOf(errorCode));
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
						
						@Override
						public void onCancel() {
							
							leds.onCommand("error");
							
							try {
								remote.sendText("ERROR cancelado");
								
							} catch (IOException e1) {
								e1.printStackTrace();
								logger.log(Level.SEVERE,e1.getMessage(),e1);
							}

						}
					});
					

					
					
				} catch (CmdException | FingerprintMappingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		app.addCommand(r);
		
	}
	
	
	/**
	 * Actualiza una huella dentro de la base.
	 * En el caso de que ya existe el mapeo de la huella dentro del lector y la base usa esa misma posicion en la rom del lector.
	 * @param fp
	 */
	private void updateFingerprint(final Fingerprint fp, final Response remote) {
		
		Runnable r = new Runnable() {
			public void run() {
				
				leds.onCommand("blocked");
				
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
						
					} finally {
						leds.onCommand("error");
						
					}
				}					
				
				try {
					leds.onCommand("subok");
					
					// actualizo la huella en la base.
					fingerprintDAO.persist(fp);
					
				} catch (FingerprintException fe) {
					try {
						remote.sendText("ERROR " + fe.getMessage());
						return;
								
					} catch (IOException e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						return;
					} finally {
						leds.onCommand("error");
					}
				}
				
				
				
				if (fpm != null) {
					
					// sobre escribo la rom del lector en la posicion que ya tenía esa huella.
					
					TemplateData tedata = new TemplateData();
					tedata.setFingerprint(fp);
					tedata.setNumber(fpm.getFpNumber());

					leds.onCommand("subok");					
					
					writeTemplate(tedata, remote);						
					
				} else {
				
					try {
						leds.onCommand("subok");
						
						final String fpId = fp.getId();
						getEmptyId.execute(sd, new GetEmptyIdResult() {
							
							@Override
							public void onSuccess(int tmplNumber) {
								
								try {
									leds.onCommand("subok");
									// genero el mapeo en la base
									
									FingerprintMapping fpMapping = new FingerprintMapping();
									fpMapping.setFingerprintId(fpId);
									fpMapping.setPersonId(fp.getPersonId());
									fpMapping.setFpNumber(tmplNumber);
									
									fingerprintMappingDAO.persist(fpMapping);
									
									
									leds.onCommand("subok");
									
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
										
									} finally {
										leds.onCommand("error");
									}
									
								}
								
							}
							
							@Override
							public void onFailure(int errorCode) {
								try {
									remote.sendText("ERROR " + String.valueOf(errorCode));
									
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
								} finally {
									leds.onCommand("error");
								}							
							}
							
							@Override
							public void onEmptyNotExistent() {
								try {
									remote.sendText("ERROR no existe lugar libre en la rom del lector");
									
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
									
								} finally {
									leds.onCommand("error");									
								}							}
							
							@Override
							public void onCancel() {
								try {
									remote.sendText("ERROR comando cancelado");
									
								} catch (IOException e) {
									logger.log(Level.SEVERE,e.getMessage(),e);
									
								} finally {
									leds.onCommand("error");									
								}							}
						});

						
					} catch (CmdException cmdE) {
						try {
							remote.sendText("ERROR " + cmdE.getMessage());
							
						} catch (IOException e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
							
						} finally {
							leds.onCommand("error");							
						}						
					}
				}
						
			}
		};
		app.addCommand(r);
		
	}



	


		
	
	private void cancel(final Response remote) throws CmdException {
		
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
		app.addCommand(r);
		
	}
	
	
	/**
	 * Se inicializan los datos del dispositivo dentro de la base local.
	 */
	private void initializeDevice(final Response remote) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					
					leds.onCommand("blocked");
					
					String id = initialize.execute();
					
					leds.onCommand("ok");
					
					remote.sendText("OK " + id);
							
					
				} catch (CmdException | IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR " + e.getMessage());
						
					} catch (IOException e1) {
						logger.log(Level.SEVERE,e1.getMessage(),e1);
						
					} finally {
						leds.onCommand("error");
					}
				}
			}
		};
		app.addCommand(r);
	}
	
	/**
	 * Setea el estado del dispositivo dentro de la base.
	 * @param v
	 * @param remote
	 */
	private void setEnabled(final Boolean v, final Response remote) {
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
		app.addCommand(r);
	}

	
	
		
	private void purgeFingerprints(final Response remote) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					clearAllTemplate.excecute(sd, new ClearAllTemplateResult() {
						
						@Override
						public void onSuccess(int number) {
							try {
								List<FingerprintMapping> fps = fingerprintMappingDAO.findAll();
								fingerprintMappingDAO.deleteAll();
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
		app.addCommand(r);
	}
	
	
	

	
	private void checkIntegrity(final Response remote) {
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				
				leds.onCommand("blocked");
				
				try {
					List<FingerprintMapping> fms = fingerprintMappingDAO.findAll();
					List<String> fps = fingerprintDAO.findAll();

					try {
						remote.sendText("OK fingerprints " + String.valueOf(fps.size()) + " mapping " + String.valueOf(fms.size()));
					} catch (IOException e) {
					}
				
					
					// chequeo que los mapeos tengan los mismos bytes dentro del lector y en la base.
					
					final Semaphore sem = new Semaphore(1);
					final Queue<FingerprintMapping> qfms = new ConcurrentLinkedQueue<FingerprintMapping>();
					qfms.addAll(fms);
					
					while (!qfms.isEmpty()) {
						
						try {
							sem.acquire();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						leds.onCommand("subOk");

						final FingerprintMapping fm = qfms.poll();
						String fpId = fm.getFingerprintId();
						if (fpId == null) {
							try {
								remote.sendText("ERROR mapping with fingerprint.id == null");
							} catch (IOException e) {
							} finally {
								sem.release();
							}
							continue;
						}
						
						try {
							
							leds.onCommand("subOk");

							final Fingerprint fp = fingerprintDAO.findById(fpId);

							leds.onCommand("subOk");

							// obtengo los datos raw del lector y los comparo con los bytes del template guardado en la base.
							readRawTemplate.execute(sd, fm.getFpNumber(), new ReadRawTemplateResult() {
								@Override
								public void onSuccess(byte[] templ) {

									leds.onCommand("ok");
									
									try {
										byte[] tmplData = fp.getTemplate();
										
										if (tmplData.length != templ.length) {
											
											leds.onCommand("error");
											
											try {
												remote.sendText("ERROR " + fp.getId() + " mapping to " + String.valueOf(fm.getFpNumber()) + " different lengths");
											} catch (IOException e) {
											}
											return;
										}
										
										
										for (int i = 0; i < tmplData.length; i++) {
											if (tmplData[i] != templ[i]) {
												
												leds.onCommand("error");
												
												try {
													remote.sendText("ERROR " + fp.getId() + " mapping to " + String.valueOf(fm.getFpNumber()) + " byteB != byteL " + String.format("%02X",tmplData[i]) + " != " + String.format("%02X",templ[i]));
												} catch (IOException e) {
												}
												return;
											}
										}
										
									} finally {
										sem.release();
									}
									
								}
								
								@Override
								public void onInvalidTemplateNumber(int number) {
									
									leds.onCommand("error");
									
									try {
										remote.sendText("ERROR " + fp.getId() + " " + " mapping to " + String.valueOf(number) + " invalid template number");
									} catch (IOException e) {
									} finally {
										sem.release();
									}
								}
								
								@Override
								public void onFailure(int errorCode) {
									
									leds.onCommand("error");
									
									try {
										remote.sendText("ERROR " + fp.getId() + " " + String.valueOf(errorCode));
									} catch (IOException e) {
									} finally {
										sem.release();
									}
								}
								
								@Override
								public void onEmptyTemplate(int number) {
									
									leds.onCommand("error");
									
									try {
										remote.sendText("ERROR " + fp.getId() + " " + " mapping to " + String.valueOf(number) + " empty");
									} catch (IOException e) {
									} finally {
										sem.release();
									}
								}
								
								@Override
								public void onCancel() {
									
									leds.onCommand("error");
									
									try {
										remote.sendText("ERROR canceled");
									} catch (IOException e) {
									} finally {
										sem.release();
									}
								}
							});
							
						
						} catch (FingerprintException | CmdException e1) {
							try {
								remote.sendText("ERROR " + fpId);
							} catch (IOException e) {
							} finally {
								sem.release();
							}
						}
						
					}
					
					
				} catch (FingerprintMappingException | FingerprintException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR");
					} catch (IOException e1) {
					}
				}
				
			}
		};
		app.addCommand(r);
		
	}
	
	
	private void summary(final Response remote) {
		
		leds.onCommand("blocked");
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				
				// obtengo datos de la base.
				try {
					leds.onCommand("subOk");

					List<Person> persons = personsManager.findAll();
					
					remote.sendText("OK persons : " + String.valueOf(persons.size()));
					
					for (Person p : persons) {

						leds.onCommand("subOk");

						remote.sendText("OK " + p.getId() + " " + p.getDni());
					}
					
				} catch (PersonException | IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR persons");
						
					} catch (IOException e1) {
						logger.log(Level.WARNING,e1.getMessage(),e1);
					}
				
				}
				
				// obtengo datos de las huellas
				try {
					leds.onCommand("subOk");

					List<String> fids = fingerprintDAO.findAll();
					
					remote.sendText("OK fingerprints : " + String.valueOf(fids.size()));
					
					for (String id : fids) {
						leds.onCommand("subOk");

						remote.sendText("OK " + id);
					}
					
				} catch (FingerprintException | IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR fingerprints");
						
					} catch (IOException e1) {
						logger.log(Level.WARNING,e1.getMessage(),e1);
					}
				}
				
				// obtengo datos de los mapeos
				try {
					leds.onCommand("subOk");

					List<FingerprintMapping> fms = fingerprintMappingDAO.findAll();
					
					remote.sendText("OK fingerprint mappings : " + String.valueOf(fms.size()));
					
					for (FingerprintMapping fm : fms) {
						leds.onCommand("subOk");
						remote.sendText("OK " + fm.getFingerprintId() + " " + fm.getPersonId() + " " + String.valueOf(fm.getFpNumber()));
					}
					
				} catch (FingerprintMappingException | IOException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR fingerprints mappings");
						
					} catch (IOException e1) {
						logger.log(Level.WARNING,e1.getMessage(),e1);
					}

				}
				
				leds.onCommand("subOk");
				try {
					getEmptyId.execute(sd, new GetEmptyIdResult() {
						
						@Override
						public void onSuccess(int tmplNumber) {
							leds.onCommand("ok");
							try {
								remote.sendText("OK " + String.valueOf(tmplNumber));
								
							} catch (IOException e) {
								logger.log(Level.WARNING,e.getMessage(),e);
							}
						}
						
						@Override
						public void onFailure(int errorCode) {
							leds.onCommand("error");
							try {
								remote.sendText("ERROR " + String.valueOf(errorCode));
								
							} catch (IOException e) {
								logger.log(Level.WARNING,e.getMessage(),e);
							}
						}
						
						@Override
						public void onEmptyNotExistent() {
							leds.onCommand("ok");
							try {
								remote.sendText("OK full");
								
							} catch (IOException e) {
								logger.log(Level.WARNING,e.getMessage(),e);
							}
						}
						
						@Override
						public void onCancel() {
							leds.onCommand("error");
							try {
								remote.sendText("ERROR canceled");
								
							} catch (IOException e) {
								logger.log(Level.WARNING,e.getMessage(),e);
							}
							
						}
					});
					
				} catch (CmdException e) {
					logger.log(Level.SEVERE,e.getMessage(),e);
					try {
						remote.sendText("ERROR get empty id");
						
					} catch (IOException e1) {
						logger.log(Level.WARNING,e1.getMessage(),e1);
					}
				}			
				
			}
		};
		app.addCommand(r);
		
	}
	
	
	@OnMessage
	public void onMessage(String m, final Session session) {
		
		logger.fine("Mensaje recibido : " + m);
		
		Response remote = new Response() {
			@Override
			public void sendText(String text) throws IOException {
				session.getBasicRemote().sendText(text);
			}
		};
		
		Response exclusionRemote = new Response() {
			@Override
			public void sendText(String text) throws IOException {

				try {
					session.getBasicRemote().sendText(text);
					
				} finally {
					if (text.startsWith("OK") || text.startsWith("ERROR")) {
						MutualExclusion.using[MutualExclusion.EXECUTING_COMMAND].release();
					}			
				}
			}
		};


		try {
			
			/// app //
			
			if (m.startsWith("firmware;")) {
			
				String subcommand = m.substring(9);
				if ("end".equalsIgnoreCase(subcommand)) {
					end(remote);
				}				
				
				
			//// leds /////
			
			} else if (m.startsWith("leds;")) {
				
				String subcommand = m.substring(5);
				leds.onCommand(subcommand);
			

			// modelo ///
				
			} else if (m.startsWith("model;")) {
				
				String subcommand = m.substring(6);
				model.onCommand(subcommand,remote);
				
				
			//// lector ////
				
				
			} else if (m.startsWith("reader;")) {
				
				String subcommand = m.substring(7);
				reader.onCommand(subcommand,remote);
				

			} else if ("cancel".equals(m)) {
				
				logger.fine("Cancelando comando");
				cancel(remote);
				
				
				
				
				
				
			//// base ////
				/*
				
			} else if ("initialize".equals(m)) {
				
				initializeDevice(remote);
				
			} else if ("enable".equals(m)) {
				
				setEnabled(true, remote);
				
			} else if ("disable".equals(m)) {
				
				setEnabled(false, remote);
				
				
				
			/// base y lector ////	
				
			} else if ("check".equals(m)) {
				
				checkIntegrity(remote);
				
			} else if ("regenerate".equals(m)) {
				
				consistency(remote);
				
				
			} else if ("purgeFingerprints".equals(m)) {
				
				purgeFingerprints(remote);				


			} else if (m.startsWith("persistFingerprint;")) {
				
				// extraigo la huella del comando.
				String cmd = "persistFingerprint;";
				String json = m.substring(cmd.length());
				FingerprintSerializer fps = new FingerprintSerializer();
				Fingerprint fp = fps.read(json);
				
				updateFingerprint(fp, remote);
				
				
			} else if ("summary".equals(m)) {
				
				summary(remote);
			
				
				
			/////// aplicacion ////////////	

				*/
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
