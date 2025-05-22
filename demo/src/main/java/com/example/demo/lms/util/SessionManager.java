package com.example.demo.lms.util;

import com.example.demo.lms.model.Admin;

public class SessionManager {
    private static Admin currentAdmin = null;
    private static boolean loggedIn = false;

    public static void login(Admin admin) {
        currentAdmin = admin;
        loggedIn = true;
    }

    public static void logout() {
        currentAdmin = null;
        loggedIn = false;
    }

    public static boolean isLoggedIn() {
        return loggedIn;
    }

    public static Admin getCurrentAdmin() {
        return currentAdmin;
    }
}
