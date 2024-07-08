FROM gradle:jdk17

COPY build/libs/cloud_alloc-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/home/gradle/app.jar"]