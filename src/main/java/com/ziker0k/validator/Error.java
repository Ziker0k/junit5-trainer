package com.ziker0k.validator;

import lombok.Value;

@Value(staticConstructor = "of")
public class Error {
    Integer code;
    String message;
}
