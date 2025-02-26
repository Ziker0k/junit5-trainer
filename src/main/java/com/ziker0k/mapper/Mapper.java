package com.ziker0k.mapper;

public interface Mapper<F, T> {

    T map(F object);
}
