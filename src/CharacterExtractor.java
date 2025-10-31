import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.regex.*;

public class CharacterExtractor {

    //Regex that captures multi-word uppercase names, group1 = name, group2 = parenthetical (optional)
    private static final Pattern MULTI_WORD_NAME = Pattern.compile("\\b((?:[A-Z][A-Z0-9'’\\.\\-]*)(?:\\s+[A-Z][A-Z0-9'’\\.\\-]*)*)\\b(?:\\s*\\(([^)]*)\\))?");

    //A small stoplist for common screenplay tokens that are not names
    private static final Set<String> STOPLIST = new HashSet<>(Arrays.asList("INT","EXT","OMITTED","DAY","NIGHT","SAME","TIME","CONTINUOUS","PART","END","SCENE","PAGE"));
    private static final Pattern NAME_TOKEN = Pattern.compile("^[A-Z][A-Z0-9'’\\.\\-]*$");
    private static final Set<String> SOUND_WORDS = new HashSet<>(Arrays.asList("BOOM", "BANG", "CRASH", "RUMBLE", "SOUND", "NOISE", "FX", "CUT", "FADE", "CONTINUED", "SFX", "CROWD"));
    //optional debug flag to print decisions
    private static final boolean DEBUG = false;

    public static void extractCharacterToCVS(List<Scene> scenes, String outputPath) throws IOException{
        //using try-with-resources for safe closing
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath, false))){
            //header
            writer.write("sceneNumber, candidateName, rule, contextSnippet\n");

            for (Scene scene : scenes){
                String sceneKey = scene.getSceneNumber();
                String content = scene.getContent();
                if (content == null || content.trim().isEmpty()) continue;

                //keeps track of the names already written for this scene
                Set<String> written = new LinkedHashSet<>();

                //speaker line above dialogue rule
                //split into lines and look for an uppercase cue line followed by a non-uppercase line
                String[] lines = content.split("\n", -1);
                for (int i = 0; i < lines.length; i++){
                    String line = lines[i].trim();
                    if (line.isEmpty()) continue;

                    //try to match a name on this line
                    Matcher lineMatcher = MULTI_WORD_NAME.matcher(line);
                    if(lineMatcher.find()){
                        //to reduce false positives, require this line to look like a speaker cue
                        if (isCharacterCue(line)){
                            //look ahead to next non-empty line (dialogue)
                            int j = i + 1;
                            while(j < lines.length && lines[j].trim().isEmpty()) j++;
                            if (j < lines.length){
                                String next = lines[j].trim();
                                //treats next as dialogue if it is not mostly uppercase (i.e. not another cue/heading)
                                if(!isCharacterCue(next)){
                                    //capture the candidate name from the line
                                    String rawName = lineMatcher.group(1);
                                    String name = normalizeName(rawName);
                                    name = name.replaceAll("\\s+\\d+[A-Z]?\\.?\\s*$", "").trim();
                                    if (name.length() >= 2){
                                        String base = name.replaceAll("[^A-Z]", "");
                                        if (!base.isEmpty() && !STOPLIST.contains(base)){
                                            if (written.add(name)){
                                                String snippet = safeSnippet(line + " / " + next);
                                                writeRow(writer, sceneKey, name, "SPEAKER_ABOVE_DIALOGUE", snippet);
                                                if (DEBUG) System.out.printf("SPEAKER_ABOVE_DIALOGUE keep: %s -> %s%n", sceneKey, name);
                                            }
                                        } else if (DEBUG){
                                            System.out.printf("SPEAKER_ABOVE_DIALOGUE skip stoplist: %s%n", name);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                /*
                Matcher m = MULTI_WORD_NAME.matcher(content);
                while (m.find()){
                    String rawName = m.group(1); //the captured name
                    String paren = m.group(2); //parentheses content if present (e.g., O.S.)

                    //Normalize name:collapse multiple spaces, trim, replace curly apostrophes
                    String name = normalizeName(rawName);

                    //Remove trailing page-like numbers (" - 89", "89", "89A") if copied in heading text
                    name = name.replaceAll("\\s+\\d+[A-Z]?\\.?\\s*$", "").trim();

                    //skips very short or obviously non-name tokens
                    if (name.length() < 2) continue;

                    //filters stoplist (compares base token uppercase)
                    String base = name.replaceAll("[^A-Z]", "");
                    if (base.isEmpty() || STOPLIST.contains(base)){
                        if (DEBUG) System.out.println("SKIP stoplist: " + name);
                        continue;
                    }

                    //composes a rule label
                    String rule = (paren != null && ! paren.trim().isEmpty()) ? "UPPER_WITH_PAREN" : "UPPERCASE_NAME";

                    //Avoid duplicates in the same scene
                    if (written.add(name)){
                        String snippet = safeSnippet(m.group(0));
                        writeRow(writer, sceneKey, name, rule, snippet);
                        if (DEBUG) System.out.printf("KEEP: scene = %s; name = %s; rule = %s; snippet = %s%n", sceneKey, name, rule, snippet);
                    } else {
                        if (DEBUG) System.out.printf("DUP: scene = %s; name = %s; (skipped)%n", sceneKey, name);
                    }
                }
                */


            } //scenes loop
        } //writer auto-closed
        if (DEBUG) System.out.println("Character extraction finished.");
    }

    //Helper methods

    //Normalize: collapse spaces, convert curly apostrophes to straight, trim
    private static String normalizeName(String raw){
        if (raw == null) return "";
        String s = raw.replace("’", "'").replaceAll("\\s+", " ").trim();
        //removes stray trailing punctuation except middle-of-name punctuation
        s = s.replaceAll("^[^A-Z0-9]+", "").replaceAll("[^A-Z0-9]+$", "");
        return s;
    }

    //returns true if the text is mostly uppercase tokens
    private static boolean isMostlyUppercase(String text){
        String[] toks = TextUnits.tokenize(text);
        if(toks.length == 0) return false;
        int upper = 0, total = 0;
        for (String t : toks){
            String letters = t.replaceAll("[^A-Z]", "");
            if (letters.length() == 0) continue;
            total++;
            if (letters.equals(letters.toUpperCase(Locale.ROOT))) upper++;
        }
        return total > 0 && ((double) upper / total) >= 0.80; //80% tokens uppercase -> treat it as an uppercase line
    }

    private static boolean isCharacterCue(String line){
        String trimmed = line.trim();

        //Ignores very short or empty lines
        if (trimmed.isEmpty() || trimmed.length() < 2) return false;

        if(!isMostlyUppercase(trimmed)) return false;
        String[] toks = TextUnits.tokenize(trimmed);

        int nameTokens = 0;
        int tokenCount = 0;

        for (String t : toks){
            //skips pure punctuation tokens
            String lettersOnly = t.replaceAll("[^A-Z]", "");
            if (lettersOnly.length() == 0) continue;
            tokenCount++;

            //normalizes token for matching (strip punctuation that tokenizer may still keep)
            String clean = t.replaceAll("[^A-Z0-9'’\\.\\-]", "");
            if (NAME_TOKEN.matcher(clean).matches()){
                nameTokens++;
            }
        }

        if (tokenCount == 0) return false;
        if (nameTokens == 0) return false;

        //if there is at least one name-like token
        if (nameTokens >= Math.ceil(toks.length * 0.8) && tokenCount <= 5){
            //rejects if punctuation or obvious sound/transition keywords appear
            if (trimmed.matches(".*[!\\?\\,\\.;:\\(\\)\\[\\]].*")) return false;
            String up = trimmed.replaceAll("[^A-Z\\s]", "");
            for (String s : SOUND_WORDS) if (up.contains(s)) return false;
            return true;
        }
        return false;
    }

    //Create a safe snippet for CSV (collapse newlines and escape quotes)
    private  static String safeSnippet(String raw){
        if (raw == null) return "";
        String oneLine = raw.replaceAll("\\s+", " ").trim();
        //Espace any double quotes inside snippet by doubling them (CSV-compliant)
        String esc = oneLine.replace("\"", "\"\"");
        return esc;
    }

    //Writes a CSV row
    private static void writeRow(BufferedWriter writer, String sceneNumber, String name, String rule, String snippet) throws IOException{
        //name and rules are assumed safe (uppercase tokens); snippets need quoting
        StringBuilder sb = new StringBuilder();
        sb.append(sceneNumber).append(",")
                .append(name).append(",")
                .append(rule).append(",")
                .append("\"").append(snippet).append("\"");
        writer.write(sb.toString());
        writer.newLine();
    }
}

