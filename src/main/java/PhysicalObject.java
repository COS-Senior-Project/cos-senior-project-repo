import  opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;
import java.io.InputStream;
import java.util.Map;

public abstract class PhysicalObject extends ExtractableItem {

    private static POSTaggerME posTagger;

    static {
        try (InputStream modelIn = PhysicalObject.class.getResourceAsStream("/models/opennlp-en-ud-ewt-pos-1.3-2.5.4.bin")){
            POSModel model = new POSModel(modelIn);
            posTagger = new POSTaggerME(model);
        } catch (Exception e){
            e.printStackTrace();
            posTagger = null;
        }
    }

    public PhysicalObject(String nameItem, int sceneNumberInt, String sceneNumber,
                          String contextSnippet, double confidenceScore){
        super(nameItem, sceneNumberInt, sceneNumber, contextSnippet, confidenceScore);
    }

    @Override
    public String toString(){
        return String.format("Physical Object: %n%s", super.toString());
    }

    //normalizes physical object names
    @Override
    public String getNormalizedName(String name){
        if (name == null) return "";
        return  name.trim().toLowerCase();
    }

    @Override
    public void boostConfidence(){
        if (nameItem != null && nameItem.contains(" ")){
            //multi-word objects are usually real props or set dressing
            this.confidenceScore += 0.1;
        }
    }

    //checks if there are any adjectives in the context snippet
    public boolean hasAdjective() {
        if (posTagger == null || contextSnippet == null || contextSnippet.isEmpty()){
            return false;
        }
        String[] tokens = SimpleTokenizer.INSTANCE.tokenize(contextSnippet);
        String[] tags = posTagger.tag(tokens);

        for (String tag : tags) {
            //looks for adjective, comparative, or superlative
            if (tag.equals("JJ") || tag.equals("JJR") || tag.equals("JJS")) {
                return true;
            }
        }
        return false;
    }

    //will prepare each mention for CSV
    public abstract String toTrainingFeatures();

    //checks if an item is possibly movable
    public boolean isMovable(){
        if (contextSnippet != null && !contextSnippet.isEmpty() && nameItem != null && !nameItem.isEmpty()) {
            //verbs that indicate the item can be moved
            String verbs = "(pick(?:s|ed)? up|grab(?:s|bed)?|hold(?:s)?|held|carry|carries|carried|lift(?:s|ed)?|move(?:s|d)?|pick(?:s|ed)?|" +
                    "handle(?:s|d)?|transport(?:s|ed)?|take(?:s)?|took|taken|throw(?:s)?|threw|thrown|push(?:s|ed)?|pull(?:s|ed)?|place(?:s|ed)?)";
            //optional articles' non-capturing group
            String articles = "(?:a|an|the|some|any)?\\s*";
            //escape any regex special characters in the item's name
            String escapedName = nameItem.replaceAll("([\\\\.*+?\\[\\]^$(){}=!<>|:-])", "\\\\$1");

            //pattern = verb + optional article + object name
            String pattern = "\\b" + verbs + "\\b\\s+" + articles + escapedName + "\\b";
            if (contextSnippet.toLowerCase().matches(".*" + pattern.toLowerCase() + ".*")) {
                return true;
            }
        }
        return false; //if false => likely set dressing
    }

    public boolean isInteractedWith(){
        return contextSnippet.matches(".*\\b(takes|holds|grabs|picks up|uses|gives)\\b.*");
    }

    public String getSurroundingContext(int window){
        if (contextSnippet == null || contextSnippet.isEmpty()){
            return "";
        }

        //tokenizes snippet
        String[] token = TextUnits.tokenize(contextSnippet);
        //normalizing the item
        String itemLower = getNormalizedName(nameItem);

        //finds the first token that contains the object name
        int itemIndex = -1;
        for (int i = 0; i < token.length; i++){
            if (token[i].toLowerCase().contains(itemLower)) {
                itemIndex = i;
                break;
            }
        }

        //if object not found, returns the whole snippet as fallback
        if (itemIndex == -1) {
            return contextSnippet;
        }

        //computes the start and end of the surrounding context window
        int start = Math.max(0, itemIndex - window);
        int end = Math.min(token.length - 1, itemIndex + window);

        //build the surrounding context string
        StringBuilder sb = new StringBuilder();
        for (int j = start; j <= end; j++) {
            sb.append(token[j]);
            if (j < end && !token[j+1].matches("[.,!?;:]")) sb.append(" ");
        }
        return  sb.toString();
    }
}
