package test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import zksoftware.soap.Base64;
import zksoftware.soap.UserInfo;
import zksoftware.soap.UserTemplate;
import zksoftware.soap.ZkSoftwareException;
import zksoftware.soap.ZkSoftwareImpl;




public class Test {
	public static void main(String[] args) {

		try {
/*		
			ZkSoftware zk1 = new ZkSoftware(new URL("http://163.10.56.203/iWsService"));
			zk1.setDebug(true);
			List<UserTemplate> templates = zk1.getAllUserTemplates();
	*/		
			
			//ZkSoftware zk = new ZkSoftware(new URL("http://163.10.56.8/iWsService"));

			ZkSoftwareImpl zkprivado = new ZkSoftwareImpl(new URL("http://192.168.0.240/iWsService"));
			zkprivado.setDebug(true);
//			zkprivado.deleteUsers();
//			zkprivado.refreshDB();
			
			/*
			 *	este es el procedimiento para cambiar cualquier propiedad del usuario.
			 *  la poronga del soap de zksoftware!!.
			 *  hay que :
			 *  1 - obtener todos los datos y huellas
			 *  2 - borrar el usuario
			 *  3 - agregar la info del usuario cambiada con setUserInfo
			 *  4 - agregar las huellas 			
			 */
			
			UserInfo i = zkprivado.getUserInfo("27294557").get(0);
			List<UserTemplate> temps = zkprivado.getUserTemplate("27294557");
			
			for (UserTemplate ut : temps) {
				int size = Integer.parseInt(ut.getSize());
				String template = ut.getTemplate();
				byte[] decoded = Base64.decode(template.getBytes());
				if (decoded.length != size) {
					System.out.println("Error en los tamaños");
				}
			}
			
			/*
			 * ejemplo de huella de pablo
			 * 
			 * template = TAVTUzIxAAAFRkYECAUHCc7QAAAdR2kBAAAAhes1B0cNAEAPOQDKAPdJUgAkAPUPOAAsRrsOZgA9ADgP+UZHALwOeQCNAH1JkgBOABAPJQBvRp0ONQBzACIPk0Z4ABoP5QC/AKJJcQB7AAYPPwCHRr8MBAGFAIcM8EaQAK8NdwBWABlJBAGWAMENIgClRqkPzACnAGAPKUarAFkPPQBpAORJZwCsACEPBwCqRi0PZAC2AIkP6ka7ALQOCwF4ADpIVADCAFEPKADGRj8OkADPAIQP70bQALsPCgEUAMdItQDZADcPxAHiRr0NCAHqAI0N6kbuALkOAgE4AMxKAAEIAUYN6QAIR1QPyAATAYYP00YVAToPrwDQAU5J/wAYAcINKAAgR7gNUAAtAZgPMEYxAdYP9wD0AUtL6gA2AcANwAEyR8YNPABIARkPekZIAVUO3wCIAc5IqABdAdQNTIuCwZKnqvyz92p/t74rBK99CQNIfx5lmIYqCJPsQAcmZhYWnX4Ra5Z1/N2v+wMUGfTD4Rep4pLXC6PjIxbPRCcPjQzp6sT7tKh77sflzH/UEFRcyYd9gKWVZPDYtrRtGRq+/H/SSFJ8gG0GufnI90hBQXvR//Ly6Hg6Tu52JYi+kG+WrZG40zoEOIuDhqex/S+Z/Qoy0XoQvIAPhX3Fj38AEEMIM85wDI5IhDtGTw3X9d8iSHyITAMEgYUNCsR+078bD0d+1HbwBQCwyY6Fgf2KfP3ktMBy/IeFgbAO+TmAhAYAYoGO+GY9QAi5+lKODPhkxcfyMQo2Ej4XgDokCQl37WzI+Ki/oIt+gcMIJP6gPWOBpJNFiBCUtTGECBX3SXj4C1NWWAqCgVuL+oDGvK57iQ+WfUMXRsaiCnOLfWTQXHYuAiBBAQKl4zAARjEAaW4EAKEAdAoGAEgBd8WGwQ5GfAF0wMH/BXxsQgAGASkpA8XqLXv+CQEGDzQ5Vsd+EADCEpOQVWb6h3HBCgBVIDX/Q3X+BADvITT6CAUJJ3DC/3vCywBSYPZX/zPANAQMBSw4+sD9/0b4/A9GYz50wYRzBAoFNj8DT0c1DMV1QDvDwYR7agnFfU1GMcA2DQB1iXrHhonAZI4QAFNQFQFN/v79wUQEFAWbU5x4d4hmBXdlUwHiTqKLdwfC+oRnwmLBwRHFN3WhR/7B+z7/gS8PRpB3jMPDfgd8A0aYeBMw/xTFc39F/yj///3AB//7h//Bw/3//MMBCzxC/8HE+xDFmHhcRUxAR8D/1gB/1A79/isvwDr8x7nA/UEPAHJWfcaCwYHB/1xU1wB/0RsuRjb+VAU0E0binal+wsCywsfNbsDBwMENxUCimDL9/P79//4FBEWrRocGAChuYMeGgBEAbK0eOCc7hj///1oHAOiuXyxaCgDEsiuRTGVMAWG3TMI2BDIdRgK92kxXwDo7+23+Nf9LDwCSvdO0+vsp/0vB8QMESMA6/gcAUQFXxIbATAkA8cTy/0C5w/4YAAfFGUrFAf/BI/5A/vI+CUZWxlBbwMAFwD1IAZDUPUBEOkDFuQoAuNswLzjBRbkNALPcPcAE/jkb/gYBE+dJrcImVgUJ0FVS/gcq+rv8//0xRMHwwcW4P/8EERUKjFkBVwUMRowFEO0PUob/wgUQLhGWwDpBEdcXOsD9BCMNVskYQDv//ToHFegYSVRCIRDBJts5wVhGwP38PCr6dkrA/zhMH9UOL5DBYltC/Pw8/Pi4M0BUP/8K1TkqnGt7wcQOAK7FP29F/kqGBRHWMEoSAhAxNFzAwRD8ckIvBBDyNZJz
			 * size = 1350
			 *
			 * <GetUserTemplateResponse>
					<Row><PIN>27294557</PIN><FingerID>0</FingerID><Size>1350</Size><Valid>1</Valid><Template>TAVTUzIxAAAFRkYECAUHCc7QAAAdR2kBAAAAhes1B0cNAEAPOQDKAPdJUgAkAPUPOAAsRrsOZgA9ADgP+UZHALwOeQCNAH1JkgBOABAPJQBvRp0ONQBzACIPk0Z4ABoP5QC/AKJJcQB7AAYPPwCHRr8MBAGFAIcM8EaQAK8NdwBWABlJBAGWAMENIgClRqkPzACnAGAPKUarAFkPPQBpAORJZwCsACEPBwCqRi0PZAC2AIkP6ka7ALQOCwF4ADpIVADCAFEPKADGRj8OkADPAIQP70bQALsPCgEUAMdItQDZADcPxAHiRr0NCAHqAI0N6kbuALkOAgE4AMxKAAEIAUYN6QAIR1QPyAATAYYP00YVAToPrwDQAU5J/wAYAcINKAAgR7gNUAAtAZgPMEYxAdYP9wD0AUtL6gA2AcANwAEyR8YNPABIARkPekZIAVUO3wCIAc5IqABdAdQNTIuCwZKnqvyz92p/t74rBK99CQNIfx5lmIYqCJPsQAcmZhYWnX4Ra5Z1/N2v+wMUGfTD4Rep4pLXC6PjIxbPRCcPjQzp6sT7tKh77sflzH/UEFRcyYd9gKWVZPDYtrRtGRq+/H/SSFJ8gG0GufnI90hBQXvR//Ly6Hg6Tu52JYi+kG+WrZG40zoEOIuDhqex/S+Z/Qoy0XoQvIAPhX3Fj38AEEMIM85wDI5IhDtGTw3X9d8iSHyITAMEgYUNCsR+078bD0d+1HbwBQCwyY6Fgf2KfP3ktMBy/IeFgbAO+TmAhAYAYoGO+GY9QAi5+lKODPhkxcfyMQo2Ej4XgDokCQl37WzI+Ki/oIt+gcMIJP6gPWOBpJNFiBCUtTGECBX3SXj4C1NWWAqCgVuL+oDGvK57iQ+WfUMXRsaiCnOLfWTQXHYuAiBBAQKl4zAARjEAaW4EAKEAdAoGAEgBd8WGwQ5GfAF0wMH/BXxsQgAGASkpA8XqLXv+CQEGDzQ5Vsd+EADCEpOQVWb6h3HBCgBVIDX/Q3X+BADvITT6CAUJJ3DC/3vCywBSYPZX/zPANAQMBSw4+sD9/0b4/A9GYz50wYRzBAoFNj8DT0c1DMV1QDvDwYR7agnFfU1GMcA2DQB1iXrHhonAZI4QAFNQFQFN/v79wUQEFAWbU5x4d4hmBXdlUwHiTqKLdwfC+oRnwmLBwRHFN3WhR/7B+z7/gS8PRpB3jMPDfgd8A0aYeBMw/xTFc39F/yj///3AB//7h//Bw/3//MMBCzxC/8HE+xDFmHhcRUxAR8D/1gB/1A79/isvwDr8x7nA/UEPAHJWfcaCwYHB/1xU1wB/0RsuRjb+VAU0E0binal+wsCywsfNbsDBwMENxUCimDL9/P79//4FBEWrRocGAChuYMeGgBEAbK0eOCc7hj///1oHAOiuXyxaCgDEsiuRTGVMAWG3TMI2BDIdRgK92kxXwDo7+23+Nf9LDwCSvdO0+vsp/0vB8QMESMA6/gcAUQFXxIbATAkA8cTy/0C5w/4YAAfFGUrFAf/BI/5A/vI+CUZWxlBbwMAFwD1IAZDUPUBEOkDFuQoAuNswLzjBRbkNALPcPcAE/jkb/gYBE+dJrcImVgUJ0FVS/gcq+rv8//0xRMHwwcW4P/8EERUKjFkBVwUMRowFEO0PUob/wgUQLhGWwDpBEdcXOsD9BCMNVskYQDv//ToHFegYSVRCIRDBJts5wVhGwP38PCr6dkrA/zhMH9UOL5DBYltC/Pw8/Pi4M0BUP/8K1TkqnGt7wcQOAK7FP29F/kqGBRHWMEoSAhAxNFzAwRD8ckIvBBDyNZIA</Template></Row>
					<Row><PIN>27294557</PIN><FingerID>1</FingerID><Size>1416</Size><Valid>1</Valid><Template>TMtTUzIxAAAFiIwECAUHCc7QAAAdiWkBAAAAhbU1B4kMADcPkQDSAPqGgAAZANsNtAAbiNcNZgAfAC0OPogwAGEPUgD8AOqHjgBCAAYPoABRiGQPfgBZAKANgohdAFkNmQCbABaGKgBkAN4PRgBuiN0NZwB3AC0PmIh4AH0O6gBGAKyFpgCFACcOPACDiMANlQCJAOAPsoiKACcP7wBRAKiEmwCZAEcNQgCfiGUOqQCsAOkOUIixAN4P/AB0ALiFzwCzADAPvACwiOsO8QC4AGoOj4jDAB4NPQAOANGH5wDRALEPSADQiD0ObgDiACIPgYjjAFYOhgA3AEiGWQD0ANAPPQD/iLcPAgH+AHsPeIgAAU8PRADBAVCH2gASATwPdQAQiUUPBgEXAQYO+ogbATsOCwHjAcSGBQE4AcUPmQA+iV8PoABHAZEP6YhGAcEPSwCMAdmH+QBbAc4PAjuWG457/ST6KAP7pFQYBB33KPNY/KdQlReRD/b46HYW8G6FIZJuCH+MzCfUo670JQIDeCOFKQ5RiaUCcfZ09CxJ8b3ZO2ybdvWC85t7cYSceZRDiwxWhXaG7Fhc0bxv4egN/c7s+CRoANEFSRjcFPuPkOAxqVUBLAD3JMvgreyJBcfw6Ky45REiHR08Qnf0+OYmBV7+xw2vfZJ+LBNyENsOUwnbBnaCsc2viBuFufchA1qBWOX4vavIivnXBAbupI93gqr2neg4IYt4mJL9G7GfwBxggKxy8fiJAAhrEPeU676C2Ps/9Af0UQv9/gmHzAT7cONpEYcTcu4KS4krisL7OgtO8d5yLYrxBHkG8Xpg9Ah/dQBhiNT9iI0kB3aMhYYWC/6b+hG3+y+XLPasd58DiYJji2KLWIaDCYMO+xrLQ41cByBMAQLn4gABiQYCLTcEAJgLYsEEAGYPXv0FwQCIjBtwVgwApyJoScJSc3AHAGwmhXeY/wMAMiqhwQCIMjNiwWEIxTsw6MD/wcNQBcVOOe92DgDKPJdXhlQcBgCSPgD+OMItjgHhZCInxtUAjst7wsH+nXSWgwaIkkQD/QMBxks4SgQA+Uw3wPQTBSFRiZDDf3C+wHaOAb5QHEf9zQCo2Q79RD4PAKRWbAPBcF//ixTF31MojcKGwcBuB3TFSg4Aglf6Ljr9UUjA/FAQAHqcbcf4wMB7wMH/jgoFFV0MKUtWEsV+WO/BxP3CwHCtwftJ/sQDAIRhm/0KiJNhHsJE/on/xXZqDwCcYhOEwDvcRQcAJ2hXBW/7mwGKb3TCwAeDb85liwsAkm/VQPp2wUQLAJp4RsPGDnX/BAENe4VfDYiigonDl3XVAK4MJlj9a0zAghIFEYca/P9owTjBQdlBBgCRiHRVwgmIqYkrwFdUBUcGiJmNIv0SAH2NIczBTMBE/sD9EgUGmVb8wv/CB3H7wk3AEACEnKFqekr/UP6CEQBYnTJJwv7//sH+oEQ8iwACpD3+BMQPr8TD/w8Ara3sOMVI/Gv//1MPxaiqpcD+/lRYVTsMBduyV8DAwkM6wXCdAXy08MbCF6x0SVr/XXAMAAq3KNlF/0YMAI0FCfx1Of/+wEkPxYXG+MSFZErAUMsAiE0dIzXA//87VgOJEclAwME6xgEGXjzCDQCH3kbJgkhSwDQQAHAa3Ph/+/3+/v/AO8D6SPzBDACA4p/BxNBEOAkAheaWwMXoRgoAwu06gVM8gwGR7j3//on/Q54BWe/QS/w7/Ply///A/sDABf/FdsD9CwCF9YxHxbY5CRB8BEw6/8VJ/jIGEEIGlsD60gcQ2RU9wPgGFVcUOlT/BxBqGEW2NQYRFSZG9/8BmFs/Wv//A9UFT6XAAxCcSlA7AxUqS0z+DBBZoN59/MHCVwsAlSw0L0jB//84</Template></Row>
					<Row><PIN>27294557</PIN><FingerID>2</FingerID><Size>1620</Size><Valid>1</Valid><Template>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</Template></Row>
				</GetUserTemplateResponse>
				
				otra la huella es la misma que la 0
				
				<GetUserTemplateResponse>
					<Row><PIN>27294557</PIN><FingerID>0</FingerID><Size>1350</Size><Valid>1</Valid><Template>TAVTUzIxAAAFRkYECAUHCc7QAAAdR2kBAAAAhes1B0cNAEAPOQDKAPdJUgAkAPUPOAAsRrsOZgA9ADgP+UZHALwOeQCNAH1JkgBOABAPJQBvRp0ONQBzACIPk0Z4ABoP5QC/AKJJcQB7AAYPPwCHRr8MBAGFAIcM8EaQAK8NdwBWABlJBAGWAMENIgClRqkPzACnAGAPKUarAFkPPQBpAORJZwCsACEPBwCqRi0PZAC2AIkP6ka7ALQOCwF4ADpIVADCAFEPKADGRj8OkADPAIQP70bQALsPCgEUAMdItQDZADcPxAHiRr0NCAHqAI0N6kbuALkOAgE4AMxKAAEIAUYN6QAIR1QPyAATAYYP00YVAToPrwDQAU5J/wAYAcINKAAgR7gNUAAtAZgPMEYxAdYP9wD0AUtL6gA2AcANwAEyR8YNPABIARkPekZIAVUO3wCIAc5IqABdAdQNTIuCwZKnqvyz92p/t74rBK99CQNIfx5lmIYqCJPsQAcmZhYWnX4Ra5Z1/N2v+wMUGfTD4Rep4pLXC6PjIxbPRCcPjQzp6sT7tKh77sflzH/UEFRcyYd9gKWVZPDYtrRtGRq+/H/SSFJ8gG0GufnI90hBQXvR//Ly6Hg6Tu52JYi+kG+WrZG40zoEOIuDhqex/S+Z/Qoy0XoQvIAPhX3Fj38AEEMIM85wDI5IhDtGTw3X9d8iSHyITAMEgYUNCsR+078bD0d+1HbwBQCwyY6Fgf2KfP3ktMBy/IeFgbAO+TmAhAYAYoGO+GY9QAi5+lKODPhkxcfyMQo2Ej4XgDokCQl37WzI+Ki/oIt+gcMIJP6gPWOBpJNFiBCUtTGECBX3SXj4C1NWWAqCgVuL+oDGvK57iQ+WfUMXRsaiCnOLfWTQXHYuAiBBAQKl4zAARjEAaW4EAKEAdAoGAEgBd8WGwQ5GfAF0wMH/BXxsQgAGASkpA8XqLXv+CQEGDzQ5Vsd+EADCEpOQVWb6h3HBCgBVIDX/Q3X+BADvITT6CAUJJ3DC/3vCywBSYPZX/zPANAQMBSw4+sD9/0b4/A9GYz50wYRzBAoFNj8DT0c1DMV1QDvDwYR7agnFfU1GMcA2DQB1iXrHhonAZI4QAFNQFQFN/v79wUQEFAWbU5x4d4hmBXdlUwHiTqKLdwfC+oRnwmLBwRHFN3WhR/7B+z7/gS8PRpB3jMPDfgd8A0aYeBMw/xTFc39F/yj///3AB//7h//Bw/3//MMBCzxC/8HE+xDFmHhcRUxAR8D/1gB/1A79/isvwDr8x7nA/UEPAHJWfcaCwYHB/1xU1wB/0RsuRjb+VAU0E0binal+wsCywsfNbsDBwMENxUCimDL9/P79//4FBEWrRocGAChuYMeGgBEAbK0eOCc7hj///1oHAOiuXyxaCgDEsiuRTGVMAWG3TMI2BDIdRgK92kxXwDo7+23+Nf9LDwCSvdO0+vsp/0vB8QMESMA6/gcAUQFXxIbATAkA8cTy/0C5w/4YAAfFGUrFAf/BI/5A/vI+CUZWxlBbwMAFwD1IAZDUPUBEOkDFuQoAuNswLzjBRbkNALPcPcAE/jkb/gYBE+dJrcImVgUJ0FVS/gcq+rv8//0xRMHwwcW4P/8EERUKjFkBVwUMRowFEO0PUob/wgUQLhGWwDpBEdcXOsD9BCMNVskYQDv//ToHFegYSVRCIRDBJts5wVhGwP38PCr6dkrA/zhMH9UOL5DBYltC/Pw8/Pi4M0BUP/8K1TkqnGt7wcQOAK7FP29F/kqGBRHWMEoSAhAxNFzAwRD8ckIvBBDyNZIA</Template></Row>
				</GetUserTemplateResponse>
			 * 
			 */
			
			/*
			zkprivado.deleteUserTemplate("27294557");
			
			UserTemplate ut = new UserTemplate();
			ut.setFingerId("0");
			ut.setPin("27294557");
			ut.setTemplate("TAVTUzIxAAAFRkYECAUHCc7QAAAdR2kBAAAAhes1B0cNAEAPOQDKAPdJUgAkAPUPOAAsRrsOZgA9ADgP+UZHALwOeQCNAH1JkgBOABAPJQBvRp0ONQBzACIPk0Z4ABoP5QC/AKJJcQB7AAYPPwCHRr8MBAGFAIcM8EaQAK8NdwBWABlJBAGWAMENIgClRqkPzACnAGAPKUarAFkPPQBpAORJZwCsACEPBwCqRi0PZAC2AIkP6ka7ALQOCwF4ADpIVADCAFEPKADGRj8OkADPAIQP70bQALsPCgEUAMdItQDZADcPxAHiRr0NCAHqAI0N6kbuALkOAgE4AMxKAAEIAUYN6QAIR1QPyAATAYYP00YVAToPrwDQAU5J/wAYAcINKAAgR7gNUAAtAZgPMEYxAdYP9wD0AUtL6gA2AcANwAEyR8YNPABIARkPekZIAVUO3wCIAc5IqABdAdQNTIuCwZKnqvyz92p/t74rBK99CQNIfx5lmIYqCJPsQAcmZhYWnX4Ra5Z1/N2v+wMUGfTD4Rep4pLXC6PjIxbPRCcPjQzp6sT7tKh77sflzH/UEFRcyYd9gKWVZPDYtrRtGRq+/H/SSFJ8gG0GufnI90hBQXvR//Ly6Hg6Tu52JYi+kG+WrZG40zoEOIuDhqex/S+Z/Qoy0XoQvIAPhX3Fj38AEEMIM85wDI5IhDtGTw3X9d8iSHyITAMEgYUNCsR+078bD0d+1HbwBQCwyY6Fgf2KfP3ktMBy/IeFgbAO+TmAhAYAYoGO+GY9QAi5+lKODPhkxcfyMQo2Ej4XgDokCQl37WzI+Ki/oIt+gcMIJP6gPWOBpJNFiBCUtTGECBX3SXj4C1NWWAqCgVuL+oDGvK57iQ+WfUMXRsaiCnOLfWTQXHYuAiBBAQKl4zAARjEAaW4EAKEAdAoGAEgBd8WGwQ5GfAF0wMH/BXxsQgAGASkpA8XqLXv+CQEGDzQ5Vsd+EADCEpOQVWb6h3HBCgBVIDX/Q3X+BADvITT6CAUJJ3DC/3vCywBSYPZX/zPANAQMBSw4+sD9/0b4/A9GYz50wYRzBAoFNj8DT0c1DMV1QDvDwYR7agnFfU1GMcA2DQB1iXrHhonAZI4QAFNQFQFN/v79wUQEFAWbU5x4d4hmBXdlUwHiTqKLdwfC+oRnwmLBwRHFN3WhR/7B+z7/gS8PRpB3jMPDfgd8A0aYeBMw/xTFc39F/yj///3AB//7h//Bw/3//MMBCzxC/8HE+xDFmHhcRUxAR8D/1gB/1A79/isvwDr8x7nA/UEPAHJWfcaCwYHB/1xU1wB/0RsuRjb+VAU0E0binal+wsCywsfNbsDBwMENxUCimDL9/P79//4FBEWrRocGAChuYMeGgBEAbK0eOCc7hj///1oHAOiuXyxaCgDEsiuRTGVMAWG3TMI2BDIdRgK92kxXwDo7+23+Nf9LDwCSvdO0+vsp/0vB8QMESMA6/gcAUQFXxIbATAkA8cTy/0C5w/4YAAfFGUrFAf/BI/5A/vI+CUZWxlBbwMAFwD1IAZDUPUBEOkDFuQoAuNswLzjBRbkNALPcPcAE/jkb/gYBE+dJrcImVgUJ0FVS/gcq+rv8//0xRMHwwcW4P/8EERUKjFkBVwUMRowFEO0PUob/wgUQLhGWwDpBEdcXOsD9BCMNVskYQDv//ToHFegYSVRCIRDBJts5wVhGwP38PCr6dkrA/zhMH9UOL5DBYltC/Pw8/Pi4M0BUP/8K1TkqnGt7wcQOAK7FP29F/kqGBRHWMEoSAhAxNFzAwRD8ckIvBBDyNZJz");
			ut.setSize("1350");
			ut.setValid("1");
			
			zkprivado.setUserTemplate(ut);

			ut.setFingerId("1");
			ut.setPin("27294557");
			ut.setTemplate("TMtTUzIxAAAFiIwECAUHCc7QAAAdiWkBAAAAhbU1B4kMADcPkQDSAPqGgAAZANsNtAAbiNcNZgAfAC0OPogwAGEPUgD8AOqHjgBCAAYPoABRiGQPfgBZAKANgohdAFkNmQCbABaGKgBkAN4PRgBuiN0NZwB3AC0PmIh4AH0O6gBGAKyFpgCFACcOPACDiMANlQCJAOAPsoiKACcP7wBRAKiEmwCZAEcNQgCfiGUOqQCsAOkOUIixAN4P/AB0ALiFzwCzADAPvACwiOsO8QC4AGoOj4jDAB4NPQAOANGH5wDRALEPSADQiD0ObgDiACIPgYjjAFYOhgA3AEiGWQD0ANAPPQD/iLcPAgH+AHsPeIgAAU8PRADBAVCH2gASATwPdQAQiUUPBgEXAQYO+ogbATsOCwHjAcSGBQE4AcUPmQA+iV8PoABHAZEP6YhGAcEPSwCMAdmH+QBbAc4PAjuWG457/ST6KAP7pFQYBB33KPNY/KdQlReRD/b46HYW8G6FIZJuCH+MzCfUo670JQIDeCOFKQ5RiaUCcfZ09CxJ8b3ZO2ybdvWC85t7cYSceZRDiwxWhXaG7Fhc0bxv4egN/c7s+CRoANEFSRjcFPuPkOAxqVUBLAD3JMvgreyJBcfw6Ky45REiHR08Qnf0+OYmBV7+xw2vfZJ+LBNyENsOUwnbBnaCsc2viBuFufchA1qBWOX4vavIivnXBAbupI93gqr2neg4IYt4mJL9G7GfwBxggKxy8fiJAAhrEPeU676C2Ps/9Af0UQv9/gmHzAT7cONpEYcTcu4KS4krisL7OgtO8d5yLYrxBHkG8Xpg9Ah/dQBhiNT9iI0kB3aMhYYWC/6b+hG3+y+XLPasd58DiYJji2KLWIaDCYMO+xrLQ41cByBMAQLn4gABiQYCLTcEAJgLYsEEAGYPXv0FwQCIjBtwVgwApyJoScJSc3AHAGwmhXeY/wMAMiqhwQCIMjNiwWEIxTsw6MD/wcNQBcVOOe92DgDKPJdXhlQcBgCSPgD+OMItjgHhZCInxtUAjst7wsH+nXSWgwaIkkQD/QMBxks4SgQA+Uw3wPQTBSFRiZDDf3C+wHaOAb5QHEf9zQCo2Q79RD4PAKRWbAPBcF//ixTF31MojcKGwcBuB3TFSg4Aglf6Ljr9UUjA/FAQAHqcbcf4wMB7wMH/jgoFFV0MKUtWEsV+WO/BxP3CwHCtwftJ/sQDAIRhm/0KiJNhHsJE/on/xXZqDwCcYhOEwDvcRQcAJ2hXBW/7mwGKb3TCwAeDb85liwsAkm/VQPp2wUQLAJp4RsPGDnX/BAENe4VfDYiigonDl3XVAK4MJlj9a0zAghIFEYca/P9owTjBQdlBBgCRiHRVwgmIqYkrwFdUBUcGiJmNIv0SAH2NIczBTMBE/sD9EgUGmVb8wv/CB3H7wk3AEACEnKFqekr/UP6CEQBYnTJJwv7//sH+oEQ8iwACpD3+BMQPr8TD/w8Ara3sOMVI/Gv//1MPxaiqpcD+/lRYVTsMBduyV8DAwkM6wXCdAXy08MbCF6x0SVr/XXAMAAq3KNlF/0YMAI0FCfx1Of/+wEkPxYXG+MSFZErAUMsAiE0dIzXA//87VgOJEclAwME6xgEGXjzCDQCH3kbJgkhSwDQQAHAa3Ph/+/3+/v/AO8D6SPzBDACA4p/BxNBEOAkAheaWwMXoRgoAwu06gVM8gwGR7j3//on/Q54BWe/QS/w7/Ply///A/sDABf/FdsD9CwCF9YxHxbY5CRB8BEw6/8VJ/jIGEEIGlsD60gcQ2RU9wPgGFVcUOlT/BxBqGEW2NQYRFSZG9/8BmFs/Wv//A9UFT6XAAxCcSlA7AxUqS0z+DBBZoN59/MHCVwsAlSw0L0jB//84");
			ut.setSize("1416");
			ut.setValid("1");

			zkprivado.setUserTemplate(ut);
			*/
			
			
			//			i.setPassword("123");
//			zkprivado.deleteUser("27294557");
//			zkprivado.setUserInfo(i);
//			zkprivado.refreshDB();
			/*
			zkprivado.deleteUserTemplate("27294557");
			for (UserTemplate t : temps) {
				zkprivado.setUserTemplate(t);
			}
			zkprivado.refreshDB();
			*/
			
			/*
			ZkSoftware zkecono = new ZkSoftware(new URL("http://127.0.0.1:8070/iWsService"));
			zkecono.setDebug(true);
			Date date = zkecono.getDate();
			System.out.println(date);
			
			UserInfo i = zkecono.getUserInfo("27294557").get(0);
			List<UserTemplate> temps = zkecono.getUserTemplate("27294557");
			i.setPassword("");
			zkecono.setUserInfo(i);
			for (UserTemplate t : temps) {
				zkecono.setUserTemplate(t);
			}
			zkecono.refreshDB();

			
/*			List<UserInfo> uinfo = zkecono.getAllUserInfo();
			List<UserTemplate> utemp = zkecono.getAllUserTemplates();
			List<AttLog> logs = zkecono.getAllAttLogs();
			*/

/*			for (UserInfo i : uinfo) {
				zkprivado.setUserInfo(i);
			}
			zkprivado.refreshDB();
			
			for (UserTemplate t : utemp) {
				zkprivado.setUserTemplate(t);
			}
			zkprivado.refreshDB();
			
			/*			
			zk.setDebug(true);
			List<UserInfo> users = zk.getAllUserInfo();
			
			zk.setDebug(false);
			List<UserTemplate> templates = zk.getAllUserTemplates();
			HashMap<UserInfo,List<UserTemplate>> map = new HashMap<UserInfo,List<UserTemplate>>();
			
			for (UserInfo u : users) {
				List<UserTemplate> temps = map.get(u);
				if (temps == null) {
					temps = new ArrayList<UserTemplate>();
					map.put(u, temps);
				}
			}
			
			for (UserTemplate t : templates) {
				String pin = t.getPin();
				boolean found = false;
				for (UserInfo u : users) {
					if (pin.equals(u.getPin2())) {
						List<UserTemplate> temps = map.get(u);
						temps.add(t);
						found = true;
						break;
					}
				}
				if (!found) {
					System.out.println("El usuario para el template (" + t.getPin() + "," + t.getFingerId() + ") no pudo ser encontrado");
				}
			}
			
			int count = 0;
			int aux = 0;
			for (UserInfo u : map.keySet()) {
				count = map.get(u).size();
				aux = aux + count;
				System.out.println("usuario : " + u.getPin2() + " tiene " + String.valueOf(count) + " huellas");
			}
			System.out.println("Total huellas : " + String.valueOf(aux) );
			
			zk.setDebug(true);
			zk.deleteUser("27294557");
			zk.refreshDB();
*/			
/*			List<UserInfo> info = zk.getUserInfo("27294557");
			info.get(0).setPassword("1234567");
			zk.setUserInfo(info.get(0));
			zk.refreshDB();
*/			
/*			
			System.out.println("eliminando la info de las huellas de : 27294557");
			if (zk.deleteUserTemplate("27294557")) {
				zk.refreshDB();
			}
			
	*/		
/*			for (UserTemplate t : templates) {
				System.out.println("Seteando el template del usuario :" + t.getPin());
				zk.setUserTemplate(t);
			}
			*/
			
//			List<AttLog> logs = zk.getAllAttLogs();
//			zk.deleteAttLogs();
			
			//zk.deleteAttLogs();

//			zk.deleteUser("2729457");
//			zk.refreshDB();
//			zk.deleteUser("27294557");
//			zk.refreshDB();
//			zk.deleteUser("27294557");
//			zk.refreshDB();
			
//			UserInfo info = zk.getUserInfo("27294557").get(0);
			
/*			UserInfo info = new UserInfo();
			info.setPin("1");
			info.setCardNumber("0");
			info.setPin2("27294557");
			info.setPassword("13795");
			info.setGroup("0");
			info.setName("");
			info.setPrivilege("6");
			zk.setUserInfo(info);
			zk.refreshDB();*/
			
/*			UserTemplate t = zk.getUserTemplate("31381082",1);
			t.setPin("27294557");
			t.setFingerId("1");
			zk.setUserTemplate(t);
	*/		
			
//			zk.getAllUserTemplates();
			
//			zk.getAllUserInfo();

//			zk.deleteUser("27294557");
	/*	
			for (AttLog l : logs) {
				System.out.println("-------------");
				System.out.println(l.getPin());
				System.out.println(l.getDateTime());
				System.out.println(l.getVerifiedMode());
				System.out.println(l.getStatus());
				System.out.println(l.getWorkCode());
			}
			*/
		
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ZkSoftwareException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
