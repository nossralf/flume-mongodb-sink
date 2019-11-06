[![Build Status](https://travis-ci.org/nossralf/flume-mongodb-sink.svg)](https://travis-ci.org/nossralf/flume-mongodb-sink)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=nossralf_flume-mongodb-sink&metric=alert_status)](https://sonarcloud.io/dashboard?id=nossralf_flume-mongodb-sink)
# flume-mongodb-sink
An Apache Flume sink that writes JSON to a MongoDB collection.

flume-mongodb-sink assumes the body of an event contains a valid UTF-8 encoded JSON object.

# Flume configuration properties

Here is an example excerpt from a Flume configuration file with a MongoDB sink configured for a Flume agent. Events will be written to the ``json`` collection in the ``flume`` database.

```
agent.sinks = mongo

agent.sinks.mongo.type = art.iculate.flume.MongoSink
agent.sinks.mongo.hostNames = localhost
agent.sinks.mongo.database = flume
agent.sinks.mongo.collection = json
agent.sinks.mongo.user = admin
agent.sinks.mongo.password = admin
```

## Hostnames

Hostnames is a comma-separated list of MongoDB server hostnames on the form ``hostname:port``. If the port number is omitted, the default port 27017 will be used.

## Examples

Three servers, the default port number 27017 will be used:
```
agent.sinks.mongo.hostNames = mongo1,mongo2,mongo3
```

Fully specified host names:
```
agent.sinks.mongo.hostNames = mongodb:27017,mongodb:27018,mongodb:27019
```
