Schowek
=======

simple android PIM.


# Table of contents


BUGS:
-----

- needs testing: backup manager

- main activity: security is nonexistent - JP :)

- main activity: fix changing language when app is already loaded (works except main screen)

- other: PL: add string "reset password to none"

- todo activity: a lot. tags, sorting, filter etc.

- bilans: finish implementing debts activity


Random notes, random roadmap:
-----------------------------

Todo: indicator how much notes are stored for now? allow to sort notes differently than creation time (for example by modification time, or size?), after tags - allow also to search in content, and allow searching via tags. Allow searching via full text.

Todo: test and improve bilans, and prepare at least stub of 5th submodule, shopping list creator (with checkboxes) allowing easy checking standard items to be bought, and perhaps in future integrate it with bilans (predicions, and moving shopping list instantly to bought summary - bilans)

Todo: bilans borrowed/lend should have add (with amount and date), and plain checkbox items to check returned date. 

Todo: birthday / calendar with anniversaries. in very distant future, if ever, if schowek evolves to contain sms archive and contact's db's = map/connect contact to some date. Or item to date. Or item to tag..etc,etc.

Todo: add availability to connect application through socket and write/read DB

Main: Readme is a mess, TOC is nonexistent


# DONE

1. List of android studio required files to be able to import project between various devices
*.gradle files (also app/build.gradle, quite important). Besides obvious app directory. I had a list also what was responsible for what, and i'll update this readme with it.
