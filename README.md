# Aloha-Server

## Why do we need the Aloha-Server?

Due to problems running (scaling) [Aloha](https://github.com/zalora/aloha "Aloha") in our
production environment, we split the app in a server part, which is pretty much the vanilla
infinispan application with (optional) jgroups-jdbc and the [client](https://gitlab.com/wolframite/aloha-client "Aloha-Client")
part, which is a HotRod client with [JMemcached](https://github.com/zalora/jmemcached "JMemcached")
in front.

## Configuration

Configuration takes place in three files:

- application.yml
- hibernate.properties (If you want to use Read-Through)
- jgroups-aws.xml (If you are using AWS API based discovery)

You can change the variables in the configuration files via command line parameters, e.g.

`$ java -Dinfinispan.cluster.name=Kohala -jar aloha-server.jar`

This example changes the cluster name to Kohala

### application.yml

To run Aloha-Server in production, those values might be interesting for you to change:

- **infinispan.cluster.name**: Change the cluster name if you have more than one
- **infinispan.cluster.jgroups.config**: The jgroups file used for autodiscovery (jgroups-aws.xml|/default-configs/default-jgroups-(tcp|udp).xml)
- **infinispan.cluster.network.address**: IP address the Hot Rod server will listen (127.0.0.1 by default)

### jgroups-aws.xml

AWS Discovery uses the IAM Role permissions to retrieve the private IP address from each host which matches the tag
key-value combination.

The policy of the IAM role has to allow all describe actions:

```
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "ec2:Describe*",
            "Effect": "Allow",
            "Resource": "*"
        }
    ]
}
```

If you prefer to provide Access- and Secret Key instead, make sure to export the following environment variables:
- AWS_ACCESS_KEY_ID
- AWS_SECRET_KEY

To configure the key and value of the tag, use the following variables:

- jgroups.tag.key
- jgroups.tag.value

### Ports

The server exposes the caches via HotRod, which is listening on port `11222`. It's also running spring actuator 
and jolokia listening on the standard port `8080`. JGroups needs `7800` for node coordination.

### Loadbalancer

This setup doesn't need a load balancer, as the client is smart enough to know which server has which key.
The client however needs the IP of at least one server to start, but this should be easy to find out via the
AWS API or Route53.
