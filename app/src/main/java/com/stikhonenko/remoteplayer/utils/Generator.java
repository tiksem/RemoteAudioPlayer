package com.stikhonenko.remoteplayer.utils;

/**
 * Created by stikhonenko on 1/5/16.
 */
public interface Generator<From, To> {
    To get(From from);
}
