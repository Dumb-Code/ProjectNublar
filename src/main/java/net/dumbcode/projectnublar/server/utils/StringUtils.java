package net.dumbcode.projectnublar.server.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {
    public static String toCamelCase(String snakeCase) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0;i<snakeCase.length();i++) {
            char c = snakeCase.charAt(i);
            if(c == '_') {
                if(i+1 < snakeCase.length()) {
                    builder.append(Character.toUpperCase(snakeCase.charAt(i+1)));
                    i++;
                }
            } else {
                if(i == 0)
                    builder.append(Character.toUpperCase(c));
                else
                    builder.append(c);
            }
        }
        return builder.toString();
    }
}
