﻿/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

#pragma once
#include <aws/nimble/NimbleStudio_EXPORTS.h>
#include <aws/nimble/NimbleStudioRequest.h>
#include <aws/core/utils/memory/stl/AWSString.h>
#include <aws/core/utils/memory/stl/AWSMap.h>
#include <utility>

namespace Aws
{
namespace NimbleStudio
{
namespace Model
{

  /**
   */
  class AWS_NIMBLESTUDIO_API TagResourceRequest : public NimbleStudioRequest
  {
  public:
    TagResourceRequest();

    // Service request name is the Operation name which will send this request out,
    // each operation should has unique request name, so that we can get operation's name from this request.
    // Note: this is not true for response, multiple operations may have the same response name,
    // so we can not get operation's name from response.
    inline virtual const char* GetServiceRequestName() const override { return "TagResource"; }

    Aws::String SerializePayload() const override;


    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline const Aws::String& GetResourceArn() const{ return m_resourceArn; }

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline bool ResourceArnHasBeenSet() const { return m_resourceArnHasBeenSet; }

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline void SetResourceArn(const Aws::String& value) { m_resourceArnHasBeenSet = true; m_resourceArn = value; }

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline void SetResourceArn(Aws::String&& value) { m_resourceArnHasBeenSet = true; m_resourceArn = std::move(value); }

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline void SetResourceArn(const char* value) { m_resourceArnHasBeenSet = true; m_resourceArn.assign(value); }

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline TagResourceRequest& WithResourceArn(const Aws::String& value) { SetResourceArn(value); return *this;}

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline TagResourceRequest& WithResourceArn(Aws::String&& value) { SetResourceArn(std::move(value)); return *this;}

    /**
     * <p> The Amazon Resource Name (ARN) of the resource you want to add tags to. </p>
     */
    inline TagResourceRequest& WithResourceArn(const char* value) { SetResourceArn(value); return *this;}


    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline const Aws::Map<Aws::String, Aws::String>& GetTags() const{ return m_tags; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline bool TagsHasBeenSet() const { return m_tagsHasBeenSet; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline void SetTags(const Aws::Map<Aws::String, Aws::String>& value) { m_tagsHasBeenSet = true; m_tags = value; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline void SetTags(Aws::Map<Aws::String, Aws::String>&& value) { m_tagsHasBeenSet = true; m_tags = std::move(value); }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& WithTags(const Aws::Map<Aws::String, Aws::String>& value) { SetTags(value); return *this;}

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& WithTags(Aws::Map<Aws::String, Aws::String>&& value) { SetTags(std::move(value)); return *this;}

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(const Aws::String& key, const Aws::String& value) { m_tagsHasBeenSet = true; m_tags.emplace(key, value); return *this; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(Aws::String&& key, const Aws::String& value) { m_tagsHasBeenSet = true; m_tags.emplace(std::move(key), value); return *this; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(const Aws::String& key, Aws::String&& value) { m_tagsHasBeenSet = true; m_tags.emplace(key, std::move(value)); return *this; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(Aws::String&& key, Aws::String&& value) { m_tagsHasBeenSet = true; m_tags.emplace(std::move(key), std::move(value)); return *this; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(const char* key, Aws::String&& value) { m_tagsHasBeenSet = true; m_tags.emplace(key, std::move(value)); return *this; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(Aws::String&& key, const char* value) { m_tagsHasBeenSet = true; m_tags.emplace(std::move(key), value); return *this; }

    /**
     * <p>A collection of labels, in the form of key:value pairs, that apply to this
     * resource.</p>
     */
    inline TagResourceRequest& AddTags(const char* key, const char* value) { m_tagsHasBeenSet = true; m_tags.emplace(key, value); return *this; }

  private:

    Aws::String m_resourceArn;
    bool m_resourceArnHasBeenSet;

    Aws::Map<Aws::String, Aws::String> m_tags;
    bool m_tagsHasBeenSet;
  };

} // namespace Model
} // namespace NimbleStudio
} // namespace Aws
