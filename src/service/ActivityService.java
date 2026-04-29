package service;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import model.Activity;
import storage.StorageManager;


public class ActivityService {
    private ArrayList<Activity> activities; // runtime memory.
    private StorageManager storageManager;

    public ActivityService() {
        this.storageManager = new StorageManager();
        this.activities = storageManager.loadActivity(); // load activities from file on initialization
    }

    // create method
    public void addActivity(Activity activity) {
        activities.add(activity); // add to runtime memory
        storageManager.saveActivity(activities); // save to file after adding
    }

    // read method
    public ArrayList<Activity> getAllActivities() {
        return activities; // return the current list of activities in runtime memory
    }

    // delete method
    public boolean deleteActivity(String id) {
        Activity activityToRemove = null;
        for (Activity activity : activities) {
            if (activity.getId().equals(id)) {
                activityToRemove = activity;
                break;
            }
        }
        boolean removed = activities.remove(activityToRemove); // remove from runtime memory
        if (removed) {
            storageManager.saveActivity(activities); // save to file after deletion
            return true; // indicate successful deletion
        }
        return false; // indicate activity not found
    }
    
    // update method
    public void updateActivity(Activity updatedActivity) {
        // first, we need to see if the activity exists
        String _inputID = updatedActivity.getId();
        for (int i = 0; i < activities.size(); i++) {
            if (activities.get(i).getId().equals(_inputID)) {
                activities.set(i, updatedActivity); // update the activity in runtime memory
                storageManager.saveActivity(activities); // save to file after updating
                return; // exit the method after successful update
            }
        }

        // it means that this activity don't exist, we just add it as a new activity
        activities.add(updatedActivity); // add to runtime memory
        storageManager.saveActivity(activities); // save to file after adding
    }

    // search and filter methods
    public ArrayList<Activity> search(String Keyword) {
        // for now we just use the notes field to serach, but if we need to search other fields, we can easily add them here.
        ArrayList<Activity> results = new ArrayList<>();
        for (Activity activity : activities) {
            if (activity.getNotes().contains(Keyword)) {
                results.add(activity);
            }
        }
        return results;
    }

    public ArrayList<Activity> filterByType(String type) {
        ArrayList<Activity> results = new ArrayList<>();
        for (Activity activity : activities) {
            if (activity.getType().equalsIgnoreCase(type)) {
                results.add(activity);
            }
        }
        return results;
    }

    public ArrayList<Activity> filterByDate(LocalDate date) {
        ArrayList<Activity> results = new ArrayList<>();
        for (Activity activity : activities) {
            if (activity.getDate().equals(date)) {
                results.add(activity);
            }
        }
        return results;
    }

    public ArrayList<Activity> filterByCollaborator(String name) {
        ArrayList<Activity> results = new ArrayList<>();
        for (Activity activity : activities) {
            if (activity.getCollaborators().contains(name)) {
                results.add(activity);
            }
        }
        return results;
    }
}
