package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import logica.*;

import java.awt.*;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class VentanaPrincipal extends JFrame {
    private JTextField txtIpInicio, txtIpFin;
    private JButton btnEscanear, btnLimpiar, btnGuardar;

    // Filtros
    private JTextField txtFiltro;
    private JComboBox<String> cmbEstado;
    private JButton btnAplicarFiltro, btnQuitarFiltro;
    private TableRowSorter<DefaultTableModel> sorter;

    // Tabla y progreso
    private JTable tabla;
    private DefaultTableModel modelo;
    private JProgressBar barra;

    public VentanaPrincipal() {
        setTitle("Escáner de Red");
        setSize(800, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // ---------- Panel Superior (IPs y acciones) ----------
        JPanel panelSuperior = new JPanel(new GridLayout(2, 4, 6, 6));
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
        panelSuperior.add(new JLabel("")); // relleno

        add(panelSuperior, BorderLayout.NORTH);

        // ---------- Tabla ----------
        modelo = new DefaultTableModel(new String[] { "IP", "Nombre", "Estado", "Tiempo (ms)" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tabla = new JTable(modelo);
        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ---------- Panel de Filtros ----------
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        txtFiltro = new JTextField(20);
        cmbEstado = new JComboBox<>(new String[] { "Todos", "Conectado", "No conectado" });
        btnAplicarFiltro = new JButton("Filtrar");
        btnQuitarFiltro = new JButton("Quitar filtro");

        panelFiltros.add(new JLabel("Filtro texto:"));
        panelFiltros.add(txtFiltro);
        panelFiltros.add(new JLabel("Estado:"));
        panelFiltros.add(cmbEstado);
        panelFiltros.add(btnAplicarFiltro);
        panelFiltros.add(btnQuitarFiltro);

        add(panelFiltros, BorderLayout.WEST);

        // ---------- Barra de Progreso ----------
        barra = new JProgressBar();
        add(barra, BorderLayout.SOUTH);

        // ---------- Listeners ----------
        btnEscanear.addActionListener(e -> escanear());
        btnLimpiar.addActionListener(e -> limpiar());
        btnGuardar.addActionListener(e -> guardar());
        btnAplicarFiltro.addActionListener(e -> aplicarFiltro());
        btnQuitarFiltro.addActionListener(e -> quitarFiltro());

        setVisible(true);
    }

    // ----- Lógica de escaneo -----
    private void escanear() {
        String inicio = txtIpInicio.getText().trim();
        String fin = txtIpFin.getText().trim();

        if (!ValidadorIP.esValida(inicio) || !ValidadorIP.esValida(fin)) {
            JOptionPane.showMessageDialog(this, "IPs inválidas");
            return;
        }

        // Limpiar resultados anteriores
        modelo.setRowCount(0);
        barra.setValue(0);

        // Hilo para no congelar la GUI
        new Thread(() -> {
            Escaner escaner = new Escaner();
            List<Equipo> lista = escaner.escanearRango(inicio, fin, barra);

            // Cargar resultados a la tabla
            for (Equipo eq : lista) {
                modelo.addRow(new Object[] {
                        eq.getIp(),
                        eq.getNombre(),
                        eq.isConectado() ? "Conectado" : "No conectado",
                        eq.getTiempo()
                });
            }
        }).start();
    }

    // ----- Filtros -----
    private void aplicarFiltro() {
        String texto = txtFiltro.getText().trim();
        String estado = (String) cmbEstado.getSelectedItem();

        List<RowFilter<Object, Object>> filtros = new ArrayList<>();

        // Filtro por texto (aplica a todas las columnas)
        if (!texto.isEmpty()) {
            try {
                filtros.add(RowFilter.regexFilter("(?i)" + PatternQuote(texto)));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Expresión de filtro inválida.");
                return;
            }
        }

        // Filtro por estado (columna 2 = "Estado")
        if (estado != null && !estado.equals("Todos")) {
            filtros.add(RowFilter.regexFilter("^" + PatternQuote(estado) + "$", 2));
        }

        if (filtros.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filtros));
        }
    }

    private void quitarFiltro() {
        txtFiltro.setText("");
        cmbEstado.setSelectedIndex(0);
        sorter.setRowFilter(null);
    }

    // Escapa texto para usar en regex literal
    private String PatternQuote(String s) {
        // Evita que caracteres especiales de regex rompan el filtro
        return java.util.regex.Pattern.quote(s);
    }

    // ----- Utilidades -----
    private void limpiar() {
        txtIpInicio.setText("");
        txtIpFin.setText("");
        modelo.setRowCount(0);
        barra.setValue(0);
        quitarFiltro();
    }

    private void guardar() {
        try (FileWriter fw = new FileWriter("resultados.txt")) {
            for (int i = 0; i < modelo.getRowCount(); i++) {
                fw.write(
                        modelo.getValueAt(i, 0) + " - " +
                        modelo.getValueAt(i, 1) + " - " +
                        modelo.getValueAt(i, 2) + " - " +
                        modelo.getValueAt(i, 3) + " ms\n"
                );
            }
            JOptionPane.showMessageDialog(this, "Archivo guardado como resultados.txt");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al guardar archivo");
        }
    }
}
