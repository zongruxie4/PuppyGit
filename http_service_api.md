## Rules of token and ip whitelist:
1. Every request must with token and the ip of client must in the your ip white list.
2. if token or ip list empty, all requests will be rejected
3. if you want to allow all request ip, use '*' to match all
4. if host set to 0.0.0.0 will allow external access, else will only allow request with specified ip

### You can go to `PuppyGit -> Service` to manage your tokens and ip whitelist  

---

## Quick Start:
pull: http://127.0.0.1:52520/pull?token=replace_to_your_token&repoNameOrId=yourRepoName

push: http://127.0.0.1:52520/push?token=replace_to_your_token&repoNameOrId=yourRepoName

pullAll: http://127.0.0.1:52520/pullAll?token=replace_to_your_token

pushAll: http://127.0.0.1:52520/pushAll?token=replace_to_your_token

---

## Apis:
### path: /pull

protocol: http

Http method: GET

params:
- repoNameOrId: repo name or id, match by name first, if none, will match by id
- gitUsername: using for create commit, if not pass this param, will use PuppyGit settings
- gitEmail: using for create commit, if not pass this param, will use PuppyGit settings
- forceUseIdMatchRepo: 1 enable or 0 disable, default 0, if enable, will force match repo by repo id, else will match by name first, if no match, then match by id
- token: a valid token in your token list

example:<br>
http://127.0.0.1/pull?repoNameOrId=your_repo_name&token=your_token

http://127.0.0.1/pull?repoNameOrId=your_repo_name&token=your_token&gitUsername=your_git_username&gitEmail=your_email@example.com



### path: /push

protocol: http

Http method: GET

params:
- repoNameOrId: repo name or id, match by name first, if none, will match by id
- gitUsername: using for create commit, if not pass this param, will use PuppyGit settings
- gitEmail: using for create commit, if not pass this param, will use PuppyGit settings
- force: force push, 1 enable , 0 disable, default 0
- forceUseIdMatchRepo: 1 enable or 0 disable, default 0, if enable, will force match repo by repo id, else will match by name first, if no match, then match by id
- token: a valid token in your token list
- autoCommit: 1 enable or 0 disable, default 1: if enable and no conflict items exists, will auto commit all changes, and will check index, if index empty, will not pushing; if disable, will only do push, no commit changes, no index empty check, no conflict items check.

example:<br>
http://127.0.0.1/push?repoNameOrId=your_repo_name&token=your_token

http://127.0.0.1/push?repoNameOrId=your_repo_name&token=your_token&gitUsername=your_git_user_name&gitEmail=your_email@example.com



### path: /pullAll

protocol: http

Http method: GET

params:
- gitUsername: using for create commit, if not pass this param, will use PuppyGit settings
- gitEmail: using for create commit, if not pass this param, will use PuppyGit settings
- token: a valid token in your token list


example:<br>
http://127.0.0.1/pullAll?token=your_token

http://127.0.0.1/pullAll?token=your_token&gitUsername=your_git_username&gitEmail=your_email@example.com



### path: /pushAll

protocol: http

Http method: GET

params:
- gitUsername: using for create commit, if not pass this param, will use PuppyGit settings
- gitEmail: using for create commit, if not pass this param, will use PuppyGit settings
- force: force push, 1 enable , 0 disable, default 0
- autoCommit: 1 enable or 0 disable, default 1: if enable and no conflict items exists, will auto commit all changes, and will check index, if index empty, will not pushing; if disable, will only do push, no commit changes, no index empty check, no conflict items check.
- token: a valid token in your token list


example:<br>
http://127.0.0.1/pushAll?token=your_token

http://127.0.0.1/pushAll?token=your_token&gitUsername=your_git_username&gitEmail=your_email@example.com
