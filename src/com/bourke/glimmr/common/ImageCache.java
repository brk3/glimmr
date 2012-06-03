package com.bourke.glimmr;

import android.graphics.Bitmap;

import java.lang.ref.SoftReference;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ImageCache {

	private static final Map<String, SoftReference<Bitmap>> cache =
        new LinkedHashMap<String, SoftReference<Bitmap>>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(
				Entry<String, SoftReference<Bitmap>> eldest) {
			return size() > 50;
		}

	};

	private static final ReentrantReadWriteLock readWriteLock =
        new ReentrantReadWriteLock();
	private static final Lock read  = readWriteLock.readLock();
	private static final Lock write = readWriteLock.writeLock();

	private ImageCache() {
		super();
	}

	public static Bitmap getFromCache(String id){
		read.lock();
		try {
			if(!cache.containsKey(id))
				return null;
			SoftReference<Bitmap> ref=cache.get(id);
			return ref.get();
		} finally {
			read.unlock();
		}
	}

	public static void saveToCache(String id, Bitmap bitmap){
		write.lock();
		try {
			cache.put(id, new SoftReference<Bitmap>(bitmap));
		} finally {
			write.unlock();
		}
	}

	public static void clear() {
		write.lock();
		try {
			cache.clear();
		} finally {
			write.unlock();
		}
	}

}
