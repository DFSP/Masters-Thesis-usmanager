# AppsApi

All URIs are relative to *http://localhost:1906/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAllAppsByName**](AppsApi.md#getAllAppsByName) | **GET** /apps/{appName}/all | Get all apps endpoints by app name
[**getAppsByName**](AppsApi.md#getAppsByName) | **GET** /apps/{appName} | Get an app endpoint by app name
[**register**](AppsApi.md#register) | **GET** /register | Register an app on Eureka


<a name="getAllAppsByName"></a>
# **getAllAppsByName**
> List&lt;App&gt; getAllAppsByName(appName)

Get all apps endpoints by app name

Returns an array of app endpoint

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.AppsApi;


AppsApi apiInstance = new AppsApi();
String appName = "appName_example"; // String | App name
try {
    List<App> result = apiInstance.getAllAppsByName(appName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AppsApi#getAllAppsByName");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **appName** | **String**| App name |

### Return type

[**List&lt;App&gt;**](App.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="getAppsByName"></a>
# **getAppsByName**
> App getAppsByName(appName)

Get an app endpoint by app name

Returns an app endpoint

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.AppsApi;


AppsApi apiInstance = new AppsApi();
String appName = "appName_example"; // String | App name
try {
    App result = apiInstance.getAppsByName(appName);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AppsApi#getAppsByName");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **appName** | **String**| App name |

### Return type

[**App**](App.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="register"></a>
# **register**
> Msg register()

Register an app on Eureka

Returns success

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.AppsApi;


AppsApi apiInstance = new AppsApi();
try {
    Msg result = apiInstance.register();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AppsApi#register");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Msg**](Msg.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

