# Java Destination Example

## Pre-requisites
JDK 17
Gradle 8 

## Steps
1. Copy proto files from the root folder
```
> gradle copyProtos
```
2. Build the Jar
```
> gradle jar
```
3. Run the Jar
```
> java -jar build/libs/JavaDestination.jar 
```
