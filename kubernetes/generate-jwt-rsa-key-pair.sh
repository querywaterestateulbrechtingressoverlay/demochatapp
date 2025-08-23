#!/bin/bash
PRIVATE_KEY=$(openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 2>/dev/null)
PUBLIC_KEY=$(echo "$PRIVATE_KEY" | openssl rsa -pubout)
kubectl create secret generic chat-app-jwt-keys --from-literal=private="$PRIVATE_KEY"  --from-literal=public="$PUBLIC_KEY"
