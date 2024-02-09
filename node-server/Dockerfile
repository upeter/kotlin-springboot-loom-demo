FROM node:18
WORKDIR /app
RUN apt-get update -yq \
    && apt-get install tcpdump -yq 
COPY package.json /app
RUN npm install
COPY . /app
CMD node index.js
EXPOSE 8081

