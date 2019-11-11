package org.mule.extension.aws.secrets.provider.api.api;

import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration(name="config")
public class AwsSecretsPropertiesProviderConfiguration {
    private final static Logger LOGGER = LoggerFactory.getLogger(AwsSecretsPropertiesProviderConfiguration.class);
    @DisplayName("Secret Key")
    @Parameter
    private String secretKey;
    @DisplayName("Access Key")
    @Parameter
    private String accessKey;
    @DisplayName("Region")
    @Parameter
    private String region;
}
