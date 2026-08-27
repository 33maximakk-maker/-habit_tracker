// HabitTracker.cs
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace HabitTracker
{
    class Program
    {
        static void Main(string[] args)
        {
            var opts = new Options();
            for (int i = 0; i < args.Length; i++)
            {
                switch (args[i])
                {
                    case "--add": opts.Add = args[++i]; break;
                    case "--remove": opts.Remove = args[++i]; break;
                    case "--check": opts.Check = args[++i]; break;
                    case "--list": opts.List = true; break;
                    case "--reset": opts.Reset = true; break;
                    case "--file": opts.File = args[++i]; break;
                    default: Console.WriteLine("Неизвестный аргумент"); return;
                }
            }
            var tracker = new Tracker(opts.File);
            if (!string.IsNullOrEmpty(opts.Add)) tracker.AddHabit(opts.Add);
            else if (!string.IsNullOrEmpty(opts.Remove)) tracker.RemoveHabit(opts.Remove);
            else if (!string.IsNullOrEmpty(opts.Check)) tracker.CheckHabit(opts.Check);
            else if (opts.List) tracker.ListHabits();
            else if (opts.Reset) tracker.ResetData();
            else Console.WriteLine("Используйте --help для справки");
        }

        class Options
        {
            public string Add { get; set; }
            public string Remove { get; set; }
            public string Check { get; set; }
            public bool List { get; set; }
            public bool Reset { get; set; }
            public string File { get; set; } = "habits.json";
        }

        class Tracker
        {
            private Dictionary<string, List<string>> habits;
            private readonly string filepath;

            public Tracker(string filepath)
            {
                this.filepath = filepath;
                Load();
            }

            private void Load()
            {
                if (File.Exists(filepath))
                {
                    string json = File.ReadAllText(filepath);
                    habits = JsonSerializer.Deserialize<Dictionary<string, List<string>>>(json) ?? new Dictionary<string, List<string>>();
                }
                else
                {
                    habits = new Dictionary<string, List<string>>();
                }
            }

            private void Save()
            {
                string json = JsonSerializer.Serialize(habits, new JsonSerializerOptions { WriteIndented = true });
                File.WriteAllText(filepath, json);
            }

            public void AddHabit(string name)
            {
                if (habits.ContainsKey(name))
                {
                    Console.WriteLine($"Привычка '{name}' уже существует.");
                    return;
                }
                habits[name] = new List<string>();
                Save();
                Console.WriteLine($"Привычка '{name}' добавлена.");
            }

            public void RemoveHabit(string name)
            {
                if (!habits.ContainsKey(name))
                {
                    Console.WriteLine($"Привычка '{name}' не найдена.");
                    return;
                }
                habits.Remove(name);
                Save();
                Console.WriteLine($"Привычка '{name}' удалена.");
            }

            public void CheckHabit(string name)
            {
                if (!habits.ContainsKey(name))
                {
                    Console.WriteLine($"Привычка '{name}' не найдена.");
                    return;
                }
                string today = DateTime.UtcNow.ToString("yyyy-MM-dd");
                if (habits[name].Contains(today))
                {
                    Console.WriteLine($"Привычка '{name}' уже отмечена сегодня.");
                    return;
                }
                habits[name].Add(today);
                Save();
                Console.WriteLine($"Привычка '{name}' отмечена на сегодня.");
            }

            public void ListHabits()
            {
                if (habits.Count == 0)
                {
                    Console.WriteLine("Нет привычек.");
                    return;
                }
                var today = DateTime.UtcNow;
                foreach (var kv in habits)
                {
                    var week = new List<string>();
                    for (int i = 6; i >= 0; i--)
                    {
                        week.Add(today.AddDays(-i).ToString("yyyy-MM-dd"));
                    }
                    int count = week.Count(d => kv.Value.Contains(d));
                    string bar = string.Join("", week.Select(d => kv.Value.Contains(d) ? "[x]" : "[ ]"));
                    Console.WriteLine($"Привычка: {kv.Key}");
                    Console.WriteLine($"  {bar}  ({count}/7, {count*100/7}%)");
                    Console.WriteLine($"  Всего дней: {kv.Value.Count}");
                }
            }

            public void ResetData()
            {
                habits.Clear();
                Save();
                Console.WriteLine("Все данные сброшены.");
            }
        }
    }
}
