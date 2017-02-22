package com.beoni.openwaterswimtracking.utils;

public interface ICallback
{
    void onSuccess(Object result);

    void onFail(Exception ex);
}
