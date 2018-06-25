package com.haha.zy.glide;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

/**
 * @Description:
 * @Author: Terrence Zhao
 * @Date: 19/06/2018
 */

public class SingerModelLoader implements ModelLoader<SingerModel, InputStream> {

    @Nullable
    private final ModelCache<SingerModel, SingerModel> modelCache;

    public SingerModelLoader() {
        this(null);
    }

    public SingerModelLoader(@Nullable ModelCache<SingerModel, SingerModel> modeCache) {
        this.modelCache = modeCache;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull SingerModel singerModel, int width, int height, @NonNull Options options) {
        SingerModel singer = singerModel;

        if (modelCache != null) {
            singer = modelCache.get(singerModel, 0, 0);
            if (singer == null) {
                modelCache.put(singerModel, 0, 0, singerModel);
                singer = singerModel;
            }
        }
        return new LoadData<>((Key) singer, new SingerFetcher(singer));
    }

    @Override
    public boolean handles(@NonNull SingerModel singerModel) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<SingerModel, InputStream> {
        private final ModelCache<SingerModel, SingerModel> modelCache = new ModelCache<>(500);

        @NonNull
        @Override
        public ModelLoader<SingerModel, InputStream> build(MultiModelLoaderFactory multiFactory) {
            return new SingerModelLoader(modelCache);
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }

}
