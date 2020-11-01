# IP Blocking Handler

A sample handler implementation to block API requests based on IP addresses (applied per API level and not global). The implementation presented in the `main` branch supports WSO2 API Manager v3.2.0.

> To use the handler implementation on lower versions of the API Manager, please change the required dependency versions in the `pom.xml` and build the project as instructed in [Build](#build).

> **Mediator Implementation**
>
> Find the mediator implementation of the IP-Block under the [mediator](https://github.com/athiththan11/IP-Block-Handler/tree/mediator) branch with the instructions on engaging the mediator in `Global Mediation sequence` with `local-entries`

## Build

Execute the following command from the root directory of the project to build

```sh
mvn clean package
```

## Usage

Copy the built JAR artifact and place it inside the `<gateway>/repository/components/lib` directory and start the server to load the required classes. 

After a successful server start, navigate to `<apim>/repository/deployment/server/syanpse-configs/default/api` directory and open the respective API synapse artifact and add the `IPBlockHandler` definition after the `CORSRequestHandler` to block the requests based on the specified IP Regex conditions.

> The IP addresses that are required to be blocked for this particular API needs to be passed in a Regex format

```xml
<handler class="com.sample.handlers.IPBlockHandler">
    <!-- IPRegex accepts only the Regex patterns -->
    <property name="IPRegex" value="^(127\.0\.0\.[1-5])$" />
</handler>
```

### Configure Velocity Template

We can introduce an API Property to configure the IP Regex patterns from the Publisher UI to change the values dynamically. Please follow the given instructions to make the required changes in the API Manager server

> Please note the built JAR artifact has to be placed inside the `<apim>/repository/components/lib` directory prior to applying the following changes
>
> A complete `velocity_template.xml` can be found under [here](example/velocity_template.xml)

- Navigate and open the `<apim>/repository/resources/api_templates/velocity_template.xml` and add the following changes
  
    ```xml
    ...
    <handlers xmlns="http://ws.apache.org/ns/synapse">
    #foreach($handler in $handlers)

        #if($handler.className == 'org.wso2.carbon.apimgt.gateway.handlers.security.APIAuthenticationHandler')
            #if($apiObj.additionalProperties.get('IPRegex'))
                <handler class="com.sample.handlers.IPBlockHandler">
                    <property name="IPRegex" value="$apiObj.additionalProperties.get('IPRegex')" />
                </handler>
            #end
        #end

        <handler xmlns="http://ws.apache.org/ns/synapse" class="$handler.className">
    ...
    ```

- Save the `velocity_template.xml`
- Log-in to the Publisher portal and open the specific API that you want to perform IP blocking
- Go to Properties section and add the following property with the Regex pattern
  - Property Name: `IPRegex`
  - Property Value: `^(127\.0\.0\.[1-5])$`
- Click on `Add` and then click on `Save` to publish the API with the changes

## License

[Apache 2.0 License](LICENSE)
