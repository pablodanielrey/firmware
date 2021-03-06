package ar.com.dcsys.firmware.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;


public class FirmwareUsersRealm implements Realm {

	@Override
	public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		return null;
	}

	@Override
	public String getName() {
		return FirmwareUsersRealm.class.getName();
	}

	@Override
	public boolean supports(AuthenticationToken token) {
		return UsernamePasswordToken.class.isAssignableFrom(token.getClass());
	}

}
