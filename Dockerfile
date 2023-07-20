FROM gradle:latest

WORKDIR /app
COPY . .

EXPOSE 7001/udp

RUN gradle clean :relay-server:build --no-daemon
CMD ["gradle", ":relay-server:run", "--console=plain"]
