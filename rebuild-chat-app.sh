#!/bin/bash
eval $(minikube docker-env)
kubectl delete -f kubernetes/chat-app.yaml
cd chat-app && rm -rf build/ && ./gradlew bootJar
cd ..
docker image remove localhost:5000/chat-app
docker build -t localhost:5000/chat-app chat-app
docker push localhost:5000/chat-app
docker image remove localhost:5000/chat-app
kubectl create -f kubernetes/chat-app.yaml
