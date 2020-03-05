package com.example.instaspam.Utils;

public class StringManipulation {

    public static String expandUsername(String username){

        return username.replace(".", " ");
    }

    public static String condenseUsername(String username) {
        return username.replace(" ", ".");
    }


    public static String getTags(String string) {

        if(string.indexOf('#') > 0) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;

            for (char c : charArray) {
                if (c == '#') {
                    foundWord = true;
                    sb.append(c);
                } else {
                    if (foundWord) {
                        sb.append(c);
                    }
                }

                if (c == ' ') foundWord = false;

            }
            String s = sb.toString().replace(" ", "").replace("#", ",#");


            return s.substring(1, s.length());
        }
        return string;
    }
    /*
    some desc #tag1 #tag2 #othertag
    sb = #tag1#tag2#othertag
    tag1, #tag2, #tag3
     */



}
