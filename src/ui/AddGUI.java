package ui;

import service.ActivityService;
import model.Activity;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class AddGUI {

    private final ActivityService activityService;
    private final JFrame owner;
    private final JFrame frame;
    private final JComboBox<String> typeDropdown;
    private final JTextField durationField;
    private final JTextField collaboratorsField;
    private final JSpinner qualitySpinner;
    private final JTextArea notesArea;
    private final JButton saveButton;
    private final JButton resetButton;
    private final JButton cancelButton;

    public AddGUI(ActivityService activityService) {
        this(null, activityService);
    }

    public AddGUI(JFrame owner, ActivityService activityService) {
        this.activityService = activityService;
        this.owner = owner;
        this.frame = new JFrame("Add Activity");
        this.typeDropdown = new JComboBox<>(new String[] {
            "Running",
            "Swimming",
            "Gym",
            "Cycling",
            "Yoga",
            "Hiking",
            "Other"
        });
        this.durationField = new JTextField();
        this.collaboratorsField = new JTextField();
        this.qualitySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        this.notesArea = new JTextArea(4, 20);
        this.saveButton = new JButton("Save");
        this.resetButton = new JButton("Reset");
        this.cancelButton = new JButton("Cancel");
        buildForm();
    }

    public void buildForm() {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 8, 8));
        formPanel.add(new JLabel("Type"));
        formPanel.add(typeDropdown);
        formPanel.add(new JLabel("Duration (minutes)"));
        formPanel.add(durationField);
        formPanel.add(new JLabel("Collaborators"));
        formPanel.add(collaboratorsField);
        formPanel.add(new JLabel("Quality (1-5)"));
        formPanel.add(qualitySpinner);
        formPanel.add(new JLabel("Notes"));
        formPanel.add(new JScrollPane(notesArea));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(e -> saveActivity());
        resetButton.addActionListener(e -> resetForm());
        cancelButton.addActionListener(e -> close());

        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(owner);
    }

    public void saveActivity() {
        if (activityService == null) {
            JOptionPane.showMessageDialog(frame, "Activity service is not configured.");
            return;
        }

        String durationText = durationField.getText().trim();
        if (durationText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Duration is required.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                JOptionPane.showMessageDialog(frame, "Duration must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Duration must be a number.");
            return;
        }

        try {
            Activity activity = new Activity();
            callSetter(activity, "setId", String.class, UUID.randomUUID().toString());
            callSetter(activity, "setType", String.class, (String) typeDropdown.getSelectedItem());
            callSetter(activity, "setDuration", int.class, duration);
            callSetter(activity, "setCollaborators", List.class, parseCollaborators(collaboratorsField.getText()));
            callSetter(activity, "setQuality", int.class, (Integer) qualitySpinner.getValue());
            callSetter(activity, "setNotes", String.class, notesArea.getText().trim());
            callSetter(activity, "setDate", LocalDate.class, LocalDate.now());
            callService("addActivity", Activity.class, activity);
            close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Could not save activity: " + e.getMessage());
        }
    }

    public void resetForm() {
        typeDropdown.setSelectedIndex(0);
        durationField.setText("");
        collaboratorsField.setText("");
        qualitySpinner.setValue(3);
        notesArea.setText("");
    }

    public void close() {
        frame.dispose();
    }

    public void show() {
        frame.setVisible(true);
    }

    private List<String> parseCollaborators(String rawText) {
        List<String> collaborators = new ArrayList<>();
        if (rawText == null || rawText.trim().isEmpty()) {
            return collaborators;
        }

        String[] parts = rawText.split(",");
        for (String part : parts) {
            String name = part.trim();
            if (!name.isEmpty()) {
                collaborators.add(name);
            }
        }
        return collaborators;
    }

    private void callSetter(Activity activity, String methodName, Class<?> argType, Object value) throws Exception {
        Method setter = activity.getClass().getMethod(methodName, argType);
        setter.invoke(activity, value);
    }

    private void callService(String methodName, Class<?> argType, Activity activity) throws Exception {
        Method serviceMethod = activityService.getClass().getMethod(methodName, argType);
        serviceMethod.invoke(activityService, activity);
    }

}
