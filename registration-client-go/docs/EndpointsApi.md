# Endpoints Api

All URIs are relative to *http://localhost:1906/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**GetServiceEndpoint**](EndpointsApi.md#GetServiceEndpoint) | **Get** /services/{service}/endpoint | Obtém o melhor endpoint para o serviço {service}
[**GetServiceEndpoints**](EndpointsApi.md#GetServiceEndpoints) | **Get** /services/{service}/endpoints | Obtém todos os endpoints registados em nome do serviço {service}
[**Register**](EndpointsApi.md#Register) | **Post** /register | Regista o endpoint no servidor eureka


# **GetServiceEndpoint**
> Endpoint GetServiceEndpoint(ctx, service)
Obtém o melhor endpoint para o serviço {service}

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **service** | **string**| Service name | 

### Return type

[**Endpoint**](Endpoint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **GetServiceEndpoints**
> []Endpoint GetServiceEndpoints(ctx, service)
Obtém todos os endpoints registados em nome do serviço {service}

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **service** | **string**| Service name | 

### Return type

[**[]Endpoint**](Endpoint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Register**
> Register(ctx, )
Regista o endpoint no servidor eureka

### Required Parameters
This endpoint does not need any parameter.

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

