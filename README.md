[![Build Status](https://travis-ci.com/cirrocumulus-io/registry.svg?branch=master)](https://travis-ci.com/cirrocumulus-io/cirrocumulus)

# Cirrocumulus Registry
Provide a REST API to share image used by Cirrocumulus.

## How to build
```shell
./gradlew assemble
```

## How to test
:warning: **The database is reset before tests executing**

### Prerequisites
* docker
* docker-compose
* psql

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
./gradlew run
```

A first user is created with username `admin` and password `changeit`.

### Configuration
Configuration files are in `api/etc` directory:
- `database.yml` which is database configuration;
- `netty.yml` which is Netty configuration;
- `registry.yml` which is application configuration.
Default configuration can be found in `*-default.yml`. Note these files are just indicative.

By example, if you want customize database configuration, you can copy `database-default.yml` as `database.yml` and
override appropriate properties.

## How to contribute
Please open an issue to propose your feature!
