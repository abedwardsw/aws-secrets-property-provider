package org.mule.extension.aws.secrets.provider.api.api;


import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationPropertiesProvider;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider to read AWS secrets
 */
public class AwsSecretsConfigurationPropertiesProvider implements ConfigurationPropertiesProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(AwsSecretsConfigurationPropertiesProvider.class);

    private final static String PROPERTIES_PREFIX = "aws-secret::";
    private final static Pattern KEY_PATTERN = Pattern.compile(PROPERTIES_PREFIX + "([^#]*)[#]?(.*)?");
    // used for junit tests only
    public boolean wasFromCache;
    public String lastKey;

    private Map<String, String> cachedData;
    private AWSSecretsManager awsClient;

    /**
     * Constructs a AwsSecretsConfigurationPropertiesProvider
     * @param awsClient
     */
    public AwsSecretsConfigurationPropertiesProvider(AWSSecretsManager awsClient) {
        LOGGER.info("provider initializing");
        cachedData = new HashMap<>();
        this.awsClient = awsClient;
    }

    @Override
    public String getDescription() {
        return "AWS Secrets properties provider";
    }

    /**
     * Lookup the AWS secret (Caching)
     *
     * @param secretName     the path to the secret
     * @param attribute if secret is JSON, pass optional attribute name
     * @return         the value of the property or null if the property is not found
     * @throws Exception
     */
    private String getProperty(String secretName, String attribute) throws Exception {
        String cachePath = secretName + "/" + attribute;

        if (!cachedData.containsKey(cachePath)) {
            wasFromCache = false;
            LOGGER.trace("Getting data from aws key: {}", cachePath);
            String secret = this.getAwsProperty(secretName, attribute);
            cachedData.put(cachePath, secret);
        } else {
            wasFromCache = true;
            LOGGER.trace("Getting data from Cache key: {}", cachePath);
        }

        return cachedData.get(cachePath);
    }
    /**
     * Lookup the AWS secret
     *
     * @param secretName     the path to the secret
     * @param attribute if secret is JSON, pass optional attribute name
     * @return         the value of the property or null if the property is not found
     */
    private String getAwsProperty(String secretName, String attribute) throws Exception {
        LOGGER.trace("getProperty path: {} attribute: {}", secretName, attribute);

        String secret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = null;

        try {
            getSecretValueResult = awsClient.getSecretValue(getSecretValueRequest);

            // String property
            if (getSecretValueResult.getSecretString() != null) {
                LOGGER.trace("String value was not null");
                secret = getSecretValueResult.getSecretString();

                // Retrieve JSON attribute
                if (attribute != null && !attribute.isEmpty()) {
                    LOGGER.trace("Parsing attribute: {}", attribute, secret);
                    final ObjectMapper objectMapper = new ObjectMapper();
                    final HashMap<String, String> secretMap = objectMapper.readValue(secret, HashMap.class);
                    secret = secretMap.get(attribute);
                }
                return secret;
            } else {
                // Binary property
                String decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
                return decodedBinarySecret;
            }
        } catch (ResourceNotFoundException e) {
            LOGGER.warn("The requested secret {} was not found", secretName);
            throw e;
        } catch (InvalidRequestException e) {
            LOGGER.warn("The request was invalid due to: {}", e.getMessage());
            throw e;
        } catch (InvalidParameterException e) {
            LOGGER.warn("The request had invalid params: {}", e.getMessage());
            throw e;
        } catch (DecryptionFailureException e) {
            // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
            // Deal with the exception here, and/or rethrow at your discretion.
            LOGGER.warn("Failed to decrypt secret", e);
            throw e;
        } catch (InternalServiceErrorException e) {
            // An error occurred on the server side.
            // Deal with the exception here, and/or rethrow at your discretion.
            LOGGER.warn("Internal service error", e);
            throw e;
        } catch (Exception e) {
            LOGGER.warn("Failure while looking up secret "+secretName, e);
            throw e;
        }

    }

    /**
     * Get a configuration property value from AWS.
     *
     * @param configurationAttributeKey  the key to lookup
     * @return                           the String value of the property
     */
    @Override
    public Optional<ConfigurationProperty> getConfigurationProperty(String configurationAttributeKey) {

        LOGGER.trace("Provider getConfigurationProperty: {}", configurationAttributeKey);
        if (configurationAttributeKey.startsWith(PROPERTIES_PREFIX)) {

            Matcher matcher = KEY_PATTERN.matcher(configurationAttributeKey);
            if (matcher.find()) {
                LOGGER.trace("Provider getConfigurationProperty: matched");
                final String effectiveKey = configurationAttributeKey.substring(PROPERTIES_PREFIX.length());

                // The Vault path is everything after the prefix and before the first period
                final String awsPath = matcher.group(1);

                // The secret key is everything after the first period
                final String jsonAttribute = matcher.group(2);

                try {
                    final String value = getProperty(awsPath, jsonAttribute);

                    if (value != null) {
                        return Optional.of(new ConfigurationProperty() {

                            @Override
                            public Object getSource() {
                                return "AWS Secrets provider source";
                            }

                            @Override
                            public Object getRawValue() {
                                return value;
                            }

                            @Override
                            public String getKey() {
                                return effectiveKey;
                            }
                        });
                    }
                } catch (Exception e) {
                    return Optional.empty();
                }

                return Optional.empty();

            }
        }
        return Optional.empty();
    }

}