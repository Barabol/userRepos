# Spring Boot API

Simple API for fetching github user's repositories \
that are not froks and getting all of that repo branches

# How to use

### Request
```
GET /{userName}
Accept: application/json
```
### Response
```json
[
    {
        "name":"name of repository",
        "owner":"owner of that repository",
        "branches":[
            {
                "name":"name of branch",
                "last_commit_sha":"sha of last commit for that branch"
            }
        ]
    }
]
```
