package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.common.entities.DifficultyLevel;
import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.common.entities.QuestionRequest;
import co.edu.unicauca.microkernel.common.exceptions.QuestionValidationException;
import co.edu.unicauca.microkernel.common.interfaces.QuestionPlugin;
import co.edu.unicauca.microkernel.core.QuestionMicrokernel;
import co.edu.unicauca.microkernel.pipeline.filters.CompetencyCatalog;
import co.edu.unicauca.microkernel.pipeline.filters.OptionsValidationFilter;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Formulario para crear preguntas. Envía la solicitud al núcleo, que la delega al
 * plugin correspondiente según el tipo seleccionado.
 */
public class QuestionFormPanel extends JPanel {

    static final String[] QUESTION_TYPES = {
            ExampleQuestions.MULTIPLE_CHOICE, ExampleQuestions.CASE, ExampleQuestions.MULTIMEDIA
    };
    private static final String AUTOMATIC_DIFFICULTY = "Automática";
    private static final Color OK_COLOR = new Color(0x1B7F3B);
    private static final Color ERROR_COLOR = new Color(0xB3261E);

    private final QuestionMicrokernel kernel;
    private final EventLog log;
    private final Runnable onQuestionCreated;

    private final JComboBox<String> typeCombo = new JComboBox<>(QUESTION_TYPES);
    private final JLabel pluginLabel = new JLabel();
    private final JTextField titleField = new JTextField(40);
    private final JComboBox<String> areaCombo = new JComboBox<>(CompetencyCatalog.areas().toArray(new String[0]));
    private final JComboBox<String> difficultyCombo = new JComboBox<>(new String[]{
            AUTOMATIC_DIFFICULTY, DifficultyLevel.BAJO.getLabel(), DifficultyLevel.MEDIO.getLabel(),
            DifficultyLevel.ALTO.getLabel()});
    private final JTextArea contextArea = new JTextArea(3, 40);
    private final JComboBox<String> mediaTypeCombo = new JComboBox<>(new String[]{"IMAGEN", "AUDIO", "VIDEO"});
    private final JTextField mediaUrlField = new JTextField(40);
    private final JTextArea contentArea = new JTextArea(2, 40);
    private final JTextField[] optionFields = new JTextField[OptionsValidationFilter.REQUIRED_OPTIONS];
    private final JRadioButton[] correctButtons = new JRadioButton[OptionsValidationFilter.REQUIRED_OPTIONS];
    private final ButtonGroup correctGroup = new ButtonGroup();
    private final JTextArea justificationArea = new JTextArea(2, 40);
    private final JLabel statusLabel = new JLabel(" ");

    private final JButton generateButton = new JButton("Generar pregunta");
    private final JButton validExampleButton = new JButton("Cargar ejemplo válido");
    private final JButton invalidExampleButton = new JButton("Cargar ejemplo inválido");
    private final JButton clearButton = new JButton("Limpiar");

    private int nextInvalidScenario;

