package main;

import service.ActivityService;

public class MainGUI {

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ActivityService service = new ActivityService();
            ui.MainGUI mainWindow = new ui.MainGUI(service);
            mainWindow.setVisible(true);
        });
    }
}
