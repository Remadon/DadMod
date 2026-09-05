# DadMod - An Automated, Lightweight, Configurable Mod, to Troll Your Players

<img width="1119" height="249" alt="image" src="https://github.com/user-attachments/assets/51533157-8d77-48fd-a91f-95d589e59190" />

Installation:
Drop it in the mods directory. It expects NeoForge, and minecraft 26.1.2 at this time. More modloaders and older/newer minecraft versions are planned.

Configuration:
There are four things you may configure:
1. replyFormat - This is the string the mod replies with in response to a detected phrase. {name} is a placeholder for the triggering players name.
2. triggerPatterns - This is a list of regular expressions to detect strings. By default, it detects im IM i'm I'm i'M, but not mime, crime, dime, etc.
3. ignoredPlayerUuids - This is a blacklist of players to never respond to. Helpful for operators that want to create an unfair dual standard of Trolls, and Trollees.
4. replyDelayTicks - The number of ticks that the mod will wait to reply.

Future Plans:
1. Add a calendar system, so that you may troll (or not troll) on certain days, automatically.
2. Potentially add /commands to control the blacklist and reply format right away. Maybe the regex and delay time as well.
3. Support chat mods, maybe.
