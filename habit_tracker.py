
# habit_tracker.py
import json
import os
import sys
from datetime import datetime, timedelta
from colorama import init, Fore, Style

init(autoreset=True)

class HabitTracker:
    def __init__(self, filepath="habits.json"):
        self.filepath = filepath
        self.data = self.load()

    def load(self):
        if os.path.exists(self.filepath):
            with open(self.filepath, 'r') as f:
                return json.load(f)
        return {"habits": {}}

    def save(self):
        with open(self.filepath, 'w') as f:
            json.dump(self.data, f, indent=2)

    def add_habit(self, name):
        if name in self.data["habits"]:
            print(Fore.YELLOW + f"Привычка '{name}' уже существует.")
            return
        self.data["habits"][name] = []
        self.save()
        print(Fore.GREEN + f"Привычка '{name}' добавлена.")

    def remove_habit(self, name):
        if name not in self.data["habits"]:
            print(Fore.RED + f"Привычка '{name}' не найдена.")
            return
        del self.data["habits"][name]
        self.save()
        print(Fore.GREEN + f"Привычка '{name}' удалена.")

    def check_habit(self, name):
        if name not in self.data["habits"]:
            print(Fore.RED + f"Привычка '{name}' не найдена.")
            return
        today = datetime.now().date().isoformat()
        if today in self.data["habits"][name]:
            print(Fore.YELLOW + f"Привычка '{name}' уже отмечена сегодня.")
            return
        self.data["habits"][name].append(today)
        self.save()
        print(Fore.GREEN + f"Привычка '{name}' отмечена на сегодня.")

    def list_habits(self):
        if not self.data["habits"]:
            print(Fore.YELLOW + "Нет привычек.")
            return
        today = datetime.now().date()
        for name, dates in self.data["habits"].items():
            # Прогресс за последние 7 дней
            week_dates = [(today - timedelta(days=i)).isoformat() for i in range(6, -1, -1)]
            done = [d in dates for d in week_dates]
            count = sum(done)
            total = len(week_dates)
            # Визуализация
            bar = "".join("[x]" if d else "[ ]" for d in done)
            print(Fore.CYAN + f"Привычка: {name}")
            print(f"  {bar}  ({count}/{total}, {count/total*100:.0f}%)")
            # Статистика всех выполнений
            total_days = len(set(dates))
            print(f"  Всего дней: {total_days}")

    def reset(self):
        self.data = {"habits": {}}
        self.save()
        print(Fore.GREEN + "Все данные сброшены.")

def main():
    import argparse
    parser = argparse.ArgumentParser(description="Трекер привычек")
    parser.add_argument("--add", help="Добавить привычку")
    parser.add_argument("--remove", help="Удалить привычку")
    parser.add_argument("--check", help="Отметить привычку выполненной сегодня")
    parser.add_argument("--list", action="store_true", help="Показать все привычки")
    parser.add_argument("--reset", action="store_true", help="Сбросить все данные")
    parser.add_argument("--file", default="habits.json", help="Файл данных")
    args = parser.parse_args()

    tracker = HabitTracker(args.file)

    if args.add:
        tracker.add_habit(args.add)
    elif args.remove:
        tracker.remove_habit(args.remove)
    elif args.check:
        tracker.check_habit(args.check)
    elif args.list:
        tracker.list_habits()
    elif args.reset:
        tracker.reset()
    else:
        parser.print_help()

if __name__ == "__main__":
    main()
