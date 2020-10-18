# IP Blocking Handler

A sample handler implementation to block API requests based on IP addresses (applied per API level and not global). The implementation presented in the `main` branch supports WSO2 API Manager v3.2.0.

> To use the handler implementation on lower versions of the API Manager, please change the required dependency versions in the `pom.xml` and build the project as instructed in [Build](#build).

### Build

Execute the following command from the root directory of the project to build

```sh
mvn clean package
```

### Usage

Copy the built JAR artifact and place it inside the `<gateway>/repository/components/lib` directory and start the server to load the required classes. 

After a successful server start, navigate to `<apim>/repository/deployment/server/syanpse-configs/default/api` directory and open the respective API synapse artifact and add the `IPBlockHandler` definition after the `CORSRequestHandler` to block the requests based on the specified IP Regex conditions.

> The IP addresses that are required to be blocked for this particular API needs to be passed in a Regex format

```xml
<handler class="com.sample.handlers.IPBlockHandler">
    <!-- IPRegex accepts only the Regex patterns -->
    <property name="IPRegex" value="^(127\.0\.0\.[1-5])$" />
</handler>
```

### License

[Apache 2.0 License](LICENSE)
