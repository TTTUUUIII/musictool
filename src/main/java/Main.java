import com.outlook.wn123o.mediadl.ExtractorManager;
import com.outlook.wn123o.mediadl.bean.Command;
import com.outlook.wn123o.mediadl.bean.MediaSource;
import com.outlook.wn123o.mediadl.bean.SearchResult;
import com.outlook.wn123o.mediadl.interfaces.Extractor;
import de.vandermeer.asciitable.AsciiTable;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Extractor curExecutor;
    private static List<SearchResult> curResults;
    public static void main(String[] args) {
        boolean shouldExit = false;
        Scanner scanner = new Scanner(System.in);
        while (!shouldExit) {
            System.out.println();
            System.out.print("mediadl>");
            Command command = parseCommand(scanner.nextLine());
            switch (command.getAction()) {
                case "use":
                    handleUseAction(command);
                    break;
                case "search":
                    handleSearchAction(command);
                    break;
                case "show":
                    handleShowAction(command);
                    break;
                case "exit":
                case "q":
                case "quit":
                    shouldExit = true;
                    break;
                default:
                    System.err.println("Invalid action! " + command.getAction());
            }
        }
    }

    private static void handleShowAction(Command cmd) {
        String args = cmd.getArgs();
        int n = Integer.parseInt(args);
        SearchResult meta = curResults.get(n - 1);
        MediaSource source = curExecutor.extract(meta);
        System.out.println("Title: " + source.getTitle() + " - " + source.getArtist());
        System.out.println("Album: " + source.getAlbum());
        System.out.println("Quality: " + source.getQuality());
        System.out.println("Size: " + source.getSize());
        System.out.println("Format: " + source.getFormat());
        System.out.println("Url: " + source.getUrl());
        System.out.println("Cover url: " + source.getCoverUrl());
    }

    private static void handleSearchAction(Command cmd) {
        String args = cmd.getArgs();
        curResults = curExecutor.search(args);
        AsciiTable at = new AsciiTable();
        at.addRule();
        at.addRow("No.", "Title", "Album");
        at.addRule();
        for (int i = 0; i < curResults.size(); i++) {
            SearchResult result = curResults.get(i);
            at.addRow(i + 1, result.getTitle() + " - " + result.getArtist(), result.getAlbum());
            at.addRule();
        }
        at.addRule();
        System.out.println(at.render());
    }

    private static void handleUseAction(Command cmd) {
        String args = cmd.getArgs();
        if (ExtractorManager.getInstance().getExtractor(args) != null) {
            curExecutor = ExtractorManager.getInstance().getExtractor(args);
            System.out.println("OK.");
        } else {
            System.err.println("Failed! Extractor not exists!");
        }
    }

    private static Command parseCommand(String s) {
        String action = "";
        String args = null;
        int i = s.indexOf(" ");
        if (i == -1) {
            action = s;
        } else {
            action = s.substring(0, i);
            args = s.substring(i + 1);
        }
        return Command.builder().action(action).args(args).build();
    }
}
