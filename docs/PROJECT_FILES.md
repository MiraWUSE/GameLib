# Файлы GameLib: назначение и связи

Это объяснение текущего кода проекта. Здесь описаны файлы приложения, ресурсы, настройки сборки и тесты.

## Как читать пути и связи

Основной Kotlin-код лежит в `app/src/main/java/com/example/gamelib/`. В разделах про приложение пути указаны **от этой папки**. Например, `domain/model/Game.kt` означает `app/src/main/java/com/example/gamelib/domain/model/Game.kt`.

В остальных разделах указан свой начальный путь. Папки `build/`, `.gradle/` и служебные файлы `.git/` не являются исходным кодом: их создают инструменты сборки и Git.

Названия трёх основных папок означают:

| Папка | За что отвечает | Пример |
| --- | --- | --- |
| `presentation` | Что видит пользователь и как интерфейс реагирует на действия. | Карточка игры, форма редактирования, состояние загрузки. |
| `domain` | Как выглядит игра внутри приложения и какие действия с ней доступны. | Добавить игру, получить библиотеку. |
| `data` | Откуда приходят данные и как они сохраняются. | Запрос FreeToGame, чтение и запись Room. |

**Room** — библиотека для работы с локальной SQLite-базой на устройстве. **Retrofit** отправляет запросы к API, а **Gson** превращает полученный JSON-текст в Kotlin-объекты. **Compose** рисует интерфейс. **Coil** загружает картинки по ссылкам. **Hilt** создаёт нужные объекты и передаёт их друг другу.

## Запуск приложения

### `GameLibApplication.kt`

**Зачем нужен:** настраивает Hilt на уровне всего приложения.

**Что делает:** наследуется от Android-класса `Application` и содержит аннотацию `@HiltAndroidApp`. Собственной логики загрузки игр здесь нет.

**Связи:** указан в `app/src/main/AndroidManifest.xml` как класс приложения. Благодаря ему Hilt может предоставлять зависимости для `MainActivity` и `GameViewModel`, используя файлы из `data/di`.

### `MainActivity.kt`

**Зачем нужен:** это Android-экран, внутри которого размещаются все Compose-экраны GameLib.

**Что делает:**

- Получает `GameViewModel` через Hilt.
- Подключает оформление `GameLibTheme`.
- Выбирает, что показать: библиотеку, каталог или форму создания/редактирования.
- Хранит выбранную для редактирования игру и флаги переключения экранов.
- Получает заполненную игру из формы и вызывает добавление либо обновление.
- Слушает событие `DataSaved` и показывает сообщение «Данные сохранены».

**Связи:** `GameLibTheme`, `GameViewModel`, `GameListScreen`, `GameCatalogScreen`, `GameEditScreen`, `GameUiEvent`, `Game`.

Важное различие: флаги открытого экрана хранятся в памяти через `remember`. Сами сохранённые игры находятся в Room, а не в этих флагах.

## Интерфейс — `presentation`

### `screen/GameListScreen.kt`

**Зачем нужен:** показывает раздел «Мои игры» — локальную библиотеку.

**Что делает:** читает `uiState.games` из `GameViewModel` и рисует список. Внутри этого же файла находится функция `GameItem`, которая рисует отдельную карточку. Есть кнопки перехода в каталог, добавления, редактирования и удаления.

**Связи:**

- `MainActivity` открывает этот экран и передаёт функции для переходов. Такая переданная функция называется обратным вызовом: экран сообщает о нажатии, а `MainActivity` решает, какой экран открыть.
- `GameViewModel` предоставляет игры и принимает команду удаления.
- `GameStatusExtensions.kt` переводит статус для показа пользователю.
- `GameCover` рисует обложку только при `game.remoteId != null`, то есть для игры из API.

Созданная вручную игра имеет `remoteId = null`, поэтому у неё нет ни блока картинки, ни заглушки «Нет обложки».

### `screen/GameCatalogScreen.kt`

**Зачем нужен:** показывает игры, полученные из FreeToGame.

**Что делает:** при открытии вызывает `loadCatalogGames()`, если каталог в состоянии ещё пуст. Показывает загрузку, ошибку с кнопкой «Повторить» либо список. Функция `CatalogGameItem` в этом же файле рисует карточку с обложкой и кнопкой «Добавить в библиотеку».

