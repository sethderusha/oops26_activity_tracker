package ui;

import model.Activity;
import model.Duration;
import service.ActivityService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainGUI extends JFrame {

    private static final String[] TABLE_COLUMNS = {
            "Type", "Date", "Duration", "Collaborators", "Quality", "Notes"
    };

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ActivityService service;
    private final DefaultTableModel tableModel;
    private final JTable activityTable;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField filterField;
    private final JPopupMenu rowPopupMenu;
    /** Rows currently shown in the table (same order as model); used for edit/delete when filter is active. */
    private List<Activity> displayedActivities = new ArrayList<>();

    public MainGUI(ActivityService service) {
        super("Fitness Tracker");
        this.service = service;
        this.filterField = new JTextField(20);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildToolBar(), BorderLayout.NORTH);

        this.tableModel = new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int col) {
                switch (col) {
                    case 2: return Duration.class;
                    case 4: return Integer.class;
                    default: return String.class;
                }
            }
        };
        this.activityTable = new JTable(tableModel);
        activityTable.setRowHeight(24);

        this.sorter = new TableRowSorter<>(tableModel);
        activityTable.setRowSorter(sorter);

        activityTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = activityTable.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        JScrollPane scroll = new JScrollPane(activityTable);
        add(scroll, BorderLayout.CENTER);

        this.rowPopupMenu = buildRowPopupMenu();
        activityTable.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { maybePopup(e); }
            @Override public void mouseReleased(MouseEvent e) { maybePopup(e); }
            private void maybePopup(MouseEvent e) {
                if (!e.isPopupTrigger()) return;
                int row = activityTable.rowAtPoint(e.getPoint());
                if (row < 0) return;
                activityTable.setRowSelectionInterval(row, row);
                rowPopupMenu.show(activityTable, e.getX(), e.getY());
            }
        });

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        refreshDisplay();
    }

    private JToolBar buildToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);

        JButton functionsBtn = new JButton("Functions");
        JPopupMenu functionsMenu = new JPopupMenu();
        JMenuItem addItem = new JMenuItem("Add");
        JMenuItem searchItem = new JMenuItem("Search");
        addItem.addActionListener(e -> openAddDialog());
        searchItem.addActionListener(e -> openSearchDialog());
        functionsMenu.add(addItem);
        functionsMenu.add(searchItem);
        functionsBtn.addActionListener(e ->
                functionsMenu.show(functionsBtn, 0, functionsBtn.getHeight()));

        bar.add(functionsBtn);
        bar.add(Box.createHorizontalGlue());
        bar.add(new JLabel("Filter: "));
        bar.add(filterField);
        bar.add(Box.createHorizontalStrut(8));
        return bar;
    }

    private JPopupMenu buildRowPopupMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem   = new JMenuItem("Edit");
        JMenuItem deleteItem = new JMenuItem("Delete");

        editItem.addActionListener(e -> editSelected());
        deleteItem.addActionListener(e -> deleteSelected());

        menu.add(editItem);
        menu.add(deleteItem);
        return menu;
    }

    private void applyFilter() {
        repopulateFromService();
    }

    private void repopulateFromService() {
        List<Activity> all = service.getAllActivities();
        String text = filterField.getText().trim();
        List<Activity> toShow;
        if (text.isEmpty()) {
            toShow = all;
        } else {
            Pattern p = Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE);
            toShow = new ArrayList<>();
            for (Activity a : all) {
                if (matchesFilter(a, p)) {
                    toShow.add(a);
                }
            }
        }
        populateTable(toShow);
    }

    private boolean matchesFilter(Activity a, Pattern p) {
        if (p.matcher(a.getType() == null ? "" : a.getType()).find()) {
            return true;
        }
        if (a.getDate() != null && p.matcher(a.getDate().format(DATE_FMT)).find()) {
            return true;
        }
        if (p.matcher(String.valueOf(a.getDuration())).find()) {
            return true;
        }
        String collab = a.getCollaborators() == null ? "" : String.join(", ", a.getCollaborators());
        if (p.matcher(collab).find()) {
            return true;
        }
        if (p.matcher(String.valueOf(a.getQuality())).find()) {
            return true;
        }
        return p.matcher(a.getNotes() == null ? "" : a.getNotes()).find();
    }

    private void openAddDialog() {
        AddGUI dialog = new AddGUI(this, service, this::refreshDisplay);
        dialog.show();
    }

    private void openSearchDialog() {
        SearchGUI dialog = new SearchGUI(this, service);
        dialog.setVisible(true);
    }

    private void editSelected() {
        Activity selected = getSelectedActivity();
        if (selected == null) return;

        EditGUI dialog = new EditGUI(this, service, selected, this::refreshDisplay);
        dialog.show();
    }

    private void deleteSelected() {
        Activity selected = getSelectedActivity();
        if (selected == null) return;

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete this activity?\n\n" + selected.getType() + " on " + selected.getDate(),
                "Confirm delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.OK_OPTION) return;

        service.deleteActivity(selected.getId());
        refreshDisplay();
    }

    public void refreshDisplay() {
        repopulateFromService();
    }

    private void populateTable(List<Activity> activities) {
        displayedActivities = new ArrayList<>(activities);
        tableModel.setRowCount(0);
        for (Activity a : activities) {
            tableModel.addRow(new Object[]{
                    a.getType(),
                    a.getDate() == null ? "" : a.getDate().format(DATE_FMT),
                    a.getDuration(),
                    a.getCollaborators() == null ? "" : String.join(", ", a.getCollaborators()),
                    a.getQuality(),
                    a.getNotes() == null ? "" : a.getNotes()
            });
        }
    }

    private Activity getSelectedActivity() {
        int viewRow = activityTable.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = activityTable.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= displayedActivities.size()) return null;
        return displayedActivities.get(modelRow);
    }
}
