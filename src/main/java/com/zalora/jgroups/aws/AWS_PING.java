package com.zalora.jgroups.aws;

import com.amazonaws.auth.*;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.jgroups.Address;
import org.jgroups.*;
import org.jgroups.annotations.Property;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.*;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.NameCache;
import org.jgroups.util.Responses;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Ported AWS_PING to JGroups 4
 *
 * @link https://github.com/meltmedia/jgroups-aws
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 */
public class AWS_PING extends Discovery {

    static {
        ClassConfigurator.addProtocol((short) 666, AWS_PING.class);
    }

    @Property(description = "The tag key to look for")
    protected String key;

    @Property(description = "The tag value to look for")
    protected String value;

    @Property(description = "AWS region, defaults to Singapore")
    protected String region = "ap-southeast-1";

    @Property(description = "The port number being used for cluster membership. The default is 7800.")
    protected int port = 7800;

    /**
     * The EC2 client used to look up cluster members.
     */
    private AmazonEC2 ec2;

    /**
     * Describe request contains the filters
     * All programmatic, not this string parsing shit show
     */
    private DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();

    /**
     * Scans the environment for information about the AWS node that we are
     * currently running on and parses the filters and tags.
     */
    public void init() throws Exception {
        super.init();

        List<Filter> filters = new ArrayList<>();
        filters.add(new Filter(String.format("tag:%s", key), new ArrayList<String>() {{
            add(value);
        }}));

        describeInstancesRequest.setFilters(filters);

        // Try to get credentials from ENV and via an instance role
        ec2 = AmazonEC2ClientBuilder.standard().withCredentials(
            new AWSCredentialsProviderChain(
                new InstanceProfileCredentialsProvider(true),
                new EnvironmentVariableCredentialsProvider()
            )
        ).withRegion(region).build();
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    /**
     * Returns the unique name for this protocol AWS_PING.
     */
    @Override
    public String getName() {
        return "AWS_PING";
    }

    /**
     * Fetches all of the cluster members found on EC2. The host portion of the
     * addresses are the private ip addresses of the matching nodes. The port
     * numbers of the addresses are set to the port number
     *
     * @param members
     * @param initial_discovery
     * @param responses
     */
    @Override
    protected void findMembers(List<Address> members, boolean initial_discovery, Responses responses) {
        PhysicalAddress physical_addr = (PhysicalAddress) down(new Event(Event.GET_PHYSICAL_ADDRESS, local_addr));

        PingData data = new PingData(local_addr, false, NameCache.get(local_addr), physical_addr);
        PingHeader hdr = new PingHeader(PingHeader.GET_MBRS_REQ).clusterName(cluster_name);

        List<PhysicalAddress> clusterMembers = expandClusterMemberPorts(getPrivateIpAddresses());

        for (final PhysicalAddress addr : clusterMembers) {
            if (physical_addr != null && addr.equals(physical_addr)) {
                continue;
            }

            final Message msg = new Message(addr).setFlag(
                Message.Flag.INTERNAL, Message.Flag.DONT_BUNDLE, Message.Flag.OOB
            ).putHeader(this.id, hdr).setBuffer(marshal(data));

            if (async_discovery_use_separate_thread_per_request) {
                timer.execute(() -> sendDiscoveryRequest(msg), sends_can_block);
            } else {
                sendDiscoveryRequest(msg);
            }
        }
    }

    private List<PhysicalAddress> expandClusterMemberPorts(List<String> privateIpAddresses) {
        List<PhysicalAddress> clusterMembers = new ArrayList<>();

        for (String privateIpAddress : privateIpAddresses) {
            try {
                clusterMembers.add(new IpAddress(privateIpAddress, port));
            } catch (UnknownHostException e) {
                log.warn("Could not create an IpAddress for " + privateIpAddress + ":" + port);
            }
        }

        return clusterMembers;
    }

    /**
     * Gets the list of private IP addresses found in AWS based on the filters and
     * tag names defined.
     *
     * @return the list of private IP addresses found on AWS.
     */
    private List<String> getPrivateIpAddresses() {
        List<String> ipAddresses = new ArrayList<>();

        DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);
        for (Reservation reservation : describeInstancesResult.getReservations()) {
            for (Instance instance : reservation.getInstances()) {
                try {
                    ipAddresses.add(instance.getPrivateIpAddress());
                } catch (Exception ex) {
                    log.error("Failed to parse IP address", ex);
                }
            }
        }

        return ipAddresses;
    }

    private void sendDiscoveryRequest(Message req) {
        try {
            log.trace("%s: sending discovery request to %s", local_addr, req.getDest());
            down_prot.down(req);
        } catch (Throwable t) {
            log.trace("sending discovery request to %s failed: %s", req.dest(), t);
        }
    }

}