**Связи:** `MainActivity` открывает экран и обрабатывает кнопку «Мои игры»; `GameViewModel` загружает каталог и сохраняет выбранную игру через `addGame(game)`; `GameCover` показывает изображение; `GameStatusExtensions.kt` даёт русское название статуса.

Сам экран не отправляет HTTP-запросы и не выполняет SQL. Он вызывает методы `GameViewModel`.

### `screen/GameEditScreen.kt`

**Зачем нужен:** одна форма и для создания новой игры, и для изменения существующей.

**Что делает:** хранит введённые название, описание, жанр, платформу, разработчика и выбранный статус. Если пришёл `game`, заполняет поля его значениями. При сохранении проверяет обязательные поля, убирает пробелы по краям текста и создаёт объект `Game`.

**Связи:** получает `Game` от `MainActivity` и возвращает результат через `onSaveClick`. `MainActivity` затем выбирает `viewModel.addGame()` или `viewModel.updateGame()`. Для названий статусов используется `GameStatusExtensions.kt`.

При редактировании сохраняет прежние `id`, `remoteId` и `thumbnail`. Поэтому изменение названия или статуса не теряет связь с API и обложкой. Загрузки собственных фотографий в этой форме сейчас нет.

### `component/GameCover.kt`

**Зачем нужен:** общий блок обложки, чтобы не повторять код загрузки картинки в разных карточках.

**Что делает:** принимает ссылку `thumbnail` и название игры `title`. Передаёт ссылку в Coil `AsyncImage`, рисует картинку в прямоугольнике 16:9 со скруглёнными углами. Пока картинка загружается, показывает текст загрузки; при ошибке — сообщение; при пустой ссылке — «Нет обложки».

**Связи:** вызывается из `GameCatalogScreen.kt` и `GameListScreen.kt`; Coil подключён в `app/build.gradle.kts`; цвета и форма берутся из `MaterialTheme`.

Этот файл не решает, относится ли игра к API. Решение, вызывать ли `GameCover` для карточки библиотеки, находится в `GameListScreen.kt`. Room хранит ссылку на изображение, а сам файл картинки загружает и кэширует Coil.

### `viewmodel/GameViewModel.kt`

**Зачем нужен:** связывает действия на экране с получением и сохранением данных.

**Что делает:**

| Метод или свойство | Назначение |
| --- | --- |
| `uiState` | Текущее состояние для экранов: библиотека, каталог, загрузка и ошибка. |
| `events` | Одноразовые события для `MainActivity`, например успешное сохранение. |
| `observeGames()` | Начинает наблюдать за библиотекой при создании ViewModel и обновляет `uiState.games`. |
| `loadCatalogGames()` | Включает загрузку, получает каталог, записывает результат или ошибку в состояние. |
| `addGame(game)` | Запускает сохранение новой игры, после выполнения отправляет `DataSaved`. |
| `updateGame(game)` | Сохраняет изменения и отправляет `DataSaved`. |
| `deleteGame(game)` | Удаляет игру через соответствующее действие. |

**Связи:** получает пять классов из `domain/usecase` через Hilt. Создаёт `GameUiState` и `GameUiEvent`. Экраны читают его состояние и вызывают методы; `MainActivity` слушает события.

`StateFlow` здесь — наблюдаемое текущее состояние: экран получает обновления. `Flow` списка игр — поток значений из базы: после изменения таблицы приходит новый список. Корутины `viewModelScope.launch` позволяют выполнять асинхронные действия, не заставляя интерфейс ждать их обычным последовательным вызовом.

ViewModel живёт в памяти процесса. После нового запуска список восстанавливается через `observeGames()` из Room.

### `state/GameUiState.kt`

**Зачем нужен:** собирает данные для интерфейса в один объект.

**Что делает:** содержит `games` для библиотеки, `catalogGames` для каталога, `isCatalogLoading` для индикатора и `catalogErrorMessage` для текста ошибки.

**Связи:** `GameViewModel` обновляет объект; `GameListScreen` и `GameCatalogScreen` его читают. Списки содержат объекты `Game`. Сам этот файл ничего не загружает и не сохраняет.

