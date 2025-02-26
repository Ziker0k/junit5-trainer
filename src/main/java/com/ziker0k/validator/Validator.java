package com.ziker0k.validator;

public interface Validator<T> {

    ValidationResult validate(T object);
}
