
---
x 已完成）editor支持语法高亮 20250704：
只取text mate分析语法高亮：
完全改用sora editor需要改的东西太多，只拿语法高亮的部分会相对简单，sora editor 使用textmate实现语法高亮，流程大概如下：
sora editor创建text mate language对象后应该就会执行分析，然后分析完会调用receiver，
可调用analyzer manager的insert/delete执行增量更新



支持的语法高亮的语言需要特定配置文件，参见：
https://project-sora.github.io/sora-editor-docs/zh/guide/using-language
其中有语法文件的下载地址




给ppgit的的编辑器应用语法高亮：
可以直接把sora editor设置receiver的代码拿出来，逻辑在sora editor的 CodeEditor.setEditorLanguage() 里
再把span转成annotatedstring，再在我的app里逐行显示，就行了，

可优化：span转AnnotatedString可以修改下，直接返回AnnotatedString，这样就不需要转换了，但这个影响应该不大，可先测试下，如果感觉不卡，就先不用实现

x 备忘：
x 测了，没报错）需要测试 RTL布局 语法高亮是否会报错，可能需要逆向遍历索引
x 改用不连笔的字体了）连笔字ligature，关闭




如果修改sora editor以支持accept ours/theirs buttons:
（未测试）找到`EditorRender`类的`drawRows()`，在那个函数里绘制的行，找到绘制行的部分，在合适的位置画图标，然后记录坐标，然后在`CodeEditor.onTouchEvent()`里判断是否点了对应坐标，如果点了则执行对应操作
p.s. sora editor似乎有留api，可为span（或row?）画extras 内容，用这个api可能会方便加图标，但我不确定是否方便处理点击事件，用canvas画界面，再用点击的offset去判断，我对这种方式不太熟悉，很多东西不确定，所以不想改sora editor的代码，把语法高亮的功能拿出来，放到ppgit里，然后别的就不做了，用户如果期望更复杂的功能，不如用外部编辑器。



备忘 20250706：
重置语法高亮（mark1）：在 EditorInnerPage 搜："TODO 如果切换了语法高亮，也在这里 reset 为自动检测（打开其他文件则重置）"， 在对应地方添加reset语法高亮为自动检测的代码

editor有至少有3处需调整以实现增量更新语法，尽量简化逻辑，把需要更新的行整个删除，然后再insert新内容：
1 updateField函数
2 插入新行后的函数，split new line那个
3 append内容的函数，就是剪贴板粘贴调用的那个
4 删除函数

调用 MyEditorStyleDelegate 时，styles cache map可公用，但需要在不同时期清空：
1 子页面的styles cache可在AppNavigator的返回函数里清空
2 顶级页面的在切换文件后清空(即 mark1要做的事)
3 顶级页面的在关闭文件后清空

编辑器字体可以考虑换成jet brains的 ttf
如果切换了语法高亮的语言，clear() code editor，然后重新针对当前文件执行分析

x 还是弹吧）考虑要不要把打开过滤模式自动聚焦关闭？弹软键盘有点突兀，但不弹又没法立刻输入。。。。。。想想



text edito state需修改：
append/replace
delete

x 测试：
x 能，之前不能是因为没有 apply color scheme）把 codeEditor.colorscheme = tm theme，那个代码删掉，试试能不能正常分析语法高亮？若能就不需要sora editor的CodeEditor了

---
DONE 20241228:
x - restore Screen after rotated phone (旋转屏幕后恢复导航目标页面以及整个导航栈) (consider use this replace jetpack shit navigator: https://github.com/adrielcafe/voyager)
