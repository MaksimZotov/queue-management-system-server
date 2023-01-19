package com.maksimzotov.queuemanagementsystemserver.util;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CodeGenerator {
    public static String generate() {
        return String.valueOf(new Random().nextInt(9000) + 1000);
    }

    public static Integer generate(List<Integer> list) {
        int result = 1;
        Optional<Integer> minOptional = list.stream().min(Integer::compare);
        if (minOptional.isPresent()) {
            int min = minOptional.get();
            if (min > 1) {
                result = min - 1;
            } else {
                result = list.stream().max(Integer::compare).get() + 1;
            }
        }
        return result;
    }
}
