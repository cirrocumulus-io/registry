[![Build Status](https://travis-ci.com/cirrocumulus-io/registry.svg?branch=master)](https://travis-ci.com/cirrocumulus-io/cirrocumulus)

# Cirrocumulus Registry
Provide a REST API to share image used by Cirrocumulus.

## How to build
```shell
./gradlew assemble
```

## How to test
```shell
docker-compose up -d
./gradlew check
```

## How to execute
### Prerequisites
* docker
* docker-compose

```shell
docker-compose up -d
./gradlew update run
```

A first user is created with username `admin` and password `changeit`.

## How to contribute
Please open an issue to propose your feature!
