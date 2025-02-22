# WeatherSDK

WeatherSDK - Java библиотека для работы с OpenWeatherAPI.


___
## Почему именно WeatherSDK?
✅ Позволяет получать данные и требует лишь название города       
✅ Кеширование 10 последних запросов с помощью GuavaCache (TTL 10 минут)  
✅ Два режима работы
- `ON_DEMAND` — данные обновляются только после запроса
- `POLLING` — данные автоматически обновляются каждые 10 минут

✅ Можно создать несколько инстансов SDK с разными API ключами.

___

## Начало

**Шаг 1.** Добавьте JitPack репозиторий в свой build файл.

Добавлять в `pom.xml`
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
**Шаг 2.** Добавьте зависимость
```xml
<dependency>  
    <groupId>com.github.Mur0riki</groupId>
    <artifactId>WeatherSDKTestTask</artifactId>
    <version>Tag</version>
</dependency>
```
___
## Примеры использования
+ Создание инстсанса SDK:
  
    + Если вы хотите "ON_DEMAND" режим
    ```java
    WeatherSDK sdk = WeatherSDK.addInstance("YOUR_API_KEY", Mode.ON_DEMAND);
    ```
    + "POLLING" режим
    ```java
    WeatherSDK sdk = WeatherSDK.addInstance("YOUR_API_KEY", Mode.POLLING);
    ```
+ Получение данных о погоде:
    + как java обьект:
    ```java
    WeatherData data = sdk.getWeather("City");
    ```
    + как json обьект:
    ```java
    String json = sdk.getWeatherAsJson("City");
    ```
+ Удаление инстанса:
  ```java
  WeatherSDK.removeInstance("Your_API_Key");
  ```
## Формат JSON-ответа
SDK возвращает объект `WeatherData`, который форматируется следующим образом:
```json
{
  "weather": {
    "main": "Clear",
    "description": "clear sky"
  },
  "temperature": {
    "temp": 20.5,
    "feels_like": 19.0
  },
  "visibility": 10000,
  "wind": {
    "speed": 5.1
  },
  "dt": 1618317045,
  "sys": {
    "sunrise": 1618301045,
    "sunset": 1618354200
  },
  "timezone": 10800,
  "name": "London"
}
```
## Обработка ошибок
SDK выбрасывает исключения, если что-то пошло не так:
| Ошибка                    | Исключение                      | Причина |
|---------------------------|--------------------------------|---------|
| Неверный API-ключ        | `WeatherClientException`       | Введен неверный API-ключ |
| Город не найден          | `WeatherClientException`        | Указанный город отсутствует в базе OpenWeather |
| Ошибка сети              | `WeatherClientException`                  | Проблема с подключением к API |
