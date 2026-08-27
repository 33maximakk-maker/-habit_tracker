// habit_tracker.cpp
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <map>
#include <ctime>
#include <iomanip>
#include <sstream>
#include <algorithm>
#include <json/json.h> // using jsoncpp

using namespace std;

class Tracker {
private:
    string filepath;
    map<string, vector<string>> habits;

    string today() {
        time_t t = time(nullptr);
        tm* now = localtime(&t);
        char buf[11];
        strftime(buf, sizeof(buf), "%Y-%m-%d", now);
        return string(buf);
    }

    void load() {
        ifstream ifs(filepath);
        if (!ifs) return;
        Json::Value root;
        ifs >> root;
        for (const auto& key : root.getMemberNames()) {
            vector<string> dates;
            for (const auto& d : root[key]) {
                dates.push_back(d.asString());
            }
            habits[key] = dates;
        }
    }

    void save() {
        Json::Value root;
        for (const auto& kv : habits) {
            Json::Value dates(Json::arrayValue);
            for (const auto& d : kv.second) {
                dates.append(d);
            }
            root[kv.first] = dates;
        }
        ofstream ofs(filepath);
        ofs << root.toStyledString();
    }

    string getDate(int daysOffset) {
        time_t t = time(nullptr);
        t += daysOffset * 86400;
        tm* now = localtime(&t);
        char buf[11];
        strftime(buf, sizeof(buf), "%Y-%m-%d", now);
        return string(buf);
    }

public:
    Tracker(const string& path) : filepath(path) {
        load();
    }

    void addHabit(const string& name) {
        if (habits.count(name)) {
            cout << "Привычка '" << name << "' уже существует." << endl;
            return;
        }
        habits[name] = vector<string>();
        save();
        cout << "Привычка '" << name << "' добавлена." << endl;
    }

    void removeHabit(const string& name) {
        if (!habits.count(name)) {
            cout << "Привычка '" << name << "' не найдена." << endl;
            return;
        }
        habits.erase(name);
        save();
        cout << "Привычка '" << name << "' удалена." << endl;
    }

    void checkHabit(const string& name) {
        if (!habits.count(name)) {
            cout << "Привычка '" << name << "' не найдена." << endl;
            return;
        }
        string td = today();
        auto& dates = habits[name];
        if (find(dates.begin(), dates.end(), td) != dates.end()) {
            cout << "Привычка '" << name << "' уже отмечена сегодня." << endl;
            return;
        }
        dates.push_back(td);
        save();
        cout << "Привычка '" << name << "' отмечена на сегодня." << endl;
    }

    void listHabits() {
        if (habits.empty()) {
            cout << "Нет привычек." << endl;
            return;
        }
        for (const auto& kv : habits) {
            vector<string> week;
            for (int i = 6; i >= 0; --i) {
                week.push_back(getDate(-i));
            }
            int count = 0;
            string bar;
            for (const auto& d : week) {
                if (find(kv.second.begin(), kv.second.end(), d) != kv.second.end()) {
                    bar += "[x]";
                    count++;
                } else {
                    bar += "[ ]";
                }
            }
            cout << "Привычка: " << kv.first << endl;
            cout << "  " << bar << "  (" << count << "/7, " << count*100/7 << "%)" << endl;
            cout << "  Всего дней: " << kv.second.size() << endl;
        }
    }

    void reset() {
        habits.clear();
        save();
        cout << "Все данные сброшены." << endl;
    }
};

int main(int argc, char* argv[]) {
    string add, remove, check, file = "habits.json";
    bool list = false, reset = false;

    for (int i = 1; i < argc; ++i) {
        string arg = argv[i];
        if (arg == "--add" && i+1 < argc) add = argv[++i];
        else if (arg == "--remove" && i+1 < argc) remove = argv[++i];
        else if (arg == "--check" && i+1 < argc) check = argv[++i];
        else if (arg == "--list") list = true;
        else if (arg == "--reset") reset = true;
        else if (arg == "--file" && i+1 < argc) file = argv[++i];
    }

    Tracker tracker(file);
    if (!add.empty()) tracker.addHabit(add);
    else if (!remove.empty()) tracker.removeHabit(remove);
    else if (!check.empty()) tracker.checkHabit(check);
    else if (list) tracker.listHabits();
    else if (reset) tracker.reset();
    else cout << "Используйте --help для справки" << endl;
    return 0;
}
