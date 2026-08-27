// HabitTracker.java
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HabitTracker {
    @Parameter(names = "--add")
    private String add;
    @Parameter(names = "--remove")
    private String remove;
    @Parameter(names = "--check")
    private String check;
    @Parameter(names = "--list")
    private boolean list;
    @Parameter(names = "--reset")
    private boolean reset;
    @Parameter(names = "--file")
    private String file = "habits.json";

    private Map<String, List<String>> habits;
    private String filepath;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) {
        HabitTracker tracker = new HabitTracker();
        JCommander jc = JCommander.newBuilder().addObject(tracker).build();
        jc.parse(args);
        tracker.run();
    }

    public void run() {
        filepath = file;
        load();
        if (add != null) addHabit(add);
        else if (remove != null) removeHabit(remove);
        else if (check != null) checkHabit(check);
        else if (list) listHabits();
        else if (reset) resetData();
        else System.out.println("Используйте --help для справки");
    }

    @SuppressWarnings("unchecked")
    private void load() {
        File f = new File(filepath);
        if (f.exists()) {
            try (Reader reader = new FileReader(f)) {
                Type type = new TypeToken<Map<String, List<String>>>(){}.getType();
                habits = gson.fromJson(reader, type);
            } catch (IOException e) {
                habits = new HashMap<>();
            }
        } else {
            habits = new HashMap<>();
        }
    }

    private void save() {
        try (Writer writer = new FileWriter(filepath)) {
            gson.toJson(habits, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void addHabit(String name) {
        if (habits.containsKey(name)) {
            System.out.println("Привычка '" + name + "' уже существует.");
            return;
        }
        habits.put(name, new ArrayList<>());
        save();
        System.out.println("Привычка '" + name + "' добавлена.");
    }

    private void removeHabit(String name) {
        if (!habits.containsKey(name)) {
            System.out.println("Привычка '" + name + "' не найдена.");
            return;
        }
        habits.remove(name);
        save();
        System.out.println("Привычка '" + name + "' удалена.");
    }

    private void checkHabit(String name) {
        if (!habits.containsKey(name)) {
            System.out.println("Привычка '" + name + "' не найдена.");
            return;
        }
        String today = LocalDate.now().toString();
        List<String> dates = habits.get(name);
        if (dates.contains(today)) {
            System.out.println("Привычка '" + name + "' уже отмечена сегодня.");
            return;
        }
        dates.add(today);
        save();
        System.out.println("Привычка '" + name + "' отмечена на сегодня.");
    }

    private void listHabits() {
        if (habits.isEmpty()) {
            System.out.println("Нет привычек.");
            return;
        }
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (Map.Entry<String, List<String>> entry : habits.entrySet()) {
            String name = entry.getKey();
            List<String> dates = entry.getValue();
            // Последние 7 дней
            List<String> week = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                week.add(today.minusDays(i).format(fmt));
            }
            int count = 0;
            StringBuilder bar = new StringBuilder();
            for (String d : week) {
                if (dates.contains(d)) {
                    bar.append("[x]");
                    count++;
                } else {
                    bar.append("[ ]");
                }
            }
            System.out.println("Привычка: " + name);
            System.out.printf("  %s  (%d/7, %d%%)%n", bar, count, count*100/7);
            System.out.println("  Всего дней: " + dates.size());
        }
    }

    private void resetData() {
        habits.clear();
        save();
        System.out.println("Все данные сброшены.");
    }
}
