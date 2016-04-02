#WSO2 Gateway 

This is a temporary repo for WSO2 Gateway work.


Building the Product
--------------------

Build the master branch of this repository.
Can be built only from JDK 1.8.


Running Samples
---------------

Sample Integration Flow configurations are available at samples/SequenceDiagramDSLSamples directory.

Configuration can be deployed to server by dropping the file to <CARBON_HOME>/deployment/integration-flows/ directory.


####Sample Configuration

```sh
@startuml

IntegrationFlow : Message_Router

participant inboundendpointListener : InboundEndpoint(protocol("http"),port(8080),context("/router"))

participant pipeline1 : Pipeline("router_flow")

participant outboundendpoint1 : OutboundEndpoint(protocol("http"),host("http://localhost:8280/backend1"))

participant outboundendpoint2 : OutboundEndpoint(protocol("http"),host("http://localhost:8280/backend2"))

inboundendpointListener -> pipeline1 : "request"

log("Before Filter")

alt condition(source("$header.routeId"),pattern("r1"))
    log("Filter True")
    pipeline1 -> outboundendpoint1 : "Request_to_Backend1"
    outboundendpoint1 -> pipeline1 : "Response_from_Backend1"

else
    log("Filter False")
    pipeline1 -> outboundendpoint2 : "Request_to_Backend2"
    outboundendpoint2 -> pipeline1 : "Response_from_Backend2"
end

log("After Filter")

pipeline1 -> inboundendpointListener : "Final_Response"

@enduml
```


Architecture
------------

<br/>
<b>High Level Architecture</b>
<br/>

![alt tag](docs/gw-architecture.png)



<br/><br/>
<b>Engine Architecture</b>
<br/>

![alt tag](docs/engine-architecture.png)



<br/><br/>
<b>Configuration Model</b>
<br/>

![alt tag](docs/config-model.png)

