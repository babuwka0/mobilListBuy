# Описание приложения «Список покупок»

Ниже по файлам: что за поля класса, что делают методы и блоки кода. Ссылки на фрагменты в формате `@путь:строка-строка`.

---

## `app/src/main/java/com/example/zametki/MainActivity.java`

### Поля класса (`@app/src/main/java/com/example/zametki/MainActivity.java:20-35`)

| Имя | Тип | Назначение |
|-----|-----|------------|
| `dbH` | `DatabaseHelper` | Помощник SQLite: создание БД и таблицы |
| `db` | `SQLiteDatabase` | Открытая база для запросов и вставок |
| `c` | `Cursor` | Результат `SELECT` для списка на экране |
| `ad` | `SimpleCursorAdapter` | Связка `ListView` ↔ данные из курсора `c` |
| `lv` | `ListView` | Список покупок |
| `statTxt` | `TextView` | Строка «Записей: …, сумма: …» |
| `nameInp` | `EditText` | Ввод названия товара |
| `priceInp` | `EditText` | Ввод цены |
| `typeSp` | `Spinner` | Выбор типа (еда, услуги, …) |
| `sortFldSp` | `Spinner` | По какому полю сортировать |
| `sortOrdSp` | `Spinner` | Порядок: по возрастанию или убыванию |
| `sortCols` | `String[]` | Имена колонок БД в том же порядке, что пункты сортировки в спиннере |

```20:35:app/src/main/java/com/example/zametki/MainActivity.java
    private DatabaseHelper dbH;
    private SQLiteDatabase db;
    private Cursor c;
    private SimpleCursorAdapter ad;
    private ListView lv;
    private TextView statTxt;
    private EditText nameInp;
    private EditText priceInp;
    private Spinner typeSp;
    private Spinner sortFldSp;
    private Spinner sortOrdSp;
    private static final String[] sortCols = {
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_PRICE,
            DatabaseHelper.COLUMN_TYPE
    };
```

### `onCreate` (`@app/src/main/java/com/example/zametki/MainActivity.java:37-80`)

Подключает разметку `activity_main`, создаёт `dbH`, находит все виджеты по `id`, создаёт три `ArrayAdapter` из массивов в `strings.xml` и вешает их на спиннеры, задаёт слушатель `rL`: при смене сортировки вызывается `refreshList()`. Кнопка «Добавить» вызывает `addItem()`.

### `onResume` (`@app/src/main/java/com/example/zametki/MainActivity.java:82-87`)

Открывает БД на запись (`getWritableDatabase`) и сразу обновляет список и статистику через `refreshList()`.

### `onPause` (`@app/src/main/java/com/example/zametki/MainActivity.java:89-104`)

Закрывает курсор `c`, снимает адаптер с `lv`, закрывает `db`, обнуляет ссылки — чтобы при уходе с экрана не держать ресурсы.

### `addItem` (`@app/src/main/java/com/example/zametki/MainActivity.java:106-124`)

Если `db` ещё нет — выход. Читает текст из `nameInp`, `priceInp`, выбранный тип из `typeSp`, парсит цену в `pr`, собирает `ContentValues` `cv`, делает `insert` в таблицу, очищает поля ввода, снова вызывает `refreshList()`.

### `refreshList` (`@app/src/main/java/com/example/zametki/MainActivity.java:126-162`)

Проверяет `db`, закрывает старый курсор `c`. Берёт позиции спиннеров `sortFldSp` и `sortOrdSp`, строит `ORDER BY` (`ASC` / `DESC`), выполняет `rawQuery`, кладёт результат в `c`. Если адаптера ещё не было — создаёт `SimpleCursorAdapter` (`ad`) и вешает на `lv`, иначе `changeCursor(c)`. В конце вызывает `updateStats()`.

### `updateStats` (`@app/src/main/java/com/example/zametki/MainActivity.java:164-172`)

Отдельный короткий запрос: число строк и сумма цен (`COUNT`, `SUM`). Результат в локальном курсоре `cur`, читает `cnt` и `sm`, закрывает `cur`, пишет строку в `statTxt`.

---

## `app/src/main/java/com/example/zametki/DatabaseHelper.java`

Наследник `SQLiteOpenHelper`: при первом обращении создаёт файл БД и таблицу `items` с колонками `_id`, `name`, `price`, `type_name`. Константы `TABLE`, `COLUMN_*` используются в `MainActivity` в SQL и в адаптере. `onUpgrade` пересоздаёт таблицу (для смены версии схемы).

```7:31:app/src/main/java/com/example/zametki/DatabaseHelper.java
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "shop.db";
    private static final int SCHEMA = 1;
    public static final String TABLE = "items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_TYPE = "type_name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" + COLUMN_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME
                + " TEXT, " + COLUMN_PRICE + " REAL, " + COLUMN_TYPE + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
```

---

## Разметка и ресурсы

### `app/src/main/res/layout/activity_main.xml`

Вертикальный `LinearLayout`: поля ввода, спиннер типа, кнопка добавления, `TextView` статистики, два спиннера сортировки в одной строке, внизу `ListView` с весом 1 на оставшуюся высоту.

### `app/src/main/res/layout/list_item_row.xml`

Одна строка списка: три `TextView` (`row_name`, `row_price`, `row_type`) под значения из курсора.

### `app/src/main/res/values/strings.xml`

`app_name`, массивы `purchase_types`, `sort_fields`, `sort_orders` для спиннеров.

---

## `gradle.properties` / настройки JDK

В корне проекта может быть `org.gradle.java.home` — какую JVM использует Gradle (если задано).
