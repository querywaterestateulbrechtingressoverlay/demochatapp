minikube start --memory=3500Mb
eval $(minikube docker-env)
minikube addons enable ingress
docker run -d -p 5000:5000 registry:2
cd chat-app && rm -rf build/ && ./gradlew bootJar
cd ..
docker build -t localhost:5000/frontend frontend
docker build -t localhost:5000/chat-app chat-app
docker push localhost:5000/frontend
docker push localhost:5000/chat-app
docker image remove localhost:5000/frontend
docker image remove localhost:5000/chat-app
bash kubernetes/generate-jwt-rsa-key-pair.sh
KUBE_CONFIG_FILES=$( find kubernetes/ -regex '.*.yaml' )
for config in $KUBE_CONFIG_FILES; do
	kubectl create -f $config
done
echo $(minikube ip) chatapp.test
openssl req -x509 -newkey rsa:2048 -days 365 -nodes \
  -keyout tls.key -out tls.crt \
  -subj '/CN=chatapp.test' \
  -extensions san -config <(cat << EOF
[req]
distinguished_name=req
[san]
subjectAltName=@alt_names
[alt_names]
DNS.1=chatapp.test
DNS.2=
EOF
)
kubectl create secret tls tls-secret --cert=tls.crt --key=tls.key
rm tls.crt tls.key

