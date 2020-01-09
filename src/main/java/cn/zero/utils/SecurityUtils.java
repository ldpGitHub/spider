package cn.zero.utils;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static String username() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
