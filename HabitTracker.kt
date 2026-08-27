// HabitTracker.kt
import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitTracker {
    @Parameter(names = ["--add"])
    private var add: String? = null

    @Parameter(names = ["--remove"])
    private var remove: String? = null

    @Parameter(names = ["--check"])
    private var check: String? = null

    @Parameter(names = ["--list"])
    private var list: Boolean = false

    @Parameter(names = ["--reset"])
    private var reset: Boolean = false

    @Parameter(names = ["--file"])
    private var file: String = "habits.json"

    private lateinit var habits: MutableMap<String, MutableList<String>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val type = object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type

    private fun load() {
        val f = File(file)
        habits = if (f.exists()) {
            val json = f.readText()
            gson.fromJson(json, type) ?: mutableMapOf()
        } else {
            mutableMapOf()
        }
    }

    private fun save() {
        File(file).writeText(gson.toJson(habits))
    }

    private fun addHabit(name: String) {
        if (habits.containsKey(name)) {
            println("Привычка '$name' уже существует.")
            return
        }
        habits[name] = mutableListOf()
        save()
        println("Привычка '$name' добавлена.")
    }

    private fun removeHabit(name: String) {
        if (!habits.containsKey(name)) {
            println("Привычка '$name' не найдена.")
            return
        }
        habits.remove(name)
        save()
        println("Привычка '$name' удалена.")
    }

    private fun checkHabit(name: String) {
        if (!habits.containsKey(name)) {
            println("Привычка '$name' не найдена.")
            return
        }
        val today = LocalDate.now().toString()
        val dates = habits[name]!!
        if (dates.contains(today)) {
            println("Привычка '$name' уже отмечена сегодня.")
            return
        }
        dates.add(today)
        save()
        println("Привычка '$name' отмечена на сегодня.")
    }

    private fun listHabits() {
        if (habits.isEmpty()) {
            println("Нет привычек.")
            return
        }
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        for ((name, dates) in habits) {
            val week = (6 downTo 0).map { today.minusDays(it.toLong()).format(formatter) }
            val count = week.count { dates.contains(it) }
            val bar = week.joinToString("") { if (dates.contains(it)) "[x]" else "[ ]" }
            println("Привычка: $name")
            println("  $bar  ($count/7, ${count*100/7}%)")
            println("  Всего дней: ${dates.size}")
        }
    }

    private fun resetData() {
        habits.clear()
        save()
        println("Все данные сброшены.")
    }

    fun run() {
        load()
        when {
            add != null -> addHabit(add!!)
            remove != null -> removeHabit(remove!!)
            check != null -> checkHabit(check!!)
            list -> listHabits()
            reset -> resetData()
            else -> println("Используйте --help для справки")
        }
    }
}

fun main(args: Array<String>) {
    val tracker = HabitTracker()
    JCommander.newBuilder().addObject(tracker).build().parse(*args)
    tracker.run()
}
