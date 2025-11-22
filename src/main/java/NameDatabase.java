import java.io.*;

//class working with the name file databases
//it loads the files and creates a function checking
// if the word is in the Trie structure
public class NameDatabase {
    //objects for the first name and the last name trie structures
    public static final NameTrie FIRST_NAME_TRIE = new NameTrie();
    public static final NameTrie LAST_NAME_TRIE = new NameTrie();

    //calls the loadNames file to load the files into the structures
    // when the class is first referenced
    static {
        loadNames("/names/first_names.txt", FIRST_NAME_TRIE);
        loadNames("/names/last_names.txt", LAST_NAME_TRIE);
    }

    //method to load the names
    public static void loadNames (String filePath, NameTrie trie) {
        //tries to load the file from the classpath (inside "resources")
        //then, tries to decode it and read it with a buffered reader
        try (InputStream in = NameDatabase.class.getResourceAsStream(filePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(in))){

            String line;
            //reads until there are lines left
            while ((line = br.readLine()) != null) {
                //inserts into the first or last name structure
                trie.insert(line.trim());
            }
        } catch (FileNotFoundException e) {
            System.out.println("File name not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading name file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("General exception: " + e.getMessage());
        }
    }

    //checks if a string is found in the first or last name Trie structures
    //and boost confidence depending on where it is found
    public static double confidenceBoostMatchFile (String raw) {
        //if the string is null or empty, it returns 0 boost
        if (raw == null || raw.isEmpty()) return 0.0;

        String name = raw.toLowerCase();
        //creates an array with each word of the name
        String[] parts = name.split("\\s+");
        double boost = 0.0;

        //if the first name matches
        if (parts.length >= 1 && FIRST_NAME_TRIE.contain(parts[0])){
            boost += 0.4;
        }

        //if any last or middle names match
        if (parts.length >= 2){
            for (int i = 1; i < parts.length; i++) {
                if (LAST_NAME_TRIE.contain(parts[i])) {
                    boost += 0.3;
                }
            }
        }

        //if both first name and at least one other name match, boost to 1
        if (boost >= 0.7) {
            boost += 0.3;
        }

        //returns the boost, and returns 1 if the boost is higher than that
        return Math.min(boost, 1.0);
    }

}
