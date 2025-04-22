# this machine's ip address
MANAGER_IP=${MANAGER_IP}
# comma-separated ip addresses of worker machines
NODE_IPS=${NODE_IPS}

# build chat app
./gradlew build -x test
# create a swarm and save the command to join it
JOIN_CMD=$(docker swarm init --advertise-addr "$MANAGER_IP" | grep "$MANAGER_IP" | awk '{$1=$1;print}')
# create registry to host built images
docker service create --name registry --publish published=5000,target=5000 registry:2

OLDIFS=$IFS
IFS=','

# join the swarm on worker machines
for nodeip in $NODE_IPS
do
    ssh $nodeip $JOIN_CMD
done

# start the app
docker compose up --no-start
docker compose push
docker stack deploy -c compose.yml chatapp

IFS=$OLDIFS
