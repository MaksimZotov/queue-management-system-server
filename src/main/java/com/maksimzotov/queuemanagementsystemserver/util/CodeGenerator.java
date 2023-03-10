package com.maksimzotov.queuemanagementsystemserver.util;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class CodeGenerator {
    public static Integer generateCodeForEmail() {
        return new Random().nextInt(9000) + 1000;
    }

    public static Integer generateCodeInLocation(List<Integer> list) {
        int result = 1;
        Optional<Integer> minOptional = list.stream().filter(Objects::nonNull).min(Integer::compare);
        if (minOptional.isPresent()) {
            int min = minOptional.get();
            if (min > 1) {
                result = min - 1;
            } else {
                result = list.stream().filter(Objects::nonNull).max(Integer::compare).get() + 1;
            }
        }
        return result;
    }

    public static Integer generateAccessKey() {
        return new Random().nextInt(9000000) + 1000000;
    }
}
