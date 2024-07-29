package helper;

import java.util.Base64;

public class HashPass {

    public static String hashPass(String pass) {
        return Base64.getEncoder().encodeToString(pass.getBytes());
    }

    public static String decodedHashPass(String hashPass) {
        byte[] decodedBytes = Base64.getDecoder().decode(hashPass);
        return new String(decodedBytes);
    }

}
