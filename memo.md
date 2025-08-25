

---
pr_87修改备忘 20250825：
pr_87链接：https://github.com/catpuppyapp/PuppyGit/pull/87


需要修改：
1. 文案“Quick Links”改成“Links”
   1.2 和最新的main分支合并 （提交 `0a30bd52ad4975efc97a99ad75adf4b958b0cf3a` 创建了links集合，并包含缺失的联系作者等url，以及设置了部分class为private）
2. 暗黑主题下的卡片背景颜色需要调整，不要和页面背景太接近
3. "Links"、"Contributors"等新增的字符串资源添加到strings.xml，不会的语言可使用ai协助翻译
4. 替换about页面中的`forEach`和`forEachIndexed`为 `forEachBetter` 和 `forEachIndexedBetter`，后者可避免某些情况下的并发修改异常（不过这里的list多数是只读的，所以其实无所谓，但最好换下）
5. `animatedColorAsState` 好像和直接改颜色看不出太大区别，可删掉
6. 需要确保开源项目的 LICENSE 可点击打开对应链接
7. 添加 pr_87的贡献者名字和url到contributor集合(可点击pr_87链接查看其名字和主页)
8. app icon 搞大点，图标的内边距可删掉


---
update libgit2 procedure 20250816:
1. replace header files: delete previous and copy latest headers to `src/main/jni/include`
2. copy .so libs to repo for developing: build .so libs on github workflow then put them in to `src/main/jniLibs` (only for development, when build release version, will build the .so libs from source of libgit2 and it's dependencies)


---
为内置Editor关联文件 流程 procedure 20250718：
1 在 `MimeTypeMapCompat.kt` 添加对应的mime
2 在 `MimeTypeIconKt.mimeTypeToIconMap` 关联对应的图标


3 为内置文本编辑器关联文件
方法1 在 `SettingsCons.getEditor_defaultFileAssociationList` 添加文件后缀名：但需要写迁移代码合并用户自定义的扩展名，否则用户若修改过关联列表，必须重置才会包含开发者后来新增的扩展名。但并不推荐写迁移代码，因为如果用户编辑过关联列表，自然能想到再关联新增的类型，我再写迁移代码意义不大，反而没准增加新bug。

方法2 修改Mime类型： 在 `MimeTypeConversionExtensionsKt.mimeTypeToIntentMimeTypeMap` 将文件后缀名转换为 "text/your_type" 类型，内置编辑器会默认打开所有mime为text/*的文件。这种方式的缺点是对用户来说不可控，用户无法通过用户界面取消关联，但这种方法不需要写设置项迁移代码，而且用户总是可通过open as来使用外部app打开文件，所以这种方法也可接受。
