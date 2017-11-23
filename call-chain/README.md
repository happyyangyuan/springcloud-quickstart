This example demonstrates the following call chaining:
  
  zipkin-client -->  zipkin-client0  -->  zipkin-client1
  
We use feign with service discovery to route requests to the destiny nodes. 
To run these zipkin-clients, the discovery eureka-server must be running.