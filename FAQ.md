## Q: what is the `PuppyGit-Data` folder?
## A: it saved app settings and file snapshot/tls certs/app settings

## Q: How I find the `PuppyGit-Data` folder?
## A: Go to Files Page, click top right 3 dot menu, click `Internal Storage`, then you can find the `PuppyGit-Data` under this folder.

## Q: Can I delete the `PuppyGit-Data` folder?
## A: You can, but better don't do that, it will delete the app settings also, if you want to clear cache of app, go to Setting Screen, and try clear cache

## Q: I enabled snapshot feature for Editor and Diff, How I found the snapshot files?
## A: go to the path `PuppyGit-Data/FileSnapshot`, then you can filter you files by name, and the file name include a timestamp indicate when the file created

## Q: If I disabled the snapshot feature for Diff, and app crashed when I edit lines on the Diff screen, and my origin file got broken, how I restore it?
## A: If your origin file broken, it means app crashing when try overwrite your origin file, so don't worry, it should has a tmp file include the content you was edited in the Diff screen, you can go to the Files screen, then go to `Internal Storage`, press back twice, then you see a `cache` folder, open it, you should see a file name like "diff__RLTF-YourFileName-some_random_str.tmp", then open the file, check the content, if good, you can use it replace your broken file.

## Q: I saw the `EditCache` in Settings Screen, what is used for?
## A: if enable, it will cache your input text into the `PuppyGit-Data/EditCache` folder.

## Q: How I use self-signed cert for https?
## A: put your cert into `PuppyGit-Data/cert-user` and restart app(kill the process by slip it out from the task manager or 'force stop' app in the app information screen)

## Q: What the `master password` used for?
## A: It used to encrypt your credentials, if you forgot it, can reset it, but all password/passphrase of your credentials will be invalid, you must re-set the `password/passphrase` filed for your credentials, else, it still can't decrypt by new master password, so, ofc, the encrypted password can't used for connect remote git repository as well.

## Q: If I don't use the `master password`, is dangerous?
## A: Nah, actually, is not really very dangerous, but I can't promise, even your `password/passphrase` still will encrypted by default password, but if a hacker steal database from your phone, then, he can easy get the default password if he is a good hacker, and when he got the default password, he ofc can decrypt your `password/passphrase`.