### `state/GameUiEvent.kt`

**Зачем нужен:** описывает одноразовые уведомления, которые не являются постоянным содержимым экрана.

**Что делает:** сейчас объявляет только событие `DataSaved`.

**Связи:** `GameViewModel` отправляет его после добавления или обновления; `MainActivity` получает и показывает всплывающее сообщение. В отличие от `GameUiState`, здесь не хранится список игр.

### `util/GameStatusExtensions.kt`

**Зачем нужен:** позволяет показывать статусы на русском, сохраняя понятные программные обозначения внутри кода.

**Что делает:** функция `GameStatus.toDisplayName()` преобразует `WANT_TO_PLAY` в «Хочу поиграть», `PLAYING` в «Играю», `COMPLETED` в «Пройдено», `DROPPED` в «Заброшено».

**Связи:** использует `GameStatus` из `Game.kt`; вызывается в трёх файлах экранов. Содержимое базы эта функция не меняет.

### Файлы `theme`

| Файл | Зачем нужен и что делает | Связи |
| --- | --- | --- |
| `theme/Color.kt` | Объявляет наборы цветов, используемые для светлой и тёмной темы. | Цвета читает `Theme.kt`. |
| `theme/Type.kt` | Объявляет `Typography`: настройки текста, в том числе размер и высоту строки для `bodyLarge`. | `Theme.kt` передаёт эти настройки в `MaterialTheme`. |
| `theme/Theme.kt` | Функция `GameLibTheme` выбирает светлую/тёмную тему. На Android 12+ по умолчанию использует динамические системные цвета. | Использует `Color.kt` и `Type.kt`; вызывается в `MainActivity` и UI-тестах. Экраны получают оформление через `MaterialTheme`. |

## Модель игры и действия — `domain`

### `model/Game.kt`

**Зачем нужен:** единое представление игры для экранов и действий приложения.

**Что делает:** объявляет `Game` и перечисление `GameStatus`. Поля игры:

| Поле | Значение |
| --- | --- |
| `id` | Номер записи в локальной базе. Для новой игры перед сохранением — `0`; Room назначает номер при вставке. |
| `remoteId` | Номер игры в FreeToGame. У вручную созданной игры — `null`, то есть значения нет. |
| `title`, `description` | Название и описание. |
| `genre`, `platform`, `developer` | Жанр, платформа и разработчик. |
| `status` | Один из вариантов `GameStatus`. По умолчанию — «Хочу поиграть». |
| `thumbnail` | Ссылка на обложку либо `null`. Это текст ссылки, не байты изображения. |

**Связи:** используется в экранах, ViewModel, use case и `GameRepository`. `GameMapper.kt` переводит в него данные API и базы и обратно в запись базы.

`id` и `remoteId` — разные номера. Например, игра может иметь `id = 7` в твоей библиотеке и `remoteId = 452` на сервере.

### `repository/GameRepository.kt`

**Зачем нужен:** описывает, какие операции с играми нужны приложению, без подробностей про HTTP и SQL.

**Что делает:** объявляет интерфейс с методами `getAllGames`, `getCatalogGames`, `addGame`, `updateGame`, `deleteGame`. Интерфейс — это договор: какие методы должен предоставить работающий с данными класс.

**Связи:** классы `usecase` вызывают этот интерфейс; `GameRepositoryImpl` реализует его; `RepositoryModule` сообщает Hilt, какую реализацию передавать.

### Файлы `usecase`

Use case здесь — отдельный класс для одного действия. Сейчас эти классы короткие: передают вызов в репозиторий. Они позволяют ViewModel обращаться к действиям, не зная устройства базы или API.

