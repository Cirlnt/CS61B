package capers;

import java.io.File;
import static capers.Utils.*;

/** A repository for Capers 
 * @author Cirlnt
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
//    static final File CAPERS_FOLDER = null;
    static final File capers = Utils.join(".capers");
    static final File dogs = Utils.join(".capers","dogs");
    static final File story = Utils.join(".capers","story");

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     */
    public static void setupPersistence() {
//        if (!capers.exists()) {
//            capers.mkdir();
//            dogs.mkdir();
//            story.mkdir();
//        }
//        if (!dogs.exists()) {
//            dogs.mkdir();
//        }
//        if (!story.exists()) {
//            story.mkdir();
//        }
        if (!capers.exists()) {
            if (!capers.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + capers);
            }
        }
        if (!dogs.exists()) {
            if (!dogs.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + dogs);
            }
        }
        if (!story.exists()) {
            if (!story.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + story);
            }
        }
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        String newText = text;
        String old = readContentsAsString(story);
        if (!old.isEmpty()) {
            newText = old+"\n"+text;
        }
        System.out.println(text);
        Utils.writeContents(story,newText);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        Utils.writeContents(dogs,name,breed,age);
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        Dog dog = Dog.fromFile(name);
        dog.haveBirthday();
        dog.saveDog();
        dog.toString();
    }

}
