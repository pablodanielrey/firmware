package ar.com.dcsys.firmware.soap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SoapDevice {

	private ZkSoftwareImpl zkSoftware = null;
	private String algorithm;
	private String codification;
	
	@Inject
	public SoapDevice(SoapDeviceData soapDeviceData) {
		try {
			algorithm = soapDeviceData.getAlgorithm();
			codification = soapDeviceData.getCodification();
			zkSoftware = new ZkSoftwareImpl(new URL("http://" + soapDeviceData.getIp() + soapDeviceData.getSoapUrl()));
			zkSoftware.setDebug(Boolean.parseBoolean(soapDeviceData.getDebug()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getCodification() {
		return codification;
	}

	public ZkSoftwareImpl getZkSoftware() {
		return zkSoftware;
	}

	public void setZkSoftware(ZkSoftwareImpl zkSoftware) {
		this.zkSoftware = zkSoftware;
	}
	
	
}
