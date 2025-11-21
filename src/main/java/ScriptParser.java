import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.*;
import java.util.*;

public class ScriptParser {
    //load a script file into a String
    public static String loadScript(String filePath) throws IOException {
        String script = new String(Files.readAllBytes(Paths.get(filePath)));
        script = script
                .replaceAll("\u00A0", " ") //replaces non-breaking spaces
                .replaceAll("\\r", "") //normalizes line breaks (carriage returns)
                .replaceAll("(?m)^\\s+", "") //removes leading spaces on each line
                .replaceAll("(?m)\\s+$", ""); //removes trailing spaces
        return script;
    }

    //Removes page headers/footers and obvious page number-only lines,
    //but only if they are not immediately followed by a heading keyword
    private String removePageNumbers(String script){
        String[] lines = script.split("\n", -1);
        StringBuilder out = new StringBuilder();

        Pattern numberOnly = Pattern.compile("^\\s*(\\d+)\\s*\\.?\\s*\\*?\\s*$"); //"98", "98.", "98 *"
        Pattern repeatedNumberLine = Pattern.compile("^\\s*(\\d+)\\s+\\1\\s*$"); //"98  98"
        Pattern headingKeyword = Pattern.compile("^(INT\\.?|EXT\\.?|INT/EXT\\.?|EXT/INT\\.?|I/E\\.?|EST\\.?|OMITTED)\\b",
                Pattern.CASE_INSENSITIVE);


        for (int i = 0; i< lines.length; i++){
            String line = lines[i];
            String trimmed = line.trim();

            //case A: repeated number line (page header/footr like "98  98")
            if (repeatedNumberLine.matcher(trimmed).find()) {
                //capture the number
                Matcher rep = Pattern.compile("^\\s*(\\d+)\\s+\\1\\s*$").matcher(trimmed);
                String repNum = null;
                if (rep.find()) repNum = rep.group(1);

                int j = i + 1;
                while (j < lines.length && lines[j].trim().isEmpty()) j++;
                int k = i - 1;
                while(k >= 0 && lines[k].trim().isEmpty()) k--;

                boolean nextIsHeading = j < lines.length && headingKeyword.matcher(lines[j].trim()).find();
                boolean prevIsHeading = k >= 0 && headingKeyword.matcher(lines[k].trim()).find();

                //If adjacent to a heading, keep the number (as a single number line),
                //so the main parser will treat it as a scene-number line above the heading.
                if(nextIsHeading || prevIsHeading){
                    if (repNum != null){
                        //write single number line (not the repeated layout)
                        out.append(repNum).append("\n");
                    }
                }
                continue;
            }

            //case B: number-only line -> *maybe* page number: keeps it if next meaningful line is a heading,
            //otherwise drops it (treats it as a page number).
            Matcher mnum = numberOnly.matcher(trimmed);
            if (mnum.find()){
                //looks ahead to next non-empty, non-dash line
                int j = i + 1;
                while(j < lines.length && lines[j].trim().isEmpty()) j++;
                boolean nextIsHeading = false;
                if (j < lines.length){
                    nextIsHeading = headingKeyword.matcher(lines[j].trim()).find();
                }

                if (!nextIsHeading){
                    //skip this line (likely page number)
                    continue;
                }
                //else keep it (it's probably a scene number above a heading)
            }
            //normal lines (keep)
            out.append(line).append("\n");
        }
        return out.toString();
    }
    //Main split function (line-oriented, robust)
    public List<Scene> splitScenes(String script) {
        // 1) pre-clean page numbers/headers
        script = removePageNumbers(script);

        // 2) split into lines
        String[] lines = script.split("\n", -1);

        //Patterns
        Pattern numberOnly = Pattern.compile("^\\s*(\\d+[A-Z]?)\\s*\\.?\\s*\\*?\\s*$"); // "98" or "98A"
        Pattern headingPattern = Pattern.compile("^(?:((\\d+[A-Z]?)\\s+)?(INT\\.?|EXT\\.?|INT/EXT\\.?|EXT/INT\\.?|I/E\\.?|E/I\\.?|EST\\.?|OMITTED)\\b\\s*([^\\-\\n]*?)(?:-\\s*(.*))?)$",
                Pattern.CASE_INSENSITIVE);
        Pattern headingKeyword = Pattern.compile("^(INT\\.?|EXT\\.?|INT/EXT\\.?|EXT/INT\\.?|I/E\\.?|E/I\\.?|EST\\.?|OMITTED)\\b",
                Pattern.CASE_INSENSITIVE);

        List<Scene> scenes = new ArrayList<>();


        int sceneRawCounter = 0; //actual number of all scenes (for example, without "98A")
        int fallbackCounter = 1; //number when the String scene numbering fails, skipped when a scene with a letter
        String pendingSceneNumber = null; //number found on its own line (above heading)

        String currentSceneNumber = null;
        String currentHeading = null;
        String currentLocation = null;
        String currentTime = null;
        StringBuilder currentContent = new StringBuilder();

        boolean debug = false;

        for (int i = 0; i < lines.length; i++) {
            String raw = lines[i];
            String line = raw.trim();

            if (line.isEmpty()) {
                //blank or separator: keep as possible content (preserve blank lines only content),
                //but if no current scene, skip extra blanks
                if (currentHeading != null && currentContent.length() > 0) {
                    currentContent.append("\n");
                }
                continue;
            }

            //If this line is a number alone, check lookahead to decide if it's a scene number or a page number
            Matcher numOnlyM = numberOnly.matcher(line);
            if (numOnlyM.find()) {
                //Look ahead to next non-empty line
                int j = i + 1;
                while (j < lines.length && lines[j].trim().isEmpty()) j++;
                boolean nextIsHeading = false;
                if (j < lines.length) {
                    nextIsHeading = headingKeyword.matcher(lines[j].trim()).find();
                }
                if (nextIsHeading) {
                    //this is a scene-number line (store and skip it, the next line will be heading)
                    pendingSceneNumber = numOnlyM.group(1); //e.g., "98" or "98A"
                    if (debug) System.out.println("DEBUG: pending scene number = " + pendingSceneNumber + " (line " + i + ")");
                    continue; //proceed to the line (which should be the heading)
                } else {
                    //it's a page number or stray number -> skip it
                    if (debug) System.out.println("DEBUG: dropped page number line: " + line + " (line " + i + ")");
                    continue;
                }


            }
            //Check if this is a heading line (inline number optional)
            Matcher h = headingPattern.matcher(line);
            if (h.find()) {
                //If we were building a previous scene, finalize it now
                if (currentHeading != null) {
                    sceneRawCounter++;
                    scenes.add(new Scene(sceneRawCounter, currentSceneNumber, currentHeading, currentContent.toString().trim(), currentLocation, currentTime));
                    currentContent.setLength(0);
                    currentHeading = null;
                    currentLocation = null;
                    currentTime = null;
                    currentSceneNumber = null;
                }

                //Determine scene number: pending (number-on-previous-line) has priority
                String sceneNumber = (pendingSceneNumber != null) ? pendingSceneNumber : (h.group(2) != null ? h.group(2).trim() : "");
                pendingSceneNumber = null;

                //If no number, use fallback
                if (sceneNumber.isEmpty()) {
                    sceneNumber = String.valueOf(fallbackCounter);
                }

                //Normalize: if pure numeric, sync fallback
                if (sceneNumber.matches("^\\d+$")) {
                    //fallbackCounter = Integer.parseInt(sceneNumber);
                }

                //Extract location (the keyword), description and time
                String locationKeyword = h.group(3) != null ? h.group(3).trim().toUpperCase() : "";
                String locationDesc = h.group(4) != null ? h.group(4).trim() : "";
                String time = h.group(5) != null ? h.group(5).trim() : "";

                locationDesc = locationDesc.replaceAll("\\s+\\d+[A-Z]?(\\s*\\*?)?$", "").trim();

                //Build the displayed heading (keep keyword)
                String displayedHeading = locationKeyword + (locationDesc.isEmpty() ? "" : " " + locationDesc) + (time.isEmpty() ? "" : " - " + time);

                //Set current scene varibles
                currentSceneNumber = sceneNumber;
                currentHeading = displayedHeading;
                currentLocation = locationKeyword;
                currentTime = time;

                //increment fallback for next scene (only if numeric)
                if (sceneNumber.matches("^\\d+$")) {
                    fallbackCounter = Integer.parseInt(sceneNumber) + 1;
                }
                if (debug) {
                    System.out.println("DEBUG: new heading -> sceneNumber=" + currentSceneNumber + ", heading='" + currentHeading + "'");
                }
                continue; //heading handled
            }
            //Not a heading, not a page number: treat as content for current scene (if any)
            if (currentHeading != null) {
                //append the raw line (preserve formatting within content)
                if (currentContent.length() > 0) currentContent.append("\n");
                currentContent.append(raw);
            }

        }
        //finalize last scene
        if (currentHeading != null) {
            sceneRawCounter++;
            scenes.add(new Scene(sceneRawCounter, currentSceneNumber, currentHeading, currentContent.toString().trim(), currentLocation, currentTime));
        }
        return scenes;
    }
}