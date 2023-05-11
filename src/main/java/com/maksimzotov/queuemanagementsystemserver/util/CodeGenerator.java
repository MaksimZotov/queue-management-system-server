package com.maksimzotov.queuemanagementsystemserver.util;

import java.util.List;
import java.util.Random;

public class CodeGenerator {
    public static Integer generateCodeForEmail() {
        return new Random().nextInt(9000) + 1000;
    }

    public static Integer generateCodeInLocation(List<Integer> list) {
        if (list.isEmpty()) {
            return 1;
        }
        List<Integer> sorted = list.stream().sorted().toList();
        if (sorted.get(0) == 2) {
            return 1;
        }
        for (int i = 0; i < sorted.size() - 1; i++) {
            if (sorted.get(i + 1) - sorted.get(i) > 1) {
                return sorted.get(i) + 1;
            }
        }
        return sorted.get(sorted.size() - 1) + 1;
    }

    public static Integer generateAccessKey() {
        return new Random().nextInt(9000) + 1000;
    }
}
