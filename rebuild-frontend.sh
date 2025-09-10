#!/bin/bash
eval $(minikube docker-env)
kubectl delete -f kubernetes/frontend.yaml
sleep 10
docker image remove localhost:5000/frontend
docker build --no-cache -t localhost:5000/frontend frontend
docker push localhost:5000/frontend
docker image remove localhost:5000/frontend
kubectl create -f kubernetes/frontend.yaml
