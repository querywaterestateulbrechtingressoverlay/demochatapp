NODE_IPS=${NODE_IPS}

OLDIFS=$IFS
IFS=','

docker stack rm chatapp

for nodeip in $NODE_IPS
do
    ssh $nodeip "docker swarm leave -f"
    ssh $nodeip "docker container prune -f"
    ssh $nodeip "docker network prune -f"
    ssh $nodeip "docker image prune -f"
    ssh $nodeip "docker image rm 127.0.0.1:5000/chatapp"
    ssh $nodeip "docker volume prune -a -f"

done

docker container prune -f
docker swarm leave --force
docker network prune -f
docker image prune -a -f
docker volume prune -a -f

IFS=$OLDIFS
