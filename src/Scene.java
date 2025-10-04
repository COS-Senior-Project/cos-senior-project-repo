public class Scene {
    private final String sceneNumber;
    private final String heading;
    private final String content;

    public Scene(String sceneNumber, String heading, String content){
        this.sceneNumber = sceneNumber;
        this.heading = heading;
        this.content = content;
    }

    public String getSceneNumber(){
        return sceneNumber;
    }

    public String getHeading(){
        return heading;
    }

    public String getContent(){
        return  content;
    }

    @Override
    public String toString(){
        return "Scene: " + sceneNumber + "\nHeading: " + heading + "\nContent:\n" + content + "\n-----------------------------------\n";
    }
}
