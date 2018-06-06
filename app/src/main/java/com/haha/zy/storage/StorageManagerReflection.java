package com.haha.zy.storage;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.RequiresApi;

import com.haha.zy.ZYApplication;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class StorageManagerReflection {

	private static final String METHOD_NAME_GET_PATH = "getPath";

	public static List<StorageVolumeInfo> getStorageVolumes(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return getAvailableStorageN(context.getApplicationContext());
		} else {
			return getAvailableStoragePreN(context.getApplicationContext());
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	private static List<StorageVolumeInfo> getAvailableStorageN(Context context) {
		Method getPathMethod = null;
		try {
			getPathMethod = StorageVolume.class.getMethod(METHOD_NAME_GET_PATH);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}

		if (getPathMethod == null){
			return null;
		}

		ArrayList<StorageVolumeInfo> storageInfoList = new ArrayList<StorageVolumeInfo>();

		StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		List<StorageVolume> storageVolumes = storageManager.getStorageVolumes();
		if (storageVolumes != null) {
			for (StorageVolume volume : storageVolumes) {
				String path = null;
				try {
					path = (String) getPathMethod.invoke(volume);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}

				if (path != null) {
					StorageVolumeInfo info = new StorageVolumeInfo(path);
					info.mState = volume.getState();
					info.mRemovable = volume.isRemovable();

					storageInfoList.add(info);
				}
			}
		}

		storageInfoList.trimToSize();

		return storageInfoList;
	}

	private static List<StorageVolumeInfo> getAvailableStoragePreN(Context context) {
		ArrayList<StorageVolumeInfo> storaggeInfoList = new ArrayList<>();
		StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		try {
			Class<?>[] paramClasses = {};
			Method getVolumeList = StorageManager.class.getMethod("getVolumeList", paramClasses);
			getVolumeList.setAccessible(true);
			Object[] params = {};
			Object[] invokes = (Object[]) getVolumeList.invoke(storageManager, params);
			if (invokes != null) {
				StorageVolumeInfo info = null;
				for (int i = 0; i < invokes.length; i++) {
					Object obj = invokes[i];
					Method getPath = obj.getClass().getMethod(METHOD_NAME_GET_PATH);
					String path = (String) getPath.invoke(obj);
					info = new StorageVolumeInfo(path);
					File file = new File(info.mPath);
					if ((file.exists()) && (file.isDirectory()) && (file.canWrite())) {
						Method isRemovable = obj.getClass().getMethod("isRemovable");
						String state = null;
						try {
							Method getVolumeState = StorageManager.class.getMethod("getVolumeState", String.class);
							state = (String) getVolumeState.invoke(storageManager, info.mPath);
							info.mState = state;
						} catch (Exception e) {
							e.printStackTrace();
						}

						if (info.isMounted()) {
							info.mRemovable = ((Boolean) isRemovable.invoke(obj)).booleanValue();
							storaggeInfoList.add(info);
						}
					}
				}
			}
		} catch (NoSuchMethodException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		storaggeInfoList.trimToSize();

		return storaggeInfoList;
	}
}