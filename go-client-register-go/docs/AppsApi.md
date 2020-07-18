# \AppsApi

All URIs are relative to *http://localhost:1906/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**GetAllAppsByName**](AppsApi.md#GetAllAppsByName) | **Get** /apps/{appName}/all | Get all apps endpoints by app name
[**GetAppsByName**](AppsApi.md#GetAppsByName) | **Get** /apps/{appName} | Get an app endpoint by app name
[**Register**](AppsApi.md#Register) | **Get** /register | Register an app on Eureka


# **GetAllAppsByName**
> []App GetAllAppsByName(ctx, appName)
Get all apps endpoints by app name

Returns an array of app endpoint

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for logging, tracing, authentication, etc.
  **appName** | **string**| App name | 

### Return type

[**[]App**](App.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **GetAppsByName**
> App GetAppsByName(ctx, appName)
Get an app endpoint by app name

Returns an app endpoint

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for logging, tracing, authentication, etc.
  **appName** | **string**| App name | 

### Return type

[**App**](App.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Register**
> Msg Register(ctx, )
Register an app on Eureka

Returns success

### Required Parameters
This endpoint does not need any parameter.

### Return type

[**Msg**](Msg.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

