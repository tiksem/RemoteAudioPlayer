package com.stikhonenko.remoteplayer.utils;

import java.util.AbstractList;
import java.util.List;

/**
 * Created by stikhonenko on 1/5/16.
 */
public class Lists {
    public static <From, To> List<To> map(final List<From> from,
                                          final Generator<From, To> generator) {
        return new AbstractList<To>() {
            @Override
            public To get(int location) {
                return generator.get(from.get(location));
            }

            @Override
            public int size() {
                return from.size();
            }
        };
    }
}
