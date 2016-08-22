package com.uniquemiban.travelmanager.verifications;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Verification {
    ///stuguma vor usernam@ minimum
    public  boolean usernameVerification(String username) {
        Pattern pt= Pattern.compile("([a-zA-Z0-9]){6,}");
        Matcher m=pt.matcher(username);
        return m.matches();
    }

    //stuguma vor chisht e-mail gren
    public  boolean emailVerification(String email){
        Pattern pt= Pattern.compile("([a-zA-Z0-9]{1,}[\\_]{0,1}[a-zA-Z0-9]{1,}[\\-]{0,1}[a-zA-Z0-9]{1,}[\\.]{0,1}[a-zA-Z0-9]{1,})+"+
                "@[a-zA-Z0-9]{1,}[\\_]{0,1}[a-zA-Z0-9]{1,}[\\-]{0,1}[a-zA-Z0-9]{1,}[\\.]{0,1}[a-zA-Z0-9]{1,}(([\\.]){1}[a-zA-Z]{2,4})+");
        Matcher m=pt.matcher(email);
        return m.matches();
    }

    //// stuguma vor password@ lini minimum 9 nish , unena 2 hat metsatar ev 3 hat tiv

    public  boolean passwordVerification(String password){
        boolean result=false;
        Pattern pt= Pattern.compile("[a-zA-z0-9]{9,}");
        Matcher m=pt.matcher(password);
        int upperCount=0;
        int numberCount=0;
        for(char c:password.toCharArray()){
            if(Character.isUpperCase(c))
                upperCount++;
            if(Character.isDigit(c))
                numberCount++;
        }
        if(upperCount>2 && numberCount>3)
            result=true;
        if(result=true && m.matches())
            return true;
        return  false;
    }
}
