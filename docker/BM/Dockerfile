FROM openjdk:8
WORKDIR /usr/src/myapp
COPY  BM-core-0.1.0.jar .
COPY lib lib/ 
COPY configuration configuration/
COPY cir cir/ 
CMD ["java","-cp","BM-core-0.1.0.jar:lib/*:lib/firebase/*","hello.Application"]

