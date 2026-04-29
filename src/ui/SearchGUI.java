package ui;

import model.Activity;
import service.ActivityService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SearchGUI extends JDialog {

    private static final String[] CATEGORIES = {"Type", "Date", "Collaborator", "Keyword"};

    private static final String[] ACTIVITY_TYPES = {
            "Running", "Swimming", "Gym", "Cycling", "Yoga", "Hiking", "Other"
    };

    private static final String[] RESULT_COLUMNS = {
            "Type", "Date", "Duration (min)", "Collaborators", "Quality", "Notes"
    };

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ActivityService service;

    private final JComboBox<String> categoryCombo;
    private final JPanel inputCard;
    private final CardLayout inputLayout;
    private final JTextField textInput;
    private final JTextField dateInput;
    private final JComboBox<String> typeCombo;

    private final DefaultTableModel resultsModel;

    public SearchGUI(JFrame owner, ActivityService service) {
        super(owner, "Search Activities", false);
        this.service = service;

        setSize(720, 460);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(8, 8));

        JPanel queryPanel = new JPanel(new GridBagLayout());
        queryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0;
        queryPanel.add(new JLabel("Category:"), g);

        this.categoryCombo = new JComboBox<>(CATEGORIES);
        g.gridx = 1; g.gridy = 0;
        queryPanel.add(categoryCombo, g);

        g.gridx = 0; g.gridy = 1;
        queryPanel.add(new JLabel("Search for:"), g);

        this.inputLayout = new CardLayout();
        this.inputCard   = new JPanel(inputLayout);
        this.textInput   = new JTextField(20);
        this.dateInput   = new JTextField(20);
        this.typeCombo   = new JComboBox<>(ACTIVITY_TYPES);

        dateInput.setToolTipText("Format: yyyy-MM-dd");

        inputCard.add(typeCombo, "Type");
        inputCard.add(dateInput, "Date");
        inputCard.add(textInput, "Text");

        g.gridx = 1; g.gridy = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;
        queryPanel.add(inputCard, g);

        JButton searchBtn = new JButton("Search");
        g.gridx = 2; g.gridy = 1; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        queryPanel.add(searchBtn, g);

        add(queryPanel, BorderLayout.NORTH);

        this.resultsModel = new DefaultTableModel(RESULT_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable resultsTable = new JTable(resultsModel);
        resultsTable.setRowHeight(22);
        resultsTable.setAutoCreateRowSorter(true);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        categoryCombo.addActionListener(e -> updateInputField());
        searchBtn.addActionListener(e -> onSearch());
        textInput.addActionListener(e -> onSearch());
        dateInput.addActionListener(e -> onSearch());

        updateInputField();
    }

    private void updateInputField() {
        String cat = (String) categoryCombo.getSelectedItem();
        if (cat == null) return;
        switch (cat) {
            case "Type":          inputLayout.show(inputCard, "Type"); break;
            case "Date":          inputLayout.show(inputCard, "Date"); break;
            case "Collaborator":
            case "Keyword":
            default:              inputLayout.show(inputCard, "Text"); break;
        }
    }

    private void onSearch() {
        String category = (String) categoryCombo.getSelectedItem();
        String keyword  = currentKeyword();

        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a search value.",
                    "Empty query", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Activity> hits = service.search(category, keyword);
        displayResults(hits);
    }

    private String currentKeyword() {
        String cat = (String) categoryCombo.getSelectedItem();
        if (cat == null) return "";
        switch (cat) {
            case "Type":  return ((String) typeCombo.getSelectedItem() == null)
                                 ? "" : ((String) typeCombo.getSelectedItem()).trim();
            case "Date":  return dateInput.getText().trim();
            default:      return textInput.getText().trim();
        }
    }

    private void displayResults(List<Activity> activities) {
        resultsModel.setRowCount(0);
        for (Activity a : activities) {
            resultsModel.addRow(new Object[]{
                    a.getType(),
                    a.getDate() == null ? "" : a.getDate().format(DATE_FMT),
                    a.getDuration(),
                    String.join(", ", a.getCollaborators()),
                    a.getQuality(),
                    a.getNotes() == null ? "" : a.getNotes()
            });
        }
        if (activities.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No activities matched your query.",
                    "No results", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
