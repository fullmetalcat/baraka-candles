This service is done as an assignment for Baraka
It has two main functionalities:
    * read stream of websocket events, save it in internal storage
    * REST API to return candles based on the events received

How to build:
./gradlew build

How to launch locally:
java -jar ./build/libs/candles-CURRENT-SNAPSHOT-all.jar ./src/main/resources/candles-config.yml

How to use with docker:
docker build -t dockerfile .
docker run -d -p 9989:9989 -p 8080:8080 dockerfile



REST API has one method - GET /<stock_name>/candles?cu=<chrono_unit_name>&l=<size>;

stock_name - name of the symbol. can be any stock name that was processed by the app.

chrono_unit_name - name of candle chronoUnit(java class) - is configured in CandleSize. 

size - number of chrono unints in the candle

example: http://localhost:8080/DFE/candles?cu=SECONDS&l=5

API will return results only for pre-configured candle sizes(candles-config.yml)
