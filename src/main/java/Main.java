import com.outlook.wn123o.mediadl.ExtractorManager;
import com.outlook.wn123o.mediadl.bean.Command;
import com.outlook.wn123o.mediadl.bean.MediaSource;
import com.outlook.wn123o.mediadl.bean.SearchResult;
import com.outlook.wn123o.mediadl.interfaces.Extractor;
import com.outlook.wn123o.mediadl.util.Settings;
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
                case "download":
                    handleDownloadAction(command);
                    break;
                case "set":
                    handleSetAction(command);
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

    private static void handleSetAction(Command cmd) {
        String args = cmd.getArgs();
        String[] parts = args.split("=");
        if (parts.length == 2) {
            Settings.putString(parts[0], parts[1]);
            System.out.println("OK.");
        } else {
            System.err.println("Invalid args! " + args);
        }
    }

    private static void handleDownloadAction(Command cmd) {
        String args = cmd.getArgs();
        int n = Integer.parseInt(args);
        SearchResult result = curResults.get(n - 1);
        MediaSource source = curExecutor.extract(result);
        if (source == null) return;
        curExecutor.download(source);
    }

    private static void handleShowAction(Command cmd) {
        String args = cmd.getArgs();
        int n = Integer.parseInt(args);
        SearchResult meta = curResults.get(n - 1);
        MediaSource source = curExecutor.extract(meta);
        if (source == null) return;
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
