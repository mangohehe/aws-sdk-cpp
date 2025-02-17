﻿/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#include <aws/firehose/model/GetKinesisStreamResult.h>
#include <aws/core/utils/json/JsonSerializer.h>
#include <aws/core/AmazonWebServiceResult.h>
#include <aws/core/utils/StringUtils.h>
#include <aws/core/utils/UnreferencedParam.h>
#include <aws/core/utils/memory/stl/AWSStringStream.h>

#include <utility>

using namespace Aws::Firehose::Model;
using namespace Aws::Utils::Json;
using namespace Aws::Utils;
using namespace Aws;

GetKinesisStreamResult::GetKinesisStreamResult()
{
}

GetKinesisStreamResult::GetKinesisStreamResult(const Aws::AmazonWebServiceResult<JsonValue>& result)
{
  *this = result;
}

GetKinesisStreamResult& GetKinesisStreamResult::operator =(const Aws::AmazonWebServiceResult<JsonValue>& result)
{
  JsonView jsonValue = result.GetPayload().View();
  if(jsonValue.ValueExists("KinesisStreamARN"))
  {
    m_kinesisStreamARN = jsonValue.GetString("KinesisStreamARN");

  }

  if(jsonValue.ValueExists("CredentialsForReadingKinesisStream"))
  {
    m_credentialsForReadingKinesisStream = jsonValue.GetObject("CredentialsForReadingKinesisStream");

  }


  const auto& headers = result.GetHeaderValueCollection();
  const auto& requestIdIter = headers.find("x-amzn-requestid");
  if(requestIdIter != headers.end())
  {
    m_requestId = requestIdIter->second;
  }


  return *this;
}
