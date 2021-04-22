### Starting the container

Build the application:

```shell
docker build -t mjavault/qwirkle:latest .
docker push mjavault/qwirkle:latest
```

Run the application:

```shell
docker run -p 8080:8080 mjavault/qwirkle 
```

## TODO

- Keep username between sessions
- Keep player selection between reload?
- AI
- Rotate on tablet
