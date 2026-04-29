Неофициальный Android-клиент для просмотра контента с сайта [kino.pub](https://kino.pub).  
Доступны два приложения — для смартфонов и для Android TV.

## Скриншоты

### Mobile

<div align="center">
  <img width="24%" alt="Mobile screenshot 1" src="https://github.com/user-attachments/assets/5153359f-b1e5-45a4-baf2-eaec0a7f1586" />
  <img width="24%" alt="Mobile screenshot 2" src="https://github.com/user-attachments/assets/3b282091-8f77-425e-aa49-9552a8757794" />
  <img width="24%" alt="Mobile screenshot 3" src="https://github.com/user-attachments/assets/9c04060d-9fdf-49be-a154-3067c6e0a621" />
  <img width="24%" alt="Mobile screenshot 4" src="https://github.com/user-attachments/assets/c0aa12ba-fd02-47c5-be06-4fc49737d5e7" />
</div>
<div align="center">
  <img width="49%" alt="Mobile landscape" src="https://github.com/user-attachments/assets/4ed4d14e-1586-4be3-b00d-292e51ee7237" />
</div>


### TV

<div align="center">
  <img width="49%" alt="TV screenshot 1" src="https://github.com/user-attachments/assets/b5b22190-ce99-4c99-abb0-efb3217eea7a" />
  <img width="49%" alt="TV screenshot 2" src="https://github.com/user-attachments/assets/871ac0fe-8299-40d1-9815-85d712499891" />
  <img width="49%" alt="TV screenshot 3" src="https://github.com/user-attachments/assets/9d9ff134-c0a6-436d-8af5-44e545401a15" />
  <img width="49%" alt="TV screenshot 4" src="https://github.com/user-attachments/assets/23e7750d-581d-4bee-8c1c-b586363391bd" />
  <img width="49%" alt="TV screenshot 5" src="https://github.com/user-attachments/assets/944ad874-781c-47c3-8071-55cd82442bfa" />
</div>

## Загрузка

Актуальные APK доступны на странице [Releases](https://github.com/PoSayDone/kinopub/releases):

- `kinopub-mobile-*.apk` — для смартфонов
- `kinopub-tv-*.apk` — для Android TV

## Требования

| Приложение | Минимальный Android |
|------------|---------------------|
| Mobile     | Android 7.0 (API 24) |
| TV         | Android 7.0 (API 24) |

## Сборка

```bash
# Debug
./gradlew :app:mobile:assembleDebug
./gradlew :app:tv:assembleDebug

# Release (требуется keystore)
./gradlew :app:mobile:assembleRelease
./gradlew :app:tv:assembleRelease
```

Для release-сборки укажите параметры подписи в `local.properties`:

```properties
RELEASE_KEYSTORE_FILE=path/to/keystore.jks
KEYSTORE_PASSWORD=...
RELEASE_SIGN_KEY_ALIAS=...
RELEASE_SIGN_KEY_PASSWORD=...
```

## Структура проекта

```
app/
  mobile/   — приложение для смартфонов
  tv/       — приложение для Android TV
  shared/   — общая навигация и UI-утилиты
core/
  model/    — модели данных
  network/  — Retrofit-сервисы, interceptor-ы
  data/     — репозитории, менеджеры сессий и обновлений
  common/   — общие ViewModel-и, утилиты
```

## Технологии

- **Kotlin** + **Jetpack Compose** / **Compose for TV**
- **Hilt** — dependency injection
- **Retrofit** + **Gson** — сетевой слой
- **ExoPlayer / Media3** — воспроизведение видео
- **Coil** — загрузка изображений
- **Navigation 3** — навигация

## Лицензия

Проект распространяется под лицензией MIT.