| Файл | Зачем нужен и что делает | Связи |
| --- | --- | --- |
| `usecase/GetGamesUseCase.kt` | Получает поток локальной библиотеки. Вызов `invoke()` возвращает `repository.getAllGames()`. | `GameViewModel.observeGames()` → этот класс → `GameRepository`. |
| `usecase/GetCatalogGamesUseCase.kt` | Получает список каталога через `repository.getCatalogGames()`. | `GameViewModel.loadCatalogGames()` → этот класс → `GameRepository`. |
| `usecase/AddGameUseCase.kt` | Передаёт новую игру в `repository.addGame(game)`. | `GameViewModel.addGame()` → этот класс → `GameRepository`; передаётся `Game`. |
| `usecase/UpdateGameUseCase.kt` | Передаёт изменённую игру в `repository.updateGame(game)`. | `GameViewModel.updateGame()` → этот класс → `GameRepository`. |
| `usecase/DeleteGameUseCase.kt` | Передаёт удаляемую игру в `repository.deleteGame(game)`. | `GameViewModel.deleteGame()` → этот класс → `GameRepository`. |

`operator fun invoke` позволяет писать `addGameUseCase(game)` вместо `addGameUseCase.invoke(game)`. Это один и тот же вызов.

## Источники данных — `data`

### `repository/GameRepositoryImpl.kt`

**Зачем нужен:** выполняет операции из `GameRepository`, используя реальные источники данных.

**Что делает:** локальные игры читает через `GameDao`, преобразует записи в `Game`; каталог получает через `FreeToGameApi`, берёт первые 50 игр и преобразует их в `Game`. Для добавления, обновления и удаления переводит `Game` в `GameEntity` и вызывает DAO.

**Связи:** реализует `GameRepository`; получает `GameDao` и `FreeToGameApi` через Hilt; использует функции из `GameMapper.kt`. Вызывается через use case.

Каталог автоматически целиком в Room не сохраняется. Запись появляется в библиотеке после действия «Добавить в библиотеку».

### `mapper/GameMapper.kt`

**Зачем нужен:** переводит данные между тремя представлениями игры, чтобы экран не зависел от формата API или таблицы.

**Что делает:**

| Функция | Преобразование |
| --- | --- |
| `GameDto.toDomain()` | Ответ API → `Game`. Серверный `id` становится `remoteId`, локальный `id` равен `0`, начальный статус — `WANT_TO_PLAY`. |
| `Game.toEntity()` | `Game` → запись Room. Статус превращается в строку через `status.name`. |
| `GameEntity.toDomain()` | Запись Room → `Game`. Строка статуса превращается обратно в `GameStatus`. |

Все преобразования передают `thumbnail`. Преобразования между `Game` и `GameEntity` сохраняют также оба идентификатора.

**Связи:** `GameDto`, `Game`, `GameEntity`, `GameStatus`; функции вызывает `GameRepositoryImpl`, а корректность проверяют тесты.

### `remote/api/FreeToGameApi.kt`

**Зачем нужен:** описывает HTTP-запросы к FreeToGame для Retrofit.

**Что делает:** метод `getGames()` с `@GET("games")` запрашивает список и возвращает `List<GameDto>`. Полный адрес складывается из базового адреса и `games`.

**Связи:** `NetworkModule` задаёт базовый адрес и создаёт реализацию интерфейса через Retrofit; `GameRepositoryImpl` вызывает `getGames()`; результат представлен `GameDto`.

### `remote/dto/GameDto.kt`

**Зачем нужен:** описывает те поля ответа API, которые приложение использует. DTO означает объект для передачи данных.

**Что делает:** хранит серверный номер, название, короткое описание, жанр, платформу, разработчика и ссылку на обложку. `@SerializedName("short_description")` связывает поле JSON `short_description` с Kotlin-полем `shortDescription`.

**Связи:** Gson заполняет объект при ответе `FreeToGameApi`; `GameMapper.kt` превращает его в `Game`. Сам DTO не рисует карточку и не создаёт запись в Room.

### `local/entity/GameEntity.kt`

**Зачем нужен:** описывает одну строку таблицы `games` в локальной базе.

**Что делает:** `@Entity(tableName = "games")` задаёт таблицу, `@PrimaryKey(autoGenerate = true)` — автоматически назначаемый локальный номер. Статус хранится как строка, обложка — как nullable-ссылка `thumbnail`.

**Связи:** зарегистрирован в `GameDatabase`; `GameDao` читает и записывает такие объекты; `GameMapper.kt` преобразует их в `Game` и обратно.

### `local/dao/GameDao.kt`

