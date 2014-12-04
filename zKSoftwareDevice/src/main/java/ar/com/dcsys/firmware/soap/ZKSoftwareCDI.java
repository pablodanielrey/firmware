package ar.com.dcsys.firmware.soap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ZKSoftwareCDI {

	private ZkSoftwareImpl zkSoftware = null;
	
	@Inject
	public ZKSoftwareCDI(ZKSoftwareCDIData zkSoftwareCDIData) {
		try {
			zkSoftware = new ZkSoftwareImpl(new URL("http://" + zkSoftwareCDIData.getIp() + zkSoftwareCDIData.getSoapUrl()));
			zkSoftware.setDebug(Boolean.parseBoolean(zkSoftwareCDIData.getDebug()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	public ZkSoftwareImpl getZkSoftware() {
		return zkSoftware;
	}

	public void setZkSoftware(ZkSoftwareImpl zkSoftware) {
		this.zkSoftware = zkSoftware;
	}
	
	
}
