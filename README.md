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

Notes: find note via *%LIKE%* or something similar.

Bilans: wallet purpose, and UX (somewhat) description. Wallet is supposed to show current state of funds,regardless of location - account, wallet, credit card etc. It should be settable, so we could set desiredamount at any given time, and continue to calculate from this moment - any deductions or incomes should be properly shown, however - if any divergence is found it should be noted and displayed to user - have funds been lost, or just forgotten to update their state, thus became divergent?

Bilans: lacks possibility that was present in previous implementation - adding item not permanently, be it item with varying price each time item is bought or just item user does not want to add permanently to database. Also editing entries just sets timestamp values, but not preselected items count, and adds new entry instead of modyfying old one.

Security: User should have option to completely disable security. In case security is enabled:

 - possibility to reset password
 
 - possibility to temporarily disable password (same as session cookie, for period of X minutes)

Todo activity: filter tags, find via tag, tag manipulation

Todo: indicator how much notes are stored for now? allow to sort notes differently than creation time (for example by modification time, or size?), after tags - allow also to search in content, and allow searching via tags. Allow searching via full text.

Todo: test and improve bilans, and prepare at least stub of 5th submodule, shopping list creator (with checkboxes) allowing easy checking standard items to be bought, and perhaps in future integrate it with bilans (predicions, and moving shopping list instantly to bought summary - bilans)

Todo: bilans borrowed/lend should have add (with amount and date), and plain checkbox items to check returned date. 

Todo: birthday / calendar with anniversaries. in very distant future, if ever, if schowek evolves to contain sms archive and contact's db's = map/connect contact to some date. Or item to date. Or item to tag..etc,etc.

Todo: add availability to connect application through socket and write/read DB

Main: Readme is a mess, TOC is nonexistent


# DONE

1. List of android studio required files to be able to import project between various devices
*.gradle files (also app/build.gradle, quite important). Besides obvious app directory. I had a list also what was responsible for what, and i'll update this readme with it.
