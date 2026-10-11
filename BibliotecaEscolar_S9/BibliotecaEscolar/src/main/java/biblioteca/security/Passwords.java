package biblioteca.security;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class Passwords {
  private Passwords() {}

  private static byte[] derive(char[] pass, byte[] salt, int rounds) throws Exception {
    PBEKeySpec spec = new PBEKeySpec(pass, salt, rounds, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } finally {
      spec.clearPassword();
    }
  }

  public static String hash(char[] pass) throws Exception {
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return "210000$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derive(pass, salt, 210000));
  }

  public static boolean verify(char[] pass, String encoded) throws Exception {
    String[] a = encoded.split("\\$");
    return a.length == 3
        && MessageDigest.isEqual(
            Base64.getDecoder().decode(a[2]),
            derive(pass, Base64.getDecoder().decode(a[1]), Integer.parseInt(a[0])));
  }
}
