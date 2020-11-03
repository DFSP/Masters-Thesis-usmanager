# EndpointsApi

All URIs are relative to *http://localhost:1906/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getServiceEndpoint**](EndpointsApi.md#getServiceEndpoint) | **GET** /services/{service}/endpoint | Obtém o melhor endpoint para o serviço {service}
[**getServiceEndpoints**](EndpointsApi.md#getServiceEndpoints) | **GET** /services/{service}/endpoints | Obtém todos os endpoints registados em nome do serviço {service}
[**register**](EndpointsApi.md#register) | **POST** /register | Regista o endpoint no servidor eureka


<a name="GetServiceEndpoint"></a>
# **getServiceEndpoint**
> Endpoint getServiceEndpoint(service)

Obtém o melhor endpoint para o serviço {service}

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.EndpointsApi;


EndpointsApi apiInstance = new EndpointsApi();
String service = "service_example"; // String | Service name
try {
    Endpoint result = apiInstance.getServiceEndpoint(service);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling EndpointsApi#getServiceEndpoint");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **service** | **String**| Service name |

### Return type

[**Endpoint**](Endpoint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="getServiceEndpoints"></a>
# **getServiceEndpoints**
> List&lt;Endpoint&gt; getServiceEndpoints(service)

Obtém todos os endpoints registados em nome do serviço {service}

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.EndpointsApi;


EndpointsApi apiInstance = new EndpointsApi();
String service = "service_example"; // String | Service name
try {
    List<Endpoint> result = apiInstance.getServiceEndpoints(service);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling EndpointsApi#getServiceEndpoints");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **service** | **String**| Service name |

### Return type

[**List&lt;Endpoint&gt;**](Endpoint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="register"></a>
# **register**
> register()

Regista o endpoint no servidor eureka

### Example
```java
// Import classes:
//import io.swagger.client.ApiException;
//import io.swagger.client.api.EndpointsApi;


EndpointsApi apiInstance = new EndpointsApi();
try {
    apiInstance.register();
} catch (ApiException e) {
    System.err.println("Exception when calling EndpointsApi#register");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

