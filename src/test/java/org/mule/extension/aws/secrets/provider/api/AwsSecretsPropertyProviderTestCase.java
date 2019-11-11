/***
 * Was not able to find a good way to mock the Provider when loaded through the mule runtime, for now can use this
 * test to run manually by updating the credentials
 *
 * This test case is ignored, but may be useful to test in your environment
 */
package org.mule.extension.aws.secrets.provider.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.mule.extension.aws.secrets.provider.api.api.AwsSecretsConfigurationPropertiesProviderFactory;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

@Ignore
public class AwsSecretsPropertyProviderTestCase extends MuleArtifactFunctionalTestCase {

  private final Logger LOGGER = LoggerFactory.getLogger(AwsSecretsConfigurationPropertiesProviderFactory.class);
  /**
   * Specifies the mule config xml with the flows that are going to be executed in the tests, this file lives in the test resources.
   */
  @Override
  protected String getConfigFile() {
    return "test-mule-config.xml";
  }

  @BeforeClass
  public static void setupContainer() throws IOException, InterruptedException {
// Update with your values
//    System.setProperty("aws.region", "");
//    System.setProperty("aws.secretKey", "");
//    System.setProperty("aws.accessKey", "");
  }

  @Test
  public void testStandardSecret() throws Exception {
    String payloadValue = ((String) flowRunner("standardSecretFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue, is("mysecret"));
  }

  @Test
  public void testJsonSecret() throws Exception {
    String payloadValue = ((String) flowRunner("jsonSecretFlow").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue, is("{\"username\":\"myusername\",\"password\":\"mypassword\"}"));

  }
  @Test
  public void testJsonSecretUsername() throws Exception {
    String payloadValue = ((String) flowRunner("jsonSecretFlowUsername").run()
            .getMessage()
            .getPayload()
            .getValue());
    assertThat(payloadValue, is("myusername/mypassword"));
  }

}
