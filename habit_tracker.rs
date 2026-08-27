// habit_tracker.rs
use chrono::{Local, Duration};
use clap::{App, Arg};
use serde::{Deserialize, Serialize};
use serde_json;
use std::collections::HashMap;
use std::fs;
use std::path::Path;

#[derive(Serialize, Deserialize)]
struct Data {
    habits: HashMap<String, Vec<String>>,
}

impl Default for Data {
    fn default() -> Self {
        Data { habits: HashMap::new() }
    }
}

struct Tracker {
    filepath: String,
    data: Data,
}

impl Tracker {
    fn new(filepath: &str) -> Self {
        let data = if Path::new(filepath).exists() {
            let content = fs::read_to_string(filepath).unwrap_or_default();
            serde_json::from_str(&content).unwrap_or_default()
        } else {
            Data::default()
        };
        Tracker { filepath: filepath.to_string(), data }
    }

    fn save(&self) {
        let json = serde_json::to_string_pretty(&self.data).unwrap();
        fs::write(&self.filepath, json).unwrap();
    }

    fn add_habit(&mut self, name: &str) {
        if self.data.habits.contains_key(name) {
            println!("Привычка '{}' уже существует.", name);
            return;
        }
        self.data.habits.insert(name.to_string(), Vec::new());
        self.save();
        println!("Привычка '{}' добавлена.", name);
    }

    fn remove_habit(&mut self, name: &str) {
        if !self.data.habits.contains_key(name) {
            println!("Привычка '{}' не найдена.", name);
            return;
        }
        self.data.habits.remove(name);
        self.save();
        println!("Привычка '{}' удалена.", name);
    }

    fn check_habit(&mut self, name: &str) {
        let dates = self.data.habits.get_mut(name);
        if let Some(dates) = dates {
            let today = Local::now().format("%Y-%m-%d").to_string();
            if dates.contains(&today) {
                println!("Привычка '{}' уже отмечена сегодня.", name);
                return;
            }
            dates.push(today);
            self.save();
            println!("Привычка '{}' отмечена на сегодня.", name);
        } else {
            println!("Привычка '{}' не найдена.", name);
        }
    }

    fn list_habits(&self) {
        if self.data.habits.is_empty() {
            println!("Нет привычек.");
            return;
        }
        let today = Local::now();
        for (name, dates) in &self.data.habits {
            // Последние 7 дней
            let week_dates: Vec<String> = (0..7)
                .map(|i| (today - Duration::days(6 - i)).format("%Y-%m-%d").to_string())
                .collect();
            let count = week_dates.iter().filter(|d| dates.contains(d)).count();
            let bar: String = week_dates
                .iter()
                .map(|d| if dates.contains(d) { "[x]" } else { "[ ]" })
                .collect();
            println!("Привычка: {}", name);
            println!("  {}  ({}/7, {}%)", bar, count, count * 100 / 7);
            println!("  Всего дней: {}", dates.len());
        }
    }

    fn reset(&mut self) {
        self.data.habits.clear();
        self.save();
        println!("Все данные сброшены.");
    }
}

fn main() {
    let matches = App::new("Трекер привычек")
        .arg(Arg::with_name("add").long("add").takes_value(true).help("Добавить привычку"))
        .arg(Arg::with_name("remove").long("remove").takes_value(true).help("Удалить привычку"))
        .arg(Arg::with_name("check").long("check").takes_value(true).help("Отметить выполнение"))
        .arg(Arg::with_name("list").long("list").help("Показать все привычки"))
        .arg(Arg::with_name("reset").long("reset").help("Сбросить данные"))
        .arg(Arg::with_name("file").long("file").takes_value(true).default_value("habits.json"))
        .get_matches();

    let file = matches.value_of("file").unwrap();
    let mut tracker = Tracker::new(file);

    if let Some(name) = matches.value_of("add") {
        tracker.add_habit(name);
    } else if let Some(name) = matches.value_of("remove") {
        tracker.remove_habit(name);
    } else if let Some(name) = matches.value_of("check") {
        tracker.check_habit(name);
    } else if matches.is_present("list") {
        tracker.list_habits();
    } else if matches.is_present("reset") {
        tracker.reset();
    } else {
        println!("Используйте --help для справки");
    }
}
