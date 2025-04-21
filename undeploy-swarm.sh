NODE_IPS=${NODE_IPS}

OLDIFS=$IFS
IFS=','

for nodeip in $NODE_IPS
do
    ssh $nodeip "docker swarm leave"
done

docker swarm leave --force

IFS=$OLDIFS
