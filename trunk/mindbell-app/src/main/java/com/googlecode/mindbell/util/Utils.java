/**
 * Copyright (C) 2011 Marc Schroeder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mindbell.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;

/**
 * @author marc
 * 
 */
public class Utils {

    /**
     * Convert a resource id into a Uri.
     * 
     * @param context
     * @param resid
     * @return
     */
    public static Uri getResourceUri(Context context, int resid) throws NotFoundException {
        Resources resources = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resid) + "/" + resid);

    }
}
