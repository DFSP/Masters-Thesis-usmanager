/**
 * Registration-client-api
 * Interage com o registration server (eureka) para registar esta instância ou obter servidores com o qual pode comunicar
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator 2.4.17.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

/*
 * Endpoint.h
 *
 * 
 */

#ifndef IO_SWAGGER_CLIENT_MODEL_Endpoint_H_
#define IO_SWAGGER_CLIENT_MODEL_Endpoint_H_


#include "../ModelBase.h"

#include <cpprest/details/basic_types.h>

namespace io {
namespace swagger {
namespace client {
namespace model {

/// <summary>
/// 
/// </summary>
class  Endpoint
    : public ModelBase
{
public:
    Endpoint();
    virtual ~Endpoint();

    /////////////////////////////////////////////
    /// ModelBase overrides

    void validate() override;

    web::json::value toJson() const override;
    void fromJson(web::json::value& json) override;

    void toMultipart(std::shared_ptr<MultipartFormData> multipart, const utility::string_t& namePrefix) const override;
    void fromMultiPart(std::shared_ptr<MultipartFormData> multipart, const utility::string_t& namePrefix) override;

    /////////////////////////////////////////////
    /// Endpoint members

    /// <summary>
    /// 
    /// </summary>
    utility::string_t getInstanceId() const;
    bool instanceIdIsSet() const;
    void unsetInstanceId();
    void setInstanceId(utility::string_t value);
    /// <summary>
    /// 
    /// </summary>
    utility::string_t getEndpoint() const;
    bool endpointIsSet() const;
    void unsetEndpoint();
    void setEndpoint(utility::string_t value);

protected:
    utility::string_t m_InstanceId;
    bool m_InstanceIdIsSet;
    utility::string_t m_Endpoint;
    bool m_EndpointIsSet;
};

}
}
}
}

#endif /* IO_SWAGGER_CLIENT_MODEL_Endpoint_H_ */