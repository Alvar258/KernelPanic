package authn;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // Coste de trabajo para BCrypt (entre 10 y 31)
    private static final int WORK_FACTOR = 12;

    /**
     * Genera un hash seguro de la contraseña usando BCrypt
     * @param plainTextPassword La contraseña en texto plano
     * @return El hash de la contraseña
     */
    public static String hashPassword(String plainTextPassword) {
        if (plainTextPassword == null || plainTextPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifica si una contraseña coincide con su hash
     * @param plainTextPassword La contraseña en texto plano
     * @param hashedPassword El hash almacenado
     * @return true si la contraseña coincide, false en caso contrario
     */
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
} 