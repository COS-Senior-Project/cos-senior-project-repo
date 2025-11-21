public class SetDressing extends PhysicalObject {
    public SetDressing(String nameItem, int sceneNumberInt, String sceneNumber,
                       String contextSnippet, double confidenceScore) {
        super(nameItem, sceneNumberInt, sceneNumber, contextSnippet, confidenceScore);
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
