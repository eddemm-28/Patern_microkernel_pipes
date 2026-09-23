package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.core.PluginDescriptor;
import co.edu.unicauca.microkernel.core.PluginState;
import co.edu.unicauca.microkernel.core.QuestionMicrokernel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Administración del ciclo de vida de los plugins del núcleo: consulta, activación,
 * desactivación, descarga, recarga desde plugins.properties y registro en caliente
 * por reflexión.
 */
public class PluginPanel extends JPanel {

    private final QuestionMicrokernel kernel;
    private final EventLog log;
    private final Runnable onPluginsChanged;
    private final PluginTableModel model = new PluginTableModel();
    private final JTable table = new JTable(model);
    private final JTextField keyField = new JTextField(12);
    private final JTextField classField = new JTextField(38);

    public PluginPanel(QuestionMicrokernel kernel, EventLog log, Runnable onPluginsChanged) {
        super(new BorderLayout(8, 8));
        this.kernel = kernel;
        this.log = log;
        this.onPluginsChanged = onPluginsChanged;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel info = new JLabel("<html>El núcleo lee <b>" + kernel.getConfigurationResource()
                + "</b> y crea cada plugin por <b>reflexión</b> "
                + "(<code>Class.forName(clase).getDeclaredConstructor().newInstance()</code>). "
                + "Solo conoce el contrato <b>QuestionPlugin</b>; los plugins se pueden activar, "
                + "desactivar, descargar o registrar sin modificar el núcleo.</html>");
        info.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
        add(info, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(360);
        table.getColumnModel().getColumn(5).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setCellRenderer(new StateRenderer());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton activate = new JButton("Activar");
        JButton deactivate = new JButton("Desactivar");
        JButton unload = new JButton("Descargar");
        JButton reload = new JButton("Recargar plugins.properties");
        activate.addActionListener(event -> onSelected("activar", key -> {
            kernel.activatePlugin(key);
            log.log("Plugin '" + key + "' activado.");
        }));
        deactivate.addActionListener(event -> onSelected("desactivar", key -> {
            kernel.deactivatePlugin(key);
            log.log("Plugin '" + key + "' desactivado: el núcleo ya no lo usará.");
        }));
        unload.addActionListener(event -> onSelected("descargar", key -> {
            kernel.unregisterPlugin(key);
            log.log("Plugin '" + key + "' descargado del núcleo.");
        }));
        reload.addActionListener(event -> reloadPlugins());

        JPanel lifecycle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        lifecycle.setBorder(BorderFactory.createTitledBorder("Ciclo de vida del plugin seleccionado"));
        lifecycle.add(activate);
        lifecycle.add(deactivate);
        lifecycle.add(unload);
        lifecycle.add(reload);

        JButton register = new JButton("Registrar por reflexión");
        register.addActionListener(event -> registerPlugin());
        JPanel registration = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        registration.setBorder(BorderFactory.createTitledBorder("Registrar un plugin en caliente"));
        registration.add(new JLabel("Clave:"));
        registration.add(keyField);
        registration.add(new JLabel("Clase:"));
        registration.add(classField);
        registration.add(register);

        JPanel south = new JPanel(new BorderLayout());
        south.add(lifecycle, BorderLayout.NORTH);
        south.add(registration, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);

        refresh();
    }

    /** Vuelve a leer los plugins registrados en el núcleo. */
    public void refresh() {
        model.setDescriptors(kernel.getPluginDescriptors());
    }

    /** Recarga los plugins desde el archivo de configuración. */
    public void reloadPlugins() {
        List<PluginDescriptor> descriptors = kernel.reloadPlugins();
        log.log("Configuración recargada: " + descriptors.size() + " plugin(s) registrados.");
        if (kernel.getConfigurationError() != null) {
            log.log("Error de configuración: " + kernel.getConfigurationError());
        }
        changed();
    }

    /** Registra en el núcleo la clase escrita en el formulario. */
    public void registerPlugin() {
        String key = keyField.getText().trim();
        String className = classField.getText().trim();
        if (key.isEmpty() || className.isEmpty()) {
            log.log("Indique la clave y el nombre completo de la clase del plugin.");
            return;
        }
        PluginDescriptor descriptor = kernel.registerPlugin(key, className);
        log.log("Registro de '" + key + "' (" + className + "): " + descriptor.getState().getLabel()
                + " - " + descriptor.getMessage());
        changed();
    }

    /** Prepara el formulario de registro (usado por las demostraciones). */
    public void setRegistrationData(String key, String className) {
        keyField.setText(key);
        classField.setText(className);
    }

    private void onSelected(String action, Consumer<String> operation) {
        int row = table.getSelectedRow();
        if (row < 0) {
            log.log("Seleccione un plugin de la tabla para " + action + ".");
            return;
        }
        String key = model.getDescriptor(row).getKey();
        try {
            operation.accept(key);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.log("No fue posible " + action + " el plugin: " + ex.getMessage());
        }
        changed();
    }

    private void changed() {
        refresh();
        onPluginsChanged.run();
    }

    /**
     * Modelo de la tabla de plugins.
     */
    private static final class PluginTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Clave", "Clase", "Nombre", "Estado", "Ejecuciones", "Mensaje"};
        private List<PluginDescriptor> descriptors = new ArrayList<>();

        void setDescriptors(List<PluginDescriptor> descriptors) {
            this.descriptors = new ArrayList<>(descriptors);
            fireTableDataChanged();
        }

        PluginDescriptor getDescriptor(int row) {
            return descriptors.get(row);
        }

        @Override
        public int getRowCount() {
            return descriptors.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            PluginDescriptor descriptor = descriptors.get(row);
            switch (column) {
                case 0:
                    return descriptor.getKey();
                case 1:
                    return descriptor.getClassName();
                case 2:
                    return descriptor.getPluginName();
                case 3:
                    return descriptor.getState();
                case 4:
                    return descriptor.getExecutions();
                default:
                    return descriptor.getMessage();
            }
        }
    }

    /**
     * Pinta el estado del plugin con un color distinto.
     */
    private static final class StateRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            PluginState state = (PluginState) value;
            super.getTableCellRendererComponent(table, state.getLabel(), isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                switch (state) {
                    case ACTIVO:
                        setForeground(new Color(0x1B7F3B));
                        break;
                    case INACTIVO:
                        setForeground(Color.GRAY);
                        break;
                    default:
                        setForeground(new Color(0xB3261E));
                }
            }
            return this;
        }
    }
}
