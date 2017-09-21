package com.beoni.openwaterswimtracking.utils;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reusable implementation, just extend this class with
 * a Loader and the returning type you need to use it
 * in the activity. See HugeContentLoader<String> as
 * example.
 * @param <T>
 */
public abstract class AsyncTaskLoaderEx<T> extends AsyncTaskLoader<T>
{
    private static final AtomicInteger sCurrentUniqueId = new AtomicInteger(0);
    private T mData;
    public boolean hasResult = false;

    public static int getNewUniqueLoaderId() {
        return sCurrentUniqueId.getAndIncrement();
    }

    public AsyncTaskLoaderEx(final Context context) {
        super(context);
        onContentChanged();
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged())
            forceLoad();
        else if (hasResult)
            deliverResult(mData);
    }

    @Override
    public void deliverResult(final T data) {
        mData = data;
        hasResult = true;
        super.deliverResult(data);
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        if (hasResult) {
            onReleaseResources(mData);
            mData = null;
            hasResult = false;
        }
    }

    protected void onReleaseResources(T data) {
        //nothing to do.
    }

    public T getResult() {
        return mData;
    }
}
