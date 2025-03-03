package com.ziker0k.exception;

import com.ziker0k.validator.Error;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ValidationException extends RuntimeException {

    private final List<Error> errors;
}
