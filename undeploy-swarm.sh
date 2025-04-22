NODE_IPS=${NODE_IPS}

OLDIFS=$IFS
IFS=','

docker stack rm chatapp

for nodeip in $NODE_IPS
do
    ssh $nodeip "docker container prune"
    ssh $nodeip "docker swarm leave"
    ssh $nodeip "docker network prune"
done

docker container prune
docker swarm leave --force
docker network prune

IFS=$OLDIFS
