package edu.uoc.elc.spring.lti.tool;

import edu.uoc.elc.lti.tool.ToolDefinition;

/**
 * @author xaracil@uoc.edu
 */
public class ToolDefinitionFactory {
	public static ToolDefinition from(ToolDefinitionBean toolDefinitionBean) {
		return ToolDefinition.builder()
						.clientId(toolDefinitionBean.getClientId())
						.name(toolDefinitionBean.getName())
						.platform(toolDefinitionBean.getPlatform())
						.deploymentId(toolDefinitionBean.getDeploymentId())
						.keySetUrl(toolDefinitionBean.getKeySetUrl())
						.accessTokenUrl(toolDefinitionBean.getAccessTokenUrl())
						.oidcAuthUrl(toolDefinitionBean.getOidcAuthUrl())
						.privateKey(toolDefinitionBean.getPrivateKey())
						.publicKey(toolDefinitionBean.getPublicKey())
						.build();

	}
}
