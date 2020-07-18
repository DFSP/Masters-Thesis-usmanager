# swagger-java-client

## Requirements

Building the API client library requires [Maven](https://maven.apache.org/) to be installed.

## Installation

To install the API client library to your local Maven repository, simply execute:

```shell
mvn install
```

To deploy it to a remote Maven repository instead, configure the settings of the repository and execute:

```shell
mvn deploy
```

Refer to the [official documentation](https://maven.apache.org/plugins/maven-deploy-plugin/usage.html) for more information.

### Maven users

Add this dependency to your project's POM:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>org.bitbucket.cloudedgemicroserviceteam</groupId>
    <artifactId>java-client-register-go</artifactId>
    <version>RELEASE-1.0.0</version>
</dependency>
```

### Gradle users

Add this dependency to your project's build file:

```groovy
compile "io.swagger:swagger-java-client:1.0.0"
```

### Others

At first generate the JAR by executing:

    mvn package

Then manually install the following JARs:

* target/swagger-java-client-1.0.0.jar
* target/lib/*.jar

## Getting Started

Please follow the [installation](#installation) instruction and execute the following Java code:

```java

import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.AppsApi;

import java.io.File;
import java.util.*;

public class AppsApiExample {

    public static void main(String[] args) {
        
        AppsApi apiInstance = new AppsApi();
        String appName = "appName_example"; // String | App name
        try {
            List<App> result = apiInstance.getAllAppsByName(appName);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling AppsApi#getAllAppsByName");
            e.printStackTrace();
        }
    }
}

```

## Documentation for API Endpoints

All URIs are relative to *http://localhost:1906/api*

Class | Method | HTTP request | Description
------------ | ------------- | ------------- | -------------
*AppsApi* | [**getAllAppsByName**](docs/AppsApi.md#getAllAppsByName) | **GET** /apps/{appName}/all | Get all apps endpoints by app name
*AppsApi* | [**getAppsByName**](docs/AppsApi.md#getAppsByName) | **GET** /apps/{appName} | Get an app endpoint by app name
*AppsApi* | [**register**](docs/AppsApi.md#register) | **GET** /register | Register an app on Eureka


## Documentation for Models

 - [App](docs/App.md)
 - [Msg](docs/Msg.md)


## Documentation for Authorization

All endpoints do not require authorization.
Authentication schemes defined for the API:

## Recommendation

It's recommended to create an instance of `ApiClient` per thread in a multithreaded environment to avoid any potential issues.

## Author



