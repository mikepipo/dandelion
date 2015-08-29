/*
 * [The "BSD licence"]
 * Copyright (c) 2013-2015 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.asset.merging.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.github.dandelion.core.asset.Asset;
import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.asset.locator.impl.ApiLocator;
import com.github.dandelion.core.asset.merging.AbstractAssetMergingStrategy;
import com.github.dandelion.core.storage.MultiAssetEntry;
import com.github.dandelion.core.storage.SingleAssetEntry;
import com.github.dandelion.core.storage.StorageEntry;
import com.github.dandelion.core.util.AssetUtils;

/**
 * <p>
 * Strategy that performs an aggressive merge.
 * </p>
 * <p>
 * This strategy merges all assets needed by the HTML page, including vendor
 * assets.
 * </p>
 * 
 * @author Thibault Duchateau
 * @since 2.0.0
 */
public class AggressiveMergingStrategy extends AbstractAssetMergingStrategy {

   public static final String NAME = "aggressive";

   @Override
   public String getName() {
      return NAME;
   }

   @Override
   public Set<Asset> prepareStorageAndGet(Set<Asset> rawAssets, HttpServletRequest request) {

      Set<Asset> retval = new LinkedHashSet<Asset>();
      StorageEntry entry = null;

      // No distinction is done between vendor and regular assets
      for (Asset asset : rawAssets) {
         if (ApiLocator.LOCATION_KEY.equalsIgnoreCase(asset.getConfigLocationKey())) {
            String contents = AssetUtils.getAssetLocator(asset, context).getContent(asset, request);
            entry = new SingleAssetEntry(asset, contents);
         }
         else {
            entry = new SingleAssetEntry(asset, null);
         }
         this.context.getAssetStorage().put(asset.getStorageKey(), entry);
      }

      String storageKey = AssetUtils.generateStorageKey(rawAssets, request);

      Asset merge = new Asset();
      merge.setName(getMergedAssetName());
      merge.setVersion(getVersion(rawAssets, request));
      merge.setType(AssetType.valueOf(rawAssets.iterator().next().getType().toString()));
      merge.setStorageKey(storageKey);
      merge.setFinalLocation(AssetUtils.getAssetFinalLocation(request, merge, null));

      entry = new MultiAssetEntry(merge, rawAssets);
      this.context.getAssetStorage().put(storageKey, entry);
      
      retval.add(merge);

      return retval;
   }
}