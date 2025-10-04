import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;
import java.util.*;

public class ScriptParser {
    //load a script file into a String
    public static String loadScript(String filePath) throws IOException{
        try{
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    public List<Scene> splitScenes(String script){
        List<Scene> scenes = new ArrayList<>();
        //Regex for scene headings
        Pattern scenePattern = Pattern.compile(
                "(?m)^(\\d+[A-Z]?\\s+)?((INT\\.?|EXT\\.?|INT/EXT\\.?|I/E\\.?|EST\\.|OMITTED)\\s+[^\n]+)"
        );
        Matcher matcher = scenePattern.matcher(script);

        //Stores the positions of the scene matches in order to allow content extraction between them
        List<Integer> positions = new ArrayList<>();
        while (matcher.find()){
            positions.add(matcher.start());
        }
        //Adds one moe end position for the end of the script
        positions.add(script.length());

        int sceneCounter = 1; //only used as fallback

        //Loops through each scene heading and extract content up to the next heading
        for (int i = 0; i < positions.size() - 1; i++){
            //Extracts the text from the start of the current heading to the start of the next heading
            String block = script.substring(positions.get(i), positions.get(i + 1));
            String sceneNumber= "";
            Matcher m = scenePattern.matcher(block);
            if (m.find()){
                //Checks if a scene number exists
                if (m.group(1) != null){
                    sceneNumber = m.group(1).trim();

                    //If the scene number contains a letter, the counter doesn't increment
                    if (!sceneNumber.matches(".*[A-Z].*")){
                        sceneCounter = Integer.parseInt(sceneNumber); //keeps the counter in sync
                    }
                }
            } else {
                //No scene number so fallback counter used
                sceneNumber = String.valueOf(sceneCounter);
            }

            //Full heading (keyword + location/time) without the number
            String heading = m.group(2).trim();
            //Takes out any trailing numbers
            heading = heading.replaceAll("\\s+\\d+[A-Z]?\\.?(\\s*\\*?)$", "").trim();

            //Content is everything after the heading until the next scene
            String content = block.substring(m.end()).trim();

            scenes.add(new Scene(sceneNumber, heading, content));
        }
        return scenes;
    }
}