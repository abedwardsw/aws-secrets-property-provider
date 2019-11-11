package org.mule.extension.aws.secrets.provider.api.api;

import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Export;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mule.runtime.api.component.ComponentIdentifier.builder;

@Xml(prefix = "aws-secrets-properties-provider")
@Extension(name = "AWS Secrets Properties Provider")
@Configurations(AwsSecretsPropertiesProviderConfiguration.class)
@Export(classes = AwsSecretsConfigurationPropertiesProviderFactory.class,
        resources = "META-INF/services/org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProviderFactory")
public class AwsSecretsPropertiesProviderExtension {
    public static final String EXTENSION_NAMESPACE = "aws-secrets-properties-provider";
    public static final ComponentIdentifier AWS_PROPERTIES_PROVIDER =
            builder().namespace(EXTENSION_NAMESPACE).name("config").build();

    private final static Logger LOGGER = LoggerFactory.getLogger(AwsSecretsPropertiesProviderExtension.class);
}
