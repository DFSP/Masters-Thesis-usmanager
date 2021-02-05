# swagger_client.EndpointsApi

All URIs are relative to *http://localhost:1906/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**get_service_endpoint**](EndpointsApi.md#get_service_endpoint) | **GET** /services/{service}/endpoint | Obtém o melhor endpoint para o serviço {service}
[**get_service_endpoints**](EndpointsApi.md#get_service_endpoints) | **GET** /services/{service}/endpoints | Obtém todos os endpoints registados em nome do serviço {service}
[**register_endpoint**](EndpointsApi.md#register_endpoint) | **POST** /register | Regista o endpoint no servidor eureka


# **get_service_endpoint**
> Endpoint get_service_endpoint(service)

Obtém o melhor endpoint para o serviço {service}

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.EndpointsApi()
service = 'service_example' # str | Service name

try:
    # Obtém o melhor endpoint para o serviço {service}
    api_response = api_instance.get_service_endpoint(service)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling EndpointsApi->get_service_endpoint: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **service** | **str**| Service name | 

### Return type

[**Endpoint**](Endpoint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **get_service_endpoints**
> list[Endpoint] get_service_endpoints(service)

Obtém todos os endpoints registados em nome do serviço {service}

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.EndpointsApi()
service = 'service_example' # str | Service name

try:
    # Obtém todos os endpoints registados em nome do serviço {service}
    api_response = api_instance.get_service_endpoints(service)
    pprint(api_response)
except ApiException as e:
    print("Exception when calling EndpointsApi->get_service_endpoints: %s\n" % e)
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **service** | **str**| Service name | 

### Return type

[**list[Endpoint]**](Endpoint.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **register_endpoint**
> register_endpoint()

Regista o endpoint no servidor eureka

### Example
```python
from __future__ import print_function
import time
import swagger_client
from swagger_client.rest import ApiException
from pprint import pprint

# create an instance of the API class
api_instance = swagger_client.EndpointsApi()

try:
    # Regista o endpoint no servidor eureka
    api_instance.register_endpoint()
except ApiException as e:
    print("Exception when calling EndpointsApi->register_endpoint: %s\n" % e)
```

### Parameters
This endpoint does not need any parameter.

### Return type

void (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

