import java.util.Map;

public abstract class ExtractableItem {
    protected int sceneNumberInt;
    protected String sceneNumber;
    protected String nameItem;
    protected String rule;
    protected String contextSnippet;
    protected double confidenceScore;

    public ExtractableItem(int sceneNumberInt, String sceneNumber, String nameItem, String rule, String contextSnippet, double confidenceScore){
        this.sceneNumberInt = sceneNumberInt;
        this.sceneNumber = sceneNumber;
        this.nameItem = nameItem;
        this.rule = rule;
        this.contextSnippet = contextSnippet;
        this.confidenceScore = confidenceScore;
    }

    //getters and setters

    public int getSceneNumberInt() {
        return sceneNumberInt;
    }

    public void setSceneNumberInt(int sceneNumberInt) {
        this.sceneNumberInt = sceneNumberInt;
    }

    public String getSceneNumber() {
        return sceneNumber;
    }

    public String getNameItem(){
        return nameItem;
    }

    public void setNameItem(String nameItem){
        this.nameItem = nameItem;
    }

    public void setRule(String rule) { this.rule = rule; }

    public String getRule() {
        return rule;
    }

    public void setSceneNumber(String sceneNumber) {
        this.sceneNumber = sceneNumber;
    }

    public String getContextSnippet() {
        return contextSnippet;
    }

    public void setContextSnippet(String contextSnippet) {
        this.contextSnippet = contextSnippet;
    }

    public double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    //abstract methods

    //normalizes item names
    public  abstract String getNormalizedName(String name);

    //boosts confidence based on internal heuristics
    public  abstract  void boostConfidence();

    //non-abstract methods

    public String bootstrappingObjects(String classLabel) {
        String[] tokens = TextUnits.tokenize(this.contextSnippet);
        StringBuilder sb = new StringBuilder();
        String nameLower = nameItem.toLowerCase();
        int nameWords = nameLower.split("\\s+").length;
        int i = 0;
        while (i < tokens.length) {
            //checks for multi-word match
            boolean match = false;
            if (i + nameWords - 1 < tokens.length) {
                StringBuilder candidate = new StringBuilder();
                for (int j = 0; j < nameWords; j++) {
                    String next = tokens[i + j].toLowerCase();
                    //if it's not the first token or punctuation, append a space
                    if (j > 0 && !next.matches("[.,!?;:]")) {
                        candidate.append(" ");
                    }
                    //appends the token with the space
                    candidate.append(next);

                }
                //if the candidate equals the name of character, prop, or set dressing
                if (candidate.toString().equals(nameLower)) {
                    match = true;
                }
            }
            if (match) {
                //inserts <START:LABEL>
                sb.append("<START:").append(classLabel).append(">");

                //appends the actual name tokens
                for (int j = 0; j < nameWords; j++) {
                    sb.append(tokens[i + j]);
                }

                //insert <END>
                sb.append("<END>");

                i += nameWords;
            }
            else {
                sb.append(tokens[i]);
                if (i > 0 && !tokens[i+1].matches("[.,!?;:]")) {
                    sb.append(" ");
                }
                i++;
            }
        }

        return sb.toString().trim();
    }

    /*
    @Override
    public String toString(){
        return "Extractable Item:" +
                "\nname = " + nameItem +
                "\nscene order number (integer) = " + sceneNumberInt +
                "\nscene script number (String) = " + sceneNumber +
                "\ncontext snippet = " + contextSnippet +
                "\nconfidence score = " + confidenceScore;
    }
    */
}
