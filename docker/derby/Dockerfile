FROM openjdk:8
COPY db-derby-10.14.1.0-bin /usr/local/db-derby-10.14.1.0-bin
EXPOSE 1527
WORKDIR usr/local/db-derby-10.14.1.0-bin/bin/
CMD ["./startNetworkServer","-h","0.0.0.0"]