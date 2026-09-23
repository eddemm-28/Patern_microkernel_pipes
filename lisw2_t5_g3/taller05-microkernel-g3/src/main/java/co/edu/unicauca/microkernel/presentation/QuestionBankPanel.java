package co.edu.unicauca.microkernel.presentation;

import co.edu.unicauca.microkernel.common.entities.Question;
import co.edu.unicauca.microkernel.core.QuestionMicrokernel;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Muestra el banco de preguntas almacenado en el núcleo, con filtro por tipo (RF-07).
 */
public class QuestionBankPanel extends JPanel {

    private static final String ALL = "Todos";

    private final QuestionMicrokernel kernel;
    private final JComboBox<String> typeFilter;
    private final JLabel countLabel = new JLabel();
    private final QuestionTableModel model = new QuestionTableModel();
    private final JTable table = new JTable(model);
    private final JTextArea detailArea = new JTextArea();

    public QuestionBankPanel(QuestionMicrokernel kernel) {
        super(new BorderLayout(8, 8));
        this.kernel = kernel;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        List<String> filterOptions = new ArrayList<>();
        filterOptions.add(ALL);
        filterOptions.addAll(List.of(QuestionFormPanel.QUESTION_TYPES));
        typeFilter = new JComboBox<>(filterOptions.toArray(new String[0]));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Filtrar por tipo:"));
        top.add(typeFilter);
        top.add(countLabel);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelected();
            }
        });

        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Detalle de la pregunta seleccionada"));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), detailScroll);
        split.setResizeWeight(0.45);
        add(split, BorderLayout.CENTER);

        typeFilter.addActionListener(event -> refresh());
        refresh();
    }

    /** Vuelve a leer el banco de preguntas del núcleo. */
    public void refresh() {
        String type = String.valueOf(typeFilter.getSelectedItem());
        List<Question> questions = ALL.equals(type)
                ? new ArrayList<>(kernel.getQuestions().values())
                : kernel.findQuestionsByType(type);
        model.setQuestions(questions);
        countLabel.setText("   Preguntas en el banco: " + kernel.getQuestionCount()
                + "   (mostrando " + questions.size() + ")");
        if (!questions.isEmpty()) {
            table.setRowSelectionInterval(questions.size() - 1, questions.size() - 1);
        } else {
            detailArea.setText("");
        }
    }

    /** Selecciona una fila de la tabla. */
    public void selectRow(int row) {
        if (row >= 0 && row < model.getRowCount()) {
            table.setRowSelectionInterval(row, row);
        }
    }

    private void showSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            detailArea.setText(QuestionFormatter.format(model.getQuestion(table.convertRowIndexToModel(row))));
            detailArea.setCaretPosition(0);
        }
    }

    /**
     * Modelo de la tabla de preguntas.
     */
    private static final class QuestionTableModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"ID", "Tipo", "Título", "Área", "Competencia", "Dificultad", "Plugin"};
        private List<Question> questions = new ArrayList<>();

        void setQuestions(List<Question> questions) {
            this.questions = new ArrayList<>(questions);
            fireTableDataChanged();
        }

        Question getQuestion(int row) {
            return questions.get(row);
        }

        @Override
        public int getRowCount() {
            return questions.size();
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
            Question question = questions.get(row);
            switch (column) {
                case 0:
                    return question.getId().substring(0, Math.min(8, question.getId().length()));
                case 1:
                    return question.getType();
                case 2:
                    return question.getTitle();
                case 3:
                    return question.getClassification();
                case 4:
                    return question.getCompetency();
                case 5:
                    return question.getDifficulty() == null ? "-" : question.getDifficulty().getLabel();
                default:
                    return question.getGeneratedBy();
            }
        }
    }
}
