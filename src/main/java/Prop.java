public class Prop extends PhysicalObject {
    public Prop (int sceneNumberInt, String sceneNumber, String nameItem, String rule,
                      String contextSnippet, double confidenceScore) {
        super(sceneNumberInt, sceneNumber, nameItem, rule, contextSnippet, confidenceScore);
    }

    @Override
    public String toTrainingFeatures() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNormalizedName(nameItem)).append(",");
        sb.append(sceneNumberInt).append(",");
        sb.append(isMovable() ? 1 : 0).append(",");
        sb.append(hasAdjective() ? 1 : 0);
        return sb.toString();
    }


}
