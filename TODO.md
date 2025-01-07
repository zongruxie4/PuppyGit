## TODO 20241221
- 支持以 isMerged NotMerged hasUpstream noUpstream等附加条件过滤提交、分支，或者强制把这些东西改成可搜索的英语和供查看的指定语言？例如可通过IsMerged/NotMerged过滤条目，但显示的时候显示为合并而来和非合并而来
- support gif-lfs(should compitable .gitconfig settings)
- sign commit and tag(include rebase/merge/cherrypick generated commits, and can enable or disable respectively)(at least support gpg sign, better ssh as well)(should compitable .gitconfig settings)
- ChangeList/Index/TreeToTree support view by folder and support more sort method(sort by name/modified time, asc/desc just like Files screen)（submodule虽是folder但实际处理起来类似file，普通folder点击可展开/隐藏，点击file前往diff页面，选择模式下点folder自动选择其下所有文件，长按folder也可开启选择模式）
- syntax  highlighting in Editor and Diff screen
- 启动一个http服务器，支持执行拉取推送，并返回是否出错。需要考虑如何保持进程不被杀掉，常驻通知栏或许可行。创建提交的请求必须有一个参数控制是否创允许建空提交，只有允许才能创建空提交否则不能，这样是为了避免执行命令后创建空提交并推送。细节：需要针对每个仓库分别使用各自的公平锁以避免冲突并保持操作顺序与请求顺序一致。
- 实现http服务后，写一个obsidian插件，启动时发送拉取请求给puppygit，关闭时发送推送请求。

---


## TODO (early, maybe some features already implemented)
- show translation and web search and share etc context menu when text selected (may need filter the apps which supportted PROCESS_TEXT intent)
- show process detail when cloning (now only show"Cloning...")
- improve ChangeList load performance
- multi cherry-pick
- optimize Editor performance
- Editor merge mode: add colors for ours/theirs blocks
- Editor merge mode: add accept ours/theirs buttons at conflict block (expect like vscode style)
- highlighting keyword when finding in Editor
- support more encoding for Editor(now only supported utf8)
- view commit history as a interactable tree (the tree should scrollable for vertical and horizontal)
- view history of file(plan: add menu item in Files page single item menu, and Editor page top bar menu. if file is not in a git repo, show a toast)
- Files: support more order methods(order by name, order by date, desc, asc, etc), and each path can have difference order method(path and order method should save to a single config file like file opened history for avoid settings file oversized)
- Files: support override or rename for copy/move when target and src have same name
- ChangeList: support group by folder(try use different status options, if good, no need group by myself)(should expend when clicked a folder, if it is not a submodule or git repo. and can view diff content when clicked a file under a folder. and should make sure it can't stage a folder when it is a git repo under other repo but isn't a submodule)
- ChangeList: support show/hide submodules(try different status opts, if good, no need implement by myself)
- ChangeList: support show folder first(order method)
- add go to bottom for every go to top fab(column layout, upside icon, clicked go to top, bottom icon, clicked go to bottom)
- signed commits (by gpg key)
- support ssh
- git blame
- optional: can disable auto loading when go to ChangeList (for avoid this case: go to ChangeList, but the repo is not you want, and page loading, you can't switch repo...stucked... but actually, have workaround, go to Files or Repos page can direct show ChangeList of selected repo)
- support git lfs
- optional: multi worktree manage(most time users only need 1 worktree, so this feature is low priority and optional)
- support set color for:
  - Editor: conflict ours/theirs block
  - DiffScreen: add/del/context line background
- DiffScreen:
  - support adjust font size
  - support adjust line num size
  - show/hide line num
  - show/hide +/- sign for add/del line
- squash commits
- ChangeList: view difference of all changed files in one page(提供一个入口，点击进入diff页面，可预览所有修改过的文件的diff内容）
- support terminal: provide a git cli by termux, can use ssh or intent to communicate with termux, but, the only sense of this feature is can let termux use credential provided by PuppyGit, in other case, users can use ssh client instead or direct use Termux
- <del> (checked, actually is fine) check code, fix memory leak: I bet this code has many memory leak, because in git24j, many constructors create instance with weak ptr, which hold instance, but never free it </del>
