package logica;

public class Equipo {
    private String ip;
    private String nombre;
    private boolean conectado;
    private long tiempo;

    public Equipo(String ip, String nombre, boolean conectado, long tiempo) {
        this.ip = ip;
        this.nombre = nombre;
        this.conectado = conectado;
        this.tiempo = tiempo;
    }

    public String getIp() { return ip; }
    public String getNombre() { return nombre; }
    public boolean isConectado() { return conectado; }
    public long getTiempo() { return tiempo; }
}
