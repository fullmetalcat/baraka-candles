FROM openjdk:11

COPY build/libs/candles-CURRENT-SNAPSHOT-all.jar candles.jar
COPY build/resources/main/candles-config.yml .
COPY run.sh .
RUN chmod +x run.sh
CMD ./run.sh