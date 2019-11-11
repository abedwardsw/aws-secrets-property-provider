Aws-secrets-property-provider Extension
=======================================

This extension provides a way to use AWS Secrets in your MULE configuration properties

On startup, this extension will resolve any properties that start 
with "aws-secrets::" from AWS secrets manager using the [AWS Secrets API](https://docs.aws.amazon.com/secretsmanager/latest/apireference/Welcome.html) / aws-java-sdk-secretsmanager

## Usage

### maven
Add this dependency to your application pom.xml

```
<groupId>org.mule.extensions</groupId>
<artifactId>aws-secrets-property-provider</artifactId>
<version>1.0.0</version>
<classifier>mule-plugin</classifier>
```

### examples
#### property
Example using the lookup in a property file
```
my-vault-property=${aws-secret::secret-key} 
```
### flow
See the following file for an example using in a flow [test-mule-config.xml](src/test/resources/test-mule-config.xml)
