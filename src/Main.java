import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import java.io.IOException;
import java.util.List;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        //Loads script text using ScriptParser and cleans the page numbers and blank lines
        String rawScript = ScriptParser.loadScript("Scripts/The Break Room.txt");
        String cleanedScript = ScriptParser.cleanScript(rawScript);
        //Splits into scenes
        List<Scene> scenes = ScriptParser.splitScenes(cleanedScript);

        //Prints scenes
        for (int i = 0; i < scenes.size(); i++) {
            System.out.println(String.format("Scene: %d", i + 1));
            System.out.println(String.format("Heading: %s", scenes.get(i).getHeading()));
            System.out.println(String.format("Content: %s", scenes.get(i).getContent()));
            System.out.println("-----------------------------------");
        }
    }
}