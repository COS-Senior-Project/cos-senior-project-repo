import java.util.HashMap;
import java.util.Map;

//class for the Trie data structure that will be used to check if
// candidate names are contained in the common name files
public class NameTrie {
    private static class Node {
        //creates map connecting characters (e.g. a, b) to node (e.g. Node 1, Node 2)
        Map<java.lang.Character, Node> children = new HashMap<>();
        //default value of each node value is
        // that the character is not complete word
        boolean isWord = false;
    }

    //new node object
    private final Node root = new Node();

    //inserts a new word in the trie structure
    public void insert (String word) {
        //starts from the root of the Trie, it has no characters
        Node current = root;
        //loops through each character of the word, converted to lowercase
        for (char c : word.toLowerCase().toCharArray()) {
            //computeIfAbsent:
            //if the character of the word is not in the Trie, it adds a new node attached to it and returns it
            //if the character already exists, it returns it

            //current moves the pointer down to the child node of the returned node
            current = current.children.computeIfAbsent(c, k -> new Node());
        }
        //marks the last character of the word as the final word node
        current.isWord = true;
    }

    //checks if the word is contained in the trie
    public boolean contain (String word) {
        //the current node examined starts with the root of the tree
        Node current = root;
        //converts the word to lowercase and checks each character of the word
        for (char c : word.toLowerCase().toCharArray()) {
            //checks if current has a child that can be mapped to the
            // character of the word we are looking for
            Node next = current.children.get(c);
            //if not in the map, the word is not contained in the trie structure
            if (next == null) {
                return false;
            }
            //moves to the next node
            current = next;
        }
        //returns true if after the end of the word the current node
        // represents a complete word
        //returns false if it is just a prefix in the trie
        return current.isWord;
    }
}
