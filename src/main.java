import task1.Task1Processor;

import java.util.List;
import java.util.Optional;
import java.nio.file.Path;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <dblp-xml.gz> <dblp.dtd> [--limit=N]");
            return;
        }

        String xmlPath = args[0];
        String dtdPath = args[1];

        int limit = Integer.MAX_VALUE;
        int printEvery = 100000;

        if (args.length >= 3 && args[2].startsWith("--limit=")) {
            limit = Integer.parseInt(args[2].substring("--limit=".length()));
        }

        Task1Processor task1 = new Task1Processor(printEvery);

        try {
            DblpPublicationGenerator generator =
                new DblpPublicationGenerator(Path.of(xmlPath), Path.of(dtdPath), limit);

            Optional<?> optionalPublication;
            while ((optionalPublication = generator.nextPublication()).isPresent()) {
                Object publication = optionalPublication.get();
                List<String> authors = extractAuthors(publication);
                task1.processPublication(authors);
            }

            task1.writeHistogram("output/task1_histogram.txt");

            System.out.println("Done.");
            System.out.println("Final number of communities: " + task1.getCommunityCount());
            System.out.println("Histogram written to output/task1_histogram.txt");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> extractAuthors(Object publication) {
        Class<?> cls = publication.getClass();

        // 1) Try field: authors
        try {
            Field f = cls.getDeclaredField("authors");
            f.setAccessible(true);
            Object v = f.get(publication);
            if (v instanceof List<?>) return (List<String>) v;
        } catch (Exception ignored) {}

        // 2) Try getter: getAuthors()
        try {
            Method m = cls.getDeclaredMethod("getAuthors");
            m.setAccessible(true);
            Object v = m.invoke(publication);
            if (v instanceof List<?>) return (List<String>) v;
        } catch (Exception ignored) {}

        // 3) Print what the real publication object contains, then stop
        System.out.println("Could not extract authors.");
        System.out.println("Real publication class: " + cls.getName());

        System.out.println("Declared fields:");
        for (Field f : cls.getDeclaredFields()) {
            System.out.println("  " + f.getName() + " : " + f.getType().getName());
        }

        System.out.println("Declared methods:");
        for (Method m : cls.getDeclaredMethods()) {
            System.out.println("  " + m.getName());
        }

        throw new RuntimeException("Author extraction failed.");
    }
}