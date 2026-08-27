// habit_tracker.js
const { program } = require('commander');
const fs = require('fs');
const chalk = require('chalk');

const DEFAULT_FILE = 'habits.json';

class HabitTracker {
    constructor(filepath = DEFAULT_FILE) {
        this.filepath = filepath;
        this.data = this.load();
    }

    load() {
        if (fs.existsSync(this.filepath)) {
            return JSON.parse(fs.readFileSync(this.filepath));
        }
        return { habits: {} };
    }

    save() {
        fs.writeFileSync(this.filepath, JSON.stringify(this.data, null, 2));
    }

    addHabit(name) {
        if (this.data.habits[name]) {
            console.log(chalk.yellow(`Привычка '${name}' уже существует.`));
            return;
        }
        this.data.habits[name] = [];
        this.save();
        console.log(chalk.green(`Привычка '${name}' добавлена.`));
    }

    removeHabit(name) {
        if (!this.data.habits[name]) {
            console.log(chalk.red(`Привычка '${name}' не найдена.`));
            return;
        }
        delete this.data.habits[name];
        this.save();
        console.log(chalk.green(`Привычка '${name}' удалена.`));
    }

    checkHabit(name) {
        if (!this.data.habits[name]) {
            console.log(chalk.red(`Привычка '${name}' не найдена.`));
            return;
        }
        const today = new Date().toISOString().split('T')[0];
        const dates = this.data.habits[name];
        if (dates.includes(today)) {
            console.log(chalk.yellow(`Привычка '${name}' уже отмечена сегодня.`));
            return;
        }
        dates.push(today);
        this.save();
        console.log(chalk.green(`Привычка '${name}' отмечена на сегодня.`));
    }

    listHabits() {
        const habits = this.data.habits;
        if (Object.keys(habits).length === 0) {
            console.log(chalk.yellow('Нет привычек.'));
            return;
        }
        const today = new Date();
        for (const [name, dates] of Object.entries(habits)) {
            const weekDates = [];
            for (let i = 6; i >= 0; i--) {
                const d = new Date(today);
                d.setDate(d.getDate() - i);
                weekDates.push(d.toISOString().split('T')[0]);
            }
            const done = weekDates.map(d => dates.includes(d));
            const count = done.filter(Boolean).length;
            const total = weekDates.length;
            const bar = done.map(d => d ? '[x]' : '[ ]').join('');
            console.log(chalk.cyan(`Привычка: ${name}`));
            console.log(`  ${bar}  (${count}/${total}, ${Math.round(count/total*100)}%)`);
            console.log(`  Всего дней: ${new Set(dates).size}`);
        }
    }

    reset() {
        this.data = { habits: {} };
        this.save();
        console.log(chalk.green('Все данные сброшены.'));
    }
}

program
    .option('--add <name>', 'Добавить привычку')
    .option('--remove <name>', 'Удалить привычку')
    .option('--check <name>', 'Отметить выполнение')
    .option('--list', 'Показать все привычки')
    .option('--reset', 'Сбросить данные')
    .option('--file <path>', 'Файл данных', DEFAULT_FILE)
    .parse(process.argv);

const opts = program.opts();
const tracker = new HabitTracker(opts.file);

if (opts.add) tracker.addHabit(opts.add);
else if (opts.remove) tracker.removeHabit(opts.remove);
else if (opts.check) tracker.checkHabit(opts.check);
else if (opts.list) tracker.listHabits();
else if (opts.reset) tracker.reset();
else program.help();
