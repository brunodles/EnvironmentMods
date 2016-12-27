### How to release on Bintray

In order to release a new version of this project

- Update the version on ```version``` file, in the repository folder
- Add you Bintray username and API Key to your ```~/.gradle/gradle.properties``` file as follows:
```
bintray.user=myUser
bintray.apikey=myApiKey
```

- In the repository folder run
```bash
./gradlew clean install
./gradlew bintrayUpload
```