**Зачем нужен:** задаёт операции над таблицей. DAO — объект доступа к данным.

**Что делает:**

- `getAllGames()` выполняет `SELECT * FROM games` и возвращает наблюдаемый `Flow<List<GameEntity>>`.
- `insertGame()` вставляет запись.
- `updateGame()` обновляет существующую запись по первичному ключу `id`.
- `deleteGame()` удаляет запись по первичному ключу.

**Связи:** `GameDatabase` предоставляет DAO; `DatabaseModule` передаёт его через Hilt в `GameRepositoryImpl`; операции используют `GameEntity`.

Это интерфейс. Реализацию SQL-операций Room генерирует при сборке с помощью KSP. Сгенерированный `GameDao_Impl` не нужно писать вручную.

### `local/database/GameDatabase.kt`

**Зачем нужен:** определяет состав, версию и способ открытия локальной базы.

**Что делает:** объявляет таблицу `GameEntity`, текущую версию схемы `3`, доступ к `GameDao` и функцию `open()`. По умолчанию открывает файл базы `game_database` в данных приложения на устройстве.

Здесь же находятся миграции — изменения структуры существующей базы без удаления её записей:

- `MIGRATION_1_2` добавляет `remoteId`, если такого столбца ещё нет. Проверка нужна, потому что ранние сборки версии 1 встречались уже с этим столбцом.
- `MIGRATION_2_3` добавляет `thumbnail`.

Автоматическое удаление базы при отсутствии подходящей миграции не включено.

**Связи:** использует `GameEntity` и `GameDao`; `DatabaseModule` вызывает `open()` для приложения. Тесты вызывают тот же метод, чтобы проверять те же настройки.

### Файлы `di`

DI означает передачу зависимостей: например, репозиторию нужен DAO, и кто-то должен его создать и передать. В проекте этим управляет Hilt.

| Файл | Зачем нужен и что делает | Связи |
| --- | --- | --- |
| `di/DatabaseModule.kt` | Объясняет Hilt, как получить базу через `GameDatabase.open(context)` и DAO через `database.gameDao()`. База объявлена `@Singleton`: один экземпляр в рамках процесса приложения. | `GameDatabase` → `GameDao` → `GameRepositoryImpl`. |
| `di/NetworkModule.kt` | Создаёт Retrofit с адресом `https://www.freetogame.com/api/` и Gson-конвертером. Затем создаёт `FreeToGameApi`. | Retrofit/Gson → `FreeToGameApi` → `GameRepositoryImpl`. |
| `di/RepositoryModule.kt` | Связывает интерфейс `GameRepository` с классом `GameRepositoryImpl` через `@Binds`. | Когда use case просит `GameRepository`, Hilt передаёт `GameRepositoryImpl`. |

`@Inject constructor` в репозитории и use case позволяет Hilt собрать их из уже известных зависимостей. `@HiltViewModel` позволяет получить собранный `GameViewModel` в `MainActivity`.

## Как файлы работают вместе

### Открытие локальной библиотеки

1. Android запускает `MainActivity`, которая получает `GameViewModel`.
2. ViewModel начинает `observeGames()` → `GetGamesUseCase` → `GameRepositoryImpl.getAllGames()`.
3. Репозиторий подписывается на `GameDao.getAllGames()` и преобразует `GameEntity` в `Game` через `GameMapper`.
4. ViewModel записывает полученный список в `GameUiState.games`.
5. `GameListScreen` получает состояние и рисует карточки. После изменения базы эта же подписка приносит обновлённый список.

### Загрузка каталога

```text
GameCatalogScreen → GameViewModel.loadCatalogGames()
→ GetCatalogGamesUseCase → GameRepositoryImpl
→ FreeToGameApi → сервер FreeToGame

Ответ сервера → GameDto → GameMapper → Game
→ GameUiState.catalogGames → GameCatalogScreen
```

Retrofit и API для этой цепочки предоставляет `NetworkModule`.

### Создание игры вручную и добавление из API

Начало различается:

- Вручную: `GameEditScreen` собирает `Game` из полей → вызывает `onSaveClick` → `MainActivity` вызывает `GameViewModel.addGame()`.
- Из каталога: `GameCatalogScreen` уже имеет `Game` из API → кнопка вызывает `GameViewModel.addGame(game)`.

