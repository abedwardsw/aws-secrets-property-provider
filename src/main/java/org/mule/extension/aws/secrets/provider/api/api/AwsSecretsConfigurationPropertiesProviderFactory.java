package org.mule.extension.aws.secrets.provider.api.api;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.config.api.dsl.model.ConfigurationParameters;
import org.mule.runtime.config.api.dsl.model.ResourceProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AwsSecretsConfigurationPropertiesProviderFactory implements ConfigurationPropertiesProviderFactory {

    private final Logger LOGGER = LoggerFactory.getLogger(AwsSecretsConfigurationPropertiesProviderFactory.class);

    public AwsSecretsConfigurationPropertiesProviderFactory() {
        LOGGER.info("Provider factory instantiating");
    }

    @Override
    public ComponentIdentifier getSupportedComponentIdentifier() {
        return AwsSecretsPropertiesProviderExtension.AWS_PROPERTIES_PROVIDER;
    }

    @Override
    public ConfigurationPropertiesProvider createProvider(ConfigurationParameters parameters, ResourceProvider externalResourceProvider) {
        return new AwsSecretsConfigurationPropertiesProvider(createAwsClient(parameters));
    }

    public AWSSecretsManager createAwsClient(ConfigurationParameters parameters) {

        String secretKey = parameters.getStringParameter("secretKey");
        String accessKey = parameters.getStringParameter("accessKey");
        String region = parameters.getStringParameter("region");

        AWSSecretsManagerClientBuilder clientBuilder = AWSSecretsManagerClientBuilder.standard();
        clientBuilder.setRegion(region);
        clientBuilder.setCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(accessKey, secretKey)));
        return clientBuilder.build();
    }
}
