package org.mule.extension.aws.secrets.provider.api;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mule.extension.aws.secrets.provider.api.api.AwsSecretsConfigurationPropertiesProvider;
import org.mule.extension.aws.secrets.provider.api.api.AwsSecretsConfigurationPropertiesProviderFactory;
import org.mule.runtime.config.api.dsl.model.properties.ConfigurationProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Base64;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;


public class AwsSecretsPropertyProviderMockCase {

    private final Logger LOGGER = LoggerFactory.getLogger(AwsSecretsConfigurationPropertiesProviderFactory.class);

    @Mock
    AWSSecretsManager sm;

    @Before
    public void setup() throws IOException, InterruptedException {
        MockitoAnnotations.initMocks(this);
    }


    /**
     * Test the basic happy path
     */
    @Test
    public void getSecretBasic() throws Exception {
        // Setup the mock
        GetSecretValueResult mockResult = new GetSecretValueResult();
        mockResult.setSecretString("mysecretresult");
        when(sm.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        AwsSecretsConfigurationPropertiesProvider provider = new AwsSecretsConfigurationPropertiesProvider(sm);

        // Retrieve the secret
        Optional<ConfigurationProperty> result = provider.getConfigurationProperty("aws-secret::secretName");

        // Validate the results
        assertThat(result.get().getRawValue().toString(), is("mysecretresult"));
        assertThat(result.get().getKey(), is("secretName"));
        assertThat(provider.wasFromCache, is(false));

        // Confirm that the subsequent results are from cache
        result = provider.getConfigurationProperty("aws-secret::secretName");
        assertThat(result.get().getRawValue().toString(), is("mysecretresult"));
        assertThat(provider.wasFromCache, is(true));
    }

  /**
   * Test the basic happy path binary
   */
  @Test
  public void getSecretBinary() throws Exception {
    // Setup the mock
    GetSecretValueResult mockResult = new GetSecretValueResult();
    CharsetEncoder encoder = Charset.defaultCharset().newEncoder();
    ByteBuffer buffer = encoder.encode(CharBuffer.wrap("mysecretresult".toCharArray()));
    mockResult.setSecretBinary(Base64.getEncoder().encode(buffer));
    when(sm.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
    AwsSecretsConfigurationPropertiesProvider provider = new AwsSecretsConfigurationPropertiesProvider(sm);

    // Retrieve the secret
    Optional<ConfigurationProperty> result = provider.getConfigurationProperty("aws-secret::secretName");

    // Validate the results
    assertThat(result.get().getRawValue().toString(), is("mysecretresult"));
    assertThat(result.get().getKey(), is("secretName"));
    assertThat(provider.wasFromCache, is(false));
  }

    /**
     * Test non existent secret
     */
    @Test
    public void getSecretBasicNotExists() throws Exception {
        // Setup the mock
        when(sm.getSecretValue(any(GetSecretValueRequest.class))).thenThrow(new ResourceNotFoundException("not exists"));
        AwsSecretsConfigurationPropertiesProvider provider = new AwsSecretsConfigurationPropertiesProvider(sm);

        // Retrieve the secret
        Optional<ConfigurationProperty> result = provider.getConfigurationProperty("aws-secret::secretName");

        // Validate the results
        assertThat(result.toString(), is("Optional.empty"));
    }

    /**
     * Test secrets in JSON format
     */
    @Test
    public void getSecretJson() throws Exception {
        // Setup the mock
        GetSecretValueResult mockResult = new GetSecretValueResult();
        mockResult.setSecretString("{ \"username\": \"admin\", \"password\": \"topsecret\" }");
        when(sm.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(mockResult);
        AwsSecretsConfigurationPropertiesProvider provider = new AwsSecretsConfigurationPropertiesProvider(sm);

        // Retrieve the secret
        Optional<ConfigurationProperty> result = provider.getConfigurationProperty("aws-secret::secretName#password");

        // Validate the results
        assertThat(result.get().getRawValue().toString(), is("topsecret"));
        assertThat(result.get().getKey(), is("secretName#password"));
    }
}
