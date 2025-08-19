package logica;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Escaner {

    public List<Equipo> escanearRango(String ipInicio, String ipFin, JProgressBar barra) {
        List<Equipo> lista = new ArrayList<>();
        int start = obtenerUltimo(ipInicio);
        int end = obtenerUltimo(ipFin);
        String base = obtenerBase(ipInicio);

        barra.setMinimum(0);
        barra.setMaximum(end - start + 1);

        for (int i = start; i <= end; i++) {
            String ipActual = base + i;
            long tiempo = System.currentTimeMillis();
            boolean conectado = hacerPing(ipActual);
            tiempo = System.currentTimeMillis() - tiempo;

            String nombre = conectado ? obtenerNombre(ipActual) : "-";
            lista.add(new Equipo(ipActual, nombre, conectado, tiempo));
            barra.setValue(i - start + 1);
        }

        return lista;
    }

    private boolean hacerPing(String ip) {
        try {
            Process proceso = Runtime.getRuntime().exec("ping -n 1 " + ip);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.contains("tiempo=") || linea.contains("TTL=")) return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private String obtenerNombre(String ip) {
        try {
            Process proceso = Runtime.getRuntime().exec("nslookup " + ip);
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.toLowerCase().contains("name") || linea.toLowerCase().contains("nombre")) {
                    return linea.split(":")[1].trim();
                }
            }
        } catch (Exception e) {
            return "-";
        }
        return "-";
    }

    private String obtenerBase(String ip) {
        return ip.substring(0, ip.lastIndexOf(".") + 1);
    }

    private int obtenerUltimo(String ip) {
        return Integer.parseInt(ip.split("\\.")[3]);
    }
}
