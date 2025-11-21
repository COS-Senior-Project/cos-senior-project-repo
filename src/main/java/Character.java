import java.util.Locale;

public class Character extends ExtractableItem {
    public Character (String nameItem, int sceneNumberInt, String sceneNumber,
                      String contextSnippet, double confidenceScore) {
        super(nameItem, sceneNumberInt, sceneNumber, contextSnippet, confidenceScore);
    }

    @Override
    public String getNormalizedName(String raw){
        if (raw == null) return "";
        String s = raw.replace("’", "'").replaceAll("\\s+", " ").trim();
        //removes stray trailing punctuation except middle-of-name punctuation
        s = s.replaceAll("^[^A-Z0-9]+", "").replaceAll("[^A-Z0-9]+$", "");
        return s;
    }

    @Override
    public void boostConfidence() {
        //multi-word names (e.g. ANNA SMITH) can be stronger matches
        if (nameItem != null && nameItem.length() >= 2) {
            confidenceScore += 0.1;
        }

    }
}
