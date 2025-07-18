
---
为内置Editor关联文件 20250718：
1 在 `MimeTypeMapCompat.kt` 添加对应的mime
2 在 `MimeTypeIconKt.mimeTypeToIconMap` 关联对应的图标
3 在 `SettingsCons.getEditor_defaultFileAssociationList` 添加文件后缀名

其中第3步可替换为修改Mime类型，但不推荐： 在 `MimeTypeConversionExtensionsKt.mimeTypeToIntentMimeTypeMap` 将文件后缀名转换为 "text/your_type" 类型，内置编辑器会默认打开所有mime为text/*的文件。之所以不推荐这种方式是因为这种方式对用户来说不可控，用户无法通过用户界面取消关联，但如果同一mime类型关联特别多的后缀名，则可使用这种方式来减少代码，由于用户总是可通过open as选择外部程序打开文件，所以这种方式虽然不推荐，但也可接受。 
