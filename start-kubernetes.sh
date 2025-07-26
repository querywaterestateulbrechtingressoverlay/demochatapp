cd chat-app && rm -rf build/ && ./gradlew bootJar && cd ..
docker build -t localhost:5000/chat-app chat-app
docker push localhost:5000/chat-app
docker image remove localhost:5000/chat-app
