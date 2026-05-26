package capers;

import java.io.File;
import java.io.IOException;

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
    static final File story = new File(capers, "story");

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
            try {
                story.createNewFile(); // 创建空文件
            } catch (IOException e) {
                throw new RuntimeException("Failed to create story file", e);
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
        System.out.println(newText);
        Utils.writeContents(story,newText);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        Dog dog = new Dog(name,breed,age);
        System.out.println(dog.toString());
        File DogName= new File(dogs, name);
        //不能存dog对象，只能存字符串，所以需要把dog变成字符串
//        Utils.writeContents(DogName,dog.toString());
        writeObject(DogName,dog);
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        //错过的问题：目前的问题好像是在makedog的时候将dog的信息是以字符串的形式写入，但是在dog.java里面我用的是序列化对象，也就是说我用字符串写，用序列化读需要改变(只有第一次可能),改变想法初步为用dog.java里面的save函数处理，记住需要改变makedog里面
        Dog dog = Dog.fromFile(name);
        dog.haveBirthday();
        dog.saveDog();
        dog.toString();
    }

}
