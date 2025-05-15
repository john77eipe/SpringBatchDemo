package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;



@Configuration
public class AzureKeyVaultConfig {

	@Value("${azure.vaulturl}")
	private String vaultUrl;

	@Value("${azure.clientid}")
	private String clientId;

	@Value("${azure.tenantid}")
	private String tenantId;

	@Value("${azure.clientsecret}")
	private String clientSecret;

	@Bean(name = "keyVaultSecret")
	public SecretClient keyVaultSecret() {
		ClientSecretCredential credentials = new ClientSecretCredentialBuilder().clientId(clientId).tenantId(tenantId)
				.clientSecret(clientSecret).build();

		SecretClient secretClient = new SecretClientBuilder().vaultUrl(vaultUrl).credential(credentials).buildClient();
		return secretClient;
	}

}