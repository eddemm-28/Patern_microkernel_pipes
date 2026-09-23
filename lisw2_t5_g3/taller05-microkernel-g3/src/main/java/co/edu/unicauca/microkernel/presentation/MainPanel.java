package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.core.PluginDescriptor;
import co.edu.unicauca.microkernel.core.QuestionMicrokernel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Panel principal de la aplicación: pestañas para crear preguntas, consultar el
 * banco y administrar los plugins, más la bitácora de eventos del núcleo.
 */
public class MainPanel extends JPanel implements EventLog {

    public static final int TAB_FORM = 0;
    public static final int TAB_BANK = 1;
    public static final int TAB_PLUGINS = 2;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final JTextArea logArea = new JTextArea(7, 80);
    private final JTabbedPane tabs = new JTabbedPane();
    private final QuestionFormPanel formPanel;
    private final QuestionBankPanel bankPanel;
    private final PluginPanel pluginPanel;

    public MainPanel(QuestionMicrokernel kernel) {
        super(new BorderLayout());

        JLabel header = new JLabel("<html><span style='font-size:15pt'><b>Banco de Preguntas Saber PRO</b></span>"
                + "<br>Arquitectura Microkernel + Tuberías y Filtros &nbsp;·&nbsp; "
                + "Laboratorio de Ingeniería de Software II &nbsp;·&nbsp; Grupo 3</html>");
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        add(header, BorderLayout.NORTH);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        bankPanel = new QuestionBankPanel(kernel);
        formPanel = new QuestionFormPanel(kernel, this, bankPanel::refresh);
        pluginPanel = new PluginPanel(kernel, this, formPanel::updateTypeDependentFields);

        tabs.addTab("Crear pregunta", formPanel);
        tabs.addTab("Banco de preguntas", bankPanel);
        tabs.addTab("Plugins del núcleo", pluginPanel);
        tabs.addChangeListener(event -> {
            if (tabs.getSelectedIndex() == TAB_PLUGINS) {
                pluginPanel.refresh();
            }
        });

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Bitácora del núcleo y del pipeline"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, logScroll);
        split.setResizeWeight(0.78);
        split.setBorder(BorderFactory.createEmptyBorder(0, 6, 6, 6));
        add(split, BorderLayout.CENTER);

        logStartup(kernel);
    }

    @Override
    public void log(String message) {
        logArea.append(LocalTime.now().format(TIME_FORMAT) + "  " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /** Muestra la pestaña indicada. */
    public void selectTab(int index) {
        tabs.setSelectedIndex(index);
    }

    public QuestionFormPanel getFormPanel() {
        return formPanel;
    }

    public QuestionBankPanel getBankPanel() {
        return bankPanel;
    }

    public PluginPanel getPluginPanel() {
        return pluginPanel;
    }

    private void logStartup(QuestionMicrokernel kernel) {
        log("Núcleo iniciado. Plugins leídos de " + kernel.getConfigurationResource() + " y cargados por reflexión:");
        if (kernel.getConfigurationError() != null) {
            log("    " + kernel.getConfigurationError());
        }
        for (PluginDescriptor descriptor : kernel.getPluginDescriptors()) {
            log("    " + descriptor.getKey() + " -> " + descriptor.getClassName() + " [" + descriptor.getState().getLabel() + "]");
        }
    }
}
