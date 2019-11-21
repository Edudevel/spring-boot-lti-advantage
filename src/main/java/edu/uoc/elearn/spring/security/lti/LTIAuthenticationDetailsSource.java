package edu.uoc.elearn.spring.security.lti;

import edu.uoc.elc.lti.tool.Tool;
import edu.uoc.elc.lti.tool.claims.JWSClaimAccessor;
import edu.uoc.elearn.spring.security.lti.openid.HttpSessionOIDCLaunchSession;
import edu.uoc.elearn.spring.security.lti.tool.ToolDefinition;
import edu.uoc.elearn.spring.security.lti.utils.RequestUtils;
import lombok.Getter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.Attributes2GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAttributes2GrantedAuthoritiesMapper;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;

/**
 * AuthenticationDetailsSource from LTI
 *
 * @author xaracil@uoc.edu
 */
public class LTIAuthenticationDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails> {
	private final Log logger = LogFactory.getLog(this.getClass());

	private final Attributes2GrantedAuthoritiesMapper ltiUserRoles2GrantedAuthoritiesMapper = new SimpleAttributes2GrantedAuthoritiesMapper();

	private final ToolDefinition toolDefinition;
	@Getter
	private Tool tool;

	public LTIAuthenticationDetailsSource() {
		this(null);
	}

	public LTIAuthenticationDetailsSource(ToolDefinition toolDefinition) {
		this.toolDefinition = toolDefinition;
	}

	protected Collection<String> getUserRoles(HttpServletRequest request) {
		ArrayList<String> ltiUserRolesList = new ArrayList<>();

		final HttpSessionOIDCLaunchSession oidcLaunchSession = new HttpSessionOIDCLaunchSession(request);
		final JWSClaimAccessor jwsClaimAccessor = new JWSClaimAccessor(toolDefinition.getKeySetUrl());

		this.tool = new Tool(toolDefinition.getName(),
						toolDefinition.getClientId(),
						toolDefinition.getPlatform(),
						toolDefinition.getKeySetUrl(),
						toolDefinition.getAccessTokenUrl(),
						toolDefinition.getOidcAuthUrl(),
						toolDefinition.getPrivateKey(),
						toolDefinition.getPublicKey(),
						jwsClaimAccessor,
						oidcLaunchSession);

		String token = RequestUtils.getToken(request);
		String state = request.getParameter("state");
		tool.validate(token, state);

		if (tool.isValid()) {
			ltiUserRolesList.add("USER");
		}

		if (tool.isLearner()) {
			ltiUserRolesList.add("LEARNER");
		}

		if (tool.isInstructor()) {
			ltiUserRolesList.add("INSTRUCTOR");
		}

		return ltiUserRolesList;
	}

	@Override
	public PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails buildDetails(HttpServletRequest httpServletRequest) {
		Collection<String> ltiUserRoles = this.getUserRoles(httpServletRequest);
		final Collection<? extends GrantedAuthority> userGas = this.ltiUserRoles2GrantedAuthoritiesMapper.getGrantedAuthorities(ltiUserRoles);
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("LTI roles [" + ltiUserRoles + "] mapped to Granted Authorities: [" + userGas + "]");
		}

		return new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(httpServletRequest, userGas);
	}
}
