package ui;

import model.Activity;
import service.ActivityService;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

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

public class EditGUI {

    private final ActivityService activityService;
    private final Activity activity;
    private final JFrame owner;
    private final JFrame frame;
    private final JComboBox<String> typeDropdown;
    private final JTextField durationField;
    private final JTextField collaboratorsField;
    private final JSpinner qualitySpinner;
    private final JTextArea notesArea;
    private final JButton saveButton;
    private final JButton cancelButton;

    public EditGUI(ActivityService activityService, Activity activity) {
        this(null, activityService, activity);
    }

    public EditGUI(JFrame owner, ActivityService activityService, Activity activity) {
        this.activityService = activityService;
        this.activity = activity;
        this.owner = owner;
        this.frame = new JFrame("Edit Activity");
        this.typeDropdown = new JComboBox<>(new String[] {
            "Work",
            "Exercise",
            "Study",
            "Social",
            "Other"
        });
        this.durationField = new JTextField();
        this.collaboratorsField = new JTextField();
        this.qualitySpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        this.notesArea = new JTextArea(4, 20);
        this.saveButton = new JButton("Save");
        this.cancelButton = new JButton("Cancel");
        buildForm();
        loadActivity();
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
        buttonPanel.add(cancelButton);

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> close());

        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(owner);
    }

    public void loadActivity() {
        if (activity == null) {
            JOptionPane.showMessageDialog(frame, "No activity selected.");
            close();
            return;
        }

        try {
            String type = (String) callGetter(activity, "getType");
            Integer duration = (Integer) callGetter(activity, "getDuration");
            Integer quality = (Integer) callGetter(activity, "getQuality");
            String notes = (String) callGetter(activity, "getNotes");
            @SuppressWarnings("unchecked")
            List<String> collaborators = (List<String>) callGetter(activity, "getCollaborators");

            if (type != null) {
                typeDropdown.setSelectedItem(type);
            }
            if (duration != null) {
                durationField.setText(String.valueOf(duration));
            }
            if (quality != null) {
                qualitySpinner.setValue(quality);
            }
            notesArea.setText(notes == null ? "" : notes);
            collaboratorsField.setText(joinCollaborators(collaborators));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Could not load activity.");
        }
    }

    public void saveChanges() {
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
            callSetter(activity, "setType", String.class, (String) typeDropdown.getSelectedItem());
            callSetter(activity, "setDuration", int.class, duration);
            callSetter(activity, "setCollaborators", List.class, parseCollaborators(collaboratorsField.getText()));
            callSetter(activity, "setQuality", int.class, (Integer) qualitySpinner.getValue());
            callSetter(activity, "setNotes", String.class, notesArea.getText().trim());
            callService("updateActivity", Activity.class, activity);
            close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Could not update activity: " + e.getMessage());
        }
    }

    public void close() {
        frame.dispose();
    }

    public void show() {
        frame.setVisible(true);
    }

    private Object callGetter(Activity target, String methodName) throws Exception {
        Method getter = target.getClass().getMethod(methodName);
        return getter.invoke(target);
    }

    private void callSetter(Activity target, String methodName, Class<?> argType, Object value) throws Exception {
        Method setter = target.getClass().getMethod(methodName, argType);
        setter.invoke(target, value);
    }

    private void callService(String methodName, Class<?> argType, Activity target) throws Exception {
        Method serviceMethod = activityService.getClass().getMethod(methodName, argType);
        serviceMethod.invoke(activityService, target);
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

    private String joinCollaborators(List<String> collaborators) {
        if (collaborators == null || collaborators.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (String collaborator : collaborators) {
            if (collaborator != null && !collaborator.trim().isEmpty()) {
                joiner.add(collaborator.trim());
            }
        }
        return joiner.toString();
    }

}