    public QuestionFormPanel(QuestionMicrokernel kernel, EventLog log, Runnable onQuestionCreated) {
        super(new BorderLayout(8, 8));
        this.kernel = kernel;
        this.log = log;
        this.onQuestionCreated = onQuestionCreated;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        typeCombo.setEditable(true);
        areaCombo.setEditable(true);
        for (int index = 0; index < optionFields.length; index++) {
            optionFields[index] = new JTextField(40);
            correctButtons[index] = new JRadioButton("Correcta");
            correctGroup.add(correctButtons[index]);
        }

        JPanel form = new JPanel();
        form.setLayout(new javax.swing.BoxLayout(form, javax.swing.BoxLayout.Y_AXIS));
        form.add(buildGeneralSection());
        form.add(buildTypeSection());
        form.add(buildQuestionSection());

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        typeCombo.addActionListener(event -> updateTypeDependentFields());
        generateButton.addActionListener(event -> generateQuestion());
        validExampleButton.addActionListener(event -> loadValidExample());
        invalidExampleButton.addActionListener(event -> loadNextInvalidExample());
        clearButton.addActionListener(event -> clear());

        updateTypeDependentFields();
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel buildGeneralSection() {
        JPanel panel = section("1. Datos generales");
        GridBagConstraints c = constraints();
        addRow(panel, c, 0, "Tipo de pregunta:", typeCombo);
        c.gridx = 2;
        c.weightx = 1;
        pluginLabel.setFont(pluginLabel.getFont().deriveFont(Font.BOLD));
        panel.add(pluginLabel, c);
        addRow(panel, c, 1, "Título:", titleField, 2);
        addRow(panel, c, 2, "Área (clasificación):", areaCombo);
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        difficultyPanel.add(new JLabel("Dificultad:"));
        difficultyPanel.add(difficultyCombo);
        c.gridx = 2;
        c.gridy = 2;
        c.weightx = 1;
        panel.add(difficultyPanel, c);
        return panel;
    }

    private JPanel buildTypeSection() {
        JPanel panel = section("2. Datos propios del tipo de pregunta");
        GridBagConstraints c = constraints();
        contextArea.setLineWrap(true);
        contextArea.setWrapStyleWord(true);
        addRow(panel, c, 0, "Caso (solo CASE):", new JScrollPane(contextArea), 2);
        JPanel mediaPanel = new JPanel(new BorderLayout(6, 0));
        mediaPanel.add(mediaTypeCombo, BorderLayout.WEST);
        mediaPanel.add(mediaUrlField, BorderLayout.CENTER);
        addRow(panel, c, 1, "Recurso (solo MULTIMEDIA):", mediaPanel, 2);
        return panel;
    }

    private JPanel buildQuestionSection() {
        JPanel panel = section("3. Pregunta, opciones y respuesta correcta");
        GridBagConstraints c = constraints();
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        addRow(panel, c, 0, "Enunciado:", new JScrollPane(contentArea), 2);
        for (int index = 0; index < optionFields.length; index++) {
            addRow(panel, c, index + 1, "Opción " + OptionsValidationFilter.letterOf(index) + ":", optionFields[index]);
            c.gridx = 2;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(correctButtons[index], c);
            c.fill = GridBagConstraints.HORIZONTAL;
        }
        justificationArea.setLineWrap(true);
        justificationArea.setWrapStyleWord(true);
        addRow(panel, c, optionFields.length + 1, "Justificación:", new JScrollPane(justificationArea), 2);
        return panel;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        generateButton.setFont(generateButton.getFont().deriveFont(Font.BOLD));
        buttons.add(generateButton);
        buttons.add(validExampleButton);
        buttons.add(invalidExampleButton);
        buttons.add(clearButton);
        actions.add(buttons, BorderLayout.NORTH);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        actions.add(statusLabel, BorderLayout.SOUTH);
        return actions;
    }

    private static JPanel section(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(2, 6, 4, 6)));
        return panel;
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label, java.awt.Component field) {
        addRow(panel, c, row, label, field, 1);
    }

    private static void addRow(JPanel panel, GridBagConstraints c, int row, String label,
                               java.awt.Component field, int width) {
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 1;
        c.weightx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        c.gridwidth = width;
        c.weightx = 1;
        panel.add(field, c);
        c.gridwidth = 1;
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    /** Envía la solicitud al núcleo para que el plugin correspondiente genere la pregunta. */
    public void generateQuestion() {
        String type = selectedType();
        QuestionRequest request = readRequest();
        log.log("Solicitud de tipo " + type + " enviada al núcleo.");
        try {
            Question question = kernel.executePlugin(type, request);
            log.log("Plugin '" + question.getGeneratedBy() + "' ejecutó el pipeline:");
            question.getProcessingTrace().forEach(step -> log.log("    " + step));
            log.log("Pregunta agregada al banco con ID " + question.getId()
                    + " | competencia: " + question.getCompetency()
                    + " | dificultad: " + question.getDifficulty().getLabel());
            showStatus("Pregunta creada por el plugin '" + question.getGeneratedBy() + "' con ID "
                    + shortId(question.getId()) + ". Total en el banco: " + kernel.getQuestionCount(), true);
            onQuestionCreated.run();
        } catch (QuestionValidationException ex) {
            log.log("La solicitud fue rechazada por el pipeline:");
            ex.getTrace().forEach(step -> log.log("    " + step));
            showStatus("Rechazada por " + ex.getFilterName() + ": " + ex.getReason(), false);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.log("Error: " + ex.getMessage());
            showStatus(ex.getMessage(), false);
        }
    }

    /** Llena el formulario con una pregunta válida del tipo seleccionado. */
    public void loadValidExample() {
        fill(ExampleQuestions.valid(selectedType()));
        showStatus("Ejemplo válido de tipo " + selectedType() + " cargado. Presione \"Generar pregunta\".", true);
    }

    /** Llena el formulario con el siguiente escenario inválido (uno por cada filtro). */
    public void loadNextInvalidExample() {
        List<ExampleQuestions.Scenario> scenarios = ExampleQuestions.invalidScenarios();
        ExampleQuestions.Scenario scenario = scenarios.get(nextInvalidScenario % scenarios.size());
        nextInvalidScenario++;
        fill(scenario.getRequest());
        showStatus("Ejemplo inválido: " + scenario.getDescription() + ". Presione \"Generar pregunta\".", false);
    }

    /** Limpia el formulario. */
    public void clear() {
        titleField.setText("");
        contextArea.setText("");
        mediaUrlField.setText("");
        contentArea.setText("");
        justificationArea.setText("");
        for (JTextField field : optionFields) {
            field.setText("");
        }
        correctGroup.clearSelection();
        difficultyCombo.setSelectedIndex(0);
        areaCombo.setSelectedIndex(0);
        showStatus(" ", true);
    }

    /** Selecciona un tipo de pregunta en el formulario. */
    public void selectType(String type) {
        typeCombo.setSelectedItem(type);
        updateTypeDependentFields();
    }

    /** Actualiza la etiqueta del plugin y habilita los campos según el tipo. */
    public void updateTypeDependentFields() {
        String type = selectedType();
        Optional<QuestionPlugin> plugin = kernel.findPlugin(type);
        if (plugin.isPresent()) {
            pluginLabel.setText("Lo atenderá el plugin: " + plugin.get().getName()
                    + " (" + plugin.get().getClass().getSimpleName() + ")");
            pluginLabel.setForeground(OK_COLOR);
        } else {
            pluginLabel.setText("Ningún plugin activo soporta el tipo \"" + type + "\"");
            pluginLabel.setForeground(ERROR_COLOR);
        }
        boolean isCase = ExampleQuestions.CASE.equalsIgnoreCase(type);
        boolean isMultimedia = ExampleQuestions.MULTIMEDIA.equalsIgnoreCase(type);
        setEditable(contextArea, isCase);
        mediaTypeCombo.setEnabled(isMultimedia);
        mediaUrlField.setEnabled(isMultimedia);
    }

    // ------------------------------------------------------------------
    // Conversión formulario <-> solicitud
    // ------------------------------------------------------------------

    /** Construye la solicitud con los datos del formulario. */
    QuestionRequest readRequest() {
        List<String> options = new ArrayList<>();
        String correctAnswer = "";
        for (int index = 0; index < optionFields.length; index++) {
            options.add(optionFields[index].getText());
            if (correctButtons[index].isSelected()) {
                correctAnswer = optionFields[index].getText();
            }
        }
        String type = selectedType();
        QuestionRequest.Builder builder = QuestionRequest.builder()
                .type(type)
                .title(titleField.getText())
                .content(contentArea.getText())
                .classification(String.valueOf(areaCombo.getEditor().getItem()))
                .options(options)
                .correctAnswer(correctAnswer)
                .justification(justificationArea.getText())
                .difficulty(DifficultyLevel.parse(String.valueOf(difficultyCombo.getSelectedItem())).orElse(null));
        if (ExampleQuestions.CASE.equalsIgnoreCase(type)) {
            builder.context(contextArea.getText());
        }
        if (ExampleQuestions.MULTIMEDIA.equalsIgnoreCase(type)) {
            builder.mediaType(String.valueOf(mediaTypeCombo.getSelectedItem()))
                    .mediaUrl(mediaUrlField.getText());
        }
        return builder.build();
    }

    /** Llena el formulario con los datos de una solicitud. */
    void fill(QuestionRequest request) {
        clear();
        if (request.getType() != null) {
            selectType(request.getType());
        }
        titleField.setText(nullSafe(request.getTitle()));
        areaCombo.setSelectedItem(nullSafe(request.getClassification()));
        contentArea.setText(nullSafe(request.getContent()));
        contextArea.setText(nullSafe(request.getContext()));
        justificationArea.setText(nullSafe(request.getJustification()));
        if (request.getMediaType() != null) {
            mediaTypeCombo.setSelectedItem(request.getMediaType());
        }
        mediaUrlField.setText(nullSafe(request.getMediaUrl()));
        difficultyCombo.setSelectedItem(request.getDifficulty() == null
                ? AUTOMATIC_DIFFICULTY : request.getDifficulty().getLabel());
        List<String> options = request.getOptions() == null ? List.of() : request.getOptions();
        for (int index = 0; index < optionFields.length && index < options.size(); index++) {
            optionFields[index].setText(options.get(index));
            if (options.get(index) != null && options.get(index).equals(request.getCorrectAnswer())) {
                correctButtons[index].setSelected(true);
            }
        }
    }

    private String selectedType() {
        Object item = typeCombo.getEditor().getItem();
        Object selected = item == null || String.valueOf(item).isBlank() ? typeCombo.getSelectedItem() : item;
        return selected == null ? "" : String.valueOf(selected).trim().toUpperCase();
    }

    private void showStatus(String message, boolean ok) {
        statusLabel.setText(message);
        statusLabel.setForeground(ok ? OK_COLOR : ERROR_COLOR);
    }

    private static void setEditable(JTextArea area, boolean editable) {
        area.setEnabled(editable);
        area.setBackground(editable
                ? UIManager.getColor("TextArea.background")
                : UIManager.getColor("Panel.background"));
    }

    private static String shortId(String id) {
        return id.length() > 8 ? id.substring(0, 8) + "..." : id;
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
