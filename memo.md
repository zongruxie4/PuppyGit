editor支持语法高亮 20250704：
只取text mate分析语法高亮：
完全改用sora editor需要改的东西太多，只拿语法高亮的部分会相对简单，sora editor 使用textmate实现语法高亮，流程大概如下：
sora editor创建text mate language对象后应该就会执行分析，然后分析完会调用receiver，
可调用analyzer manager的inser/delete执行增量更新



支持的语法高亮的语言需要特定配置文件，参见：
https://project-sora.github.io/sora-editor-docs/zh/guide/using-language
其中有语法文件的下载地址




给ppgit的的编辑器应用语法高亮：
可以直接把sora editor设置receiver的代码拿出来，逻辑在sora editor的 CodeEditor.setEditorLanguage() 里
再把span转成annotatedstring，再在我的app里逐行显示，就行了，

可优化：span转AnnotatedString可以修改下，直接返回AnnotatedString，这样就不需要转换了，但这个影响应该不大，可先测试下，如果感觉不卡，就先不用实现

备忘：
需要测试 RTL布局 语法高亮是否会报错，可能需要逆向遍历索引
连笔字ligature，关闭




如果修改sora editor以支持accept ours/theirs buttons:
（未测试）找到`EditorRender`类的`drawRows()`，在那个函数里绘制的行，找到绘制行的部分，在合适的位置画图标，然后记录坐标，然后在`CodeEditor.onTouchEvent()`里判断是否点了对应坐标，如果点了则执行对应操作
p.s. sora editor似乎有留api，可为span（或row?）画extras 内容，用这个api可能会方便加图标，但我不确定是否方便处理点击事件，用canvas画界面，再用点击的offset去判断，我对这种方式不太熟悉，很多东西不确定，所以不想改sora editor的代码，把语法高亮的功能拿出来，放到ppgit里，然后别的就不做了，用户如果期望更复杂的功能，不如用外部编辑器。


---
TODO sha256 support 20250607:
1. waiting for libgit2 sha256 feature become stable
2. UI: add menu item to RepoCard menu to allow users switch hash method, then save settings into .gitconfig (need test if were switched to sha256, then, still can switch back to sha1?)
3. UI: can choose sha256/sha1 when init a folder to git repo, and save to .gitconfig file
4. GUIDE: honor sha256 settings in the .gitconfig file, rather than save settings to db repo table
5. CODE: change Oid class of git24j to libgit2 oid series method binding, else need implement sha256 in java side, may cause bug


中文：
1. 等 libgit2 的sha256功能稳定
2. UI: 仓库卡片添加可切换hash算法的菜单项，并保存到仓库的.gitconfig（需要测试如果切换到 sha256 还能否切换回sha1？）
3. UI: 初始化文件夹为仓库时可选hash算法，并保存到.gitconfig
4. GUIDE: 遵循 .gitconfig 的 sha256 相关选项，把相关设置都存到 .gitcofnig 里，尽量别往 db repo表 存
5. CODE: 修改 git24j 的 Oid 类为libgit2 oid系列函数的binding，不然还得自己写sha256相关的算法，可能导致新的bug

