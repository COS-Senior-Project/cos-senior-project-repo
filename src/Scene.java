public class Scene {
    private final int sceneIntNumber;
    private final String sceneNumber;
    private final String heading;
    private final String content;
    private final String locationKeyword;
    private final String time;

    public Scene(int sceneIntNumber, String sceneNumber, String heading, String content, String locationKeyword, String time){
        this.sceneIntNumber = sceneIntNumber;
        this.sceneNumber = sceneNumber;
        this.heading = heading;
        this.content = content;
        this.locationKeyword = locationKeyword;
        this.time = time;
    }

    public int getSceneIntNumber(){
        return sceneIntNumber;
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

    public String getLocationKeyword(){
        return  locationKeyword;
    }
    public String getTime(){
        return  time;
    }

    @Override
    public String toString(){
        return "Scene: " + sceneNumber +
                "\nHeading: " + heading +
                "\nContent:\n" + content +
                "\n-----------------------------------\n";
    }
}
