// habit_tracker.go
package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"os"
	"time"
)

type HabitTracker struct {
	filepath string
	data     map[string]interface{}
}

func NewHabitTracker(filepath string) *HabitTracker {
	ht := &HabitTracker{filepath: filepath}
	ht.load()
	return ht
}

func (ht *HabitTracker) load() {
	if _, err := os.Stat(ht.filepath); err == nil {
		b, err := os.ReadFile(ht.filepath)
		if err == nil {
			json.Unmarshal(b, &ht.data)
		}
	}
	if ht.data == nil {
		ht.data = make(map[string]interface{})
	}
	if _, ok := ht.data["habits"]; !ok {
		ht.data["habits"] = make(map[string][]string)
	}
}

func (ht *HabitTracker) save() {
	b, _ := json.MarshalIndent(ht.data, "", "  ")
	os.WriteFile(ht.filepath, b, 0644)
}

func (ht *HabitTracker) addHabit(name string) {
	habits := ht.data["habits"].(map[string][]string)
	if _, ok := habits[name]; ok {
		fmt.Printf("Привычка '%s' уже существует.\n", name)
		return
	}
	habits[name] = []string{}
	ht.save()
	fmt.Printf("Привычка '%s' добавлена.\n", name)
}

func (ht *HabitTracker) removeHabit(name string) {
	habits := ht.data["habits"].(map[string][]string)
	if _, ok := habits[name]; !ok {
		fmt.Printf("Привычка '%s' не найдена.\n", name)
		return
	}
	delete(habits, name)
	ht.save()
	fmt.Printf("Привычка '%s' удалена.\n", name)
}

func (ht *HabitTracker) checkHabit(name string) {
	habits := ht.data["habits"].(map[string][]string)
	dates, ok := habits[name]
	if !ok {
		fmt.Printf("Привычка '%s' не найдена.\n", name)
		return
	}
	today := time.Now().Format("2006-01-02")
	for _, d := range dates {
		if d == today {
			fmt.Printf("Привычка '%s' уже отмечена сегодня.\n", name)
			return
		}
	}
	habits[name] = append(dates, today)
	ht.save()
	fmt.Printf("Привычка '%s' отмечена на сегодня.\n", name)
}

func (ht *HabitTracker) listHabits() {
	habits := ht.data["habits"].(map[string][]string)
	if len(habits) == 0 {
		fmt.Println("Нет привычек.")
		return
	}
	today := time.Now()
	for name, dates := range habits {
		week := make([]string, 7)
		for i := 0; i < 7; i++ {
			d := today.AddDate(0, 0, -6+i)
			week[i] = d.Format("2006-01-02")
		}
		count := 0
		for _, d := range week {
			for _, v := range dates {
				if v == d {
					count++
					break
				}
			}
		}
		bar := ""
		for _, d := range week {
			found := false
			for _, v := range dates {
				if v == d {
					found = true
					break
				}
			}
			if found {
				bar += "[x]"
			} else {
				bar += "[ ]"
			}
		}
		fmt.Printf("Привычка: %s\n", name)
		fmt.Printf("  %s  (%d/7, %d%%)\n", bar, count, count*100/7)
		fmt.Printf("  Всего дней: %d\n", len(dates))
	}
}

func (ht *HabitTracker) reset() {
	ht.data["habits"] = make(map[string][]string)
	ht.save()
	fmt.Println("Все данные сброшены.")
}

func main() {
	var (
		add    string
		remove string
		check  string
		list   bool
		reset  bool
		file   string
	)
	flag.StringVar(&add, "add", "", "Добавить привычку")
	flag.StringVar(&remove, "remove", "", "Удалить привычку")
	flag.StringVar(&check, "check", "", "Отметить выполнение")
	flag.BoolVar(&list, "list", false, "Показать все привычки")
	flag.BoolVar(&reset, "reset", false, "Сбросить данные")
	flag.StringVar(&file, "file", "habits.json", "Файл данных")
	flag.Parse()

	tracker := NewHabitTracker(file)
	if add != "" {
		tracker.addHabit(add)
	} else if remove != "" {
		tracker.removeHabit(remove)
	} else if check != "" {
		tracker.checkHabit(check)
	} else if list {
		tracker.listHabits()
	} else if reset {
		tracker.reset()
	} else {
		fmt.Println("Используйте --help для справки")
	}
}
