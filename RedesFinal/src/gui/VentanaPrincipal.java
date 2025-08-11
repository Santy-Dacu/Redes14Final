package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import logica.*;
import java.awt.*;
import java.io.FileWriter;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private JTextField txtIpInicio, txtIpFin;
    private JButton btnEscanear, btnLimpiar, btnGuardar;
    private JTable tabla;
    private JProgressBar barra;
    private DefaultTableModel modelo;

    public VentanaPrincipal() {
        setTitle("Escáner de Red");
        setSize(700, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Panel superior con campos y botones
        JPanel panelSuperior = new JPanel(new GridLayout(2, 4));
        txtIpInicio = new JTextField();
        txtIpFin = new JTextField();
        btnEscanear = new JButton("Escanear");
        btnLimpiar = new JButton("Limpiar");
        btnGuardar = new JButton("Guardar");

        panelSuperior.add(new JLabel("IP Inicio:"));
        panelSuperior.add(txtIpInicio);
        panelSuperior.add(new JLabel("IP Fin:"));
        panelSuperior.add(txtIpFin);
        panelSuperior.add(btnEscanear);
        panelSuperior.add(btnLimpiar);
        panelSuperior.add(btnGuardar);

        // Tabla para mostrar los resultados
        modelo = new DefaultTableModel(new String[]{"IP", "Nombre", "Estado", "Tiempo (ms)"}, 0);
        tabla = new JTable(modelo);
        JScrollPane scroll = new JScrollPane(tabla);

        // Barra de progreso
        barra = new JProgressBar();

        // Agregar todo a la ventana
        add(panelSuperior, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(barra, BorderLayout.SOUTH);

        // Acciones de los botones
        btnEscanear.addActionListener(e -> escanear());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());

        setVisible(true);
    }

    // Escanea el rango de IPs
    private void escanear() {
        String inicio = txtIpInicio.getText();
        String fin = txtIpFin.getText();

        // Validar IPs
        if (!ValidadorIP.esValida(inicio) || !ValidadorIP.esValida(fin)) {
            JOptionPane.showMessageDialog(this, "IPs inválidas");
            return;
        }

        // Limpiar la tabla antes de iniciar
        modelo.setRowCount(0);

        // Hacer el escaneo en un hilo separado para no trabar la interfaz
        new Thread(() -> {
            Escaner escaner = new Escaner();
            List<Equipo> lista = escaner.escanearRango(inicio, fin, barra);

            for (Equipo eq : lista) {
                modelo.addRow(new Object[]{
                    eq.getIp(),
                    eq.getNombre(),
                    eq.isConectado() ? "Conectado" : "No conectado",
                    eq.getTiempo()
                });
            }
        }).start();
    }

    // Limpia campos y tabla
    private void limpiar() {
        txtIpInicio.setText("");
        txtIpFin.setText("");
        modelo.setRowCount(0);
        barra.setValue(0);
    }

    // Guarda los resultados en un archivo .txt
    private void guardar() {
        try {
            FileWriter fw = new FileWriter("resultados.txt");
            for (int i = 0; i < modelo.getRowCount(); i++) {
                fw.write(
                    modelo.getValueAt(i, 0) + " - " +
                    modelo.getValueAt(i, 1) + " - " +
                    modelo.getValueAt(i, 2) + " - " +
                    modelo.getValueAt(i, 3) + " ms\n"
                );
            }
            fw.close();
            JOptionPane.showMessageDialog(this, "Archivo guardado como resultados.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar archivo");
        }
    }
}
	