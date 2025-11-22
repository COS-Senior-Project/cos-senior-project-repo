public class SetDressing extends PhysicalObject {
    public SetDressing (int sceneNumberInt, String sceneNumber, String nameItem, String rule,
                      String contextSnippet, double confidenceScore) {
        super(sceneNumberInt, sceneNumber, nameItem, rule, contextSnippet, confidenceScore);
    }

    @Override
    public String toTrainingFeatures() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNormalizedName(nameItem)).append(",");
        sb.append(sceneNumberInt).append(",");
        sb.append(0).append(","); //not movable
        sb.append(hasAdjective() ? 1 : 0);
        return sb.toString();
    }

}
