NODE_IPS=${NODE_IPS}

OLDIFS=$IFS
IFS=','

docker stack rm chatapp

for nodeip in $NODE_IPS
do
    ssh $nodeip "docker container prune -a"
    ssh $nodeip "docker swarm leave"
done

docker swarm leave --force

IFS=$OLDIFS
