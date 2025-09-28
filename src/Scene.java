public class Scene {
    private String heading;
    private String content;

    public Scene(String heading, String content){
        this.heading = heading;
        this.content = content;
    }

    public String getHeading(){
        return heading;
    }

    public String getContent(){
        return  content;
    }

    @Override
    public String toString(){
        return heading + "\n" + content;
    }
}
