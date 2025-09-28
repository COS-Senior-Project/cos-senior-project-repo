import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;
import java.util.*;

public class ScriptParser {
    //load a script file into a String
    public static String loadScript(String filePath) throws IOException{
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    //Split a script into Scene objects
    public static List<Scene> splitScenes(String script){
        //Regex for scene headings
        Pattern scenePattern = Pattern.compile("(?m)^(\\d+\\s+)?(INT\\.?|EXT\\.?|INT/EXT\\.?|I/E\\.?|EST\\.).*?(\\s+\\d+\\.?$)?");
        Matcher matcher = scenePattern.matcher(script);

        List<Scene> scenes = new ArrayList<>();
        int lastIndex = 0;
        String lastHeading = null;

        while(matcher.find()){
            if (lastHeading != null) {
                //From previous heading to this heading is one scene
                String sceneText = script.substring(lastIndex, matcher.start()).trim();
                scenes.add(new Scene(lastHeading, sceneText));
            }

            lastHeading = matcher.group().trim()
                        .replaceAll("^\\d+\\s+", "")
                        .replaceAll("\\s+\\d+\\.?$", "");
            lastIndex = matcher.end();
        }
        //Add the last scene (heading + text until the end)
        if (lastHeading != null){
            String sceneText = script.substring(lastIndex).trim();
            scenes.add(new Scene(lastHeading, sceneText));
        }
        return scenes;
    }

    public static String cleanScript(String script){
        //Removes lines that are page numbers
        script = script.replaceAll("(?m)^\\d+\\.?\\s*$", "");

        //Removes blank lines
        script = script.replaceAll("(?m)^[ \t]*\r?\n", "");

        return script.trim();
    }
}