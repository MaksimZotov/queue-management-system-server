package com.maksimzotov.queuemanagementsystemserver.util;

import java.util.Random;

public class CodeGenerator {
    public static Integer generate() {
        return new Random().nextInt(9000) + 1000;
    }
}
