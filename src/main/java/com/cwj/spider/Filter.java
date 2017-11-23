package com.cwj.spider;

public interface Filter<T> {  
    public boolean accept(T obj);  
}  
