public class Prop extends PhysicalObject {
    public Prop (String nameItem, int sceneNumberInt, String sceneNumber,
                 String contextSnippet, double confidenceScore){
        super(nameItem, sceneNumberInt, sceneNumber, contextSnippet, confidenceScore);
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