Далее путь общий:

```text
GameViewModel.addGame → AddGameUseCase → GameRepositoryImpl.addGame
→ GameMapper.toEntity → GameDao.insertGame → таблица games
```

Изменение таблицы возвращается на экран через поток библиотеки. Отдельно ViewModel отправляет `GameUiEvent.DataSaved` → `MainActivity` показывает сообщение.

### Редактирование и удаление

Редактирование: `GameListScreen` сообщает о выбранной игре → `MainActivity` открывает `GameEditScreen` → форма возвращает изменённый `Game` → `MainActivity` вызывает `GameViewModel.updateGame()` → `UpdateGameUseCase` → репозиторий → mapper → `GameDao.updateGame()`.

Удаление: `GameListScreen` → `GameViewModel.deleteGame()` → `DeleteGameUseCase` → репозиторий → mapper → `GameDao.deleteGame()`.

После обоих действий новый список приходит из Room через существующее наблюдение, как при открытии библиотеки.

### Показ обложки

```text
GameDto.thumbnail → GameMapper → Game.thumbnail
→ GameCatalogScreen → GameCover → Coil → изображение по ссылке
```

При добавлении в библиотеку ссылка проходит также через `GameEntity.thumbnail` и Room. При последующем чтении возвращается в `Game.thumbnail`. В `GameListScreen` обложка включается по наличию `remoteId`. У вручную созданных игр блока обложки нет.

## Android-ресурсы и манифест

Пути в таблице указаны от `app/src/main/`.

| Файл | Зачем нужен и что делает | Связи |
| --- | --- | --- |
| `AndroidManifest.xml` | Сообщает Android имя Application-класса, стартовую Activity, значок, тему и разрешение на интернет. Также подключает правила резервного копирования. | `GameLibApplication`, `MainActivity`, файлы из `res`. Разрешение `INTERNET` нужно запросам API и картинок. |
| `res/values/strings.xml` | Хранит строковый ресурс `app_name` со значением GameLib. | Имя используется манифестом. Большинство текстов экранов сейчас записано прямо в Kotlin-файлах. |
| `res/values/themes.xml` | Задаёт Android-тему `Theme.GameLib` без стандартной панели ActionBar. | Её подключает манифест. Оформление Compose-компонентов отдельно задаёт `presentation/theme/Theme.kt`. |
| `res/values/colors.xml` | Содержит XML-ресурсы цветов из исходного шаблона. | Это отдельные ресурсы Android; основные цвета Compose сейчас определены в `Color.kt` и `Theme.kt`. |
| `res/drawable/ic_launcher_background.xml` | Фон значка приложения. | Используется адаптивными значками `ic_launcher.xml` и `ic_launcher_round.xml`. |
| `res/drawable/ic_launcher_foreground.xml` | Передний слой значка приложения. | Используется теми же адаптивными значками. |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | Собирает обычный адаптивный значок из фона и переднего слоя. | Манифест ссылается на `@mipmap/ic_launcher`. |
| `res/mipmap-anydpi-v26/ic_launcher_round.xml` | Собирает круглый вариант адаптивного значка. | Манифест ссылается на `@mipmap/ic_launcher_round`. |
| `res/mipmap-mdpi/`, `mipmap-hdpi/`, `mipmap-xhdpi/`, `mipmap-xxhdpi/`, `mipmap-xxxhdpi/`: `ic_launcher.webp` и `ic_launcher_round.webp` в каждой папке | Готовые изображения значков для разных плотностей экрана. Android выбирает подходящий ресурс. | Используются через ссылки на значки в манифесте. Это значки приложения, а не обложки игр. |
| `res/xml/backup_rules.xml` | Файл правил Android Auto Backup для соответствующих версий Android; сейчас содержит шаблон без активных индивидуальных include/exclude. | Подключён через `android:fullBackupContent` в манифесте. |
| `res/xml/data_extraction_rules.xml` | Файл правил резервного копирования/переноса для новых версий Android; сейчас содержит шаблон правил. | Подключён через `android:dataExtractionRules`. Не является реализованной внутри GameLib облачной синхронизацией. |
| `keepRules/rules.keep` | Место для правил R8, сохраняющих необходимые элементы кода при оптимизации. Сейчас содержит только комментарии. | Читается Android-плагином сборки; в `app/build.gradle.kts` оптимизация release сейчас отключена. |

