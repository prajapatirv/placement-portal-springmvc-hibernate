import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/** DEMO 3: modern Java in one file (needs JDK 21+). Run: java Demo3Modern.java */
public class Demo3Modern {
    record StudentDto(String name, String branch, double cgpa) {}

    static String grade(double cgpa) {
        return switch ((int) cgpa) {
            case 9, 10 -> "Outstanding";
            case 8 -> "Excellent";
            case 7 -> "Good";
            default -> "Keep going";
        };
    }

    public static void main(String[] args) throws Exception {
        var students = List.of(
            new StudentDto("Asha", "CE", 8.6), new StudentDto("Rohan", "IT", 7.9),
            new StudentDto("Meera", "CE", 9.1), new StudentDto("Kabir", "CS", 8.2));

        Map<String, List<String>> byBranch = students.stream().collect(
            Collectors.groupingBy(StudentDto::branch,
                Collectors.mapping(StudentDto::name, Collectors.toList())));
        System.out.println(byBranch);
        students.forEach(s -> System.out.println(s.name() + " -> " + grade(s.cgpa())));

        long start = System.nanoTime();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> { Thread.sleep(1000); return null; });
            }
        }
        System.out.println("10,000 sleeping tasks finished in about "
            + (System.nanoTime() - start) / 1_000_000 + " ms");
    }
}
