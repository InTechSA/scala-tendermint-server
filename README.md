# tendermint-server

Scala implementation of Tendermint TSP ABCI Server (see [Tendermint documentation](https://tendermint.com/docs/guides/app-development#tsp)) using Akka Stream.


## Build & run the example

Run the main class `lu.intech.tendermint.Main` which starts a TSP server:

```
sbt run
```

In parallel, run **abci-cli**:

```
abci-cli info
```

or **tendermint**:

```
tendermint node
```

This basic implementation logs messages to stdout.


## Integration in your application

The purpose of this project is to integrate tendermint in your Scala application. 

Add the dependency to your `build.sbt`:


```
libraryDependencies += "lu.intech" %% "tendermint-server" % "1.0.0")
```
 
And create an instance of the `Server` class, and set your implementations to handle TSP messages: 


```
import lu.intech.tendermint.Server
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}


implicit val system: ActorSystem = ActorSystem()
implicit val materializer: Materializer = ActorMaterializer()
import scala.concurrent.ExecutionContext.Implicits.global

val server = new Server(
	consensusHandler = new lu.intech.tendermint.ConsensusHandler { /* ... */ } ,
	mempoolHandler   = new lu.intech.tendermint.MempoolHandler { /* ... */ } ,
	queryHandler     = new lu.intech.tendermint.QueryHandler { /* ... */ } ,

	/* default values      */
	/* host = "127.0.0.1" ,*/
	/* port = 46658        */
)

server.start()
```

## Logging

`tendermint-server` uses **slf4j** as logging api and **logback** binding by default.  
If you want to configure loggers, just provide a `logback.xml` file.
