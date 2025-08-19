package logica;

public class ValidadorIP {
    public static boolean esValida(String ip) {
        String[] partes = ip.split("\\.");
        if (partes.length != 4) return false;

        try {
            for (String parte : partes) {
                int num = Integer.parseInt(parte);
                if (num < 0 || num > 255) return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
