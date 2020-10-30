# IP Blocking Mediator

A sample mediator implementation to block API requests based on IP addresses per Application (applied per API level or globally). The implementation presented in the `main` branch supports WSO2 API Manager v3.1.0.

> To use the mediator implementation on lower versions of the API Manager, please change the required dependency versions in the `pom.xml` and build the project as instructed in [Build](#build).

## Build

Execute the following command from the root directory of the project to build

```sh
mvn clean package
```

## Usage

Copy the built JAR artifact and place it inside the `<gateway>/repository/components/lib` directory and start the server to load the required classes. 

After a successful server start, navigate to `<apim>/repository/deployment/server/syanpse-configs/default/local-entries` directory and add a local entry artifact specifying the Application name and the IP Regex pattern. Given below is a sample local-entry

> The IP addresses that are required to be blocked for this particular API needs to be passed in a Regex format. You find a sample local-entry in [./example/local-entries](./example/local-entries)
>
> Make sure to store the file with the name `ip-block`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<localEntry xmlns="http://ws.apache.org/ns/synapse" key="ip-block">
{"DefaultApplication": "^(127\\.0\\.0\\.[1-5])$"}
</localEntry>
```

Then add either a Global mediation sequence or a API specific mediation sequence as per your requirement and define the following class mediator.

```xml
<class class="com.sample.mediators.IPBlockMediator" />
```

## License

[Apache 2.0 License](LICENSE)
