import opennlp.tools.tokenize.SimpleTokenizer;

public class TextUnits {
    //Tokenize any piece of text into words/punctuations
    public static String[] tokenize(String text){
        //SimpleTokenizer is a built-in OpenNLP class that splits text into tokens
        SimpleTokenizer tk = SimpleTokenizer.INSTANCE; //ready-to-use singleton as there is no need for a new instance every time
        return tk.tokenize(text);
    }
}
