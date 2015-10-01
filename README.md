This project implements a distributed group membership protocol using gossip 
style message passing.

How to compile:
$ cd distMembership
$ mvn package

This will create a fat jar in the target folder. Copy the jar to all the node. 
You can use the below command to copy the jar to all the nodes.
$ for NUM in `seq 1 1 7`; do scp distMemFinal.jar fa15-cs425-g01-0$NUM:~; done

The setup expects one of the nodes to be an introducer. The rest of the nodes are just participants. A config xml defines the intoducer node and port to connect. The xml also has a bunch of other configuraion parameters. The same config xml should be given to all the nodes in the system. 

To run the executable run the below command.
$ java -cp ~/distMemFinal.jar edu.uiuc.cs425.App <XML_PATH>
Arg 1: path to the configuration xml


