package storage;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList; 
import model.Activity;


// this only handles the file I/O, the runtime activities are handled by the ActivityService, 
// which calls this StorageManager to save and load activities from the file.
public class StorageManager {
    private static final String DEFAULT_PATH = "./activities.dat";
    private String FILE_PATH;

    public StorageManager() {
        this(DEFAULT_PATH); 
    }

    public StorageManager(String filePath) {
        this.FILE_PATH = filePath;
    }

    public void saveActivity(ArrayList<Activity> activities) {
        try {
            ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream(FILE_PATH));

            out.writeObject(activities); // write the list of activities to the file   
            out.close();
            System.out.println("Successfully saved activities to file, and updated runtime activities.");
            return; // exit the method after successful save
        } catch (IOException e) {
            e.printStackTrace(); 
        }
        System.out.println("Unable to save activities to file.");

    }

    public ArrayList<Activity> loadActivity() {
        try {
            ObjectInputStream in =
                new ObjectInputStream(new FileInputStream(FILE_PATH));

            ArrayList<Activity> activities = (ArrayList<Activity>) in.readObject();  
            in.close();

            return activities;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("No activities found in file, returning empty list.");
        return new ArrayList<Activity>();
    }

}
