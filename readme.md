# Avocado

iTunes podcast cacher and management API

The goal of this project was to create a podcast cacher and management system API.

Licensed under the GPLv2.
 
## Building

**IDE: The project must be built with gradle first for the constants class to be generated and available to the IDE.**

```
./gradlew jar
```

The output will be at: `./server/build/libs/avocado-*.jar` and `./cacher/build/libs/cacher-*.jar`.

## Running

Avocado expects to connect to a Redis server, RabbitMQ server, and PostgreSQL server. The connections URLs can be 
 specified through environment variables.
 
The SQL database schema has been dumped to `avocado.sql`.

The API endpoints have been documented at `avocado_api.pdf`

### Main

The main app can be launched without any arguments. This app does not handle requests. It checks the Redis cache and
 forwards a request to the API cacher. If a request has not been cached an empty 218 response will be sent. This 
 indicates that the cacher has not cached the request yet.

### Cacher

The cacher can operate in 3 different modes.

The first is the API cacher. In this mode all requests made to the front end will be handled and cached to Redis. The
 frontend then serves the Redis cache.
 
`--podcast`

Launching with the --podcast argument will cause the cacher to only cache podcast searches. The query will be forwarded
 to iTunes, parsed and any missing podcasts will be added to the database. New podcasts then have their episodes
 updated in the database.
 
`--update`

Launching with the --update argument will cause the cacher to check the `last_update` timestamp on all podcasts and 
 request the podcast cacher to update the episode and metadata of podcasts that have not been updated for one day or
 more. This process does not run continuously and is meant to be executed as a cron job.
 
## Environment Variables

### LOG_LEVEL

Values: SEVERE WARNING INFO CONFIG FINE FINER FINEST

Set the log level

The allowed values are the same as java.util.logging levels.

### NO_AUTH_CLIENT_ID

Values: TRUE FALSE

If true no client ids will be tested against allowed ids

### PORT

Integer

Default: 5000

Specifies the port the server will run on.

### GRADLE_TASK

Heroku specific variable that defines what task should be used to build
 with Gradle

This should be set to "jar".
 
### REDIS_URL

Redis server URL

This should be in the following format:
 `redis://<username>:<password>@<server>:<port>`
 
### REDIS_URL_ENV

Optional

Changes the environment variable name to use instead of REDIS_URL.

For example, if set to EXT_REDIS_URL, the Redis URL will be pulled from EXT_REDIS_URL instead of the default REDIS_URL.
 
### REDIS_CONNECTIONS

Amount of redis connections allowed

### SQL_URL

SQL server URL

This should be in the following format:
 `postgres://<username>:<password>@<server>:<port>/<database>`

### SQL_URL_ENV

Optional

Changes the environment variable name to use instead of SQL_URL.

For example, if set to EXT_SQL_URL, the SQL URL will be pulled from EXT_SQL_URL instead of the default SQL_URL.

### SQL_SSL

_Optional_

Values: TRUE FALSE

Default: TRUE

If set to true, SSL connections will required on the database connection.

### SQL_SCHEMA

_Optional_

Default: avocado

Main database scheme name.

### ALLOWED_CLIENT_ID

String of client IDs separated by commas

Client IDs that are allowed to connect to the API

### REDIRECT_HTTP

Values: TRUE FALSE

If set to true all http traffic will be redirected to the HTTPS variant of the URL

### MQ_URL_ENV

Optional

Changes the environment variable name to use instead of MQ_URL.

For example, if set to EXT_MQ_URL, the SQL URL will be pulled from EXT_SQL_URL instead of the default MQ_URL.

### MQ_URL

Message queue server URL

This should be in the following format:
 `amqp://<username>:<password>@<server>:<port>/<host>`
 
### MQ_SECURE

Values: TRUE FALSE

Should the MQ connection use TLS

### MQ_TRUST

Literal `\n` characters will be replaced with new lines.

MQ TLS certificate

### REDIS_SECURE

If set to TRUE, the Redis connection will be made over TLS

### REDIS_TRUST

Literal `\n` characters will be replaced with new lines.

Redis TLS certificate