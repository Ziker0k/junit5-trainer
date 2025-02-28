package com.dmdev.exception;

import com.dmdev.validator.Error;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ValidationException extends RuntimeException {

    private final List<Error> errors;
}
