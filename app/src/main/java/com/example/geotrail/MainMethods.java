package com.example.geotrail;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class MainMethods {

    public static boolean isPasswordStrongEnough(String password){
        if (password.length()<8){
            return false;
        }
        else if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*")) {
            return false;
        }
        else {
            return true;
        }
    }

    public static String hashPassword(String password) {
        try {
            // SHA-256 hashing algoritmasını kullanarak bir MessageDigest örneği oluşturun
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Kullanıcı şifresini byte dizisine dönüştürün ve hash'leyin
            byte[] hashedBytes = digest.digest(password.getBytes());

            // Byte dizisini hexadecimal formatına dönüştürün
            StringBuilder hexString = new StringBuilder();
            for (byte hashedByte : hashedBytes) {
                String hex = Integer.toHexString(0xff & hashedByte);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // Hexadecimal string'i döndürün (hash değeri)
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Hashing algoritması bulunamazsa null döndürün (hata durumu)
            return null;
        }
    }

    public static List<String> getCountryList() {
        List<String> list = new ArrayList<>();
        list.add("United States of America");
        list.add("Italy");
        list.add("Japan");
        list.add("Turkey");
        list.add("Spain");
        list.add("Germany");
        list.add("France");
        list.add("Belgium");
        list.add("Bosnia and Herzegovina");
        list.add("Serbia");
        return list;
    }
}

