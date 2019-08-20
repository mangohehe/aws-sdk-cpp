﻿/*
* Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License").
* You may not use this file except in compliance with the License.
* A copy of the License is located at
*
*  http://aws.amazon.com/apache2.0
*
* or in the "license" file accompanying this file. This file is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing
* permissions and limitations under the License.
*/

#pragma once
#include <aws/appstream/AppStream_EXPORTS.h>
#include <aws/appstream/model/AccessEndpointType.h>
#include <aws/core/utils/memory/stl/AWSString.h>
#include <utility>

namespace Aws
{
namespace Utils
{
namespace Json
{
  class JsonValue;
  class JsonView;
} // namespace Json
} // namespace Utils
namespace AppStream
{
namespace Model
{

  /**
   * <p>Describes a virtual private cloud (VPC) interface endpoint that lets you
   * create a private connection between the VPC that you specify and AppStream 2.0.
   * When you specify a VPC interface endpoint for a stack, users of the stack can
   * connect to AppStream 2.0 only through that endpoint. When you specify a VPC
   * interface endpoint for an image builder, administrators can connect to the image
   * builder only through that endpoint.</p><p><h3>See Also:</h3>   <a
   * href="http://docs.aws.amazon.com/goto/WebAPI/appstream-2016-12-01/AccessEndpoint">AWS
   * API Reference</a></p>
   */
  class AWS_APPSTREAM_API AccessEndpoint
  {
  public:
    AccessEndpoint();
    AccessEndpoint(Aws::Utils::Json::JsonView jsonValue);
    AccessEndpoint& operator=(Aws::Utils::Json::JsonView jsonValue);
    Aws::Utils::Json::JsonValue Jsonize() const;


    /**
     * <p>The type of VPC interface endpoint.</p>
     */
    inline const AccessEndpointType& GetEndpointType() const{ return m_endpointType; }

    /**
     * <p>The type of VPC interface endpoint.</p>
     */
    inline bool EndpointTypeHasBeenSet() const { return m_endpointTypeHasBeenSet; }

    /**
     * <p>The type of VPC interface endpoint.</p>
     */
    inline void SetEndpointType(const AccessEndpointType& value) { m_endpointTypeHasBeenSet = true; m_endpointType = value; }

    /**
     * <p>The type of VPC interface endpoint.</p>
     */
    inline void SetEndpointType(AccessEndpointType&& value) { m_endpointTypeHasBeenSet = true; m_endpointType = std::move(value); }

    /**
     * <p>The type of VPC interface endpoint.</p>
     */
    inline AccessEndpoint& WithEndpointType(const AccessEndpointType& value) { SetEndpointType(value); return *this;}

    /**
     * <p>The type of VPC interface endpoint.</p>
     */
    inline AccessEndpoint& WithEndpointType(AccessEndpointType&& value) { SetEndpointType(std::move(value)); return *this;}


    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline const Aws::String& GetVpceId() const{ return m_vpceId; }

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline bool VpceIdHasBeenSet() const { return m_vpceIdHasBeenSet; }

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline void SetVpceId(const Aws::String& value) { m_vpceIdHasBeenSet = true; m_vpceId = value; }

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline void SetVpceId(Aws::String&& value) { m_vpceIdHasBeenSet = true; m_vpceId = std::move(value); }

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline void SetVpceId(const char* value) { m_vpceIdHasBeenSet = true; m_vpceId.assign(value); }

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline AccessEndpoint& WithVpceId(const Aws::String& value) { SetVpceId(value); return *this;}

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline AccessEndpoint& WithVpceId(Aws::String&& value) { SetVpceId(std::move(value)); return *this;}

    /**
     * <p>The identifier (ID) of the VPC in which the endpoint is used.</p>
     */
    inline AccessEndpoint& WithVpceId(const char* value) { SetVpceId(value); return *this;}

  private:

    AccessEndpointType m_endpointType;
    bool m_endpointTypeHasBeenSet;

    Aws::String m_vpceId;
    bool m_vpceIdHasBeenSet;
  };

} // namespace Model
} // namespace AppStream
} // namespace Aws
