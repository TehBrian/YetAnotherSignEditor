# YetAnotherSignEditor
Easily edit signs, with support for both MiniMessage and legacy formatting.

* [Downloads](https://github.com/TehBrian/YetAnotherSignEditor/releases/latest)
* [Discord](https://chat.tehbrian.xyz)

## .. Another one?
Yeah yeah, I know. We didn't need another sign editing plugin floating around. I
have good reason for creating this one, though! All the available sign editing
plugins were either outdated, ugly, or they didn't work how I wanted them to.
This plugin is not outdated, not ugly, and it works just how I want it to!

## What makes this plugin better?
Try it out for yourself and see if you like it. I'll list off the reasons that I
would personally use this plugin over others:

* You can either edit signs with a command or directly in the sign interface.
* Each player can individually toggle plugin features on and off.
* Sign coloring and sign editing can be toggled separately.
* You can use either [MiniMessage][MiniMessage] or legacy formatting to format
  sign text.
* Editing a sign reverts text formatting back into plain text for easy editing.
* **All** plugin messages are easily configurable.
* There are separate permissions for sign coloring and sign editing.

Above all, it just feels good to use! No bloat, no extra features.

[MiniMessage]: https://docs.adventure.kyori.net/minimessage.html

## Cool! How do I get it?
First and foremost, you'll need to use Paper. Sorry to anyone who uses Spigot,
but Paper has an API that this plugin requires in order to work. If there's
enough demand, I'll see about porting it to Spigot. ~~or boycotting them till
they add that freakin' API method~~

Once you're using Paper, just drag and drop the plugin jar into your plugins
folder! The jar can be found [here][releases].

[releases]: https://github.com/TehBrian/YetAnotherSignEditor/releases

If you'd like to build the jar yourself, it's quite easy. Just clone the
repository onto your computer, navigate to the project directory, and
run `./gradlew shadowJar`. The built jar should be in `build/libs`!

## What are the permissions and commands?
You can do `/yase` in-game to find a list of all commands. The permissions can
be found [here][plugin.yml].

[plugin.yml]: https://github.com/TehBrian/YetAnotherSignEditor/blob/master/src/main/resources/plugin.yml

## Screenshots?
![help menu](images/help-menu.png)
![commands](images/commands.png)

## Can I contribute?
Sure! If you find a problem or have a suggestion, feel free to file
an [issue](https://github.com/TehBrian/YetAnotherSignEditor/issues). If you
prefer to contribute code, go ahead and submit a pull-request! Please make sure
to follow the `.checkstyle`.