## Файлы сборки и служебные файлы

Здесь пути указаны от корня проекта.

| Файл | Зачем нужен и что делает | Связи |
| --- | --- | --- |
| `settings.gradle.kts` | Задаёт имя проекта, подключает модуль `app`, репозитории для скачивания плагинов/библиотек и механизм поиска Java toolchain. | Gradle читает его для определения состава проекта перед сборкой `app`. |
| `build.gradle.kts` | Объявляет общие плагины Android, Compose, KSP и Hilt, не применяя их к корню. | Версии берёт из `gradle/libs.versions.toml`; плагины применяются в модуле `app`. |
| `app/build.gradle.kts` | Настраивает сборку приложения: package ID, SDK, Java, Compose, библиотеки и варианты сборки. | Подключает Room, Retrofit, Coil, Hilt, Compose и тестовые библиотеки через каталог `libs.versions.toml`. |
| `gradle/libs.versions.toml` | Содержит версии и короткие имена библиотек/плагинов. Объявление здесь само по себе ещё не подключает библиотеку. | `build.gradle.kts` и `app/build.gradle.kts` обращаются к именам вида `libs.coil.compose`. |
| `gradle.properties` | Общие настройки Gradle: память процесса, UTF-8, кэш конфигурации, стиль Kotlin и проектная Android-опция. | Влияет на работу сборки, не на содержимое библиотеки игр. |
| `gradle/gradle-daemon-jvm.properties` | Указывает Java toolchain для процесса Gradle: сейчас версия 25 и адреса подходящих сборок Java для разных платформ. | Используется при запуске Gradle; связан с настройкой toolchain в `settings.gradle.kts`. |
| `gradlew.bat` | Запускает Gradle Wrapper на Windows. | Использует `gradle-wrapper.jar` и `gradle-wrapper.properties`. |
| `gradlew` | Аналогичный запуск Wrapper для Unix-подобных систем. | Использует те же файлы Wrapper. |
| `gradle/wrapper/gradle-wrapper.jar` | Служебная программа, запускающая нужную версию Gradle и при необходимости скачивающая её. | Вызывается скриптами `gradlew`/`gradlew.bat`. |
| `gradle/wrapper/gradle-wrapper.properties` | Задаёт версию Gradle (сейчас 9.5.0), адрес дистрибутива, контрольную сумму и параметры загрузки. | Читается Wrapper при запуске. |
| `local.properties` | Локальная настройка пути к Android SDK на конкретном компьютере. | Читается сборкой, исключён из Git через `.gitignore`. |
| `.gitignore` | Перечисляет файлы и папки, которые Git не должен добавлять как новые исходники, например `local.properties` и кэш Gradle. | Используется Git, на работу Android-приложения не влияет. |
| `TESTING.md` | Объясняет запуск проверок и отдельную проверку сохранения после остановки процесса и установки APK поверх старого. | Связан с тестовыми задачами из `app/build.gradle.kts` и `ProcessPersistenceTest.kt`. |
| `docs/PROJECT_FILES.md` | Этот файл: объясняет назначение исходников и их связи. | Описывает проект; в APK как код приложения не выполняется. |

В `app/build.gradle.kts` обычный вариант `debug` использует `com.example.gamelib`. Вариант `verification` предназначен для тестов и использует `com.example.gamelib.verification`. Это отдельные приложения с отдельными данными. `testBuildType = "verification"` направляет проверки на устройстве в тестовую копию.

KSP при сборке создаёт код для Room и Hilt. Поэтому часть классов, которые фактически выполняют запросы и создают зависимости, находится в `app/build/generated/`, а не среди написанных вручную исходников.

## Тестовые файлы

Тесты из `app/src/test/java/com/example/gamelib/` запускаются на компьютере. Тесты из `app/src/androidTest/java/com/example/gamelib/` требуют Android-устройства или эмулятора.

