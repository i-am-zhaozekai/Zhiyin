package com.haha.zy.glide;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.ExternalPreferredCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @Description: 自定义 GlideModule
 * @Author: Terrence Zhao
 * @Date: 17/06/2018
 */

@GlideModule
public class ZYGlideModule extends AppGlideModule {

    private static final int DEFAULT_DISK_CACHE_SIZE_ZY = 500 * 1024 * 1024;
    private static final String DEFAULT_DISK_CACHE_DIR_ZY = "image_cache";

    // URL 定期更新时间，1 周
    private static final long GLIDE_KEY_INVALID_TIME = 7 * 24 * 3600L;

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            builder.setDiskCache(new ExternalPreferredCacheDiskCacheFactory(context,
                    DEFAULT_DISK_CACHE_DIR_ZY, DEFAULT_DISK_CACHE_SIZE_ZY));
        } else {
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context,
                    DEFAULT_DISK_CACHE_DIR_ZY, DEFAULT_DISK_CACHE_SIZE_ZY));
        }

    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);

        registry.append(SingerModel.class, InputStream.class, new SingerModelLoader.Factory());
    }

    public static ObjectKey obtainSignatureKey() {
        return new ObjectKey(System.currentTimeMillis() / GLIDE_KEY_INVALID_TIME);
    }

}
