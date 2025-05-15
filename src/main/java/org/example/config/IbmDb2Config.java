package org.example.config;

import javax.sql.DataSource;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Configuration
public class IbmDb2Config {


    @Bean
    @Primary
    public DataSource readCustomerMasterDataDb(
            @Qualifier("keyVaultSecret") SecretClient secretClient) {

        String driverClassName = secretClient.getSecret("db2Driver-class-name").getValue();
        String password = secretClient.getSecret("db2Password").getValue();
        String username = secretClient.getSecret("db2Username").getValue();
        String ibmDbUrl = secretClient.getSecret("db2URL").getValue();


        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .username(username)
                .password(password)
                .url(ibmDbUrl)
                .build();
    }
}