| Файл относительно указанной папки | Зачем нужен и что проверяет | Связи |
| --- | --- | --- |
| `test`: `ExampleUnitTest.kt` | Исходный пример JUnit: проверяет `2 + 2 = 4`. Логику GameLib не проверяет. | JUnit из настроек сборки. |
| `test`: `GameThumbnailTest.kt` | Проверяет чтение ссылки из JSON и её сохранение при преобразовании моделей; отдельно проверяет отсутствие картинки. | Gson, `GameDto`, `GameMapper`, `Game`, `GameEntity`. |
| `androidTest`: `ExampleInstrumentedTest.kt` | Проверяет, что тест работает с пакетом `com.example.gamelib.verification`. | `testBuildType` и `applicationIdSuffix` из `app/build.gradle.kts`. |
| `androidTest`: `CatalogLibraryTest.kt` | Нажимает кнопки добавления и редактирования, проверяет сохранение игры, статуса и ссылки после закрытия/открытия базы. Ответ API заменён тестовым, остальная цепочка реальная. | Экраны, `GameViewModel`, use case, репозиторий, mapper и Room. |
| `androidTest`: `GameCoverTest.kt` | Проверяет отображение настоящего локального тестового PNG, заглушку при отсутствии ссылки и сообщение при ошибке. | `GameCover`, Coil, Compose. Не проверяет доступность сервера FreeToGame. |
| `androidTest`: `GameDatabaseMigrationTest.kt` | Создаёт старую базу и проверяет сохранение игры при обновлении. Проверяет версии 1 без `remoteId`, 1 с `remoteId` и 2. | `GameDatabase.open()`, обе миграции и `GameDao`. |
| `androidTest`: `ProcessPersistenceTest.kt` | В фазе `seed` сохраняет две тестовые игры; в отдельном запуске `verify` проверяет их наличие и поля. Без аргумента фазы пропускается. | Использует `GameDatabase.open()` в тестовом приложении; порядок отдельных запусков описан в `TESTING.md`. |

## Настройки Android Studio — `.idea`

Эти файлы описывают поведение редактора и его связь со сборкой. Они не сохраняют игры и не выполняются на телефоне.

| Файл внутри `.idea/` | Зачем нужен и что делает | Связи |
| --- | --- | --- |
| `.gitignore` | Исключает личные файлы IDE, например `workspace.xml` и `shelf/`. | Используется Git для содержимого `.idea`. |
| `AndroidProjectSystem.xml` | Указывает Android Studio использовать систему проекта Gradle. | Связан с Gradle-настройками проекта. |
| `gradle.xml` | Хранит связь IDE с корнем проекта и модулем `app`, а также настройку выбора запуска тестов. | `settings.gradle.kts`, модуль `app`. |
| `misc.xml` | Общие настройки проекта в IDE: Android-проект, выбранный Java SDK и уровень языка. | Android Studio и окружение Java. |
| `compiler.xml` | Настройки компилятора в IDE, включая целевой уровень байткода. | IDE; параметры Android-сборки дополнительно определены в Gradle-файлах. |
| `deploymentTargetSelector.xml` | Сохраняет настройки выбора устройства для конфигурации запуска `app`. | Окно запуска Android Studio. |
| `deviceManager.xml` | Хранит настройки отображения списка устройств, сейчас сортировку по имени. | Device Manager в Android Studio. |
| `runConfigurations.xml` | Настраивает создание конфигураций запуска тестов; содержит список исключённых автоматических JUnit-конфигураций. | Механизм запуска тестов IDE. |
| `vcs.xml` | Указывает, что корень проекта находится под управлением Git. | Панель Git в Android Studio и репозиторий `.git`. |
| `codeStyles/Project.xml` | Правила оформления Kotlin и XML: стиль Kotlin, отступы и порядок XML-атрибутов. | Форматирование кода в редакторе. |
| `codeStyles/codeStyleConfig.xml` | Включает использование проектных настроек оформления. | `codeStyles/Project.xml`. |
| `inspectionProfiles/Project_Default.xml` | Настраивает проверки редактора, в том числе корректность Compose Preview. | Подсветка ошибок и предупреждений Android Studio. |
