cd chat-app && rm -rf build/ && ./gradlew bootJar && cd ..
docker build -t localhost:5000/frontend frontend
docker build -t localhost:5000/chat-app chat-app
docker push localhost:5000/frontend
docker push localhost:5000/chat-app
docker image remove localhost:5000/frontend
docker image remove localhost:5000/chat-app
