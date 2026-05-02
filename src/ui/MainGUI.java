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

        JButton addBtn     = new JButton("Add");
        JButton searchBtn  = new JButton("Search");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.addActionListener(e -> openAddDialog());
        searchBtn.addActionListener(e -> openSearchDialog());
        refreshBtn.addActionListener(e -> refreshDisplay());

        bar.add(addBtn);
        bar.add(searchBtn);
        bar.add(refreshBtn);
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
        String text = filterField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
        }
    }

    private void openAddDialog() {
        AddGUI dialog = new AddGUI(this, service);
        dialog.show();
        refreshDisplay();
    }

    private void openSearchDialog() {
        SearchGUI dialog = new SearchGUI(this, service);
        dialog.setVisible(true);
    }

    private void editSelected() {
        Activity selected = getSelectedActivity();
        if (selected == null) return;

        EditGUI dialog = new EditGUI(this, service, selected);
        dialog.show();
        refreshDisplay();
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
        List<Activity> all = service.getAllActivities();
        populateTable(all);
    }

    private void populateTable(List<Activity> activities) {
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

        List<Activity> all = service.getAllActivities();
        if (modelRow >= all.size()) return null;
        return all.get(modelRow);
    }
}
