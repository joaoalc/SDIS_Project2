#! /bin/bash

# Script for running a peer
# To be run in the root of the build tree
# No jar files used
# Assumes that Peer is the main class
#  and that it belongs to the peer package
# Modify as appropriate, so that it can be run
#  from the root of the compiled tree

# Check number input arguments
argc=$#

echo $argc

if (( argc != 4 && argc != 6 ))
then
	echo "Usage: $0 <peer_id> <svc_access_point> <peer_address> <peer_port> [<known_peer_address> <known_peer_port>]"
	exit 1
fi

# Assign input arguments to nicely named variables

id=$1
sap=$2
p_address=$3
p_port=$4
if (( argc == 6 ))
then
  kp_address=$5
  kp_port=$6
fi

# Execute the program
# Should not need to change anything but the class and its package, unless you use any jar file

# echo "java peer.Peer ${ver} ${id} ${sap} ${mc_addr} ${mc_port} ${mdb_addr} ${mdb_port} ${mdr_addr} ${mdr_port}"

if (( argc == 4 ))
then
  java -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore -Djavax.net.ssl.trustStorePassword=123456 peers.Peer ${id} ${sap} ${p_address} ${p_port}
else
    java -Djavax.net.ssl.keyStore=keys/client.keys -Djavax.net.ssl.keyStorePassword=123456 -Djavax.net.ssl.trustStore=keys/truststore -Djavax.net.ssl.trustStorePassword=123456 peers.Peer ${id} ${sap} ${p_address} ${p_port} ${kp_address} ${kp_port}
fi

