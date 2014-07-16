package ar.com.dcsys.firmware.cmd.enroll;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import ar.com.dcsys.firmware.camabio.CamabioResponse;
import ar.com.dcsys.firmware.camabio.CamabioUtils;
import ar.com.dcsys.firmware.camabio.SerialUtils;
import ar.com.dcsys.firmware.cmd.CmdException;
import ar.com.dcsys.firmware.cmd.ProcessingException;
import ar.com.dcsys.firmware.serial.SerialDevice;
import ar.com.dcsys.firmware.serial.SerialException;

public class EnrollAndStoreInRam {
	
	private static Logger logger = Logger.getLogger(EnrollAndStoreInRam.class.getName());
	
	private final GetEnrollData getEnrollData;
	
	@Inject
	public EnrollAndStoreInRam(GetEnrollData getEnrollData) {
		this.getEnrollData = getEnrollData;
	}
	
	private void checkPreconditions(CamabioResponse rsp) throws CmdException {
		if (rsp.prefix != CamabioUtils.RSP && rsp.prefix != CamabioUtils.RSP_DATA) {
			throw new CmdException("Prefijo inválido : " + rsp.prefix);
		}
		
		if (rsp.rcm != CamabioUtils.CMD_FP_CANCEL && rsp.rcm != CamabioUtils.CMD_ENROLL_AND_STORE_IN_RAM) {
			throw new CmdException("RCM != EnrollAndStoreInRam");
		}
	}	
	
	public void execute(SerialDevice serialPort, EnrollResult result, EnrollData edata) throws CmdException {
		try {
			byte[] cmd = CamabioUtils.enrollAndStoreInRam();
			serialPort.writeBytes(cmd);
			
			boolean canceled = false;
			
			while (true) {
				byte[] data = SerialUtils.readPackage(serialPort);
				CamabioResponse rsp = CamabioUtils.getResponse(data);
				checkPreconditions(rsp);
				
				
				// evaluo la respuesta del FP_CANCEL.
				if (rsp.rcm == CamabioUtils.CMD_FP_CANCEL && rsp.ret == CamabioUtils.ERR_SUCCESS) {
					if (canceled) {
						return;
					} else {
						canceled = true;
					}
					continue;
				}				
				
				
				if (rsp.ret == CamabioUtils.ERR_SUCCESS) {

					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);
					
					if (code == CamabioUtils.GD_NEED_FIRST_SWEEP) {
						try {
							result.needFirstSweep();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}
					
					if (code == CamabioUtils.GD_NEED_SECOND_SWEEP) {
						try {
							result.needSecondSweep();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}
					
					if (code == CamabioUtils.GD_NEED_THIRD_SWEEP) {
						try {
							result.needThirdSweep();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}

					if (code == CamabioUtils.GD_NEED_RELEASE_FINGER) {
						try {
							result.releaseFinger();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}
					
					getEnrollData.execute(serialPort,result,edata);
					return;
				}
				
				if (rsp.ret == CamabioUtils.ERR_FAIL) {
					
					int code = CamabioUtils.getDataIn2ByteInt(rsp.data);

					/////////// errores no fatales /////////////
					
					if (code == CamabioUtils.ERR_TIME_OUT) {
						try {
							result.onTimeout();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}
					
					if (code == CamabioUtils.ERR_BAD_CUALITY) {
						try {
							result.onBadQuality();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						continue;
					}
					
					/////////////// errores fatales ////////////////
					
					if (code == CamabioUtils.ERR_GENERALIZE) {
						try {
							result.onFailure(code);
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}
						return;
					}
					
					//////////////// cancel //////////////////
					
					if (code == CamabioUtils.ERR_FP_CANCEL) {
						try {
							result.onCancel();
						} catch (Exception e) {
							logger.log(Level.SEVERE,e.getMessage(),e);
						}

						if (canceled) {
							return;
						} else {
							canceled = true;
						}
						continue;
					}					
					
					throw new CmdException("Datos inesperados - rcm " + rsp.rcm + " ret " + rsp.ret + " code " + code);
				}
			}
			
			
		} catch (SerialException | ProcessingException e) {
			throw new CmdException(e);
		}
	}
}
