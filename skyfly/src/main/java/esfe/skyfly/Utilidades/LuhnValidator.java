package esfe.skyfly.Utilidades;

public class LuhnValidator {

    /**
     * Valida un número de tarjeta con el algoritmo de Luhn
     * @param numeroTarjeta El número de tarjeta como String
     * @return true si es válido, false si no
     */
    public static boolean isValid(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.isEmpty())
            return false;

        numeroTarjeta = numeroTarjeta.replaceAll("\\s", ""); // quitar espacios

        int suma = 0;
        boolean alternar = false;

        for (int i = numeroTarjeta.length() - 1; i >= 0; i--) {
            char c = numeroTarjeta.charAt(i);
            if (!Character.isDigit(c)) return false;

            int n = c - '0';
            if (alternar) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            suma += n;
            alternar = !alternar;
        }
        return suma % 10==0;
    }
}