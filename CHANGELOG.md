1.0.8.7v63 - 20250217:
- add dev mode
- update russian translation (thx @triksterr)
- support commit/unstage/revert items from Diff screen
- optimize match by words for compare files


- 添加开发模式
- 更新俄语翻译 (thx @triksterr)
- 支持从Diff页面提交、取消暂存、恢复条目
- 优化比较文件时的按单词匹配模式

---

1.0.8.6v62 - 20250215:
- fix: file history omitted items


- 修复文件历史页面遗漏条目的bug

---

1.0.8.5v61 - 20250214:
- update tls certs
- fix: commit oid in the file history didn't match the file modified commit oid 


- 更新tls证书
- 修复文件历史页面中的提交oid与文件修改时的提交oid不匹配的bug

---
1.0.8.4v60 - 20250213:
- remember read-only for DiffScreen
- adjust compare method


- 为比较页面记住只读状态
- 调整比较方法

---

1.0.8.3v59 - 20250206:
- update russian translation (thx @triksterr)


- 更新俄语翻译 (感谢 @triksterr)

---

1.0.8.2v58 - 20250203:
- optimize notification method


- 优化通知显示逻辑

---

1.0.8.1v57 - 20250131:
- support auto pull/push when enter/exit specified apps (issue #28)
- support run a http service, it can used for request action by tasker or other apps which can send http request (issue #23)
- will save master password on the local, no longer input required when launching app


- 支持 进入/退出 指定app时自动执行 拉取/推送 (issue #28)
- 支持运行Http服务，可通过Tasker等自动化工具发送http请求以实现某些需求 (issue #23)
- 主密码将保存在本地，启动app时不再需要输入

---

1.0.8.0v56 - 20250125:
- update Repos Screen: disable "Sync" and "Set Upstream" for not-cloned Repo
- fix: copy a file which path contains '.' may failed


- 仓库页面修改：对未克隆仓库禁用“同步”和“设置上游”选项
- 修复拷贝路径包含'.'的文件可能会出错的bug

---

1.0.7.9v55 - 20250124:
- add Status "Need Pull/Need Push" in to Repo Card


- 添加状态 "需要拉取/需要推送" 到仓库卡片

---

1.0.7.8v54 - 20250123:
- indicate repo has uncommitted changes on RepoCard (issue #26)
- support multi selection on Repos page (issue #27)


- 在仓库卡片显示仓库是否有未提交修改 (issue #26)
- 仓库页面支持多选 (issue #27)

---

1.0.7.7v53 - 20250113:
- update Editor: reload file will not clear undo stack


- 更新文本编辑器: 重载文件将不会清空撤销记录

---

1.0.7.6v52 - 20250112:
- support clear upstream for branch

---

1.0.7.5v51 - 20250107:
- fix bug: Files filter by keyword "document" will match all folders
- change BackHandler logic: press back button when filter and selection mode both are enabled, will quit selection first


- 修复文件管理器过滤关键字为"document"时将匹配所有文件夹的bug
- 修改返回键处理逻辑：当过滤模式和选择模式同时开启时，按返回键先退出选择模式

---

1.0.7.4v50 - 20250106:
- show TimeZone info at Commits/Tags/Reflogs/FileHistory if unset timezone for app
- remove "PuppyGit" from auto generated commit msg


- 如果未设置时区则在 提交/标签/引用日志/文件历史 页面显示时区信息
- 从自动生成的提交信息中移除 "PuppyGit"

---
1.0.7.3v49 - 20250105:
- ChangeList: move "Sync" button from BottomBar to commit msg dialog
- support Undo/Redo for Editor
- support stage item on DiffScreen
- update libgit2 to 1.9.0 (fix ssh shallow clone err, issue #10)


中文更新日志：
- 修改列表页面: 把 "同步按钮"从BottomBar挪到了提交信息弹窗
- 文本编辑器支持撤销和重做
- 支持在比较页面(DiffScreen)暂存条目
- 更新 libgit2 版本 1.9.0 (修复ssh浅克隆错误, issue #10)

---

1.0.7.2v48 - 20241231:
english:
- switch dark theme no more restart app required
- fix bug of Diff Screen select compare: when group by line off, select two lines no match will not update target line, but it should update to no match
- fix after rotated screen force back to Home screen (issue #19)
- add timezone settings, default is follow system (issue #20)


chinese:
- 切换主题不需要重启app了
- 修复 Diff Screen 选择比较 bug: 当按行分组关闭时，选择比较两个行（或 某行和剪贴板）后若无匹配则目标行状态不会更新
- 修复旋转屏幕后页面重置到首页的bug (issue #19)
- 添加时区设置，默认跟随系统时区，此设置会影响预览和创建提交、标签等GitObject时的时间字段以及Files页面文件的最后修改时间 (issue #20)

---

1.0.7.1v47 - 20241218:
- make app pass the Reproducible Build check, it used for verify the apk 100% built from open source (issue #13, thx @IzzySoft)
- set jvmTarget from 11 to 17
- bundled git24j src code and fix some bugs

---

1.0.7.0v46 - 20241212:
- update bundled cert

---

1.0.6.9v45 - 20241210:
- Diff Screen support match by words
- Editor changes:
  - add a recent file list
  - use icons instead texts for actions when no file opened (long press icon will got hint of it) 
  - fix bug: keyword exist but not found
- Repos Page layout adjusted for Tablet: now each row repos count will calculating by screen width (each row repos count = screen width / a repo item width) 
- support set a master password instead default password encrypt password/passphrase of your credentials
- support push at commit msg dialog
- support russian (translate by github user @triksterr)

---

1.0.6.8v44 - 20241124:
- support sort for Files (issue: #8)
- adjust Files explorer breadcrumb style
- add fallback mechanism for get external app dir: if try get external app dir failed will try get inner instead
- change android targetSdk to 35
- update UI libs version
- change app chinese name to "小狗Git"(former name "狗狗Git"）

---

1.0.6.7v43 - 20241119:
- fixed when edit repo and don't change the url, the new credential will show username/password even is ssh url(issue https://github.com/catpuppyapp/PuppyGit/issues/5)

---

1.0.6.6v42 - 20241118:
- fixed: after deleting submodule still show it in Submodules Screen
- fixed: on RepoScreen, repo status not update when do act under filter mode

---

1.0.6.5v41 - 20241117:
- DiffScreen: change deleted content color from red to gray

---

1.0.6.4v40 - 20241115:
- CloneScreen: if the git url is not http or https url, will show private key and passphrase instead username and password for the "New Credential"

---

1.0.6.3v39 - 20241115:
- fix some minor bugs
- update dependency libs
    - libgit2 from 1.7.2 to 1.8.4
    - openssl from 3.3.0 to 3.4.0
    - libssh2 from 1.11.0 to 1.11.1
    - git24j from 1.0.3 to 1.0.4

---

1.0.6.2v38 - 20241113:
- support ssh

---

1.0.6.1v37 - 20241111:
- support view file history
- support direct edit line in diff screen
- commit list support filter commits by paths
- support select lines to compare at diff screen, it's useful when similar lines has difference line number

---

1.0.6.0v36 - 20241101:
- fix Difference Screen crash

---

1.0.5.9v35 - 20241031:
- update git24j libs

---

1.0.5.8v34 - 20241030:
- support squash commits
- Improve view differences performance, now very fast

---

1.0.5.7v33 - 20241029:
- Optimize page scroll performance
- Editor, resolve conflict optimize: 
  - ours/theirs now has background color
  - add accept ours/theirs buttons
- can switch Prev/Next File in DiffScreen
- adjust go to top fab:
    - can permanent hide from settings
    - can temporary hide
    - support go to bottom
- fixed bug: commit history list omit commits
- able to copy clone err msg by long pressing the err msg on err repo card
- show last modified time in Files(selected single file) and Editor(clicked title, show file details)

---

1.0.5.6v32 - 20241017:
- fixed few minor bugs
- improved chinese translation

---

1.0.5.5v31 - 20241016:
- rename app name to PuppyGit, no more Pro suffix ever
- support chinese
- add Settings page

---

1.0.5.4v30 - 20241013:<br>
ignore file change:
- change comment start sign form "#" to "//"
- deprecated "ignores.txt", instead by "ignore_v2.txt", if users was use puppy git ignore feature, need re-ignore files

---

1.0.5.3v29 - 20241011:
- fixed Files Page loading very slow
- fixed Files Page open a unreadable dir when first launch app

---

1.0.5.2v28 - 20241011:
- Files page support go to ChangeList or Repos
- other minor bugs fixed

---

1.0.5.1v27 - 20241008:
- support submodules
- support init dir as git repo at Files page
- support ignore files at ChangeList page (git status for worktree to index)

---

1.0.5v26 - 20241005:
- settings file move to user-visible PuppyGit-Data folder
    * NOTICE: if you upgrade app from old version, below settings will lost, you can set it again, will save to new settings:
        * Editor Page: font size/show or hide line number/files last edited positions
        * Global Git username and email
        * Storage Paths
- dozen bugs fixed

---

1.0.4v25:
- important update, unlock all features

---

1.0.3.2v24:
- support clone repo to external storage (need grant manage storage permission, if don't grant, still can clone in to app internal storage like old versions)
- enable better compare method
- enable shallow clone (if make repo corrupted, please re-clone full repo)

---

1.0.3.1v23:
- open source

