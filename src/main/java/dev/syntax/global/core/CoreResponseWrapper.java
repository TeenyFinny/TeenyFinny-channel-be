package dev.syntax.global.core;

import lombok.Getter;

@Getter
public class CoreResponseWrapper<T> {
    private T data;
}
