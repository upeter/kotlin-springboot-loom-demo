### Demo Project for my Youtube talks
- Part 1: Headache-Free Reactive Programming With Spring Boot and Kotlin Coroutines:
  - https://www.youtube.com/watch?v=ahTXElHrV0c&ab_channel=KotlinbyJetBrains
- Part 2: Headache-Free Reactive Programming With Spring Boot and Kotlin Coroutines - Adding Virtual Threads to the mix
  - https://www.youtube.com/watch?v=szl3eWA0VRw&lc=UgyA1j8C3_TlfQhXCcp4AaABAg&ab_channel=KotlinbyJetBrains

### Check out our Kotlin courses and upskilling programs!
- https://xebia.com/academy/nl/upskilling/kotlin-academy/

### Installation
#### Install Java 21
- Install openjdk 21

#### Install baton commandline perf tool
- See instructions at: ```https://github.com/americanexpress/baton```

#### Create curltime
- Add `~/.curl-format.txt` with the following content:
  ```===========================\n
  TOTAL TIME:  %{time_total}s\n
  ===========================\n```
- in `!/.bash_profile` add the following entry: `alias curltime="curl -w \"@$HOME/.curl-format.txt\" -o NUL -s "`
- source `~/.bash_profile`

#### Start remote server
For the samples to run, we need to talk to a remote server.
- Go to the folder `node-server`
- execute: `build.sh`
- execute: `run.sh`


### All demo examples
#### Blocking
- Disable Virtual Threads: `spring.threads.virtual.enabled=true`
```
curl  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/blocking/users\?delay\=200 | jq
curltime  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/blocking/users\?delay\=200
baton -m POST -u http://localhost:8085/blocking/users?delay=2000 -z users-blocking.csv  -c 100 -r 110
```

#### Reactor
```
curl  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/reactorj/users\?delay\=200 | jq
curltime  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/reactorj/users\?delay\=200
baton -m POST -u http://localhost:8083/reactorj/users?delay=2000 -z users-reactive.csv  -c 100 -r 110
```

#### Coroutines
```
curl  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/users\?delay\=200 | jq
curltime  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/users\?delay\=200
baton -m POST -u http://localhost:8085/users?delay=2000 -z users-coroutines.csv  -c 100 -r 110
```

#### Blocking with Virtual Threads
- Enable Virtual Threads: `spring.threads.virtual.enabled=true`
```
curl  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/blocking/users\?delay\=200 | jq
curltime  -X POST -H "Content-Type: application/json" -d '{"id":null,"userName":"Jack","email":"Rabbit@hi.nl","avatarUrl":null}' http://localhost:8085/blocking/users\?delay\=200
baton -m POST -u http://localhost:8085/blocking/users?delay=2000 -z users-blocking.csv  -c 100 -r 110
```
