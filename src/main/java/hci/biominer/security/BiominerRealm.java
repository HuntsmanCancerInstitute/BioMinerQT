package hci.biominer.security;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.SimpleByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;

import hci.biominer.service.UserService;
import hci.biominer.model.access.Permission;
import hci.biominer.model.access.Role;
import hci.biominer.model.access.User;


@Controller
public class BiominerRealm extends AuthorizingRealm {
	private final static int ITERATIONS = 1000;
	private final static int KEY_LENGTH = 196;
	
	@Autowired
	private UserService userService;
	
	
	public BiominerRealm() {
		setName("BiominerRealm");
		PBKDF2CredentialsMatcher pcm = new PBKDF2CredentialsMatcher(ITERATIONS,KEY_LENGTH);
		setCredentialsMatcher(pcm);
	}
	
	public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) {
		UsernamePasswordToken token = (UsernamePasswordToken)authcToken;
		
		User user = userService.getUserByUsername(token.getUsername());
			
		if (user != null) {
			System.out.println("CHecking up on you yo!");
			SimpleAuthenticationInfo sai = new SimpleAuthenticationInfo(user.getIdUser(),user.getPassword(),getName());
			sai.setCredentialsSalt(new SimpleByteSource(user.getSalt()));
			return sai;
		} else {
			System.out.println("User is null");
			return null;
		}	
	
	}
	
	public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Long idUser = (Long)principals.fromRealm(getName()).iterator().next();
		User user = userService.getUser(idUser);
		if (user != null) {
			SimpleAuthorizationInfo sai = new SimpleAuthorizationInfo();
			for (Role role: user.getRoles()) {
				sai.addRole(role.getName());
				
				List<String> stringPermissions = new ArrayList<String>();
				for (Permission p: role.getPermissions()) {
					stringPermissions.add(p.getPermission());
				}
				
				sai.addStringPermissions(stringPermissions);
			}
			return sai;
		} else {
			return null;
		}
	}
	
}
