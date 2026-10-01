# Проверка сохранения библиотеки

Обычный запуск в Android Studio: модуль `app`, вариант `debug`.
Он сохраняет библиотеку в `com.example.gamelib/databases/game_database`.
Обновляйте приложение поверх установленного: удаление приложения или очистка
его данных удаляет и локальную библиотеку.

Тесты на устройстве используют отдельный вариант `verification`
(`com.example.gamelib.verification`), чтобы установка и очистка тестовой копии
не затрагивали обычное приложение.

```powershell
.\gradlew.bat :app:assembleDebug :app:testVerificationUnitTest :app:connectedVerificationAndroidTest
```

Дополнительная проверка между независимыми процессами (нужен `adb` в PATH):

```powershell
.\gradlew.bat :app:assembleVerification :app:assembleVerificationAndroidTest
adb install -r app/build/outputs/apk/verification/app-verification.apk
adb install -r app/build/outputs/apk/androidTest/verification/app-verification-androidTest.apk
adb shell am instrument -w -e class com.example.gamelib.ProcessPersistenceTest -e persistencePhase seed com.example.gamelib.verification.test/androidx.test.runner.AndroidJUnitRunner
adb shell am force-stop com.example.gamelib.verification
adb shell am start -W -n com.example.gamelib.verification/com.example.gamelib.MainActivity
adb shell am instrument -w -e class com.example.gamelib.ProcessPersistenceTest -e persistencePhase verify com.example.gamelib.verification.test/androidx.test.runner.AndroidJUnitRunner
adb install -r app/build/outputs/apk/verification/app-verification.apk
adb shell am instrument -w -e class com.example.gamelib.ProcessPersistenceTest -e persistencePhase verify com.example.gamelib.verification.test/androidx.test.runner.AndroidJUnitRunner
```

Обе проверки `verify` должны завершиться `OK (1 test)`. Они проверяют вручную
созданную игру и игру из каталога, включая статус, remoteId и ссылку на обложку.
Без аргумента `persistencePhase` этот тест пропускается: проверять перезапуск
процесса внутри одного запуска инструментации недостаточно.

Изменения схемы Room требуют миграции. Автоматическое удаление базы при
отсутствии миграции отключено. Тесты миграций проверяют версии 1 и 2, в том числе
ранний вариант версии 1, в котором remoteId уже присутствовал.
