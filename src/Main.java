import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import java.io.IOException;
import java.util.List;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws IOException {
        String script = ScriptParser.loadScript("Scripts/Wicked.txt");

        ScriptParser parser = new ScriptParser();
        List<Scene> scenes = parser.splitScenes(script);

        for (Scene scene : scenes){
            System.out.println(scene);
        }
    }
}