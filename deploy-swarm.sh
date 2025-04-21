MANAGER_IP=${MANAGER_IP}
NODE_IPS=${NODE_IPS}

JOIN_CMD=$(docker swarm init --advertise-addr "$MANAGER_IP" | grep "$MANAGER_IP" | awk '{$1=$1;print}')
docker service create --name registry --publish published=5000,target=5000 registry:2

OLDIFS=$IFS
IFS=','

for nodeip in $NODE_IPS
do
    ssh $nodeip $JOIN_CMD
done

IFS=$OLDIFS
