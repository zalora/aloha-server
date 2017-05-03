# Aloha-Server

## Why do we need the Aloha-Server?

Due to problems running (scaling) [Aloha](https://github.com/zalora/aloha "Aloha") in our
production environment, we split the app in a server part, which is pretty much the vanilla
infinispan application with (optional) jgroups-jdbc and the [client](https://gitlab.com/wolframite/aloha-client "Aloha-Client")
part, which is a HotRod client with [JMemcached](https://github.com/zalora/jmemcached "JMemcached")
in front.

## Configuration

If you're not familiar with infinispan, have a look at [Aloha](https://github.com/zalora/aloha "Aloha"),
which might be more interesting for you and is also properly documented ;-)

### Docker

The docker container is configured by environment variables. In general all variables in the
application.yml can be overridden, but the following are important for daily use:

Choose from either JDBC based or AWS API discovery:

- `SPRING_PROFILES_ACTIVE=jdbc` to activate jgroups JDBC
    - `jgroups.jdbc.connection_url` to set the connection string, e.g. `jdbc:mysql://rds.example.com/dbname`
    - `jgroups.jdbc.connection_username` to set the database username
    - `jgroups.jdbc.connection_password` to set the database password

- `SPRING_PROFILES_ACTIVE=aws_ping` to activate jgroups AWS Ping
    - `jgroups.aws_ping.tags` ec2 instance tags
    - `jgroups.aws_ping.filters` AWS API filters
    - `jgroups.aws_ping.access_key` AWS Access Key
    - `jgroups.aws_ping.secret_key` AWS Secret Key

- `AWS=true` to make the docker start script assign the network interface IP address instead of `127.0.0.1`
- `infinispan.cluster.network.address` to set the Hot Rot listen address
- `infinispan.cluster.name` to change the name of the cluster, which comes in handy when you have more than one.

### Ports

The server exposes the caches via HotRod, which is listening on port `11222`. It's also running spring actuator 
and jolokia listening on the standard port `8080`. JGroups needs `7800` for node coordination.

### Loadbalancer

This setup doesn't need a load balancer, as the client is smart enough to know which server has which key. The
client however needs the IP of at least one server to start, but this should be easy to find out via the AWS API or Route53.
