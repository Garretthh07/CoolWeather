This is an Open Source Weather App.

### db:
- I create three db table to store data: 

    - province:

    ```
        create table Province {
            id integer primary key autoincrement,
            province_name text,
            province_code text
        }
    ```

    - city:

    ```
        create table City {
            id integer primary key autoincrement,
            city_name text,
            city_code text,
            province_id integer
        }
    ```

    - county:

    ```
        create table County {
            id integer primary key autoincrement,
            county_name text,
            county_code text,
            city_id integer
        }
    ```

### model:

- Add Entity Class For `City`, `County` and `Province`.
- `CoolWeatherDB`: DB Class To Save Data.


### util:


- `HttpCallbackListener`: Interface To Handler different status from serve
- `HttpUtil`: Get Data From [http://www.weather.com.cn/data/list3/city21.xml](http://www.weather.com.cn/data/list3/city21.xml “中国天气网”)
- `Utility`: Handler The Response Data Format.


