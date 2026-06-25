# Docs
Please see the Github Pages Site for complete documentation: [quarkusdroneshop.github.io](https://quarkusdroneshop.github.io)

# About 

This repos contains the Quarkus Coffeeshop Counter Microservice.  The Counter microservice coordinates events in the system.  It receives orders from the Web microservice from a Kakfa topic, records the orders in a database, sends messages to the QDCA10 and QDCA10Pro microservices, listens for updates from the QDCA10 and QDCA10Pro microservices, and updates the Web microservice.

This project uses Quarkus, the Supersonic Subatomic Java Framework.  If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Requirements

*Java* 
This one should be obvious.  We like [AdoptOpenJDK](https://adoptopenjdk.net/)  

BTW, if you want to manage multiple JDK's [SDKMan](https://sdkman.io/) is a great tool 

*Docker and Docker Compose*
You can install PostgreSQL and Kafka locally, but we have a docker-compose.yaml file that will do everything for you

## Working Locally
Grab the supporting files

### Supporting infrastructure

Clone the quarkusdroneshop-support project (in another directory)

```shell
git clone https://github.com/quarkusdroneshop/quarkusdroneshop-support.git
```

From inside the quarkusdroneshop-support project (on MacOS or Linux) run:

```shell
docker-compose up
```

This will start PostgreSQL, PGAdmin, Kafka and Zookeeper

### Counter microservice

From the quarkusdroneshop-counter directory you can start the counter microservice with:

```shell
./mvnw clean compile quarkus:dev
```

### Kafka Consumers and Producers

If you want to monitor the Kafka topics and have Kafka's command line tools installed you can watch the topics with:

```shell script
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic web-updates --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic qdca10-in --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic qdca10pro-in --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 --topic orders-up --from-beginning
```

Orders can be sent directly to the topics with:

```shell script
kafka-console-producer --broker-list localhost:9092 --topic <<TOPIC_NAME>>
```

## Packaging and publishing the application to a repository

```shell
./mvnw clean package -Pnative -Dquarkus.native.container-build=true
```

```shell
docker build -f src/main/docker/Dockerfile.native -t <<DOCKER_HUB_ID>>/quarkusdroneshop-counter .
```
```shell
export KAFKA_BOOTSTRAP_URLS=localhost:9092 \
PGSQL_URL="jdbc:postgresql://localhost:5432/droneshopdb?currentSchema=droneshop" \
PGSQL_USER="droneshopuser" \
PGSQL_PASS="redhat-21"
```

```shell
docker run -i --network="host" -e PGSQL_URL=${PGSQL_URL} -e PGSQL_USER=${PGSQL_USER} -e PGSQL_PASS=${PGSQL_PASS} e KAFKA_BOOTSTRAP_URLS=${KAFKA_BOOTSTRAP_URLS} <<DOCKER_HUB_ID>>/quarkusdroneshop-counter:latest
```

```shell
docker images -a | grep counter
docker tag <<RESULT>> <<DOCKER_HUB_ID>>/quarkusdroneshop-counter:<<VERSION>>
```
